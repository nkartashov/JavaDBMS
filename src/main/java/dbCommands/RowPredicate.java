package dbCommands;

import queryParser.SingleCondition;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 16/01/2014
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class RowPredicate
{
    public RowPredicate(List<SingleCondition> conditions) {
        _conditions = conditions;
    }

	public boolean evaluate(List<Object> row) {
//        for (SingleCondition cond : _conditions) {
//
//        }
        return true;
    }

    public List<SingleCondition> conditions() { return _conditions; }

    private List<SingleCondition> _conditions;
}
