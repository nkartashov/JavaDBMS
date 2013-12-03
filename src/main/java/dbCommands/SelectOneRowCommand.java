package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 15:18
 * To change this template use File | Settings | File Templates.
 */
public class SelectOneRowCommand implements DbCommand, DbResultCommand
{
	public SelectOneRowCommand(String tableName)
	{
		_tableName = tableName;
	}

	public void executeCommand(DbContext context)
	{
		_hasBeenExecuted = true;
		Table tableToInsertInto = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.getLocation() + tableToInsertInto.getRelativeDataPath(),
			tableToInsertInto.rowSignature());

		_result = tableHeapFile.selectRow();

	}

	public ArrayList<Object> getResult()
	{
		if (!_hasBeenExecuted)
			return null;
		return _result;
	}

	private boolean _hasBeenExecuted = false;
	private String _tableName;
	private ArrayList<Object> _result;
}