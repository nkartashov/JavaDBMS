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
        for (SingleCondition cond : _conditions) {
            Object val1 = getValue(cond._val1, row);
            Object val2 = getValue(cond._val2, row);

//            if(cond._operator.equals(">")) {
//                if (<=) return false;
//            }
//            if(cond._operator.equals("<")) {
//                if (>=) return false;
//            }
//            if(cond._operator.equals(">=")) {
//                if (<) return false;
//            }
//            if(cond._operator.equals("<=")) {
//                if (>) return false;
//            }
//            if(cond._operator.equals("==")) {
//                if (!=) return false;
//            }
//            if(cond._operator.equals("<>")) {
//                if (==) return false;
//            }
        }
        return true;
    }

    private Object getValue(String raw_val, List<Object> row) {
        if (raw_val.indexOf('{') != -1) {
            return row.get(Integer.valueOf(raw_val.replaceAll("[{}]", "")));
        }
        else {
            if (raw_val.matches("-?\\d+(\\.\\d+)?")) {
                return Integer.valueOf(raw_val);
            }
            else {
                return raw_val;
            }
        }
    }

    public List<SingleCondition> conditions() { return _conditions; }

    private List<SingleCondition> _conditions;
}
