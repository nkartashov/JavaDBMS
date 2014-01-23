package index;

import memoryManager.PageId;
import memoryManager.PageManager;
import utils.ByteConverter;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/27/13
 * Time: 12:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexFile {

    public IndexFile (String file_name, String table_name, int field_no) {
        _file_name = file_name;
        PageId header_page_id = new PageId(_file_name, _header_page_num);
        byte[] header_page = _page_manager.createPage(header_page_id);
        System.arraycopy(ByteConverter.longToByte(_root_ptr), 0, header_page, ROOT_PTR_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
        _page_manager.updateAndReleasePage(header_page_id, header_page);
        createIndex(table_name, field_no);
    }

    public void createIndex(String table_name, int field_no) {

    }

    public TableEntryPtr tryFindEntry(int key) {
        Object[] ret_values = findLeafNodePage(key);
        PageId page_id = (PageId) ret_values[0];
        byte[] node_raw_page = (byte[]) ret_values[1];

        LeafNodePage leaf_node = new LeafNodePage(node_raw_page, page_id.getPageNumber(), false);
        TableEntryPtr entry_pointer = leaf_node.tryFindKey(key);
        _page_manager.releasePage(page_id);
        return entry_pointer;
    }

    public TableEntryPtr tryDeleteKey(int key) {
        Object[] ret_values = findLeafNodePage(key);
        PageId page_id = (PageId) ret_values[0];
        byte[] node_raw_page = (byte[]) ret_values[1];

        LeafNodePage leaf_node = new LeafNodePage(node_raw_page, page_id.getPageNumber(), false);
        TableEntryPtr entry_pointer = leaf_node.deleteKey(key);
        _page_manager.releasePage(page_id);
        return entry_pointer;
    }

    public void insertEntry(int key, TableEntryPtr table_entry_pointer) {
        PageId root_page_id = new PageId(_file_name, _root_ptr);
        byte[] root_page = _page_manager.getPage(root_page_id);
        MoveUpElem new_root_elem = insertInBTree(key, table_entry_pointer, root_page, root_page_id.getPageNumber());
        if(new_root_elem != null) {
            createNewRootNode(new_root_elem);
        }
        if(_page_has_been_updated) {
            _page_manager.updateAndReleasePage(root_page_id, root_page);
            _page_has_been_updated = false;
        }
        else {
            _page_manager.releasePage(root_page_id);
        }
    }

    private MoveUpElem insertInBTree(int key, TableEntryPtr table_entry_ptr, byte[] raw_node_page, long node_ptr) {
        if(NodePage.getPageType(raw_node_page) == LeafNodePage.TYPE) {
            LeafNodePage leaf_node = new LeafNodePage(raw_node_page, node_ptr, false);
            if(leaf_node.isFull()) {
                return splitLeafNode(leaf_node, key, table_entry_ptr);
            }
            else {
                leaf_node.insertNotFull(key, table_entry_ptr);
                _page_has_been_updated = true;
                return null;
            }
        }
        InnerNodePage current_node = new InnerNodePage(raw_node_page, node_ptr, false);
        PageId next_node_id = new PageId(_file_name, current_node.nextNodePointer(key));
        byte[] raw_next_node_page = _page_manager.getPage(next_node_id);
        MoveUpElem elem = insertInBTree(key, table_entry_ptr, raw_next_node_page, next_node_id.getPageNumber());
        if (_page_has_been_updated) {
            _page_manager.updateAndReleasePage(next_node_id, raw_next_node_page);
            _page_has_been_updated = false;
        }
        else {
            _page_manager.releasePage(next_node_id);
        }

        if(elem != null) {
            if(current_node.isFull()) {
                return splitInnerNode(current_node, elem);
            }
            else {
                current_node.insertNotFull(elem);
                _page_has_been_updated = true;
                return null;
            }
        }
        return null;
    }

    private Object[] findLeafNodePage(int key) {
        byte[] node_raw_page;
        long page_pointer = _root_ptr;
        PageId page_id;
        while (true) {
            page_id = new PageId(_file_name, page_pointer);
            node_raw_page = _page_manager.getPage(page_id);
            int page_type = NodePage.getPageType(node_raw_page);
            if (page_type == LeafNodePage.TYPE) {
                break;
            }
            InnerNodePage current_node = new InnerNodePage (node_raw_page, page_id.getPageNumber(), false);
            page_pointer = current_node.nextNodePointer(key);               //Returns first 8 bytes (only page pointer, not row pointer!)
            _page_manager.releasePage(page_id);
        }
        Object[] return_values = new Object[2];
        return_values[0] = page_id;
        return_values[1] = node_raw_page;
        return return_values;
    }

    private void createNewRootNode(MoveUpElem new_root_elem) {
        PageId new_page_id = new PageId(_file_name, 0);
        byte[] new_root_page = _page_manager.createPage(new_page_id);
        InnerNodePage.init(new_root_page, new_root_elem);
        _root_ptr = new_page_id.getPageNumber();
        updateRootPtr();
        _page_manager.updateAndReleasePage(new_page_id, new_root_page);
    }

    private MoveUpElem splitLeafNode(LeafNodePage leaf_node, int key, TableEntryPtr table_entry_ptr) {
        byte[] second_part = leaf_node.insertFull(key, table_entry_ptr);
        _page_has_been_updated = true;
        PageId new_page_id = new PageId(_file_name, 0);
        byte[] new_page = _page_manager.createPage(new_page_id);
        LeafNodePage.init(new_page, second_part, LeafNodePage.KEYS_MAX_NUM - LeafNodePage.KEYS_MAX_NUM / 2);
        MoveUpElem elem = new MoveUpElem(leaf_node.getKey(leaf_node.lastEntryPos()),
                                         leaf_node._self_ptr,
                                         new_page_id.getPageNumber());
        _page_manager.updateAndReleasePage(new_page_id, new_page);
        return elem;
    }

    private MoveUpElem splitInnerNode(InnerNodePage inner_node, MoveUpElem elem_to_insert) {
        byte[] second_part = inner_node.insertFull(elem_to_insert);
        _page_has_been_updated = true;
        PageId new_page_id = new PageId(_file_name, 0);
        byte[] new_page = _page_manager.createPage(new_page_id);
        InnerNodePage.init(new_page, second_part, LeafNodePage.KEYS_MAX_NUM - LeafNodePage.KEYS_MAX_NUM / 2);
        MoveUpElem elem = new MoveUpElem(inner_node.getKey(inner_node.lastEntryPos()),
                inner_node._self_ptr,
                new_page_id.getPageNumber());
        inner_node.deleteLastEntry();
        _page_manager.updateAndReleasePage(new_page_id, new_page);
        return elem;
    }

    private void updateRootPtr() {
        PageId header_page_id = new PageId(_file_name, _header_page_num);
        byte[] header_page = _page_manager.getPage(header_page_id);
        System.arraycopy(ByteConverter.longToByte(_root_ptr), 0, header_page, ROOT_PTR_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
        _page_manager.updateAndReleasePage(header_page_id, header_page);
    }

    private String _file_name;
    private long _header_page_num = 0;
    private int ROOT_PTR_OFFSET = 0;
    private long _root_ptr = 0;
    private static PageManager _page_manager = PageManager.getInstance();
    private boolean _page_has_been_updated = false;
}

//    public void InsertEntry(int key, TableEntryPtr table_entry_pointer) {
//        PageId root_page_id = new PageId(_file_name, _root_ptr);
//        InnerNodeInsertEntry insert_entry = InsertInBTree(key, table_entry_pointer, root_page_id);
//        if(!insert_entry.isEmpty()) {
//            PageId new_root_page_id = new PageId(_file_name, 0);
//            byte[] new_root_raw_page = _page_manager.createPage(new_root_page_id);
//            InnerNodePage.init(new_root_raw_page, insert_entry);
//            _page_manager.releasePage(new_root_page_id);
//            _root_ptr = new_root_page_id.getPageNumber();
//        }
//    }
//
//    private InnerNodeInsertEntry InsertInBTree(int key, TableEntryPtr table_entry_pointer, PageId page_id) {
//        byte[] node_raw_page = _page_manager.getPage(page_id);
//        if (NodePage.getPageType(node_raw_page) == LeafNodePage.TYPE) {
//            LeafNodePage leaf_node = new LeafNodePage(node_raw_page, false);
//            List<LeafNodeEntry> new_page_entries = leaf_node.insertNotFull(key, table_entry_pointer);
//            if(new_page_entries != null) {
//                PageId new_page_id = new PageId(_file_name, 0);
//                byte[] new_raw_page = _page_manager.createPage(new_page_id);
//                LeafNodePage.init(new_raw_page, new_page_entries);
//                _page_manager.releasePage(new_page_id);
//                _page_manager.releasePage(page_id);
//                return new InnerNodeInsertEntry(new_page_entries.get(new_page_entries.size() - 1).Key(),
//                                                page_id.getPageNumber(),
//                                                new_page_id.getPageNumber());
//            }
//            _page_manager.releasePage(page_id);
//            return new InnerNodeInsertEntry();
//        }
//        InnerNodePage cur_node = new InnerNodePage (node_raw_page, false);
//        InnerNodeInsertEntry insert_entry = InsertInBTree(key,
//                                                          table_entry_pointer,
//                                                          new PageId(_file_name, cur_node.nextNodePointer(key)));
//        if(!insert_entry.isEmpty()) {
//            //InnerNode insert
//        }
//        _page_manager.releasePage(page_id);
//        return new InnerNodeInsertEntry();
//    }

//    public void InsertEntry(int key, TableEntryPtr table_entry_pointer) {
//        PageId root_page_id = new PageId(_file_name, _root_ptr);
//        byte[] root_page = _page_manager.getPage(root_page_id);
//        if(NodePage.getPageType(root_page) == LeafNodePage.TYPE) {
//            LeafNodePage root_node = new LeafNodePage(root_page, false);
//            if(root_node.isFull()) {
//                MoveUpElem new_root_elem = SplitLeafNode(root_node, key, table_entry_pointer);
//                createNewRootNode(new_root_elem);
//            }
//            else {
//                root_node.insertNotFull(key, table_entry_pointer);
//            }
//        }
//        else {
//            InnerNodePage root_node = new InnerNodePage(root_page, false);
//            MoveUpElem elem = InsertInBTree(key, table_entry_pointer, root_page);
//            if(elem != null) {
//                if(root_node.isFull()) {
//                    MoveUpElem new_root_elem = root_node.insertFull(elem);
//                    createNewRootNode(new_root_elem);
//                }
//                else {
//                    root_node.insertNotFull(elem);
//                }
//            }
//        }
//        _page_manager.releasePage(root_page_id);
//    }