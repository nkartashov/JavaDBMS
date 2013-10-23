package queryParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class QueryReader {

    public QueryReader() { query = new ArrayList<String>(); }
    public void read() {

        try{

            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            String s;

            do {

                s = bufferRead.readLine();
                query.add(s);
            }
            while (!s.endsWith(";"));
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getQuery() {

        String result = new String();
        for (String str : query)
            result += str + ' ';
        return result;
    }

    private ArrayList<String> query;
}
