package ohbot;

// fork from https://gist.github.com/xchinjo/60c16be6a14ca7599cb267f153a75b25
import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

public class LineNotify {
    private static final String strEndpoint = "https://notify-api.line.me/api/notify";

    public static boolean callEvent(String token, String message, String image) {
        boolean result = false;
        try {
            message = replaceProcess(message);
            message = URLEncoder.encode(message, "UTF-8");
            if (!image.equals("")) {
            	image = replaceProcess(image);
            	image = URLEncoder.encode(image, "UTF-8");
            }
            String strUrl = strEndpoint;
            URL url = new URL( strUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("Authorization",  "Bearer " + token);
            connection.setRequestMethod( "POST" );
            connection.addRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setDoOutput( true );
            String parameterMessageString = new String("message=" + message);
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.println(parameterMessageString);
            if (!image.equals("")) {
            	String imageFile = new String("imageFile=" + image);
            	printWriter.println(imageFile);
            }
            printWriter.close();
            connection.connect();
            
            int statusCode = connection.getResponseCode();
            if ( statusCode == 200 ) {
                result = true;
            } else {
                throw new Exception( "Error:(StatusCode)" + statusCode + ", " + connection.getResponseMessage() );
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean callEvent(String token, String message) {
        return callEvent(token, message, "");
    }

    private static String replaceProcess(String txt){
            txt = replaceAllRegex(txt, "\\\\", "￥");        // \
        return txt;
    }
    private static String replaceAllRegex(String value, String regex, String replacement) {
        if ( value == null || value.length() == 0 || regex == null || regex.length() == 0 || replacement == null )
            return "";
        return Pattern.compile(regex).matcher(value).replaceAll(replacement);
    }
}