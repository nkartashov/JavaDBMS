package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.ColumnTuple;
import tableTypes.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 15:01
 * To change this template use File | Settings | File Templates.
 */
public class RemoveTableCommand implements DbCommand {
	public RemoveTableCommand(String tableName)
	{
		_tableName = tableName;
	}

	public void executeCommand(DbContext context)
	{
		Table tableToDelete = context.tables().get(_tableName);

		File tableFile = new File(context.getLocation() + tableToDelete.getRelativeTablePath());
		tableFile.delete();
		File tableDataFile = new File(context.getLocation() + tableToDelete.getRelativeDataPath());
		tableDataFile.delete();

		context.tables().remove(_tableName);
	}

	private String _tableName;
}