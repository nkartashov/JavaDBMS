package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.HeapFile;
import tableTypes.Table;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/01/2014
 * Time: 21:31
 * To change this template use File | Settings | File Templates.
 */
public class UpdateCommand implements DbCommand
{
	public UpdateCommand(String tableName, RowPredicate predicate,TableRow row)
	{
		_tableName = tableName;
		_predicate = predicate;
		_row = row;
	}

	public void executeCommand(DbContext context)
	{
		Table tableToSelectFrom = context.getTableByName(_tableName);

		HeapFile tableHeapFile = new HeapFile(context.location() + tableToSelectFrom.relativeDataPath(),
			tableToSelectFrom.rowSignature());

		tableHeapFile.updateRows(_predicate, _row);
	}

	private String _tableName;
	private RowPredicate _predicate = null;
	private TableRow _row;
}
