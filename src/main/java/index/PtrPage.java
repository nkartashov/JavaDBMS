package index;

import memoryManager.DiskPage;
import utils.ByteConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/23/14
 * Time: 5:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class PtrPage extends DiskPage {

    public PtrPage(byte[] rawPage, boolean blankPage) {
        super(rawPage, blankPage);
        if(blankPage) {
            System.arraycopy(ByteConverter.intToByte(PTRS_MAX_NUM), 0, _rawPage, PTRS_MAX_NUM_OFFSET, 4);
            System.arraycopy(ByteConverter.intToByte(DATA_OFFSET), 0, _rawPage, LAST_PTR_POS_OFFSET, 4);
            _lastPtrPos = DATA_OFFSET;
        }
        else {
            _lastPtrPos = ByteConverter.intFromByte(_rawPage, LAST_PTR_POS_OFFSET);
        }
    }

    public boolean isFull() {
        return _lastPtrPos + PTR_SIZE > MAX_PAGE_SIZE;
    }

    public int size() {
        return (_lastPtrPos - DATA_OFFSET) / PTR_SIZE;
    }

    public boolean add(byte[] ptr) {
        if(isFull()) {
            return false;
        }
        System.arraycopy(ptr, 0, _rawPage, _lastPtrPos, PTR_SIZE);
        _lastPtrPos += PTR_SIZE;
        dumpChanges();
        return true;
    }

    public List<TableEntryPtr> getPtrs() {
        if (size() == 0) {
            return new ArrayList<TableEntryPtr>();
        }
        List<TableEntryPtr> ptrs = new ArrayList<TableEntryPtr>();
        int pos = DATA_OFFSET;
        for (int i = 0; i < size(); ++i) {
            ptrs.add(new TableEntryPtr(ByteConverter.longFromByte(_rawPage, pos),
                                       ByteConverter.intFromByte(_rawPage, pos + 8)));
            pos += PTR_SIZE;
        }
        return ptrs;
    }

    private void dumpChanges() {
        System.arraycopy(ByteConverter.intToByte(_lastPtrPos), 0, _rawPage, LAST_PTR_POS_OFFSET, 4);
    }

    static private final int PTR_SIZE = 12;
    static public final int PTRS_MAX_NUM = DATA_PAGE_SIZE_IN_BYTES / PTR_SIZE;
    private final int PTRS_MAX_NUM_OFFSET = 16;
    private int _lastPtrPos;
    private final int LAST_PTR_POS_OFFSET = 20;
}
