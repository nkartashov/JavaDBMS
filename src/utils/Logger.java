package utils;

/**
 * Created with IntelliJ IDEA.
 * User: nikita_kartashov
 * Date: 23/10/2013
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public class Logger
{
    public static void LogErrorMessage(Exception e)
    {
        // Add logging to file here
        System.out.println(e.toString());
        e.printStackTrace();
    }
}
