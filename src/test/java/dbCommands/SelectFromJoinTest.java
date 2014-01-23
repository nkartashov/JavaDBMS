package dbCommands;

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
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class SelectFromJoinTest
{
	public DbContext initContext(String leftTableName, String rightTableName)
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 1, "char");
		List<ColumnTuple> tuples1 = new ArrayList<ColumnTuple>();
		tuples1.add(columnTuple1);
		tuples1.add(columnTuple2);
		tuples1.add(columnTuple3);
		CreateTableCommand createTableCommand1 = new CreateTableCommand(leftTableName, tuples1);

		ColumnTuple columnTuple4 = new ColumnTuple("losdfasdfsl", 30, "char");
		ColumnTuple columnTuple5 = new ColumnTuple("48", 16, "int");
		List<ColumnTuple> tuples2 = new ArrayList<ColumnTuple>();
		tuples2.add(columnTuple4);
		tuples2.add(columnTuple5);
		CreateTableCommand createTableCommand2 = new CreateTableCommand(rightTableName, tuples2);

		createTableCommand1.executeCommand(context);
		createTableCommand2.executeCommand(context);

		return context;
	}

	@Test
	public void SimpleCartesianProductTest()
	{
		String leftTableName = "lolz";
		String rightTableName = "Rororov";

		DbContext context = initContext(leftTableName, rightTableName);

		int leftNumberOfRows = 300;
		int rightNumberOfRows = 716;

		List<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		List<TableRow> rows = new ArrayList<TableRow>();

		for (int i = 0; i < leftNumberOfRows; ++i)
			rows.add(tableRow);

		List<String> args1 = new ArrayList<String>();
		args1.add("sdfsdf");
		args1.add("14");
		TableRow tableRow1 = new TableRow(args1);
		List<TableRow> rows1 = new ArrayList<TableRow>();

		for (int i = 0; i < rightNumberOfRows; ++i)
			rows1.add(tableRow1);

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(leftTableName, rows);
		InsertRowsCommand insertRowsCommand1 = new InsertRowsCommand(rightTableName, rows1);

		insertRowsCommand.executeCommand(context);
		insertRowsCommand1.executeCommand(context);

		SelectFromJoinCommand selectFromJoinCommand = new SelectFromJoinCommand(leftTableName,
			rightTableName, RowPredicate.TRUE_PREDICATE, leftNumberOfRows * rightNumberOfRows);

		selectFromJoinCommand.executeCommand(context);

		Assert.assertEquals(leftNumberOfRows * rightNumberOfRows, selectFromJoinCommand.getResult().size());
	}

	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/dbCommands/";
}
