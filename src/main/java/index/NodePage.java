package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.BitArray;
import utils.ByteConverter;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodePage {

    public NodePage(byte[] nodePageData, long self_ptr, boolean page_is_new) {
        _node_page_data = nodePageData;
        _self_ptr = self_ptr;
        if(page_is_new) {
            initHeader(_node_page_data, 0);
            _num_of_valid_keys = 0;
            _valid_keys = new BitArray(KEYS_MAX_NUM);
        }
        else {
            _num_of_valid_keys = ByteConverter.intFromByte(nodePageData, NUM_OF_VALID_KEYS_OFFSET);
            byte[] valid_keys_table = new byte[VALID_KEYS_TABLE_SIZE];
            System.arraycopy(_node_page_data, VALID_KEYS_TABLE_OFFSET, valid_keys_table, 0, VALID_KEYS_TABLE_SIZE);
            _valid_keys = new BitArray(valid_keys_table);
        }
    }

    static protected void initHeader(byte[] node_page_data, int valid_keys) {
        byte[] num_of_valid_keys = ByteConverter.intToByte(valid_keys);
        BitArray valid_keys_bitarray = new BitArray(KEYS_MAX_NUM);
        valid_keys_bitarray.set(0, valid_keys);
        byte[] valid_keys_table = valid_keys_bitarray.toByteArray();

        System.arraycopy(num_of_valid_keys, 0, node_page_data, NUM_OF_VALID_KEYS_OFFSET, num_of_valid_keys.length);
        System.arraycopy(valid_keys_table, 0, node_page_data, VALID_KEYS_TABLE_OFFSET, valid_keys_table.length);
    }

    protected int getKey(int key_index_in_valid_keys_bitset) {
        int key_offset = HEADER_SIZE + POINTER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        return ByteConverter.intFromByte(_node_page_data, key_offset);
    }

    protected long nodePointerBeforeKey (int key_index_in_valid_keys_bitset) {
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        return ByteConverter.longFromByte(_node_page_data, pointer_offset);
    }

    protected long lastNodePointer () {
        int after_last_entry_index = lastEntryPos() + 1;
        return nodePointerBeforeKey (after_last_entry_index);
    }

    protected int lastEntryPos() {
        int i = 0;
        int current_index = -1;
        while (i != _num_of_valid_keys) {
            current_index = _valid_keys.nextSetBit(current_index + 1);
            ++i;
        }
        return current_index;
    }

    protected void dumpChanges() {
        byte[] valid_keys_table = _valid_keys.toByteArray();
        System.arraycopy(valid_keys_table, 0, _node_page_data, VALID_KEYS_TABLE_OFFSET, VALID_KEYS_TABLE_SIZE);
        byte[] num_of_valid_keys = ByteConverter.intToByte(_num_of_valid_keys);
        System.arraycopy(num_of_valid_keys, 0, _node_page_data, NUM_OF_VALID_KEYS_OFFSET, num_of_valid_keys.length);
    }

    static public int getPageType(byte[] raw_node_page) {
        return ByteConverter.intFromByte(raw_node_page, PAGE_TYPE_OFFSET);
    }

    protected byte[] getRawEntry(int key) {
        int pointer_offset = HEADER_SIZE + key*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = new byte[POINTER_SIZE + KEY_SIZE];
        System.arraycopy(_node_page_data, pointer_offset, buffer, 0, POINTER_SIZE + KEY_SIZE);
        return buffer;
    }

    protected boolean isFull() {
        return _valid_keys.cardinality() == KEYS_MAX_NUM;
    }

    protected void moveDataRight(int from_pos) {
        int start_offset = HEADER_SIZE + from_pos*(POINTER_SIZE + KEY_SIZE);
        int end_offset = HEADER_SIZE + (lastEntryPos() + 1)*(POINTER_SIZE + KEY_SIZE) + POINTER_SIZE;
        byte[] buffer = new byte[end_offset - start_offset];
        System.arraycopy(_node_page_data, start_offset, buffer, 0, buffer.length);
        int start_plus_one = start_offset + POINTER_SIZE + KEY_SIZE;
        System.arraycopy(buffer, 0, _node_page_data, start_plus_one, buffer.length);

        BitArray val_keys_copy = new BitArray(_valid_keys.toByteArray());
        int to_pos = lastEntryPos() + 2;
        _valid_keys.clear(from_pos);
        for (int i = from_pos + 1; i < to_pos; ++i) {
            if(val_keys_copy.get(i - 1)) {
                _valid_keys.set(i);
            }
            else {
                _valid_keys.clear(i);
            }
        }
    }

    protected int findInsertPos(int key) {
        int key_pos = -1;
        boolean key_has_max_val = true;
        for(int i = 0; i < _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = getKey(key_pos);
            if(key == current_key) {
                return -1;
            }
            if(key < current_key) {
                key_has_max_val = false;
                break;
            }
        }
        if (key_has_max_val) {
            ++key_pos;
        }
        else if (key_pos != 0 && !_valid_keys.get(key_pos - 1)) {
             --key_pos;
        }
        else if (_num_of_valid_keys != 0) {
            moveDataRight(key_pos);
        }
        return key_pos;
    }

    protected byte[] split() {
        int middle = KEYS_MAX_NUM / 2;
        _num_of_valid_keys -= KEYS_MAX_NUM - middle;
        _valid_keys.clear(middle, _num_of_valid_keys);
        dumpChanges();

        int bytes_to_copy = (KEYS_MAX_NUM - middle) * (POINTER_SIZE + KEY_SIZE);
        byte[] second_part = new byte[bytes_to_copy];
        int middle_offset = HEADER_SIZE + middle * (POINTER_SIZE + KEY_SIZE);

        System.arraycopy(_node_page_data, middle_offset, second_part, 0, bytes_to_copy);
        return second_part;
    }

    public void insertNotFull(int key, byte[] data) {
        int insert_pos = findInsertPos(key);
        if(insert_pos == -1) {
            return;
        }
        _valid_keys.set(insert_pos);
        ++_num_of_valid_keys;
        dumpChanges();
        System.arraycopy(data, 0, _node_page_data, HEADER_SIZE + insert_pos*(POINTER_SIZE + KEY_SIZE), data.length);
    }

    public byte[] insertFull (int key, byte[] data) {
        int last_key = getKey(lastEntryPos());
        if(last_key == key) {
            return null;
        }
        byte[] last_entry = null;
        if (last_key > key) {
            last_entry = ArrayUtils.addAll(getRawEntry(lastEntryPos()), ByteConverter.longToByte(lastNodePointer()));
            _valid_keys.clear(lastEntryPos());
            --_num_of_valid_keys;
            insertNotFull(key, data);
        }
        byte[] second_part = split ();
        if(last_entry != null) {
            return ArrayUtils.addAll(second_part, last_entry);
        }
        return ArrayUtils.addAll(second_part, data);
    }

    protected byte[] _node_page_data;
    protected BitArray _valid_keys;
    protected int _num_of_valid_keys;
    public long _self_ptr;

    static public final int PAGE_SIZE = 4096;
    static public final int HEADER_SIZE = 96;
    static public final int KEY_SIZE = 4;
    static public final int POINTER_SIZE = 12;
    static public final int KEYS_MAX_NUM = (PAGE_SIZE - HEADER_SIZE - POINTER_SIZE) / (KEY_SIZE + POINTER_SIZE);
    static protected final int PAGE_TYPE_OFFSET = 0;
    static protected final int NUM_OF_VALID_KEYS_OFFSET = PAGE_TYPE_OFFSET + ByteConverter.INT_LENGTH_IN_BYTES;
    static protected final int VALID_KEYS_TABLE_OFFSET = NUM_OF_VALID_KEYS_OFFSET + ByteConverter.INT_LENGTH_IN_BYTES;
    static protected final int VALID_KEYS_TABLE_SIZE = (int) Math.ceil(KEYS_MAX_NUM / 8.0);
}


//    public void reInit(byte[] data, int keys_num) {
//        _num_of_valid_keys = keys_num;
//        _valid_keys.clear();
//        _valid_keys.set(0, keys_num);
//        System.arraycopy(data, 0, _node_page_data, HEADER_SIZE, data.length);
//        dumpChanges();
//    }

//    protected void WriteEntriesList(List<LeafNodeEntry> list) {
//        byte[] raw_all_entries = ByteConverter.leafEntriesToBytes(list);
//        System.arraycopy(raw_all_entries, 0, _node_page_data, HEADER_SIZE, raw_all_entries.length);
//    }

//    protected void SortEntries() {
//        List<LeafNodeEntry> all_entries = GetEntriesList();
//        Collections.sort(all_entries, new Comparator<LeafNodeEntry>() {
//            public int compare(LeafNodeEntry e1, LeafNodeEntry e2) {
//                if(e1.Key() > e2.Key()) { return 1; } else if (e1.Key() < e2.Key()) { return -1; }
//                return 0;
//            }
//        });
//        WriteEntriesList(all_entries);
//        _valid_keys.clear();
//        for(int i = 0; i != _num_of_valid_keys; ++i) { _valid_keys.set(i, true); }
//    }

//    protected List<LeafNodeEntry> GetEntriesList() {
//        List<LeafNodeEntry> entries_list = new ArrayList<LeafNodeEntry>();
//        int next_entry_pos = _valid_keys.nextSetBit(0);
//        while(next_entry_pos != -1) {
//            byte[] raw_entry = getRawEntry(next_entry_pos);
//            entries_list.add(new LeafNodeEntry(raw_entry));
//            next_entry_pos = _valid_keys.nextSetBit(next_entry_pos + 1);
//        }
//        return entries_list;
//    }