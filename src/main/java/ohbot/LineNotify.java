package ohbot;

// fork from https://gist.github.com/xchinjo/60c16be6a14ca7599cb267f153a75b25
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import ohbot.utils.PgLog;

public class LineNotify {
    private static final String strEndpoint = "https://notify-api.line.me/api/notify";
    
    // Reference from: https://github.com/artit-po/Line-Notify-Api-SpringBoot
	public static LinkedHashMap<String, Object> sendLineNotifyImage(String token, String msg, MultipartFile file) {
		try {
			MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("message", msg);
			if (file != null) {
				map.add("imageFile", file.getResource());
			}
			return callLineNotify(token, map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private static LinkedHashMap<String, Object> callLineNotify(String token, MultiValueMap<String, Object> map) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		if (map.get("imageFile") != null) {
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		} else {
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		}
		headers.add("Authorization", "Bearer " + token);
		HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(map, headers);
		return restTemplate.postForObject(strEndpoint, request, LinkedHashMap.class);
	}
	
    public static boolean callLocalImageEvent(String token, String message, File image) {
    	boolean result = false;
    	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    	String charset = "UTF-8";
    	String CRLF = "\r\n"; // Line separator required by multipart/form-data.
        try {
            /*message = replaceProcess(message);
            message = URLEncoder.encode(message, "UTF-8");
            if (!image.equals("")) {
            	image = replaceProcess(image);
            	image = URLEncoder.encode(image, "UTF-8");
            }*/
            String strUrl = strEndpoint;
            URL url = new URL( strUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.addRequestProperty("Authorization",  "Bearer " + token);
            connection.setRequestMethod( "POST" );
            connection.setDoOutput( true );
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            
            OutputStream output = connection.getOutputStream();
            String parameterMessageString = new String("message=" + message);
            
            //PrintWriter printWriter = new PrintWriter(output);
            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(output, charset), true);
            //printWriter.print(parameterMessageString);
            /*if (!image.equals("")) {
            	String imageFile = new String("&imageFile=@" + image);
            	PgLog.info("imageFile: " + imageFile); 
            	printWriter.print(imageFile);
            }*/
            
            // Send binary file.
            printWriter.append("message="+message);
            printWriter.append("--" + boundary).append(CRLF);
            printWriter.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + image.getName() + "\"").append(CRLF);
            printWriter.append("Content-Type: " + URLConnection.guessContentTypeFromName(image.getName())).append(CRLF);
            printWriter.append("Content-Transfer-Encoding: binary").append(CRLF);
            printWriter.append(CRLF).flush();
            Files.copy(image.toPath(), output);
            output.flush(); // Important before continuing with writer!
            printWriter.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

            // End of multipart/form-data.
            printWriter.append("--" + boundary + "--").append(CRLF).flush();
            PgLog.info("printWriter(): " + printWriter);
            printWriter.close();
            //connection.connect();
            PgLog.info("connection.getResponseMessage(): " + connection.getResponseMessage());
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
    public static boolean callImageEvent(String token, String message, String image) {
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
            printWriter.print(parameterMessageString);
            if (!image.equals("")) {
            	String imageThumbnail = new String("&imageThumbnail=" + image);
            	String imageFullsize = new String("&imageFullsize=" + image);
            	printWriter.print(imageThumbnail);
            	printWriter.print(imageFullsize);
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
        return callImageEvent(token, message, "");
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