package memoryManager;

import dbCommands.*;
import dbEnvironment.DbContext;
import org.junit.Assert;
import org.junit.Test;
import tableTypes.BaseTableType;
import tableTypes.ColumnTuple;
import tableTypes.Table;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
	public void Insert50000RowsTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");
		ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTablegfsgdfks";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 300;

		for (int i = 0; i < numberOfRows; ++i)
		{
			rows.add(tableRow);
		}
		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);
		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
		selectAllRowsCommand.executeCommand(context);
		List<Object> result = selectAllRowsCommand.getResult();

		Assert.assertNotEquals(null, result);

		Assert.assertEquals(numberOfRows, result.size());

		context.close();
	}

	@Test
	public void HugeInsertTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");
		ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTablegfsgdfks";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 50000;

		for (int i = 0; i < numberOfRows; ++i)
		{
			rows.add(tableRow);
		}
		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);
		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
		selectAllRowsCommand.executeCommand(context);
		List<Object> result = selectAllRowsCommand.getResult();

		Assert.assertNotEquals(null, result);

		Assert.assertEquals(numberOfRows, result.size());

		context.close();
	}

	@Test
	public void SimpleInsertAndDecodeTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");

		ArrayList <ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTabsdfgasdflegfsgdfks";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 300;

		for (int i = 0; i < numberOfRows; ++i)
		{
			rows.add(tableRow);
		}

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);
		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
		selectAllRowsCommand.executeCommand(context);
		List<Object> result = selectAllRowsCommand.getResult();

		Assert.assertNotEquals(null, result);

		Assert.assertEquals(numberOfRows, result.size());

		Table table = context.getTableByName(tableName);
		ArrayList<BaseTableType> rowSignature = table.rowSignature();

		ArrayList<Object> expected = new ArrayList<Object>();

		for (int i = 0; i < args.size(); ++i)
			expected.add(rowSignature.get(i).getAsObject(args.get(i)));

		for (int i = 0; i < numberOfRows; ++i)
		{
			ArrayList<Object> actual = (ArrayList<Object>) result.get(i);
			for (int j = 0; j < actual.size(); ++j)
				Assert.assertEquals(expected.get(j), actual.get(j));
		}

		context.close();
	}

	@Test
	public void ComplexInsertAndDecodeTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");

		ArrayList <ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTabsdfgfasdflhsgdfkahsdlkfgasljdgfasdflegfsgdfks";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 300;

		for (int i = 0; i < numberOfRows; ++i)
		{
			rows.add(tableRow);
		}

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);
		SelectAllRowsCommand selectAllRowsCommand = new SelectAllRowsCommand(tableName);
		selectAllRowsCommand.executeCommand(context);
		List<Object> result = selectAllRowsCommand.getResult();

		Assert.assertNotEquals(null, result);

		Assert.assertEquals(numberOfRows, result.size());

		Table table = context.getTableByName(tableName);
		ArrayList<BaseTableType> rowSignature = table.rowSignature();

		ArrayList<Object> expected = new ArrayList<Object>();

		for (int i = 0; i < args.size(); ++i)
			expected.add(rowSignature.get(i).getAsObject(args.get(i)));

		for (int i = 0; i < numberOfRows; ++i)
		{
			ArrayList<Object> actual = (ArrayList<Object>) result.get(i);
			for (int j = 0; j < actual.size(); ++j)
				Assert.assertEquals(expected.get(j), actual.get(j));
		}

		context.close();
	}

	@Test
	public void BoundSelectTest()
	{
		DbContext context = new DbContext(RESOURCE_PATH);

		ColumnTuple columnTuple1 = new ColumnTuple("lol", 4, "int");
		ColumnTuple columnTuple2 = new ColumnTuple("foz", 16, "char");
		ColumnTuple columnTuple3 = new ColumnTuple("baz", 2, "char");

		ArrayList <ColumnTuple> tuples = new ArrayList<ColumnTuple>();
		tuples.add(columnTuple1);
		tuples.add(columnTuple2);
		tuples.add(columnTuple3);
		String tableName = "testTabsdasbflaksdbfklfgasdflegfsgdfks";
		CreateTableCommand createTableCommand = new CreateTableCommand(tableName, tuples);

		createTableCommand.executeCommand(context);

		ArrayList<String> args = new ArrayList<String>();
		args.add("4");
		args.add("gjghjf");
		args.add("k");
		TableRow tableRow = new TableRow(args);
		ArrayList<TableRow> rows = new ArrayList<TableRow>();

		int numberOfRows = 1000;

		for (int i = 0; i < numberOfRows; ++i)
		{
			rows.add(tableRow);
		}

		int expectedRowCount = 200;

		InsertRowsCommand insertRowsCommand = new InsertRowsCommand(tableName, rows);
		insertRowsCommand.executeCommand(context);
		SelectCommand selectRowsCommand = new SelectCommand(tableName, null, expectedRowCount);
		selectRowsCommand.executeCommand(context);
		List<Object> result = selectRowsCommand.getResult();

		Assert.assertNotEquals(null, result);

		Assert.assertEquals(expectedRowCount, result.size());

		context.close();
	}

	private static final String RESOURCE_PATH = "/Users/nikita_kartashov/Documents/Work/java/JavaDBMS/src/test/resources/memoryManager/";
}
