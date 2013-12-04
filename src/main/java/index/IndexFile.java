package index;

import memoryManager.PageId;
import memoryManager.PageManager;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/27/13
 * Time: 12:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexFile {

    public IndexFile (String file_name) {
        _file_name = file_name;
    }

    public TableEntryPointer TryFindEntry(int key) {
        byte[] node_raw_page = null;
        PageId page_id = FindLeafNodeRawPageForKey(key, node_raw_page);
        LeafNodePage leaf_node = new LeafNodePage(node_raw_page, false);
        TableEntryPointer entry_pointer = leaf_node.TryFindKey(key);
        _page_manager.releasePage(page_id);
        return entry_pointer;
    }

    public TableEntryPointer TryDeleteKey(int key) {
        byte[] node_raw_page = null;
        PageId page_id = FindLeafNodeRawPageForKey(key, node_raw_page);
        LeafNodePage leaf_node = new LeafNodePage(node_raw_page, false);
        TableEntryPointer entry_pointer = leaf_node.DeleteKey(key);
        _page_manager.releasePage(page_id);
        return entry_pointer;
    }

    private PageId FindLeafNodeRawPageForKey(int key, byte[] node_raw_page) {
        long page_pointer = ROOT_POINTER;
        PageId page_id;
        while (true) {
            page_id = new PageId(_file_name, page_pointer);
            node_raw_page = _page_manager.getPage(page_id);
            int page_type = NodePage.GetPageType(node_raw_page);
            if (page_type == LeafNodePage.TYPE) {
                break;
            }
            InnerNodePage current_node = new InnerNodePage (node_raw_page, false);
            page_pointer = current_node.GetNextNodePointerForKey(key);
            _page_manager.releasePage(page_id);
        }
        return page_id;
    }

    private String _file_name;
    private final long ROOT_POINTER = 0;
    private static PageManager _page_manager = PageManager.getInstance();
}
