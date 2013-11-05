/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;
public class BaseTableType {

    public int size()
    {
        return _size;
    }
    public void setSize(int size)
    {
        _size = size;
    }

    @Override public String toString()
    {
        return String.valueOf(_size);
    }

    private int _size;
}

