package index;

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

    public NodePage(byte[] nodePageData, boolean page_is_new) {
        _node_page_data = nodePageData;
        if(page_is_new) {
            InitHeader(_node_page_data);
            _num_of_valid_keys = 0;
            _valid_keys = new BitSet(KEYS_MAX_NUM);
        }
        else {
            _num_of_valid_keys = ByteConverter.intFromByte(nodePageData, NUM_OF_VALID_KEYS_OFFSET);
            byte[] valid_keys_table = new byte[VALID_KEYS_TABLE_SIZE];
            System.arraycopy(_node_page_data, VALID_KEYS_TABLE_OFFSET, valid_keys_table, 0, VALID_KEYS_TABLE_SIZE);
            _valid_keys = BitSet.valueOf(valid_keys_table);
        }
    }

    static public void InitHeader(byte[] nodePageData) {
        byte[] num_of_valid_keys = ByteConverter.intToByte(0);
        System.arraycopy(num_of_valid_keys, 0, nodePageData, NUM_OF_VALID_KEYS_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);

        BitSet valid_keys_bitset = new BitSet(KEYS_MAX_NUM);
        byte[] valid_keys_table = ByteConverter.bitsetToBytes(valid_keys_bitset);
        System.arraycopy(valid_keys_table, 0, nodePageData, VALID_KEYS_TABLE_OFFSET, VALID_KEYS_TABLE_SIZE);
    }

    protected int GetKey(int key_index_in_valid_keys_bitset) {
        int key_offset = HEADER_SIZE + POINTER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        return ByteConverter.intFromByte(_node_page_data, key_offset);
    }

    protected long GetNodePointerBeforeKey (int key_index_in_valid_keys_bitset) {
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
//        byte[] buffer = new byte[POINTER_SIZE];
//        System.arraycopy(_node_page_data, pointer_offset, buffer, 0, POINTER_SIZE);
//        return ByteConverter.longFromByte(buffer, 0);
        return ByteConverter.longFromByte(_node_page_data, pointer_offset);
    }

    protected long GetLastNodePointer () {
        int i = 0;
        int current_index = -1;
        while (i != _num_of_valid_keys) {
            current_index = _valid_keys.nextSetBit(current_index + 1);
            ++i;
        }
        int last_key_index = current_index + 1; //??????????????????????????????????????????????????????????????????????????????????????????????????????????
        return GetNodePointerBeforeKey (last_key_index);
    }

    protected void DumpChanges () {
        byte[] valid_keys_table = ByteConverter.bitsetToBytes(_valid_keys);
        System.arraycopy(valid_keys_table, 0, _node_page_data, VALID_KEYS_TABLE_OFFSET, VALID_KEYS_TABLE_SIZE);
        byte[] num_of_valid_keys = ByteConverter.intToByte(_num_of_valid_keys);
        System.arraycopy(num_of_valid_keys, 0, _node_page_data, NUM_OF_VALID_KEYS_OFFSET, num_of_valid_keys.length);
    }

    static public int GetPageType(byte[] raw_node_page) {
        return ByteConverter.intFromByte(raw_node_page, PAGE_TYPE_OFFSET);
    }

    protected void SortEntries() {
        List<LeafNodeEntry> all_entries = GetEntriesList();
        Collections.sort(all_entries, new Comparator<LeafNodeEntry>() {
            public int compare(LeafNodeEntry e1, LeafNodeEntry e2) {
                if(e1.Key() > e2.Key()) { return 1; } else if (e1.Key() < e2.Key()) { return -1; }
                return 0;
            }
        });
        WriteEntriesList(all_entries);
        _valid_keys.clear();
        for(int i = 0; i != _num_of_valid_keys; ++i) { _valid_keys.set(i, true); }
    }

    protected List<LeafNodeEntry> GetEntriesList() {
        List<LeafNodeEntry> entries_list = new ArrayList<LeafNodeEntry>();
        int next_entry_pos = _valid_keys.nextSetBit(0);
        while(next_entry_pos != -1) {
            byte[] raw_entry = GetRawEntry(next_entry_pos);
            entries_list.add(new LeafNodeEntry(raw_entry));
            next_entry_pos = _valid_keys.nextSetBit(next_entry_pos + 1);
        }
        return entries_list;
    }

    private byte[] GetRawEntry(int key) {
        int pointer_offset = HEADER_SIZE + key*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = new byte[POINTER_SIZE + KEY_SIZE];
        System.arraycopy(_node_page_data, pointer_offset, buffer, 0, POINTER_SIZE + KEY_SIZE);
        return buffer;
    }

    protected void WriteEntriesList(List<LeafNodeEntry> list) {
        byte[] raw_all_entries = ByteConverter.leafEntriesToBytes(list);
        System.arraycopy(raw_all_entries, 0, _node_page_data, HEADER_SIZE, raw_all_entries.length);
    }

    protected byte[] _node_page_data;
    protected BitSet _valid_keys;
    protected int _num_of_valid_keys;

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