package RedBot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main extends ListenerAdapter {
    static String prefix = "d."; static String token; public static String mcAddress; Bot bot = new Bot(); 
    public static void main(String[] args) throws LoginException{
        JDA jda;
        try{
            Scanner botInfo = new Scanner(new FileInputStream("botInfo.txt"));
            token = botInfo.nextLine();
            mcAddress = botInfo.nextLine();
            jda = JDABuilder.create(token, intents()).setStatus(OnlineStatus.ONLINE).build();
            jda.addEventListener(new Main());
        }catch(FileNotFoundException e) {
            System.out.println("\"botInfo.txt\" not found!\n\n"
                    + "Create a \"botInfo.txt\" file in the bin folder\n"
                    + "containing two lines about your bot:\n"
                    + "\n"
                    + "<Bot token>\n"
                    + "<Minecraft server IP>\n"
                    + "\n"
                    + "Restart the application after doing so.");
            System.exit(0);
        }
    }
    
    private static ArrayList<GatewayIntent> intents() { //Add more if necessary
        ArrayList<GatewayIntent> intents = new ArrayList<>();
        intents.add(GatewayIntent.GUILD_MEMBERS);
        intents.add(GatewayIntent.GUILD_MESSAGES);
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);
        intents.add(GatewayIntent.GUILD_EMOJIS);
        intents.add(GatewayIntent.GUILD_PRESENCES);
        return intents;
    }
    
    @Override
    public void onReady(ReadyEvent event) {
        bot.mcStatusPresence(event);
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot())
            return;
        String user = event.getAuthor().getAsTag();
        String userID = event.getAuthor().getId();
        String message = event.getMessage().getContentRaw();
        if(message.startsWith(prefix))
            bot.exec(event, user, userID,message.split(" ")[0].substring(prefix.length()), message.split(" "));
        if(message.contains("<@!") && !message.startsWith(prefix) && !message.startsWith(">"))
            bot.mentionParse(event, message, userID);
    }
}
