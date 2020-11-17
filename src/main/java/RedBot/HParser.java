package RedBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HParser {
    
    private static String source="";

    public HParser(String url) {
        InputStream is = null;
        BufferedReader br;
        String line;
        String source="";
        try {
            URL address = new URL(url);
            is = address.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                source+=line;
            }
        } catch (MalformedURLException mue) {
             mue.printStackTrace();
        } catch (IOException ioe) {
             ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {}
        }
        this.source = source;
        //System.out.println(this.source);
    }

    public static void ParserHeaded(String url) { //Parses a webpage with a user agent
        try {
            String doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64").get().toString();
            HParser.source = doc;
        } catch (IOException e) {
            return;
        }
    }
    
    public boolean noResults() { //Checks weather the query hasn't found anything
        Document doc = Jsoup.parse(source);
        return !doc.select("h2").isEmpty();
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
