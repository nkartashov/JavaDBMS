package index;

import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/20/13
 * Time: 2:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class Node {
    public Node() {
        _keys = new ArrayList<Object>();
    }

//    public void DeleteKey(Object key) {
//        ByteUtil._keys.set(_keys.indexOf(key), null);
//        int keyOffset = getOffset(key);
//        writePagePointer(_nodeData, keyOffset, 0);
//    }
//
//    public Node getNextNode(Object key, PageProvider provider) {
//        int keyOffset = getOffset(key);
//        PagePointer pp = ByteUtil.readPagePointer(keyOffset, _nodeData);
//        return new Node(provider.getRawPage(pp));
//    }

    private byte[] _nodeData;
    private List<Object> _keys;
}