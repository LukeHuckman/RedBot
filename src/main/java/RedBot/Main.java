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
    static String prefix = "d."; static String botID; Command command; 
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
            command = new Command(event, user, userID,message.split(" ")[0].substring(2), message.split(" "));
        if(message.contains("<@!") && !message.startsWith("d."))
            mentionParse(event, message, userID);
    }
    
    public void mentionParse(MessageReceivedEvent event, String message, String userID){
        String[] words = message.split(" ");
        for(int i=0;i<words.length;i++)
            if(words[i].charAt(0)=='<' && words[i].charAt(1)=='@')
                switch(words[i].charAt(2)){
                    case '!': //A snarky remark if you mention yourself
                        if(words[i].substring(3, words[i].length()-1).equals(userID))
                            event.getChannel().sendMessage("Lmao y u mentioning urself").queue();
                        break;
                }
    }
}
