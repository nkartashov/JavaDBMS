package memoryManager;

import dbCommands.TableRow;
import tableTypes.BaseTableType;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 06/11/2013
 * Time: 04:23
 * To change this template use File | Settings | File Templates.
 */
public class HeapFile
{
	public HeapFile(String filePath, ArrayList<BaseTableType> rowSignature)
	{
		_filePath = filePath;
		_rowSignature = rowSignature;
		_rowSize = 0;
		for (BaseTableType type: _rowSignature)
			_rowSize += type.size();
	}

	public static void seedDataFile(String filePath)
	{
		PageId firstNotFullPageId = new PageId(filePath, FIRST_NOT_FULL_PAGE_INDEX);
		byte[] firstNotFullRawPage = _pageManager.createPage(firstNotFullPageId);
		if (firstNotFullRawPage == null)
			return;
		PointerPage firstNotFullPage = new PointerPage(firstNotFullRawPage, true);

		_pageManager.updateAndReleasePage(firstNotFullPageId, firstNotFullPage.rawPage());

		PageId secondFullPageId = new PageId(filePath, FIRST_FULL_PAGE_INDEX);
		byte[] secondFullRawPage = _pageManager.createPage(secondFullPageId);
		if (secondFullRawPage == null)
			return;
		PointerPage secondFullPage = new PointerPage(secondFullRawPage, true);

		_pageManager.updateAndReleasePage(secondFullPageId, secondFullPage.rawPage());
	}

	public void insertRow(TableRow row)
	{
		PageId notFullPageId = localPageId(getNotFullPageIndex());
		RowPage notFullPage = new RowPage(_pageManager.getPage(notFullPageId), false, _rowSize);

		try
		{
			byte[] byteRow = row.getAsByteArray(_rowSignature);
			notFullPage.putRow(byteRow);

			if (notFullPage.isFull())
			{
				addFullPointer(notFullPageId.getPageNumber());
				removeNotFullPointerFromLastPage(notFullPageId.getPageNumber());
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(notFullPageId, notFullPage.rawPage());
		}
	}

	public ArrayList<Object> selectRow()
	{
		ArrayList<Object> result = null;
		PageId notEmptyPageId = null;

		PageId fullPointerPageId = localPageId(FIRST_FULL_PAGE_INDEX);
		PointerPage fullPointerPage = new PointerPage(_pageManager.getPage(fullPointerPageId), false);

		try
		{
			if (!fullPointerPage.isEmpty())
				notEmptyPageId = localPageId(fullPointerPage.getLastPointer());
		}
		finally
		{
			_pageManager.releasePage(fullPointerPageId);
		}


		if (notEmptyPageId == null)
		{
			PageId notFullPointerPageId = localPageId(FIRST_NOT_FULL_PAGE_INDEX);
			PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);

			try
			{
				if (!notFullPointerPage.isEmpty())
					notEmptyPageId = localPageId(notFullPointerPage.getLastPointer());
			}
			finally
			{
				_pageManager.releasePage(fullPointerPageId);
			}

		}

		if (notEmptyPageId != null)
		{
			RowPage notEmptyPage = new RowPage(_pageManager.getPage(notEmptyPageId), false, _rowSize);

			try
			{
				if (!notEmptyPage.isEmpty())
				{
					byte[] rowData = notEmptyPage.getRow(notEmptyPage.firstOccupiedRowIndex());
					int byteOffset = 0;

					result = new ArrayList<Object>();

					for (BaseTableType type : _rowSignature)
					{
						result.add(type.getAsObject(rowData, byteOffset, type.size()));
						byteOffset += type.size();
					}
				}
			}
			finally
			{
				_pageManager.releasePage(notEmptyPageId);
			}
		}

		return result;
	}

	private long getLastPointerFullPageIndex()
	{
		PageId fullPointerPageId = localPageId(FIRST_FULL_PAGE_INDEX);
		PointerPage fullPointerPage = new PointerPage(_pageManager.getPage(fullPointerPageId), false);

		try
		{
			while (fullPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				_pageManager.releasePage(fullPointerPageId);
				fullPointerPageId = localPageId(fullPointerPage.nextPageIndex());
				fullPointerPage = new PointerPage(_pageManager.getPage(fullPointerPageId), false);
			}
			return fullPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(fullPointerPageId);
		}
	}

	private void addFullPointer(long pointer)
	{
		long lastFullPointerPageIndex = getLastPointerFullPageIndex();

		PageId fullPointerPageId = localPageId(lastFullPointerPageIndex);
		PointerPage fullPointerPage = new PointerPage(_pageManager.getPage(fullPointerPageId), false);

		try
		{
			if (!fullPointerPage.isFull())
				fullPointerPage.addPointer(pointer);
			else
			{
				PageId newFullPointerPageId = blankLocalPageId();
				PointerPage newPage = new PointerPage(_pageManager.createPage(newFullPointerPageId), true);

				try
				{
					newPage.addPointer(pointer);
					fullPointerPage.setNextPageIndex(newFullPointerPageId.getPageNumber());
				}
				finally
				{
					_pageManager.updateAndReleasePage(newFullPointerPageId, newPage.rawPage());
				}
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(fullPointerPageId, fullPointerPage.rawPage());
		}
	}

	public void removeNotFullPointerFromLastPage(long pointer)
	{
		long lastPointerNotFullPageIndex = getLastPointerFullPageIndex();
		PageId notFullPointerPageId = localPageId(lastPointerNotFullPageIndex);
		PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);

		try
		{
			notFullPointerPage.removePointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(notFullPointerPageId, notFullPointerPage.rawPage());
		}
	}

	public long getNotFullPageIndex()
	{
		long lastNotFullPointerPage = getNotFullPointerPage();

		PageId notFullPointerPageId = localPageId(lastNotFullPointerPage);
		PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);
		_pageManager.releasePage(notFullPointerPageId);

		long notFullPageIndex = notFullPointerPage.getLastPointer();
		if (notFullPageIndex != -1)
			return notFullPageIndex;

		PageId notFullPageId = blankLocalPageId();
		byte[] newPage = _pageManager.createPage(notFullPageId);
		RowPage.initHeader(newPage);
		_pageManager.updateAndReleasePage(notFullPageId, newPage);

		addNotFullPointer(notFullPageId.getPageNumber());

		return notFullPageId.getPageNumber();
	}


	public long getNotFullPointerPage()
	{
		PageId notFullPointerPageId = localPageId(FIRST_NOT_FULL_PAGE_INDEX);
		PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);

		try
		{
			while (notFullPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				_pageManager.releasePage(notFullPointerPageId);
				notFullPointerPageId = localPageId(notFullPointerPage.nextPageIndex());
				notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);
			}
			return notFullPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(notFullPointerPageId);
		}
	}

	private void addNotFullPointer(long pointer)
	{
		long lastNotFullPointerPage = getNotFullPointerPage();

		PageId notFullPointerPageId = localPageId(lastNotFullPointerPage);
		PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);

		try
		{
			if (!notFullPointerPage.isFull())
				notFullPointerPage.addPointer(pointer);
			else
			{
				PageId newNotFullPointerPageId = blankLocalPageId();
				PointerPage newPage = new PointerPage(_pageManager.createPage(newNotFullPointerPageId), true);

				try
				{
					newPage.addPointer(pointer);
					notFullPointerPage.setNextPageIndex(newNotFullPointerPageId.getPageNumber());
				}
				finally
				{
					_pageManager.updateAndReleasePage(newNotFullPointerPageId, newPage.rawPage());
				}
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(notFullPointerPageId, notFullPointerPage.rawPage());
		}
	}

	private PageId localPageId(long pageIndex) {return new PageId(_filePath, pageIndex);}

	private PageId blankLocalPageId() {return localPageId(0);}


	private String _filePath;
	private ArrayList<BaseTableType> _rowSignature;
	private int _rowSize;
	private static PageManager _pageManager = PageManager.getInstance();

	private long _lastPointerNotFullPageIndex = DiskPage.NULL_PTR;
	private long _lastPointerFullPageIndex = DiskPage.NULL_PTR;
	private static final long FIRST_NOT_FULL_PAGE_INDEX = 0;
	private static final long FIRST_FULL_PAGE_INDEX = 1;
}
