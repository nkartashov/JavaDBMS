package utils;

import index.LeafNodeEntry;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

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

	public static String stringFromBytes(byte[] rawData, int index)
	{
		return new String(rawData, index + 1, rawData[index]);
	}

	public static byte[] longToByte(long item)
	{
		return ByteBuffer.allocate(LONG_LENGTH_IN_BYTES).putLong(item).array();
	}

	public static byte[] intToByte(int item)
	{
		return ByteBuffer.allocate(INT_LENGTH_IN_BYTES).putInt(item).array();
	}

	public static byte[] stringToBytes(String item, int length)
	{
		byte[] result = new byte[1];
		byte[] bytes = item.getBytes();
		result[0] = (byte) bytes.length;
		assert bytes.length <= Byte.MAX_VALUE;
		return ArrayUtils.addAll(ArrayUtils.addAll(result, bytes),
			new byte[length - item.length() -1]);
	}

	public static int LONG_LENGTH_IN_BYTES = 8;
	public static int INT_LENGTH_IN_BYTES = 4;
	public static int CHAR_LENGTH_IN_BYTES = 1;

	public static int BITS_IN_BYTE = 8;
}
