package utils;

import org.junit.Assert;
import org.junit.Test;



/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 01:08
 * To change this template use File | Settings | File Templates.
 */
public class BitArrayTests
{
	@Test
	public void BasicInitTest()
	{
		byte[] testData = {0, -1, 100, 50, 23, 127, 0};
		BitArray bitArray = new BitArray(testData);
		Assert.assertArrayEquals(testData, bitArray.toByteArray());
	}

	@Test
	public void BasicSetReadTest()
	{
		int length = 8;
		BitArray bitArray = new BitArray(length);

		for(int i = 0; i < length; ++i)
			Assert.assertEquals(false, bitArray.get(i));

		int index = 5;
		bitArray.set(index);
		Assert.assertEquals(true, bitArray.get(index));
	}

	@Test
	public void FirstNotSetBitTest()
	{
		int length = 13;
		BitArray bitArray = new BitArray(length);

		Assert.assertEquals(0, bitArray.nextClearBit(0));

		bitArray.set(1);
		bitArray.set(2);

		Assert.assertEquals(0, bitArray.nextClearBit(0));
		Assert.assertEquals(3, bitArray.nextClearBit(1));
	}

	@Test
	public void CardinalityTest()
	{
		int length = 17;
		BitArray bitArray = new BitArray(length);

		Assert.assertEquals(0, bitArray.cardinality());

		int index = 12;
		bitArray.set(index);
		Assert.assertEquals(true, bitArray.get(index));
		Assert.assertEquals(1, bitArray.cardinality());

		bitArray.set(index);
		Assert.assertEquals(true, bitArray.get(index));
		Assert.assertEquals(1, bitArray.cardinality());

		int index2 = length - 1;
		bitArray.set(index2);
		Assert.assertEquals(true, bitArray.get(index2));
		Assert.assertEquals(2, bitArray.cardinality());

		bitArray.clear(index);
		Assert.assertEquals(false, bitArray.get(index));
		Assert.assertEquals(1, bitArray.cardinality());
	}

	@Test
	public void ReadFromMemoryTest()
	{
		BitArray test = new BitArray(47);

		int[] testBits = {0, 2, 7, 9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		byte[] byteRepr = test.toByteArray();

		BitArray bitArray = new BitArray(byteRepr);

		Assert.assertEquals(testBits.length, bitArray.cardinality());

		for (int i = 0; i < test.size(); ++i)
			Assert.assertEquals(test.get(i), bitArray.get(i));
	}

	@Test
	public void LastSetBitTest()
	{
		BitArray test = new BitArray(47);
		int[] testBits = {0, 2, 7, 9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		Assert.assertEquals(testBits[testBits.length - 1], test.lastSetBit());
	}

	@Test
	public void FirstClearBitTestEasy()
	{
		BitArray test = new BitArray(47);
		int[] testBits = {0, 1, 2, 7, 9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		Assert.assertEquals(3, test.firstClearBit());
	}

	@Test
	public void FirstClearBitTestComplex()
	{
		BitArray test = new BitArray(47);
		int[] testBits = {0, 1, 2, 3, 4, 5, 6,  7, 8, 9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		Assert.assertEquals(11, test.firstClearBit());
	}

	@Test
	public void FirstSetBitTestEasy()
	{
		BitArray test = new BitArray(47);
		int[] testBits = {0, 1, 2, 3, 4, 5, 6,  7, 8, 9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		Assert.assertEquals(testBits[0], test.firstSetBit());
	}

	@Test
	public void FirstSetBitTestComplex()
	{
		BitArray test = new BitArray(47);
		int[] testBits = {9, 10, 15, 27, 32, 44, 46};

		for (int i: testBits)
			test.set(i);

		Assert.assertEquals(testBits[0], test.firstSetBit());
	}
}
