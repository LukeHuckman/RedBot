package RedBot;

import java.util.List;
import java.util.Random;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
                if(event.getMessage().getMentionedMembers().get(0).getUser().isBot()){
                    event.getChannel().sendMessage("I can't quote bots").queue();
                    return; //To prevent infinite loops
                }
                List<Message> postHistory 
                        = event.getChannel().getHistoryBefore(event.getMessage(), 100)
                                .complete().getRetrievedHistory();
                if(message[1].startsWith("<@!")){ //Quote of a paricular user
                    for(int i=0;i<postHistory.size();i++){
                        int random = r.nextInt(postHistory.size()-1);
                        Message post = postHistory.get(random);
                        if(post.getAuthor().getId().equals(message[1].substring(3, message[1].length()-1))
                                && !post.getContentRaw().startsWith("d.")){
                            event.getChannel().sendMessage("\""+post.getContentRaw()+"\" - <@!"
                                    +post.getAuthor().getId()+">").queue();
                            break;
                        }
                        if(i==postHistory.size()-1)
                            event.getChannel().sendMessage("No recent posts from this user.").queue();
                    }
                }
                else if(message[1].equals("random") || message[1].equals("Random")){ //Completely random quote
                    for(int i=0;i<postHistory.size();i++){
                        int random = r.nextInt(postHistory.size()-1);
                        Message post = postHistory.get(random);
                        if(post.getAuthor().isBot())
                            continue;
                        event.getChannel().sendMessage("\""+post.getContentRaw()+"\" - <@!"
                                +post.getAuthor().getId()+">").queue();
                        break;
                    }
                }
                break;
            
            case "8ball":
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
                    "Concentrate ans ask again",
                    "Don't count on it",
                    "My reply is no",
                    "My sources say no",
                    "Outlook not so good",
                    "Very doubtful"
                };
                event.getChannel().sendMessage(":8ball: "+answer[r.nextInt(answer.length-1)]).queue();
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
                            event.getChannel().sendMessage("Lmao y u mentioning urself").queue();
                        break;
                }
    }
    
    public String help(){
        return 
                "`d.quote random` or `d.quote @user`:\n"
                + "Quotes a random post (from member of the server)\n"
                + "\n"
                + "`d.8ball <optional question>`:\n"
                + "Answers a yes/no question";
    }
}
