package index;

import org.junit.Test;
import org.junit.Assert;
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

    @Test
    public void findTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        byte[] entries = new byte[48];
        int[] raw_entries = {11, 0, 1, 22, 0, 2, 33, 0, 3};
        fillRawEntries(entries, raw_entries);

        LeafNodePage.init(page_data, entries, 3);
        LeafNodePage leaf = new LeafNodePage(page_data, 0, false);

        TableEntryPtr ptr = leaf.tryFindKey(3);
        Assert.assertEquals(33, ptr.pagePointer());
        TableEntryPtr ptr2 = leaf.tryFindKey(4);
        Assert.assertEquals(true, ptr2._is_null);

        InnerNodePage.init(page_data, entries, 3);
        InnerNodePage inner = new InnerNodePage(page_data, 0, false);
        Assert.assertEquals(33, inner.nextNodePointer(3));
        Assert.assertEquals(0, inner.nextNodePointer(4));
    }

    @Test
    public void deleteTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        byte[] entries = new byte[48];
        int[] raw_entries = {11, 0, 1, 22, 0, 2, 33, 0, 3};
        fillRawEntries(entries, raw_entries);

        LeafNodePage.init(page_data, entries, 3);
        LeafNodePage leaf = new LeafNodePage(page_data, 0, false);

        Assert.assertEquals(33, leaf.deleteKey(3).pagePointer());
        Assert.assertEquals(true, leaf.tryFindKey(3)._is_null);
        Assert.assertEquals(true, leaf.deleteKey(4)._is_null);
        Assert.assertEquals(true, leaf.tryFindKey(4)._is_null);

        InnerNodePage.init(page_data, entries, 3);
        InnerNodePage inner = new InnerNodePage(page_data, 0, false);
        Assert.assertEquals(0, inner.nextNodePointer(4));
        inner.deleteLastEntry();
        Assert.assertEquals(33, inner.nextNodePointer(4));
    }

    @Test
    public void leafInsertNotFullTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        LeafNodePage leaf = new LeafNodePage(page_data, 0, true);

        leaf.insertNotFull(2, getTableEntryPrt(22, 0));
        Assert.assertEquals(22, leaf.tryFindKey(2).pagePointer());
        leaf.insertNotFull(1, getTableEntryPrt(11, 0));
        Assert.assertEquals(11, leaf.tryFindKey(1).pagePointer());
        Assert.assertEquals(22, leaf.tryFindKey(2).pagePointer());
        leaf.insertNotFull(3, getTableEntryPrt(33, 0));
        Assert.assertEquals(11, leaf.tryFindKey(1).pagePointer());
        Assert.assertEquals(22, leaf.tryFindKey(2).pagePointer());
        Assert.assertEquals(33, leaf.tryFindKey(3).pagePointer());
    }

    @Test
    public void innerInsertNotFullTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        InnerNodePage inner = new InnerNodePage(page_data, 0, true);

        inner.insertNotFull(new MoveUpElem(2, 11, 33));
        inner.insertNotFull(new MoveUpElem(4, 33, 55));
        inner.insertNotFull(new MoveUpElem(0, -11, 66));
        Assert.assertEquals(66, inner.nextNodePointer(1));
        Assert.assertEquals(33, inner.nextNodePointer(3));
        Assert.assertEquals(55, inner.nextNodePointer(5));
        Assert.assertEquals(-11, inner.nextNodePointer(-1));
    }

    @Test
    public void leafInsertFullTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        LeafNodePage leaf = new LeafNodePage(page_data, 0, true);
        int i = 1;
        while(!leaf.isFull()) {
            leaf.insertNotFull(i, getTableEntryPrt(10*i, 0));
            ++i;
        }
        byte[] second_part = leaf.insertFull(i,getTableEntryPrt(10*i, 0));
        for (int j = 1; j < i / 2; ++j) {
            Assert.assertEquals(10*j, leaf.tryFindKey(j).pagePointer());
        }
        for (int j = i / 2; j < i ; ++j) {
            Assert.assertEquals(true, leaf.tryFindKey(j)._is_null);
        }

        byte[] page_data2 = new byte[NodePage.PAGE_SIZE];
        LeafNodePage.init(page_data2, second_part, LeafNodePage.KEYS_MAX_NUM - LeafNodePage.KEYS_MAX_NUM / 2);
        LeafNodePage leaf2 = new LeafNodePage(page_data2, 0, false);
        for (int j = 1; j < i / 2; ++j) {
            Assert.assertEquals(true, leaf2.tryFindKey(j)._is_null);
        }
        for (int j = i / 2; j < i ; ++j) {
            Assert.assertEquals(10*j, leaf2.tryFindKey(j).pagePointer());
        }
    }

    @Test
    public void innerInsertFullTest() {
        byte[] page_data = new byte[NodePage.PAGE_SIZE];
        InnerNodePage inner = new InnerNodePage(page_data, 0, true);
        int i = 1;
        while(!inner.isFull()) {
            inner.insertNotFull(new MoveUpElem(i*10, i*10 - 1, i*10 + 1));
            ++i;
        }
        byte[] second_part = inner.insertFull(new MoveUpElem(i*10, i*10 - 1, i*10 + 1));
        for (int j = 1; j < i / 2; ++j) {
            Assert.assertEquals(10 * j - 1, inner.nextNodePointer(10*j));
        }
        long ptr = (i / 2) * 10 - 1;
        for (int j = i / 2; j < i ; ++j) {
            Assert.assertEquals(ptr, inner.nextNodePointer(10*j));
        }

        byte[] page_data2 = new byte[NodePage.PAGE_SIZE];
        InnerNodePage.init(page_data2, second_part, LeafNodePage.KEYS_MAX_NUM - LeafNodePage.KEYS_MAX_NUM / 2);
        InnerNodePage inner2 = new InnerNodePage(page_data2, 0, false);
        for (int j = 1; j < i / 2; ++j) {
            Assert.assertEquals(ptr, inner2.nextNodePointer(10*j));
        }
        for (int j = i / 2; j < i ; ++j) {
            Assert.assertEquals(10 * j - 1, inner2.nextNodePointer(10*j));
        }
    }

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

    private TableEntryPtr getTableEntryPrt(long page_prt, int row_ptr) {
        byte[] data = new byte[12];
        System.arraycopy(ByteConverter.longToByte(page_prt), 0, data, 0, ByteConverter.LONG_LENGTH_IN_BYTES);
        System.arraycopy(ByteConverter.intToByte(row_ptr), 0, data, ByteConverter.LONG_LENGTH_IN_BYTES,
                ByteConverter.INT_LENGTH_IN_BYTES);
        TableEntryPtr ptr = new TableEntryPtr();
        ptr.setPointer(data);
        return ptr;
    }
}
