package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class InnerNodePage extends NodePage {

    public InnerNodePage(byte[] nodePageData, long self_ptr, boolean page_is_new) {
        super(nodePageData, self_ptr, page_is_new);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, _node_page_data, PAGE_TYPE_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);
    }

    static public void init(byte[] node_page_data, MoveUpElem insert_elem) {
        initHeaderAndType(node_page_data, 1);
        byte[] buffer = insert_elem.toByteArray();
        System.arraycopy(buffer, 0, node_page_data, HEADER_SIZE, buffer.length);
    }

    static public void init(byte[] node_page_data, byte[] entries, int entries_num) {
        initHeaderAndType(node_page_data, entries_num);
        System.arraycopy(entries, 0, node_page_data, HEADER_SIZE, entries.length);
    }

    public long nextNodePointer(int key) {
        int key_pos = -1;
        for(int i = 0; i != _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = getKey(key_pos);
            if (key <= current_key) {
                return nodePointerBeforeKey(key_pos);
            }
        }
        return lastNodePointer();
    }

    public void insertNotFull (MoveUpElem elem) {
        insertNotFull(elem.key(), elem.toByteArray());
    }

    public byte[] insertFull (MoveUpElem elem) {
        return insertFull(elem.key(), elem.toByteArray());
    }

    public void deleteLastEntry() {
        _valid_keys.clear(lastEntryPos());
        --_num_of_valid_keys;
        dumpChanges();
    }

    static private void initHeaderAndType(byte[] node_page_data, int entries_num) {
        initHeader(node_page_data, entries_num);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, node_page_data, PAGE_TYPE_OFFSET, page_type.length);
    }

    static final int TYPE = 0;
}