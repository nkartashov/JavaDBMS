package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.DiskPage;
import memoryManager.PageId;
import memoryManager.PageManager;
import tableTypes.Table;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:36
 * To change this template use File | Settings | File Templates.
 */
public class InsertRowsCommand implements DbCommand
{
    public InsertRowsCommand(String tableName, TableRow[] rows)
    {
        _tableName = tableName;
        _rows = rows;
    }

    public void executeCommand(DbContext context)
    {
        for (TableRow row : _rows)
        {
            InsertRow(context, row);
        }
    }

    private void InsertRow(DbContext context, TableRow row)
    {
        Table tableToInsertInto = context.getTableByName(_tableName);
        PageManager pageManager = context.getPageManager();

        // get second page of data
        PageId notFullPage = new PageId(tableToInsertInto.getDataFileName(), NOT_FULL_PAGE_INDEX);

        byte[] rawNotFullPage = pageManager.GetPage(notFullPage);

    }

    // the index of the first page, which contains indeces for first n not full pages
    private static long NOT_FULL_PAGE_INDEX = 2;

    private String _tableName;
    private TableRow[] _rows;
}
