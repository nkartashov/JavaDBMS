package dbCommands;

import dbEnvironment.DbContext;
import index.IndexFile;
import index.TableEntryPtr;
import memoryManager.HeapFile;
import tableTypes.Table;

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
        HeapFile tableHeapFile = new HeapFile(context.getLocation() + tableToSelectFrom.getRelativeDataPath(),
                tableToSelectFrom.rowSignature());

        if(true) {
            _result = tableHeapFile.selectWhere(_predicate, _count);
        }
        else {
            IndexFile index = new IndexFile(null, false);
            List<TableEntryPtr> entry_ptrs = index.select(_predicate);
            _result = new ArrayList<Object>();
            for(TableEntryPtr ptr : entry_ptrs) {
                _result.add(tableHeapFile.selectRowFromPage(ptr.pagePointer(), ptr.rowPointer()));
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
