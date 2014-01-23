package dbCommands;

import dbEnvironment.DbContext;
import org.junit.Assert;
import org.junit.Test;
import queryParser.SQLParser;
import tableTypes.ColumnTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 24/01/2014
 * Time: 00:50
 * To change this template use File | Settings | File Templates.
 */

public class IndexTests
{
	@Test
	public void CreateAndQueryTest()
	{
		String tableName = "indexTest";
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "int");
		List<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);
		createTableCommand.executeCommand(context);

		List<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 2000;

		for (int i = 0; i < numberOfRows; ++i)
		{
			List<String> args = new ArrayList<String>();
			args.add(Integer.toString(i));
			args.add(Integer.toString(i % 4));
			TableRow tableRow = new TableRow(args);
			rows.add(tableRow);
		}

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);

		CreateIndexCommand createIndexCommand = new CreateIndexCommand(tableName, 1);
		createIndexCommand.executeCommand(context);

		long start = System.nanoTime();

		SQLParser sqlParser = new SQLParser(context);

		SelectCommand selectCommand = (SelectCommand) sqlParser.parse("SELECT * FROM " + tableName + " WHERE foz = 2");
		selectCommand.executeCommand(context);

		Assert.assertEquals(numberOfRows / 4, selectCommand.getResult().size());

		long elapsed = System.nanoTime() - start;
		System.out.println(elapsed);
	}

	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/dbCommands/";
}
