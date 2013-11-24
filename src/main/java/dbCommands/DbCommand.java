package dbCommands;

import dbEnvironment.DbContext;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public interface DbCommand {
    void executeCommand(DbContext context);
}
