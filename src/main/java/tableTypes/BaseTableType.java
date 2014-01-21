/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
package tableTypes;

import dbCommands.TableRow;

public abstract class BaseTableType {

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

	public abstract byte[] getAsByte(TableRow row, int columnPos);

	public abstract Object getAsObject(byte[] data, int offset, int size);

	public abstract Object getAsObject(String s);

	public boolean equals(Object left, Object right) {return left.equals(right);}
	public boolean notEquals(Object left, Object right) {return !equals(left, right);}
	public abstract boolean less(Object left, Object right);
	public abstract boolean greater(Object left, Object right);
	public boolean lessOrEqual(Object left, Object right) {return !greater(left, right);}
	public boolean greaterOrEqual(Object left, Object right) {return !less(left, right);}

    private int _size;
}

