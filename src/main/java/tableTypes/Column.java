package tableTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */

public class Column {

    public Column()
    {
    }

    public Column(ColumnTuple tuple)
    {
        _name = tuple.Name();
        if (tuple.Type().contentEquals("char"))
            _type = new TableChar(tuple.Size());
        else
            _type = new TableInt();
    }

	public int size()
	{
		return _type.size();
	}

    @Override public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("<column>");
        result.append("<name>");
        result.append(_name);
        result.append("</name>");
        result.append(_type.toString());
	    result.append("</column>");
	    return result.toString();
    }

    public void DeserializeColumn(String text)
    {
        Pattern namePattern = Pattern.compile("<name>(.*?)</name>");
        Matcher nameMatcher = namePattern.matcher(text);
        nameMatcher.find();
        _name = nameMatcher.group(1);

        Pattern typePattern = Pattern.compile("<type>(.*?)</type>");
        Matcher typeMatcher = typePattern.matcher(text);
        typeMatcher.find();
        String typeText = typeMatcher.group(1);
        String[] typeAndSize= typeText.split(" ");
        if (typeAndSize[0].equals("char"))
            _type = new TableChar(Integer.parseInt(typeAndSize[1]));
        else
            _type = new TableInt();
    }

	public BaseTableType type() {return _type;}
    public String name() { return _name; }

    private String _name;
    private BaseTableType _type;
}
