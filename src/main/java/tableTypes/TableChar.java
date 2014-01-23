/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;

import dbCommands.TableRow;
import utils.ByteConverter;

public class TableChar extends BaseTableType {

    public TableChar(int size)
    {
        // In order to hold the size
	    setSize(size + 1);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("<type>char ");
        result.append(size() - 1);
        result.append("</type>");
        return result.toString();
    }

	@Override
	public byte[] getAsByte(TableRow row, int columnPos)
	{
		String stringToConvert = row.getAsString(columnPos);
		return ByteConverter.stringToBytes(stringToConvert, size());
	}

	@Override
	public Object getAsObject(byte[] data, int offset, int size)
	{
		return ByteConverter.stringFromBytes(data, offset);
	}

	@Override
	public Object getAsObject(String s)
	{
		return s;
	}

	@Override
	public boolean less(Object left, Object right)
	{
		return false;
	}

	@Override
	public boolean greater(Object left, Object right)
	{
		return false;
	}
}
