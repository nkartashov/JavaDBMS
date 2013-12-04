package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class LeafNodePage extends NodePage {

    public LeafNodePage (byte[] nodePageData, boolean page_is_new) {
        super(nodePageData, page_is_new);
        byte[] page_type = ByteConverter.intToByte(TYPE);
        System.arraycopy(page_type, 0, _node_page_data, PAGE_TYPE_OFFSET, ByteConverter.INT_LENGTH_IN_BYTES);
    }

    public int TryFindKeyIndex (int key_to_be_found) {
        int key_pos = -1;
        for(int i = 0; i != _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = GetKey(key_pos);
            if (current_key == key_to_be_found) {
                return key_pos;
            }
        }
        return -1;
    }

    public TableEntryPointer TryFindKey (int key_to_be_found) {
        TableEntryPointer pointer = new TableEntryPointer();
        int key_pos = TryFindKeyIndex(key_to_be_found);
        if (key_pos != -1) {
            byte[] pointer_in_bytes = GetEntryPointerForKey(key_pos);
            pointer.SetPointer(pointer_in_bytes);
        }
        return pointer;
    }

    public TableEntryPointer DeleteKey(int key_to_be_deleted) {
        int key_pos = TryFindKeyIndex(key_to_be_deleted);
        if (key_pos != -1) {
            _valid_keys.set(key_pos, false);
            _num_of_valid_keys -= 1;
            DumpChanges();
        }
        byte[] raw_pointer = GetEntryPointerForKey(key_pos);
        TableEntryPointer pointer_to_deleted_entry = new TableEntryPointer();
        pointer_to_deleted_entry.SetPointer(raw_pointer);
        return pointer_to_deleted_entry;
    }

    public boolean Insert(int key, TableEntryPointer pointer, int key_to_be_pushed) {
        int first_free_position = _valid_keys.nextClearBit(0);
        if (first_free_position != -1) {
            _valid_keys.set(first_free_position, true);
            _num_of_valid_keys += 1;
            SetPointerAndKey(first_free_position, pointer, key);
            SortEntries();
            return false;
        }
        // list is full
        List<NodePageEntry> all_entries = GetEntriesList();
        NodePageEntry inserting_entry = new NodePageEntry(
                ArrayUtils.addAll(pointer.ToByteArray(), ByteConverter.intToByte(key)));
        all_entries.add(inserting_entry);
        Collections.sort(all_entries, new Comparator<NodePageEntry>() {
            public int compare(NodePageEntry e1, NodePageEntry e2) {
                if(e1.Key() > e2.Key()) { return 1; } else if (e1.Key() < e2.Key()) { return -1; }
                return 0;
            }
        });
        List<NodePageEntry> first_part = all_entries.subList(0, all_entries.size()/2);
        List<NodePageEntry> second_part = all_entries.subList(all_entries.size()/2, all_entries.size());
        WriteEntriesList(first_part);
        _valid_keys.clear();
        for(int i = 0; i != first_part.size(); ++i) { _valid_keys.set(i, true); }
        _num_of_valid_keys = first_part.size();

        //do something with the second part!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        return true;
    }

    public void SetPointerAndKey (int key_index_in_valid_keys_bitset, TableEntryPointer pointer, int key) {
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = pointer.ToByteArray();
        System.arraycopy(buffer, 0, _node_page_data, pointer_offset, POINTER_SIZE);
        System.arraycopy(ByteConverter.intToByte(key), 0, _node_page_data, pointer_offset + POINTER_SIZE, KEY_SIZE);
    }

    private byte[] GetEntryPointerForKey(int key_index_in_valid_keys_bitset) {
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = new byte[POINTER_SIZE];
        System.arraycopy(_node_page_data, pointer_offset, buffer, 0, POINTER_SIZE);
        return buffer;
    }

    static final int TYPE = 1;
}