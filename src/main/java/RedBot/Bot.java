package RedBot;

import java.lang.Character;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jsoup.select.Elements;

public class Bot {

    public Bot() {}

    public void exec(MessageReceivedEvent event, String user, String userID,
            String command, String[] message) {
        Random r = new Random();
        switch(command){
            case "help":
                event.getChannel().sendMessage(help()).queue();
                break;
            case "quote": //Quotes a user's post from the past 100 posts
                if(message.length==1)
                    event.getChannel().sendMessage("Usage: `d.quote @user` or `d.quote random`").queue();
                else{
                    List<Message> postHistory 
                            = event.getChannel().getHistoryBefore(event.getMessage(), 100)
                                    .complete().getRetrievedHistory();
                    if(message[1].startsWith("<@!")){ //Quote of a paricular user
                        if(event.getMessage().getMentionedMembers().get(0).getUser().isBot()){
                        event.getChannel().sendMessage("I can't quote bots").queue();
                        return; //To prevent infinite loops
                    }
                        for(int i=0;i<postHistory.size();i++){
                            int random = r.nextInt(postHistory.size()-1);
                            Message post = postHistory.get(random);
                            if(post.getAuthor().getId().equals(message[1].substring(3, message[1].length()-1))
                                    && !post.getContentRaw().startsWith("d.")){
                                event.getChannel().sendMessage("`\""+post.getContentRaw()+"\"` - "
                                        +post.getAuthor().getName()).queue();
                                break;
                            }
                            if(i==postHistory.size()-1)
                                event.getChannel().sendMessage("No recent posts from this user.").queue();
                        }
                    }
                    else if(message[1].equalsIgnoreCase("random")){ //Completely random quote
                        for(int i=0;i<postHistory.size();i++){
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
                if(makeDecision){ //Controls whether decision making is actually required
                    String fullstring="";
                        for(int i=1;i<message.length;i++)
                            fullstring+=message[i]+" ";
                        if(!fullstring.contains(",")){
                            event.getChannel().sendMessage("Wrong syntax. Try using commas").queue();
                            break;
                        }
                        String[] choices = fullstring.split(", ");
                        if(choices.length==1)
                            choices = choices[0].split(",");
                        /* Duplicate check: Doesn't work for some reason
                        
                        boolean same = false;
                 outer: for(int i=0;i<choices.length;i++){
                            for(int j=i+1;j<choices.length;j++){
                                if(choices[i].equalsIgnoreCase(choices[j])){
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
                else if(!event.getTextChannel().isNSFW()){
                    event.getChannel().sendMessage("This command can only be used in a NSFW channel.").queue();
                }
                else{
                    String url = "https://nhentai.net/search/?q=";
                    for(int i=1;i<message.length;i++)
                        url +=message[i]+"+";
                    HParser Page = new HParser(url); //Get the first page of results
                    Elements links = Page.getData("link");
                    //Get the total number of results pages
                    String lastpage[] = links.last().attr("href").split("=");
                    url+="&page="+(1+r.nextInt(Integer.parseInt(lastpage[lastpage.length-1])-1));
                    Page = new HParser(url); //get a random results page
                    Elements magicNumberURLs = Page.getData("link");
                    List linksList = magicNumberURLs.subList(21, links.size()-9);
                    int magicNumber = Integer.parseInt(linksList.get(r.nextInt(linksList.size()-1))
                            .toString().split("/")[2]); //Magic digits taken from <a> tags
                    Page = new HParser("https://nhentai.net/g/"+magicNumber);
                    Elements pageTitle = Page.getData("title");
                    List rawTitle = pageTitle.subList(3, 5);
                    String title = "";
                    for(int i=0;i<rawTitle.size();i++){ // Extract and combine the title
                        String text = rawTitle.get(i).toString().split(">")[1];
                        title+=text.substring(0, text.length()-6);
                    }
                    Elements rawTags = Page.getData("tags");
                    String tags="Tags:\n";
                    for(int i=0;i<rawTags.size();i++){ //Extract the tags
                        tags += rawTags.get(i).toString().split("<")[1].substring(18);
                        if(i!=rawTags.size()-1)
                            tags += ", ";
                    }
                    Elements thumbnail = Page.getData("thumb"); //Get the thumbnail
                    //Building the embed
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setImage(thumbnail.attr("data-src"));
                    embed.setTitle(title,"https://nhentai.net/g/"+magicNumber);
                    embed.setDescription("#"+ magicNumber + "\n\n" + tags);
                    MessageEmbed hentaiEmbed = embed.build();
                    event.getChannel().sendMessage(hentaiEmbed).queue();
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
                EmbedBuilder embed = new EmbedBuilder(); //initiates the building of embed
                try{ //it tries to run whatever in the {} and if there's an error in the {}, it runs the thing in catch{}
                    embed.setTitle(message[1]); //sets the title of the embed
                } catch (ArrayIndexOutOfBoundsException ex) { //the error if some unexpected shit happens(f an unexpected error happens then the error will give a traceback and the command will fail)
                    event.getChannel().sendMessage("`d.poll <topic> <option1> <option2> <...>`").queue(); //sends the poll to the chat
                    break; //ends the loop thingy
                } 
                embed.setColor(0x2f3136); //sets the color of the embed to the same as discord dark mode bg
                embed.setAuthor(user); //sets the author of the embed, usually the user
                for(int i=2;i<message.length;i++){ // iterate from the third word of the command cause d.poll topic option, it takes the option.
                    StringBuffer sbubby = new StringBuffer(); //initiates a stringbuffer that will be used to create an emote
                    int num = i + 47; //the poll reaction emote 
                    sbubby.append(Character.toChars(num)); //append stuff that will eventually become the string
                    sbubby.append(Character.toChars(0xfe0f)); //append stuff that will eventually become the string
                    sbubby.append(Character.toChars(0x20e3)); //append stuff that will eventually become the string
                    embed.addField("Choice "+ sbubby, message[i], false); //append stuff that will eventually become the string
                } //keep looping until i is longer than the msg
                MessageEmbed pollembed = embed.build(); //embed is done
                event.getChannel().sendMessage(pollembed).queue(sentmessage -> { //sends the poll to the chat
                    for(int i=2;i<message.length;i++){
                        StringBuffer sbubby = new StringBuffer(); 
                        int num = i + 47; //refer above
                        sbubby.append(Character.toChars(num)); 
                        sbubby.append(Character.toChars(0xfe0f)); 
                        sbubby.append(Character.toChars(0x20e3)); 
                        sentmessage.addReaction(sbubby.toString()).queue(); //sends the reactions
                    } //repeats
                }); // comments by egg with the guidance of med (idk if the comments are accurate or not)
                break; //ends loop or something

            default:
                event.getChannel().sendMessage("Unknown command. "
                        + "Use `d.help` to see available commands").queue();
        }
    }
    
    public void mentionParse(MessageReceivedEvent event, String message, String userID){
        String[] words = message.split(" ");
        for(int i=0;i<words.length;i++)
            if(words[i].charAt(0)=='<' && words[i].charAt(1)=='@')
                switch(words[i].charAt(2)){
                    case '!': //A snarky remark if you mention yourself
                        if(words[i].substring(3, words[i].length()-1).equals(userID))
                            event.getChannel().sendMessage("Lmao y u @ urself").queue();
                        break;
                }
    }
    
    public void mcStatusPresence(ReadyEvent event){
        new Timer().schedule(new TimerTask(){
            public void run(){
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
                    switch(playerNum){
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
    
    public String help(){
        return 
                "`d.quote random` or `d.quote @user`:\n"
                + "Quotes a random post (from member of the server)\n"
                + "\n"
                + "`d.8ball <optional question>`:\n"
                + "Answers a yes/no question\n"
                + "Example: `d.8ball is he horny?`\n"
                + "\n"
                + "`d.pick <option 1>, <option 2> ...`:\n"
                + "Chooses an option from a given selection\n"
                + "Example: `d.pick homework, left 4 dead`\n"
                + "\n"
                + "`d.hentai <term 1> <term 2> ...`:\n"
                + "Gives a random hentai based on the given terms\n"
                + "Example: `d.hentai english catgirl`\n"
                + "\n"
                + "`d.minecraft`:\n"
                + "Shows info and/or status of the Minecraft server\n"
                + "\n"
                + "`d.poll <topic> <choice1> <choice2>`:\n"
                + "Creates a poll for your fellow humans to vote on.";
              
    } 
}
