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

	public static byte[] longToByte(long item)
	{
		return ByteBuffer.allocate(LONG_LENGTH_IN_BYTES).putLong(item).array();
	}

	public static byte[] intToByte(int item)
	{
		return ByteBuffer.allocate(INT_LENGTH_IN_BYTES).putInt(item).array();
	}

	public static byte[] charsToByte(char[] item)
	{
		ByteBuffer result = ByteBuffer.allocate(CHAR_LENGTH_IN_BYTES * item.length);
		for (int i = 0; i < item.length; ++i)
			result.putChar(i, item[i]);
		return result.array();
	}

	public static void intToBuffer(int item, byte[] buffer, int offset)
	{
		System.arraycopy(intToByte(item), 0, buffer, offset, INT_LENGTH_IN_BYTES);
	}

	public static void longToBuffer(long item, byte[] buffer, int offset)
	{
		System.arraycopy(longToByte(item), 0, buffer, offset, LONG_LENGTH_IN_BYTES);
	}

	public static void charsToBuffer(char[] item, byte[] buffer, int offset)
	{
		System.arraycopy(charsToByte(item), 0, buffer, offset, item.length * CHAR_LENGTH_IN_BYTES);
	}

    public static byte[] bitsetToBytes(BitSet item) {
        byte[] bytes = new byte[(item.length() / 8) + 1];
        for (int i = 0; i != bytes.length; ++i) {
            bytes[i] = 0;
        }
        for (int i = 0; i != item.length(); ++i) {
            if (item.get(i)) {
                bytes[i / 8] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public static byte[] leafEntriesToBytes(List<LeafNodeEntry> list) {
        byte[] raw_all_entries = null;
        for(int i = 0; i != list.size(); ++i) {
            raw_all_entries = ArrayUtils.addAll(raw_all_entries, list.get(i).FullEntry());
        }
        return raw_all_entries;
    }


	public static int LONG_LENGTH_IN_BYTES = 8;
	public static int INT_LENGTH_IN_BYTES = 4;
	public static int CHAR_LENGTH_IN_BYTES = 2;
}
