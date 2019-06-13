package ohbot;

// fork from https://gist.github.com/xchinjo/60c16be6a14ca7599cb267f153a75b25
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.util.Random;

public class PttOver18Checker {
    private static final String generateUrl = "https://www.ptt.cc/ask/over18";

    public static String sendYes() {
        String result = null;
        try {
            String strUrl = generateUrl;
            URL url = new URL( strUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod( "POST" );
            connection.addRequestProperty( "Accept", "application/json, text/javascript, */*; q=0.01" );
            connection.addRequestProperty( "Origin", "https://singengo.com" );
            connection.addRequestProperty( "X-Requested-With", "XMLHttpRequest" );
            connection.addRequestProperty( "x-hapi-key", "twitter-net_hiroki-followMe!" );
            connection.addRequestProperty( "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36" );
            connection.addRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setDoOutput(true);
            String parameterMessageString = new String("from="+"/bbs/Gossiping/index.html"+"&yes=yes");
            //String parameterMessageString = new String("txt=%E7%89%B9%E5%83%B9&type=1&twid=");
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(parameterMessageString);
            printWriter.close();
            connection.connect();
            
            int statusCode = connection.getResponseCode();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}