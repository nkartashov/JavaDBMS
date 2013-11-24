package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.ColumnTuple;
import tableTypes.Table;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class CreateTableCommand implements DbCommand {
    public CreateTableCommand(String tableName, ArrayList<ColumnTuple> tuples)
    {
        _tableName = tableName;
        _tuples = tuples;
    }

    public void executeCommand(DbContext context)
    {
        Table tableToAdd = new Table(UUID.randomUUID(), _tableName, _tuples);
        context.tables().put(_tableName, tableToAdd);

	    HeapFile.seedDataFile(context.getLocation()  + tableToAdd.getRelativeDataPath(),
		    tableToAdd.rowSignature());
    }

    private String _tableName;
    private ArrayList<ColumnTuple> _tuples;
}