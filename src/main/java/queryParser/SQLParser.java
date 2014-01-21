package queryParser;

import dbCommands.*;
import dbEnvironment.DbContext;
import tableTypes.Column;
import tableTypes.ColumnTuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SQLParser {

    public SQLParser (DbContext context) {
        _context = context;
    }

    public DbCommand parse (String query) {
        query = query.trim().replaceAll(" +", " ").replaceAll(" +,", ",").replace(";", "");
        String firstWord = query.split(" ", 2)[0].toUpperCase();
        if (firstWord.equals("CREATE")) {
            String secondWord = query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("TABLE")) {
                if (Checkers.CheckCreateTable(query)) {
                    DbCommand ctCommand = parseCreateTable(query);
                    return ctCommand;
                }
                else {
                    System.out.println("Error: incorrect syntax");
                    return null;
                }
            }
        }
        if (firstWord.equals("INSERT")) {
            String secondWord = query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("INTO")) {
                if (Checkers.CheckInsertInto(query)) {
                    DbCommand iiCommand = parseInsertInto(query);
                    return iiCommand;
                }
            }
        }
        if (firstWord.equals("SELECT")) {
            if (Checkers.CheckSelect(query)) {
                DbCommand selectCommand = parseSelect(query);
                return selectCommand;
            }
        }
        return null;
    }

    private DbCommand parseCreateTable(String query) {
        String nameAndRes[] =  query.split(" ", 4);
        String name = nameAndRes[2];
        ArrayList<Object> tableParams = new ArrayList<Object>();
        tableParams.add(name);

        String columns[] = nameAndRes[3].replace("(", " ").replace(")", " ").trim().split(",");
        ArrayList<ColumnTuple> parsedColumns = new ArrayList<ColumnTuple>();

        for (String column : columns) {
            String forNameAndType[] = column.trim().replaceAll(" +", " ").split(" ");
            if (forNameAndType.length == 3)
                parsedColumns.add(new ColumnTuple(forNameAndType[0], Integer.parseInt(forNameAndType[2]), forNameAndType[1]));
            else
                parsedColumns.add(new ColumnTuple(forNameAndType[0], 0, forNameAndType[1]));
        }
        tableParams.add(parsedColumns);
        DbCommand ctc = new CreateTableCommand(tableParams.get(0).toString(), (ArrayList<ColumnTuple>)tableParams.get(1));
        //System.out.println(tableParams);
        return ctc;
    }

    private DbCommand parseInsertInto(String query) {
        String nameAndRes[] =  query.split(" ", 4);
        String name = nameAndRes[2];
        String rows[] = nameAndRes[3].replace("VALUES", "").replace("(", "").trim().split("\\),");
        TableRow[] insertionList = new TableRow[rows.length];
        int i = 0;
        for (String row : rows) {
            String[] values = row.replace(")", "").split(",");
            insertionList[i] = new TableRow(new ArrayList<String>(Arrays.asList(values)));
            ++i;
        }
        //DbCommand iic = new InsertRowsCommand(name, insertionList);
        System.out.println(name);
        //System.out.println(insertionList);
        //return iic;
        return null;
    }

    private DbCommand parseSelect(String query) {
        // !!! assuming that only SELECT * can be used !!!
        String table_name = query.split(" ", 5)[TABLE_NAME_POSITION].trim();
        RowPredicate predicate = null;
        int index_of_where = query.toUpperCase().indexOf("WHERE");
        if (index_of_where != -1) {
            predicate = parseWhere(table_name, query.substring(index_of_where + WHERE_WORD_LENGTH));
        }
        if(predicate != null) {
            return new SelectCommand(table_name, predicate, -1);
        }
        return new SelectAllRowsCommand(table_name);
    }

    private RowPredicate parseWhere(String table_name, String queue) {
        List<SingleCondition> conditions = new ArrayList<SingleCondition>();
        parseCondition(queue, conditions);
        if(_error_occured) {
            return null;
        }
        for(int i = 0; i < conditions.size(); ++i) {
            namesToColumnNumbers(table_name, conditions.get(i));
        }
        List<Column> row_signature = _context.getTableByName(table_name).rowSignature();
        return new RowPredicate(row_signature, conditions);
    }

    private void parseCondition(String query, List<SingleCondition> conditions) {
        String[] cond_array = query.split("AND | and", 2);
        String operator = findCompOperator(cond_array[0]);
        if(operator == null) {
            _error_occured = true;
            return;
        }
        String[] exprs = cond_array[0].split(operator);
        conditions.add(new SingleCondition(exprs[0].trim(), operator, exprs[1].trim()));
        if(cond_array.length == 1) {
            return;
        }
        parseCondition(cond_array[1], conditions);
    }

    private String findCompOperator(String query) {
        String operator;
        if (query.indexOf("<") != -1) {
            operator = "<";
        }
        else if (query.indexOf(">") != -1) {
            operator = ">";
        }
        else if (query.indexOf("=") != -1) {
            operator = "=";
        }
        else return null;
        int index = query.indexOf(operator);
        String second_part = query.substring(index + 1, index + 2);
        if(second_part.equals("<") || second_part.equals(">") || second_part.equals("=")) {
            return operator + second_part;
        }
        return operator;
    }

    private void namesToColumnNumbers(String table_name, SingleCondition condition) {
        List<Column> columns = _context.getTableByName(table_name).rowSignature();
        if(!condition._val1.contains("\"")) {
            for (int i = 0; i < columns.size(); ++i) {
                if (columns.get(i).name().compareTo(condition._val1) == 0) {
                    condition._val1 = "{" + Integer.toString(i) + "}";
                    break;
                }
            }
        }
        if(!condition._val2.contains("\"")) {
            for (int i = 0; i < columns.size(); ++i) {
                if (columns.get(i).name().compareTo(condition._val2) == 0) {
                    condition._val2 = "{" + Integer.toString(i) + "}";
                    break;
                }
            }
        }
    }

    private DbContext _context;
    private boolean _error_occured = false;

    final private int WHERE_WORD_LENGTH = 5;
    final private int TABLE_NAME_POSITION = 3;
}

