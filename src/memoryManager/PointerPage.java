package memoryManager;

import utils.ByteConverter;
import utils.ByteIterator;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 06/11/2013
 * Time: 00:38
 * To change this template use File | Settings | File Templates.
 */
public class PointerPage extends DiskPage
{
	public PointerPage(byte[] rawPage)
	{
		super(rawPage);
	}

	public long GetPointer(int index)
	{
		return ByteConverter.LongFromByte(_rawPage, POINTERS_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES);
	}

	public void SetPointer(int index, long pointer)
	{
		byte[] newValues = ByteConverter.LongToByte(pointer);
		ByteIterator it = new ByteIterator(_rawPage, 0, POINTERS_OFFSET + index * ByteConverter.LONG_LENGTH_IN_BYTES);
		it.write(newValues, ByteConverter.LONG_LENGTH_IN_BYTES);
	}

	private static int POINTERS_OFFSET = 96;
}
