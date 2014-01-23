package index;

import memoryManager.PageManager;
import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/23/14
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexFileTests {

    @Test
    public void InsertTest() {

        IndexFile index = new IndexFile(path, true);

        int k = 1000;
        for(int i = 1; i < k; ++i) {
            index.insertEntry(i, new TableEntryPtr(i*100, i*10));
        }
        for(int i = 1; i < k; ++i) {
            Assert.assertEquals(i * 100, index.tryFindEntries(i).get(0).pagePointer());
        }

        k = 100;
        for(int i = 0; i < k; ++i) {
            index.insertEntry(1001, new TableEntryPtr(i*100, i*10));
        }
        List<TableEntryPtr> ptrs1001 = index.tryFindEntries(1001);
        Assert.assertEquals(100, ptrs1001.size());
        for(int i = 0; i < k; ++i) {
            Assert.assertEquals(i*100, ptrs1001.get(i).pagePointer());
        }

        File index_file = new File(path);
        index_file.deleteOnExit();
        PageManager.getInstance().close();
    }

//    @Test
//    public void OpenIndexTest() {
//        String path = "/home/maratx/GitRepos/JavaDBMS/src/test/resources/index/test_index";
//        IndexFile index = new IndexFile(path, false);
//
//        Assert.assertEquals(500, index.tryFindEntry(5).pagePointer());
//        Assert.assertEquals(600, index.tryFindEntry(6).pagePointer());
//        Assert.assertEquals(true, index.tryFindEntry(0)._is_null);
//    }

    @Test
    public void DeleteTest() {

    }

    private String path = "/home/maratx/GitRepos/JavaDBMS/src/test/resources/index/test_index";
}
