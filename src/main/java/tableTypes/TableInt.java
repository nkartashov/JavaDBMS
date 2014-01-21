/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;

import dbCommands.TableRow;
import utils.ByteConverter;

public class TableInt extends BaseTableType {

    public TableInt()
    {
        this.setSize(4);
    }

    @Override public String toString()
    {
        return "<type>int</type>";
    }

	@Override
	public byte[] getAsByte(TableRow row, int columnPos)
	{
		return ByteConverter.intToByte(row.getAsInt(columnPos));
	}

	@Override
	public Object getAsObject(byte[] data, int offset, int size)
	{
		return ByteConverter.intFromByte(data, offset);
	}

	@Override
	public Object getAsObject(String s)
	{
		return Integer.parseInt(s);
	}

	@Override
	public boolean less(Object left, Object right)
	{
		return ((Integer) left) < ((Integer) right);
	}

	@Override
	public boolean greater(Object left, Object right)
	{
		return ((Integer) left) > ((Integer) right);
	}
}
