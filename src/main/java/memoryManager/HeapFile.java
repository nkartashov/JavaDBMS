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
	// Not full = green
	// Full = red

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
		PageId firstGreenPageId = new PageId(filePath, FIRST_GREEN_POINTER_PAGE_INDEX);
		byte[] greenPointerPage = _pageManager.createPage(firstGreenPageId);
		PointerPage.initHeader(greenPointerPage);
		_pageManager.updateAndReleasePage(firstGreenPageId, greenPointerPage);


		PageId firstRedPageId = new PageId(filePath, FIRST_GREEN_POINTER_PAGE_INDEX);
		byte[] redPointerPage = _pageManager.createPage(firstRedPageId);
		PointerPage.initHeader(redPointerPage);
		_pageManager.updateAndReleasePage(firstRedPageId, redPointerPage);
	}

	public void insertRow(TableRow row)
	{
		PageId greenPageId = localPageId(getGreenPageIndex());
		RowPage greenPage = new RowPage(_pageManager.getPage(greenPageId), false, _rowSize);
		try
		{
			byte[] byteRow = row.getAsByteArray(_rowSignature);
			greenPage.putRow(byteRow);

			if (greenPage.isFull())
			{
				addRedPointer(greenPageId.getPageNumber());
				removeGreenPointerFromLastPage(greenPageId.getPageNumber());
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(greenPageId, greenPage.rawPage());
		}
	}

	public ArrayList<Object> selectAllRows()
	{
		ArrayList<Object> result = new ArrayList<Object>();
		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		try
		{
			while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				if (!redPointerPage.isEmpty())
					result.addAll(iterateOnAllPointers(redPointerPage.allPointers()));
				_pageManager.releasePage(redPointerPageId);
				redPointerPageId = localPageId(redPointerPage.nextPageIndex());
				redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			}
		}
		finally
		{
			_pageManager.releasePage(redPointerPageId);
		}
		if (!redPointerPage.isEmpty())
			result.addAll(iterateOnAllPointers(redPointerPage.allPointers()));



		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		try
		{
			while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				if (!greenPointerPage.isEmpty())
					result.addAll(iterateOnAllPointers(greenPointerPage.allPointers()));
				_pageManager.releasePage(greenPointerPageId);
				greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
				greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			}
		}
		finally
		{
			_pageManager.releasePage(greenPointerPageId);
		}
		if (!greenPointerPage.isEmpty())
			result.addAll(iterateOnAllPointers(greenPointerPage.allPointers()));

		return result;
	}

	public ArrayList<Object> iterateOnAllPointers(long[] allPointers)
	{
		ArrayList<Object> result = new ArrayList<Object>();

		for (long pageNumber: allPointers)
			result.addAll(selectAllRowsFromPage(pageNumber));

		return result;
	}

	public ArrayList<Object> selectAllRowsFromPage(long pageNumber)
	{
		ArrayList<Object> result = null;
		PageId pageId = localPageId(pageNumber);
		RowPage page = new RowPage(_pageManager.getPage(pageId), false, _rowSize);
		ArrayList<Integer> rowList = null;
		try
		{
			if (!page.isEmpty())
				rowList = page.occupiedRowsList();
			if (rowList == null)
				return result;
			result = new ArrayList<Object>();
			for (Integer rowNumber: rowList)
			{
				byte[] rowData = page.getRow(rowNumber);
				int byteOffset = 0;
				ArrayList<Object> rowAsObject = new ArrayList<Object>();
				for (BaseTableType type : _rowSignature)
				{
					rowAsObject.add(type.getAsObject(rowData, byteOffset, type.size()));
					byteOffset += type.size();
				}
				result.add(rowAsObject);
			}
		}
		finally
		{
			_pageManager.releasePage(pageId);
		}
		return result;
	}

	private long getLastRedPointerPageIndex()
	{
		long result = -1;
		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		try
		{
			while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				_pageManager.releasePage(redPointerPageId);
				redPointerPageId = localPageId(redPointerPage.nextPageIndex());
				redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			}
			result = redPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(redPointerPageId);
		}
		return result;
	}

	private void addRedPointer(long pointer)
	{
		long lastRedPointerPageIndex = getLastRedPointerPageIndex();
		PageId redPointerPageId = localPageId(lastRedPointerPageIndex);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		try
		{
			if (!redPointerPage.isFull())
				redPointerPage.addPointer(pointer);
			else
			{
				PageId newRedPointerPageId = blankLocalPageId();
				PointerPage newRedPointerPage = new PointerPage(_pageManager.createPage(newRedPointerPageId), true);
				try
				{
					newRedPointerPage.addPointer(pointer);
					redPointerPage.setNextPageIndex(newRedPointerPageId.getPageNumber());
				}
				finally
				{
					_pageManager.updateAndReleasePage(newRedPointerPageId, newRedPointerPage.rawPage());
				}
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(redPointerPageId, redPointerPage.rawPage());
		}
	}

	public void removeGreenPointerFromLastPage(long pointer)
	{
		long lastGreenPointerPageIndex = getLastGreenPointerPageIndex();
		PageId greenPointerPageId = localPageId(lastGreenPointerPageIndex);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		try
		{
			greenPointerPage.removePointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(greenPointerPageId, greenPointerPage.rawPage());
		}
	}

	public long getGreenPageIndex()
	{
		long lastGreenPointerPage = getLastGreenPointerPageIndex();

		PageId greenPointerPageId = localPageId(lastGreenPointerPage);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);
		long greenPageIndex = greenPointerPage.getLastPointer();
		if (greenPageIndex != -1)
			return greenPageIndex;
		PageId greenPageId = blankLocalPageId();
		byte[] newPage = _pageManager.createPage(greenPageId);
		RowPage.initHeader(newPage);
		_pageManager.updateAndReleasePage(greenPageId, newPage);
		addGreenPointer(greenPageId.getPageNumber());
		return greenPageId.getPageNumber();
	}

	public long getLastGreenPointerPageIndex()
	{
		long result = -1;
		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		try
		{
			while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
			{
				_pageManager.releasePage(greenPointerPageId);
				greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
				greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			}
			result = greenPointerPageId.getPageNumber();
		}
		finally
		{
			_pageManager.releasePage(greenPointerPageId);
		}

		return result;
	}

	private void addGreenPointer(long pointer)
	{
		long lastGreenPointerPage = getLastGreenPointerPageIndex();
		PageId greenPointerPageId = localPageId(lastGreenPointerPage);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		try
		{
			if (!greenPointerPage.isFull())
				greenPointerPage.addPointer(pointer);
			else
			{
				PageId newGreenPointerPageId = blankLocalPageId();
				PointerPage newGreenPointerPage = new PointerPage(_pageManager.createPage(newGreenPointerPageId), true);
				try
				{
					newGreenPointerPage.addPointer(pointer);
					greenPointerPage.setNextPageIndex(newGreenPointerPageId.getPageNumber());
					newGreenPointerPage.setPrevPageIndex(greenPointerPageId.getPageNumber());
				}
				finally
				{
					_pageManager.updateAndReleasePage(newGreenPointerPageId, newGreenPointerPage.rawPage());
				}
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(greenPointerPageId, greenPointerPage.rawPage());
		}
	}

	private PageId localPageId(long pageIndex) {return new PageId(_filePath, pageIndex);}
	private PageId blankLocalPageId() {return localPageId(0);}

	private String _filePath;
	private ArrayList<BaseTableType> _rowSignature;
	private int _rowSize;
	private static PageManager _pageManager = PageManager.getInstance();

	private static final long FIRST_GREEN_POINTER_PAGE_INDEX = 0;
	private static final long FIRST_RED_POINTER_PAGE_INDEX = 1;
}