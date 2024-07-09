package ohbot;

import ohbot.utils.PgLog;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.*;

public class PttStockAuthorMonitorThread extends Thread {
	private byte[] lock = new byte[0];
    private boolean isUpdating = false;
    private ArrayList<AuthorData> mMonitorAuthors = new ArrayList<AuthorData>();
    private int mUpdateFrequent = 30 * 1000; // 30s
    private final String INGRESS_STOCK_NOTIFY_TOKEN = "McE8tNie8utDcRbpBrUq9QZ7Q6qWBE9BtmM5HZwxQbo";
    
    private class AuthorData {
        String mUserid = "";
        String mPost = ""; // URL
        public AuthorData(String userid, String url) {
            mUserid = userid;
            mPost = url;
        }
        public String getUserId() {
            return mUserid;
        }
        public String getPost() {
            return mPost;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AuthorData) {
                AuthorData data = (AuthorData) obj;
                if (mUserid.equals(data.getUserId()) &&
                        mPost.equals(data.getPost())) {
                    return true;
                }
            }
            return super.equals(obj);
        }
        @Override
        public String toString() {
            return mUserid + ": " + mPost;
        }
    }
    
    public boolean isMatchMonitorAuthor(String userid) {
        for (AuthorData data : mMonitorAuthors) {
            data.getUserId().equals(userid);
            return true;
        }
    	return false;
    }
    
    public String getLastestPost(String author) {
        for (AuthorData data : mMonitorAuthors) {
            data.getUserId().equals(author);
            return data.getPost();
        }
        return "";
    }
    
    public void resetMonitorAuthors() {
        mMonitorAuthors.clear();
    }

    public void run() {
        while (true) {
            try {
                if (!isUpdating) {
                	if (!mMonitorAuthors.isEmpty()) {
                		checkPttStockWebsite();
                	}

                }
            	
            	Thread.sleep(mUpdateFrequent);
                           
            } catch (Exception e) {
            	e.printStackTrace();
            }
        }
    }
    
    public void addMonitorAuthor(String author) {
        if (!isMatchMonitorAuthor(author)) {
            mMonitorAuthors.add(new AuthorData(author, ""));
        }
    }
    
    private void checkPttStockWebsite() {
        PgLog.info("checkPttStockWebsite() monitor author");
        isUpdating = true;
        String replyResult = "";
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://www.ptt.cc/bbs/Stock/search?q=標的");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");
            
            PgLog.info("strResult: " + strResult);
            while(strResult.contains("<a href=\"/bbs/Stock/M.")) {
            	String author = "";
            	String post = "";
            	String title = "";
            	
            	// Process post url
            	post = post.substring(strResult.indexOf("<a href=\"/bbs/Stock/M.")+9, strResult.length());
            	post = post.substring(0, strResult.indexOf("\">"));
            	post = "https://www.ptt.cc/" + post;
            	
            	// process title
            	title = title.substring(strResult.indexOf("<a href=\"/bbs/Stock/M.")+9, strResult.length());
            	title = title.substring(strResult.indexOf("\">")+2, strResult.length());
            	title = title.substring(0, strResult.indexOf("</a>"));
            	
            	// process author user id
            	author = author.substring(strResult.indexOf("<div class=\"author\">")+20, strResult.length());
            	author = author.substring(0, strResult.indexOf("</div>"));
            	
            	PgLog.info("author: " + author + " title: " + title + " post: " + post);
            	
            	if (isMatchMonitorAuthor(author)) {
                	String lastestPost = getLastestPost(author);
            	    if (!lastestPost.equals(post)) {
            	        replyResult = author + "\n";
            	        replyResult += title + "\n";
            	        replyResult += post;
            	        processReplyToNotify(replyResult);
            	    }
            	}
            	
            	// clean the data already Analyzed
            	strResult = strResult.substring(strResult.indexOf("<div class=\"mark\"></div>"), strResult.length());
            }
            

        } catch (Exception e) {
        	e.printStackTrace();
        }
        PgLog.info("checkPttStockWebsite monitor author update finished.");
        processReplyToNotify(replyResult);
        replyResult = null;
        isUpdating = false;
    }

    private void processReplyToNotify(String data) {
        synchronized (lock) {
        	if (data != null && !data.equals("\n")) {
        		//PgLog.info("Piggy Check notify: " +  data);
        		LineNotify.callEvent(INGRESS_STOCK_NOTIFY_TOKEN, data);
        	}
        }
    }

}

