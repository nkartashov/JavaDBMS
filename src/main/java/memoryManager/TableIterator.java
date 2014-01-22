package memoryManager;

import dbCommands.RowPredicate;
import dbEnvironment.DbContext;
import tableTypes.Table;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 22/01/2014
 * Time: 15:11
 * To change this template use File | Settings | File Templates.
 */
public class TableIterator
{
	public TableIterator(DbContext context, String tableName)
	{
		Table tableToSelectFrom = context.getTableByName(tableName);

		_heapFile = new HeapFile(context.getLocation() + tableToSelectFrom.getRelativeDataPath(),
			tableToSelectFrom.rowSignature());

		_currentRow = -1;
		_nextPointerPageIndex = DiskPage.NULL_PTR;
		_status = RED_PAGES;
		_currentRows = null;
	}

	public List<Object> nextRow()
	{
		if (_currentRows == null || _currentRow == _currentRows.size())
			nextPage();
		if (isFinished())
			return null;
		return (List<Object>) _currentRows.get(_currentRow++);
	}

	public boolean isFinished() {return _status == FINISHED && _currentRow == _currentRows.size();}

	private void nextPage()
	{
		if (_pointers == null || _currentPointerIndex == _pointers.length)
		{
			_pointers = null;
			while (_pointers == null)
			{
				if (_nextPointerPageIndex == DiskPage.NULL_PTR)
					switch (_status)
					{
						case RED_PAGES:
							firstRedPage();
							break;
						case GREEN_PAGES:
							firstGreenPage();
							break;
						case FINISHED:
							return;
					}

				updatePointersAndNextPageIndex();

				if (_nextPointerPageIndex == DiskPage.NULL_PTR)
				{
					switch (_status)
					{
						case RED_PAGES:
							_status = GREEN_PAGES;
							break;
						case GREEN_PAGES:
							_status = FINISHED;
							break;
					}
					break;
				}
			}
		_currentPointerIndex = 0;
		}
		_currentRows = _heapFile.selectAllRowsFromPage(_pointers[_currentPointerIndex++], RowPredicate.TRUE_PREDICATE);
		_currentRow = 0;
	}

	private void updatePointersAndNextPageIndex()
	{
		PointerPage page = new PointerPage(_heapFile.getLocalPage(_nextPointerPageIndex), DiskPage.NOT_BLANK_PAGE);
		_heapFile.releasePage(_nextPointerPageIndex);
		_pointers = page.allPointers();
		_nextPointerPageIndex = page.nextPageIndex();
	}

	private void firstRedPage()
	{
		_nextPointerPageIndex = HeapFile.FIRST_RED_POINTER_PAGE_INDEX;
	}

	private void firstGreenPage()
	{
		_nextPointerPageIndex = HeapFile.FIRST_GREEN_POINTER_PAGE_INDEX;
	}

	public int status() {return _status;}

	public static final int RED_PAGES = -1;
	public static final int GREEN_PAGES = -2;
	public static final int FINISHED = -3;


	private int _currentPointerIndex;
	private long _nextPointerPageIndex;
	private int _status = RED_PAGES;
	private long[] _pointers;
	private int _currentRow;

	private HeapFile _heapFile;
	private List<Object> _currentRows;
}
