package utils;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 06/11/2013
 * Time: 01:42
 * To change this template use File | Settings | File Templates.
 */
public class ByteIterator
{
	public ByteIterator(byte[] bytes, int insideByteOffset, int offset)
	{
		_bytes = bytes;
		_insideByteOffset = insideByteOffset;
		_offset = offset;
	}

	public boolean next()
	{
		if (_insideByteOffset != MAX_INSIDE_BYTE_OFFSET)
			return (_bytes[_offset] & (1 << _insideByteOffset++)) > 0;
		else
		{
			_insideByteOffset = 0;
			_offset++;
			return (_bytes[_offset] & 1) > 0;
		}
	}

	private byte[] _bytes;
	private int _insideByteOffset;
	private int _offset;

	private static int MAX_INSIDE_BYTE_OFFSET = 0xFF;
}
