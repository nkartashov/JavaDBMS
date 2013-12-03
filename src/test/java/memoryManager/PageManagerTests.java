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
		String testFileName = RESOURCE_PATH + "testfile";

		PageManager pageManager = PageManager.getInstance();

		int prevValue = 0;
		int maxPages = 10;

		Assert.assertEquals(prevValue, pageManager.size());

		for (int i = 0; i < 15; i++)
		{
			PageId newPageId = new PageId(testFileName, 0);
			pageManager.createPage(newPageId);
			Assert.assertEquals(Math.min(++prevValue, maxPages), pageManager.size());

			pageManager.releasePage(newPageId);
		}

		File testFile = new File(testFileName);
		testFile.delete();
	}

 	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";

}