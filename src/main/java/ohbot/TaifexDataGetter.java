package ohbot;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import ohbot.utils.PgLog;

public class TaifexDataGetter {

    public static int getCurrentTxData() {
        
        HttpClient httpclient = new DefaultHttpClient();
        String postRequest = "{\"SymbolID\":[\"TXF-S\"]}"; 
                        
        try {
            String url = "https://mis.taifex.com.tw/futures/api/getQuoteDetail";
            HttpPost request = new HttpPost(url);
            request.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            request.setHeader("Connection", "Keep-Alive");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Content-Encoding", "gzip");

            // Request body
            StringEntity reqEntity = new StringEntity(postRequest,"UTF-8");
            request.setEntity(reqEntity);
            PgLog.info("Piggy Check request: " + request);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity);
            PgLog.info("Piggy Check post result: " + result);

        } catch (Exception e) {
            PgLog.info("Exception e: " + e);
        }
        
        return -1;
    }
}