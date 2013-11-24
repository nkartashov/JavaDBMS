/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;
public class ColumnTuple {
    public ColumnTuple(String name, int size, String type)
    {
        _name = name;
        _size = size;
        _type = type;
    }

    public String Name()
    {
        return _name;
    }

    public int Size()
    {
        return _size;
    }

    public String Type()
    {
        return _type;
    }

    private String _name;
    private int _size;
    private String _type;

}
