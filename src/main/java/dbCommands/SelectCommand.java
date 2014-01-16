package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/01/2014
 * Time: 20:56
 * To change this template use File | Settings | File Templates.
 */
public class SelectCommand implements DbCommand, DbResultCommand
{
	public SelectCommand(String tableName, RowPredicate predicate, int count)
	{
		_tableName = tableName;
		_predicate = predicate;
		_count = count;
	}

	public void executeCommand(DbContext context)
	{
		_hasBeenExecuted = true;
		Table tableToSelectFrom = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.getLocation() + tableToSelectFrom.getRelativeDataPath(),
			tableToSelectFrom.rowSignature());

		_result = tableHeapFile.selectWhere(_predicate, _count);
	}

	public List<Object> getResult()
	{
		if (!_hasBeenExecuted)
			return null;
		return _result;
	}

	private boolean _hasBeenExecuted = false;
	private String _tableName;
	private RowPredicate _predicate = null;
	private int _count = Integer.MAX_VALUE;
	private List<Object> _result;
}
