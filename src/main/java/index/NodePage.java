package index;

import utils.ByteConverter;

import java.util.BitSet;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 5:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodePage {

    public NodePage(byte[] nodePageData, boolean page_is_new) {
        _nodePageData = nodePageData;
        if(page_is_new) {
            InitHeader(_nodePageData);
            _num_of_valid_keys = 0;
            _valid_keys = new BitSet(KEYS_MAX_NUM);
        }
        else {
            _num_of_valid_keys = ByteConverter.intFromByte(nodePageData, 0);
            byte[] valid_keys_table = new byte[VALID_KEYS_TABLE_SIZE];
            System.arraycopy(_nodePageData, VALID_KEYS_TABLE_OFFSET, valid_keys_table, 0, VALID_KEYS_TABLE_SIZE);
            _valid_keys = BitSet.valueOf(valid_keys_table);
        }
    }

    static public void InitHeader(byte[] nodePageData) {
        byte[] header = ByteConverter.intToByte(0);
        System.arraycopy(header, 0, nodePageData, 0, ByteConverter.INT_LENGTH_IN_BYTES);

        BitSet valid_keys_bitset = new BitSet(KEYS_MAX_NUM);
        byte[] valid_keys_table = ByteConverter.bitsetToBytes(valid_keys_bitset);
        System.arraycopy(valid_keys_table, 0, nodePageData, VALID_KEYS_TABLE_OFFSET, VALID_KEYS_TABLE_SIZE);
    }

//    public void DeleteKey(int key) {
//        for(int i = 0; i != KEYS_MAX_NUM; ++i) {
//            if (_valid_keys.get(i) == true) {
//                int current_key_offset = HEADER_SIZE + POINTER_SIZE + i*(KEY_SIZE + POINTER_SIZE);
//                int current_key = ByteConverter.intFromByte(_nodePageData, current_key_offset);
//                if(key == current_key) {
//                    _valid_keys.set();
//                }
//            }
//        }
//    }

    public int GetKey(int key_index_in_valid_keys_bitset) {
        int key_offset = HEADER_SIZE + POINTER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        return ByteConverter.intFromByte(_nodePageData, key_offset);
    }

    public long GetPointerBeforeKey (int key_index_in_valid_keys_bitset) {
        int pointer_offset = HEADER_SIZE + key_index_in_valid_keys_bitset*(KEY_SIZE + POINTER_SIZE);
        byte[] buffer = new byte[POINTER_SIZE];
        System.arraycopy(_nodePageData, pointer_offset, buffer, 0, POINTER_SIZE);
        return ByteConverter.longFromByte(buffer, 0);
    }

    public long GetLastPointer () {
        int i = 0;
        int current_index = -1;
        while (i != _num_of_valid_keys) {
            current_index = _valid_keys.nextSetBit(current_index + 1);
            ++i;
        }
        int last_key_index = current_index + 1; //??????????????????????????????????????????????????????????????????????????????????????????????????????????
        return GetPointerBeforeKey (last_key_index);
    }

    public void DumpChanges () {
        byte[] valid_keys_table = ByteConverter.bitsetToBytes(_valid_keys);
        System.arraycopy(valid_keys_table, 0, _nodePageData, VALID_KEYS_TABLE_OFFSET, VALID_KEYS_TABLE_SIZE);
    }

    protected byte[] _nodePageData;
    protected BitSet _valid_keys;
    protected int _num_of_valid_keys;

    static public final int PAGE_SIZE = 4096;
    static public final int HEADER_SIZE = 96;
    static public final int KEY_SIZE = 4;
    static public final int POINTER_SIZE = 8;
    static public final int KEYS_MAX_NUM = 332;//(PAGE_SIZE - HEADER_SIZE - POINTER_SIZE) / (KEY_SIZE + POINTER_SIZE);
    static protected final int VALID_KEYS_TABLE_OFFSET = ByteConverter.INT_LENGTH_IN_BYTES;
    static protected final int VALID_KEYS_TABLE_SIZE = 42;
}