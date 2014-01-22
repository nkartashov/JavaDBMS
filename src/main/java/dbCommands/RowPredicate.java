package dbCommands;

import queryParser.SingleCondition;
import tableTypes.Column;

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
    public RowPredicate(List<Column> row_signature, List<SingleCondition> conditions) {
        _row_signature = row_signature;
        _conditions = conditions;
    }

	public boolean evaluate(List<Object> row) {
        for (SingleCondition cond : _conditions) {
            int field_index = getFieldIndex(cond);
            if(cond._operator.equals(">")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().greater(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (Integer.valueOf(cond._val1) <= Integer.valueOf(cond._val2)) {
                        return false;
                    }
                }
            }
            if(cond._operator.equals("<")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().less(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (Integer.valueOf(cond._val1) >= Integer.valueOf(cond._val2)) {
                        return false;
                    }
                }
            }
            if(cond._operator.equals(">=")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().greaterOrEqual(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (Integer.valueOf(cond._val1) < Integer.valueOf(cond._val2)) {
                        return false;
                    }
                }
            }
            if(cond._operator.equals("<=")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().lessOrEqual(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (Integer.valueOf(cond._val1) > Integer.valueOf(cond._val2)) {
                        return false;
                    }
                }
            }
            if(cond._operator.equals("==")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().equals(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (!cond._val1.equals(cond._val2)) {
                        return false;
                    }
                }
            }
            if(cond._operator.equals("<>")) {
                if(field_index != -1) {
                    if (!_row_signature.get(field_index).type().notEquals(toObject(cond._val1, row), toObject(cond._val2, row))) {
                        return false;
                    }
                }
                else {
                    if (cond._val1.equals(cond._val2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private int getFieldIndex(SingleCondition condition) {
        if (condition._val1.indexOf('{') != -1) {
            return Integer.valueOf(condition._val1.replaceAll("[{}]", ""));
        }
        if (condition._val2.indexOf('{') != -1) {
            return Integer.valueOf(condition._val2.replaceAll("[{}]", ""));
        }
        return -1;
    }

    private Object toObject(String raw_val, List<Object> row) {
        if (raw_val.indexOf('{') != -1) {
            return row.get(Integer.valueOf(raw_val.replaceAll("[{}]", "")));
        }
        else {
            if (isNumber(raw_val)) {
                return Integer.valueOf(raw_val);
            }
            else {
                return raw_val;
            }
        }
    }

    private boolean isNumber(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    public List<SingleCondition> conditions() { return _conditions; }

	public static final RowPredicate TRUE_PREDICATE = null;

    private List<Column> _row_signature;
    private List<SingleCondition> _conditions;
}
