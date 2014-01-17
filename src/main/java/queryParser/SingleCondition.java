package queryParser;

/**
 * Created with IntelliJ IDEA.
 * User: maratx
 * Date: 1/17/14
 * Time: 11:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class SingleCondition {

    public SingleCondition(String val1, String operator, String val2) {
        _val1 = val1;
        _operator = operator;
        _val2 = val2;
    }

    public String _val1;
    public String _val2;
    public String _operator;
}
