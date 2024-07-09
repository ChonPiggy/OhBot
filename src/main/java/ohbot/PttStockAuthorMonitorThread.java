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
    private final String INGRESS_JJ_STOCK_NOTIFY_TOKEN = "4tzvLEOrmh8Z5Xm7u3ghkBhIJLTT17E4Ldqzikn78rB";
    
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
        public void setPost(String post) {
            mPost = post;
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
        PgLog.info("isMatchMonitorAuthor: " + userid);
        for (AuthorData data : mMonitorAuthors) {
            if (data.getUserId().equals(userid)) {
                PgLog.info("isMatchMonitorAuthor: " + userid + " matched.");
                return true;
            }
        }
        PgLog.info("isMatchMonitorAuthor: " + userid + " not matched.");
    	return false;
    }
    
    public String getLastestPost(String author) {
        for (AuthorData data : mMonitorAuthors) {
            if (data.getUserId().equals(author)) {
                PgLog.info("getLastestPost: " + data.getPost());
                return data.getPost();
            }
        }
        PgLog.info("getLastestPost: null");
        return "";
    }
    
    public void updateLastestPost(String author, String post) {
        PgLog.info("updateLastestPost() author: " + author + " post: " + post);
        for (AuthorData data : mMonitorAuthors) {
            if (data.getUserId().equals(author)) {
                PgLog.info("updateLastestPost() author: " + author + " post: " + post + " UPDATED.");
                data.setPost(post);
            }
        }
    }
    
    public void removeMonitorAuthor(String author) {
        PgLog.info("removeMonitorAuthor() author: " + author);
        for (AuthorData data : mMonitorAuthors) {
            if (data.getUserId().equals(author)) {
                mMonitorAuthors.remove(data);
                PgLog.info("removeMonitorAuthor() author: " + author + " REMOVED.");
                return;
            }
        }
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
        PgLog.info("addMonitorAuthor() author: " + author);
        if (!isMatchMonitorAuthor(author)) {
            mMonitorAuthors.add(new AuthorData(author, ""));
            PgLog.info("addMonitorAuthor() author: " + author + " ADDED.");
        }
    }
    
    private void checkPttStockWebsite() {
        //PgLog.info("checkPttStockWebsite() monitor author");
        isUpdating = true;
        String replyResult = "";
        String strResult = "";
        String oldPost = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://www.ptt.cc/bbs/Stock/search?q=標的");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");
            
            //PgLog.info("strResult: " + strResult);
            while(strResult.contains("<a href=\"/bbs/Stock/M.")) {
            	String author = "";
            	String post = "";
            	String title = "";
            	
            	// Process post url
            	post = strResult;
            	post = post.substring(post.indexOf("<a href=\"/bbs/Stock/M.")+9, post.length());
            	post = post.substring(0, post.indexOf("\">"));
            	post = "https://www.ptt.cc/" + post;
            	
            	// process title
            	title = strResult;
            	title = title.substring(title.indexOf("<a href=\"/bbs/Stock/M.")+9, title.length());
            	title = title.substring(title.indexOf("\">")+2, title.length());
            	title = title.substring(0, title.indexOf("</a>"));
            	
            	// process author user id
            	author = strResult;
            	author = author.substring(author.indexOf("<div class=\"author\">")+20, author.length());
            	author = author.substring(0, author.indexOf("</div>"));

            	PgLog.info("author: " + author + " title: " + title + " post: " + post);
            	
                if (oldPost.equals(post)) {
                    PgLog.info("oldPost: " + oldPost);
                    PgLog.info("post: " + post);
                    PgLog.info("Problem occur, break here.");
                    break;
                }
                oldPost = post;

            	if (isMatchMonitorAuthor(author)) {
                	String lastestPost = getLastestPost(author);
                	PgLog.info("lastestPost: " + lastestPost);
            	    if (!lastestPost.equals(post)) {
            	        PgLog.info("author: " + author + " title: " + title + " post: " + post + " ADDED.");
            	        replyResult = "\n";
            	        replyResult += author + "\n";
            	        replyResult += title + "\n";
            	        replyResult += post + "\n";
            	        replyResult += getPttStockPostContent(post);
            	        processReplyToNotify(replyResult);
            	        updateLastestPost(author, post);
            	    }
            	}
            	
            	// clean the data already Analyzed
            	strResult = strResult.substring(strResult.indexOf("<div class=\"mark\">")+18, strResult.length());
            	//PgLog.info("strResult finished: " + strResult);
            }
            

        } catch (Exception e) {
        	e.printStackTrace();
        }
        //PgLog.info("checkPttStockWebsite monitor author update finished.");
        replyResult = null;
        isUpdating = false;
    }
    
    private String getPttStockPostContent(String url) {
        //PgLog.info("getPttStockPostContent() url: " + url);
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");
            
            //PgLog.info("strResult: " + strResult);
            strResult = strResult.substring(strResult.indexOf("</span></div>"), strResult.indexOf("--\n<span"));
            int tryCount = 50;
            while(strResult.contains("</span></div>")&&tryCount>0) {
                strResult = strResult.substring(strResult.indexOf("</span></div>")+13, strResult.length());
                PgLog.info("strResult temp1: " + strResult.substring(0, 100));
                tryCount--;
            }
            PgLog.info("tryCount1: " + tryCount);
            // process link text
            tryCount = 30;
            while(strResult.contains("<a href=\"http")&&tryCount>0) {
                String firstLinkText = strResult.substring(strResult.indexOf("<a href=\""), strResult.indexOf("</a>")+4);
                strResult = strResult.replace(firstLinkText, getUrlLinkString(firstLinkText));
                PgLog.info("strResult temp2: " + strResult.substring(0, 300));
                tryCount--;
            }
            PgLog.info("tryCount2: " + tryCount);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return strResult;
    }
    
    private String getUrlLinkString(String text) {
        PgLog.info("getUrlLinkString: " + text);
        // <a href="https://udn.com/news/amp/story/7252/7944912" target="_blank" rel="noreferrer noopener nofollow">https://udn.com/news/amp/story/7252/7944912</a>
        text = text.substring(text.indexOf("<a href=\"")+9, text.length());
        text = text.substring(0, text.indexOf("\" target="));
        PgLog.info("getUrlLinkString() result: " + text);
        return text;
    }

    private void processReplyToNotify(String data) {
        synchronized (lock) {
        	if (data != null && !data.equals("")) {
        		//PgLog.info("Piggy Check notify: " +  data);
        		LineNotify.callEvent(INGRESS_JJ_STOCK_NOTIFY_TOKEN, data);
        	}
        }
    }

}

