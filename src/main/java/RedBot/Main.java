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
    public static void main(String[] args) throws LoginException, FileNotFoundException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        Scanner botInfo = new Scanner(new FileInputStream("botInfo"));
        builder.setToken(botInfo.nextLine());
        botID = botInfo.nextLine();
        builder.addEventListeners(new Main());
        builder.build();
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
            bot.command(event, user, userID,message.split(" ")[0].substring(2), message.split(" "));
        if(message.contains("<@!") && !message.startsWith("d."))
            bot.mentionParse(event, message, userID);
    }
}
