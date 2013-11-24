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
	public void write(byte[] buffer, int insideRowOffset, TableRow row, int columnPos)
	{
		ByteConverter.intToBuffer(row.getAsInt(columnPos), buffer, insideRowOffset);
	}

	@Override
	public byte[] getAsByte( TableRow row, int columnPos)
	{
		return ByteConverter.intToByte(row.getAsInt(columnPos));
	}
}
