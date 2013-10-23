/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:43
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;
public class TableChar extends BaseTableType {

    public TableChar(int size)
    {
        this.SetSize(size);
    }

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder("<type>char ");
        result.append(this.GetSize());
        result.append("</type>");
        return result.toString();
    }
}
