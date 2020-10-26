package RedBot;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Helper { //a helper class that contains QoL improvements
    public static String[] commandParser (String[] message) { //Combines arguments in quotes
        List<String> str = new ArrayList<String>(); //List of strings to add to
        Boolean isInQuotes = false; //A flag to decide whether to combine or not
        StringBuffer buffer = new StringBuffer(); //A buffer to place the strings in
        for (String i : message) { //Iterate through the message to find any strings in quotes
            if (message[0].equals(i)) continue;
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
    public static Member convertToMember (String input, MessageReceivedEvent event) { //Takes input and tries to find a specific member
        if (input.startsWith("<@")&&input.endsWith(">")) {
            input = input.replaceAll("\\D", "");
        }
        if (input.matches("[0-9]+")) { //If a supposed id is sent,
            Member member = event.getGuild().getMemberById(input); //get member from the guild and return it
            if (member instanceof Member) return member;
        }
        try {
            Member member = event.getGuild().getMemberByTag(input); //by tag
            return member;
        } catch (IllegalArgumentException ex) {
            try {
                Member member = event.getGuild().getMembersByName(input, true).get(0); //by name
                return member;
            } catch (IndexOutOfBoundsException exc) {
                try {
                    Member member = event.getGuild().getMembersByNickname(input, true).get(0); //by nickname
                    return member;
                } catch (IndexOutOfBoundsException exce) {
                    return null; //if still fails
                }
            }
        }
    }
    public static void webhookSender (String content, Webhook webhook) { //A method to send webhooks
        try {
            URL url = new URL("https://discord.com/api/v7/webhooks/"+webhook.getId()+"/"+webhook.getToken()); //The API endpoint
            URLConnection conn = url.openConnection();
            HttpURLConnection http = (HttpURLConnection)conn;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            String json = "{\"content\": \""+content+"\"}"; //The json to send, currently only for content strings
            byte[] out = json.getBytes(StandardCharsets.UTF_8); //Turn the json into bytes
            int length = out.length;
            http.setFixedLengthStreamingMode(length);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8"); //Specify the content type as json
            http.connect(); //Make the connection
            try(OutputStream os = http.getOutputStream()) {
                os.write(out);
            }
        } catch (IOException e) {e.printStackTrace();}
    }
}
