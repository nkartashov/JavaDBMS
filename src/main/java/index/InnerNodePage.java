package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

import java.util.BitSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class InnerNodePage extends NodePage {

    public InnerNodePage(byte[] nodePageData, boolean page_is_new) {
        super(nodePageData, page_is_new);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, _node_page_data, PAGE_TYPE_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);
    }

    public long GetNextNodePointerForKey(int key) {
        int key_pos = -1;
        for(int i = 0; i != _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = GetKey(key_pos);
            if (key < current_key) {
                return GetNodePointerBeforeKey(key_pos);
            }
        }
        return GetLastNodePointer();
    }

    public void Insert() {

    }

    static public void InitPage(byte[] node_page_data, InnerNodeInsertEntry insert_entry) {
        byte[] page_type = ByteConverter.intToByte(TYPE);
        byte[] num_of_valid_keys = ByteConverter.intToByte(1);
        BitSet valid_keys_bitset = new BitSet(1);
        valid_keys_bitset.set(0, 1, true);
        byte[] valid_keys_table = ByteConverter.bitsetToBytes(valid_keys_bitset);

        System.arraycopy(page_type, 0, node_page_data, PAGE_TYPE_OFFSET, page_type.length);
        System.arraycopy(num_of_valid_keys, 0, node_page_data, NUM_OF_VALID_KEYS_OFFSET, num_of_valid_keys.length);
        System.arraycopy(valid_keys_table, 0, node_page_data, VALID_KEYS_TABLE_OFFSET, valid_keys_table.length);

        byte[] data = ArrayUtils.addAll(ByteConverter.longToByte(insert_entry.leftChildPtr()),
                                        ByteConverter.intToByte(0));                          //0 - trash
        data = ArrayUtils.addAll(data, ByteConverter.intToByte(insert_entry.key()));
        data = ArrayUtils.addAll(data, ByteConverter.longToByte(insert_entry.rightChildPtr()));
        System.arraycopy(data, 0, node_page_data, HEADER_SIZE, data.length);
    }

    static final int TYPE = 0;
}