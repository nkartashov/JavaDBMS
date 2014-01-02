package memoryManager;

import utils.BitArray;
import utils.ByteIterator;

import java.util.ArrayList;
import java.util.BitSet;

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
			_isOccupied = BitArray.readBitArray(_rawPage, BIT_MASK_OFFSET, BIT_MASK_SIZE_IN_BYTES);
		}
	}

	public byte[] getRow(int rowId)
	{
		int requestedRowOffset = DATA_OFFSET + rowId * _rowSize;
		byte[] result = new byte[_rowSize];
		System.arraycopy(_rawPage, requestedRowOffset, result, 0, _rowSize);
		return result;
	}

	public void setRow(int rowId, byte[] rowData)
	{
		int requestedRowOffset = DATA_OFFSET + rowId * _rowSize;
		System.arraycopy(rowData, 0, _rawPage, requestedRowOffset, _rowSize);
	}

	public void deleteRow(int rowId)
	{
		_isOccupied.clear(rowId);
	}

	public boolean isFull()
	{
		return _isOccupied.cardinality() == _rowsOnPage;
	}

	public boolean isEmpty()
	{
		return _isOccupied.cardinality() == 0;
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
	private static final int DATA_PAGE_SIZE_IN_BYTES = 4000;
	private static final int MAX_ROWS_ON_PAGE = DATA_PAGE_SIZE_IN_BYTES / 4; // size of int
}
