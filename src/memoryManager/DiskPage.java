package memoryManager;

import utils.ByteConverter;
import utils.ByteIterator;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:35
 * To change this template use File | Settings | File Templates.
 */
public class DiskPage {
    public static final int MAX_PAGE_SIZE = 4096; // 4K

    public DiskPage(byte[] rawPage)
    {
		_rawPage = rawPage;
	    _nextPageIndex = ByteConverter.LongFromByte(rawPage, NEXT_PAGE_INDEX_OFFSET);
	    _prevPageIndex = ByteConverter.LongFromByte(rawPage, PREV_PAGE_INDEX_OFFSET);
    }

	public void SetNextPageIndex(long pointer)
	{
		byte[] newValues = ByteConverter.LongToByte(pointer);
		ByteIterator it = new ByteIterator(_rawPage, 0, NEXT_PAGE_INDEX_OFFSET);
		it.write(newValues, ByteConverter.LONG_LENGTH_IN_BYTES);
	}

	public void SetPrevPageIndex(long pointer)
	{
		byte[] newValues = ByteConverter.LongToByte(pointer);
		ByteIterator it = new ByteIterator(_rawPage, 0, PREV_PAGE_INDEX_OFFSET);
		it.write(newValues, ByteConverter.LONG_LENGTH_IN_BYTES);
	}

	public long nextPageIndex() {return _nextPageIndex;}
	
	public long prevPageIndex() {return _prevPageIndex;}


	protected byte[] _rawPage;
	protected long _nextPageIndex;
	protected long _prevPageIndex;


	protected static int NEXT_PAGE_INDEX_OFFSET = 0;
	protected static int PREV_PAGE_INDEX_OFFSET = 8;

}
