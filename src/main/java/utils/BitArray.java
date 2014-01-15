package utils;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 26/11/2013
 * Time: 23:53
 * To change this template use File | Settings | File Templates.
 */
public class BitArray
{
	public BitArray(byte[] bytes)
	{
		_bytes = bytes;
		_lengthInBytes = bytes.length;
		_size = bytes.length * ByteConverter.BITS_IN_BYTE;
		for (int i = 0; i < _size; ++i)
			if ((_bytes[byteIndex(i)] & leftShiftOne(bitIndex(i))) > 0)
				++_setBits;
	}

	public BitArray(int size)
	{
		_lengthInBytes = size / ByteConverter.BITS_IN_BYTE;
		if (size % ByteConverter.BITS_IN_BYTE > 0)
			++_lengthInBytes;
		_bytes = new byte[_lengthInBytes];
		_size = size;
		_setBits = 0;
	}

	public int cardinality() {return _setBits;}

	public void set(int index)
	{
		if (isClear(index))
			++_setBits;
		_bytes[index / ByteConverter.BITS_IN_BYTE] |= leftShiftOne(bitIndex(index));
	}

    //Sets the bits from the specified from_index (inclusive) to the specified to_index (exclusive)
    public void set(int from_index, int to_index) {
        for(int i = from_index; i < to_index; ++i) {
            set(i);
        }
    }

	public void clear(int index)
	{
		if (isSet(index))
			--_setBits;
		_bytes[index / ByteConverter.BITS_IN_BYTE] &= ~(leftShiftOne(bitIndex(index)));
	}

    //Clears the bits from the specified from_index (inclusive) to the specified to_index (exclusive)
    public void clear(int from_index, int to_index) {
        for(int i = from_index; i < to_index; ++i) {
            clear(i);
        }
    }

    //Sets all bits to 0
    public void clear() {
        for (int i = 0; i < _bytes.length; ++i) {
            _bytes[i] = 0;
        }
        _setBits = 0;
    }

	public int nextClearBit(int index)
	{
		for (int i = index; i < _size; ++i)
			if (isClear(i))
				return i;
		return -1;
	}

	public int nextSetBit(int index)
	{
		for (int i = index; i < _size; ++i)
			if (isSet(i))
				return i;
		return -1;
	}

	public boolean isClear(int index)
	{
		return (_bytes[byteIndex(index)] & leftShiftOne(bitIndex(index))) == 0;
	}

	public boolean isSet(int index)
	{
		return !isClear(index);
	}

	public boolean get(int index)
	{
		return isSet(index);
	}

	public byte[] toByteArray()
	{
		byte[] result = new byte[_lengthInBytes];
		writeBitArray(result, 0);
		return result;
	}

	public void writeBitArray(byte[] destination, int destinationIndex)
	{
		System.arraycopy(_bytes, 0, destination, destinationIndex, _lengthInBytes);
	}

	public static BitArray readBitArray(byte[] source, int sourceIndex, int lengthBits)
	{
		BitArray result = new BitArray(lengthBits);

		System.arraycopy(source, sourceIndex, result._bytes, 0, result._lengthInBytes);

		for (int i = 0; i < result._size; ++i)
			if (result.isSet(i))
				++result._setBits;

		return result;
	}

	private int byteIndex(int index)
	{
		return index / ByteConverter.BITS_IN_BYTE;
	}

	private int bitIndex(int index)
	{
		return index % ByteConverter.BITS_IN_BYTE;
	}

	private byte leftShiftOne(int index)
	{
		return (byte) (1 << index);
	}

	private int _lengthInBytes;
	private int _setBits = 0;
	private int _size;
	private byte[] _bytes;
}
