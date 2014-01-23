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
        query = query.trim().replaceAll(" +", " ").replaceAll(" +,", ",").replace(";", "").replaceAll("\n", "");
        String firstWord = query.split(" ", 2)[0].toUpperCase();
        if (firstWord.equals("CREATE")) {
            String secondWord = query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("TABLE")) {
                if (Checkers.CheckCreateTable(query)) {
	                return parseCreateTable(query);
                }
                else {
                    System.out.println("Error: incorrect syntax");
                    return null;
                }
            }
            if(secondWord.equals("INDEX")) {
                return parseCreateIndex(query.split("INDEX", 2)[1].trim());
            }
        }
        if (firstWord.equals("INSERT")) {
            String secondWord = query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("INTO")) {
                if (Checkers.CheckInsertInto(query)) {
	                return parseInsertInto(query);
                }
            }
        }
        if (firstWord.equals("SELECT")) {
            if (Checkers.CheckSelect(query)) {
	            return parseSelect(query);
            }
        }
        return null;
    }

    private DbCommand parseCreateTable(String query) {
        query =  query.split(" ", 3)[2];
        String name = query.split("\\(", 2)[0];
        String[] columns = query.split("\\(", 2)[1].split(",");
        List<ColumnTuple> parsedColumns = new ArrayList<ColumnTuple>();

        for (String column : columns) {
            String columnName = column.trim().split(" ", 2)[0].trim();
            String restPart = column.trim().split(" ", 2)[1].trim();
            int size = 0;
            String type;
            if(restPart.contains("(")) {
                type = restPart.split("\\(", 2)[0].trim();
                size = Integer.valueOf(restPart.split("\\(", 2)[1].replace(")", "").trim());
            }
            else {
                type = restPart;
            }
            parsedColumns.add(new ColumnTuple(columnName, size, type));
        }
        return new CreateTableCommand(name, parsedColumns);
    }

    private DbCommand parseCreateIndex(String query) {
        String indexName = query.split(" ", 2)[0].trim();
        String restPart = query.split(" ", 2)[1].replace("ON | on", "").trim();
        String tableName = restPart.split("\\(", 2)[0].trim();
        String columnName = restPart.split("\\(", 2)[1].replace(")", "").trim();
        return new CreateIndexCommand(tableName, nameToColumnNo(tableName, columnName));
    }

    private DbCommand parseInsertInto(String query) {
        String nameAndRes[] =  query.split(" ", 4);
        String name = nameAndRes[2];
        String rows[] = nameAndRes[3].replace("VALUES", "").trim().split("\\) ?,");
        List<TableRow> insertionList = new ArrayList<TableRow>();
        for (String row : rows) {
            String[] values = row.trim().replace("(", "").replace(")", "").split(", ?");
            insertionList.add(new TableRow(new ArrayList<String>(Arrays.asList(values))));
        }
        return new InsertRowsCommand(name, insertionList);
    }

    private DbResultCommand parseSelect(String query) {
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
	    for (SingleCondition condition : conditions)
	    {
		    namesToColumnNumbers(table_name, condition);
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
        if (query.contains("<")) {
            operator = "<";
        }
        else if (query.contains(">")) {
            operator = ">";
        }
        else if (query.contains("=")) {
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

    private int nameToColumnNo(String tableName, String columnName) {
        List<Column> columns = _context.getTableByName(tableName).rowSignature();
        for (int i = 0; i < columns.size(); ++i) {
            if (columns.get(i).name().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private DbContext _context;
    private boolean _error_occured = false;

    final private int WHERE_WORD_LENGTH = 5;
    final private int TABLE_NAME_POSITION = 3;
}

