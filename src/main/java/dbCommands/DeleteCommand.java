package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/01/2014
 * Time: 22:10
 * To change this template use File | Settings | File Templates.
 */
public class DeleteCommand implements DbCommand
{
	public DeleteCommand(String tableName, RowPredicate predicate)
	{
		_tableName = tableName;
		_predicate = predicate;
	}

	public void executeCommand(DbContext context)
	{
		Table tableToSelectFrom = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.location() + tableToSelectFrom.relativeDataPath(),
			tableToSelectFrom.rowSignature());

		int numberOfDeletions = tableHeapFile.deleteRows(_predicate);
		tableToSelectFrom.deleteRows(numberOfDeletions);
	}

	private String _tableName;
	private RowPredicate _predicate = null;
}
