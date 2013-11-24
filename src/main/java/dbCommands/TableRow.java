package dbCommands;

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

	public char[] getAsCharArray(int pos)
	{
		return _arguments.get(pos).toCharArray();
	}

	public ArrayList<byte[]> getAsByteArray(ArrayList<BaseTableType> rowSignature)
	{
		ArrayList<byte[]> result = new ArrayList<byte[]>();

		for (int i = 0; i < _arguments.size(); ++i)
		{
			result.add(rowSignature.get(i).getAsByte(this, i));
		}

		return result;
	}

    private ArrayList<String> _arguments;
}
