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
    }
    
    public static Elements getData(String type){
        Document doc = Jsoup.parse(source);
        Elements data = null;
        switch(type){
            case "link":
                data = doc.select("a[href]");
                break;
            case"numbers":
                data = doc.select("a[href*='/g/']");
                break;
            case "title":
                data = doc.select("span");
                break;
            case "thumb":
                data = doc.select("img[class]");
                break;
            case "tags":
                data = doc.select("a[href*='/tag/'] span[class='name']");
                break;
        }
        return data;
    }
}
