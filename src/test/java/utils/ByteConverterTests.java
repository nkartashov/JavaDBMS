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
	public void CodeDecodeStringTest()
	{
		String lol = "teststseseds";
		byte[] b = ByteConverter.stringToBytes(lol, lol.length() - 1);
		Assert.assertEquals(lol, ByteConverter.stringFromBytes(b, 0));
	}

	@Test
	public void CodeStringTest()
	{
		String lol = "teststseseds";
		int fieldSize = 39;

		Assert.assertEquals(fieldSize, ByteConverter.stringToBytes(lol, fieldSize).length);
	}
}
