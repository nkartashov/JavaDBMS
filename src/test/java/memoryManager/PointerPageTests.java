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
	public void CorrectAddingSettingTest()
	{
		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];

		PointerPage newPage = new PointerPage(rawData, true);

		Assert.assertTrue(newPage.isEmpty());
		Assert.assertEquals(newPage.pointersCount(), 0);

		newPage.addPointer(687);

		Assert.assertEquals(newPage.pointersCount(), 1);
		Assert.assertEquals(newPage.getPointer(newPage.pointersCount() - 1), 687);

		newPage.addPointer(590);

		Assert.assertEquals(newPage.pointersCount(), 2);
		Assert.assertEquals(newPage.getPointer(newPage.pointersCount() - 1), 590);

		newPage.setPointer(1, 198);

		byte[] dataPayload = newPage.rawPage();

		newPage = new PointerPage(dataPayload, false);

		Assert.assertEquals(newPage.pointersCount(), 2);
		Assert.assertEquals(newPage.getPointer(newPage.pointersCount() - 1), 198);
	}

	@Test
	public void CorrectAddingRemoving()
	{
		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];

		PointerPage newPage = new PointerPage(rawData, true);

		Assert.assertTrue(newPage.isEmpty());
		Assert.assertEquals(newPage.pointersCount(), 0);

		long[] values = {687, 590, 127};

		for (int i = 0; i < 3; ++i)
		{
			newPage.addPointer(values[i]);

			Assert.assertEquals(newPage.pointersCount(), i + 1);
			Assert.assertEquals(newPage.getPointer(newPage.pointersCount() - 1), values[i]);
		}

		newPage.removePointer(1);
		long[] expectedValues = {687, 127};

		Assert.assertArrayEquals(expectedValues, newPage.allPointers());
	}
}
