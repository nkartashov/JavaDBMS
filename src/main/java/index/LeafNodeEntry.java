package index;

import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 12/4/13
 * Time: 2:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class LeafNodeEntry {

    public LeafNodeEntry(byte[] full_entry) {
        _full_entry = full_entry;
        _key = ByteConverter.intFromByte(full_entry, NodePage.POINTER_SIZE);
    }

    public int Key() { return _key; }
    public byte[] FullEntry() { return _full_entry; }

    private int _key;
    private byte[] _full_entry;
}
