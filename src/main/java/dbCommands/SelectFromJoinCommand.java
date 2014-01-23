package dbCommands;

import dbEnvironment.DbContext;
import memoryManager.TableIterator;
import tableTypes.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/01/2014
 * Time: 13:57
 * To change this template use File | Settings | File Templates.
 */
public class SelectFromJoinCommand implements DbResultCommand
{
	public SelectFromJoinCommand(String leftTableName, String rightTableName, RowPredicate predicate, int count)
	{
		_leftTableName = leftTableName;
		_rightTableName = rightTableName;
		_predicate = predicate;
		_count = count;
	}

	public void executeCommand(DbContext context)
	{
		_result = new ArrayList<Object>();

		Table leftTable = context.getTableByName(_leftTableName);
		Table rightTable = context.getTableByName(_rightTableName);

		if (leftTable.numberOfRows() > rightTable.numberOfRows())
		{
			Table buf = leftTable;
			leftTable = rightTable;
			rightTable = buf;
		}

		TableIterator leftIterator = new TableIterator(context, leftTable.getName());
		TableIterator rightIterator = new TableIterator(context, rightTable.getName());

		while (!leftIterator.isFinished())
		{
			List<Object> leftRow = leftIterator.nextRow();
			while (!rightIterator.isFinished())
			{
				List<Object> row = new ArrayList<Object>(leftRow);
				row.addAll(rightIterator.nextRow());
				if (_predicate != RowPredicate.TRUE_PREDICATE)
				{
					if (_predicate.evaluate(row))
						_result.add(row);
				}
				else
					_result.add(row);
			}
			rightIterator.resetIterator();
		}
	}

	public List<Object> getResult()
	{
		return _result;
	}

	private String _leftTableName;
	private String _rightTableName;
	private RowPredicate _predicate = null;
	private int _count = Integer.MAX_VALUE;
	private List<Object> _result = null;
}
