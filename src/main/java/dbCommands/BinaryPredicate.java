package dbCommands;

import queryParser.SingleCondition;
import tableTypes.Column;
import utils.IntPair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/24/14
 * Time: 1:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class BinaryPredicate {

    public BinaryPredicate(List<Column> row_signature1, List<SingleCondition> conditions) {
        _row_signature1 = row_signature1;
        _conditions = conditions;
    }

    public boolean evaluate(List<Object> row1, List<Object> row2) {
        for (SingleCondition condition : _conditions) {
            int field_no1 = getFieldNo(condition._val1);
            int field_no2 = getFieldNo(condition._val2);

            if(condition._operator.equals(">")) {
                if (!_row_signature1.get(field_no1).type().greater(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
            else if(condition._operator.equals("<")) {
                if (!_row_signature1.get(field_no1).type().less(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
            else if(condition._operator.equals(">=")) {
                if (!_row_signature1.get(field_no1).type().greaterOrEqual(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
            else if(condition._operator.equals("<=")) {
                if (!_row_signature1.get(field_no1).type().lessOrEqual(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
            else if(condition._operator.equals("=")) {
                if (!_row_signature1.get(field_no1).type().equals(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
            else if(condition._operator.equals("<>")) {
                if (!_row_signature1.get(field_no1).type().notEquals(row1.get(field_no1), row2.get(field_no2))) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getFieldNo(String rawFieldNo) {
        return Integer.valueOf(rawFieldNo.replaceAll("[{}]", ""));
    }

    private List<Column> _row_signature1;;
    private List<SingleCondition> _conditions;
}
