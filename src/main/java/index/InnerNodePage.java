package index;

import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/24/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class InnerNodePage extends NodePage {

    public InnerNodePage(byte[] nodePageData, boolean page_is_new) {
        super(nodePageData, page_is_new);
    }

    public long GetNextNodePointerForKey(int key) {
        int key_pos = -1;
        for(int i = 0; i != _num_of_valid_keys; ++i) {
            key_pos = _valid_keys.nextSetBit(key_pos + 1);
            int current_key = GetKey(key_pos);
            if (key < current_key) {
                return GetPointerBeforeKey(key_pos);
            }
        }
        return GetLastPointer();
    }
}
