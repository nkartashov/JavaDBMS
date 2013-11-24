package memoryManager;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 21/11/2013
 * Time: 20:41
 * To change this template use File | Settings | File Templates.
 */
public class PointerPageTests
{
	@Test
	public void CorrectDataWritingTest()
	{
		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];

		PointerPage newPage = new PointerPage(rawData, true);

		Assert.assertTrue(newPage.isEmpty());
		Assert.assertEquals(newPage.pointersCount(), 0);

		newPage.setPointer(26, 987);

		Assert.assertEquals(newPage.getPointer(26), 987);
	}

	@Test
	public void CorrectAddingRemovalTest()
	{
		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];

		PointerPage newPage = new PointerPage(rawData, true);

		Assert.assertTrue(newPage.isEmpty());
		Assert.assertEquals(newPage.pointersCount(), 0);

		newPage.addPointer(687);

		Assert.assertEquals(newPage.pointersCount(), 1);
		Assert.assertEquals(newPage.getLastPointer(), 687);

		newPage.addPointer(590);

		Assert.assertEquals(newPage.pointersCount(), 2);
		Assert.assertEquals(newPage.getLastPointer(), 590);

		newPage.setPointer(1, 198);

		byte[] dataPayload = newPage.rawPage();

		newPage = new PointerPage(dataPayload, false);

		Assert.assertEquals(newPage.pointersCount(), 2);
		Assert.assertEquals(newPage.getLastPointer(), 198);
	}
}
