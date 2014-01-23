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
public class SelectAllRowsCommand implements DbResultCommand
{
	public SelectAllRowsCommand(String tableName)
	{
		_tableName = tableName;
	}

	public void executeCommand(DbContext context)
	{
		Table tableToSelectFrom = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.location() + tableToSelectFrom.relativeDataPath(),
			tableToSelectFrom.rowSignature());

		_result = tableHeapFile.selectAllRows();
	}

	public List<Object> getResult()
	{
		return _result;
	}

    public String tableName() { return _tableName; }

	private String _tableName;
	private List<Object> _result;

}
