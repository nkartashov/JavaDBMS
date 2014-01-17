package dbCommands;


import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

import java.util.List;


/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 04/12/2013
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class SelectAllRowsCommand implements DbCommand, DbResultCommand
{
	public SelectAllRowsCommand(String tableName)
	{
		_tableName = tableName;
	}

	public void executeCommand(DbContext context)
	{
		_hasBeenExecuted = true;
		Table tableToSelectFrom = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.getLocation() + tableToSelectFrom.getRelativeDataPath(),
			tableToSelectFrom.rowSignature());

		_result = tableHeapFile.selectAllRows();
	}

	public List<Object> getResult()
	{
		if (!_hasBeenExecuted)
			return null;
		return _result;
	}

	private boolean _hasBeenExecuted = false;
	private String _tableName;
	private List<Object> _result;

}
