package RedBot;

import java.util.ArrayList;
import java.util.List;

public class Helper { //a helper class that contains QoL improvements
    public static String[] commandParser (String[] message) { //Combines arguments in quotes
        List<String> str = new ArrayList<String>(); //List of strings to add to
        Boolean isInQuotes = false; //A flag to decide whether to combine or not
        StringBuffer buffer = new StringBuffer(); //A buffer to place the strings in
        for (String i : message) { //Iterate through the message to find any strings in quotes
            if (message[0]==i) {continue;}
            if (i.startsWith("\"")&&i.endsWith("\"")&&!isInQuotes) str.add(i.substring(1,i.length()-1)); //If string starts and ends with quotes, treat as a normal word
            else if (i.startsWith("\"")&&!isInQuotes) { //Else if string begins with a quote,
                buffer.append(i.substring(1)); //append said string minus the quote itself
                buffer.append(" ");
                isInQuotes = true;
            } else if (i.endsWith("\"")&&isInQuotes) { //If string ends with a quote,
                buffer.append(i.substring(0, i.length()-1)); //append to buffer minus the quote,
                str.add(buffer.toString()); //turn it into a string and flushes it to main string list,
                buffer.setLength(0); //and reset the buffer
                isInQuotes = false;
            } else if (isInQuotes) {
                buffer.append(i);
                buffer.append(" ");
            } else {str.add(i);} //Place the rest of the strings in the list as is
        }
        String[] strarray = new String[str.size()];
        strarray = str.toArray(strarray);
        return strarray;
    }
}
