package dbCommands;

import org.apache.commons.lang3.ArrayUtils;
import tableTypes.Column;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 17:45
 * To change this template use File | Settings | File Templates.
 */
public class TableRow
{
    public TableRow(List<String> arguments)
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

	public byte[] getAsByteArray(List<Column> rowSignature)
	{
		byte[] result = rowSignature.get(0).type().getAsByte(this, 0);

		for (int i = 1; i < rowSignature.size(); ++i)
		{
			result = ArrayUtils.addAll(result, rowSignature.get(i).type().getAsByte(this, i));
		}

		return result;
	}

    private List<String> _arguments;
}
