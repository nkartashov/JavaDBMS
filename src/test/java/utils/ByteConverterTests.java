package utils;

import org.junit.Test;
import org.junit.Assert;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
public class ByteConverterTests
{
	@Test
	public void BytesFromCharsTest()
	{
		int expectedResultLength = 20;
		String testString = "jfkfldkf";
		Assert.assertEquals(expectedResultLength,
			ByteConverter.charsToByte(testString.toCharArray(),
				expectedResultLength - testString.length() * 2).length);
	}
}
