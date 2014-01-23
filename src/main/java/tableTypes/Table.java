package tableTypes;

import utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/10/2013
 * Time: 16:49
 * To change this template use File | Settings | File Templates.
 */


public class Table {

    public Table() {}

    public Table(UUID uid, String name, List<ColumnTuple> tuples)
    {
        _name = name;
	    Column columnToAdd;
        for (ColumnTuple tuple : tuples)
        {
	        columnToAdd = new Column(tuple);
            _columns.add(columnToAdd);
        }
        _uid = uid;

        _dataFileName = _uid + "_data";
    }

    public StringBuilder Serialize(StringBuilder result)
    {
        result.append("<table>");
        SerializeMetadata(result);
        SerializeColumns(result);
        result.append("</table>");
        return result;
    }

    public void Deserialize(UUID uid, String text)
    {
        _uid = uid;

        Pattern metadataPattern = Pattern.compile("<meta>(.*?)</meta>");
        Matcher metadataMatcher = metadataPattern.matcher(text);
        metadataMatcher.find();
        DeserializeMetadata(metadataMatcher.group(1));

        Pattern columnsPattern = Pattern.compile("<columns>(.*?)</columns>");
        Matcher columnsMatcher = columnsPattern.matcher(text);
        columnsMatcher.find();
        DeserializeColumns(columnsMatcher.group(1));

        _dataFileName = _uid + "_data";
    }

    public static String readTableHeader(File file)
    {
        StringBuilder builder = new StringBuilder();
        String buf;
        try
        {
            BufferedReader r = new BufferedReader(new FileReader(file));
            while (r.ready())
            {
                buf = r.readLine();
                builder.append(buf);
                if (buf.contains("</table>"))
                    break;
            }
            return builder.toString();
        }
        catch (IOException e)
        {
            Logger.LogErrorMessage(e);
            return null;
        }
    }

    public String name() {return _name;}

	public String relativeTablePath() {return "tables/" + _uid.toString();}

	public String relativeDataPath() {return "tables/" + _dataFileName;}

    public void insertRows(int rows) {_numberOfRows += rows;}

    public void deleteRows(int rows) {_numberOfRows -= rows;}

	public int numberOfRows() {return _numberOfRows;}

	public List<Column> rowSignature() {return _columns;}

    private StringBuilder SerializeMetadata(StringBuilder result)
    {
        result.append("<meta>");
        result.append("<name>");
        result.append(_name);
        result.append("</name>");
        result.append("<numRows>");
        result.append(_numberOfRows);
        result.append("</numRows>");
        result.append("</meta>");
        result.append("\n");
        return result;
    }

    private StringBuilder SerializeColumns(StringBuilder result)
    {
        result.append("<columns>");
        for (Column column: _columns)
        {
            result.append(column.toString());
            result.append("\n");
        }
        result.append("</columns>");
        result.append("\n");
        return result;
    }

    private void DeserializeColumns(String text)
    {
        Pattern columnPattern = Pattern.compile("<column>(.*?)</column>");
        Matcher columnMatcher = columnPattern.matcher(text);
        while (columnMatcher.find())
        {
            Column deserializedColumn = new Column();
            deserializedColumn.DeserializeColumn(columnMatcher.group(1));
            _columns.add(deserializedColumn);
        }
    }

    private void DeserializeMetadata(String text)
    {
        Pattern namePattern = Pattern.compile("<name>(.*?)</name>");
        Matcher nameMatcher = namePattern.matcher(text);
        nameMatcher.find();
        _name = nameMatcher.group(1);

        Pattern numberPattern = Pattern.compile("<numRows>(.*?)</numRows>");
        Matcher numberMatcher = numberPattern.matcher(text);
        numberMatcher.find();
        _numberOfRows = Integer.parseInt(numberMatcher.group(1));
    }

    private List<Column> _columns = new ArrayList<Column>();
    private String _name;
    private UUID _uid;
    private int _numberOfRows = 0;
    private String _dataFileName;
}
