package dbCommands;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import tableTypes.BaseTableType;
import tableTypes.TableChar;
import tableTypes.TableInt;
import utils.ByteConverter;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
public class TableRowTests
{
	@Test
	public void ConvertToByteTest()
	{
		ArrayList<BaseTableType> rowSignature = new ArrayList<BaseTableType>();
		rowSignature.add(new TableInt());
		rowSignature.add(new TableChar(12));
		rowSignature.add(new TableChar(2));
		ArrayList <String> args = new ArrayList<String>();
		int arg1 = 4;
		String arg2 = "gjghjf";
		String arg3 = "k";
		args.add(String.valueOf(arg1));
		args.add(arg2);
		args.add(arg3);


	}
}
