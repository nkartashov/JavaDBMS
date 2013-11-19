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
    public Node(int children_number) {
        _children = new ArrayList<Object>(children_number);
    }

    private List<Object> _children;
}
