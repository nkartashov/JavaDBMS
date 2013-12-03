package memoryManager;

import com.sun.rowset.internal.InsertRow;
import dbCommands.*;
import dbEnvironment.DbContext;
import org.junit.Assert;
import org.junit.Test;
import tableTypes.ColumnTuple;
import tableTypes.Table;

import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class HeapFileTests
{
	@Test
	public void SeedFileTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 1, "char");
		ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTable";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		Table newTable = context.tables().get(tableName);

		context.close();

		File tableFile = new File(context.getLocation() + newTable.getRelativeTablePath());
		Assert.assertEquals(true, tableFile.exists());

		File tableDataFile = new File(context.getLocation() + newTable.getRelativeDataPath());
		Assert.assertEquals(true, tableDataFile.exists());

		tableFile.delete();
		tableDataFile.delete();
	}

	@Test
	public void VeryBasicInsertSelectTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");
		ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTable";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();
		rows.add(tableRow);
		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);

		insertRowsCommand.executeCommand(context);

		SelectOneRowCommand selectOneRowCommand = new SelectOneRowCommand(tableName);

		selectOneRowCommand.executeCommand(context);

		Assert.assertNotEquals(null, selectOneRowCommand.getResult());
	}

	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";

}
