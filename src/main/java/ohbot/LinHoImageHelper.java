package ohbot;

// fork from https://gist.github.com/xchinjo/60c16be6a14ca7599cb267f153a75b25
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;

public class LinHoImageHelper {
    private static final String generateUrl = "https://singengo.com/api/v1/generate";

    public static String getImageUrl(String message) {
        String result = "";
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(generateUrl);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

            request.setHeader("Connection","keep-alive");
            StringEntity params =new StringEntity("txt=%E7%89%B9%E5%83%B9&type=1&twid=");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            log.info("Piggy Check response: " + response);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}