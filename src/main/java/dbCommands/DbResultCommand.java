package dbCommands;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 27/11/2013
 * Time: 15:19
 * To change this template use File | Settings | File Templates.
 */
public interface DbResultCommand extends DbCommand
{
	public List<Object> getResult();
}
