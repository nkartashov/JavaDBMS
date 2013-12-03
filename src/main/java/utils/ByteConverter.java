package utils;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 06/11/2013
 * Time: 01:25
 * To change this template use File | Settings | File Templates.
 */
public class ByteConverter
{
	public static long longFromByte(byte[] rawData, int index)
	{
		return ByteBuffer.wrap(rawData, index, LONG_LENGTH_IN_BYTES).getLong();
	}

	public static int intFromByte(byte[] rawData, int index)
	{
		return ByteBuffer.wrap(rawData, index, INT_LENGTH_IN_BYTES).getInt();
	}

	public static String stringFromBytes(byte[] rawData, int index, int length)
	{
		return new String(rawData, index, length);
	}

	public static byte[] longToByte(long item)
	{
		return ByteBuffer.allocate(LONG_LENGTH_IN_BYTES).putLong(item).array();
	}

	public static byte[] intToByte(int item)
	{
		return ByteBuffer.allocate(INT_LENGTH_IN_BYTES).putInt(item).array();
	}

	public static byte[] charsToByte(char[] item, int padding)
	{
		ByteBuffer result = ByteBuffer.allocate(CHAR_LENGTH_IN_BYTES * item.length);
		for (int i = 0; i < item.length; ++i)
			result.putChar(i, item[i]);
		byte[] dataResult = result.array();
		if (padding > 0)
			dataResult = ArrayUtils.addAll(new byte[padding], dataResult);
		return dataResult;
	}

	public static byte[] bitsetToBytes(BitSet b)
	{
		return null;
	}

	public BitSet bitsetFromBytes(byte[] b)
	{
		return null;
	}

	public static int LONG_LENGTH_IN_BYTES = 8;
	public static int INT_LENGTH_IN_BYTES = 4;
	public static int CHAR_LENGTH_IN_BYTES = 2;

	public static int BITS_IN_BYTE = 8;
}
