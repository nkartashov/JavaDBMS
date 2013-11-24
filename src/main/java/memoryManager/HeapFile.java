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


	public void insertRows(ArrayList<TableRow> rows)
	{
		PageId notFullPage  = localPageId(getLastNotFullPageIndex());


	}

	public static void seedDataFile(String filePath, ArrayList<BaseTableType> rowSignature)
	{
		PageId firstNotFullPageId = new PageId(filePath, FIRST_NOT_FULL_PAGE_INDEX);
		byte[] firstNotFullRawPage = _pageManager.createPage(firstNotFullPageId);
		PointerPage firstNotFullPage = new PointerPage(firstNotFullRawPage, true);

		_pageManager.updateAndReleasePage(firstNotFullPageId, firstNotFullPage.rawPage());

		PageId secondFullPageId = new PageId(filePath, FIRST_FULL_PAGE_INDEX);
		byte[] secondFullRawPage = _pageManager.createPage(secondFullPageId);
		PointerPage secondFullPage = new PointerPage(secondFullRawPage, true);

		_pageManager.updateAndReleasePage(secondFullPageId, secondFullPage.rawPage());
	}

	private void insertRow(TableRow row)
	{
		PageId notFullPointerPageId = localPageId(_lastPointerNotFullPageIndex);
		RowPage notFullPage = new RowPage(_pageManager.getPage(notFullPointerPageId), false, _rowSize);

		try
		{
			ArrayList<byte[]> byteRow = row.getAsByteArray(_rowSignature);

		}
		finally
		{
			_pageManager.updateAndReleasePage(notFullPointerPageId, notFullPage.rawPage());
		}
	}


	private long getLastNotFullPageIndex()
	{
		if (_lastPointerNotFullPageIndex == DiskPage.NULL_PTR)
			getLastPointerNotFullPageIndex();

		PageId notFullPointerPageId = localPageId(_lastPointerNotFullPageIndex);
		PointerPage notFullPointerPage = new PointerPage(_pageManager.getPage(notFullPointerPageId), false);
		_pageManager.releasePage(notFullPointerPageId);

		long notFullPageIndex = notFullPointerPage.getLastPointer();
		if (notFullPageIndex != -1)
			return notFullPageIndex;

		PageId notFullPageId = blankLocalPageId();
		byte[] newPage = _pageManager.createPage(notFullPageId);
		RowPage.initHeader(newPage);
		_pageManager.updateAndReleasePage(notFullPageId, newPage);

		addNotFullPointer(notFullPointerPageId.getPageNumber());

		return notFullPageId.getPageNumber();
	}

	private long getLastFullPageIndex()
	{
		if (_lastPointerFullPageIndex == DiskPage.NULL_PTR)
			getLastPointerFullPageIndex();

		PageId fullPointerPageId = localPageId(_lastPointerFullPageIndex);
		PointerPage fullPointerPage = new PointerPage(_pageManager.getPage(fullPointerPageId), false);
		_pageManager.releasePage(fullPointerPageId);

		return fullPointerPage.getLastPointer();
	}

	private void getLastPointerNotFullPageIndex()
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
			_lastPointerNotFullPageIndex = notFullPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(notFullPointerPageId);
		}
	}

	private void getLastPointerFullPageIndex()
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
			_lastPointerFullPageIndex = fullPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(fullPointerPageId);
		}
	}

	private void addNotFullPointer(long pointer)
	{
		PageId notFullPointerPageId = localPageId(_lastPointerNotFullPageIndex);
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

	private void addFullPointer(long pointer)
	{
		PageId fullPointerPageId = localPageId(_lastPointerFullPageIndex);
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
