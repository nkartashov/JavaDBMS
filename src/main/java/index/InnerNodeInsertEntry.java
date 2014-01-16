package index;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/2/14
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class InnerNodeInsertEntry {

    public InnerNodeInsertEntry() {
        _left_child_ptr = -1;
        _right_child_ptr = -1;
    }

    public InnerNodeInsertEntry(int key, long left_child_ptr, long right_child_ptr) {
        _key = key;
        _left_child_ptr = left_child_ptr;
        _right_child_ptr = right_child_ptr;
    }

    public int key() { return _key; }
    public long leftChildPtr() { return _left_child_ptr; }
    public long rightChildPtr() { return _right_child_ptr; }

    public boolean isEmpty() { return _left_child_ptr == -1 && _right_child_ptr == -1; }

    private int _key;
    private long _left_child_ptr;
    private long _right_child_ptr;
}
