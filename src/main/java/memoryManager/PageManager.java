package memoryManager;

import utils.Logger;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class PageManager extends LinkedHashMap<PageId, ManagedMemoryPage>
{
    public static PageManager getInstance()
    {
	    return _instance;
    }

	public byte[] createPage(PageId pageId)
	{
		try
		{
			RandomAccessFile outputFile = new RandomAccessFile(pageId.filePath(), "rw");
			long fileLength = outputFile.length();
			if (fileLength != 0)
			{
				pageId.updatePageNumber(fileLength / DiskPage.MAX_PAGE_SIZE);
			}
			else
			{
				pageId.updatePageNumber(0);
			}
			outputFile.setLength(fileLength + DiskPage.MAX_PAGE_SIZE);
			outputFile.close();
			byte[] newRawPage = new byte[DiskPage.MAX_PAGE_SIZE];
			ManagedMemoryPage page = new ManagedMemoryPage(newRawPage);
			page.get();
			put(pageId, page);
			if (_releasedPages.contains(pageId))
				_releasedPages.remove(pageId);
			return newRawPage;
		}
		catch (FileNotFoundException e)
		{
			Logger.LogErrorMessage(e);
			return null;
		}
		catch (Exception e)
		{
			Logger.LogErrorMessage(e);
			return null;
		}
	}

	public byte[] getPage(PageId pageId)
    {
        ManagedMemoryPage page = get(pageId);
        if (page == null)
        {
            loadPage(pageId);
	        page = get(pageId);
        }
	    page.get();
	    if (_releasedPages.contains(pageId))
		    _releasedPages.remove(pageId);
        return page.data();
    }

	public void close()
	{
		dumpAllPages();
		clear();
	}

    private void loadPage(PageId pageId)
    {
        try
        {
            RandomAccessFile inputFile = new RandomAccessFile(pageId.filePath(), "r");
            byte[] page = new byte[DiskPage.MAX_PAGE_SIZE];

            long offsetToPage = DiskPage.MAX_PAGE_SIZE * pageId.getPageNumber();

            inputFile.seek(offsetToPage);
            inputFile.read(page);

            inputFile.close();

            put(pageId, new ManagedMemoryPage(page));
        }
        catch (Exception e)
        {
            Logger.LogErrorMessage(e);
        }
    }

	public void releasePage(PageId pageId)
	{
		ManagedMemoryPage page = get(pageId);
		page.release();
		if (page.fullyReleased())
			_releasedPages.add(pageId);
		put(pageId, page);
	}

	public void updateAndReleasePage(PageId pageId, byte[] rawPage)
	{
		ManagedMemoryPage page = get(pageId);
		if (page == null)
			System.out.print("lol");
		page.setData(rawPage);
		if (page.fullyReleased())
			_releasedPages.add(pageId);
		page.release();
		put(pageId, page);
	}

	private void dumpAllPages()
	{
		for (Map.Entry<PageId, ManagedMemoryPage> entry: entrySet())
		{
			dumpPage(entry);
		}
	}

	private void dumpPage(PageId pageId, byte[] rawPage)
	{
		try
		{
			RandomAccessFile outputFile = new RandomAccessFile(pageId.filePath(), "rw");
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

    private void dumpPage(final Map.Entry<PageId, ManagedMemoryPage> entry)
    {
	    if (entry.getValue().references() > 0)
		    Logger.LogApplicationError("Page Manager: the entry being deleted is still accessed");
	    if (entry.getValue().references() < 0)
		    Logger.LogApplicationError("Page Manager: the entry being deleted has been released multiple times");
	    if (entry.getValue().hasBeenChanged())
	        dumpPage(entry.getKey(), entry.getValue().data());
    }

    private PageManager()
    {
        super(MAX_PAGES, 1f, true);
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<PageId, ManagedMemoryPage> entry)
    {
        if (super.size() > MAX_PAGES)
        {
	        if (!entry.getValue().fullyReleased())
	        {
		        if (!_releasedPages.isEmpty())
		        {
			        PageId releasedPageId = _releasedPages.iterator().next();
			        dumpPage(releasedPageId, get(releasedPageId).data());
			        remove(releasedPageId);
			        _releasedPages.remove(releasedPageId);
			        return false;
		        }
	        }
	        if (_releasedPages.contains(entry.getKey()))
		        _releasedPages.remove(entry.getKey());
	        dumpPage(entry);
            return true;
        }
        return false;
    }

	public static final int MAX_PAGES = 10;

	private Set<PageId> _releasedPages = new HashSet<PageId>();
    private static PageManager _instance = new PageManager();
}
