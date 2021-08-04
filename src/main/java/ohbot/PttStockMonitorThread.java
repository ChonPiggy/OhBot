package ohbot;


import emoji4j.EmojiUtils;
import lombok.ToString;
import ohbot.utils.PgLog;
import ohbot.utils.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.checkerframework.checker.units.qual.m;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.lang.Integer;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PttStockMonitorThread extends Thread {
	private byte[] lock = new byte[0];
    private boolean isUpdating = false;
    private boolean isReseted = true;
    private ArrayList<String> mMonitorSpeakers = new ArrayList<String>();
    private String mLastUpdateDate = "";
    private int mUpdateFrequent = 30000; // 30s
    private ArrayList<SpeakingData> mSpeakingDataList = new ArrayList<SpeakingData>();
    private final String INGRESS_STOCK_NOTIFY_TOKEN = "uY1Tp8L0CJwLQA1AWNHx2KFntVtIDSTCdJejJJpq7vB";
    private String mLastMontioredContent = "";
    private boolean mIsNewDateNotified = false;
    private String mForceTargetPage = "null";
    
    private class SpeakingData {
    	String mUserid = "";
    	String mContent = "";
    	String mTime = "";
    	public SpeakingData(String userid, String content, String time) {
    		mUserid = userid;
    		mContent = content;
    		mTime = time;
		}
    	public String toString() {
    		return mUserid + mContent + " " + mTime;
    	}
    }
    
    public boolean isMatchMonitorSpeakers(String userid) {
    	return mMonitorSpeakers.contains(userid);
    }
    
    public void resetMonitorSpeakers() {
    	mMonitorSpeakers.clear();
    }
    
    public boolean isTimeAfterStockClose() {
    	return Utils.isNowAfterTime("14:00");
    }

    public void run() {
    	PgLog.info("Piggy Check time: " + getCurrentDateString() + " " + getCurrentTimeString());
        while (true) {
            try {
                if (!isUpdating) {
                	if (!mMonitorSpeakers.isEmpty() && isDateNeedMonitor()/* && isTimeNeedMonitor()*/) {
                		checkPttStockWebsite();
                	}
                	
                	/*if (!isReseted && isTimeAfterStockClose()) {
                		// Do reset thing
                		mLastUpdateDate = "";
                		mSpeakingDataList.clear();
                		mLastMontioredContent = "";
                		isReseted = true;
                		mIsNewDateNotified = false;
                		mForceTargetPage = "null";
                	}*/
                	
                	Thread.sleep(mUpdateFrequent);
                }
                           
            } catch (Exception e) {
                //log.info("PttStockMonitorThread e: " + e);
            	e.printStackTrace();
            }
        }
    }
    
    public void addMonitorSpeaker(String speaker) {
    	mMonitorSpeakers.add(speaker);
    }
    
    private String getCurrentDateString() {
    	Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        return sdf.format(current.getTime());
    }
    
    private String getCurrentTimeString() {
    	Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(current.getTime());
    }
    
    private String getCurrentMDString() {
    	Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        return sdf.format(current.getTime());
    }
    
    private boolean isDateNeedMonitor() {
    	// Check if date is working date
    	Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
    	int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
    	return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }
    
    private boolean isTimeNeedMonitor() {
    	return Utils.isTimeInPeriod("08:30", "14:00");
    }
    
    public void setForceTargetPage(String target) {
    	PgLog.info("setForceTargetPage: " + target);
    	mForceTargetPage = target;
    }
    
    private String getCurrentDateTalkingPage() {
    	if (!mForceTargetPage.equals("null")) {
    		return mForceTargetPage;
    	}
    	try {
	    	CloseableHttpClient httpClient = HttpClients.createDefault();
	        //HttpGet httpget = new HttpGet(talkingPage);
	        HttpGet httpget = new HttpGet("https://www.ptt.cc/bbs/Stock/search?q="+getCurrentDateString().replace(":", "%2F"));
	        CloseableHttpResponse response = httpClient.execute(httpget);
	        HttpEntity httpEntity = response.getEntity();
	        String strResult = EntityUtils.toString(httpEntity, "utf-8");
	        
	        if (!strResult.contains("<div class=\"title\">")) {
	        	return "";
	        }
	        
	        strResult = strResult.substring(strResult.indexOf("<div class=\"title\">")+19, strResult.length());
	        strResult = strResult.substring(0, strResult.indexOf("</a>"));
	        
	        strResult = strResult.substring(strResult.indexOf("<a href=\"")+9, strResult.length());
	        String targatUrl = strResult.substring(0, strResult.indexOf("\">"));
	        String title = strResult.substring(strResult.indexOf("\">")+2, strResult.length());
	        targatUrl = "https://www.ptt.cc/" + targatUrl;
	        PgLog.info("targatUrl: " + targatUrl);
	        PgLog.info("title: " + title);
	        if (title.contains(getCurrentDateString()) && title.contains("盤中")) {
	        	if (!mIsNewDateNotified) {
	        		processReplyToNotify("準備開盤囉\n" + title + "\n" + targatUrl);
	        		mIsNewDateNotified = true;
	        	}
	        	return targatUrl;
	        }
	        
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }

    private void checkPttStockWebsite() {
        isUpdating = true;
        String talkingPage = getCurrentDateTalkingPage();
        String replyResult = "\n";
        String todayMDString = getCurrentMDString() + " ";
        if (talkingPage.equals("")) {
        	isUpdating = false;
    		return;
    	}
        PgLog.info("checkPttStockWebsite update started.");
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //HttpGet httpget = new HttpGet(talkingPage);
            HttpGet httpget = new HttpGet(talkingPage);
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            if (!strResult.contains(mLastMontioredContent)) {
            	// If last content was deleted, reset it.
            	mLastMontioredContent = "";
            }
            
            if (!mLastMontioredContent.equals("")) {
            	// Jump to last check content;
            	strResult = strResult.substring(strResult.indexOf(mLastMontioredContent)+mLastMontioredContent.length(), strResult.length());
            }
            if(strResult.contains("<div class=\"push\">")) {
            	strResult = strResult.substring(strResult.indexOf("<div class=\"push\">"), strResult.length());
            }
            
            while(strResult.contains("<div class=\"push\">")) {
            	mLastMontioredContent = strResult.substring(strResult.indexOf("<div class=\"push\">", strResult.indexOf("</span></div>")));
            	String user = "";
            	String content = "";
            	String time = "";
            	
            	// Move to new one
            	strResult = strResult.substring(strResult.indexOf("<div class=\"push\">")+18, strResult.length());
            	
            	// process user id
            	strResult = strResult.substring(strResult.indexOf("push-userid\">")+13, strResult.length());
            	user = strResult.substring(0, strResult.indexOf("</span>"));
            	user = user.replace(" ", "").trim();
            	if (user.equals("gn01765288")) {
            		user = "金庸";
            	}
            	
            	// process content
            	strResult = strResult.substring(strResult.indexOf("push-content\">")+14, strResult.length());
            	content = strResult.substring(0, strResult.indexOf("</span>"));
            	
            	// process content
            	strResult = strResult.substring(strResult.indexOf("push-ipdatetime\">")+17, strResult.length());
            	time = strResult.substring(0, strResult.indexOf("</span>"));
            	time = time.replace("\n", "");
            	time = time.replace(todayMDString, "");
            	
            	PgLog.info("user: " + user);
            	PgLog.info("content: " + content);
            	PgLog.info("time: " + time);
            	
            	
            	if (isMatchMonitorSpeakers(user)) {
            		SpeakingData data = new SpeakingData(user, content, time);
            		replyResult += (data.toString() + "\n");
            		mSpeakingDataList.add(data);
            	}
            	
            	// Finish this round and move cursor to end of this round
            	strResult = strResult.substring(strResult.indexOf("</span></div>")+13, strResult.length());
            	
            }
            

        } catch (Exception e) {
        	e.printStackTrace();
        }
        PgLog.info("checkPttStockWebsite update finished.");
        processReplyToNotify(replyResult);
        replyResult = null;
        isUpdating = false;
    }

    private void processReplyToNotify(String data) {
        synchronized (lock) {
        	if (data != null && !data.equals("")) {
        		PgLog.info("Piggy Check notify: " +  data);
        		//LineNotify.callEvent(INGRESS_STOCK_NOTIFY_TOKEN, data);
        	}
        }
    }


}

