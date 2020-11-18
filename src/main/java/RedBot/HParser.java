package RedBot;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HParser {
    
    private static String source="";

    public HParser(String url) { //Parses a webpage
        try {
            HParser.source = Jsoup.connect(url).get().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HParser(String url, String userAgent) { //Parses a webpage with a user agent
        try {
            HParser.source = Jsoup.connect(url).userAgent(userAgent).get().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean noResults(String element) { //Checks weather the query hasn't found anything
        Document doc = Jsoup.parse(source);
        return !doc.select(element).isEmpty();
    }
    
    public static Elements getData(String type) {
        Document doc = Jsoup.parse(source);
        Elements data = null;
        switch(type) {
            case "link": //All links on the page
                data = doc.select("a[href]");
                break;
                
            case"numbers": //Links with magic numbers
                data = doc.select("a[href*='/g/']");
                break;
                
            case "title": //Doujinshi titles
                data = doc.select("span");
                break;
                
            case "thumb": //Thumbnails, i.e. cover pages
                data = doc.select("img[class]");
                break;
                
            case "tags": //Self explanatory
                data = doc.select("a[href*='/tag/'] span[class='name']");
                break;
            
            case "result": //Google search result links
                data = doc.select("div[class='kCrYT'] a");
                break;
        }
        return data;
    }
}
