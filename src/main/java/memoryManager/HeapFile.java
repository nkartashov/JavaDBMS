package memoryManager;

import dbCommands.RowPredicate;
import dbCommands.TableRow;
import tableTypes.Column;

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

	public HeapFile(String filePath, List<Column> rowSignature)
	{
		_filePath = filePath;
		_rowSignature = rowSignature;
		_rowSize = 0;
		for (Column column: _rowSignature)
			_rowSize += column.size();
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

	public void deleteRows(RowPredicate predicate)
	{
		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);

		while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!greenPointerPage.isEmpty())
				deleteAllPointers(greenPointerPage.allPointers(), predicate, false);

			greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
			greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			_pageManager.releasePage(greenPointerPageId);
		}

		if (!greenPointerPage.isEmpty())
			deleteAllPointers(greenPointerPage.allPointers(), predicate, false);

		PageId redPointerPageId = localPageId(FIRST_RED_POINTER_PAGE_INDEX);
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		_pageManager.releasePage(redPointerPageId);

		while (redPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			if (!redPointerPage.isEmpty())
			{
				List<Long> pointersToMove = deleteAllPointers(redPointerPage.allPointers(), predicate, true);
				for (Long pointer: pointersToMove)
				{
					removePointer(redPointerPageId.getPageNumber(), pointer);
					addGreenPointer(pointer);
				}
			}

			redPointerPageId = localPageId(redPointerPage.nextPageIndex());
			redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
			_pageManager.releasePage(redPointerPageId);
		}

		if (!redPointerPage.isEmpty())
		{
			List<Long> pointersToMove = deleteAllPointers(redPointerPage.allPointers(), predicate, true);
			for (Long pointer: pointersToMove)
			{
				removePointer(redPointerPageId.getPageNumber(), pointer);
				addGreenPointer(pointer);
			}
		}
	}

	private List<Long> deleteAllPointers(long[] allPointers, RowPredicate predicate, boolean redPage)
	{
		List<Long> result = new ArrayList<Long>();
		for (long pageNumber: allPointers)
		{
			boolean deletionsHappened = deleteAllRowsFromPage(pageNumber, predicate);
			if (redPage && deletionsHappened)
				result.add(pageNumber);
		}
		return result;
	}

	private boolean deleteAllRowsFromPage(long pageNumber, RowPredicate predicate)
	{
		boolean deletionsHappened = false;
		PageId pageId = localPageId(pageNumber);
		RowPage page = new RowPage(_pageManager.getPage(pageId), false, _rowSize);
		try
		{
			ArrayList<Integer> rowList = null;
			if (!page.isEmpty())
				rowList = page.occupiedRowsList();
			if (rowList == null)
				return deletionsHappened;
			for (Integer rowNumber: rowList)
			{
				List<Object> rowAsObject = selectRowFromPage(pageNumber, rowNumber);
				if (predicate != null)
				{
					if (predicate.evaluate(rowAsObject))
					{
						page.deleteRow(rowNumber);
						deletionsHappened = true;
					}
				}
				else
				{
					page.deleteRow(rowNumber);
					deletionsHappened = true;
				}
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(pageId, page.rawPage());
		}
		return deletionsHappened;
	}

	public void insertRow(TableRow row)
	{
		PageId notEmptyGreenPointerPageId = localPageId(getNotEmptyGreenPointerPageIndex());
		PointerPage notEmptyGreenPointerPage = new PointerPage(_pageManager.getPage(notEmptyGreenPointerPageId), false);

		PageId greenPageId = localPageId(notEmptyGreenPointerPage.firstGreenPageIndex());
		RowPage greenPage = new RowPage(_pageManager.getPage(greenPageId), false, _rowSize);
		boolean pointerPageHasBecomeFull = false;
		try
		{
			byte[] byteRow = row.getAsByteArray(_rowSignature);
			greenPage.putRow(byteRow);

			if (greenPage.isFull())
			{
				pointerPageHasBecomeFull = true;
				addRedPointer(greenPageId.getPageNumber());
				notEmptyGreenPointerPage.removePointer(greenPageId.getPageNumber());
			}
		}
		finally
		{
			_pageManager.updateAndReleasePage(greenPageId, greenPage.rawPage());
			if (pointerPageHasBecomeFull)
				_pageManager.updateAndReleasePage(notEmptyGreenPointerPageId, notEmptyGreenPointerPage.rawPage());
			else
				_pageManager.releasePage(notEmptyGreenPointerPageId);
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
			_pageManager.updateAndReleasePage(pageId, page.rawPage());
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

		if (count != -1 && result.size() >= count)
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

		if (count != -1 && result.size() >= count)
			return result.subList(0, count);

		return result;
	}

	private List<Object> selectFromAllPointers(long[] allPointers, RowPredicate predicate)
	{
		ArrayList<Object> result = new ArrayList<Object>();

		for (long pageNumber: allPointers)
		{
			List<Object> intermediateResult = selectAllRowsFromPage(pageNumber, predicate);
			if (intermediateResult != null)
				result.addAll(intermediateResult);
		}

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
		for (Column column : _rowSignature)
		{
			result.add(column.type().getAsObject(rowData, byteOffset, column.size()));
			byteOffset += column.size();
		}

		return result;
	}

	private long getNotFullRedPointerPageIndex()
	{
		return getNotFullPointerPageIndexStarting(FIRST_RED_POINTER_PAGE_INDEX);
	}

	private void addRedPointer(long pointer)
	{
		PageId redPointerPageId = localPageId(getNotFullRedPointerPageIndex());
		PointerPage redPointerPage = new PointerPage(_pageManager.getPage(redPointerPageId), false);
		try
		{
			redPointerPage.addPointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(redPointerPageId, redPointerPage.rawPage());
		}
	}

	private long getNotFullGreenPointerPageIndex()
	{
		return getNotFullPointerPageIndexStarting(FIRST_GREEN_POINTER_PAGE_INDEX);
	}

	private void addGreenPointer(long pointer)
	{
		PageId greenPointerPageId = localPageId(getNotFullGreenPointerPageIndex());
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		try
		{
			greenPointerPage.addPointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(greenPointerPageId, greenPointerPage.rawPage());
		}
	}

	private long getNotEmptyGreenPointerPageIndex()
	{
		PageId greenPointerPageId = localPageId(FIRST_GREEN_POINTER_PAGE_INDEX);
		PointerPage greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
		_pageManager.releasePage(greenPointerPageId);

		if (!greenPointerPage.isEmpty())
			return greenPointerPageId.getPageNumber();

		while (greenPointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			greenPointerPageId = localPageId(greenPointerPage.nextPageIndex());
			greenPointerPage = new PointerPage(_pageManager.getPage(greenPointerPageId), false);
			_pageManager.releasePage(greenPointerPageId);

			if (!greenPointerPage.isEmpty())
				return greenPointerPageId.getPageNumber();
		}

		PageId greenPageId = blankLocalPageId();
		byte[] newPage = _pageManager.createPage(greenPageId);
		RowPage.initHeader(newPage);
		_pageManager.updateAndReleasePage(greenPageId, newPage);

		addPointer(greenPointerPageId.getPageNumber(), greenPageId.getPageNumber());

		return greenPointerPageId.getPageNumber();
	}

	private long getNotFullPointerPageIndexStarting(long starting)
	{
		PageId pointerPageId = localPageId(starting);
		PointerPage pointerPage = new PointerPage(_pageManager.getPage(pointerPageId), false);
		_pageManager.releasePage(pointerPageId);

		if (!pointerPage.isFull())
			return pointerPageId.getPageNumber();

		while (pointerPage.nextPageIndex() != DiskPage.NULL_PTR)
		{
			pointerPageId = localPageId(pointerPage.nextPageIndex());
			pointerPage = new PointerPage(_pageManager.getPage(pointerPageId), false);
			_pageManager.releasePage(pointerPageId);

			if (!pointerPage.isFull())
				return pointerPageId.getPageNumber();
		}

		return addNewPointerPage(pointerPageId.getPageNumber());
	}

	private long addNewPointerPage(long lastPointerPageIndex)
	{
		PageId lastPointerPageId = localPageId(lastPointerPageIndex);
		PointerPage lastPointerPage = new PointerPage(_pageManager.getPage(lastPointerPageId), false);

		PageId newPointerPageId = blankLocalPageId();
		PointerPage newPointerPage = new PointerPage(_pageManager.createPage(newPointerPageId), true);
		try
		{
			lastPointerPage.setNextPageIndex(newPointerPageId.getPageNumber());
			newPointerPage.setPrevPageIndex(lastPointerPageId.getPageNumber());
		}
		finally
		{
			_pageManager.updateAndReleasePage(newPointerPageId, newPointerPage.rawPage());
			_pageManager.updateAndReleasePage(lastPointerPageId, lastPointerPage.rawPage());
		}

		return newPointerPageId.getPageNumber();
	}

	private void addPointer(long pointerPageIndex, long pointer)
	{
		PageId pointerPageId = localPageId(pointerPageIndex);
		PointerPage pointerPage = new PointerPage(_pageManager.getPage(pointerPageId), false);
		try
		{
			pointerPage.addPointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(pointerPageId, pointerPage.rawPage());
		}
	}

	private void removePointer(long pointerPageIndex, long pointer)
	{
		PageId pointerPageId = localPageId(pointerPageIndex);
		PointerPage pointerPage = new PointerPage(_pageManager.getPage(pointerPageId), false);
		try
		{
			pointerPage.removePointer(pointer);
		}
		finally
		{
			_pageManager.updateAndReleasePage(pointerPageId, pointerPage.rawPage());
		}
	}

	private PageId localPageId(long pageIndex) {return new PageId(_filePath, pageIndex);}
	private PageId blankLocalPageId() {return localPageId(0);}

	private String _filePath;
	private List<Column> _rowSignature;
	private int _rowSize;
	private static PageManager _pageManager = PageManager.getInstance();

	private static final long FIRST_GREEN_POINTER_PAGE_INDEX = 0;
	private static final long FIRST_RED_POINTER_PAGE_INDEX = 1;
	private static final int INFINITY = Integer.MAX_VALUE;
}