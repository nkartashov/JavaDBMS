package memoryManager;

import utils.Logger;

import java.io.RandomAccessFile;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class PageManager extends LinkedHashMap<PageId, byte[]>
{
    public static PageManager GetInstance()
    {
	    return _instance;
    }

	public byte[] CreatePage(PageId pageId)
	{
		try
		{
			RandomAccessFile outputFile = new RandomAccessFile(pageId.getFilePath(), "wd");
			long fileLength = outputFile.length();
			outputFile.setLength(fileLength + DiskPage.MAX_PAGE_SIZE);
			outputFile.close();
			byte[] newRawPage = new byte[DiskPage.MAX_PAGE_SIZE];
			put(pageId, newRawPage);
			return newRawPage;
		}
		catch (Exception e)
		{
			Logger.LogErrorMessage(e);
			return null;
		}
	}

	public byte[] GetPage(PageId pageId)
    {
        byte[] rawPage = get(pageId);
        if (rawPage == null)
            LoadPage(pageId);
        return get(pageId);
    }

	public void UpdatePage(PageId pageId, byte[] rawPage)
	{
		put(pageId, rawPage);
	}

	public void close()
	{
		DumpAllPages();
		clear();
	}

    private void LoadPage(PageId pageId)
    {
        try
        {
            RandomAccessFile inputFile = new RandomAccessFile(pageId.getFilePath(), "r");
            byte[] page = new byte[DiskPage.MAX_PAGE_SIZE];

            long offsetToPage = DiskPage.MAX_PAGE_SIZE * pageId.getPageNumber();

            inputFile.seek(offsetToPage);
            inputFile.read(page);

            inputFile.close();

            put(pageId, page);
        }
        catch (Exception e)
        {
            Logger.LogErrorMessage(e);
        }
    }

	private void DumpAllPages()
	{
		for (PageId pageId: keySet())
		{
			DumpPage(pageId, get(pageId));
		}
	}

	private void DumpPage(PageId pageId, byte[] rawPage)
	{
		try
		{
			RandomAccessFile outputFile = new RandomAccessFile(pageId.getFilePath(), "wd");
			long offsetToPage = DiskPage.MAX_PAGE_SIZE * pageId.getPageNumber();

			outputFile.seek(offsetToPage);
			outputFile.write(rawPage);
			outputFile.close();
		}

		catch (Exception e)
		{
			Logger.LogErrorMessage(e);
		}
	}

    private void DumpPage(final Map.Entry<PageId, byte[]> entry)
    {
	    DumpPage(entry.getKey(), entry.getValue());
    }

    private PageManager()
    {
        super(MAX_PAGES, 1f, true);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<PageId, byte[]> entry)
    {
        if (super.size() > MAX_PAGES)
        {
            DumpPage(entry);
            return true;
        }
        return false;
    }

		// 64 * 1024 * 4K = 64 * 4M = 256M
		private static final int MAX_PAGES = 64 * 1024;

    private static PageManager _instance = new PageManager();
}
