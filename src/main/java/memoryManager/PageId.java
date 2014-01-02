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

	@Override
	public int hashCode() {
		final int prime = 641;
		int result = 1;
		result = prime * result + _filePath.hashCode();
		result = prime * result + Long.valueOf(_pageNumber).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PageId other = (PageId) obj;
		return _filePath.equals(other._filePath) && _pageNumber == other._pageNumber;
	}

    private String _filePath;
    private long _pageNumber;
}
