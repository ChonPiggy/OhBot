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

public class LinHoImageHelper {
    private static final String generateUrl = "https://singengo.com/api/v1/generate";

    public static HttpResponse getImageUrl(String message) {
        String result = "";
        HttpResponse response = null;
        try {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(generateUrl);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");

            request.setHeader("Connection","keep-alive");
            StringEntity params =new StringEntity("txt=%E7%89%B9%E5%83%B9&type=1&twid=");
            request.setEntity(params);
            response = httpClient.execute(request);
            // log.info("Piggy Check response: " + response);
            // log.info(String.valueOf(response.getStatusLine().getStatusCode()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public static String testImageUrl() {
        String result = "";
        try {
            String strUrl = generateUrl;
            URL url = new URL( strUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod( "POST" );
            connection.addRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setDoOutput( true );
            String parameterMessageString = new String("txt=%E7%89%B9%E5%83%B9&type=1&twid=");
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(parameterMessageString);
            printWriter.close();
            connection.connect();
            
            int statusCode = connection.getResponseCode();
            if (statusCode == 200) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String newLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((newLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(newLine);
                }
                result = stringBuilder.toString();
            }
            else {
                result = connection.getResponseMessage();
            }
            // if ( statusCode == 200 ) {
            //     result = true;
            // } else {
            //     throw new Exception( "Error:(StatusCode)" + statusCode + ", " + connection.getResponseMessage() );
            // }
            
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}