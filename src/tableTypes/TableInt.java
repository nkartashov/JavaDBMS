/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:41
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;
public class TableInt extends BaseTableType {

    public TableInt()
    {
        this.SetSize(4);
    }

    @Override public String toString()
    {
        return "<type>int</type>";
    }

}
