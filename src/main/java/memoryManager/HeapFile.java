package memoryManager;

import dbCommands.RowPredicate;
import dbCommands.TableRow;
import tableTypes.BaseTableType;

import java.util.ArrayList;
import java.util.List;

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

	public void updateRows(RowPredicate predicate, TableRow row)
	{
		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		_pageManager.releasePage(redPointerPageId);

		while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!redPointerPage.isEmpty())
				updateAllPointers(redPointerPage.allPointers(), predicate, row);

			redPointerPageId = localPageId(redPointerPage.nextPageIndex());
			redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			_pageManager.releasePage(redPointerPageId);
		}


		if (!redPointerPage.isEmpty())
			updateAllPointers(redPointerPage.allPointers(), predicate, row);

		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);

		while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!greenPointerPage.isEmpty())
				updateAllPointers(greenPointerPage.allPointers(), predicate, row);

			greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
			greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			_pageManager.releasePage(greenPointerPageId);
		}

		if (!greenPointerPage.isEmpty())
			updateAllPointers(greenPointerPage.allPointers(), predicate, row);
	}

	private void updateAllPointers(long[] allPointers, RowPredicate predicate, TableRow row)
	{
		for (long pageNumber: allPointers)
			updateAllRowsFromPage(pageNumber, predicate, row);
	}

	private void updateAllRowsFromPage(long pageNumber, RowPredicate predicate, TableRow row)
	{
		byte[] byteRow = row.getAsByteArray(_rowSignature);

		PageId pageId = localPageId(pageNumber);
		RowPage page = new RowPage(_pageManager.getPage(pageId), false, _rowSize);
		try
		{
			ArrayList<Integer> rowList = null;
			if (!page.isEmpty())
				rowList = page.occupiedRowsList();
			if (rowList == null)
				return;
			for (Integer rowNumber: rowList)
			{
				List<Object> rowAsObject = selectRowFromPage(pageNumber, rowNumber);
				if (predicate != null)
				{
					if (predicate.evaluate(rowAsObject))
						page.setRow(rowNumber, byteRow);

				}
				else
					page.setRow(rowNumber, byteRow);
			}
		}
		finally
		{
			_pageManager.releasePage(pageId);
		}
	}




	public List<Object> selectAllRows()
	{
		return selectWhere(null, INFINITY);
	}

	public List<Object> selectWhere(RowPredicate predicate, int count)
	{
		List<Object> result = new ArrayList<Object>();
		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		_pageManager.releasePage(redPointerPageId);

		while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!redPointerPage.isEmpty())
				result.addAll(selectFromAllPointers(redPointerPage.allPointers(), predicate));

			if (result.size() >= count)
				return result.subList(0, count);

			redPointerPageId = localPageId(redPointerPage.nextPageIndex());
			redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			_pageManager.releasePage(redPointerPageId);
		}


		if (!redPointerPage.isEmpty())
			result.addAll(selectFromAllPointers(redPointerPage.allPointers(), predicate));

		if (result.size() >= count)
			return result.subList(0, count);

		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);

		while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!greenPointerPage.isEmpty())
				result.addAll(selectFromAllPointers(greenPointerPage.allPointers(), predicate));

			if (result.size() >= count)
				return result.subList(0, count);

			greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
			greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			_pageManager.releasePage(greenPointerPageId);
		}

		if (!greenPointerPage.isEmpty())
			result.addAll(selectFromAllPointers(greenPointerPage.allPointers(), predicate));

		if (result.size() >= count)
			return result.subList(0, count);

		return result;
	}

	private List<Object> selectFromAllPointers(long[] allPointers, RowPredicate predicate)
	{
		ArrayList<Object> result = new ArrayList<Object>();

		for (long pageNumber: allPointers)
			result.addAll(selectAllRowsFromPage(pageNumber, predicate));

		return result;
	}

	private List<Object> selectAllRowsFromPage(long pageNumber, RowPredicate predicate)
	{
		List<Object> result = null;
		PageId pageId = localPageId(pageNumber);
		RowPage page = new RowPage(_pageManager.getPage(pageId), false, _rowSize);
		_pageManager.releasePage(pageId);
		ArrayList<Integer> rowList = null;
		if (!page.isEmpty())
			rowList = page.occupiedRowsList();
		if (rowList == null)
			return result;
		result = new ArrayList<Object>();
		for (Integer rowNumber: rowList)
		{
			List<Object> rowAsObject = selectRowFromPage(pageNumber, rowNumber);
			if (predicate != null)
			{
				if (predicate.evaluate(rowAsObject))
					result.add(rowAsObject);
			}
			else
				result.add(rowAsObject);
		}

		return result;
	}

	private List<Object> selectRowFromPage(long pageNumber, int rowNumber)
	{
		List<Object> result;
		PageId pageId = localPageId(pageNumber);
		RowPage page = new RowPage(_pageManager.getPage(pageId), false, _rowSize);
		_pageManager.releasePage(pageId);

		byte[] rowData = page.getRow(rowNumber);
		int byteOffset = 0;
		result = new ArrayList<Object>();
		for (BaseTableType type : _rowSignature)
		{
			result.add(type.getAsObject(rowData, byteOffset, type.size()));
			byteOffset += type.size();
		}


		return result;
	}

	private long getLastRedPointerPageIndex()
	{
		long result;
		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		_pageManager.releasePage(redPointerPageId);

		while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{

			redPointerPageId = localPageId(redPointerPage.nextPageIndex());
			redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			_pageManager.releasePage(redPointerPageId);
		}
		result = redPointerPageId.getPageNumber();

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

	private void removeGreenPointerFromLastPage(long pointer)
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

	private long getGreenPageIndex()
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

	private long getLastGreenPointerPageIndex()
	{
		long result = -1;
		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);
		while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{

			greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
			greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			_pageManager.releasePage(greenPointerPageId);
		}
		result = greenPointerPageId.getPageNumber();


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
	private static final int INFINITY = Integer.MAX_VALUE;
}