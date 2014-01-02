/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;

import dbCommands.TableRow;
import org.apache.commons.lang3.ArrayUtils;
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
	public byte[] getAsByte(TableRow row, int columnPos)
	{
		String stringToConvert = row.getAsString(columnPos);
		return ByteConverter.stringToBytes(stringToConvert, size() - stringToConvert.length() * ByteConverter.CHAR_LENGTH_IN_BYTES);
	}

	@Override
	public Object getAsObject(byte[] data, int offset, int size)
	{
		return ByteConverter.stringFromBytes(data, offset, size);
	}
}
