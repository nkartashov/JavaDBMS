package dbEnvironment;

import index.Index;
import memoryManager.PageManager;
import tableTypes.Table;
import utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
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

	public HashMap<String, Table> tables()
	{
		if (!_closed)
			return _tables;
		else
			return null;
	}

	public HashMap<String, Index> indeces()
	{
		if (!_closed)
			return _indeces;
		else
			return null;
	}

	public String location()
	{
		return _location;
	}

	public void close()
	{
		serialiseTables();
		serialiseIndeces();

		//Dumps cached pages to disk and clears cache
		PageManager.getInstance().close();

		// Everything is dumped by that point
		_closed = true;
	}

	private void serialiseTables()
	{
		StringBuilder serializer = new StringBuilder();
		String filePath;
		for (Table table : _tables.values())
		{
			serializer.setLength(0);
			table.Serialize(serializer);
			filePath = _location + table.relativeTablePath();
			dumpSerialisedDataToFile(filePath, serializer);
		}
		_tables.clear();
	}

	private void serialiseIndeces()
	{
		StringBuilder serializer = new StringBuilder();
		String filePath;
		for (Index index : _indeces.values())
		{
			serializer.setLength(0);
			index.serialize(serializer);
			filePath = _location + index.relativeIndexPath();
			dumpSerialisedDataToFile(filePath, serializer);
		}
		_indeces.clear();
	}


	private void open()
	{
		deserialiseTables();
		deserialiseIndeces();
	}

	private void deserialiseTables()
	{
		File tablePath = new File(_location + "tables/");
		String tableHeader;
		Table tableToAdd;
		UUID tableUid;

		File[] filesInDirectory = tablePath.listFiles();
		if (filesInDirectory != null)
			for (File fileInDirectory : filesInDirectory)
			{
				tableHeader = Table.readTableHeader(fileInDirectory);
				try
				{
					tableUid = UUID.fromString(fileInDirectory.getName());
				} catch (IllegalArgumentException e)
				{
					continue;
				}
				tableToAdd = new Table();
				tableToAdd.Deserialize(tableUid, tableHeader);
				_tables.put(tableToAdd.name(), tableToAdd);
			}
	}

	public void deserialiseIndeces()
	{
		File indexPath = new File(_location + "indeces/");

		String indexHeader;
		Index indexToAdd;
		UUID indexUid;

		File[] filesInDirectory = indexPath.listFiles();
		if (filesInDirectory != null)
			for (File fileInDirectory : filesInDirectory)
			{
				indexHeader = Index.readHeader(fileInDirectory);
				try
				{
					indexUid = UUID.fromString(fileInDirectory.getName());
				} catch (IllegalArgumentException e)
				{
					continue;
				}
				indexToAdd = new Index();
				indexToAdd.deserialize(indexUid, indexHeader);
				_indeces.put(indexToAdd.name(), indexToAdd);
			}
	}


	public Table getTableByName (String tableName)
	{
		return _tables.get(tableName);
	}

	public Index getIndexByName (String indexName)
	{
		return _indeces.get(indexName);
	}

	@Override
	public void finalize() throws Throwable
	{
		super.finalize();
		if (!_closed)
			close();
		_closed = true;
	}

	private void dumpSerialisedDataToFile(String filePath, StringBuilder serializer)
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
	private HashMap<String, Table> _tables = new LinkedHashMap<String, Table>();
	private HashMap<String, Index> _indeces = new LinkedHashMap<String, Index>();
}
