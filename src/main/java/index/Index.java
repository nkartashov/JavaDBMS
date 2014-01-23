package index;

import utils.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/01/2014
 * Time: 22:43
 * To change this template use File | Settings | File Templates.
 */
public class Index
{
	public Index() {}

	public Index(UUID uid, String name, int field)
	{
		_uid = uid;
		_name = name;
		_field = field;
		_dataFileName = _uid + "_data";
	}

	public StringBuilder serialize(StringBuilder result)
	{
		result.append("<index>");
		serializeMetadata(result);
		result.append("</index>");
		return result;
	}

	public void deserialize(UUID uid, String text)
	{
		_uid = uid;

		Pattern metadataPattern = Pattern.compile("<meta>(.*?)</meta>");
		Matcher metadataMatcher = metadataPattern.matcher(text);
		metadataMatcher.find();
		deserializeMetadata(metadataMatcher.group(1));

		_dataFileName = _uid + "_data";
	}

	public static String readHeader(File file)
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
				if (buf.contains("</index>"))
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

	public String relativeIndexPath() {return "indeces/" + _uid.toString();}

	public String relativeDataPath() {return "indeces/" + _dataFileName;}

	private StringBuilder serializeMetadata(StringBuilder result)
	{
		result.append("<meta>");
		result.append("<name>");
		result.append(_name);
		result.append("</name>");
		result.append("<field>");
		result.append(_field);
		result.append("</field>");
		result.append("</meta>");
		result.append("\n");
		return result;
	}

	private void deserializeMetadata(String text)
	{
		Pattern namePattern = Pattern.compile("<name>(.*?)</name>");
		Matcher nameMatcher = namePattern.matcher(text);
		nameMatcher.find();
		_name = nameMatcher.group(1);

		Pattern fieldPattern = Pattern.compile("<field>(.*?)</field>");
		Matcher fieldMatcher = fieldPattern.matcher(text);
		fieldMatcher.find();
		_field = Integer.parseInt(nameMatcher.group(1));
	}

	public String name() {return _name;}

	private int _field;
	private String _name;
	private UUID _uid;
	private String _dataFileName;
}
