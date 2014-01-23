package dbCommands;

import dbEnvironment.DbContext;
import index.Index;
import index.IndexFile;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/01/2014
 * Time: 23:32
 * To change this template use File | Settings | File Templates.
 */
public class CreateIndexCommand implements DbCommand
{

	public CreateIndexCommand(String tableName, int field)
	{
		_tableName = tableName;
		_field = field;
	}

	public void executeCommand(DbContext context)
	{
		Index indexToAdd = new Index(UUID.randomUUID(), _tableName, _field);
		context.indeces().put(_tableName + Integer.toString(_field), indexToAdd);

		new IndexFile(context.location() + indexToAdd.relativeDataPath(),
			_tableName, _field, context);
	}

	private String _tableName;
	private int _field;
}
