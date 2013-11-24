package memoryManager;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 21/11/2013
 * Time: 22:13
 * To change this template use File | Settings | File Templates.
 */
public class RowPageTest
{
	@Test
	public void CorrectDataWritingTest()
	{
		byte[] rawData = new byte[DiskPage.MAX_PAGE_SIZE];

		RowPage page = new RowPage(rawData, true, 10);

		Assert.assertFalse(page.isFull());

		byte[] dataPayload = {45, -127, -117, 32, 87, -11, 0, 1, 67, 89};

		page.putRow(dataPayload);

		Assert.assertEquals(1, page.getRowsCount());
		Assert.assertArrayEquals(dataPayload, page.getRow(0));

		byte[] newPayload = ArrayUtils.clone(dataPayload);
		ArrayUtils.reverse(newPayload);

		int test = 100;
		for (int i = 0; i < 100; ++i)
			page.putRow(newPayload);


		Assert.assertEquals(1 + test, page.getRowsCount());
		Assert.assertArrayEquals(newPayload, page.getRow(1));

		byte[] newData = page.rawPage();

		page = new RowPage(newData, false, 10);

		Assert.assertEquals(1 + test, page.getRowsCount());
		Assert.assertArrayEquals(dataPayload, page.getRow(0));
		Assert.assertArrayEquals(newPayload, page.getRow(1));

		page.deleteRow(0);

		Assert.assertEquals(test, page.getRowsCount());
	}

}
