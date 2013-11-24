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
        this.setSize(size);
    }

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder("<type>char ");
        result.append(this.size());
        result.append("</type>");
        return result.toString();
    }

	@Override
	public void write(byte[] buffer, int insideRowOffset, TableRow row, int columnPos)
	{
		ByteConverter.charsToBuffer(row.getAsCharArray(columnPos), buffer, insideRowOffset);
	}

	@Override
	public byte[] getAsByte(TableRow row, int columnPos)
	{
		return ByteConverter.charsToByte(row.getAsCharArray(columnPos));
	}

}
