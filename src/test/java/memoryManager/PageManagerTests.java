package memoryManager;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 04/12/2013
 * Time: 00:51
 * To change this template use File | Settings | File Templates.
 */

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class PageManagerTests
{
	@Test
	public void BasicPageManagerTest()
	{
		String testFileName = RESOURCE_PATH + "testfilelhuglkfgljkhg";

		PageManager pageManager = PageManager.getInstance();

		int prevValue = pageManager.size();

		Assert.assertEquals(prevValue, pageManager.size());

		for (int i = 0; i < 15; i++)
		{
			PageId newPageId = new PageId(testFileName, 0);
			pageManager.createPage(newPageId);
			Assert.assertEquals(Math.min(++prevValue, PageManager.MAX_PAGES), pageManager.size());

			pageManager.releasePage(newPageId);
		}

		File testFile = new File(testFileName);
		testFile.delete();
	}

	@Test
	public void UpdateAndReleaseTest()
	{
		String testFileName = RESOURCE_PATH + "testfile1";
		PageManager pageManager = PageManager.getInstance();
		int rowSize = 20;
		byte[] payload = new byte[rowSize];

		for (int i = 0; i < rowSize; ++i)
			payload[i] = 127;

		PageId newPageId = new PageId(testFileName, 0);
		byte[] rawData = pageManager.createPage(newPageId);
		RowPage rowPage = new RowPage(rawData, true, rowSize);
		int numberOfRows = 51;

		for (int i = 0; i < numberOfRows; ++i)
			rowPage.putRow(payload);

		pageManager.updateAndReleasePage(newPageId, rowPage.rawPage());
		rawData = pageManager.getPage(newPageId);
		rowPage = new RowPage(rawData, false, rowSize);
		Assert.assertEquals(numberOfRows, rowPage.occupiedRowsList().size());
		pageManager.releasePage(newPageId);
	}

 	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";
}