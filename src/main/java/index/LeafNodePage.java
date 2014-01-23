package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LeafNodePage extends NodePage {

    public LeafNodePage (byte[] nodePageData, long self_ptr, boolean page_is_new) {
        super(nodePageData, self_ptr, page_is_new);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, _node_page_data, PAGE_TYPE_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);
    }

    static public void init(byte[] node_page_data, byte[] entries, int entries_num) {
        initHeader(node_page_data, entries_num);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, node_page_data, PAGE_TYPE_OFFSET, page_type.length);
        System.arraycopy(entries, 0, node_page_data, HEADER_SIZE, entries.length);
    }

    static public void init(byte[] node_page_data) {
        initHeader(node_page_data, 0);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, node_page_data, PAGE_TYPE_OFFSET, page_type.length);
    }

    public TableEntryPtr tryFindKey(int key_to_be_found) {
        TableEntryPtr pointer = new TableEntryPtr();
        int key_pos = tryFindKeyIndex(key_to_be_found);
        if (key_pos != -1) {
            byte[] pointer_in_bytes = getEntryPtr(key_pos);
            pointer.setPointer(pointer_in_bytes);
        }
        return pointer;
    }

    public TableEntryPtr deleteKey(int key_to_be_deleted) {
        int key_pos = tryFindKeyIndex(key_to_be_deleted);
        if (key_pos != -1) {
            _valid_keys.clear(key_pos);
            _num_of_valid_keys -= 1;
            dumpChanges();

            byte[] raw_pointer = getEntryPtr(key_pos);
            TableEntryPtr pointer_to_deleted_entry = new TableEntryPtr();
            pointer_to_deleted_entry.setPointer(raw_pointer);
            return pointer_to_deleted_entry;
        }
        return new TableEntryPtr();
    }

    public void insertNotFull(int key, TableEntryPtr pointer) {
        byte[] buffer = ArrayUtils.addAll(pointer.toByteArray(), ByteConverter.intToByte(key));
        insertNotFull(key, buffer);
    }

    public byte[] insertFull(int key, TableEntryPtr pointer) {
        byte[] buffer = ArrayUtils.addAll(pointer.toByteArray(), ByteConverter.intToByte(key));
        return insertFull(key, buffer);
    }

    public void insertDuplicate(int key, TableEntryPtr pointer) {
        byte[] buffer = ArrayUtils.addAll(pointer.toByteArray(), ByteConverter.intToByte(key));
        insertDuplicate(key, buffer);
    }

    private int tryFindKeyIndex(int key_to_be_found) {
        int key_pos = -1;
        for(int i = 0; i != _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = getKey(key_pos);
            if (current_key == key_to_be_found) {
                return key_pos;
            }
        }
        return -1;
    }

    private byte[] getEntryPtr(int key_index_in_valid_keys_bitset) {
        if(key_index_in_valid_keys_bitset == -1) {
            return null;
        }
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = new byte[POINTER_SIZE];
        System.arraycopy(_node_page_data, pointer_offset, buffer, 0, POINTER_SIZE);
        return buffer;
    }

    static final int TYPE = 1;
}