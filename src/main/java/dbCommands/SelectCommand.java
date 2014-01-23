package dbCommands;

import dbEnvironment.DbContext;
import index.Index;
import index.IndexFile;
import index.TableEntryPtr;
import memoryManager.HeapFile;
import tableTypes.Table;
import utils.IntPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/01/2014
 * Time: 20:56
 * To change this template use File | Settings | File Templates.
 */
public class SelectCommand implements DbResultCommand
{
	public SelectCommand(String tableName, RowPredicate predicate, int count)
	{
		_tableName = tableName;
		_predicate = predicate;
		_count = count;
	}

	public void executeCommand(DbContext context)
	{
		Table tableToSelectFrom = context.getTableByName(_tableName);
		HeapFile tableHeapFile = new HeapFile(context.location() + tableToSelectFrom.relativeDataPath(),
			tableToSelectFrom.rowSignature());

		boolean noIndexSelect = true;
		IntPair indexPair = null;
		Index index = null;

		for (IntPair pair: _predicate.equalityParams())
		{
			index = context.getIndexByName(_tableName + Integer.toString(pair.value2));
			if (index != null)
			{
				noIndexSelect = false;
				indexPair = pair;
				break;
			}
		}

		if (noIndexSelect)
		{
			_result = tableHeapFile.selectWhere(_predicate, _count);
		}
		else
		{
			IndexFile indexFile = new IndexFile(context.location() + index.relativeDataPath(), false);
			List<TableEntryPtr> entry_ptrs = indexFile.select(_predicate.conditions().get(indexPair.value1));
			_result = new ArrayList<Object>();
			for(TableEntryPtr ptr : entry_ptrs)
			{
				List<Object> row = tableHeapFile.selectRowFromPage(ptr.pagePointer(), ptr.rowPointer());
				if (_predicate.evaluate(row))
					_result.add(row);
			}
		}
	}

	public List<Object> getResult()
	{
		return _result;
	}

	public RowPredicate predicate() { return _predicate; }

	private String _tableName;
	private RowPredicate _predicate = null;
	private int _count = Integer.MAX_VALUE;
	private List<Object> _result = null;
}
