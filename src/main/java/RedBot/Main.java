package RedBot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter {
    static String prefix = "d."; static String botID;
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
            command(event, user, userID,message.split(" ")[0].substring(2), message.split(" "));
        if(message.contains("<@!") && !message.startsWith("d."))
            mentionParse(event, message, userID);
    }
    
    public void command(MessageReceivedEvent event, String user, String userID, String command, String[] message){
        Random r = new Random();
        switch(command){
            case "quote": //Quotes a user's post from the past 100 posts
                List<Message> postHistory = event.getChannel().getHistoryBefore(event.getMessage(), 100).complete().getRetrievedHistory();
                if(message[1].startsWith("<@!")){ //Quote of a paricular user
                    for(int i=0;i<postHistory.size();i++){
                        int random = r.nextInt(postHistory.size()-1);
                        Message post = postHistory.get(random);
                        if(post.getAuthor().getId().equals(message[1].substring(3, message[1].length()-1))){
                            if(post.getAuthor().isBot()){
                                event.getChannel().sendMessage("I can't quote bots").queue();
                                break;
                            }
                            event.getChannel().sendMessage("\""+post.getContentRaw()+"\" - <@!"+post.getAuthor().getId()+">").queue();
                            break;
                        }
                        event.getChannel().sendMessage("No recent posts from this user.").queue();
                    }
                }
                else if(message[1].equals("random") || message[1].equals("Random")){ //Completely random
                    for(int i=0;i<postHistory.size();i++){
                        int random = r.nextInt(postHistory.size()-1);
                        Message post = postHistory.get(random);
                        if(post.getAuthor().isBot())
                            continue;
                        event.getChannel().sendMessage("\""+post.getContentRaw()+"\" - <@!"+post.getAuthor().getId()+">").queue();
                        break;
                    }
                }
                break;
        }
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
