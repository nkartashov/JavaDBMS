package utils;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 06/11/2013
 * Time: 01:25
 * To change this template use File | Settings | File Templates.
 */
public class ByteConverter
{
	public static long LongFromByte(byte[] rawData, int index)
	{
		long result = 0;
		for (int i = 0; i < LONG_LENGTH_IN_BYTES; ++i)
		{
			result += ((long) rawData[index + i] & 0xFF) << (BYTE_LENGTH_IN_BITS * i);
		}
		return result;
	}

	public static int IntFromByte(byte[] rawData, int index)
	{
		int result = 0;
		for (int i = 0; i < INT_LENGTH_IN_BYTES; ++i)
		{
			result += ((int) rawData[index + i] & 0xFF) << (BYTE_LENGTH_IN_BITS * i);
		}
		return result;
	}

	public static char CharFromByte(byte[] rawData, int index)
	{
		char result = 0;
		for (int i = 0; i < CHAR_LENGTH_IN_BYTES; ++i)
		{
			result += ((char) rawData[index + i] & 0xFF) << (BYTE_LENGTH_IN_BITS * i);
		}
		return result;
	}

	public static byte[] LongToByte(long item)
	{
		return ByteBuffer.allocate(LONG_LENGTH_IN_BYTES).putLong(item).array();
	}

	public static byte[] IntToByte(int item)
	{
		return ByteBuffer.allocate(INT_LENGTH_IN_BYTES).putInt(item).array();
	}

	public static byte[] CharToByte(char item)
	{
		return ByteBuffer.allocate(CHAR_LENGTH_IN_BYTES).putChar(item).array();
	}

	public static byte[] CharsToByte(char[] item, int length)
	{
		ByteBuffer result = ByteBuffer.allocate(CHAR_LENGTH_IN_BYTES * length);
		for (int i = 0; i < length; ++i)
			result.putChar(i, item[i]);
		return result.array();
	}

	public static int LONG_LENGTH_IN_BYTES = 8;
	public static int BYTE_LENGTH_IN_BITS = 8;
	public static int INT_LENGTH_IN_BYTES = 4;
	public static int CHAR_LENGTH_IN_BYTES = 2;
}
