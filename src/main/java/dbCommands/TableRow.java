package dbCommands;

import org.apache.commons.lang3.ArrayUtils;
import tableTypes.BaseTableType;
import tableTypes.TableInt;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class TableRow
{
    public TableRow(ArrayList<String> arguments)
    {
        _arguments = arguments;
    }

	public int getAsInt(int pos)
	{
		return Integer.parseInt(_arguments.get(pos));
	}

	public String getAsString(int pos)
	{
		return _arguments.get(pos);
	}

	public byte[] getAsByteArray(ArrayList<BaseTableType> rowSignature)
	{
		byte[] result = rowSignature.get(0).getAsByte(this, 0);

		for (int i = 1; i < _arguments.size(); ++i)
		{
			result = ArrayUtils.addAll(result, rowSignature.get(i).getAsByte(this, i));
		}

		return result;
	}

    private ArrayList<String> _arguments;
}
