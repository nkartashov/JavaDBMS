package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/27/13
 * Time: 1:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableEntryPtr {

    public TableEntryPtr() {
        _is_null = true;
    }

    public void setPointer(byte[] entry_pointer) {
        if (entry_pointer.length != 0) {
            _page_pointer = ByteConverter.longFromByte(entry_pointer, 0);
            _row_pointer = ByteConverter.intFromByte(entry_pointer, ByteConverter.LONG_LENGTH_IN_BYTES);
            _is_null = false;
        }
        else {
            _is_null = true;
        }
    }

    public byte[] toByteArray() {
        return ArrayUtils.addAll(ByteConverter.longToByte(_page_pointer), ByteConverter.intToByte(_row_pointer));
    }

    public long pagePointer () { return  _page_pointer; }
    public int rowPointer () { return _row_pointer; }

    public boolean _is_null;

    private long _page_pointer;
    private int _row_pointer;
}
