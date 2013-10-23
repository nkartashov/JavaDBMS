import DbCommands.CreateTableCommand;
import DbEnvironment.DbContext;
import TableTypes.ColumnTuple;
import tableTypes.Table;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 14:55
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String[] args)
    {
        ArrayList<ColumnTuple> tuples = new ArrayList<ColumnTuple>();
        tuples.add(new ColumnTuple("lol", 4, "char"));
        tuples.add(new ColumnTuple("foz", 4, "int"));
        tuples.add(new ColumnTuple("baz", 400, "char"));
        CreateTableCommand com = new CreateTableCommand("test", tuples);
        DbContext context = new DbContext("/Users/nikita_kartashov/Documents/Work/java/testdir/");
        com.executeCommand(context);
        ArrayList<Table> tables = context.getTables();
        for (Table t: tables)
        {
            System.out.println(t.Serialize(new StringBuilder()));
        }
        context.close();
    }
}
