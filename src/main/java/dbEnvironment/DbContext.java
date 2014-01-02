package dbEnvironment;
import memoryManager.PageManager;
import tableTypes.Table;
import utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 14:01
 * To change this template use File | Settings | File Templates.
 */
public class DbContext {
	public DbContext(String location)
	{
		_location = location;
		open();
		_closed = false;
	}

	public LinkedHashMap<String, Table> tables()
	{
		if (!_closed)
			return _tables;
		else
			return null;
	}

	public String getLocation()
	{
		return _location;
	}

	public void close()
	{
		//Dumps table definitions to disk
		StringBuilder serializer = new StringBuilder();
		String filePath;
		for (Table table : _tables.values())
		{
			serializer.setLength(0);
			table.Serialize(serializer);
			filePath = _location + table.getRelativeTablePath();
			DumpTableToFile(filePath, serializer);
		}
		_tables.clear();


		//Dumps cached pages to disk and clears cache
		PageManager.getInstance().close();

		// Everything is dumped by that point
		_closed = true;
	}

	public void open()
	{
		File tablePath = new File(_location + "tables/");
		String tableHeader;
		Table tableToAdd;
		File file;
		UUID tableUid;

		File[] filesInDirectory = tablePath.listFiles();
		if (filesInDirectory != null)
			for (int i = 0; i < filesInDirectory.length; i++)
			{
				file = filesInDirectory[i];

				tableHeader = Table.readTableHeader(file);
				try
				{
					tableUid = UUID.fromString(file.getName());
				}
				catch (IllegalArgumentException e)
				{
					continue;
				}
				tableToAdd = new Table();
				tableToAdd.Deserialize(tableUid, tableHeader);
				_tables.put(tableToAdd.getName(), tableToAdd);
			}

	}

	public Table getTableByName (String tableName)
	{
		return _tables.get(tableName);
	}

	@Override
	public void finalize() throws Throwable
	{
		super.finalize();
		if (!_closed)
			close();
		_closed = true;
	}

	private void DumpTableToFile(String filePath, StringBuilder serializer)
	{
		try
		{
			BufferedWriter w = new BufferedWriter(new FileWriter(filePath));
			w.write(serializer.toString());
			w.flush();
		}
		catch (IOException e)
		{
			Logger.LogErrorMessage(e);
		}

	}

	private boolean _closed = true;
	private String _location;
	private LinkedHashMap<String, Table> _tables = new LinkedHashMap<String, Table>();
}
