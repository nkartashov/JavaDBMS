package memoryManager;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 30/10/2013
 * Time: 14:18
 * To change this template use File | Settings | File Templates.
 */
public class PageId {
    public PageId(String filePath, long pageNumber)
    {
        _filePath = filePath;
        _pageNumber = pageNumber;
    }

    public String getFilePath()
    {
        return _filePath;
    }

    public long getPageNumber()
    {
        return _pageNumber;
    }

	public void updatePageNumber(long pageNumber)
	{
		_pageNumber = pageNumber;
	}

    private String _filePath;
    private long _pageNumber;
}
