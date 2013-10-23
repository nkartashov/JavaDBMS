package dbEnvironment;
import tableTypes.Table;
import utils.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public ArrayList<Table> getTables()
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
        StringBuilder serializer = new StringBuilder();
        String filePath;
        for (Table table : _tables)
        {
            serializer.setLength(0);
            table.Serialize(serializer);
            filePath = _location.concat("tables/").concat(table.getUid().toString());
            DumpTableToFile(filePath, serializer);
        }
        _tables.clear();
        _closed = true;
    }

    public void open()
    {
        File tablePath = new File(_location.concat("tables/"));
        String tableHeader;
        Table tableToAdd;
        File file;
        File[] filesInDirectory = tablePath.listFiles();
        for (int i = 0; i < filesInDirectory.length; i++)
        {
            file = filesInDirectory[i];
            tableHeader = Table.readTableHeader(file);
            tableToAdd = new Table();
            tableToAdd.Deserialize(UUID.fromString(file.getName()), tableHeader);
            _tables.add(tableToAdd);
        }
    }

    @Override
    public void finalize() throws Throwable
    {
        super.finalize();
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
    private ArrayList<Table> _tables = new ArrayList<Table>();
}
