package queryParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dbCommands.*;
import dbEnvironment.DbContext;
import org.apache.commons.lang3.ArrayUtils;
import tableTypes.ColumnTuple;

public class SQLParser {

    public SQLParser (String queryString, DbContext context) {
        _query = queryString.trim().replaceAll(" +", " ").replaceAll(" +,", ",").replace(";", "");
        _context = context;
    }

    public DbCommand parse () {
        String firstWord = _query.split(" ", 2)[0].toUpperCase();
        if (firstWord.equals("CREATE")) {
            String secondWord = _query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("TABLE")) {
                if (Checkers.CheckCreateTable(_query)) {
                    DbCommand ctCommand = parseCreateTable();
                    return ctCommand;
                }
                else {
                    System.out.println("Error: incorrect syntax");
                    return null;
                }
            }
        }
        if (firstWord.equals("INSERT")) {
            String secondWord = _query.split(" ", 3)[1].toUpperCase();
            if (secondWord.equals("INTO")) {
                if (Checkers.CheckInsertInto(_query)) {
                    DbCommand iiCommand = parseInsertInto();
                    return iiCommand;
                }
            }
        }
        if (firstWord.equals("SELECT")) {
            if (Checkers.CheckSelect(_query)) {
                DbCommand selectCommand = parseSelect();
                return selectCommand;
            }
        }
        return null;
    }

    private DbCommand parseCreateTable() {
        String nameAndRes[] =  _query.split(" ", 4);
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

    private DbCommand parseInsertInto() {
        String nameAndRes[] =  _query.split(" ", 4);
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

    private DbCommand parseSelect() {
        if (_query.toUpperCase().indexOf("JOIN") != -1) {
            //SelectWithJoinCommand
            return null;
        }
        else if (_query.toUpperCase().indexOf("WHERE") != -1) {
             return null;
        }
        else {
            String first_col_name = _query.split(" ", 2)[1];
            if (first_col_name.equals("*")) {
                String table_name = _query.split(" ", 4)[3].trim();
                //DbCommand select_all = new SelectAllCommand(table_name);
                //return select_all;
                return null;
            }
            return null;
        }
    }

    private RowPredicate parseWhere(String table_name, String queue) {
        List<SingleCondition> conditions = new ArrayList<SingleCondition>();
        parseCondition(queue, conditions);
        if(_error_occured) {
            return null;
        }
        int[] column_numbers = null;
        for(int i = 0; i < conditions.size(); ++i) {
            column_numbers = ArrayUtils.addAll(column_numbers, findColumnNumbers(table_name, conditions.get(i)));
        }
    }

    private void parseCondition(String query, List<SingleCondition> conditions) {
        String[] cond_array = query.split("and", 2);
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
        if (query.indexOf(">") != -1) {
            operator = ">";
        }
        else if (query.indexOf("<") != -1) {
            operator = "<";
        }
        else if (query.indexOf("=") != -1) {
            operator = "=";
        }
        else return null;
        int index = query.indexOf(operator);
        operator.concat(query.substring(index, index + 1));
        return operator;
    }

    private int[] findColumnNumbers(String table_name, SingleCondition condition) {
        _context.getTableByName(table_name);
        return null;
    }

    private String _query;
    private DbContext _context;
    private boolean _error_occured = false;
}

