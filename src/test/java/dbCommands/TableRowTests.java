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
		int rowSignatureSize = 18;
		ArrayList <String> args = new ArrayList<String>();
		int arg1 = 4;
		String arg2 = "gjghjf";
		String arg3 = "k";
		args.add(String.valueOf(arg1));
		args.add(arg2);
		args.add(arg3);
		int cumulativeLength = 0;
		cumulativeLength += ByteConverter.intToByte(arg1).length;
		cumulativeLength += ByteConverter.charsToByte(arg2.toCharArray(), rowSignature.get(1).size() - arg2.length() * ByteConverter.CHAR_LENGTH_IN_BYTES).length;
		cumulativeLength += ByteConverter.charsToByte(arg3.toCharArray(), rowSignature.get(2).size() - arg2.length() * ByteConverter.CHAR_LENGTH_IN_BYTES).length;
		Assert.assertEquals(rowSignatureSize, cumulativeLength);

		TableRow tableRow = new TableRow(args);
		byte[] actualResult = tableRow.getAsByteArray(rowSignature);

		Assert.assertEquals(rowSignatureSize, actualResult.length);

		byte[] expectedResult = ArrayUtils.addAll(ByteConverter.intToByte(arg1),
			ByteConverter.charsToByte(arg2.toCharArray(), rowSignature.get(1).size() - arg2.length() * ByteConverter.CHAR_LENGTH_IN_BYTES));

		expectedResult = ArrayUtils.addAll(expectedResult, ByteConverter.charsToByte(arg3.toCharArray(), rowSignature.get(2).size() - arg2.length() * ByteConverter.CHAR_LENGTH_IN_BYTES));

		Assert.assertArrayEquals(expectedResult, actualResult);
	}

}
