package queryParser;

import java.util.ArrayList;
import DbCommands.CreateTableCommand;
import DbCommands.DbCommand;
import TableTypes.ColumnTuple;

public class SQLParser {

    public SQLParser (String queryString) { _query = queryString.trim().replaceAll(" +", " ").replace(";", ""); }

    public DbCommand parse () {

        String firstWord = _query.split(" ", 2)[0].toUpperCase();
        if (firstWord.equals("CREATE")) {

            String secondWord = _query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("TABLE")) {

                if (Checkers.CheckCreateTable(_query)) {

                    DbCommand ctCommand = parseCreateTable(_query);
                    return ctCommand;
                }
                else {

                    System.out.println("Error: incorrect syntax");
                    return null;
                }
            }
        }
        return null;
    }

    private DbCommand parseCreateTable (String ctquery) {

        String nameAndRes[] =  ctquery.split(" ", 4);
        String name = nameAndRes[2];
        ArrayList<Object> tableParams = new ArrayList<Object>();
        tableParams.add(name);

        String columns[] = nameAndRes[3].replace("(", " ").replace(")", " ").trim().split(",");
        ArrayList<ColumnTuple> parsedColumns = new ArrayList<ColumnTuple>();

        for (String column : columns) {

            String forNameAndType[] = column.trim().split(" ");

            if (forNameAndType.length == 3)
                parsedColumns.add(new ColumnTuple(forNameAndType[0], Integer.parseInt(forNameAndType[2]), forNameAndType[1]));
            else
                parsedColumns.add(new ColumnTuple(forNameAndType[0], 0, forNameAndType[1]));
        }
        tableParams.add(parsedColumns);
        DbCommand ctc = new CreateTableCommand (tableParams.get(0).toString(), (ArrayList<ColumnTuple>)tableParams.get(1));
        return ctc;
    }

    private String _query;
}

