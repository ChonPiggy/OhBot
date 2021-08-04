package ohbot;


import emoji4j.EmojiUtils;
import lombok.ToString;
import ohbot.utils.PgLog;

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
import java.lang.Integer;
import java.text.SimpleDateFormat;

public class PttStockMonitorThread extends Thread {
	private byte[] lock = new byte[0];
    private boolean isUpdating = false;
    private ArrayList<String> mMonitorSpeakers = new ArrayList<String>();
    private String mLastUpdateDate = "";
    private int mUpdateFrequent = 30000; // 30s
    private ArrayList<SpeakingData> mSpeakingDataList = new ArrayList<SpeakingData>();
    private final String INGRESS_STOCK_NOTIFY_TOKEN = "uY1Tp8L0CJwLQA1AWNHx2KFntVtIDSTCdJejJJpq7vB";
    private String mLastMontioredContent = "";
    
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
    		return mUserid + ": " + mContent + " " + mTime;
    	}
    }
    
    public boolean isMatchMonitorSpeakers(String userid) {
    	return mMonitorSpeakers.contains(userid);
    }
    
    public void resetMonitorSpeakers() {
    	mMonitorSpeakers.clear();
    }

    public void run() {
        while (true) {
            try {
                if (!isUpdating) {
                	if (!mMonitorSpeakers.isEmpty() && isNeedMonitor()) {
                		checkPttStockWebsite();
                	}
                	Thread.sleep(mUpdateFrequent);
                }
                           
            } catch (Exception e) {
                //log.info("CoronaVirusWikiRankCrawlThread e: " + e);
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
    
    private boolean isNeedMonitor() {
    	// Check if date is working date
    	Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
    	int dayOfWeek = current.get(Calendar.DAY_OF_WEEK);
    	return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    }
    
    private String getCurrentDateTalkingPage() {
    	if (true) {
    		return "https://www.ptt.cc/bbs/Stock/M.1628056815.A.E3D.html";
    	}
    	else {
    		return "";
    	}
    }

    private void checkPttStockWebsite() {
        isUpdating = true;
        String talkingPage = getCurrentDateTalkingPage();
        String replyResult = "";
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
            
            PgLog.info(strResult);

            if (strResult.contains(mLastMontioredContent)) {
            	// If last content was deleted, reset it.
            	mLastMontioredContent = "";
            }
            
            if (!mLastMontioredContent.equals("")) {
            	// Jump to last check content;
            	strResult = strResult.substring(strResult.indexOf(mLastMontioredContent), strResult.length());
            }
            
            while(strResult.contains("<div class=\"push\">")) {
            	String user = "";
            	String content = "";
            	String time = "";
            	
            	// Move to new one
            	strResult = strResult.substring(strResult.indexOf("<div class=\"push\">")+18, strResult.length());
            	
            	// process user id
            	strResult = strResult.substring(strResult.indexOf("push-userid\">")+13, strResult.length());
            	user = strResult.substring(0, strResult.indexOf("</span>"));
            	user = user.replace(" ", "").trim();
            	
            	// process content
            	strResult = strResult.substring(strResult.indexOf("push-content\">")+14, strResult.length());
            	content = strResult.substring(0, strResult.indexOf("</span>"));
            	
            	// process content
            	strResult = strResult.substring(strResult.indexOf("push-ipdatetime\">")+17, strResult.length());
            	time = strResult.substring(0, strResult.indexOf("</span>"));
            	time = time.replace("", "\n");
            	
            	mLastMontioredContent = content;
            	
            	if (isMatchMonitorSpeakers(user)) {
            		SpeakingData data = new SpeakingData(user, content, time);
            		replyResult += (data.toString() + "\n");
            		mSpeakingDataList.add(data);
            	}
            	
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
        		LineNotify.callEvent(INGRESS_STOCK_NOTIFY_TOKEN, data);
        	}
        }
    }


}

