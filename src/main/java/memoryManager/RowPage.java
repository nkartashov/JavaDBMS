package memoryManager;

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
		_isOccupied = new BitSet(BIT_MASK_SIZE_IN_BITS);
		_isOccupied.clear();

		if (!blankPage)
		{
			byte[] bitsetData = new byte[BIT_MASK_SIZE_IN_BYTES];
			System.arraycopy(_rawPage, BIT_MASK_OFFSET, bitsetData, 0, BIT_MASK_SIZE_IN_BYTES);
			_isOccupied = BitSet.valueOf(bitsetData);
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

	public int firstEmptyRowIndex()
	{
		return _isOccupied.nextClearBit(0);
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
		byte[] bytes = _isOccupied.toByteArray();
		System.arraycopy(bytes, 0, _rawPage, BIT_MASK_OFFSET, bytes.length);
	}

	private BitSet _isOccupied;
	private int _rowSize;
	private int _rowsOnPage;

	private static int BIT_MASK_OFFSET = 16;
	private static int BIT_MASK_SIZE_IN_BITS = 128;
	private static int BIT_MASK_SIZE_IN_BYTES = 128 / 8;
	private static int DATA_PAGE_SIZE_IN_BYTES = 4000;
	private static int MAX_ROWS_ON_PAGE = 100;
}
