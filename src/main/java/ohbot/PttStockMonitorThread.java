package ohbot;

import ohbot.utils.PgLog;
import ohbot.utils.PgUtils;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import emoji4j.EmojiUtils;

import java.util.*;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class PttStockMonitorThread extends Thread {
	private byte[] lock = new byte[0];
    private boolean isUpdating = false;
    private boolean isReseted = false;
    private ArrayList<String> mMonitorSpeakers = new ArrayList<String>();
    private String mLastUpdateDate = "";
    private int mUpdateFrequent = 30000; // 30s
    private ArrayList<SpeakingData> mSpeakingDataList = new ArrayList<SpeakingData>();
    private final String INGRESS_STOCK_NOTIFY_TOKEN = "McE8tNie8utDcRbpBrUq9QZ7Q6qWBE9BtmM5HZwxQbo";
    private final String SayGoAndGo_STOCK_NOTIFY_TOKEN = "gABHHem5nu1LlNWhaagxbhX5Y54LDoUgYbVgZfv3ins";
    private final String Pearl_STOCK_NOTIFY_TOKEN = "KsFvfy8f1untCDH7zHeD7s5VNxtZUcYHJexRxea71ky";
    private final String Meggie_STOCK_NOTIFY_TOKEN = "jBI0QHnvD1RTm8uPoXCchHpkDVHqne98iiR3h2fxKfg";
    private final String Discipline_Forest_STOCK_NOTIFY_TOKEN = "eP12GILXuIxyAwVEX6cqqwuvRObZY6Wz9bfZt9ObkVy";
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
    	public String getUserId() {
    		return mUserid;
    	}
    	public String getContent() {
    		return mContent;
    	}
    	public String getTime() {
    		return mTime;
    	}
    	@Override
    	public boolean equals(Object obj) {
    		if (obj instanceof SpeakingData) {
    			SpeakingData data = (SpeakingData) obj;
    			if (mUserid.equals(data.getUserId()) &&
    					mContent.equals(data.getContent()) &&
    					mTime.equals(data.getTime())) {
    				return true;
    			}
    		}
    		return super.equals(obj);
    	}
    	@Override
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
    	return PgUtils.isNowAfterTime("14:00");
    }
    
    public boolean isTimeBeforeStockOpen() {
    	return PgUtils.isNowAfterTime("08:00");
    }

    public void run() {
        while (true) {
            try {
                if (!isUpdating) {
                	if (!mMonitorSpeakers.isEmpty() && isDateNeedMonitor() && isTimeNeedMonitor()) {
                		checkPttStockWebsite();
                	}
                	
                	if (mMonitorSpeakers.isEmpty()) {
                		checkPttStockWebsitePost();
                	}	
                }
                if (!isReseted && isTimeAfterStockClose()) {
            		PgLog.info("PttMonitor daily reset. getCurrentTimeString() " + getCurrentDateString() +" " + getCurrentTimeString());
            		// Do reset thing
            		mLastUpdateDate = "";
            		mSpeakingDataList.clear();
            		mLastMontioredContent = "";
            		isReseted = true;
            		mIsNewDateNotified = false;
            		mForceTargetPage = "null";
            	}
            	
            	Thread.sleep(mUpdateFrequent);
                           
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
    	return  isTimeInStockOpening();
    }
    private boolean isTimeInStockOpening() {
    	return PgUtils.isTimeInPeriod(getCurrentTimeString(), "08:30", "13:59");
    }
    
    private boolean isTimeInStockClosing() {
    	return PgUtils.isTimeInPeriod(getCurrentTimeString(), "14:00", "08:00");
    }
    
    public void setForceTargetPage(String target) {
    	PgLog.info("setForceTargetPage: " + target);
    	mForceTargetPage = target;
    }
    
    private String getCurrentDateTalkingPageFromSearch() {
    	PgLog.info("getCurrentDateTalkingPageFromSearch()");
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
	        	return getCurrentDateTalkingPageFromBoard();
	        }
	        
	        strResult = strResult.substring(strResult.indexOf("<div class=\"title\">")+19, strResult.length());
	        strResult = strResult.substring(0, strResult.indexOf("</a>"));
	        
	        strResult = strResult.substring(strResult.indexOf("<a href=\"")+9, strResult.length());
	        String targatUrl = strResult.substring(0, strResult.indexOf("\">"));
	        String title = strResult.substring(strResult.indexOf("\">")+2, strResult.length());
	        targatUrl = "https://www.ptt.cc/" + targatUrl;
	        //PgLog.info("targatUrl: " + targatUrl);
	        //PgLog.info("title: " + title);
	        if (title.contains(getCurrentDateString()) && title.contains("盤中")) {
	        	if (!mIsNewDateNotified) {
	        		processReplyToNotify("\n準備開盤囉\n" + title + "\n" + targatUrl);
	        		mIsNewDateNotified = true;
	        	}
	        	return targatUrl;
	        }
	        else {
	        	return getCurrentDateTalkingPageFromBoard();
	        }
	        
	    } catch (IOException e) {
			e.printStackTrace();
		}
    	return "";
    }
    
    private String getCurrentDateTalkingPageFromBoard() {
    	if (!mForceTargetPage.equals("null")) {
    		return mForceTargetPage;
    	}
    	try {
	    	CloseableHttpClient httpClient = HttpClients.createDefault();
	        //HttpGet httpget = new HttpGet(talkingPage);
	        HttpGet httpget = new HttpGet("https://www.ptt.cc/bbs/Stock/index.html");
	        CloseableHttpResponse response = httpClient.execute(httpget);
	        HttpEntity httpEntity = response.getEntity();
	        String strResult = EntityUtils.toString(httpEntity, "utf-8");
	        
	        if (!strResult.contains(getCurrentDateString())) {
	        	return "";
	        }
	        
	        int index = strResult.indexOf(getCurrentDateString());
	        
	        if (index < 0) {
	        	return "";
	        }
	        
	        strResult = strResult.substring(index-60, index+20);
	        strResult = strResult.substring(strResult.indexOf("<a href=\""), strResult.indexOf("</a>"));
	        
	        strResult = strResult.substring(strResult.indexOf("<a href=\"")+9, strResult.length());
	        String targatUrl = strResult.substring(0, strResult.indexOf("\">"));
	        String title = strResult.substring(strResult.indexOf("\">")+2, strResult.length());
	        targatUrl = "https://www.ptt.cc/" + targatUrl;
	        //PgLog.info("targatUrl: " + targatUrl);
	        //PgLog.info("title: " + title);
	        if (title.contains(getCurrentDateString()) && title.contains("盤中")) {
	        	if (!mIsNewDateNotified) {
	        		processReplyToNotify("\n準備開盤囉\n" + title + "\n" + targatUrl);
	        		mIsNewDateNotified = true;
	        	}
	        	return targatUrl;
	        }
	        
	    } catch (IOException e) {
			e.printStackTrace();
		}
    	return "";
    }

    private void checkPttStockWebsite() {
        //PgLog.info("checkPttStockWebsite()");
        isUpdating = true;
        //String talkingPage = getCurrentDateTalkingPageFromSearch();
        String talkingPage = getCurrentDateTalkingPageFromBoard();
        String replyResult = "\n";
        String todayMDString = getCurrentMDString() + " ";
        String strResult = "";
        if (talkingPage.equals("")) {
        	isUpdating = false;
    		return;
    	}
        //PgLog.info("checkPttStockWebsite update started. talkingPage: " + talkingPage);
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //HttpGet httpget = new HttpGet(talkingPage);
            HttpGet httpget = new HttpGet(talkingPage);
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");

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
            	mLastMontioredContent = strResult.substring(strResult.indexOf("<div class=\"push\">"), strResult.indexOf("</span></div>"));
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
            	
            	if (content.contains(".jpg")||content.contains(".jpeg")||content.contains(".png")||content.contains("http")) {
            		String url = "";
            		String contentString = "";
            		// 3 Possible if include image url
            		// Step 1: remove ": "
            		content = content.substring(2);
            		
            		// Remove space from begin/end.
            		content = content.trim();
            		

            		// Only Image
            		// |:  <a href="https://i.imgur.com/qwqKi3R.jpg" target="_blank" rel="noreferrer noopener nofollow">https://i.imgur.com/qwqKi3R.jpg</a>|

            		if((content.indexOf("<a href=\"") == 0) && (content.indexOf("</a>")+4 == content.length())) {
            			url = content.substring(content.indexOf("nofollow\">")+10, content.length()-4);
            			content = url;
            		}
            		
            		// String before image
            		// |: 老酥今天帶墨鏡<a href="https://i.imgur.com/TKutMfv.jpg" target="_blank" rel="noreferrer noopener nofollow">https://i.imgur.com/TKutMfv.jpg</a>|
            		else if((content.indexOf("<a href=\"") != 0) && (content.indexOf("</a>")+4 == content.length())) {
            			contentString = content.substring(0, content.indexOf("<a href=\""));
            			url = content.substring(content.indexOf("nofollow\">")+10, content.length()-4);
            			content = contentString + url;
            		}
            		
            		// String after image
            		// |: <a href="https://i.imgur.com/8ZhlrKO.jpg" target="_blank" rel="noreferrer noopener nofollow">https://i.imgur.com/8ZhlrKO.jpg</a> 啟航囉|
            		else if((content.indexOf("<a href=\"") == 0) && (content.indexOf("</a>")+4 != content.length())) {
            			contentString = content.substring(content.indexOf("</a>")+4, content.length());
            			url = content.substring(content.indexOf("nofollow\">")+10, content.indexOf("</a>"));
            			content = url + contentString;
            		}
            		
            		// Step end: add ": "
            		content = ": " + content;
            	}
            	
            	// process content
            	strResult = strResult.substring(strResult.indexOf("push-ipdatetime\">")+17, strResult.length());
            	time = strResult.substring(0, strResult.indexOf("</span>"));
            	time = time.replace("\n", "");
            	time = time.replace(todayMDString, "");
            	
            	/*PgLog.info("user: " + user);
            	PgLog.info("content: " + content);
            	PgLog.info("time: " + time);*/
            	
            	
            	if (isMatchMonitorSpeakers(user)) {
            		if (user.equals("gn01765288")) {
                		user = user.replace("gn01765288", EmojiUtils.emojify("::light_bulb::") + "金庸" + EmojiUtils.emojify("::light_bulb::"));
                	}
            		SpeakingData data = new SpeakingData(user, content, time);
            		if (!mSpeakingDataList.contains(data)) {
            			replyResult += (data.toString() + "\n");
            			mSpeakingDataList.add(data);
            		}
            	}
            	
            	// Finish this round and move cursor to end of this round
            	strResult = strResult.substring(strResult.indexOf("</span></div>")+13, strResult.length());
            }
            

        } catch (Exception e) {
        	e.printStackTrace();
        }
        //PgLog.info("checkPttStockWebsite update finished.");
        processReplyToNotify(replyResult);
        replyResult = null;
        isUpdating = false;
    }

    private void checkPttStockWebsitePost() {
    	// TODO
    }
    private void processReplyToNotify(String data) {
        synchronized (lock) {
        	if (data != null && !data.equals("\n")) {
        		//PgLog.info("Piggy Check notify: " +  data);
        		LineNotify.callEvent(INGRESS_STOCK_NOTIFY_TOKEN, data);
        		//LineNotify.callEvent(SayGoAndGo_STOCK_NOTIFY_TOKEN, data);
        		LineNotify.callEvent(Pearl_STOCK_NOTIFY_TOKEN, data);
        		LineNotify.callEvent(Meggie_STOCK_NOTIFY_TOKEN, data);
        		LineNotify.callEvent(Discipline_Forest_STOCK_NOTIFY_TOKEN, data);
        	}
        }
    }


}

