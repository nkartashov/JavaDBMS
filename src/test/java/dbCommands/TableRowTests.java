package dbCommands;

import org.junit.Assert;
import org.junit.Test;
import tableTypes.Column;
import tableTypes.ColumnTuple;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/01/2014
 * Time: 21:19
 * To change this template use File | Settings | File Templates.
 */
public class TableRowTests
{

	@Test
	public void CodeDecodeTest()
	{

		List<String > args = new ArrayList<String>();
		int arg1 = 4;
		String arg2 = "gjghjf";
		String arg3 = "k";

		args.add(Integer.toString(arg1));
		args.add(arg2);
		args.add(arg3);
		TableRow tableRow = new TableRow(args);

		List<Column> columns = new ArrayList<Column>();
		columns.add(new Column(new ColumnTuple("lol", 4, "int")));
		columns.add(new Column(new ColumnTuple("foz", 16, "char")));
		columns.add(new Column(new ColumnTuple("baz", 1, "char")));

		byte[] rowData = tableRow.getAsByteArray(columns);
		int byteOffset = 0;
		List<Object> result = new ArrayList<Object>();
		for (Column column : columns)
		{
			result.add(column.type().getAsObject(rowData, byteOffset, column.size()));
			byteOffset += column.size();
		}

		Assert.assertEquals(arg1, result.get(0));
		Assert.assertEquals(arg2, result.get(1));
		Assert.assertEquals(arg3, result.get(2));
	}
}
