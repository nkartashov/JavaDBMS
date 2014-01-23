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

    public BinaryPredicate(List<Column> row_signature1, List<Column> row_signature2, List<SingleCondition> conditions) {
        _row_signature1 = row_signature1;
        _row_signature2 = row_signature2;
        _conditions = conditions;
    }

    public boolean evaluate(List<Object> row1, List<Object> row2) {
//        for (SingleCondition condition : _conditions) {
//            int field_no1 =
//        }
        return true;
    }

    private int getFieldNo(String rawFieldNo) {
        return 0;
    }

    private List<Column> _row_signature1;
    private List<Column> _row_signature2;
    private List<SingleCondition> _conditions;
}
