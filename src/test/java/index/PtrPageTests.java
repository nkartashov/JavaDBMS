package index;

import org.junit.Test;
import org.junit.Assert;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/23/14
 * Time: 7:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class PtrPageTests {

    @Test
    public void PtrPageTest() {
        byte[] raw_page = new byte[4096];
        PtrPage page = new PtrPage(raw_page, true);
        int i = 1;
        while(!page.isFull()) {
            TableEntryPtr ptr = new TableEntryPtr(i, i*10);
            page.add(ptr.toByteArray());
            ++i;
        }
        List<TableEntryPtr> ptrs = page.getPtrs();
        Assert.assertEquals(page.size(), ptrs.size());
        for(int j = 0; j < ptrs.size(); ++j) {
            Assert.assertEquals(j + 1, ptrs.get(j).pagePointer());
        }
    }
}
