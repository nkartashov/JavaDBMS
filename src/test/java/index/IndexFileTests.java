package index;

import org.junit.Test;
import org.junit.Assert;
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
        String path = "/home/maratx/GitRepos/JavaDBMS/src/test/resources/index/test_index";
        IndexFile index = new IndexFile(path);

        TableEntryPtr ptr = new TableEntryPtr();
        index.insertEntry(5, new TableEntryPtr());
    }
}
