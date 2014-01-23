package index;

import dbCommands.RowPredicate;
import dbEnvironment.DbContext;
import memoryManager.PageId;
import memoryManager.PageManager;
import memoryManager.TableIterator;
import utils.ByteConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 11/27/13
 * Time: 12:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexFile {

    public IndexFile (String file_name, boolean is_new) {
        _file_name = file_name;
        if(is_new) {
            init();
        }
        else {
            open();
        }
    }

    public IndexFile (String file_name, String table_name, int field_no, DbContext context) {
        _file_name = file_name;
        init();
        createIndex(table_name, field_no, context);
    }

    public void createIndex(String table_name, int field_no, DbContext context) {
        TableIterator iter = new TableIterator(context, table_name);
        while(!iter.isFinished()) {
            List<Object> entry = iter.nextRow();
            TableEntryPtr entry_ptr = iter.tableEntryPtr();
            insertEntry((Integer) entry.get(field_no), entry_ptr);
        }
    }

    public List<TableEntryPtr> select(RowPredicate predicate) {
        //if predicate is simple and contains "=" condition
        return tryFindEntries(Integer.getInteger(predicate.conditions().get(0)._val2));
    }

    public List<TableEntryPtr> tryFindEntries(int key) {
        TableEntryPtr list_head = tryFindKey(key);
        if(list_head._is_null) {
            return new ArrayList<TableEntryPtr>();
        }
        return getTableEntryPtrList(list_head);
    }

    private TableEntryPtr tryFindKey(int key) {
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
        TableEntryPtr list_head = tryFindKey(key);
        TableEntryPtr new_list_head;
        if(!list_head._is_null) {
            new_list_head = insertInList(list_head.pagePointer(), table_entry_pointer);
            if(new_list_head._is_null) {
                return;
            }
        }
        else {
            new_list_head = createNewList(table_entry_pointer);
        }

        PageId root_page_id = new PageId(_file_name, _root_ptr);
        byte[] root_page = _page_manager.getPage(root_page_id);
        MoveUpElem new_root_elem = insertInBTree(key, new_list_head, root_page, root_page_id.getPageNumber());
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
        LeafNodePage.init(new_page, second_part, LeafNodePage.KEYS_MAX_NUM / 2 + 1);
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
        InnerNodePage.init(new_page, second_part, LeafNodePage.KEYS_MAX_NUM / 2 + 1);
        MoveUpElem elem = new MoveUpElem(inner_node.getKey(inner_node.lastEntryPos()),
                inner_node._self_ptr,
                new_page_id.getPageNumber());
        inner_node.deleteLastEntry();
        _page_manager.updateAndReleasePage(new_page_id, new_page);
        return elem;
    }

    private void init() {
        PageId header_page_id = new PageId(_file_name, 0);
        byte[] header_page = _page_manager.createPage(header_page_id);
        _header_page_num = header_page_id.getPageNumber();

        PageId root_page_id = new PageId(_file_name, 0);
        byte[] root_page = _page_manager.createPage(root_page_id);
        _root_ptr = root_page_id.getPageNumber();
        LeafNodePage.init(root_page);
        _page_manager.updateAndReleasePage(root_page_id, root_page);

        System.arraycopy(ByteConverter.longToByte(_root_ptr), 0, header_page, ROOT_PTR_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
        _page_manager.updateAndReleasePage(header_page_id, header_page);
        int i = 0;
    }

    private void open() {
        PageId header_page_id = new PageId(_file_name, _header_page_num);
        byte[] header_page = _page_manager.getPage(header_page_id);
        byte[] root_ptr = new byte[ByteConverter.LONG_LENGTH_IN_BYTES];
        System.arraycopy(header_page, ROOT_PTR_OFFSET, root_ptr, 0, ByteConverter.LONG_LENGTH_IN_BYTES);
        _root_ptr = ByteConverter.longFromByte(root_ptr, 0);
        _page_manager.releasePage(header_page_id);
    }

    private void updateRootPtr() {
        PageId header_page_id = new PageId(_file_name, _header_page_num);
        byte[] header_page = _page_manager.getPage(header_page_id);
        System.arraycopy(ByteConverter.longToByte(_root_ptr), 0, header_page, ROOT_PTR_OFFSET, ByteConverter.LONG_LENGTH_IN_BYTES);
        _page_manager.updateAndReleasePage(header_page_id, header_page);
    }

    private TableEntryPtr insertInList(long head_ptr, TableEntryPtr table_entry_ptr) {
        PageId head_page_id = new PageId(_file_name, head_ptr);
        byte[] raw_head = _page_manager.getPage(head_page_id);
        PtrPage list_head = new PtrPage(raw_head, false);
        TableEntryPtr new_head_ptr = new TableEntryPtr();
        if(!list_head.add(table_entry_ptr.toByteArray())) {
            PageId new_head_page_id = new PageId(_file_name, 0);
            byte[] new_raw_head = _page_manager.createPage(new_head_page_id);
            PtrPage new_list_head = new PtrPage(new_raw_head, true);
            new_list_head.add(table_entry_ptr.toByteArray());
            new_list_head.setNextPageIndex(head_page_id.getPageNumber());
            list_head.setPrevPageIndex(new_head_page_id.getPageNumber());
            new_head_ptr.setPointer(new_head_page_id.getPageNumber(), 0);
            _page_manager.updateAndReleasePage(new_head_page_id, new_raw_head);
            _page_manager.releasePage(head_page_id);
        }
        else {
            _page_manager.updateAndReleasePage(head_page_id, raw_head);
        }
        return new_head_ptr;
    }

    private TableEntryPtr createNewList(TableEntryPtr table_entry_ptr) {
        PageId new_head_page_id = new PageId(_file_name, 0);
        byte[] new_raw_head = _page_manager.createPage(new_head_page_id);
        PtrPage new_list_head = new PtrPage(new_raw_head, true);
        new_list_head.add(table_entry_ptr.toByteArray());
        _page_manager.updateAndReleasePage(new_head_page_id, new_raw_head);
        return new TableEntryPtr(new_head_page_id.getPageNumber(), 0);
    }

    private List<TableEntryPtr> getTableEntryPtrList(TableEntryPtr list_head_ptr) {
        PageId head_page_id = new PageId(_file_name, list_head_ptr.pagePointer());
        byte[] raw_head = _page_manager.getPage(head_page_id);
        PtrPage list_elem = new PtrPage(raw_head, false);
        List<TableEntryPtr> res = list_elem.getPtrs();
        while (list_elem.nextPageIndex() != PtrPage.NULL_PTR && res.size() <= PtrPage.PTRS_MAX_NUM) {
            PageId next_page_id = new PageId(_file_name, list_elem.nextPageIndex());
            byte[] raw_page = _page_manager.getPage(next_page_id);
            list_elem = new PtrPage(raw_page, false);
            res.addAll(list_elem.getPtrs());
            _page_manager.releasePage(next_page_id);
        }
        _page_manager.releasePage(head_page_id);
        return res;
    }

    private String _file_name;
    private long _header_page_num = 0;
    private int ROOT_PTR_OFFSET = 0;
    private long _root_ptr;
    private static PageManager _page_manager = PageManager.getInstance();
    private boolean _page_has_been_updated = false;
}