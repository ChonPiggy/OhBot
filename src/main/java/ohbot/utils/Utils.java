package ohbot.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Utils {
    public static void writeToFile(InputStream in, String path){
         File file = new File(path);
         System.out.println("是否为文件："+file.isFile());
         try {
             FileOutputStream out = new FileOutputStream(file);
             byte[] buffer = new byte[1024];
             int len;
             while((len=in.read(buffer))>0){
                out.write(buffer, 0, len);
             }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("================文件写入失败==================");
        }        
    }
    /*
     * 
     */
    public static String convertStreamToString(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "/n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();

    }
    
    public static boolean isNowAfterTime(String time) {


    	String format = "HH:mm:ss";

    	Date nowTime = new Date(System.currentTimeMillis());
    	try {

    		Date endTime = new SimpleDateFormat(format).parse(time);

    		if (nowTime.getTime() == endTime.getTime()) {
    			return true;
    		}
    		
    		Calendar date = Calendar.getInstance();
    		date.setTime(nowTime);

    		Calendar end = Calendar.getInstance();
    		end.setTime(endTime);

    		return date.after(end);
    	} catch (ParseException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    		return false;
    	}
    
    
    
    }
    
    public static boolean isTimeInPeriod(String nowTimeString, String startTimeString, String endTimeString) {


    	String format = "HH:mm";

    	
    	try {
    		Date nowTime = new SimpleDateFormat(format).parse(nowTimeString);
    		Date startTime = new SimpleDateFormat(format).parse(startTimeString);

    		Date endTime = new SimpleDateFormat(format).parse(endTimeString);

    		if (nowTime.getTime() == startTime.getTime()
    				|| nowTime.getTime() == endTime.getTime()) {
    			return true;
    		}
    		
    		Calendar date = Calendar.getInstance();
    		date.setTime(nowTime);

    		Calendar begin = Calendar.getInstance();
    		begin.setTime(startTime);

    		Calendar end = Calendar.getInstance();
    		end.setTime(endTime);
    		
    		if (date.after(begin) && date.before(end)) {
    			return true;
    		} else {
    			return false;
    		}
    	} catch (ParseException e) {
    		e.printStackTrace();
    		return false;
    	}
    
    }
}