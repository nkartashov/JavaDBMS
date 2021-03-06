package memoryManager;

import utils.BitArray;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 30/10/2013
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class RowPage extends DiskPage
{

	public RowPage(byte[] rawData, boolean blankPage, int rowSize)
	{
		super(rawData, blankPage);

		_rowSize = rowSize;
		_rowsOnPage = min(DATA_PAGE_SIZE_IN_BYTES / _rowSize, MAX_ROWS_ON_PAGE);
		_isOccupied = new BitArray(BIT_MASK_SIZE_IN_BITS);

		if (!blankPage)
		{
			_isOccupied = BitArray.readBitArray(_rawPage, BIT_MASK_OFFSET, BIT_MASK_SIZE_IN_BITS);
		}
	}

	public byte[] getRow(int rowId)
	{
		return readData(rowId * _rowSize, _rowSize);
	}

	public void setRow(int rowId, byte[] rowData)
	{
		writeData(rowId * _rowSize, rowData, 0, _rowSize);
	}

	public void deleteRow(int rowId)
	{
		_isOccupied.clear(rowId);
		updateOccupiedSet();
	}

	public boolean isFull()
	{
		return _isOccupied.cardinality() == _rowsOnPage;
	}

	public boolean isEmpty()
	{
		return _isOccupied.cardinality() == 0;
	}

	public boolean isRowOccupied(int rowId)
	{
		return _isOccupied.isSet(rowId);
	}

	public List<Integer> occupiedRowsList()
	{
		List<Integer> result = new ArrayList<Integer>();

		int i = -1;

		while(true)
		{
			i = _isOccupied.nextSetBit(i + 1);
			if (i == -1)
				break;
			result.add(i);
		}

		return result;
	}

	public int firstEmptyRowIndex()
	{
		return _isOccupied.nextClearBit(0);
	}

	public int firstOccupiedRowIndex()
	{
		return _isOccupied.nextSetBit(0);
	}

	public void putRow(byte[] rowData)
	{
		int index = firstEmptyRowIndex();
		setRow(index, rowData);
		_isOccupied.set(index);
		updateOccupiedSet();
	}

	public int getRowsCount() {return _isOccupied.cardinality();}

	private void updateOccupiedSet()
	{
		_isOccupied.writeBitArray(_rawPage, BIT_MASK_OFFSET);
	}

	private BitArray _isOccupied;
	private int _rowSize;
	private int _rowsOnPage;


	private static final int BIT_MASK_OFFSET = 16;
	private static final int BIT_MASK_SIZE_IN_BITS = 400;
	private static final int BIT_MASK_SIZE_IN_BYTES = BIT_MASK_SIZE_IN_BITS / 8;
	private static final int MAX_ROWS_ON_PAGE = DATA_PAGE_SIZE_IN_BYTES / 4; // size of int
}
