package utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 8:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteConverterTest {
    @Test
    public void bitsetToBytesTest() {
        BitSet bitset = new BitSet(10);
        bitset.set(0, 10, true);
        byte[] converted = ByteConverter.bitsetToBytes(bitset);
        Assert.assertTrue(converted[0] == -1);
        Assert.assertTrue(converted[1] == 3);
    }
}
