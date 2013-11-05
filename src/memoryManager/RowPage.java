package memoryManager;

import tableTypes.BaseTableType;
import tableTypes.TableChar;
import tableTypes.TableInt;
import utils.ByteIterator;
import utils.ByteConverter;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 30/10/2013
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class RowPage extends DiskPage
{
	public RowPage(byte[] rawData, ArrayList<BaseTableType> rowSignature)
	{
		super(rawData);
		_isOccupied = new BitSet(MAX_ROWS_ON_PAGE);
		_rowSignature = rowSignature;
		for (BaseTableType type: _rowSignature)
			_rowSize += type.size();

		//Read data from bitset bytes on page
		ByteIterator it = new ByteIterator(_rawPage, 0, BIT_MASK_OFFSET);
		boolean bit = false;
		for (int i = 0; i < 100; ++i)
		{
			bit = it.next();
			if (bit)
				_isOccupied.set(i);
		}
	}

	public ArrayList<Object> GetRow(int rowId)
	{
		ArrayList<Object> result = new ArrayList<Object>();
		int requestedRowOffset = ROWS_OFFSET + rowId * _rowSize;
		int insideRowOffset = 0;
		for (BaseTableType type: _rowSignature)
		{
			if (type instanceof TableInt)
			{
				int item = ByteConverter.IntFromByte(_rawPage, requestedRowOffset + insideRowOffset);
				result.add(item);
				insideRowOffset += type.size();
			}
			else
			{
				int numberOfChars = type.size() / ByteConverter.CHAR_LENGTH_IN_BYTES;
				char[] item = new char[numberOfChars];
				char buf = 0;
				for (int i = 0; i < numberOfChars; ++i)
				{
					buf = ByteConverter.CharFromByte(_rawPage, requestedRowOffset + insideRowOffset);
					insideRowOffset += ByteConverter.CHAR_LENGTH_IN_BYTES;
					item[i] = buf;
				}
				result.add(item);
			}
		}
		return result;
	}

	public void SetRow(int rowId, ArrayList<Object> newRow)
	{
		int requestedRowOffset = ROWS_OFFSET + rowId * _rowSize;
		int insideRowOffset = 0;
		for (int i = 0; i < _rowSignature.size(); ++i)
		{
			if (_rowSignature.get(i) instanceof TableInt)
			{
				byte[] newValues = ByteConverter.IntToByte((Integer) newRow.get(i));
				ByteIterator it = new ByteIterator(_rawPage, 0, requestedRowOffset + insideRowOffset);
				it.write(newValues, ByteConverter.INT_LENGTH_IN_BYTES);
				insideRowOffset += ByteConverter.INT_LENGTH_IN_BYTES;
			}
			else
			{
				BaseTableType type = _rowSignature.get(i);
				int numberOfChars = type.size() / ByteConverter.CHAR_LENGTH_IN_BYTES;
				byte[] newValues = ByteConverter.CharsToByte((char[]) newRow.get(i), numberOfChars);
				ByteIterator it = new ByteIterator(_rawPage, 0, requestedRowOffset + insideRowOffset);
				it.write(newValues, numberOfChars * ByteConverter.CHAR_LENGTH_IN_BYTES);
				insideRowOffset += numberOfChars * ByteConverter.CHAR_LENGTH_IN_BYTES;
			}
		}
	}

	public void DeleteRow(int rowId)
	{
		_isOccupied.clear(rowId);
	}

	private BitSet _isOccupied;
	private ArrayList<BaseTableType> _rowSignature;
	private int _rowSize;


	private static int MAX_ROWS_ON_PAGE = 100;
	private static int BIT_MASK_OFFSET = 16;
	private static int ROWS_OFFSET = 96;
}
