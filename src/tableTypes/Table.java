package tableTypes;
import utils.Logger;

import java.io.*;
import java.util.ArrayList;
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

    public Table(UUID uid, String name, ArrayList<ColumnTuple> tuples)
    {
        _name = name;
        for (ColumnTuple tuple : tuples)
        {
            _columns.add(new Column(tuple));
        }
        _uid = uid;
    }

    public StringBuilder Serialize(StringBuilder result)
    {
        result.append("<table>");
        this.SerializeMetadata(result);
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
        this.DeserializeMetadata(metadataMatcher.group(1));

        Pattern columnsPattern = Pattern.compile("<columns>(.*?)</columns>");
        Matcher columnsMatcher = columnsPattern.matcher(text);
        columnsMatcher.find();
        this.DeserializeColumns(columnsMatcher.group(1));
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

    public UUID getUid()
    {
        return _uid;
    }

    private StringBuilder SerializeMetadata(StringBuilder result)
    {
        result.append("<meta>");
        result.append("<name>");
        result.append(_name);
        result.append("</name>");
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
    }


    private ArrayList<Column> _columns = new ArrayList<Column>();
    private String _name;
    private UUID _uid;
}
