package DbEnvironment;

import dbCommands.*;
import dbEnvironment.DbContext;
import org.junit.Assert;
import org.junit.Test;
import tableTypes.ColumnTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/01/2014
 * Time: 19:42
 * To change this template use File | Settings | File Templates.
 */
public class DbContextTests
{
	@Test
	public void SerializeDeserializeTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		String tableName = "serializetest";

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");
		List<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		List<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		List<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 300;

		for (int i = 0; i < numberOfRows; ++i)
			rows.add(tableRow);

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);

		context.close();

		context = new DbContext(RESOURCE_PATH);

		Assert.assertNotEquals(null, context.getTableByName(tableName));

		SelectCommand selectCommand = new SelectCommand(tableName, RowPredicate.TRUE_PREDICATE, numberOfRows);
		selectCommand.executeCommand(context);

		Assert.assertEquals(numberOfRows, selectCommand.getResult().size());
	}

	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/dbEnvironment/";
}

