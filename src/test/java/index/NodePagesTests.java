package index;

import junit.framework.Assert;
import org.junit.Test;
import utils.ByteConverter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/22/14
 * Time: 1:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodePagesTests {

    private void fillRawEntries(byte[] entries, int[] data) {
        int pos = 0;
        for (int i = 0; i < data.length; i += 3) {
            rawEntryInsert(entries, data[i], data[i + 1], data[i + 2], pos);
            pos += ByteConverter.LONG_LENGTH_IN_BYTES + 2*ByteConverter.INT_LENGTH_IN_BYTES;
        }
    }

    private void rawEntryInsert(byte[] raw_page, int ptr, int row_prt, int key, int pos) {
        System.arraycopy(ByteConverter.longToByte(ptr), 0, raw_page, pos, 8);
        pos += ByteConverter.LONG_LENGTH_IN_BYTES;
        System.arraycopy(ByteConverter.intToByte(row_prt), 0, raw_page, pos, 4);
        pos += ByteConverter.INT_LENGTH_IN_BYTES;
        System.arraycopy(ByteConverter.intToByte(key), 0, raw_page, pos, 4);
    }

    @Test
    public void tryFindTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        byte[] entries = new byte[48];
        int[] raw_entries = {11, 0, 1, 22, 0, 2, 33, 0, 3};
        fillRawEntries(entries, raw_entries);
        LeafNodePage.init(page_data, entries, 3);
        LeafNodePage leaf = new LeafNodePage(page_data, 0, false);

        TableEntryPtr ptr = leaf.tryFindKey(11);
        Assert.assertEquals(ptr.pagePointer(), 11);
    }
}
