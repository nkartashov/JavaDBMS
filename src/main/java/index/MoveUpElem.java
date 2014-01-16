package index;

import org.apache.commons.lang3.ArrayUtils;
import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/3/14
 * Time: 6:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveUpElem {

    public MoveUpElem(int key, long left_child_ptr, long right_child_ptr) {
        _key = key;
        _left_child_ptr = left_child_ptr;
        _right_child_ptr = right_child_ptr;
    }

    public byte[] toByteArray() {
        //0 - trash
        byte[] data = ArrayUtils.addAll(ByteConverter.longToByte(_left_child_ptr), ByteConverter.intToByte(0));
        data = ArrayUtils.addAll(data, ByteConverter.intToByte(_key));
        data = ArrayUtils.addAll(data, ByteConverter.longToByte(_right_child_ptr));
        return ArrayUtils.addAll(data, ByteConverter.intToByte(0));
    }

    public int key() { return _key; }
    public long leftChildPtr() { return _left_child_ptr; }
    public long rightChildPtr() { return _right_child_ptr; }

    private int _key;
    private long _left_child_ptr;
    private long _right_child_ptr;
}
