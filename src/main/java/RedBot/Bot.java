package RedBot;

import java.util.List;
import java.util.Random;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
                switch (message.length) {
                    case 1:
                        event.getChannel().sendMessage("Usage: `d.pick <Option 1>, <Option 2> ...`").queue();
                        break;
                    case 2:
                        event.getChannel().sendMessage("There isn't much of a choice, is there?").queue();
                        break;
                    default:
                        String fullstring="";
                        for(int i=1;i<message.length;i++)
                            fullstring+=message[i]+" ";
                        String[] choices = fullstring.split(", ");
                        if(choices.length==1)
                            choices = choices[0].split(",");
                        for(int i=0;i<choices.length;i++)
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
    
    public String help(){
        return 
                "`d.quote random` or `d.quote @user`:\n"
                + "Quotes a random post (from member of the server)\n"
                + "\n"
                + "`d.8ball <optional question>`:\n"
                + "Answers a yes/no question\n"
                + "Example: `d.8ball is he horny?`\n"
                + "\n"
                + "`d.pick <option 1>, <option 2> ...:`\n"
                + "Chooses an option from a given selection\n"
                + "Example: `d.pick homework, left 4 dead`\n"
                + "\n"
                + "`d.hentai <term 1> <term 2> ...:`\n"
                + "Gives a random hentai based on the given terms\n"
                + "Example: `d.hentai english catgirl`";
    }
}
