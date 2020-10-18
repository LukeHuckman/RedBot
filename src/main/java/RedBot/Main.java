package RedBot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
    static String prefix = "d."; static String botID; Bot bot = new Bot(); 
    public static void main(String[] args) throws LoginException{
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        try{
            Scanner botInfo = new Scanner(new FileInputStream("botInfo.txt"));
            builder.setToken(botInfo.nextLine());
            botID = botInfo.nextLine();
            builder.addEventListeners(new Main());
            builder.build();
        }catch(FileNotFoundException e){
            System.out.println("\"botInfo.txt\" not found!\n\n"
                    + "Create a \"botInfo.txt\" file in the bin folder\n"
                    + "containing two lines about your bot:\n"
                    + "\n"
                    + "<Bot token>\n"
                    + "<Bot ID (snowflake)>\n"
                    + "\n"
                    + "Restart the application after doing so.");
            System.exit(0);
        }
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot())
            return;
        String user = event.getAuthor().getAsTag();
        String userID = event.getAuthor().getId();
        String message = event.getMessage().getContentRaw();
        //System.out.println(user+"("+userID+")"+": \""+message+"\"");
        if(message.startsWith("d."))
            bot.exec(event, user, userID,message.split(" ")[0].substring(2), message.split(" "));
        if(message.contains("<@!") && !message.startsWith("d.") && !message.startsWith(">"))
            bot.mentionParse(event, message, userID);
    }
}
