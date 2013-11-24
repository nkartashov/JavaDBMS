package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:36
 * To change this template use File | Settings | File Templates.
 */
public class InsertRowsCommand implements DbCommand
{
    public InsertRowsCommand(String tableName, ArrayList<TableRow> rows)
    {
        _tableName = tableName;
        _rows = rows;
    }

    public void executeCommand(DbContext context)
    {
	    Table tableToInsertInto = context.getTableByName(_tableName);

	    HeapFile tableHeapFile = new HeapFile(context.getLocation() + tableToInsertInto.getRelativeDataPath(),
		    tableToInsertInto.rowSignature());

	    tableHeapFile.insertRows(_rows);
    }

    private String _tableName;
    private ArrayList<TableRow> _rows;
}
