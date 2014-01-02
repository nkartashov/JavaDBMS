package index;

import memoryManager.PageId;
import memoryManager.PageManager;

import java.util.List;

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

    public void InsertEntry(int key, TableEntryPointer table_entry_pointer) {
        PageId root_page_id = new PageId(_file_name, ROOT_POINTER);
        InnerNodeInsertEntry insert_entry = InsertInBTree(key, table_entry_pointer, root_page_id);
        if(!insert_entry.isEmpty()) {
            PageId new_root_page_id = new PageId(_file_name, 0);
            byte[] new_root_raw_page = _page_manager.createPage(new_root_page_id);
            InnerNodePage.InitPage(new_root_raw_page, insert_entry);
            _page_manager.releasePage(new_root_page_id);
            ROOT_POINTER = new_root_page_id.getPageNumber();
        }
    }

    private InnerNodeInsertEntry InsertInBTree(int key, TableEntryPointer table_entry_pointer, PageId page_id) {
        byte[] node_raw_page = _page_manager.getPage(page_id);
        int page_type = NodePage.GetPageType(node_raw_page);
        if (page_type == LeafNodePage.TYPE) {
            LeafNodePage leaf_node = new LeafNodePage(node_raw_page, false);
            List<LeafNodeEntry> new_page_entries = leaf_node.Insert(key, table_entry_pointer);
            if(new_page_entries != null) {
                PageId new_page_id = new PageId(_file_name, 0);
                byte[] new_raw_page = _page_manager.createPage(new_page_id);
                LeafNodePage.InitPage(new_raw_page, new_page_entries);
                _page_manager.releasePage(new_page_id);
                _page_manager.releasePage(page_id);
                return new InnerNodeInsertEntry(new_page_entries.get(new_page_entries.size() - 1).Key(),
                                                page_id.getPageNumber(),
                                                new_page_id.getPageNumber());
            }
            _page_manager.releasePage(page_id);
            return new InnerNodeInsertEntry();
        }
        InnerNodePage cur_node = new InnerNodePage (node_raw_page, false);

        InnerNodeInsertEntry insert_entry = InsertInBTree(key,
                                                          table_entry_pointer,
                                                          new PageId(_file_name, cur_node.GetNextNodePointerForKey(key)));
        if(!insert_entry.isEmpty()) {
            //InnerNode insert
        }
        _page_manager.releasePage(page_id);
        return new InnerNodeInsertEntry();
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
            page_pointer = current_node.GetNextNodePointerForKey(key);               //Returns first 8 bytes (only page pointer, not row pointer!)
            _page_manager.releasePage(page_id);
        }
        return page_id;
    }

    private String _file_name;
    private long ROOT_POINTER = 0;
    private static PageManager _page_manager = PageManager.getInstance();
}
