package RedBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.select.Elements;

public class Bot {

    public Bot() {}

    public void exec(MessageReceivedEvent event, String user, String userID,
            String command, String[] message) {
        Random r = new Random();
        switch(command) {
            case "help":
                if(message.length==1)
                    event.getChannel().sendMessage(help("1")).queue(); //Shows page 1 by default
                else {
                    String helpMsg = help(message[1]);
                    if(helpMsg != null)
                        event.getChannel().sendMessage(helpMsg).queue();
                    else
                        event.getChannel().sendMessage("Unknown help topic. Try `d.help`").queue();
                }
                break;
                
            case "quote": //Quotes a user's post from the past 100 posts
                if(message.length==1)
                    event.getChannel().sendMessage("Usage: `d.quote @user` or `d.quote random`").queue();
                else{
                    List<Message> postHistory 
                            = event.getChannel().getHistoryBefore(event.getMessage(), 100)
                                    .complete().getRetrievedHistory();
                    if(message[1].startsWith("<@!")) { //Quote of a paricular user
                        if(event.getMessage().getMentionedMembers().get(0).getUser().isBot()) {
                            event.getChannel().sendMessage("I can't quote bots").queue();
                            return; //To prevent infinite loops
                        }
                        for(int i=0;i<postHistory.size();i++) {
                            int random = r.nextInt(postHistory.size()-1);
                            Message post = postHistory.get(random);
                            if(post.getAuthor().getId().equals(message[1].substring(3, message[1].length()-1))
                                    && !post.getContentRaw().startsWith("d.")) {
                                event.getChannel().sendMessage("`\""+post.getContentRaw()+"\"` - "
                                        +post.getAuthor().getName()).queue();
                                break;
                            }
                            if(i==postHistory.size()-1)
                                event.getChannel().sendMessage("No recent posts from this user.").queue();
                        }
                    }
                    else if(message[1].equalsIgnoreCase("random")) { //Completely random quote
                        for(int i=0;i<postHistory.size();i++) {
                            int random = r.nextInt(postHistory.size()-1);
                            Message post = postHistory.get(random);
                            if(post.getAuthor().isBot() || post.getContentRaw().startsWith("d."))
                                continue;
                            event.getChannel().sendMessage("`\""+post.getContentRaw()+"\"` - "
                                    +post.getAuthor().getName()).queue();
                            break;
                        }
                    }
                }
                break;
                
            case "8ball": //Answers a yes or no question
                String[] answer = new String[]{
                    "It is certain",
                    "It is decidedly so",
                    "Without a doubt",
                    "Yes, definitely",
                    "You may rely on it",
                    "As I see it, yes",
                    "Most Likely",
                    "Outlook good",
                    "Yes",
                    "Signs point to yes",
                    "Reply hazy, try again",
                    "Ask again later",
                    "Better not tell you now",
                    "Cannot predict now",
                    "Concentrate and ask again",
                    "Don't count on it",
                    "My reply is no",
                    "My sources say no",
                    "Outlook not so good",
                    "Very doubtful"
                };
                event.getChannel().sendMessage(":8ball: "+answer[r.nextInt(answer.length-1)]).queue();
                break;
                
            case "pick": //Chooses an option from a selection
            case "choose":
                boolean makeDecision=false;
                switch (message.length) {
                    case 1:
                        event.getChannel().sendMessage("Usage: `d.pick <Option 1>, <Option 2> ...`").queue();
                        break;
                        
                    case 2:
                        if(!message[1].contains(",")) //When there's only one option
                            event.getChannel().sendMessage("There isn't much of a choice, is there?").queue();
                        else
                            makeDecision=true;
                        break;
                        
                    default:
                        makeDecision=true;
                }
                if(makeDecision) { //Controls whether decision making is actually required
                    String fullstring="";
                        for(int i=1;i<message.length;i++)
                            fullstring+=message[i]+" ";
                        if(!fullstring.contains(",")) {
                            event.getChannel().sendMessage("Wrong syntax. Try using commas").queue();
                            break;
                        }
                        String[] choices = fullstring.split(", ");
                        if(choices.length==1)
                            choices = choices[0].split(",");
                        /* Duplicate check: Doesn't work for some reason
                        
                        boolean same = false;
                 outer: for(int i=0;i<choices.length;i++) {
                            for(int j=i+1;j<choices.length;j++) {
                                if(choices[i].equalsIgnoreCase(choices[j])) {
                                    same = true;
                                    break outer;
                                }
                            }
                        }
                        if(same)
                            event.getChannel().sendMessage("Same options? That's unfair").queue();
                        else
                        */
                        event.getChannel().sendMessage(choices[r.nextInt(choices.length)]).queue();
                }
                break;
                
            case "hentai": // TODO Optimise this train wreck
                if(message.length==1 && event.getTextChannel().isNSFW())
                    event.getChannel().sendMessage("Usage: `d.hentai <term 1> <term 2> ...`").queue();
                else if(!event.getTextChannel().isNSFW()) {
                    event.getChannel().sendMessage("This command can only be used in a NSFW channel.").queue();
                }
                else{
                    String url = "https://nhentai.net/search/?q=";
                    for(int i=1;i<message.length;i++)
                        url +=message[i]+"+";
                    HParser page = new HParser(url); //Get the first page of results
                    if(!page.noResults()){
                        Elements links = page.getData("link");
                        if(links.last().toString().contains("last")) { //When the results are longer than 1 page
                            //Get the total number of results pages and append the last page
                            String lastpage[] = links.last().attr("href").split("=");
                            url+="&page="+(1+r.nextInt(Integer.parseInt(lastpage[lastpage.length-1])-1));
                        }
                        page = new HParser(url); //get a random results page
                        Elements magicNumberURLs = page.getData("numbers");
                        int magicNumber = Integer.parseInt(magicNumberURLs.get(r.nextInt(magicNumberURLs.size()-1))
                                .toString().split("/")[2]); //Magic digits taken from <a> tags
                        page = new HParser("https://nhentai.net/g/"+magicNumber);
                        Elements pageTitle = page.getData("title");
                        List rawTitle = pageTitle.subList(3, 5);
                        String title = "";
                        for(int i=0;i<rawTitle.size();i++) { // Extract and combine the title
                            String text = rawTitle.get(i).toString().split(">")[1];
                            title+=text.substring(0, text.length()-6);
                        }
                        Elements rawTags = page.getData("tags");
                        String tags="Tags:\n";
                        for(int i=0;i<rawTags.size();i++) { //Extract the tags
                            tags += rawTags.get(i).toString().split("<")[1].substring(18);
                            if(i!=rawTags.size()-1)
                                tags += ", ";
                        }
                        Elements thumbnail = page.getData("thumb"); //Get the thumbnail
                        //Building the embed
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setImage(thumbnail.attr("data-src"));
                        embed.setTitle(title,"https://nhentai.net/g/"+magicNumber);
                        embed.setDescription("#"+ magicNumber + "\n\n" + tags);
                        MessageEmbed hentaiEmbed = embed.build();
                        event.getChannel().sendMessage(hentaiEmbed).queue();
                    }
                    else
                        event.getChannel().sendMessage("No results found").queue();
                }
                break;
                
            case "mc":
            case "minecraft":
                try { // Shows info on the Minecraft server
                    String[] shellCommand = {"bash","-c","mcstatus localhost status"}; //pip install mcstatus
                    ProcessBuilder p = new ProcessBuilder(shellCommand);
                    Process pr = p.start();
                    if(!pr.waitFor(2, TimeUnit.SECONDS)) //Failsafe if the command takes too long to execute
                        pr.destroy();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = "", output = "";
                    while((line=buf.readLine())!=null) //Output from the command
                        output+=line+"\n";
                    String[] status = output.split("\\r?\\n");
                    event.getChannel()
                            .sendMessage("Server is online\n"
                                        + "IP: `" + Main.mcAddress + "`\n"
                                        + "Version: " + status[0].split(" ")[1].substring(1)
                                            + " " + status[0].split(" ")[2] + "\n"
                                        + "Players: " + status[2].split(" ")[1]).queue();
                } catch (IOException | ArrayIndexOutOfBoundsException ex) { //When the server goes offline
                    event.getChannel().sendMessage("Server is offline").queue();
                } catch (InterruptedException ex) {}
                break;
                
            case "poll":
                String[] parsed = Helper.commandParser(message); //Combines quotes together
                if (parsed.length > 10 || parsed.length < 3) { //Ensure the arguments are withing the limits
                    event.getChannel().sendMessage("2 to 9 options required").queue();
                    break;
                }
                EmbedBuilder embed = new EmbedBuilder(); //initiates the building of embed
                embed.setTitle(parsed[0]); //sets the title of the embed
                embed.setColor(0x2f3136); //sets the color of the embed to the same as discord dark mode bg
                embed.setAuthor(user);
                for(int i=1;i<parsed.length;i++) { //iterate from the first option
                    StringBuffer sbubby = new StringBuffer(); //initiates a stringbuffer that will be used to create an emote
                    int num = i + 48; //48 is equivalent to emoji 0
                    sbubby.append(Character.toChars(num));
                    sbubby.append(Character.toChars(0xfe0f));
                    sbubby.append(Character.toChars(0x20e3));
                    embed.addField("Choice "+ sbubby, parsed[i], false);
                }
                MessageEmbed pollembed = embed.build(); //embed is done
                event.getChannel().sendMessage(pollembed).queue(sentmessage -> { //sends the poll to the chat
                    for(int i=1;i<parsed.length;i++) {
                        StringBuffer sbubby = new StringBuffer(); 
                        int num = i + 48;
                        sbubby.append(Character.toChars(num)); 
                        sbubby.append(Character.toChars(0xfe0f)); 
                        sbubby.append(Character.toChars(0x20e3)); 
                        sentmessage.addReaction(sbubby.toString()).queue(); //sends the reactions
                    }
                });
                break;
                
            case "clone": //Creates a fake message using webhooks
                String[] cleanMessage = Helper.commandParser(event.getMessage().getContentDisplay().split(" "));
                Member cloneTarget = Helper.convertToMember(cleanMessage[0], event);
                if (cloneTarget == null) { //In case there's an invalid input for member param
                    event.getChannel().sendMessage("Unknown member").queue();
                    break;
                } else if (event.getMessage().mentionsEveryone()) {
                    event.getChannel().sendMessage("Cannot mass ping").queue();
                    break;
                }
                StringBuffer cloneMessageBuffer = new StringBuffer();
                for (String i : cleanMessage) {
                    if (!cleanMessage[0].equals(i)) {
                        cloneMessageBuffer.append(i + " ");
                    }
                }
                String cloneMessage = cloneMessageBuffer.toString(); //Content to be sent
                String cloneName = cloneTarget.getEffectiveName(); //Name of the target/clone
                String cloneAvatarUrl = cloneTarget.getUser().getEffectiveAvatarUrl();
                event.getTextChannel().retrieveWebhooks().queue(hooklist -> { //Try to look for DarkHook in the channel
                    for (Webhook hook : hooklist) {
                        if (hook.getName() == "DarkHook") { //If found, use it
                            Helper.webhookSender(hook, cloneMessage, cloneName, cloneAvatarUrl);
                            return;
                        }
                    }
                    event.getTextChannel().createWebhook("DarkHook").queue(clonehook -> { //else, create a new one
                        Helper.webhookSender(clonehook, cloneMessage, cloneName, cloneAvatarUrl);
                    });
                });
                break;
            
            case "ddg": //Queries top search result from https://duckduckgo.com
            case "search":
                StringBuffer ddgbuffer = new StringBuffer();
                for (String i : message) {
                    if (!i.startsWith(Main.prefix)) {
                        ddgbuffer.append(i+" ");
                    }
                }
                try {
                    String ddgquery = URLEncoder.encode(ddgbuffer.toString(), "UTF-8"); //encodes query into a valid URL format
                    URL ddgurl;
                    if(!event.getTextChannel().isNSFW())
                        ddgurl = new URL("https://duckduckgo.com/html/?q="+ddgquery+"&kp=1");
                    else
                        ddgurl = new URL("https://duckduckgo.com/html/?q="+ddgquery);
                    HttpURLConnection ddgconn = (HttpURLConnection)ddgurl.openConnection();
                    ddgconn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"); //ddg ignores requests without proper User-Agents
                    BufferedReader ddgin = new BufferedReader(new InputStreamReader(ddgconn.getInputStream()));
                    String ddgline;
                    while ((ddgline = ddgin.readLine()) != null) {
                        if (ddgline.contains("class=\"result__url\"") && !ddgline.contains("ad_provider")) { //gets results while ignoring ads
                            String ddgresult = ddgline.substring(72, ddgline.length()-2); //filters garbage data
                            ddgresult = URLDecoder.decode(ddgresult, StandardCharsets.UTF_8); //decodes URL into a valid link
                            event.getChannel().sendMessage(ddgresult).queue();
                            break;
                        }
                    }
                    ddgin.close();
                } catch (IOException ex) {
                    event.getChannel().sendMessage("Error looking up results");
                }
                break;
                
            default:
                event.getChannel().sendMessage("Unknown command. "
                        + "Use `d.help` to see available commands").queue();
        }
    }
    
    public void mentionParse(MessageReceivedEvent event, String message, String userID) {
        String[] words = message.split(" ");
        for(int i=0;i<words.length;i++)
            if(words[i].charAt(0)=='<' && words[i].charAt(1)=='@')
                switch(words[i].charAt(2)) {
                    case '!': //A snarky remark if you mention yourself
                        if(words[i].substring(3, words[i].length()-1).equals(userID))
                            event.getChannel().sendMessage("Lmao y u @ urself").queue();
                        break;
                }
    }
    
    public void mcStatusPresence(ReadyEvent event) {
        new Timer().schedule(new TimerTask() {
            public void run() {
                try { // Show numbers of Minecraft players online as presence status
                    String[] command = {"bash","-c","mcstatus localhost status"}; //pip install mcstatus
                    ProcessBuilder p = new ProcessBuilder(command);
                    Process pr = p.start();
                    if(!pr.waitFor(2, TimeUnit.SECONDS)) //Failsafe if the command takes too long to execute
                        pr.destroy();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                    String line = "", output = "";
                    while((line=buf.readLine())!=null) //Output from the command
                        output+=line+"\n";
                    String playerNum = output.split("\\r?\\n")[2].split(" ")[1].split("/")[0];
                    switch(playerNum) {
                        case "0":
                            event.getJDA().getPresence().setActivity(null);
                            break;
                            
                        case "1":
                            event.getJDA().getPresence().setPresence(Activity
                                .watching(playerNum + " Minecraft player"),true);
                            break;
                            
                        default:
                            event.getJDA().getPresence().setPresence(Activity
                                .watching(playerNum + " Minecraft players"),true);
                    }                        
                } catch (IOException | ArrayIndexOutOfBoundsException ex) { //When the server goes offline
                    event.getJDA().getPresence().setActivity(null);
                } catch (InterruptedException ex) {}
            }
        },0,10000);
    }
    
    public String help(String topic) {
        String helpMsg = "";
        String[] topics = {
            "`d.quote random` or `d.quote @user`:\n"
            + "Quotes a random post (from member of the server)",

            "`d.8ball <optional question>`:\n"
            + "Answers a yes/no question\n"
            + "Example: `d.8ball is he horny?`",

            "`d.pick <option 1>, <option 2> ...`:\n"
            + "Chooses an option from a given selection\n"
            + "Example: `d.pick homework, left 4 dead`",

            "`d.hentai <term 1> <term 2> ...`:\n"
            + "Gives a random hentai based on the given terms\n"
            + "Example: `d.hentai english catgirl`",

            "`d.minecraft`:\n"
            + "Shows info and/or status of the Minecraft server",

            "`d.poll <topic> <choice1> <choice2> ...`:\n"
            + "Creates a poll for your fellow humans to vote on\n"
            + "Example: `d.poll \"Best drink\" Coffee Tea`",

            "`d.clone <member to clone> <fake message>`:\n"
            + "Creates a fake message as if the target member posted it\n"
            + "Example: `d.clone xXSLAYERXx I miss my mom :<`",

            "`d.search <query>`:\n"
            + "Searches a query in the world wide web\n"
            + "Example: `d.search Kombucha recipe`"
        };
        try {
            switch(Integer.parseInt(topic)) { //Split all commands into 2 pages
                case 1:
                    for(int i=0;i<topics.length/2;i++) {
                        helpMsg += topics[i];
                        if(i!=topics.length/2-1)
                            helpMsg += "\n\n";
                        else
                            helpMsg += "\n\n(Page 1/2, `d.help 2` to see the next page)";
                    }
                    return helpMsg;

                case 2:
                    for(int i=topics.length/2;i<topics.length;i++) {
                        helpMsg += topics[i];
                        if(i!=topics.length-1)
                            helpMsg += "\n\n";
                        else
                            helpMsg += "\n\n(Page 2/2)";
                    }
                    return helpMsg;

                default:
                    return null;
            }
        } catch (NumberFormatException e) { //If the user is not asking for a page
            switch(topic){
                case "all": //All commands
                    for(int i=0;i<topics.length;i++) {
                        helpMsg += topics[i];
                        if(i!=topics.length-1)
                            helpMsg += "\n\n";
                    }
                    return helpMsg;

                default: //Specific commands
                    for(int i=0;i<topics.length;i++) {
                        if(topics[i].substring(3).startsWith(topic))
                            return topics[i];
                    }
                    return null;
            }
        }
    } 
}
