package ohbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.action.URIAction.AltUri;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent.Emoji;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.model.event.source.*;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import emoji4j.EmojiUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ohbot.BeautyFightingProcessor.BeautyFightingInfo;
import ohbot.aqiObj.AqiResult;
import ohbot.aqiObj.Datum;
import ohbot.stockObj.*;
import ohbot.utils.PgLog;
import ohbot.utils.Utils;

import java.lang.reflect.*;

import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Method;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.jni.Mmap;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.text.SimpleDateFormat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.lang.Integer;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.*;


/**
 * Created by lambertyang on 2017/1/13.
 */
@LineMessageHandler
@Slf4j
@RestController
public class OhBotController {

    private ArrayList<String> mEatWhatArray = new ArrayList<String>();
    private List<String> mJanDanGirlList = new ArrayList<String> ();

    private List<String> mUserAgentList = new ArrayList<String> (Arrays.asList(
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20130406 Firefox/23.0",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:18.0) Gecko/20100101 Firefox/18.0",
        "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533+ (KHTML, like Gecko) Element Browser 5.0",
        "IBM WebExplorer /v0.94', 'Galaxy/1.0 [en] (Mac OS X 10.5.6; U; en)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
        "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14",
        "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1468.0 Safari/537.36",
        "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0; TheWorld)",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36"));
    
    private List<String> mDefaultRandamLocationTitleList = Arrays.asList("正在吃飯", "正在洗澡", "死了", "正在散步", "正在合照", "正在做羞羞的事", "正在慢跑", "正在睡覺");
    private List<String> mDefaultRandamLocationAddressList = Arrays.asList("某個路邊", "某個下水溝", "某顆樹上", "某人家裡", "某個機場跑道上", "某個商店街", "某間公司");
    private List<String> mDefaultRockPaperScissors = Arrays.asList("剪刀", "石頭", "布");
    private List<String> mRandamLocationTitleList = new ArrayList<String> (mDefaultRandamLocationTitleList);
    private List<String> mRandamLocationAddressList = new ArrayList<String> (mDefaultRandamLocationAddressList);
    private boolean mIsStartJandanParsing = false;
    private boolean mIsStartJandanStarted = false;

    private int mJanDanParseCount = 0;
    private int mJanDanGifCount = 0;
    private int mJanDanMaxPage = 0;
    private int mJanDanProgressingPage = 0;
    private String mLastWorkableJsX = "";

    private String mExchangedDefaultText = "日圓";
    private String mExchangedDefaultCountry = "JPY";

    private int mPttBeautyRandomCountMin = 30;


    private boolean isKofatKeywordEnable = false;
    private boolean isEgKeywordEnable = false;
    private boolean isCathyKeywordEnable = false;
    private boolean isChuiyiKeywordEnable = false;

    private boolean isDisableBot = false;
    private boolean isPttOver18Sended = false;

    private boolean isBullyModeEnable = false;
    private int mBullyModeCount = 0;
    private String mBullyModeTarget = "";
    private String IMAGE_NO_CONSCIENCE = "https://i.imgur.com/8v9oZ2P.jpg";
    private String IMAGE_OK_FINE = "https://i.imgur.com/CNM3c0Y.jpg";
    private String IMAGE_GIVE_SALMON_NO_SWORDFISH = "https://i.imgur.com/ySGhh61.jpg";
    private String IMAGE_IF_YOU_ANGRY = "https://i.imgur.com/3ITqKUG.jpg";
    private String IMAGE_I_HAVE_NO_SPERM = "https://i.imgur.com/dL4sqfu.jpg";
    private String IMAGE_IM_NOT_YOUR_WIFE = "https://i.imgur.com/m9pXYDx.jpg";
    private String IMAGE_PANDA = "https://i.imgur.com/4RJ2AuT.jpg";
    private String IMAGE_WILL_YOU_COME = "https://i.imgur.com/11cUbVH.jpg";
    private String IMAGE_YOU_ARE_PERVERT = "https://i.imgur.com/dRJinz7.jpg";
    private String IMAGE_GPNUDD = "https://i.imgur.com/0Kr7J44.jpg";
    private String IMAGE_BE_A_GOOD_MAN = "https://i.imgur.com/Hy74quj.jpg";
    
    private List<String> mEarthQuakeMeMeImgurLinkList = Arrays.asList("https://imgur.com/a/XzsZlid.jpg", 
            "https://i.imgur.com/EqVvSJt.jpg", 
            "https://i.imgur.com/8TwTZT1.jpg",
            "https://imgur.com/XqUFKfr.jpg");

    private List<String> mIWillBeLateList = Arrays.asList("https://i.imgur.com/0cNbr9c.jpg",
                      "https://i.imgur.com/XBV3bP6.jpg",
                      "https://i.imgur.com/HDFPfUW.jpg",
                      "https://i.imgur.com/7vS3YdX.jpg",
                      "https://i.imgur.com/AhR549C.jpg",
                      "https://i.imgur.com/UqKK12E.jpg",
                      "https://i.imgur.com/QFgag6a.jpg",
                      "https://i.imgur.com/TDhfdjx.jpg",
                      "https://i.imgur.com/UunjtJN.png",
                      "https://i.imgur.com/goI7vj1.jpg",
                      "https://i.imgur.com/aTZErIM.jpg",
                      "https://i.imgur.com/eEq9MxU.jpg");

    private String IMAGE_TAIWAN_WEATHER_CLOUD = "https://www.cwb.gov.tw/Data/satellite/LCC_IR1_CR_1000/LCC_IR1_CR_1000.jpg";
    private String IMAGE_TAIWAN_WEATHER_RAIN = "https://www.cwb.gov.tw/Data/rainfall/QZJ.jpg";
    private String IMAGE_TAIWAN_WEATHER_RADAR_ECHO = "https://www.cwb.gov.tw/Data/radar/CV1_TW_1000.png";
    private String IMAGE_TAIWAN_WEATHER_TEMPERATURE = "https://www.cwb.gov.tw/Data/temperature/temp.jpg";
    private String IMAGE_TAIWAN_WEATHER_ULTRAVIOLET_LIGHT = "hhttps://www.cwb.gov.tw/Data/UVI/UVI.png";

    private List<String> mQuestionMarkImageList = Arrays.asList("https://i.imgur.com/DaTZLOa.jpg",
                      "https://i.imgur.com/93xbOIq.jpg",
                      "https://i.imgur.com/6k5QxGg.jpg",
                      "https://i.imgur.com/tFXq8Lr.jpg",
                      "https://i.imgur.com/Z987kf1.jpg",
                      "https://i.imgur.com/MSEPmEh.jpg",
                      "https://i.imgur.com/6BCL8cm.jpg",
                      "https://i.imgur.com/9eWuqBw.jpg",
                      "https://i.imgur.com/lTvALCg.jpg",
                      "https://i.imgur.com/UGAs7Qy.jpg",
                      "https://i.imgur.com/DFJs7Ww.jpg",
                      "https://i.imgur.com/Nmn5GYN.jpg",
                      "https://i.imgur.com/YR16X68.jpg",
                      "https://i.imgur.com/uPzMlqu.jpg");

    private List<String> mKofatCosplayImgurLinkList = Arrays.asList("https://i.imgur.com/gxkWn4A.jpg", 
                        "https://i.imgur.com/gb0Lq9n.jpg", 
                        "https://i.imgur.com/M9PK8Yv.jpg", 
                        "https://i.imgur.com/M9PK8Yv.jpg", 
                        "https://i.imgur.com/ModcBfG.jpg", 
                        "https://i.imgur.com/ILdOVVU.jpg", 
                        "https://i.imgur.com/9vNvyNU.jpg", 
                        "https://i.imgur.com/vCUHxNG.jpg", 
                        "https://i.imgur.com/6FnBh36.jpg", 
                        "https://i.imgur.com/LRByCFW.jpg", 
                        "https://i.imgur.com/AU6WcdZ.jpg", 
                        "https://i.imgur.com/kqMVlRL.jpg", 
                        "https://i.imgur.com/khIEZAV.jpg", 
                        "https://i.imgur.com/QxkjpS1.jpg", 
                        "https://i.imgur.com/S3zo1WG.jpg", 
                        "https://i.imgur.com/CHby1As.jpg");

    private List<String> mYouDeserveItImgurLinkList = Arrays.asList("https://i.imgur.com/lxYthkh.jpg",
                        "https://i.imgur.com/zaniSB0.jpg",
                        "https://i.imgur.com/uqcXvHg.jpg",
                        "https://i.imgur.com/GKmNzx6.jpg",
                        "https://i.imgur.com/8Nd2jdp.jpg",
                        "https://i.imgur.com/k1R05nF.png",
                        "https://i.imgur.com/O8xL0lk.jpg",
                        "https://i.imgur.com/aAI9Pwj.jpg",
                        "https://i.imgur.com/vrOtGmO.jpg",
                        "https://i.imgur.com/pL8A4nk.jpg",
                        "https://i.imgur.com/xiiPjZL.jpg",
                        "https://i.imgur.com/TP29jJ6.jpg",
                        "https://i.imgur.com/w16KAZZ.jpg",
                        "https://i.imgur.com/DA3nMD1.jpg",
                        "https://i.imgur.com/QhjuMZw.jpg",
                        "https://i.imgur.com/M02fiyV.jpg",
                        "https://i.imgur.com/fAdjcI1.jpg",
                        "https://i.imgur.com/od6GkEF.jpg",
                        "https://i.imgur.com/h2luMlP.jpg",
                        "https://i.imgur.com/tzlJlpa.jpg",
                        "https://i.imgur.com/y2yheZt.jpg",
                        "https://i.imgur.com/dJ4uunk.jpg",
                        "https://i.imgur.com/YLbewAv.jpg",
                        "https://i.imgur.com/mIHJgOV.jpg",
                        "https://i.imgur.com/My3TLjx.jpg",
                        "https://i.imgur.com/X1ruLf6.jpg",
                        "https://i.imgur.com/NFsVhu4.jpg",
                        "https://i.imgur.com/XDVLQIF.jpg");

    private String USER_ID_PIGGY = "U8147d3d84ccc1e6e12d0eb82d30b1f1a";
    private String USER_ID_KOFAT = "U9c99b691ba0b5d32de41606c19b2e2eb";
    private String USER_ID_CATHY = "U0473526c4d3f618618244132ca0d7ea0";
    private String USER_ID_MEAT_UNCLE = "U267420c1d3d7f551c2c19b312e81db86";
    private String USER_ID_TEST_MASTER = USER_ID_KOFAT;

    private String GROUP_ID_CONNECTION = "Ccc1bbf4da77b2fbbc5745be3d6ca154f";
    private String GROUP_ID_RUNRUNRUN = "C85a3ee8bcca930815577ad8955c70723";
    private String GROUP_ID_BOT_HELL = "C3691a96649f0d57c367eedb2c7f0e161";
    private String GROUP_ID_TOTYO_HOT = "C08a844342f10681cd7750d26974c5da8";
    private String GROUP_ID_INGRESS_EAT = "C0eb3ba0c74a0295aecde593c9bdc4fa3";
    private String GROUP_ID_INGRESS_FITNESS_2020 = "C1cc5b5d48eff907dd6e62bf8911bb4e1";
    private String GROUP_ID_DRAG_ON_EACH_OTHER_FITNESS = "C2593163e6a01125494fec3ee13b43eb1";    
    private String GROUP_ID_PUBLIC_ISSUES_NO_IDIOTS = "C9265f5f62fafcfd7fa4764a0047b6759";
    
    private String mRandomFootIgTargetString = "美食";
    private String mTotallyBullyUserId = USER_ID_CATHY;
    private String mTotallyBullyReplyString = "閉嘴死肥豬";
    private boolean mIsTotallyBullyEnable = false;

    private List<String> mRPSGameUserList = new ArrayList<String> ();
    private String mStartRPSGroupId = "";
    private String mStartRPSUserId = "";
    private boolean mIsUserIdDetectMode = false;
    private String mUserIdDetectModeGroupId = "";

    private String mMdMapImageSource = null;

    private List<String> mConnectionGroupRandomGirlUserIdList = new ArrayList<String> ();
    private List<String> mWizardGroupList = new ArrayList<String> (); // senderId
    private HashMap<String, PttBeautyGirl> mWhoImPickRandomGirlMap = new HashMap<>(); // userId, webLink
    private HashMap<String, PttBeautyHistory> mWhoTheyPickRandomGirlMap = new HashMap<>(); // senderId, webLink
    private HashMap<String, Integer> mTokyoHotRandomGirlLimitationList = new HashMap<>(); // userId, count
    private HashMap<String, Boolean> mPttBeautySinglePicMap = new HashMap<>(); // userId, boolean flag, default is false
    
    private class PttBeautyHistory {
        int ARRAY_MAX_ELEMENTS = 10;
        ArrayList<PttBeautyGirl> history = new ArrayList<PttBeautyGirl>(ARRAY_MAX_ELEMENTS); // search page history
        int index = -1;
        private byte[] lock = new byte[0];
        public void addHistory(PttBeautyGirl girl) {
            synchronized (lock) {
                if (history.size() >= 10) {
                    if (index+1 == ARRAY_MAX_ELEMENTS) {
                        index = 0;
                        history.set(index, girl);
                    }
                    else {
                        index++;
                        history.set(index, girl);
                    }
                }                
                else {
                    index++;
                    history.add(girl);
                }
            }
        }
        public List<PttBeautyGirl> getSearchHistoryList() {
            synchronized (lock) {
                List<PttBeautyGirl> result = new ArrayList<PttBeautyGirl>();
                int count = history.size();
                int copyIndex = index;
                while (count > 0) {
                    result.add(history.get(copyIndex));
                    copyIndex--;
                    if (copyIndex<0) {
                        copyIndex = history.size()-1;
                    }
                    count--;
                }
                return result;
            }
        }
    }

    private CoronaVirusWikiRankCrawlThread mCoronaVirusWikiRankCrawlThread = null;
    
    private HashMap<String, SheetList> mSheetListMap = new HashMap<>(); 
    private class SheetList {
        private String mSheetHolder = "";
        private String mSheetSubject = "";
        private boolean mIsFinished = false;
        private HashMap<String, String> mSheetList = new HashMap<>();
        private SheetList(String holder, String subject) {
            mSheetHolder = holder;
            mSheetSubject = subject;
        }

        public String getGuideString() {
            String result = "發起人:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "標題:" + mSheetSubject + "\n\n";
            result += "說出\"收單\"可結束表單\n\n";
            result += "說出\"查表單\"可印當前表單\n\n";
            result += "說出\"登記:XXX\"可登記商品\n\n";
            result += "如: 登記:炙燒鮭魚肚握壽司\n\n";
            result += "建議盡快結單以免資料遺失";
            return result;
        }

        public void updateData(String userId, String data) {
            mSheetList.put(userId, data);
        }

        public String getDumpResult() {
            String result = "表單:" + mSheetSubject + "\n";
            result += "發起者:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "-----\n";
            result += "點餐用:\n";
            for (String data : mSheetList.values()) {
                result += data + "\n";
            }
            result += "\n-----\n";
            result += "對帳用:\n";
            for (Map.Entry<String, String> entry : mSheetList.entrySet()) {
                result += "購買人:" + getUserDisplayName(entry.getKey()) + "\n" + "品項:" + entry.getValue();
                result += "\n---\n";
            }
            return result;
        }

        public String getHolder() {
            return mSheetHolder;
        }

        public String getSubject() {
            return mSheetSubject;
        }

        public String close() {
            mIsFinished = true;
            return getDumpResult();
        }
    }


    private HashMap<String, PlusPlusList> mPlusPlusListMap = new HashMap<>(); 
    private class PlusPlusList {
        private String mSheetHolder = "";
        private String mSheetSubject = "";
        private boolean mIsFinished = false;
        private HashMap<String, String> mPlusPlusList = new HashMap<>();
        private PlusPlusList(String holder, String subject) {
            mSheetHolder = holder;
            mSheetSubject = subject;
        }

        public String getGuideString() {
            String result = "發起人:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "標題:" + mSheetSubject + "\n\n";
            result += "說出\"截止\"可結束登記\n\n";
            result += "說出\"有誰加加\"可列出有誰加加\n\n";
            result += "說出\"+1\"代表有參加意願\n\n";
            result += "說出\"+0.5\"代表想要但不確定\n\n";
            result += "說出\"-1\"可取消登記\n\n";
            result += "建議盡快截止以免資料遺失";
            return result;
        }

        public void updateData(String userId, String data) {
            if (!data.equals("")) {
                mPlusPlusList.put(userId, data);
                return;
            }
            mPlusPlusList.remove(userId);
        }

        public String getDumpResult() {
            String result = "要的加加: " + mSheetSubject + "\n";
            int plusOneCount = 0;
            int plusPointFiveCount = 0;
            result += "發起者:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "-----\n";
            for (Map.Entry<String, String> entry : mPlusPlusList.entrySet()) {
                String countResult = "";
                if (entry.getValue().equals("0.5")) {
                    plusPointFiveCount++;
                    countResult = "0.5";
                }
                else if (entry.getValue().equals("1")) {
                    plusOneCount++;
                    countResult = "1";
                }
                result += getUserDisplayName(entry.getKey()) + " +" + countResult + "\n";
            }
            result += "-----\n";
            result += "確定人數: " + plusOneCount + "\n";
            result += "不確定人數: " + plusPointFiveCount + "\n";
            return result;
        }

        public String getHolder() {
            return mSheetHolder;
        }

        public String getSubject() {
            return mSheetSubject;
        }

        public String close() {
            mIsFinished = true;
            return getDumpResult();
        }
    }

    // <group, list>
    private HashMap<String, RandomSortList> mRandomSortMap = new HashMap<>(); 
    private class RandomSortList {
        private boolean mIsFinished = false;
        private boolean mIsSortFinished = false;
        private String mSheetHolder = "";
        private String mSheetSender = "";
        private ArrayList<String> mUserIdList = new ArrayList<String>();
        private ArrayList<String> mSortResultList = new ArrayList<String>();
        private RandomSortList(String holder, String sender) {
            mSheetHolder = holder;
            mSheetSender = sender;
        }

        public String getGuideString() {
            String result = "發起人:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "說出\"結束隨機排序\"可結束登記\n\n";
            result += "說出\"參加隨機排序\"可參加登記\n\n";
            result += "建議盡快截止以免資料遺失";
            return result;
        }

        public void addUserId(String userId) {
            if (!mUserIdList.contains(userId)) {
                mUserIdList.add(userId);
            }
        }

        public String getDumpResult() {
            String result = "發起者:" + getUserDisplayName(mSheetHolder) + "\n";
            result += "隨機排序參加成員數: " + mUserIdList.size() + "\n";
            result += "-----\n";
            for (String user : mUserIdList) {
                result += getUserDisplayName(user) + "\n";
            }
            result += "-----\n";
            return result;
        }

        public String getRandomRule() {
            String result = "請說出\"全部隨機排\"或\"隨機排序抽\"來開始排序.";
            return result;
        }

        public String getSortResult() {
            String result = "隨機排序結果:" + "\n";
            result += "-----\n";
            int count = 1;
            for (String user : mSortResultList) {
                result += ("" + count + ". " + getUserDisplayName(user) + "\n");
                count++;
            }
            result += "-----\n";
            return result;
        }

        public String getHolder() {
            return mSheetHolder;
        }

        public String runRandomSortOne() {
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(mUserIdList.size());
            String user = mUserIdList.remove(index);
            mSortResultList.add(user);
            if (mUserIdList.size() == 0) {
                mIsSortFinished = true;
                mRandomSortMap.remove(mSheetSender);
            }
            return getSortResult();
        }

        public String runRandomSortAll() {
            Random randomGenerator = new Random();
            while (mUserIdList.size() > 0) {
                int index = randomGenerator.nextInt(mUserIdList.size());
                String user = mUserIdList.remove(index);
                mSortResultList.add(user);
            }
            if (mUserIdList.size() == 0) {
                mIsSortFinished = true;
                mRandomSortMap.remove(mSheetSender);
            }
            return getSortResult();
        }

        public String stopJoin() {
            mIsFinished = true;
            return getDumpResult() + "\n\n" + getRandomRule();
        }

        public boolean isJoinFinished() {
            return mIsFinished;
        }
    }
    

    private HashSet<String> mAskedBotFriend = new HashSet<String>();
    private HashSet<String> mAskedBdCongrat = new HashSet<String>();
    private HashSet<String> mSaidBdCongrat = new HashSet<String>();
    private boolean mIsBdAdFeatureEnable = false;
    

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @RequestMapping("/")
    public String index() {
        Greeter greeter = new Greeter();
        return greeter.sayHello();
    }

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value = "city") String city) {
        String strResult = "";
        try {
            if (city != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/" + city + ".htm");
                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");

                String dateTime = "";
                String temperature = "";
                String comfort = "";
                String weatherConditions = "";
                String rainfallRate = "";

                strResult = strResult.substring(
                strResult.indexOf("<h3 class=\"CenterTitle\">今明預報<span class=\"Issued\">"), strResult.length());
                strResult = strResult.substring(0,strResult.indexOf("</tr><tr>"));
                Pattern pattern = Pattern.compile("<th scope=\"row\">.*?</th>");
                Matcher matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    dateTime = matcher.group().replaceAll("<[^>]*>", "");
                }
                pattern = Pattern.compile("<td>.*?~.*?</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    temperature = matcher.group().replaceAll("<[^>]*>","");
                }
                pattern = Pattern.compile("title=\".*?\"");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    weatherConditions = matcher.group().replace("title=\"", "").replace("\"", "");
                }
                pattern = Pattern.compile("<img.*?</td>[\\s]{0,}<td>.*?</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    comfort = matcher.group().replaceAll("<[^>]*>", "");
                }
                pattern = Pattern.compile("<td>[\\d]{0,3} %</td>");
                matcher = pattern.matcher(strResult);
                while(matcher.find()){
                    rainfallRate = matcher.group().replaceAll("<[^>]*>", "");
                }
                strResult = "氣溫"+temperature+"\n"+dateTime+"\n天氣狀況 : "+weatherConditions+"\n舒適度 : "+comfort+"\n降雨率 : "+rainfallRate;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/test")
    public String test(@RequestParam(value = "gid") String gid,@RequestParam(value = "message") String message) {
        TextMessage textMessage = new TextMessage(message);
        PushMessage pushMessage = new PushMessage(gid,textMessage);

        CompletableFuture<BotApiResponse> apiResponse = null;
        
        apiResponse = LineMessagePrimitive.pushMessage(pushMessage);
        //return String.format("Sent messages: %s %s", apiResponse.message(), apiResponse.code());
        return "";
        
    }

    @RequestMapping("/stock")
    public String stock(@RequestParam(value = "stock") String stock) {
        String strResult = "";
        try {
            if (stock != null) {
                String[] otcs = StockList.otcList;
                HashMap<String, String> otcNoMap = new HashMap<>();
                HashMap<String, String> otcNameMap = new HashMap<>();
                for (String otc : otcs) {
                    String[] s = otc.split("=");
                    otcNoMap.put(s[0], s[1]);
                    otcNameMap.put(s[1], s[0]);
                }

                String[] tses = StockList.tseList;
                HashMap<String, String> tseNoMap = new HashMap<>();
                HashMap<String, String> tseNameMap = new HashMap<>();
                for (String tse : tses) {
                    String[] s = tse.split("=");
                    tseNoMap.put(s[0], s[1]);
                    tseNameMap.put(s[1], s[0]);
                }

                System.out.println(stock);
                String companyType = "";
                Pattern pattern = Pattern.compile("[\\d]{3,}");
                Matcher matcher = pattern.matcher(stock);
                if (matcher.find()) {
                    if (otcNoMap.get(stock) != null) {
                        companyType = "otc";
                    } else {
                        companyType = "tse";
                    }
                } else {
                    if (otcNameMap.get(stock) != null) {
                        companyType = "otc";
                        stock = otcNameMap.get(stock);
                    } else {
                        companyType = "tse";
                        stock = tseNameMap.get(stock);
                    }
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://mis.twse.com.tw/stock/index.jsp";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpget.setHeader("Cache-Control", "max-age=0");
                httpget.setHeader("Connection", "keep-alive");
                httpget.setHeader("Host", "mis.twse.com.tw");
                httpget.setHeader("Upgrade-Insecure-Requests", "1");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + stock +
                      ".tw&_=" + Instant.now().toEpochMilli();
                PgLog.info(url);
                httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = "";

                Gson gson = new GsonBuilder().create();
                String s =EntityUtils.toString(httpEntity, "utf-8");
                System.out.println(s);
                StockData stockData = gson.fromJson(s, StockData.class);
                for(MsgArray msgArray:stockData.getMsgArray()){
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    Double nowPrice = Double.valueOf(msgArray.getZ());
                    Double yesterday = Double.valueOf(msgArray.getY());
                    Double diff = nowPrice - yesterday;
                    String change = "";
                    String range = "";
                    if (diff == 0) {
                        change = " " + diff;
                        range = " " + "-";
                    } else if (diff > 0) {
                        change = " +" + decimalFormat.format(diff);
                        if (nowPrice == Double.parseDouble(msgArray.getU())) {
                            range = EmojiUtils.emojify(":red_circle:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }else{
                            range = EmojiUtils.emojify(":chart_with_upwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }
                    } else {
                        change = " -" + decimalFormat.format(diff*(-1));
                        if (nowPrice == Double.parseDouble(msgArray.getW())) {
                            range = EmojiUtils.emojify(":green_circle:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }else{
                            range = EmojiUtils.emojify(":chart_with_downwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                        }
                    }
                    //開盤 : "+msgArray.getO()+"\n昨收 : "+msgArray.getY()+"
                    strResult = msgArray.getC()+" "+ msgArray.getN()+" "+change+range+" \n現價 : "+msgArray.getZ()+"\n更新 : "+msgArray.getT();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/stock2")
    public String stock2(@RequestParam(value = "stock") String stock) {
        String strResult = "";
        try {
            if (stock != null) {
                String[] otcs = StockList.otcList;
                HashMap<String, String> otcNoMap = new HashMap<>();
                HashMap<String, String> otcNameMap = new HashMap<>();
                for (String otc : otcs) {
                    String[] s = otc.split("=");
                    otcNoMap.put(s[0], s[1]);
                    otcNameMap.put(s[1], s[0]);
                }

                String[] tses = StockList.tseList;
                HashMap<String, String> tseNoMap = new HashMap<>();
                HashMap<String, String> tseNameMap = new HashMap<>();
                for (String tse : tses) {
                    String[] s = tse.split("=");
                    tseNoMap.put(s[0], s[1]);
                    tseNameMap.put(s[1], s[0]);
                }

                System.out.println(stock);
                Pattern pattern = Pattern.compile("[\\d]{3,}");
                Matcher matcher = pattern.matcher(stock);
                String stockNmae="";
                if (matcher.find()) {
                    if (otcNoMap.get(stock) != null) {
                        stockNmae = otcNoMap.get(stock);
                    } else {
                        stockNmae = tseNoMap.get(stock);
                    }
                } else {
                    if (otcNameMap.get(stock) != null) {
                        stockNmae = stock;
                        stock = otcNameMap.get(stock);
                    } else {
                        stockNmae = stock;
                        stock = tseNameMap.get(stock);
                    }
                }

                DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
                defaultHttpClient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(defaultHttpClient);
                String url="https://tw.screener.finance.yahoo.net/screener/ws?f=j&ShowID="+stock;
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpget.setHeader("Cache-Control", "max-age=0");
                httpget.setHeader("Connection", "keep-alive");
                httpget.setHeader("Upgrade-Insecure-Requests", "1");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                CloseableHttpResponse response = defaultHttpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = "";

                Gson gson = new GsonBuilder().create();
                Screener screener = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"),Screener.class);
                url="https://news.money-link.com.tw/yahoo/0061_"+stock+".html";
                httpget = new HttpGet(url);
                PgLog.info(url);
                httpget.setHeader("Accept",
                                  "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch, br");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpget.setHeader("Cache-Control", "max-age=0");
                httpget.setHeader("Connection", "keep-alive");
                httpget.setHeader("Host", "news.money-link.com.tw");
                httpget.setHeader("Upgrade-Insecure-Requests", "1");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
                response = defaultHttpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();
                Header[] ss = response.getAllHeaders();
                for(Header header:ss){
                    if(header.getName().contains("Content-Encoding"))
                    System.out.println(header.getName()+" "+header.getValue());
                }
                InputStream inputStream = httpEntity.getContent();
                inputStream = new GZIPInputStream(inputStream);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                String newLine;
                StringBuilder stringBuilder = new StringBuilder();
                while ((newLine = bufferedReader.readLine()) != null) {
                    stringBuilder.append(newLine);
                }
                strResult = stringBuilder.toString();

                //切掉不要區塊
                if (strResult.contains("<tbody>")) {
                    strResult = strResult.substring(strResult.indexOf("<tbody>"),strResult.length());
                }

                //基本評估
                String basicAssessment="\n";
                pattern = Pattern.compile("<strong>.*?</strong>.*?</td>");
                matcher = pattern.matcher(strResult);
                while (matcher.find()) {
                    String s = matcher.group();
                    basicAssessment = basicAssessment + s;
                    strResult = strResult.replace(s,"");
                }
                basicAssessment = basicAssessment.replaceAll("</td>", "\n").replaceAll("<[^>]*>", "");

                //除權息
                String XDInfo = "";
                if(strResult.contains("近1年殖利率")){
                    XDInfo = strResult.substring(0, strResult.indexOf("近1年殖利率"));
                    strResult=strResult.replace(XDInfo,"");
                }
                XDInfo = XDInfo.replaceAll("</td></tr>","\n").replaceAll("<[^>]*>", "");

                //殖利率
                String yield = "";
                pattern = Pattern.compile("近.*?</td>.*?</td>");
                matcher = pattern.matcher(strResult);
                while (matcher.find()) {
                    String s = matcher.group();
                    yield = yield + s;
                    strResult = strResult.replace(s,"");
                }
                yield = yield.replaceAll("</td>近","</td>\n近").replaceAll("<[^>]*>", "").replaceAll(" ","");

                //均線
                String movingAVG = "\n"+strResult.replaceAll("</td></tr>","\n").replaceAll("<[^>]*>", "").replaceAll(" ","");

                Item item = screener.getItems().get(0);
                System.out.println(stockNmae + " " + stock);
                System.out.println("收盤 :"+item.getVFLD_CLOSE() + " 漲跌 :" + item.getVFLD_UP_DN() + " 漲跌幅 :" + item.getVFLD_UP_DN_RATE());
                System.out.println("近52周  最高 :"+item.getV52_WEEK_HIGH_PRICE()+" 最低 :"+item.getV52_WEEK_LOW_PRICE());
                System.out.println(item.getVGET_MONEY_DATE()+" 營收 :"+item.getVGET_MONEY());
                System.out.println(item.getVFLD_PRCQ_YMD() +" 毛利率 :"+item.getVFLD_PROFIT());
                System.out.println(item.getVFLD_PRCQ_YMD() +" 每股盈餘（EPS) :"+item.getVFLD_EPS());
                System.out.println("本益比(PER) :"+item.getVFLD_PER());
                System.out.println("每股淨值(PBR) :"+item.getVFLD_PBR());
                System.out.println(item.getVFLD_PRCQ_YMD() +" 股東權益報酬率(ROE) :"+item.getVFLD_ROE());
                System.out.println("K9值 :"+item.getVFLD_K9_UPDNRATE()+"D9值 :"+item.getVFLD_D9_UPDNRATE());
                System.out.println("MACD :"+item.getVMACD());
                System.out.println(basicAssessment);
                System.out.println(XDInfo);
                System.out.println(yield);
                System.out.println(movingAVG);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/tse")
    public String tseStock() {
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://www.tse.com.tw/api/get.php?method=home_summary";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control", "max-age=0");
            httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("Host", "mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            Gson gson = new GsonBuilder().create();
            strResult = EntityUtils.toString(response.getEntity(), "utf-8");
            TseStock tseStock = gson.fromJson(strResult, TseStock.class);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/start")
    public String start(@RequestParam(value = "start") String start) {
        String strResult = "";
        try {
            if (start != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://tw.xingbar.com/cgi-bin/v5starfate2?fate=1&type="+start;
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "big5");
                strResult = strResult.substring(strResult.indexOf("<div id=\"date\">"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table><div class=\"google\">"));
                strResult = strResult.replaceAll("訂閱</a></div></td>", "");
                strResult = strResult.replaceAll("<[^>]*>", "");
                strResult = strResult.replaceAll("[\\s]{2,}", "\n");
                System.out.println(strResult);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/taiwanoil")
    public String taiwanoil() {
        String strResult = "";
        try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://taiwanoil.org/z.php?z=oiltw";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                strResult = strResult.substring(strResult.indexOf("<table"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table>\");"));
                strResult = strResult.replaceAll("</td></tr>", "\n");
                strResult = strResult.replaceAll("</td>", "：");
                strResult = strResult.replaceAll("<[^>]*>", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/aqi")
    public String aqi(@RequestParam(value = "area") String area) {
        String strResult = "";
        try {
            if (area != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://taqm.epa.gov.tw/taqm/aqs.ashx?lang=tw&act=aqi-epa";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                httpget.setHeader("Host","taqm.epa.gov.tw");
                httpget.setHeader("Connection","keep-alive");
                httpget.setHeader("Accept","*/*");
                httpget.setHeader("X-Requested-With","XMLHttpRequest");
                httpget.setHeader("User-Agent",
                                  "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                httpget.setHeader("Referer","http://taqm.epa.gov.tw/taqm/aqi-map.aspx");
                httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");

                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult =  EntityUtils.toString(httpEntity, "big5").toLowerCase();
                Gson gson = new GsonBuilder().create();
                AqiResult aqiResult = gson.fromJson(strResult, AqiResult.class);
                List<Datum> areaData = new ArrayList<>();
                for(Datum datums:aqiResult.getData()){
                    if(datums.getAreakey().equals("area")){
                        areaData.add(datums);
                    }
                }
                strResult = "";
                for (Datum datums : areaData) {
                    String aqiStyle = datums.getAQI();
                    PgLog.info(aqiStyle);
                    if (Integer.parseInt(aqiStyle) <= 50) {
                        aqiStyle = "良好";
                    } else if (Integer.parseInt(aqiStyle) >= 51 && Integer.parseInt(aqiStyle) <= 100) {
                        aqiStyle = "普通";
                    } else if (Integer.parseInt(aqiStyle) >= 101 && Integer.parseInt(aqiStyle) <= 150) {
                        aqiStyle = "對敏感族群不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 151 && Integer.parseInt(aqiStyle) <= 200) {
                        aqiStyle = "對所有族群不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 201 && Integer.parseInt(aqiStyle) <= 300) {
                        aqiStyle = "非常不健康";
                    } else if (Integer.parseInt(aqiStyle) >= 301 && Integer.parseInt(aqiStyle) <= 500) {
                        aqiStyle = "危害";
                    }
                    strResult = strResult + datums.getSitename() + " AQI : " + datums.getAQI() +"\n   " + aqiStyle+"\n";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/rate")
    public String rate(@RequestParam(value = "rate") String country) {
        String strResult = "";
        try {
            if (country != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://www.findrate.tw/"+country+"/";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                strResult = strResult.substring(strResult.indexOf("<td>現鈔買入</td>"), strResult.length());
                strResult = strResult.substring(0, strResult.indexOf("</table>"));
                strResult = strResult.replaceAll("</a></td>", " ");
                strResult = strResult.replaceAll("<[^>]*>", "");
                strResult = strResult.replaceAll("[\\s]{1,}", "");
                strResult = strResult.replaceAll("現鈔賣出", "\n現鈔賣出");
                strResult = strResult.replaceAll("現鈔買入", ":dollar:現鈔買入");
                System.out.println(EmojiUtils.emojify(strResult));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strResult;
    }

    @RequestMapping("/user")
    public String user(@RequestParam(value = "userid") String userid) {
        String strResult="";
    
        UserProfileResponse userProfileResponse = LineMessagePrimitive.getUserProfile(userid);
        strResult = userProfileResponse.getDisplayName() + "\n" + userProfileResponse.getPictureUrl();
    
        return strResult;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        PgLog.info("Received default message(Ignored): {}" + event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws IOException {
        //PgLog.info("Piggy check handleTextMessageEvent: " + event);
        handleTextContent(event.getReplyToken(), event, event.getMessage());
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {

        // Init check 
        WorldCountryPeopleCountCrawl.init();
        LineMessagePrimitive.setClient(lineMessagingClient);
        // Init check finished.
        String text = content.getText().trim();
        //PgLog.info(text);
        Source source = event.getSource();
        String senderId = source.getSenderId();
        String userId = source.getUserId();
        /*if (!isPttOver18Sended) {
            sendPttOver18Checker();
            isPttOver18Sended = true;
        }*/
        if(isAdminUserId(userId)) {
            if (text.equals("PgCommand開啟全功能")) {
                isDisableBot = false;
                this.replyText(replyToken, "好的 ＰＧ 大人");
                return;
            }
            else if (text.equals("PgCommand關閉全功能")) {
                isDisableBot = true;
                this.replyText(replyToken, "好的 ＰＧ 大人");
                return;
            }
        }

        if (isDisableBot && !isAdminUserId(userId)) {
            return;
        }
        PgLog.info("source: " + source + " name: " + getUserDisplayName(userId) + " text: " + text);

        // BD feature
        if (mIsBdAdFeatureEnable) {
            if (getUserDisplayName((userId)).equals("") && !mAskedBotFriend.contains(userId)) {
                this.replyText(replyToken, "今天是偉大的 PG 大人生日\n能不能加 BOT 好友當生日禮物呢😊");
                mAskedBotFriend.add(userId);
                return;
            }
            else if (getUserDisplayName((userId)).equals("") && mAskedBotFriend.contains(userId)) {
                return;
            }

            if(text.contains("生日快樂") || text.contains("牲日快樂") || text.contains("誕辰快樂") || 
                ((text.contains("Happy") || text.contains("happy")) && (text.contains("Birthday") || text.contains("birthday")))) {
                if (!mSaidBdCongrat.contains(userId)) {
                    this.replyText(replyToken, getUserDisplayName(userId) + "\n我代替偉大的 PG 大人感謝你😊");
                    mSaidBdCongrat.add(userId);
                    String resultText = getUserDisplayName(userId) + "\n向您說:\n" + text + "\n總數:" + mSaidBdCongrat.size();
                    LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, resultText);
                    return;
                }
            }

            if (!mSaidBdCongrat.contains(userId) && !mAskedBdCongrat.contains(userId)) {
                this.replyText(replyToken, getUserDisplayName(userId) + "\n今天是偉大的 PG 大人生日\n能不能跟他說聲生日快樂呢😊");
                mAskedBdCongrat.add(userId);
                return;
            }
            else if (!mSaidBdCongrat.contains(userId) && mAskedBdCongrat.contains(userId)) {
                return;
            }

        }
        
        // BD feature End

        if (mEarthquakeCheckThread == null) {
            mEarthquakeCheckThread = new NewestEarthquakeTimeCheckThread();
            mEarthquakeCheckThread.start();
        }

        if (mNewestDgpaReportCheckThread == null) {
            mNewestDgpaReportCheckThread = new NewestDgpaReportCheckThread();
            mNewestDgpaReportCheckThread.start();
        }

        if (mCoronaVirusWikiRankCrawlThread == null) {
            mCoronaVirusWikiRankCrawlThread = new CoronaVirusWikiRankCrawlThread();
            mCoronaVirusWikiRankCrawlThread.start();
        }

        /*if (mIngressCheckThread == null) {
            mIngressCheckThread = new NewestIngressCheckThread();
            mIngressCheckThread.start();
        }*/


        if (replyUserId(userId, senderId, replyToken)) {
            return;
        }
        // PgLog.info("senderId: " + senderId);
        // PgLog.info("userId: " + userId);

        boolean isFromPrivate = false;
        boolean isFromGroup = false;
        boolean isFromRoom = false;
        if (UserSource.class.isInstance(source)) {
            // PgLog.info("UserSource.class");
            // PgLog.info("userId: " + userId);
            isFromPrivate = true;
        }
        if (RoomSource.class.isInstance(source)) {
            // PgLog.info("RoomSource.class");
            //String roomId = source.getSenderId();
            // PgLog.info("roomId: " + roomId);
            // PgLog.info("userId: " + userId);
            isFromRoom = true;
        }
        if (GroupSource.class.isInstance(source)) {
            // PgLog.info("GroupSource.class");
            //String groupId = source.getGroupId();
            // PgLog.info("groupId: " + groupId);
            // PgLog.info("senderId: " + senderId);
            // PgLog.info("userId: " + userId);
            isFromGroup = true;
        }
        if (UnknownSource.class.isInstance(source)) {
            PgLog.info("UnknownSource.class");
        }

        if (mJanDanGirlList.size() == 0 && !mIsStartJandanStarted) {
            //mIsStartJandanStarted = true;
            //startFetchJanDanGirlImages();
        }

        checkNeedTotallyBullyReply(userId, replyToken);
        
        if (text.startsWith("PgCommand發公告:")) {
            String announeMessage = text.replace("PgCommand發公告:", "");
            AnnouncementManager.announceNewMessage(announeMessage, false, 0);
            this.replyText(replyToken, "好的PG大人, 設定公告: " + announeMessage);
        }
        
        if (text.startsWith("PgCommand取消公告")) {
            String message = AnnouncementManager.removeAnnouncement();
            if (message != null) {
                this.replyText(replyToken, "好的PG大人, 取消公告: " + message);
            }
            else {
                this.replyText(replyToken, "PG大人目前暫無公告");
            }
        }
        
        String announeMessage = AnnouncementManager.processAnnounceMessage(senderId);
        if (announeMessage != null) {
            boolean isNormalImage = announeMessage.endsWith(".jpg")||announeMessage.endsWith(".jpeg")||announeMessage.endsWith(".png");
            boolean isEarthQuakeImage = announeMessage.contains(".png")&&announeMessage.contains("cwb")&&announeMessage.contains("earthquake");
            if (announeMessage.startsWith("http") && (isNormalImage || isEarthQuakeImage)) {
                this.replyImage(replyToken, announeMessage, announeMessage);
                return;
            }
            this.replyText(replyToken, announeMessage);
            return;
        }
        
        if (text.startsWith("PgCommand測試:") && userId.equals(USER_ID_PIGGY)) {
            text = text.replace("PgCommand測試:", "");
            testPttBeautyGirl(replyToken, text);
        }

        if (text.contains("奴隸") && text.contains("滾")) {
            if (isFromGroup) {
                LineMessagePrimitive.leaveGroup(replyToken, senderId);
            }
            if (isFromRoom) {
                LineMessagePrimitive.leaveRoom(replyToken, senderId);
            }
        }


        if (text.equals("強制清除巫師求組清單") && userId.equals(USER_ID_PIGGY)) {
            isWizardWaitingListInited = false;
            initWizardWaitingList();
        }

        if (mWizardGroupList.contains(senderId)) {
            // Is wizard group

            if (text.endsWith("金加隆?") || text.endsWith("金加隆？")) {
                exchangeFromGoldGalleon(text, replyToken);
            }

            if (text.equals("指令?") || text.equals("指令？") || text.equals("功能?") || text.equals("功能？")) {
                this.replyText(replyToken, getWizardFeatureListString());
            }

            if (text.equals("取消巫師群") && userId.equals(USER_ID_PIGGY)) {
                if (mWizardGroupList.contains(senderId)) {
                    mWizardGroupList.remove(senderId);
                    this.replyText(replyToken, "已將此群組取消設定巫師群組, 可觸發一般指令.");
                }
            }

            if (text.endsWith("求組")) {
                if (getUserDisplayName(userId).equals("")) {
                    this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
                    return;
                }
                if(!isAddWizardWaitingTextValid(text)) {
                    this.replyText(replyToken, "內含不合規範的文字請重新輸入.");
                    return;
                }
                processRemoveFromWizardWaitingList(userId);
                String result = processAddToWizardWaitingList(userId, text);
                if (!result.equals("")) {
                    this.replyText(replyToken, "您已登記\n" + result);
                    return;
                }
            }

            if (text.equals("取消登記") || text.equals("取消") || text.startsWith("找到組")) {
                String result = processRemoveFromWizardWaitingList(userId);
                if (!result.equals("")) {
                    this.replyText(replyToken, "您登記的\n" + result + "已取消登記.");
                    return;
                }
            }

            if (text.equals("正氣求組清單") || text.equals("正氣?") || text.equals("正氣？")) {
                processDumpAurorWaitingList(replyToken);
                return;
            }
            if (text.equals("魔動求組清單") || text.equals("魔動?") || text.equals("魔動？")) {
                processDumpAnimalWaitingList(replyToken);
                return;
            }
            if (text.equals("教授求組清單") || text.equals("教授?") || text.equals("教授？")) {
                processDumpProfessorWaitingList(replyToken);
                return;
            }
        }

        if (text.equals("設為巫師群") && userId.equals(USER_ID_PIGGY)) {
            if (!mWizardGroupList.contains(senderId) && mWizardGroupList.size() == 0) {
                mWizardGroupList.add(senderId);
                this.replyText(replyToken, "此群組已設定為巫師群組, 只會觸發巫師相關指令.\n請參考以下指令:\n" + getWizardFeatureListString());
                return;
            }
            else if (mWizardGroupList.size() > 0) {
                this.replyText(replyToken, "ＰＧ 大人有其他群組設為巫師群組, 是否要強制設為巫師群?");
                return;
            }
        }

        if (text.equals("強制設為巫師群") && userId.equals(USER_ID_PIGGY)) {
            mWizardGroupList.clear();
            mWizardGroupList.add(senderId);
            isWizardWaitingListInited = false;
            initWizardWaitingList();
            this.replyText(replyToken, "此群組已設定為巫師群組, 只會觸發巫師相關指令.\n請參考以下指令:\n" + getWizardFeatureListString());
            return;
        }
        

        if (senderId.equals(GROUP_ID_INGRESS_EAT)) {
            // ingress eat group specific feature.
            if (userId.equals(USER_ID_MEAT_UNCLE) && text.startsWith("改抽")) {
                mRandomFootIgTargetString = text.replace("改抽", "").replace(" ", "").trim();
                this.replyText(replyToken, "好的肉叔叔, 關鍵字改為: " + mRandomFootIgTargetString);
                return;
            }
            else if (userId.equals(USER_ID_MEAT_UNCLE) && text.startsWith("現在是抽什麼")) {
                this.replyText(replyToken, "肉叔叔, 現在的關鍵字是: " + mRandomFootIgTargetString);
                return;
            }
            else if (text.equals("吃")) {
                instagramTarget(userId, senderId, mRandomFootIgTargetString, replyToken, false, false);
                return;
            }
        }

        if (text.startsWith("抽IG:") || text.startsWith("抽Ig:") || text.startsWith("抽ig:")) {
            text = text.trim().replace("抽IG:", "").replace("抽Ig:", "").replace("抽ig:", "").replace(" ", "");
            instagramTarget(userId, senderId, text, replyToken, false, true);
        }
        
        if (text.equals("設定抽單張")) {
            mPttBeautySinglePicMap.put(userId, Boolean.valueOf(true));
            this.replyText(replyToken, "好的");
            return;
        }
        else if (text.equals("設定抽多張")) {
            mPttBeautySinglePicMap.put(userId, Boolean.valueOf(false));
            this.replyText(replyToken, "好的");
            return;
        }
        
        if (text.equals("抽單張")) {
            PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, false, SINGLE_PIC);
            processReplyPttBeautyGirl(replyToken, result);
            return;
        }
        else if (text.equals("抽多張")) {
            PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, false, MULTI_PIC);
            processReplyPttBeautyGirl(replyToken, result);
            return;
        }
        else if (text.equals("爆抽單張")) {
            PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, true, SINGLE_PIC);
            processReplyPttBeautyGirl(replyToken, result);
            return;
        }
        else if (text.equals("爆抽多張")) {
            PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, true, MULTI_PIC);
            processReplyPttBeautyGirl(replyToken, result);
            return;
        }
        if (text.equals("抽")) {
            PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, false, MULTI_PIC);
            processReplyPttBeautyGirl(replyToken, result);
            //randomGirl(text, replyToken);
            return;
        }
        else if ((text.startsWith("抽") || text.startsWith("熱抽") || text.startsWith("爆抽")) && text.length() > 1) {
            if(text.replace("抽", "").replace("爆", "").replace(" ", "").trim().equals("")) {
                // Pic Source from PTT beauty
                boolean isHot = text.startsWith("爆抽");
                PttBeautyGirl result = getRandomPttBeautyGirl(userId, senderId, replyToken, isHot, MULTI_PIC);
                
                processReplyPttBeautyGirl(replyToken, result);
                return;
            }
            else {
                // Pic Source from IG
                boolean isHot = text.startsWith("熱抽");
                text = text.trim().replace("熱抽", "").replace("抽", "").replace(" ", "");

                instagramTarget(userId, senderId, text, replyToken, isHot, false);
                return;
                /*if (isStringIncludeChinese(text)) {
                    instagramTarget(text, replyToken);
                }
                else if (isStringIncludeEnglish(text)) {
                    pexelsTarget(text, replyToken);    
                }*/
            }
        }

        if (text.endsWith("天氣?") || text.endsWith("天氣？")) {
            boolean result = weatherResult(text, replyToken);
            if (!result) {
                worldWeatherResult(text, replyToken);
            }
        }

        if (text.endsWith("氣象?") || text.endsWith("氣象？")) {
            weatherResult2(text, replyToken);
        }

        if (text.endsWith("座?") || text.endsWith("座？")) {
            star(text, replyToken);
        }
        if (text.endsWith("座運勢?") || text.endsWith("座運勢？")) {
            dailyHoroscope(text, replyToken);
        }
        if (text.endsWith("油價?") || text.endsWith("油價？")) {
            taiwanoil(text, replyToken);
        }

        if ((text.startsWith("@") && text.endsWith("?")) || (text.startsWith("@") && text.endsWith("？")) ||
            (text.startsWith("＠") && text.endsWith("？")) || (text.startsWith("＠") && text.endsWith("?"))) {
            stock(text, replyToken);
        }

        if ((text.startsWith("#") && text.endsWith("?")) || (text.startsWith("#") && text.endsWith("？")) ||
            (text.startsWith("＃") && text.endsWith("？")) || (text.startsWith("＃") && text.endsWith("?"))) {
            stockMore(text, replyToken);
        }

        if (text.endsWith("空氣?") || text.endsWith("空氣？")) {
            aqiResult(text, replyToken);
        }

        if (text.endsWith("匯率?") || text.endsWith("匯率？")) {
            rate(text, replyToken);
        }

        if (text.contains("比特幣換算") && (text.endsWith("？") || text.endsWith("?"))) {
            exchangeBitcon(text, replyToken);
        }

        if (text.endsWith("換算台幣?") || text.endsWith("換算台幣？")||text.endsWith("換算臺幣?") || text.endsWith("換算臺幣？")) {
            exchangeToTwd(text, replyToken);
        }

        if (text.endsWith("金加隆?") || text.endsWith("金加隆？")) {
            exchangeFromGoldGalleon(text, replyToken);
        }

        if (text.endsWith("CMU?") || text.endsWith("CMU？") || text.endsWith("cmu?") || text.endsWith("cmu？")
             || text.endsWith("ＣＭＵ?") || text.endsWith("ＣＵＭ？")) {
            exchangeFromIngressCMU(text, replyToken);
        }

        if ((text.contains("台幣換算") || text.contains("台幣換算")||text.contains("臺幣換算") || text.contains("臺幣換算")) &&
            (text.endsWith("?") || text.endsWith("？"))) {
            exchangeFromTwd(text, replyToken);
        }

        if (text.startsWith("呆股?") || text.startsWith("呆股？")) {
            tse(text, replyToken);
        }

        if (text.equals("@?") || text.equals("@？")) {
            help2(text, replyToken);
        }
        if (text.equals("#?") || text.equals("＃？")) {
            help(text, replyToken);
        }
        if (text.endsWith("?") || text.endsWith("？")) {
            exchangeDefault(text, replyToken);
        }
        if (text.equals("每日一句?") || text.equals("每日一句？")) {
            dailySentence(text, replyToken);
        }
        if (text.equals("今日我最美?") || text.equals("今日我最美？")) {
            dailyBeauty(text, replyToken);
        }
        if (text.equals("今日我最美是誰?") || text.equals("今日我最美是誰？")) {
            dailyBeautyName(text, replyToken);
        }
        if (text.equals("吃什麼?") || text.equals("吃什麼？")) {
            eatWhat(text, replyToken);
        }

        if (text.equals("天氣雲圖?") || text.equals("天氣雲圖？")) {
            replyTaiwanWeatherCloudImage(replyToken);
        }

        if (text.equals("累積雨量圖?") || text.equals("累積雨量圖？")) {
            replyTaiwanWeatherRainImage(replyToken);
        }

        if (text.equals("雷達回波圖?") || text.equals("雷達回波圖？") || text.equals("雷達迴波圖?") || text.equals("雷達迴波圖？")) {
            replyTaiwanWeatherRadarEchoImage(replyToken);
        }

        if (text.equals("溫度分佈圖?") || text.equals("溫度分佈圖？") || text.equals("溫度分布圖?") || text.equals("溫度分布圖？")) {
            replyTaiwanWeatherTemperatureImage(replyToken);
        }

        if (text.equals("紫外線圖?") || text.equals("紫外線圖？")) {
            replyTaiwanWeatherUltravioletLightImage(replyToken);
        }

        if (text.contains("熊貓")) {
            replyImageTaiwanBearAndPanda(replyToken);
        }

        if (text.contains("會來嗎")) {
            replyImageWillYouCome(replyToken);
        }

        if (text.contains("今天") && text.contains("當") && text.contains("好人")) {
            replyImageBeAGoodMand(replyToken);
        }

        if (text.contains("我") && text.contains("老婆")) {
            replyImageIamNotYourWife(replyToken);
        }

        if (text.contains("晚點到") || text.contains("遲到") || text.contains("晚到") ) {
            replyImageIWillBeLate(replyToken);
        }

        if (text.contains("活該") || text.contains("你看看你") || text.contains("妳看看妳") ) {
            replyImageYouDeserveIt(replyToken);
        }

        if (text.contains("變態")) {
            replyImageYouArePrev(replyToken);
        }

        if (text.endsWith("幾台?") || text.endsWith("幾台？") || text.endsWith("幾臺?") || text.endsWith("幾臺？")) {
            replyTextMjHowManyTai(replyToken, text);
        }

        if (text.endsWith("幾歲?") || text.endsWith("幾歲？")) {
            replyTextHowOld(replyToken, text);
        }

        if (text.startsWith("我") && text.contains("抽了誰")) {
            whoImPickRandomPttBeautyGirlMap(userId, replyToken);
        }

        if ((text.startsWith("他") || text.startsWith("她")|| text.startsWith("牠")|| text.startsWith("它")|| text.startsWith("祂")) && text.contains("抽了誰")) {
            whoTheyPickRandomPttBeautyGirlMap(senderId, replyToken);
        }

        // MOZE

        if (isFromPrivate && (text.startsWith("記帳")||text.startsWith("記賬"))) {
            text = text.substring(2,text.length());
            String result = generateMozeUrlScheme(text);
            if (!result.equals("")) {
                this.replyText(replyToken, result);
            }
        }

        // MOZE end

        // Sheet Feature 

        if (text.startsWith("開表單:")||text.startsWith("開表單：")) {
            processSheetOpen(replyToken, senderId, userId, text);
        }

        if (text.equals("查表單")) {
            processSheetDump(replyToken, senderId, userId);
        }

        if (text.equals("收單")) {
            processSheetClose(replyToken, senderId, userId);
        }

        if (text.startsWith("登記:")||text.startsWith("登記：")) {
            processSheetAdd(replyToken, senderId, userId, text);
        }

        // Sheet Feature End

        // PlusPlus Sheet Feature

        if (text.endsWith("要的加加")) {
            processPlusPlusOpen(replyToken, senderId, userId, text);
        }

        if (text.equals("有誰加加")) {
            processPlusPlusDump(replyToken, senderId, userId);
        }

        if (text.equals("截止")) {
            processPlusPlusClose(replyToken, senderId, userId);
        }

        if (text.equals("+1")||text.equals("+0.5")||text.equals("-1")) {
            processPlusPlusAdd(replyToken, senderId, userId, text);
        }

        // PlusPlus Sheet Feature End

        if (text.endsWith("站?")||text.endsWith("站？")) {
            text = text.replace("？", "").replace("?", "").trim();
            text = text.substring(0,text.length()-1);
            String url = MrtPdfUrlMaker.getMrtPdfUrl(text);
            if (url.length() > 0) {
                this.replyText(replyToken, url);
            }
            else {
                //this.replyText(replyToken, "沒有這個站.");
            }
        }

        if (text.endsWith("停班停課?") || text.endsWith("停班停課？")) {
            text = text.replace("？", "").replace("?", "").trim();
            text = text.replace("停班停課", "");
            if (text.length() == 0) {
                this.replyText(replyToken, getDgpaReportText());
            }
            else if (text.equals("北部")) {
                this.replyText(replyToken, getDgpaNorthReportText());
            }
            else if (text.equals("中部")) {
                this.replyText(replyToken, getDgpaMiddleReportText());
            }
            else if (text.equals("南部")) {
                this.replyText(replyToken, getDgpaSouthReportText());
            }
            else if (text.equals("東部")) {
                this.replyText(replyToken, getDgpaEastReportText());
            }
            else if (text.equals("離島")) {
                this.replyText(replyToken, getDgpaSeaReportText());
            }
            else {
                String result = getDgpaReportText();
                text = text.replace("台", "臺");
                if (result.indexOf(text) > 0) {
                    result = result.substring(result.indexOf(text), result.length());
                    if (result.indexOf(EmojiUtils.emojify(":moyai:")) > 0) {
                        result = result.substring(0, result.indexOf(EmojiUtils.emojify(":moyai:")));
                    }
                    if (result.indexOf(EmojiUtils.emojify(":office:")) > 0) {
                        result = result.substring(0, result.indexOf(EmojiUtils.emojify(":office:")));
                    }
                    result = EmojiUtils.emojify(":office:") + result;
                }
                this.replyText(replyToken, result);
            }
        }

        if (text.endsWith("時差?")||text.endsWith("時差？")) {
            text = text.replace("？", "").replace("?", "").trim();
            text = text.replace("時差", "").replace(" ", "");
            processJetLag(replyToken, text);
        }
        if (text.equals("武漢肺炎") || text.equals("中國肺炎") || text.equals("中肺") || text.equals("武肺")) {
            this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_DEFAULT, -1));
        }
        else if (text.equals("武漢肺炎確診") || text.equals("中國肺炎確診") || text.equals("武肺確診") || text.equals("中肺確診")) {
            this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_CONFIRM, -1));
        }
        else if (text.equals("武漢肺炎死亡") || text.equals("中國肺炎死亡") || text.equals("武肺死亡") || text.equals("中肺死亡")) {
            this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_DEAD, -1));
        }
        else if (text.equals("武漢肺炎痊癒") || text.equals("中國肺炎痊癒") || text.equals("武肺痊癒") || text.equals("中肺痊癒")) {
            this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_HEAL, -1));
        }
        else if (text.startsWith("武漢肺炎前") || text.startsWith("中國肺炎前")) {
            text = text.replace("武漢肺炎前", "").replace("中國肺炎前", "").replace(" ", "").trim();
            int number = -1;
            try {
                number = Integer.parseInt(text);
            } catch (java.lang.NumberFormatException e) {
            }
            if (number > 0) {
                this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_DEFAULT, number));
            }
        }
        else if (text.startsWith("武漢肺炎確診前") || text.startsWith("中國肺炎確診前")) {
            text = text.replace("武漢肺炎確診前", "").replace("中國肺炎確診前", "").replace(" ", "").trim();
            int number = -1;
            try {
                number = Integer.parseInt(text);
            } catch (java.lang.NumberFormatException e) {
            }
            if (number > 0) {
                this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_CONFIRM, number));
            }
        }
        else if (text.startsWith("武漢肺炎死亡前") || text.startsWith("中國肺炎死亡前")) {
            text = text.replace("武漢肺炎死亡前", "").replace("中國肺炎死亡前", "").replace(" ", "").trim();
            int number = -1;
            try {
                number = Integer.parseInt(text);
            } catch (java.lang.NumberFormatException e) {
            }
            if (number > 0) {
                this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_DEAD, number));
            }
        }
        else if (text.startsWith("武漢肺炎痊癒前") || text.startsWith("中國肺炎痊癒前")) {
            text = text.replace("武漢肺炎痊癒前", "").replace("中國肺炎痊癒前", "").replace(" ", "").trim();
            int number = -1;
            try {
                number = Integer.parseInt(text);
            } catch (java.lang.NumberFormatException e) {
            }
            if (number > 0) {
                this.replyText(replyToken, mCoronaVirusWikiRankCrawlThread.dumpList(CoronaVirusInfo.TYPE_HEAL, number));
            }
        }
        else if ((text.startsWith("武漢肺炎") || text.startsWith("武肺"))&& text.length() < 20) {
            String country = text.replace("武漢肺炎", "").trim();
            if (country.equals("台灣")||country.equals("中華民國")) {
                country = "臺灣";
            }
            if (country.equals("瘟疫大陸")||country.equals("中國")||country.equals("大陸")||country.equals("426")||country.equals("阿六仔")||country.equals("啊六仔")) {
                country = "中國大陸";
            }
            if (country.equals("全球")) {
                country = "世界";
            }
            if (country.equals("臺灣")) {
                String result = "";
                result = getChinaVirusTaiwanData();
                if (!result.equals("")) {
                    this.replyText(replyToken, result);
                    return;    
                }
            }
            String result = mCoronaVirusWikiRankCrawlThread.getCountryDetail(country);
            if (result != null) {
                this.replyText(replyToken, result);
            }
            else {
                if ((!text.equals("武漢肺炎") || !text.equals("中國肺炎")) && text.length() > 2) {
                    this.replyText(replyToken, country + " 並不存在或者名稱不完全正確");
                }
            }
        }

        if (text.endsWith("人口?") || text.endsWith("人口？")) {
            String country = text.replace("人口", "").replace("？", "").replace("?", "").replace(" ", "").replace(" ", "").trim();
            WorldCountryPeopleInfo info = WorldCountryPeopleCountCrawl.getCountryPeopleInfo(country);
            if (info != null) {
                this.replyText(replyToken, "" + info);
            }
            else {
                notifyMessage(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, "人口查詢失敗!\n錯誤字串: " + text, replyToken);
            }
            
        }

        if (text.equals("體脂總表")||text.equals("體脂圖表")) {
            if (senderId.equals(GROUP_ID_DRAG_ON_EACH_OTHER_FITNESS)||userId.equals(USER_ID_PIGGY)) {
                String googleShareSheetsBodyFat = "https://docs.google.com/spreadsheets/d/e/2PACX-1vSyyFe-lVckUfGCeY0iuOpdA-xJXozLRZVNBxxpWAfIMtKawF2sK45DWua6MmQTl6VUqXXwnrsZqIa3/pubchart?oid=1886194158&format=image";
                this.replyImage(replyToken, googleShareSheetsBodyFat, googleShareSheetsBodyFat);
            }
        }

        if (text.equals("體重總表")||text.equals("體重圖表")) {
            if (senderId.equals(GROUP_ID_DRAG_ON_EACH_OTHER_FITNESS)||userId.equals(USER_ID_PIGGY)) {
                String googleShareSheetsWeight = "https://docs.google.com/spreadsheets/d/e/2PACX-1vSyyFe-lVckUfGCeY0iuOpdA-xJXozLRZVNBxxpWAfIMtKawF2sK45DWua6MmQTl6VUqXXwnrsZqIa3/pubchart?oid=569446092&format=image";
                this.replyImage(replyToken, googleShareSheetsWeight, googleShareSheetsWeight);
            }
        }
        String avWiki = checkAvWiki(text);

        if (avWiki != null) {
            this.replyText(replyToken, avWiki);
        }

        if (text.startsWith("AvWiki:")) {
            AvWikiInfo info = getAvWiki(text.replace("AvWiki:",""));
            if (info != null) {
                processReplyAvWikiList(replyToken, info);
            }
        }

        if (text.startsWith("AmazonJp:")) {
            amazonJpSearch(replyToken, text);
        }

        // Japanese name translator

        if (text.endsWith("的平假名?") || text.endsWith("的平假名？") || text.endsWith("的平假名是?") || text.endsWith("的平假名是？")) {
            japaneseNameToHiragana(replyToken, text);
        }

        if (text.endsWith("的片假名?") || text.endsWith("的片假名？") || text.endsWith("的片假名是?") || text.endsWith("的片假名是？")) {
            japaneseNameToKatakana(replyToken, text);
        }

        if (text.endsWith("的羅馬拼音?") || text.endsWith("的羅馬拼音？") || text.endsWith("的羅馬拼音是?") || text.endsWith("的羅馬拼音是？")) {
            japaneseNameToRomaji(replyToken, text);
        }
        
        if (text.startsWith("http") && (text.endsWith(".png")||text.endsWith(".jpeg")||text.endsWith(".jpg")||text.endsWith(".webp"))) {
            this.replyImage(replyToken, text, text);
        }

        if (text.startsWith("PgCommand設定MD地圖:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            text = text.replace("PgCommand設定MD地圖:", "").trim();
            mMdMapImageSource = text;
            this.replyText(replyToken, "設定MD地圖完成: " + text);
        }

        if (text.startsWith("PgCommand開啟生日快樂廣告")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            mIsBdAdFeatureEnable = true;
            this.replyText(replyToken, "好的 PG 大人");
        }
        if (text.startsWith("PgCommand關閉生日快樂廣告")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            mIsBdAdFeatureEnable = false;
            this.replyText(replyToken, "好的 PG 大人");
        }

        if (text.startsWith("PgCommandNotifyMessage:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            notifyMessage(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, text, replyToken);
        }

        if (text.startsWith("PgCommandNotifyImage:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            notifyImage(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM , text, replyToken);
        }
        
        if (text.startsWith("PgCommand新增吃什麼:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            updateEatWhat(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除吃什麼:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            deleteEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand清空吃什麼")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            cleanEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand列出吃什麼")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            dumpEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand煎蛋進度")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            randomGirlProgressing(text, replyToken);
        }
        if (text.equals("PgCommand煎蛋數量")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            randomGirlCount(text, replyToken);
        }
        if (text.startsWith("PgCommand煎蛋解碼:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            randomGirlDecode(text, replyToken);
        }
        if (text.startsWith("PgCommand煎蛋解碼圖:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            randomGirlDecodeImage(text, replyToken);
        }
        if (text.startsWith("PgCommand圖片:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            replyInputImage(text, replyToken);
        }
        if (text.equals("PgCommand開始煎蛋")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            startFetchJanDanGirlImages();
        }

        if (text.startsWith("PgCommand新增隨機地點:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            updateRandomAddress(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除隨機地點:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            deleteRandomAddress(text, replyToken);
        }
        if (text.equals("PgCommand清空隨機地點")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            cleanRandomAddress(text, replyToken);
        }
        if (text.equals("PgCommand列出隨機地點")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            dumpRandomAddress(text, replyToken);
        }

        if (text.startsWith("PgCommand新增隨機動作:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            updateRandomTitle(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除隨機動作:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            deleteRandomTitle(text, replyToken);
        }
        if (text.equals("PgCommand清空隨機動作")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            cleanRandomTitle(text, replyToken);
        }
        if (text.equals("PgCommand列出隨機動作")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            dumpRandomTitle(text, replyToken);
        }
        if (text.startsWith("PgCommand設定預設匯率:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            setDefaultExchanged(text,replyToken);
        }

        if (text.startsWith("PgCommand使用者顯示名稱:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            printUserDisplayName(text, replyToken);
        }

        if (text.startsWith("PgCommand使用者顯示圖片:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            printUserDisplayPicture(text, replyToken);
        }

        if (text.startsWith("PgCommand開始徹底霸凌")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            startTotallyBully(replyToken);
        }

        if (text.startsWith("PgCommand停止徹底霸凌")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            stopTotallyBully(replyToken);
        }

        if (text.startsWith("PgCommand設定徹底霸凌對象:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            if (isAdminUserId(text)) {
                this.replyText(replyToken, "小的哪敢霸凌偉大的管理員大人...");
                return;
            }
            setTotallyBullyUser(text, replyToken);
        }

        if (text.startsWith("PgCommand設定代理管理員:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            setTestAdminUser(text, replyToken);
        }

        if (text.startsWith("PgCommand設定徹底霸凌字串:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            setTotallyBullyString(text, replyToken);
        }

        if (text.equals("PgCommand強制終止猜拳")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            forceStopRPS(replyToken);
        }

        if (text.startsWith("PgCommandGetHtml:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            text = text.replace("PgCommandGetHtml:", "");
            getHtml(replyToken, text);
        }

        /*if (text.equals("PgCommand開始偵測ID")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            startUserIdDetectMode(senderId, replyToken);
        }

        if (text.equals("PgCommand停止偵測ID")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            stopUserIdDetectMode(senderId, replyToken);
        }*/

        if (text.equals("PgCommand表特最小推數設定值")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            this.replyText(replyToken, "ＰＧ大人目前設定值為 " + mPttBeautyRandomCountMin);
            return;
        }
        if (text.startsWith("PgCommand表特最小推數設定為")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            int number = 10;
            try {
                number = Integer.parseInt(text.replace("PgCommand表特最小推數設定為", ""));
            } catch (java.lang.NumberFormatException e) {
                this.replyText(replyToken, "ＰＧ 大人數值偵測錯誤\n輸入值為: " + text.replace("PgCommand表特最小推數設定為", ""));
                return;
            }
            if (number >= 100) {
                number = 99;
            }
            else if (number < 10) {
                number = 10;
            }
            mPttBeautyRandomCountMin = number;
            this.replyText(replyToken, "ＰＧ 大人目前設定值為 " + mPttBeautyRandomCountMin);
            return;
        }

        if (text.equals("PgCommand最新地震報告圖網址")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            this.replyText(replyToken, mNewestEarthquakeReportImage);
        }
        
        if (text.equals("PgCommand最新地震報告網址")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            this.replyText(replyToken, mNewestEarthquakeReportText);
        }
        
        if (text.startsWith("IgLocation:")) {
            String search = text.replace("IgLocation:", "");
            List<InstagramLocation> igLocationList = searchIgLocation(search, 10);
            
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(igLocationList.size());
            String title = igLocationList.get(random_num).getTitle();
            
            if (igLocationList != null) {
                List<InstagramItem> igList = getRandomInstagramImageUrl(userId, senderId, igLocationList.get(random_num).getUrl(), true, false);
                List<ImageItem> resultList = new ArrayList<ImageItem>();
                for(int i=0;i<igList.size();i++) {
                    InstagramItem item = igList.get(i);
                    resultList.add(new ImageItem(item.getLike()+EmojiUtils.emojify(":heart:"), item.getImgUrl(), item.getUrl()));
                }
                if (resultList.size() > 0) {
                    processReplyImageCarouselTemplateFromStringList(replyToken, resultList, title);
                }
            }
        }
        
        if (mBeautyFightingProcessor.processBeautyFightingString(replyToken, senderId, userId, text)) {
            return;
        }
        
        /*if (text.contains("www.instagram.com")&&text.contains("explore")&&text.contains("locations")) {*/
        if (text.contains("www.instagram.com\\/explore\\/locations\\/")) {
            List<InstagramItem> igList = getRandomInstagramImageUrl(userId, senderId, text, true, false);
            List<ImageItem> resultList = new ArrayList<ImageItem>();
            for(int i=0;i<igList.size();i++) {
                InstagramItem item = igList.get(i);
                resultList.add(new ImageItem(item.getLike()+EmojiUtils.emojify(":heart:"), item.getImgUrl(), item.getUrl()));
            }
            if (resultList.size() > 0) {
                processReplyImageCarouselTemplateFromStringList(replyToken, resultList, "偵測到IG地點自動觸發");
                return;
            }
        }

        if (text.equals("我的LineId")) {
            this.replyText(replyToken, "您的 Line User Id 為: " + userId);
            return;
        }

        if (text.equals("我的Line群組Id")) {
            this.replyText(replyToken, "您的 Line Group Id 為: " + senderId);
            
            return;
        }

        if (text.equals("最新地震報告圖") || text.contains("地震報告圖")) {
            this.replyImage(replyToken, mNewestEarthquakeReportImage, mNewestEarthquakeReportImage);
        }
        if (text.equals("最新地震報告") || text.contains("地震報告")) {
            this.replyText(replyToken, mNewestEarthquakeReportText);
        }

        // China media alert
        if (senderId.equals(GROUP_ID_PUBLIC_ISSUES_NO_IDIOTS)) {
            if (text.contains("http") && text.contains("udn.com") && text.contains("news")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"親中媒體警示(聯合報系)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
            else if (text.contains("http") && text.contains("chinatimes.com")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"親中媒體警示(旺旺集團)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
            else if (text.contains("http") && text.contains("new.ctv.com.tw")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"親中媒體警示(中國電視)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
            else if (text.contains("http") && text.contains("ctitv.com.tw")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"親中媒體警示(中天電視)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
            else if (text.contains("http") && text.contains("ccyp.com")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"親中媒體警示(中旺電視)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
            else if (text.contains("http") && text.contains("weixin.qq")) {
                this.replyText(replyToken, EmojiUtils.emojify(":exclamation:")+"中國媒體警示(微信)"+EmojiUtils.emojify(":exclamation:")+"\n----------\n"+text);
            }
        }
        
        // Temp feature
        
        /*if (text.contains("美國總統大選")||text.contains("美國大選")||text.contains("美國總統")) {
            this.replyText(replyToken, getUSPresidentVoie2020());
        }*/

        /*if ((text.contains("Ingress") || text.contains("ingress")) &&
            (text.contains("Twitter") || text.contains("twitter"))) {
            this.replyText(replyToken, getIngressNewestTwitter());
        }*/

        if (text.contains("蛙")) {
            whereIsMyFrog(text, replyToken);
        }

        if (text.equals("悲慘世界")) {
            keywordImage("TragicWorld",replyToken);
        }
        
        if (text.equals("地震")) {
            keywordImage("EarthQuakeSeek",replyToken);
        }

        if (text.equals("幹")||text.equals("操")||text.equals("雞掰")||text.equals("機掰")) {
            keywordImage("IfYouAngry",replyToken);
        }

        if (text.contains("不自殺聲明")||text.contains("GPNUDD")) {
            keywordImage("GPNUDD",replyToken);
        }

        // keyword image control
        if (text.endsWith("閉嘴")||text.endsWith("閉嘴！")||text.endsWith("閉嘴!")) {
            keywordImageControlDisable(text,replyToken);
            return;
        }

        if (text.endsWith("啞巴？")||text.endsWith("啞巴?")) {
            keywordImageControlEnable(text,replyToken);
            return;
        }
        
        if (text.contains("Eg")||text.contains("eg")||text.contains("egef")||text.contains("女流氓")||text.contains("蕭婆")||text.contains("EG")
            ||text.contains("一姊")||text.contains("一姐")||text.contains("婷婷")) {
            if (isEgKeywordEnable) {
                keywordImage("EG",replyToken);
            }
        }
        
        if (text.equals("部囧")) {
            if (isKofatKeywordEnable) {
                keywordImage("kofat",replyToken);
            }
        }
        if (text.contains("姨姨")||text.contains("委員")||text.contains("翠姨")) {
            if (isChuiyiKeywordEnable) {
                keywordImage("Chuiyi",replyToken);
            }
        }
        if (text.contains("凱西")||text.contains("牙醫")) {
            if (isCathyKeywordEnable) {
                keywordImage("FattyCathy",replyToken);
            }
        }
        
        if (text.contains("凱西") && text.contains("多久") && (text.contains("出獄")||text.contains("畢業"))) {
            long timeMillis = getTimeInMillis(2021, 10, 1, 0, 0, 0);
            Calendar current = Calendar.getInstance(TimeZone.getDefault());
            current.setTimeInMillis(System.currentTimeMillis());
     
            Calendar date = Calendar.getInstance(TimeZone.getDefault());
            date.setTimeInMillis(timeMillis);
            long time = date.getTimeInMillis() - current.getTimeInMillis();
            int day = Math.round(time / 1000 / 60 / 60 / 24);
            if (day > 0) {
                this.replyText(replyToken, "還有 " + day + " 天");
            }
            else {
                this.replyText(replyToken, "凱西已經畢業可以自己開診所囉");
            }
        }

        if (text.length() < 8 && (text.contains("ok") && text.contains("好")||
            text.contains("OK") && text.contains("好")||
            text.contains("Ok") && text.contains("好")||
            text.contains("ＯＫ") && text.contains("好")||
            text.contains("幹妳娘")||text.contains("幹您娘")||text.contains("幹你娘"))) {
            replyOkFineImage(replyToken);
        }

        if (text.contains("鮭魚") || text.contains("旗魚")) {
            replyGiveSalmonNoSwordFishImage(replyToken);
        }

        if (text.startsWith("霸凌模式:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            initBullyMode(text, replyToken);
        }

        if (text.startsWith("霸凌不好")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            interruptBullyMode(replyToken);
        }

        if (text.equals("開始猜拳")) {
            startRPS(userId, senderId, replyToken);
        }

        if (text.equals("結束猜拳")) {
            stopRPS(userId, senderId, replyToken);
        }

        if (text.equals("參加猜拳")) {
            joinRPS(userId, senderId, replyToken);
        }

        if (text.equals("開始隨機排序")) {
            startRandomSort(userId, senderId, replyToken);
        }

        if (text.equals("結束隨機排序")) {
            stopRandomSort(userId, senderId, replyToken);
        }

        if (text.equals("參加隨機排序")) {
            joinRandomSort(userId, senderId, replyToken);
        }

        if (text.equals("全部隨機排")) {
            sortAllRandomSort(userId, senderId, replyToken);
        }

        if (text.equals("隨機排序抽")) {
            sortOneRandomSort(userId, senderId, replyToken);
        }

        if (text.startsWith("Md")||text.startsWith("MD")||text.startsWith("ＭＤ")&&
            (text.endsWith("地圖")||text.endsWith("地圖？")||text.endsWith("地圖?"))) {
            replyMdMap(replyToken);
        }

        if ((text.startsWith("Pg")||text.startsWith("PG")||text.startsWith("ＰＧ"))&&
            (text.endsWith("怎麼解")||text.endsWith("怎麼解？")||text.endsWith("怎麼解?"))) {
            howPgSolveMdMap(replyToken);
        }

        if (text.equals("?")||text.equals("？")) {
            replyQuestionMarkImage(replyToken);
        }

        if (text.startsWith("許願:")) {
            makeWish(senderId, userId, text, replyToken);
        }

        if (text.startsWith("投稿:")) {
            makeSubmission(senderId, userId, text, replyToken);
        }

        if (text.startsWith("隨機取圖:")) {
            processRandomeGetImage(replyToken, text);
        }

        if (text.startsWith("年號:")) {
            processLinHoImage(replyToken, text);
        }

        if (text.equals("功能指令集")) {
            this.replyText(replyToken, getFeatureListString(userId, false));
        }
        else if (text.endsWith("功能指令集") && (text.startsWith("Pg") || text.startsWith("PG") || text.startsWith("ＰＧ") )) {
            this.replyText(replyToken, getFeatureListString(userId, true));
        }

        if (text.length() > 0) {
            bullyModeTrigger(replyToken);
        }

    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws IOException {
        PgLog.info("Got postBack event: {}" + event);
        String replyToken = event.getReplyToken();
        String data = event.getPostbackContent().getData();
        String senderId = event.getSource().getSenderId();
        String userId = event.getSource().getUserId();
        if (mBeautyFightingProcessor.processBeautyFightingPostback(replyToken, senderId, userId, data)) {
            return;
        }
        if (data.startsWith(AvWikiInfo.POSTBACK_PREFIX)) {
            this.replyText(replyToken, data.replace(AvWikiInfo.POSTBACK_PREFIX, ""));
            return;
        }
        switch (data) {
            case "more:1": {
                this.replyText(replyToken, "Coming soon!");
                break;
            }
            default:
                this.replyText(replyToken, "Got postback event : " + event.getPostbackContent().getData());
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        LineMessagePrimitive.reply(replyToken, new TextMessage(message));
    }

    private void replyImage(@NonNull String replyToken, @NonNull String original, @NonNull String preview) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        LineMessagePrimitive.reply(replyToken, new ImageMessage(URI.create(original), URI.create(preview)));
    }
    
    private void replyImage(@NonNull String replyToken, @NonNull URI original, @NonNull URI preview) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        LineMessagePrimitive.reply(replyToken, new ImageMessage(original, preview));
    }

    private ImageCarouselColumn getImageCarouselColumn(String imageUrl, String label, String url) {
        //return new ImageCarouselColumn(URI.create(imageUrl), new URIAction(label, URI.create(url), new AltUri(URI.create(url))));
        label = label.replace(" ", "").replace("　", "").trim();
        if (label.length()>12) {
            // Image carouse column label length limit is 12.
            label = label.substring(0, 12);
        }
        return new ImageCarouselColumn(URI.create(imageUrl), new URIAction(label, URI.create(url), new AltUri(URI.create(url))));
    }
    
    private ImageCarouselColumn getBeautyFightingImagePostbackColumn(String imageUrl, String info) {
        //return new ImageCarouselColumn(URI.create(imageUrl), new URIAction(label, URI.create(url), new AltUri(URI.create(url))));
        String label = "";
        String data = "";
        if (info.equals(BeautyFightingInfo.LEFT)) {
            label = EmojiUtils.emojify(":point_left:");
            data = BeautyFightingProcessor.PREFIX+BeautyFightingProcessor.VOTE+BeautyFightingInfo.LEFT;
        }
        else if (info.equals(BeautyFightingInfo.RIGHT)) {
            label = EmojiUtils.emojify(":point_right:");
            data = BeautyFightingProcessor.PREFIX+BeautyFightingProcessor.VOTE+BeautyFightingInfo.RIGHT;
        }
        else {
            return null;
        }
        PostbackAction action =  new PostbackAction(label, data);
        return new ImageCarouselColumn(URI.create(imageUrl), action);
    }

    private ImageCarouselColumn getImagePostbackColumn(String imageUrl, String title, String prefix, String info) {
        //return new ImageCarouselColumn(URI.create(imageUrl), new URIAction(label, URI.create(url), new AltUri(URI.create(url))));
        String label = title;
        String data = prefix+info;
        PostbackAction action =  new PostbackAction(label, data);
        return new ImageCarouselColumn(URI.create(imageUrl), action);
    }


/*
This code is public domain: you are free to use, link and/or modify it in any way you want, for all purposes including commercial applications.
*/
    public static class WebClientDevWrapper {

        public static HttpClient wrapClient(HttpClient base) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {

                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                X509HostnameVerifier verifier = new X509HostnameVerifier() {

                    @Override
                    public void verify(String string, SSLSocket ssls) throws IOException {
                    }

                    @Override
                    public void verify(String string, X509Certificate xc) throws SSLException {
                    }

                    @Override
                    public void verify(String string, String[] strings, String[] strings1) throws SSLException {
                    }

                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);
                org.apache.http.conn.ssl.SSLSocketFactory ssf = new org.apache.http.conn.ssl.SSLSocketFactory(ctx);
                ssf.setHostnameVerifier(verifier);
                ClientConnectionManager ccm = base.getConnectionManager();
                SchemeRegistry sr = ccm.getSchemeRegistry();
                sr.register(new Scheme("https", ssf, 443));
                return new DefaultHttpClient(ccm, base.getParams());
            } catch (Exception ex) {
                PgLog.error("Error in wrapClient : " + ex.toString());
                return null;
            }
        }
    }

    private boolean weatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        PgLog.info("weatherResult: " + text);
        boolean isHaveResult = true;
        try {
            if (text.length() <= 3) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                String strResult;
                switch (text) {
                    case "台北市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_63.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新北市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_65.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "桃園市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_68.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台南市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_67.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台中市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_66.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "高雄市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_64.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "基隆市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10017.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新竹市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10018.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "新竹縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10004.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "苗栗縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10005.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "彰化縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10007.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "南投縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10008.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "雲林縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10009.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "嘉義市": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10020.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "嘉義縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10010.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "屏東縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10013.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "宜蘭縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10002.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "花蓮縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10015.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "台東縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10014.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    case "澎湖縣": {
                        HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/Data/W50_10016.txt");
                        CloseableHttpResponse response = httpClient.execute(httpget);
                        HttpEntity httpEntity = response.getEntity();
                        strResult = EntityUtils.toString(httpEntity, "utf-8");
                        break;
                    }
                    default: {
                        strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
                        PgLog.info("weatherResult default: " + text);
                        return false;
                    }
                }
                strResult = strResult.replace("<BR><BR>", "\n");
                strResult = strResult.replaceAll("<[^<>]*?>", "");
                this.replyText(replyToken, strResult);

            } else {
                PgLog.info("weatherResult length: " + text.length());
                return false;
            }
        } catch (IOException e) {
            throw e;
        }
        return isHaveResult;
    }

    private boolean worldWeatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        PgLog.info(text);

        HttpGet httpget = new HttpGet("https://www.cwb.gov.tw/V7/forecast/world/world_aa.htm");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpget);
        HttpEntity httpEntity = response.getEntity();
        String strResult = EntityUtils.toString(httpEntity, "utf-8");

        String reportTime = "";
        String availableTime = "";

        if (!strResult.contains(text)) {
            strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
            PgLog.info("worldWeatherResult default: " + text);
            this.replyText(replyToken, strResult);
            return false;
        }
        else {

            reportTime = strResult.substring(strResult.indexOf("發布時間:"),strResult.indexOf("<br"));
            availableTime =  strResult.substring(strResult.indexOf("有效時間:"),strResult.indexOf("</p>"));

            String temp = "<td class=\"laf\">" + text;
            strResult = strResult.substring(strResult.indexOf(temp), strResult.length());
            strResult = strResult.substring(0,strResult.indexOf("</tr>"));
        }

        

        String locationName = text;
        String locationNameEnglish = strResult.substring(strResult.indexOf("name=\"")+6, strResult.indexOf("\" id="));

        strResult = strResult.substring(strResult.indexOf("earea")+14, strResult.length());

        String weather = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

        strResult = strResult.substring(strResult.indexOf("</td>")+4, strResult.length());

        String temperature = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

        strResult = strResult.substring(strResult.indexOf("</td>")+4, strResult.length());

        String temperatureMonthLow = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

        strResult = strResult.substring(strResult.indexOf("</td>")+4, strResult.length());

        String temperatureMonthHigh = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

        strResult = locationName + "(" + locationNameEnglish + ")" + 
                    "\n天氣: " + weather + 
                    "\n溫度: " + temperature + "℃" +
                    "\n\n月平均溫度" + 
                    "\n最高: " + temperatureMonthHigh + "℃" +
                    "\n最低: " + temperatureMonthLow + "℃" +
                    "\n" + reportTime + 
                    "\n" + availableTime;

        this.replyText(replyToken, strResult);
        return true;

    }

    private void weatherResult2(String text, String replyToken) throws IOException {
        text = text.replace("氣象", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        PgLog.info(text);
        try {
            if (text.length() <= 3) {
                String strResult;
                String url ="";
                switch (text) {
                    case "台北市": {
                        url="Taipei_City.htm";
                        break;
                    }
                    case "新北市": {
                        url="New_Taipei_City.htm";
                        break;
                    }
                    case "桃園市": {
                        url="Taoyuan_City.htm";
                        break;
                    }
                    case "台南市": {
                        url="Tainan_City.htm";
                        break;
                    }
                    case "台中市": {
                        url="Taichung_City.htm";
                        break;
                    }
                    case "高雄市": {
                        url="Kaohsiung_City.htm";
                        break;
                    }
                    case "基隆市": {
                        url="Keelung_City.htm";
                        break;
                    }
                    case "新竹市": {
                        url="Hsinchu_City.htm";
                        break;
                    }
                    case "新竹縣": {
                        url="Hsinchu_County.htm";
                        break;
                    }
                    case "苗栗縣": {
                        url="Miaoli_County.htm";
                        break;
                    }
                    case "彰化縣": {
                        url="Changhua_County.htm";
                        break;
                    }
                    case "南投縣": {
                        url="Nantou_County.htm";
                        break;
                    }
                    case "雲林縣": {
                        url="Chiayi_City.htm";
                        break;
                    }
                    case "嘉義市": {
                        url="Chiayi_City.htm";
                        break;
                    }
                    case "嘉義縣": {
                        url="Chiayi_County.htm";
                        break;
                    }
                    case "屏東縣": {
                        url="Pingtung_County.htm";
                        break;
                    }
                    case "宜蘭縣": {
                        url="Yilan_County.htm";
                        break;
                    }
                    case "花蓮縣": {
                        url="Hualien_County.htm";
                        break;
                    }
                    case "台東縣": {
                        url="Taitung_County.htm";
                        break;
                    }
                    case "澎湖縣": {
                        url="Penghu_County.htm";
                        break;
                    }
                    default:
                        text="";

                }
                CloseableHttpClient httpClient = HttpClients.createDefault();
                HttpGet httpget = new HttpGet("http://www.cwb.gov.tw/V7/forecast/taiwan/"+url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();
                strResult = EntityUtils.toString(httpEntity, "utf-8");
                if(text.equals("")){
                    strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
                    this.replyText(replyToken, strResult);
                }else{
                    String dateTime = "";
                    String temperature = "";
                    String comfort = "";
                    String weatherConditions = "";
                    String rainfallRate = "";
                    strResult = strResult.substring(
                            strResult.indexOf("<h3 class=\"CenterTitle\">今明預報<span class=\"Issued\">"), strResult.length());
                    strResult = strResult.substring(0,strResult.indexOf("</tr><tr>"));
                    Pattern pattern = Pattern.compile("<th scope=\"row\">.*?</th>");
                    Matcher matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        dateTime = matcher.group().replaceAll("<[^>]*>", "");
                    }
                    pattern = Pattern.compile("<td>.*?~.*?</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        temperature = matcher.group().replaceAll("<[^>]*>","");
                    }
                    pattern = Pattern.compile("title=\".*?\"");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        weatherConditions = matcher.group().replace("title=\"", "").replace("\"", "").replaceAll("[\\s]{0,}","");
                    }
                    pattern = Pattern.compile("<img.*?</td>[\\s]{0,}<td>.*?</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        comfort = matcher.group().replaceAll("<[^>]*>", "").replaceAll("[\\s]{0,}","");
                    }
                    pattern = Pattern.compile("<td>[\\d]{0,3} %</td>");
                    matcher = pattern.matcher(strResult);
                    while(matcher.find()){
                        rainfallRate = matcher.group().replaceAll("<[^>]*>", "");
                    }
                    strResult = text+"氣溫 : "+temperature+"\n"+dateTime+"\n天氣狀況 : "+weatherConditions+"\n舒適度 : "+comfort+"\n降雨率 : "+rainfallRate;
                    this.replyText(replyToken, strResult);
                }

            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void taiwanoil(String text, String replyToken) throws IOException {
        try {
            String strResult = "";
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://taiwanoil.org/";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");
            strResult = strResult.substring(strResult.indexOf("<td valign=top align=center>"), strResult.length());
            strResult = strResult.substring(0, strResult.indexOf("</table><br><br><br>"));
            String[] sp = strResult.split("預測下周價格");
            String title = sp[0].replaceAll(".*?<table class=\"topmenu2\">", "").replaceAll(
                    "<div align=center>[\\s]{0,}.*", "").replace("&nbsp;", "").replaceAll("<[^>]*>", "").replaceAll(
                    "\n\t\n\n", "").replaceAll("\n\n", "");
            String content = sp[1].replaceAll("<td style='text-align:right;'>[\\d]{4}/[\\d]{2}/[\\d]{2}</td>", "")
                                  .replaceAll("<td style='text-align:right;'>[\\d]{1,2}\\.[\\d]{1,2}</td></tr>", "")
                                  .replaceAll(
                                          "<td style='text-align:right;'><font color=#00bb11>(\\+|\\-)[\\d]{1,}\\.[\\d]{1,}\\%",
                                          "").replaceAll("</td></font></td>",
                                                         " > ").replaceAll("</font></td>", "\n").replace(
                            "</td></tr>", "").replaceAll("</td>", " : ").replaceAll("<[^>]*>", "");


            strResult = title + "供應商:今日油價 > 預測下周漲跌\n" + content;
            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            throw e;
        }
    }

    private void star(String text, String replyToken) throws IOException {
        text = text.replace("座", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);
        try {
            if (text.length() == 2) {
                String strResult;
                String url ="";
                switch (text) {
                    case "牡羊": {
                        url="1";
                        break;
                    }
                    case "金牛": {
                        url="2";
                        break;
                    }
                    case "雙子": {
                        url="3";
                        break;
                    }
                    case "巨蟹": {
                        url="4";
                        break;
                    }
                    case "獅子": {
                        url="5";
                        break;
                    }
                    case "處女": {
                        url="6";
                        break;
                    }
                    case "天秤": {
                        url="7";
                        break;
                    }
                    case "天蠍": {
                        url="8";
                        break;
                    }
                    case "射手": {
                        url="9";
                        break;
                    }
                    case "魔羯": {
                        url="10";
                        break;
                    }
                    case "水瓶": {
                        url="11";
                        break;
                    }
                    case "雙魚": {
                        url="12";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n09487 沒有" + text + "這個星座...";
                    this.replyText(replyToken, strResult);
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    url = "http://tw.xingbar.com/cgi-bin/v5starfate2?fate=1&type=" + url;
                    PgLog.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "big5");
                    strResult = strResult.substring(strResult.indexOf("<div id=\"date\">"), strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("</table><div class=\"google\">"));
                    strResult = strResult.replaceAll("訂閱</a></div></td>", "");
                    strResult = strResult.replaceAll("<[^>]*>", "");
                    strResult = strResult.replaceAll("[\\s]{2,}", "\n");
//                    strResult = strResult.replace("心情：", "(sun)心情：");
//                    strResult = strResult.replace("愛情：", "(2 hearts)愛情：");
//                    strResult = strResult.replace("財運：", "(purse)財運：");
//                    strResult = strResult.replace("工作：", "(bag)工作：");

                    strResult = strResult.replace("心情：", "◎心情：");
                    strResult = strResult.replace("愛情：", "◎愛情：");
                    strResult = strResult.replace("財運：", "◎財運：");
                    strResult = strResult.replace("工作：", "◎工作：");
                    if(url.endsWith("type=1")){
                        this.replyText(replyToken, "最棒的星座 " + text + "座 " + strResult);
                    }else{
                        this.replyText(replyToken, "最廢的星座之一 " + text + "座 " + strResult);
                    }

                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void dailyHoroscope(String text, String replyToken) throws IOException {
        text = text.replace("座運勢", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);
        String target = "";
        try {
            if (text.length() == 2) {
                String strResult;
                String url ="";
                switch (text) {
                    case "牡羊": 
                    case "白羊": {
                        target="Aries";
                        break;
                    }
                    case "金牛": {
                        target="Taurus";
                        break;
                    }
                    case "雙子": {
                        target="Gemini";
                        break;
                    }
                    case "巨蟹": {
                        target="Cancer";
                        break;
                    }
                    case "獅子": {
                        target="Leo";
                        break;
                    }
                    case "處女": {
                        target="Virgo";
                        break;
                    }
                    case "天秤": {
                        target="Libra";
                        break;
                    }
                    case "天蠍": {
                        target="Scorpio";
                        break;
                    }
                    case "射手": {
                        target="Sagittarius";
                        break;
                    }
                    case "魔羯": {
                        target="Capricorn";
                        break;
                    }
                    case "水瓶": {
                        target="Aquarius";
                        break;
                    }
                    case "雙魚": {
                        target="Pisces";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n09487 沒有" + text + "這個星座...";
                    this.replyText(replyToken, strResult);
                }else{

                    // Get daily website address first.
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    url = "http://www.daily-zodiac.com/mobile/zodiac/" + target;
                    PgLog.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "big5");
                    strResult = strResult.substring(strResult.indexOf("今日運勢</li>")+9, strResult.length());
                    // Then get daily date
                    String date = strResult.substring(strResult.indexOf("<li>")+4, strResult.indexOf("</li>"));
                    // Then get daily sentense
                    strResult = strResult.substring(strResult.indexOf("<article>")+9, strResult.indexOf("</article>"));
                    strResult = strResult.trim();
                    
                    PgLog.info("strResult: " + strResult);

                    
                    
                    this.replyText(replyToken, date +"\n唐綺陽占星幫 每日運勢 " + text + "座\n" + strResult);

                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void stock(String text, String replyToken) {
        try {
            text = text.replace("@","").replace("?", "").replace("？","");
            String[] otcs = StockList.otcList;
            HashMap<String, String> otcNoMap = new HashMap<>();
            HashMap<String, String> otcNameMap = new HashMap<>();
            for (String otc : otcs) {
                String[] s = otc.split("=");
                otcNoMap.put(s[0], s[1]);
                otcNameMap.put(s[1], s[0]);
            }

            String[] tses = StockList.tseList;
            HashMap<String, String> tseNoMap = new HashMap<>();
            HashMap<String, String> tseNameMap = new HashMap<>();
            for (String tse : tses) {
                String[] s = tse.split("=");
                tseNoMap.put(s[0], s[1]);
                tseNameMap.put(s[1], s[0]);
            }

            String companyType = "";
            Pattern pattern = Pattern.compile("[\\d]{3,}");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {   //如果是數字
                if (otcNoMap.get(text) != null) {
                    companyType = "otc";
                } else {
                    companyType = "tse";
                }
            } else {    //非數字
                if (otcNameMap.get(text) != null) {
                    companyType = "otc";
                    text = otcNameMap.get(text);
                } else {
                    companyType = "tse";
                    text = tseNameMap.get(text);
                }
            }

            String strResult;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="http://mis.twse.com.tw/stock/index.jsp";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control","max-age=0");
            httpget.setHeader("Connection","keep-alive");
            httpget.setHeader("Host","mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + text + ".tw&_=" +
                  Instant.now().toEpochMilli();
            PgLog.info(url);
            httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            strResult = "";

            Gson gson = new GsonBuilder().create();
            StockData stockData = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"), StockData.class);
            for(MsgArray msgArray:stockData.getMsgArray()){
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                Double nowPrice = Double.valueOf(msgArray.getZ());
                Double yesterday = Double.valueOf(msgArray.getY());
                Double diff = nowPrice - yesterday;
                String change = "";
                String range = "";
                if (diff == 0) {
                    change = " " + diff;
                    range = " " + "-";
                } else if (diff > 0) {
                    change = " +" + decimalFormat.format(diff);
                    if (nowPrice == Double.parseDouble(msgArray.getU())) {
                        range = EmojiUtils.emojify(":heart:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }else{
                        range = EmojiUtils.emojify(":chart_with_upwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }
                } else {
                    change = " -" + decimalFormat.format(diff*(-1));
                    if (nowPrice == Double.parseDouble(msgArray.getW())) {
                        range = EmojiUtils.emojify(":green_heart:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }else{
                        range = EmojiUtils.emojify(":chart_with_downwards_trend:") + decimalFormat.format((diff / yesterday)*100) + "%";
                    }
                }
                //開盤 : "+msgArray.getO()+"\n昨收 : "+msgArray.getY()+"
                strResult =msgArray.getC() + " " + msgArray.getN() + " " + change + range + " \n現價 : " + msgArray.getZ() +
                        " \n成量 : " + msgArray.getV() + "\n更新 : " + msgArray.getT();
            }
            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stockMore(String text, String replyToken) {
        try {
            text = text.replace("@","").replace("?", "").replace("？","").replace("#","");
            String[] otcs = StockList.otcList;
            HashMap<String, String> otcNoMap = new HashMap<>();
            HashMap<String, String> otcNameMap = new HashMap<>();
            for (String otc : otcs) {
                String[] s = otc.split("=");
                otcNoMap.put(s[0], s[1]);
                otcNameMap.put(s[1], s[0]);
            }

            String[] tses = StockList.tseList;
            HashMap<String, String> tseNoMap = new HashMap<>();
            HashMap<String, String> tseNameMap = new HashMap<>();
            for (String tse : tses) {
                String[] s = tse.split("=");
                tseNoMap.put(s[0], s[1]);
                tseNameMap.put(s[1], s[0]);
            }

            System.out.println(text);
            Pattern pattern = Pattern.compile("[\\d]{3,}");
            Matcher matcher = pattern.matcher(text);
            String stockName = "";
            if (matcher.find()) {
                if (otcNoMap.get(text) != null) {
                    stockName = otcNoMap.get(text);
                } else {
                    stockName = tseNoMap.get(text);
                }
            } else {
                if (otcNameMap.get(text) != null) {
                    stockName = text;
                    text = otcNameMap.get(text);
                } else {
                    stockName = text;
                    text = tseNameMap.get(text);
                }
            }

            pattern = Pattern.compile("[\\d]{3,}");
            matcher = pattern.matcher(text);
            if (!matcher.find()) {
                return;
            }

            /*DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            defaultHttpClient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(defaultHttpClient);*/
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://tw.screener.finance.yahoo.net/screener/ws?f=j&ShowID="+text;
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            //httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpget.setHeader("Accept-Encoding","gzip, deflate, br");
            //httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Accept-Language", "en-US,en;q=0.9");
            httpget.setHeader("Cache-Control", "max-age=0");
            //httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("Sec-Fetch-User", "?1");
            httpget.setHeader("Sec-Fetch-Dest", "document");
            httpget.setHeader("dnt", "1");
            httpget.setHeader("sec-fetch-mode", "navigate");
            httpget.setHeader("upgrade-insecure-requests", "1");
            httpget.setHeader("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"90\", \"Google Chrome\";v=\"90\"");
            /*httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");*/
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            //CloseableHttpResponse response = defaultHttpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            String strResult = "";
            PgLog.info("PiggyCheck: " + EntityUtils.toString(httpEntity, "utf-8"));
            Gson gson = new GsonBuilder().create();
            Screener screener = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"),Screener.class);


//            url="https://news.money-link.com.tw/yahoo/0061_"+text+".html";
//            httpget = new HttpGet(url);
//            PgLog.info(url);
//            httpget.setHeader("Accept",
//                              "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//            httpget.setHeader("Accept-Encoding","gzip, deflate, sdch, br");
//            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
//            httpget.setHeader("Cache-Control", "max-age=0");
//            httpget.setHeader("Connection", "keep-alive");
//            httpget.setHeader("Host", "news.money-link.com.tw");
//            httpget.setHeader("Upgrade-Insecure-Requests", "1");
//            httpget.setHeader("User-Agent",
//                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
//            response = defaultHttpClient.execute(httpget);
//            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
//            httpEntity = response.getEntity();
//            InputStream inputStream = httpEntity.getContent();
//            Header[] headers = response.getAllHeaders();
//            for(Header header:headers){
//                if(header.getName().contains("Content-Encoding")){
//                    System.out.println(header.getName()+" "+header.getValue());
//                    if(header.getValue().contains("gzip")){
//                        inputStream = new GZIPInputStream(inputStream);
//                    }
//                }
//            }
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
//            String newLine;
//            StringBuilder stringBuilder = new StringBuilder();
//            while ((newLine = bufferedReader.readLine()) != null) {
//                stringBuilder.append(newLine);
//            }
//            String strContent = stringBuilder.toString();
//
//            //切掉不要區塊
//            if (strContent.contains("<tbody>")) {
//                strContent = strContent.substring(strContent.indexOf("<tbody>"), strContent.length());
//            }
//
//            //基本評估
//            String basicAssessment = "";
//            pattern = Pattern.compile("<strong>.*?</strong>.*?</td>");
//            matcher = pattern.matcher(strContent);
//            while (matcher.find()) {
//                String s = matcher.group();
//                basicAssessment = basicAssessment + s;
//                strContent = strContent.replace(s,"");
//            }
//            basicAssessment = "\n" + basicAssessment.replaceAll("</td>", "\n").replaceAll("<[^>]*>", "").replace("交易所","");
//
//            //除權息
//            String XDInfo = "";
//            if(strContent.contains("近1年殖利率")){
//                XDInfo = strContent.substring(strContent.indexOf("除"), strContent.indexOf("近1年殖利率"));
//                strContent = strContent.replace(XDInfo, "");
//            }
//            XDInfo = "\n" + XDInfo.replaceAll("</td></tr>", "\n").replaceAll("<[^>]*>", "");
//
//            //殖利率
//            String yield = "\n";
//            pattern = Pattern.compile("近.*?</td>.*?</td>");
//            matcher = pattern.matcher(strContent);
//            while (matcher.find()) {
//                String s = matcher.group();
//                yield = yield + s;
//                strContent = strContent.replace(s,"");
//            }
//            yield = yield.replaceAll("</td>近","</td>\n近").replaceAll("<[^>]*>", "").replaceAll(" ","").replace("為銀行","");
//
//            //均線
//            String movingAVG = "\n"+strContent.replaceAll("</td></tr>", "\n").replaceAll("<[^>]*>", "").replaceAll(" ","");


            Item item = screener.getItems().get(0);
            strResult = "◎" + stockName + " " + text + "\n";
            strResult = strResult + "收盤："+item.getVFLD_CLOSE() + " 漲跌：" + item.getVFLD_UP_DN() + " 漲跌幅：" + item.getVFLD_UP_DN_RATE() + "%\n";
            strResult = strResult + "近52周  最高："+item.getV52_WEEK_HIGH_PRICE()+" 最低："+item.getV52_WEEK_LOW_PRICE() + "\n";
            strResult = strResult + item.getVGET_MONEY_DATE()+" 營收："+item.getVGET_MONEY() + "\n";
            strResult = strResult + item.getVFLD_PRCQ_YMD() +" 毛利率："+item.getVFLD_PROFIT() + "\n";
            strResult = strResult + item.getVFLD_PRCQ_YMD() +" 每股盈餘（EPS)："+item.getVFLD_EPS() + "\n";
            strResult = strResult + item.getVFLD_PRCQ_YMD() +" 股東權益報酬率(ROE)：" + item.getVFLD_ROE() + "\n";
            strResult = strResult + "本益比(PER)："+ item.getVFLD_PER() + "\n";
            strResult = strResult + "每股淨值(PBR)："+item.getVFLD_PBR() + "\n";
            strResult = strResult + "K9值："+item.getVFLD_K9_UPDNRATE() + "\n";
            strResult = strResult + "D9值："+item.getVFLD_D9_UPDNRATE() + "\n";
            strResult = strResult + "MACD："+item.getVMACD() + "\n";
//            strResult = strResult + movingAVG;
//            strResult = strResult + XDInfo;
//            strResult = strResult + yield;
//            strResult = strResult + basicAssessment;
            //this.replyText(replyToken, EmojiUtils.emojify(strResult));
            this.replyText(replyToken, strResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aqiResult(String text, String replyToken) throws IOException {
        text = text.replace("空氣", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);
        try {
            if (text.length() <= 3) {
                String strResult = "";
                String areakey ="";
                String sitekey ="";
                switch (text) {
                    case "北部": {
                        areakey="north";
                        break;
                    }
                    case "竹苗": {
                        areakey="chu-miao";
                        break;
                    }
                    case "中部": {
                        areakey="central";
                        break;
                    }
                    case "雲嘉南": {
                        areakey="yun-chia-nan";
                        break;
                    }
                    case "高屏": {
                        areakey="kaoping";
                        break;
                    }
                    case "花東": {
                        areakey="hua-tung";
                        break;
                    }
                    case "宜蘭": {
                        areakey="yilan";
                        break;
                    }
                    case "外島": {
                        areakey="island";
                        break;
                    }
                    default: {
                        sitekey=text;
                    }

                }
                if(text.equals("")){
                    // Deprecate
                    strResult = "義大利?維大力? \n請輸入 這些地區：\n北部 竹苗 中部 \n雲嘉南 高屏 花東 \n宜蘭 外島";
                    this.replyText(replyToken, strResult);

                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    String url="http://taqm.epa.gov.tw/taqm/aqs.ashx?lang=tw&act=aqi-epa";
                    PgLog.info(url);
                    HttpGet httpget = new HttpGet(url);
                    httpget.setHeader("Host","taqm.epa.gov.tw");
                    httpget.setHeader("Connection","keep-alive");
                    httpget.setHeader("Accept","*/*");
                    httpget.setHeader("X-Requested-With","XMLHttpRequest");
                    httpget.setHeader("User-Agent",
                                      "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                    httpget.setHeader("Referer","http://taqm.epa.gov.tw/taqm/aqi-map.aspx");
                    httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
                    httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");

                    CloseableHttpResponse response = httpClient.execute(httpget);
                    HttpEntity httpEntity = response.getEntity();
                    String pageContent =  EntityUtils.toString(httpEntity, "big5").toLowerCase();
                    Gson gson = new GsonBuilder().create();
                    AqiResult aqiResult = gson.fromJson(pageContent, AqiResult.class);
                    List<Datum> areaData = new ArrayList<>();
                    if (!areakey.equals("")) {
                        for(Datum datums:aqiResult.getData()){
                            if(datums.getAreakey().equals(areakey)){
                                areaData.add(datums);
                            }
                        }
                        for (Datum datums : areaData) {
                            String aqiStyle = datums.getAQI();
                            if (Objects.equals(aqiStyle, "")) {
                                aqiStyle = "999";
                            }
                            PgLog.info(datums.getSitename()+" "+datums.getAQI());
                            if (Integer.parseInt(aqiStyle) <= 50) {
                                aqiStyle = ":blush: " +"良好";
                            } else if (Integer.parseInt(aqiStyle) >= 51 && Integer.parseInt(aqiStyle) <= 100) {
                                aqiStyle = ":no_mouth: " +"普通";
                            } else if (Integer.parseInt(aqiStyle) >= 101 && Integer.parseInt(aqiStyle) <= 150) {
                                aqiStyle = ":triumph: " +"對敏感族群不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 151 && Integer.parseInt(aqiStyle) <= 200) {
                                aqiStyle = ":mask: " +"對所有族群不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 201 && Integer.parseInt(aqiStyle) <= 300) {
                                aqiStyle = ":scream: " +"非常不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 301 && Integer.parseInt(aqiStyle) <= 500) {
                                aqiStyle = ":imp: " +"危害";
                            } else if (Integer.parseInt(aqiStyle) >= 500) {
                                aqiStyle = "監測站資料異常";
                            }
                            strResult = strResult + datums.getSitename() + " AQI : " + datums.getAQI() + aqiStyle+"\n";
                        }    
                    }
                    else {
                        for(Datum datums:aqiResult.getData()){
                            if(datums.getSitename().equals(sitekey)){
                                areaData.add(datums);
                            }
                        }
                        for (Datum datums : areaData) {
                            String aqiStyle = datums.getAQI();
                            if (Objects.equals(aqiStyle, "")) {
                                aqiStyle = "999";
                            }
                            PgLog.info(datums.getSitename()+" "+datums.getAQI());
                            if (Integer.parseInt(aqiStyle) <= 50) {
                                aqiStyle = ":blush: " +"良好";
                            } else if (Integer.parseInt(aqiStyle) >= 51 && Integer.parseInt(aqiStyle) <= 100) {
                                aqiStyle = ":no_mouth: " +"普通";
                            } else if (Integer.parseInt(aqiStyle) >= 101 && Integer.parseInt(aqiStyle) <= 150) {
                                aqiStyle = ":triumph: " +"對敏感族群不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 151 && Integer.parseInt(aqiStyle) <= 200) {
                                aqiStyle = ":mask: " +"對所有族群不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 201 && Integer.parseInt(aqiStyle) <= 300) {
                                aqiStyle = ":scream: " +"非常不健康";
                            } else if (Integer.parseInt(aqiStyle) >= 301 && Integer.parseInt(aqiStyle) <= 500) {
                                aqiStyle = ":imp: " +"危害";
                            } else if (Integer.parseInt(aqiStyle) >= 500) {
                                aqiStyle = "監測站資料異常";
                            }
                            strResult = strResult + datums.getSitename() + " AQI : " + datums.getAQI() + aqiStyle+"\n";
                        }    
                    }
                    
                    if (!strResult.equals("")) {
                        this.replyText(replyToken, EmojiUtils.emojify(strResult));
                    }
                    else {
                        strResult = "義大利?維大力? \n請輸入 這些地區：\n北部 竹苗 中部 \n雲嘉南 高屏 花東 \n宜蘭 外島";
                        this.replyText(replyToken, strResult);
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private void rate(String text, String replyToken) throws IOException {
        text = text.replace("匯率", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);

        String strResult = "";
        String country ="";
        try {
            if (text.length() <= 3) {
                switch (text) {
                    case "美金": {
                        country="USD";
                        break;
                    }
                    case "日圓": {
                        country="JPY";
                        break;
                    }
                    case "人民幣": {
                        country="CNY";
                        break;
                    }
                    case "歐元": {
                        country="EUR";
                        break;
                    }
                    case "港幣": {
                        country="HKD";
                        break;
                    }
                    case "英鎊": {
                        country="GBP";
                        break;
                    }
                    case "韓元": {
                        country="KRW";
                        break;
                    }
                    case "越南盾": {
                        country="VND";
                        break;
                    }
                    case "澳幣": {
                        country="AUD";
                        break;
                    }
                    case "泰銖": {
                        country="THB";
                        break;
                    }
                    case "印尼盾": {
                        country="IDR";
                        break;
                    }
                    case "法郎": {
                        country="CHF";
                        break;
                    }
                    case "披索": {
                        country="PHP";
                        break;
                    }
                    case "新幣": {
                        country="SGD";
                        break;
                    }
                    case "台幣": {
                        country="TWD";
                        break;
                    }
                    case "鮭魚": {
                        country="Salmon";
                        break;
                    }
                    default:
                        text="";

                }
                if(country.equals("")){
                    strResult = "義大利?維大力? \n請輸入 這些幣別：\n美金 日圓 人民幣 歐元 \n港幣 英鎊 韓元 越南盾\n澳幣 泰銖 印尼盾 法郎\n披索 新幣";
                    this.replyText(replyToken, strResult);
                } else if (country.equals("TWD")){
                    this.replyText(replyToken, "現鈔賣出去巷口便利商店");
                } else if (country.equals("Salmon")){
                    this.replyText(replyToken, "現鈔買入去爭鮮林森北店");
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    String url="https://www.findrate.tw/"+country+"/?type="+country+"&order=in1";
                    PgLog.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "utf-8");
                    strResult = strResult.substring(strResult.indexOf("<td>台幣換")+4, strResult.indexOf("</table>")); // cut table

                    strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length()); // move to first bank title
                    String result = "" + text + "買賣推薦 轉自 findrate.tw\n";
                    result += ":dollar:要買現鈔去 ";
                    result += strResult.substring(0, strResult.indexOf("</td>")) + " :moneybag:\n"; // get buying bank
                    strResult = strResult.substring(strResult.indexOf("\">")+2, strResult.length()); // move to before buy rate
                    result += strResult.substring(0, strResult.indexOf("</td>")) + "\n"; // get buying rate

                    result += ":money_with_wings:要賣現鈔去 ";
                    strResult = strResult.substring(strResult.indexOf("換台幣</td>")+8, strResult.length()); //move to selling bank start
                    result += strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>")) + " :moneybag:\n"; // get selling bank name
                    strResult = strResult.substring(strResult.indexOf("\">")+2, strResult.length()); // move to before buy rate
                    result += strResult.substring(0, strResult.indexOf("</td>")); // get selling rate

                    this.replyText(replyToken, EmojiUtils.emojify(result));
                }
            }
        } catch (Exception e) {
            //this.replyText(replyToken, strResult);
            throw e;
        }
    }

        private void dailyBeauty(String text, String replyToken) throws IOException {

        String beautyLink = "https://tw.appledaily.com/search/result?querystrS=%E4%BB%8A%E5%A4%A9%E6%88%91%E6%9C%80%E7%BE%8E&sort=time&searchType=all&dateStart=&dateEnd=";

        //this.replyText(replyToken, beautyLink);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String targetUrl = "";
            targetUrl = EntityUtils.toString(httpEntity, "utf-8");

            targetUrl = targetUrl.substring(targetUrl.indexOf("<h2><a href=\"https://tw.appledaily.com/headline/daily/")+13, targetUrl.length());
            targetUrl = targetUrl.substring(0, targetUrl.indexOf("\" target=\"_blank\">"));

            PgLog.info(targetUrl);
            httpget = new HttpGet(targetUrl);
            response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();

            String dumpSource = "";
            dumpSource = EntityUtils.toString(httpEntity, "utf-8");

            dumpSource = dumpSource.substring(dumpSource.indexOf("\"thumbnailUrl\": \"")+17, dumpSource.length());
            dumpSource = dumpSource.substring(0, dumpSource.indexOf("\","));
                        
            PgLog.info("Piggy Check dailyBeauty image: " + dumpSource);
            //this.replyText(replyToken, dumpSource);

            this.replyImage(replyToken, dumpSource, dumpSource);

        }catch (IOException e2) {
            throw e2;
        }
    }

    /*private void dailyBeauty(String text, String replyToken) throws IOException {

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String strDate = sdFormat.format(date);
        String beautyLink = "http://unayung.cc/links/" + strDate;

        //this.replyText(replyToken, beautyLink);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String dumpSource = "";
            dumpSource = EntityUtils.toString(httpEntity, "utf-8");
            dumpSource = dumpSource.substring(dumpSource.indexOf("image_src\" href=\"")+17, dumpSource.length());
            dumpSource = dumpSource.substring(0, dumpSource.indexOf("\" />"));
            if (dumpSource.startsWith("http:")) {
                dumpSource = dumpSource.replace("http:", "https:");
            }
            if (dumpSource.contains("ab.unayung.cc")) {
                dumpSource = dumpSource.replace("ab.unayung.cc", "unayung.cc");
            }
                        
            PgLog.info("Piggy Check dailyBeauty image: " + dumpSource);
            //this.replyText(replyToken, dumpSource);

            this.replyImage(replyToken, dumpSource, dumpSource);

        }catch (IOException e2) {
            throw e2;
        }
    }*/

    private void dailyBeautyName(String text, String replyToken) throws IOException {

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String strDate = sdFormat.format(date);
        String beautyLink = "http://unayung.cc/links/" + strDate;

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String dumpSource = "";
            dumpSource = EntityUtils.toString(httpEntity, "utf-8");
            //dumpSource = dumpSource.substring(dumpSource.indexOf("og:description\" content=\"")+25, dumpSource.length());
            dumpSource = dumpSource.substring(dumpSource.indexOf("white-box detail\">"), dumpSource.length());
            //dumpSource = dumpSource.substring(0, dumpSource.indexOf("\"/>"));
            //dumpSource = dumpSource.substring(0, dumpSource.indexOf("本專欄歡迎"));

            if (dumpSource.indexOf("本專欄歡迎") > 0) {
                dumpSource = dumpSource.substring(0, dumpSource.indexOf("本專欄歡迎"));
            }
            else {
                dumpSource = dumpSource.substring(0, dumpSource.indexOf("<p>資料來源"));
            }
                

            dumpSource = dumpSource.substring(dumpSource.indexOf("<h4>")+4, dumpSource.length());
            dumpSource = dumpSource.replaceAll("          ", "");
            dumpSource = dumpSource.replaceAll("</h4>", "");
            dumpSource = dumpSource.replaceAll("<br>", "\n");

            if (dumpSource.indexOf("\" target=\"") > 0) {
                dumpSource = dumpSource.replaceAll("<a href=\"", "");
                dumpSource = dumpSource.substring(0, dumpSource.indexOf("\" target=\""));
            }
            
            this.replyText(replyToken, dumpSource);

        }catch (IOException e2) {
            throw e2;
        }
    }

    private void dailySentence(String text, String replyToken) throws IOException {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="http://www.appledaily.com.tw/index/dailyquote/";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String daySentence = "";
            String daySentenceWhoSaid = "";

            daySentence = EntityUtils.toString(httpEntity, "utf-8");
            daySentence = daySentence.substring(daySentence.indexOf("<p>")+3, daySentence.length());
            daySentence = daySentence.substring(0, daySentence.indexOf("</p>"));
            

            this.replyText(replyToken, daySentence);

        }catch (IOException e2) {
            throw e2;
        }
    }


    private void amazonJpSearch(String replyToken, String text) throws IOException {

        text = text.replace("AmazonJp:", "").trim();

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=カタカナ&url=search-alias%3Daps&field-keywords="+text;
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String context = "";

            context = EntityUtils.toString(httpEntity, "utf-8");

            int maxCount = 0; // Max: 5
            List<ImageCarouselColumn> columnsList = new ArrayList<>();
            while (maxCount<5 && context.indexOf("data-asin=\"")> 0) {
                context = context.substring(context.indexOf("data-asin=\""), context.length());
                context = context.substring(context.indexOf("href=\"https:")+6, context.length());
                String searchResultUrl = context.substring(0, context.indexOf("\"><img"));
                String imgUrl = context.substring(context.indexOf("<img src=\"")+10, context.indexOf("\" srcset="));
                PgLog.info("Piggy Check searchResultUrl: " + searchResultUrl);
                PgLog.info("Piggy Check imgUrl: " + imgUrl);
                columnsList.add(getImageCarouselColumn(imgUrl, "PG Cute!", searchResultUrl));
            }
            if (maxCount>0) {
                LineMessagePrimitive.replyImageCarouselTemplate(replyToken, "AmazonJp", columnsList);    
            }
            else {
                this.replyText(replyToken, "搜索失敗");
            }

        }catch (IOException e2) {
            this.replyText(replyToken, "搜索大失敗");
            throw e2;
        }
    }

    private void japaneseNameToHiragana(String replyToken, String text) throws IOException {
        text = text.replace("的平假名是", "").replace("的平假名", "").replace("？", "").replace("?", "").trim();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://tw.saymynamae.com/" + text + "-to-hiragana";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String result = "";

            result = EntityUtils.toString(httpEntity, "utf-8");
            result = result.substring(result.indexOf("<h2 style=\""), result.length());
            result = result.substring(result.indexOf("px;\">")+5, result.indexOf("</h2>"));
            
            String string_result = text + " 的平假名是 " + result;

            this.replyText(replyToken, string_result);

        }catch (IOException e2) {
            throw e2;
        }
    }
    
    private void japaneseNameToKatakana(String replyToken, String text) throws IOException {
        text = text.replace("的片假名是", "").replace("的片假名", "").replace("？", "").replace("?", "").trim();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://tw.saymynamae.com/" + text + "-to-katakana";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String result = "";

            result = EntityUtils.toString(httpEntity, "utf-8");
            result = result.substring(result.indexOf("<h2 style=\""), result.length());
            result = result.substring(result.indexOf("px;\">")+5, result.indexOf("</h2>"));
            
            String string_result = text + " 的片假名是 " + result;

            this.replyText(replyToken, string_result);

        }catch (IOException e2) {
            throw e2;
        }
    }
    private void japaneseNameToRomaji(String replyToken, String text) throws IOException {
        text = text.replace("的羅馬拼音是", "").replace("的羅馬拼音", "").replace("？", "").replace("?", "").trim();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://tw.saymynamae.com/" + text + "-to-romaji";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String result = "";

            result = EntityUtils.toString(httpEntity, "utf-8");
            result = result.substring(result.indexOf("<h2 style=\""), result.length());
            result = result.substring(result.indexOf("px;\">")+5, result.indexOf("</h2>"));
            
            String string_result = text + " 的羅馬拼音是 " + result;

            this.replyText(replyToken, string_result);

        }catch (IOException e2) {
            throw e2;
        }
    }

    private void whoImPickRandomPttBeautyGirlMap(String userId, String replyToken) {
        if (mWhoImPickRandomGirlMap.containsKey(userId)) {
            //this.replyText(replyToken, mWhoImPickRandomGirlMap.get(userId));
            processReplyPttBeautyGirl(replyToken, mWhoImPickRandomGirlMap.get(userId));
        }
        else {
            if (isAdminUserId(userId)) {
                this.replyText(replyToken, "ＰＧ 大人您剛還沒抽過唷");
            }
            else {
                this.replyText(replyToken, "你剛又還沒抽過...\n腦抽？");
            }
            
        }
    }

    private void whoTheyPickRandomPttBeautyGirlMap(String senderId, String replyToken) {
        if (mWhoTheyPickRandomGirlMap.containsKey(senderId)) {
            //this.replyText(replyToken, mWhoTheyPickRandomGirlMap.get(senderId));
            processReplyPttBeautyGirlHistory(replyToken, mWhoTheyPickRandomGirlMap.get(senderId));
        }
        else {
            this.replyText(replyToken, "這群組還沒人抽過唷");
        }
    }

    private String generateMozeUrlScheme(String text) {
        String result = "";
        if (text.contains("我從")&&text.contains("花")&&text.contains("買")) {
            // 我從錢包花500買午餐
            String amount = ""; // 500
            String account = ""; // 錢包
            String subcategory = ""; // 午餐
            try {
                amount = text.substring(text.indexOf("花")+1,text.indexOf("買"));
                account = text.substring(text.indexOf("我從")+2,text.indexOf("花"));
                subcategory = text.substring(text.indexOf("買")+1,text.length());
            } catch (Exception e) {
                return "";
            }

            result = "moze3://expense?amount="+amount+"&account="+account+"&subcategory="+subcategory;
        }
        else if (text.contains("我從")&&text.contains("轉")&&text.contains("到")) {
            // 我從錢包轉50到悠遊卡做儲值用
            String sourceAmount = ""; // 50
            String targetAmount = ""; // 50
            String sourceAccount = ""; // 錢包
            String targetAccount = ""; // 悠遊卡
            String subcategory = ""; // 儲值
            try {
                sourceAmount = text.substring(text.indexOf("轉")+1,text.indexOf("到"));
                targetAmount = text.substring(text.indexOf("轉")+1,text.indexOf("到"));
                sourceAccount = text.substring(text.indexOf("我從")+2,text.indexOf("轉"));
                targetAccount = text.substring(text.indexOf("到")+1,text.indexOf("做"));
                subcategory = text.substring(text.indexOf("做")+1,text.indexOf("用"));

            } catch (Exception e) {
                return "";
            }

            result = "moze3://transfer?sourceAmount="+sourceAmount+"&targetAmount="+targetAmount+"&sourceAccount="+sourceAccount+"&targetAccount="+targetAccount+"&&subcategory="+subcategory;
        }        
        return result;
    }

    private void replyTextHowOld(String replyToken, String text) {
        text = text.replace("幾歲", "").replace("?", "").replace("？", "").trim();
        String result = "";
        if (text.equals("幼稚園小小班") || text.equals("幼稚園幼幼班") || text.equals("幼幼班")) {
            result = "3 歲";
        }
        else if (text.equals("幼稚園小班") || text.equals("小班")) {
            result = "4 歲";
        }
        else if (text.equals("幼稚園中班") || text.equals("中班")) {
            result = "5 歲";
        }
        else if (text.equals("幼稚園大班") || text.equals("大班")) {
            result = "6 歲";
        }
        else if (text.equals("國小一年級") || text.equals("小一")) {
            result = "7 歲";
        }
        else if (text.equals("國小二年級") || text.equals("小二")) {
            result = "8 歲";
        }
        else if (text.equals("國小三年級") || text.equals("小三")) {
            result = "9 歲";
        }
        else if (text.equals("國小四年級") || text.equals("小四")) {
            result = "10 歲";
        }
        else if (text.equals("國小五年級") || text.equals("小五")) {
            result = "11 歲";
        }
        else if (text.equals("國小六年級") || text.equals("小六")) {
            result = "12 歲";
        }
        else if (text.equals("國中一年級") || text.equals("國一")) {
            result = "13 歲";
        }
        else if (text.equals("國中二年級") || text.equals("國二")) {
            result = "14 歲";
        }
        else if (text.equals("國中三年級") || text.equals("國三")) {
            result = "15 歲";
        }
        else if (text.equals("高中一年級") || text.equals("高一")) {
            result = "16 歲";
        }
        else if (text.equals("高中二年級") || text.equals("高二")) {
            result = "17 歲";
        }
        else if (text.equals("高中三年級") || text.equals("高三")) {
            result = "18 歲";
        }
        else if (text.equals("大學一年級") || text.equals("大一")) {
            result = "19 歲";
        }
        else if (text.equals("大學二年級") || text.equals("大二")) {
            result = "20 歲";
        }
        else if (text.equals("大學三年級") || text.equals("大三")) {
            result = "21 歲";
        }
        else if (text.equals("大學四年級") || text.equals("大四")) {
            result = "22 歲";
        }
        else if (isStringIncludeNumber(text)) {

            try {
                boolean isRepublicEra = false;
                text = text.replace("年", "").trim();
                if (text.startsWith("民國")) {
                    text = text.replace("民國", "").trim();
                    isRepublicEra = true;
                }
                else if (text.startsWith("西元")) {
                    text = text.replace("西元", "").trim();
                }
                int inputNumber = Integer.parseInt(text);
                inputNumber = isRepublicEra ? (inputNumber+1911) : inputNumber;
                int year = Calendar.getInstance().get(Calendar.YEAR);
                if (inputNumber > 0 && year - inputNumber > 0) {
                    this.replyText(replyToken, "" + (year - inputNumber) + " 歲");
                }
                else {
                    this.replyText(replyToken, "白痴...我懶得理你");
                }
            }
            catch(java.lang.NumberFormatException e1) {
                return;
            }
        }

        if (!result.equals("")) {
            this.replyText(replyToken, result);
        }
    }

    private void replyTextMjHowManyTai(String replyToken, String text) {
        text = text.replace("幾臺", "").replace("幾台", "").replace("?", "").replace("？", "").replace("\n", "").replace("\r\n", "").trim();
        String original_text = text;
        int count = 0;
        String result = "已處理:\n";

        if (text.contains("莊家連一拉一") || text.contains("連一拉一")) {
            text = text.replace("莊家連一拉一", "").replace("連一拉一", "").trim();
            count+=3;
            result = result + "莊家連一拉一 3台\n";
        }
        else if (text.contains("莊家連二拉二") || text.contains("連二拉二")) {
            text = text.replace("莊家連二拉二", "").replace("連二拉二", "").trim();
            count+=5;
            result = result + "莊家連二拉二 5台\n";
        }
        else if (text.contains("莊家連三拉三") || text.contains("連三拉三")) {
            text = text.replace("莊家連三拉三", "").replace("連三拉三", "").trim();
            count+=7;
            result = result + "莊家連三拉三 7台\n";
        }
        else if (text.contains("莊家連四拉四") || text.contains("連四拉四")) {
            text = text.replace("莊家連四拉四", "").replace("連四拉四", "").trim();
            count+=9;
            result = result + "莊家連四拉四 9台\n";
        }
        else if (text.contains("莊家連五拉五") || text.contains("連五拉五")) {
            text = text.replace("莊家連五拉五", "").replace("連五拉五", "").trim();
            count+=11;
            result = result + "莊家連五拉五 11台\n";
        }
        else if (text.contains("莊家連六拉六") || text.contains("連六拉六")) {
            text = text.replace("莊家連六拉六", "").replace("連六拉六", "").trim();
            count+=13;
            result = result + "莊家連六拉六 13台\n";
        }
        else if (text.contains("莊家連七拉七") || text.contains("連七拉七")) {
            text = text.replace("莊家連七拉七", "").replace("連七拉七", "").trim();
            count+=15;
            result = result + "莊家連七拉七 15台\n";
        }
        else if (text.contains("莊家連八拉八") || text.contains("連八拉八")) {
            text = text.replace("莊家連八拉八", "").replace("連八拉八", "").trim();
            count+=17;
            result = result + "莊家連八拉八 17台\n";
        }
        else if (text.contains("莊家連九拉九") || text.contains("連九拉九")) {
            text = text.replace("莊家連九拉九", "").replace("連九拉九", "").trim();
            count+=19;
            result = result + "莊家連九拉九 19台\n";
        }
        else if (text.contains("莊家連十拉十") || text.contains("連十拉十")) {
            text = text.replace("莊家連十拉十", "").replace("連十拉十", "").trim();
            count+=21;
            result = result + "莊家連十拉十 21台\n";
        }
        else if ((text.contains("莊家連") && text.contains("拉")) || (text.contains("連") && text.contains("拉"))) {
            this.replyText(replyToken, "放屁你連這麼多？\n做牌啦！\n拿刀來拿刀來！");
            return;
        }

        if (text.contains("莊家")) {
            text = text.replace("莊家", "").trim();
            count+=1;
            result = result + "莊家 1台\n";
        }

        if (text.contains("門清") && text.contains("自摸") ) {
            text = text.replace("門清", "").replace("自摸", "").trim();
            count+=3;
            result = result + "門清自摸 3台\n";
        }

        if (text.contains("門清")) {
            text = text.replace("門清", "").trim();
            count+=1;
            result = result + "門清 1台\n";
        }

        if (text.contains("自摸")) {
            text = text.replace("自摸", "").trim();
            count+=1;
            result = result + "自摸 1台\n";
        }

        if (text.contains("搶槓")) {
            text = text.replace("搶槓", "").trim();
            count+=1;
            result = result + "搶槓 1台\n";
        }

        if (text.contains("紅中")) {
            text = text.replace("紅中", "").trim();
            count+=1;
            result = result + "紅中 1台\n";
        }

        if (text.contains("青發")) {
            text = text.replace("青發", "").trim();
            count+=1;
            result = result + "青發 1台\n";
        }

        if (text.contains("白板")) {
            text = text.replace("白板", "").trim();
            count+=1;
            result = result + "白板 1台\n";
        }

        if (text.contains("單吊") || text.contains("單釣")) {
            text = text.replace("單吊", "").replace("單釣", "").trim();
            count+=1;
            result = result + "單吊 1台\n";
        } else if (text.contains("邊張")) {
            text = text.replace("邊張", "").trim();
            count+=1;
            result = result + "邊張 1台\n";
        }

        if (text.contains("門清")) {
            text = text.replace("門清", "").trim();
            count+=1;
            result = result + "門清 1台\n";
        }

        if (text.contains("半求") && original_text.contains("自摸")) {
            text = text.replace("半求", "").trim();
            count+=1;
            result = result + "半求 1台\n";
        } else if (text.contains("半求") && !original_text.contains("自摸")) {
            text = text.replace("半求", "").trim();
            count+=2;
            result = result + "半求 1台 (半求一定是自摸)\n自摸 1台";
        }

        if (text.contains("槓上開花")) {
            text = text.replace("槓上開花", "").trim();
            count+=1;
            result = result + "槓上開花 1台\n";
        }

        if (text.contains("海底撈月")) {
            text = text.replace("海底撈月", "").trim();
            count+=1;
            result = result + "海底撈月 1台\n";
        }

        if (text.contains("河底撈月")) {
            text = text.replace("河底撈月", "").trim();
            count+=1;
            result = result + "河底撈月 1台\n";
        }

        if (text.contains("全求")) {
            text = text.replace("全求", "").trim();
            count+=2;
            result = result + "全求 2台\n";
        }        

        if (text.contains("春夏秋冬")) {
            text = text.replace("春夏秋冬", "").trim();
            count+=2;
            result = result + "春夏秋冬 2台\n";
        }

        if (text.contains("梅蘭竹菊")) {
            text = text.replace("梅蘭竹菊", "").trim();
            count+=2;
            result = result + "梅蘭竹菊 2台\n";
        }

        if (text.contains("地聽")) {
            text = text.replace("地聽", "").trim();
            count+=4;
            result = result + "地聽 4台\n";
        }

        if (text.contains("碰碰胡")) {
            text = text.replace("碰碰胡", "").trim();
            count+=4;
            result = result + "碰碰胡 4台\n";
        }

        if (text.contains("小三元")) {
            text = text.replace("小三元", "").trim();
            count+=4;
            result = result + "小三元 4台\n";
        }

        if (text.contains("混一色")) {
            text = text.replace("混一色", "").trim();
            count+=4;
            result = result + "混一色 1台\n";
        }

        if (text.contains("三暗刻")) {
            text = text.replace("三暗刻", "").trim();
            count+=2;
            result = result + "三暗刻 2台\n";
        }
        else if (text.contains("四暗刻")) {
            text = text.replace("四暗刻", "").trim();
            count+=5;
            result = result + "四暗刻 5台\n";
        }
        else if (text.contains("五暗刻")) {
            text = text.replace("五暗刻", "").trim();
            count+=8;
            result = result + "五暗刻 8台\n";
        }

        if (text.contains("天聽")) {
            text = text.replace("天聽", "").trim();
            count+=8;
            result = result + "天聽 8台\n";
        }

        if (text.contains("大三元")) {
            text = text.replace("大三元", "").trim();
            count+=8;
            result = result + "大三元 8台\n";
        }

        if (text.contains("小四喜")) {
            text = text.replace("小四喜", "").trim();
            count+=8;
            result = result + "小四喜 8台\n";
        }

        if (text.contains("清一色")) {
            text = text.replace("清一色", "").trim();
            count+=8;
            result = result + "清一色 8台\n";
        }

        if (text.contains("字一色")) {
            text = text.replace("字一色", "").trim();
            count+=8;
            result = result + "字一色 8台\n";
        }

        if (text.contains("八仙過海")) {
            text = text.replace("八仙過海", "").trim();
            count+=8;
            result = result + "八仙過海 8台\n";
        }

        result = result + "\n未處理:\n" + text + "\n" + "總台數: " + count;

        if (!result.equals("")) {
            this.replyText(replyToken, result);
        }
    }
    
    final int SINGLE_PIC = 1;
    final int MULTI_PIC = 2;
    private PttBeautyGirl getRandomPttBeautyGirl(String userId, String senderId, String replyToken, boolean isHot, int singlePicFlag) throws IOException {
        boolean isSinglePic = false;
        /*boolean isSinglePic = mPttBeautySinglePicMap.containsKey(userId) 
                ? mPttBeautySinglePicMap.get(userId).booleanValue() 
                : false default false*/;
        if (singlePicFlag > 0) {
            switch(singlePicFlag) {
            case SINGLE_PIC:
                isSinglePic = true;
                break;
            case MULTI_PIC:
                isSinglePic = false;
                break;
            }
        }
        if (senderId != null) {
            if (senderId.equals(GROUP_ID_CONNECTION)) {
                if (userId != null) {
                    if(mConnectionGroupRandomGirlUserIdList.contains(userId)) {
                        if (replyToken != null) {
                            this.replyImage(replyToken, IMAGE_I_HAVE_NO_SPERM, IMAGE_I_HAVE_NO_SPERM);
                        }
                        return null;
                    }
                    else {
                        mConnectionGroupRandomGirlUserIdList.add(userId);
                    }
                }
            }
            if (senderId.equals(GROUP_ID_TOTYO_HOT)) {
                if (userId != null) {
                    if(mTokyoHotRandomGirlLimitationList.containsKey(userId)) {
                        int count = mTokyoHotRandomGirlLimitationList.get(userId);
                        if (count > 10) {
                            if (replyToken != null) {
                                this.replyImage(replyToken, IMAGE_I_HAVE_NO_SPERM, IMAGE_I_HAVE_NO_SPERM);
                            }
                            return null;
                        }
                        else {
                            count++;
                            mTokyoHotRandomGirlLimitationList.put(userId, count);    
                        }
                    }
                    else {
                        mTokyoHotRandomGirlLimitationList.put(userId, 1);
                    }
                }
            }
        }

        PttBeautyGirl result = getRandomPttBeautyImageUrl(userId, senderId, isHot);

        //PgLog.info("Piggy Check randomPttBeautyGirl url list size: " + result.getPicUrlList().size());
        /*if (url.equals("")) {
            this.replyText(replyToken, "PTT 表特版 parse 失敗");
            return;
        }
        if (url.endsWith(".gif")) {
            this.replyText(replyToken, "Line 不能顯示 gif 直接貼: " + url);
        }
        if (url.indexOf("http:") >= 0) {
            url = url.replace("http", "https");
        }*/
        
        if (result.getPicUrlList().size() <= 0) {
            this.replyText(replyToken, "PTT 表特版 parse 失敗");
            return null;
        }
        /*else if (result.getUrlList().size() == 1 || isSinglePic) {
            String url = "";
            if (result.getUrlList().get(0).endsWith(".gif")) {
                this.replyText(replyToken, "Line 不能顯示 gif 直接貼: " + url);
            }
            if (result.getUrlList().get(0).indexOf("http:") >= 0) {
                url = result.getUrlList().get(0).replace("http", "https");
            }
            else {
                url = result.getUrlList().get(0);
            }
            if (url.equals("")) {
                return;
            }
            this.replyImage(replyToken, url, url);
        }*/
        else {
            return result;
        }
    }
    
    private void processReplyPttBeautyGirlHistory(String replyToken, PttBeautyHistory girlHistory) {
        List<ImageCarouselColumn> columnsList = new ArrayList<>();
        int count = 0;
        int MAX_CAROUSEL_COLUMN = 9;
        List<PttBeautyGirl> history = girlHistory.getSearchHistoryList();
        for(int i=0;i<history.size();i++) {
            PgLog.info("Piggy Check title: " + history.get(i).getTitle());
            //PgLog.info("Piggy Check searchResultUrl: " + history.get(i).getResultUrl());
            //PgLog.info("Piggy Check imgUrl: " + history.get(i).getUrlList().get(0));
            if (!history.get(i).getPicUrlList().get(0).endsWith(".gif")) {
                columnsList.add(getImageCarouselColumn(history.get(i).getPicUrlList().get(0), (history.get(i).getTitle() + " " + history.get(i).getRank()), history.get(i).getResultUrl()));
                count++;
            }
            else {
                try {
                    int index = 1;
                    while(true) {
                        if (!history.get(i).getPicUrlList().get(index).endsWith(".gif")) {
                            columnsList.add(getImageCarouselColumn(history.get(i).getPicUrlList().get(index), (history.get(i).getTitle() + " " + history.get(i).getRank()), history.get(i).getResultUrl()));
                            count++;
                            break;
                        }
                        index++;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            if (count > MAX_CAROUSEL_COLUMN) {
                break;
            }
        }

        LineMessagePrimitive.replyImageCarouselTemplate(replyToken, "Ptt Beauty History", columnsList);
    }
    
    final int MAX_CAROUSEL_COLUMN = 9;

    private void processReplyPttBeautyGirl(String replyToken, PttBeautyGirl girl) {
        List<ImageCarouselColumn> columnsList = new ArrayList<>();
        int index = 0;
        int count = 0;
        PgLog.info("Piggy Check title: " + girl.getTitle());
        PgLog.info("Piggy Check searchResultUrl: " + girl.getResultUrl());
        while (index < girl.getPicUrlList().size()) {
            //PgLog.info("Piggy Check imgUrl: " + girl.getUrlList().get(index));
            if (!girl.getPicUrlList().get(index).endsWith(".gif")) {
                columnsList.add(getImageCarouselColumn(girl.getPicUrlList().get(index), (girl.getTitle() + " " + girl.getRank()), girl.getResultUrl()));
                count++;
            }
            index++;
            if (count > MAX_CAROUSEL_COLUMN) {
                break;
            }
        }

        LineMessagePrimitive.replyImageCarouselTemplate(replyToken, girl.getDescribeString(), columnsList);
    }
    
    class ImageItem {
        String mTitle;
        String mUrl;
        String mImgUrl;
        public ImageItem(String title, String imgUrl, String url) {
            mTitle = title;
            mUrl = url;
            mImgUrl = imgUrl;
            if (mImgUrl.indexOf("http:") >= 0) {
                mImgUrl = mImgUrl.replace("http", "https");
            }
        }
        public String getTitle() {
            return mTitle;
        }
        public String getUrl() {
            return mUrl;
        }
        public String getImgUrl() {
            return mImgUrl;
        }
    }
    private void processReplyImageCarouselTemplateFromStringList(String replyToken, List<ImageItem> imgs, String title) {
        List<ImageCarouselColumn> columnsList = new ArrayList<>();
        int index = 0;
        int count = 0;
        List<ImageItem> resultList = (List<ImageItem>) getRandomItemFromList(imgs, MAX_CAROUSEL_COLUMN);
        
        while (index < resultList.size()) {
            //PgLog.info("Piggy Check imgUrl: " + girl.getUrlList().get(index));
            if (!resultList.get(index).getImgUrl().endsWith(".gif")) {
                ImageItem item = resultList.get(index); 
                columnsList.add(getImageCarouselColumn(item.getImgUrl(), item.getTitle(), item.getUrl()));
                /*PgLog.info("Piggy Check TitleUrl: " + item.getTitle());
                PgLog.info("Piggy Check ImgUrl: " + item.getImgUrl());
                PgLog.info("Piggy Check Url: " + item.getUrl());*/
                count++;
            }
            index++;
            if (count > MAX_CAROUSEL_COLUMN) {
                break;
            }
        }

        LineMessagePrimitive.replyImageCarouselTemplate(replyToken, title, columnsList);
    }

    private void processReplyAvWikiList(String replyToken, AvWikiInfo info) {
        List<ImageCarouselColumn> columnsList = new ArrayList<>();
        int index = 0;
        int count = 0;
        PgLog.info("Piggy Check title: " + info.getTitle());
        PgLog.info("Piggy Check info: " + info);
        PgLog.info("Piggy Check info.getImageList().size: " + info.getImageList().size());

        while (index < info.getImageList().size()) {
            PgLog.info("Piggy Check info.getImageList(): " + info.getImageList().get(index));
            columnsList.add(getImagePostbackColumn(info.getImageList().get(index), info.getTitle(), AvWikiInfo.POSTBACK_PREFIX, info.toString()));
            count++;
            index++;
            if (count > MAX_CAROUSEL_COLUMN) {
                break;
            }
        }

        LineMessagePrimitive.replyImageCarouselTemplate(replyToken, info.getTitle(), columnsList);
    }


    private void randomGirl(String text, String replyToken) throws IOException {
        PgLog.info("Piggy Check randomGirl: " + text);
        try {
            if (mJanDanGirlList.size() > 0) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(mJanDanGirlList.size());
                String item = mJanDanGirlList.get(index);
                item = item.replace("http", "https");
                PgLog.info("Piggy Check item: " + item);
                this.replyImage(replyToken, item, item);
                // this.replyText(replyToken, item);
            }
            else {
                this.replyText(replyToken, "妹子還在跟PG睡覺喔..");
            }

        }catch (IndexOutOfBoundsException e2) {
            throw e2;
        }
        PgLog.info("Piggy Check 6");
    }

    private void instagramTarget(String userId, String senderId, String text, String replyToken, boolean isHot, boolean isPerson) throws IOException {
        if (senderId.equals(GROUP_ID_TOTYO_HOT)) {

            if(mTokyoHotRandomGirlLimitationList.containsKey(userId)) {
                int count = mTokyoHotRandomGirlLimitationList.get(userId);
                if (count > 10) {
                    this.replyImage(replyToken, IMAGE_I_HAVE_NO_SPERM, IMAGE_I_HAVE_NO_SPERM);
                    return;
                }
                else {
                    count++;
                    mTokyoHotRandomGirlLimitationList.put(userId, count);    
                }
            }
            else {
                mTokyoHotRandomGirlLimitationList.put(userId, 1);
            }
        }
        String url = "";
        if (isPerson) {
            url = getInstagramImageUrl(userId, senderId, text);
        }
        else {
            /*url = getRandomInstagramImageUrl(userId, senderId, text, isHot);*/
        }
        if (url.equals("N/A")) {
            this.replyText(replyToken, "此帳號未公開");
        }
        if (url.equals("")) {
            //this.replyText(replyToken, "沒有這個 TAG");
        }

        if (url.indexOf("http:") >= 0) {
            url = url.replace("http", "https");
        }
        this.replyImage(replyToken, url, url);
        
    }

    private void pexelsTarget(String text, String replyToken) throws IOException {
        String url = getRandomPexelsImageUrl(text);
        if (url.equals("")) {
            return;
        }

        if (url.indexOf("http:") >= 0) {
            url = url.replace("http", "https");
        }
        this.replyImage(replyToken, url, url);
        
    }

    private void pexelFoodCount(String text, String replyToken) throws IOException {

        
        
    }

    private void randomGirlProgressing(String text, String replyToken) throws IOException {
        if (mJanDanProgressingPage == 1) {
            this.replyText(replyToken, "煎蛋分析完成. 總頁數: Unknown");
        }
        else {
            this.replyText(replyToken, "煎蛋分析中... 總頁數: Unknown 當前處理第" + mJanDanProgressingPage + "頁");
        }
    }    

    private void randomGirlCount(String text, String replyToken) throws IOException {

        int correct_percentage = 0;
        int fail_percentage = 0;
        if (mJanDanParseCount > 0 && mJanDanGirlList.size() > 0) {
            correct_percentage = ((mJanDanGifCount+mJanDanGirlList.size()) * 100) / mJanDanParseCount;
        }

        if (mJanDanParseCount > 0 && mJanDanGirlList.size() > 0) {
            fail_percentage = ((mJanDanParseCount - (mJanDanGirlList.size() + mJanDanGifCount)) * 100) / mJanDanParseCount;
        }

        this.replyText(replyToken, "Correct: (" + (mJanDanGirlList.size() + mJanDanGifCount) + "/" + mJanDanParseCount + ") " + correct_percentage + "%\nIncorrect: (" + (mJanDanParseCount-(mJanDanGirlList.size()+mJanDanGifCount)) + "/" + mJanDanParseCount + ") " + fail_percentage + "%\nImage Count: " + mJanDanGirlList.size() + "\nGif Count: " + mJanDanGifCount);
        
    }

    private void randomGirlDecode(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand煎蛋解碼:", "");
        
        String item = decodeJandanImageUrl(text);
        item = item.replace("http", "https");
        this.replyText(replyToken, item);
    }

    private void randomGirlDecodeImage(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand煎蛋解碼圖:", "");
        
        String item = decodeJandanImageUrl(text);
        item = item.replace("http", "https");
        this.replyImage(replyToken, item, item);
    }

    private void replyInputImage(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand圖片:", "");
        
        String item = text;
        if (text.indexOf("https") < 0) {
            item = item.replace("http", "https");
        }        
        this.replyImage(replyToken, item, item);
    }

    // Where is my frog

    private void whereIsMyFrog(String text, String replyToken) throws IOException {
        text = text.substring(text.indexOf("蛙"), text.length());
        if (text.contains("哪")) {
            Random randomGenerator = new Random();
            int index = randomGenerator.nextInt(mRandamLocationTitleList.size());
            String title = mRandamLocationTitleList.get(index);

            index = randomGenerator.nextInt(mRandamLocationAddressList.size());
            String address = mRandamLocationAddressList.get(index);

            LineMessagePrimitive.replyLocation(replyToken, title, address, getRandomLatitude(), getRandomLongitude());
        }
    }

    private double getRandomLatitude() {
        // -40 ~ +75
        Random randomGenerator = new Random();
        int positive = randomGenerator.nextInt(2);
        int range = positive != 1 ? 40 : 75;
        int integer = randomGenerator.nextInt(range);
        String decimal = "" + (positive != 1 ? "-" : "") + integer + ".";
        for (int i=0; i<14; i++) {
            int random = randomGenerator.nextInt(10);
            decimal += random;
        }
        double result = Double.parseDouble(decimal);
        PgLog.info("getRandomLatitude: " + result);
        return result;
    }

    private double getRandomLongitude() {
        // -180 ~ +180 without -120 ~ -180, 145 ~ 180
        Random randomGenerator = new Random();
        int positive = randomGenerator.nextInt(2);
        int range = positive != 1 ? 125 : 145;
        int integer = randomGenerator.nextInt(range);
        String decimal = "" + (positive != 1 ? "-" : "") + integer + ".";
        for (int i=0; i<14; i++) {
            int random = randomGenerator.nextInt(10);
            decimal += random;
        }
        double result = Double.parseDouble(decimal);
        PgLog.info("getRandomLongitude: " + result);
        return result;
    }

    // Random location address

    private void updateRandomAddress(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand新增隨機地點:", "");

        if (mRandamLocationAddressList.indexOf(text) < 0) {

            if (text.startsWith("[") && text.endsWith("]")) {

                ArrayList<String> mTempArray = new ArrayList<String>();

                // TODO
                return;
            }



            if (text.length() > 0) {
                mRandamLocationAddressList.add(text);    
                this.replyText(replyToken, "成功新增隨機地點「" + text + "」");    
            }
            else {
                this.replyText(replyToken, "輸入值為空值");       
            }
            
        }
        else {
            this.replyText(replyToken, "「" + text + "」已存在列表");   
        }
        
    }

    private void deleteRandomAddress(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand刪除隨機地點:", "");
        try {
            if (mRandamLocationAddressList.indexOf(text) >= 0) {
                mRandamLocationAddressList.remove(mRandamLocationAddressList.indexOf(text));
                this.replyText(replyToken, "成功刪除隨機地點「" + text + "」");
            }
            else {
                this.replyText(replyToken, "「" + text + "」不存在");
            }
            

        }catch (IndexOutOfBoundsException e2) {
            this.replyText(replyToken, "「" + text + "」不存在");
            throw e2;
        }
    }

    private void cleanRandomAddress(String text, String replyToken) throws IOException {
                    
        mRandamLocationAddressList.clear();
        mRandamLocationAddressList = new ArrayList<String> (mDefaultRandamLocationAddressList);

        this.replyText(replyToken, "成功清除隨機地點");
    }

    private void dumpRandomAddress(String text, String replyToken) throws IOException {
        
        this.replyText(replyToken, "隨機地點: " + mRandamLocationAddressList.toString());
    }

    // Random location title

    private void updateRandomTitle(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand新增隨機動作:", "");

        if (mRandamLocationTitleList.indexOf(text) < 0) {

            if (text.startsWith("[") && text.endsWith("]")) {

                ArrayList<String> mTempArray = new ArrayList<String>();

                // TODO
                return;
            }



            if (text.length() > 0) {
                mRandamLocationTitleList.add(text);    
                this.replyText(replyToken, "成功新增隨機動作「" + text + "」");    
            }
            else {
                this.replyText(replyToken, "輸入值為空值");       
            }
            
        }
        else {
            this.replyText(replyToken, "「" + text + "」已存在列表");   
        }
        
    }

    private void deleteRandomTitle(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand刪除隨機動作:", "");
        try {
            if (mRandamLocationTitleList.indexOf(text) >= 0) {
                mRandamLocationTitleList.remove(mRandamLocationTitleList.indexOf(text));
                this.replyText(replyToken, "成功刪除隨機動作「" + text + "」");
            }
            else {
                this.replyText(replyToken, "「" + text + "」不存在");
            }
            

        }catch (IndexOutOfBoundsException e2) {
            this.replyText(replyToken, "「" + text + "」不存在");
            throw e2;
        }
    }

    private void cleanRandomTitle(String text, String replyToken) throws IOException {
                    
        mRandamLocationTitleList.clear();
        mRandamLocationTitleList = new ArrayList<String> (mDefaultRandamLocationTitleList);
        this.replyText(replyToken, "成功清除隨機動作");
    }

    private void dumpRandomTitle(String text, String replyToken) throws IOException {
        
        
            
        this.replyText(replyToken, "隨機動作: " + mRandamLocationTitleList.toString());

        
    }

    private void replyTaiwanWeatherCloudImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_CLOUD;
        this.replyImage(replyToken, source, source);
    }

    private void replyTaiwanWeatherRainImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_RAIN;
        this.replyImage(replyToken, source, source);
    }

    private void replyTaiwanWeatherRadarEchoImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_RADAR_ECHO;
        this.replyImage(replyToken, source, source);
    }

    private void replyTaiwanWeatherTemperatureImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_TEMPERATURE;
        this.replyImage(replyToken, source, source);
    }

    private void replyTaiwanWeatherUltravioletLightImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_ULTRAVIOLET_LIGHT;
        this.replyImage(replyToken, source, source);
    }



    // Eat what

    private void eatWhat(String text, String replyToken) throws IOException {
        
        try {
            if (mEatWhatArray.size() > 0) {
                Random randomGenerator = new Random();

                int index = randomGenerator.nextInt(mEatWhatArray.size());
                String item = mEatWhatArray.get(index);
                
                this.replyText(replyToken, "去吃" + item);
            }
            else {
                this.replyText(replyToken, "沒想法...");   
            }

        }catch (IndexOutOfBoundsException e2) {
            throw e2;
        }
    }

    private String LINE_NOTIFY_TOKEN_HELL_TEST_ROOM = "RPKQnj2YVRslWIodM2BBOZhlbJbomKzDFBOdD447png";
    private String LINE_NOTIFY_TOKEN_INGRESS_ROOM_RUN_RUN_RUN = "prpaLTiFmUvrMjZ2ggV4RdpUyol5l7nK4uwV3u2ug6Q";
    private String LINE_NOTIFY_TOKEN_INGRESS_ROOM_COMPLICATE = "Fo4mDtJlPr0Di9BTYD8eMuVWrvKjIZ0GgwyL39UeihM";
    private String LINE_NOTIFY_TOKEN_CHONPIGGY = "nOevfG97usKCBxO02FVFm0VZr32vx2d6yx76HosZAKQ";

    private void notifyMessage(String room, String text, String replyToken) throws IOException {
        text = text.replace("PgCommandNotifyMessage:", "");

        if (LineNotify.callEvent(room, text)) {
            if (!replyToken.equals("")) {
                this.replyText(replyToken, "文字發送成功");
            }
        }
        else {
            if (!replyToken.equals("")) {
                this.replyText(replyToken, "文字發送失敗");
            }
        }
    }

    private void notifyImage(String room, String image, String replyToken) throws IOException {
        image = image.replace("PgCommandNotifyImage:", "");

        if (LineNotify.callEvent(room, " ", image)) {
            this.replyText(replyToken, "圖片發送成功");
        }
        else {
            this.replyText(replyToken, "圖片發送失敗");
        }
        
    }

    private void updateEatWhat(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand新增吃什麼:", "");

        if (mEatWhatArray.indexOf(text) < 0) {

            if (text.startsWith("[") && text.endsWith("]")) {

                ArrayList<String> mTempArray = new ArrayList<String>();

                // TODO
                return;
            }



            if (text.length() > 0) {
                mEatWhatArray.add(text);    
                this.replyText(replyToken, "成功新增去吃「" + text + "」");    
            }
            else {
                this.replyText(replyToken, "輸入值為空值");       
            }
            
        }
        else {
            this.replyText(replyToken, "「" + text + "」已存在列表");   
        }
        
    }

    private void deleteEatWhat(String text, String replyToken) throws IOException {
        text = text.replace("PgCommand刪除吃什麼:", "");
        try {
            if (mEatWhatArray.indexOf(text) >= 0) {
                mEatWhatArray.remove(mEatWhatArray.indexOf(text));
                this.replyText(replyToken, "成功刪除去吃「" + text + "」");
            }
            else {
                this.replyText(replyToken, "「" + text + "」不存在");
            }
            

        }catch (IndexOutOfBoundsException e2) {
            this.replyText(replyToken, "「" + text + "」不存在");
            throw e2;
        }
    }

    private void cleanEatWhat(String text, String replyToken) throws IOException {
        mEatWhatArray.clear();
        this.replyText(replyToken, "成功清除去吃什麼");        
    }

    private void dumpEatWhat(String text, String replyToken) throws IOException {
        this.replyText(replyToken, "去吃什麼: " + mEatWhatArray.toString());
    }

    private void keywordImageControlDisable(String text, String replyToken) throws IOException {
        boolean isDoSomething = false;
        if (text.contains("Eg")||text.contains("eg")||text.contains("egef")||text.contains("女流氓")||text.contains("蕭婆")||text.contains("EG")) {
            isEgKeywordEnable = false;
            isDoSomething = true;
        }
        if (text.contains("部囧")) {
            isKofatKeywordEnable = false;
            isDoSomething = true;
        }
        if (text.contains("姨姨")||text.contains("委員")||text.contains("翠姨")) {
            isChuiyiKeywordEnable = false;
            isDoSomething = true;
        }
        if (text.contains("凱西")||text.contains("牙醫")) {
            isCathyKeywordEnable = false;
            isDoSomething = true;
        }
        if (isDoSomething) {
            this.replyText(replyToken, "喔..");
        }
    }

    private void keywordImageControlEnable(String text, String replyToken) throws IOException {
        boolean isDoSomething = false;
        if (text.contains("Eg")||text.contains("eg")||text.contains("egef")||text.contains("女流氓")||text.contains("蕭婆")||text.contains("EG")) {
            isEgKeywordEnable = true;
            isDoSomething = true;
        }
        if (text.contains("部囧")) {
            isKofatKeywordEnable = true;
            isDoSomething = true;
        }
        if (text.contains("姨姨")||text.contains("委員")||text.contains("翠姨")) {
            isChuiyiKeywordEnable = true;
            isDoSomething = true;
        }
        if (text.contains("凱西")||text.contains("牙醫")) {
            isCathyKeywordEnable = true;
            isDoSomething = true;
        }
        if (isDoSomething) {
            this.replyText(replyToken, "靠");
        }
    }

    private void replyImageTaiwanBearAndPanda(String replyToken) throws IOException {
        String source = IMAGE_PANDA;
        this.replyImage(replyToken, source, source);
    }

    private void replyImageWillYouCome(String replyToken) throws IOException {
        String source = IMAGE_WILL_YOU_COME;
        this.replyImage(replyToken, source, source);
    }

    private void replyImageBeAGoodMand(String replyToken) throws IOException {
        String source = IMAGE_BE_A_GOOD_MAN;
        this.replyImage(replyToken, source, source);
    }

    private void replyImageIamNotYourWife(String replyToken) throws IOException {
        String source = IMAGE_IM_NOT_YOUR_WIFE;
        this.replyImage(replyToken, source, source);
    }

    private void replyImageYouArePrev(String replyToken) throws IOException {
        String source = IMAGE_YOU_ARE_PERVERT;
        this.replyImage(replyToken, source, source);
    }

    private void replyImageIWillBeLate(String replyToken) throws IOException {
        String source = getRandomSourceFromList(mIWillBeLateList);
        this.replyImage(replyToken, source, source);
    }

    private void replyImageYouDeserveIt(String replyToken) throws IOException {
        String source = getRandomSourceFromList(mYouDeserveItImgurLinkList);
        this.replyImage(replyToken, source, source);
    }

    private void replyMdMap(String replyToken) throws IOException {
        if (mMdMapImageSource != null) {
            this.replyImage(replyToken, mMdMapImageSource, mMdMapImageSource);    
        }        
    }

    private void howPgSolveMdMap(String replyToken) throws IOException {
        String source = "https://i.imgur.com/KgRvW2u.png";
        this.replyImage(replyToken, source, source);
    }

    private void replyQuestionMarkImage(String replyToken) throws IOException {
        String source = getRandomSourceFromList(mQuestionMarkImageList);
        this.replyImage(replyToken, source, source);
    }

    private void makeWish(String senderId, String userId, String text, String replyToken) throws IOException {
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請先將 BOT 加為好友.");
            return;
        }
        text = text.replace("許願:", "");
        String result = "許願事件:\n";
        result += "senderId: " + senderId + "\n";
        result += "userId: " + userId + "\n";
        result += "name: " + getUserDisplayName(userId) + "\n";
        result += "內容: \n";
        result += text;
        LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, result);
        this.replyText(replyToken, "偉大的 PG 大人與你同在.");
    }

    private void makeSubmission(String senderId, String userId, String text, String replyToken) throws IOException {
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請先將 BOT 加為好友.");
            return;
        }
        text = text.replace("投稿:", "");
        String result = "投稿事件:\n";
        result += "senderId: " + senderId + "\n";
        result += "userId: " + userId + "\n";
        result += "name: " + getUserDisplayName(userId) + "\n";
        result += "內容: \n";
        result += text;
        LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, result);
        this.replyText(replyToken, "偉大的 PG 大人收到了.");
    }

    public static String sendPttOver18Checker() {
        String result = null;
        try {
            String strUrl = "https://www.ptt.cc/ask/over18?from=%2Fbbs%2FGossiping%2Findex.html";
            URL url = new URL( strUrl );
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod( "POST" );
            connection.addRequestProperty( "Accept", "application/json, text/javascript, */*; q=0.01" );
            connection.addRequestProperty( "Origin", "https://www.ptt.cc/" );
            connection.addRequestProperty( "X-Requested-With", "XMLHttpRequest" );
            connection.addRequestProperty( "x-hapi-key", "twitter-net_hiroki-followMe!" );
            connection.addRequestProperty( "User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36" );
            connection.addRequestProperty( "Content-Type", "application/x-www-form-urlencoded" );
            connection.setDoOutput(true);
            String parameterMessageString = new String("from="+"/bbs/Gossiping/index.html"+"&yes=yes");
            //String parameterMessageString = new String("txt=%E7%89%B9%E5%83%B9&type=1&twid=");
            PrintWriter printWriter = new PrintWriter(connection.getOutputStream());
            printWriter.print(parameterMessageString);
            printWriter.close();
            connection.connect();
            
            int statusCode = connection.getResponseCode();

            PgLog.info("sendPttOver18Checker statusCode: " + statusCode);

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void processRandomeGetImage(String replyToken, String text) throws IOException {
        PgLog.info("processRandomeGetImage: " + text);
        text = text.replace("隨機取圖:", "");
        if (!text.startsWith("http")) {
            return;
        }

        sendPttOver18Checker();

        try{

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url=text;
            
            Random randomGenerator = new Random();
            int random_agent_num = randomGenerator.nextInt(mUserAgentList.size());
            
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String result_image_context = EntityUtils.toString(httpEntity, "utf-8");
            //PgLog.info("Piggy Check result_image_image: |" + result_image_context +"|");
            if (result_image_context.contains("<body")) {
                result_image_context = result_image_context.substring(result_image_context.indexOf("<body"), result_image_context.length());
            }
            List<ImageItem> resultImageList = new ArrayList<ImageItem> ();
            if (result_image_context.indexOf("http://imgur.com/") > 0) {
                PgLog.info("Website contains imgur url.");
                Pattern patternJp = Pattern.compile("http:\\/\\/imgur.com\\/.*");
                Matcher matcherJp = patternJp.matcher(result_image_context);
                while(matcherJp.find()){
                    
                    String result = matcherJp.group();
                    result = result.replace("</a>","");
                    result = result.replace("http:","https:");
                    result = result.replace("imgur.com","i.imgur.com");
                    result = result + ".jpg";
                    ImageItem item = new ImageItem("隨機取圖", result, url);
                    if (!resultImageList.contains(result)&&!result.contains("logo")) {
                        resultImageList.add(item);
                    }
                    //PgLog.info("Piggy Check get image from website imgur url: " + url + " img_link: " + result);
                }
            }
            else if (result_image_context.indexOf("http://i.imgur.com/") > 0) {
                PgLog.info("Website contains i.imgur url.");
                Pattern patternJp = Pattern.compile("http:\\/\\/i.imgur.com\\/.*");
                Matcher matcherJp = patternJp.matcher(result_image_context);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    result = result.replace("http:","https:");
                    result = result + ".jpg";
                    ImageItem item = new ImageItem("隨機取圖", result, url);
                    if (!resultImageList.contains(result)&&!result.contains("logo")) {
                        resultImageList.add(item);
                    }
                    //PgLog.info("Piggy Check get image from website imgur url: " + url + " img_link: " + result);
                }
            }
            else {
                PgLog.info("Website don't have imgur url.");
                Pattern patternJp = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?.(jpeg|jpg|png)");
                Matcher matcherJp = patternJp.matcher(result_image_context);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    ImageItem item = new ImageItem("隨機取圖", result, url);
                    if (!resultImageList.contains(result)&&!result.contains("logo")) {
                        resultImageList.add(item);
                    }
                    //PgLog.info("Piggy Check get image from website url: " + url + " img_link: " + result);
                }
            }

            
            if (resultImageList.size() > 0) {
                /*int random_num = randomGenerator.nextInt(resultImageList.size());
                String result = resultImageList.get(random_num);
                if (result == null || result.equals("")) {
                    PgLog.info("Piggy Check get image from website parse fail");
                }
                else {
                    PgLog.info("Piggy Check get image from website result: " + result);
                }
                if (result.indexOf("http:") >= 0) {
                    result = result.replace("http", "https");
                }
                PgLog.info("result image: " + result);
                this.replyImage(replyToken, result, result);*/

                processReplyImageCarouselTemplateFromStringList(replyToken, resultImageList, "隨機取圖");
            }
            else {
                PgLog.info("resultImageList.size() = 0");
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void processLinHoImage(String replyToken, String text) throws IOException {
        text = text.replace("年號:", "");
        if (text.length() != 2) {
            return;
        }
        if (isStringIncludeNumber(text) || isStringIncludeEnglish(text)) {
            return;
        }
        String result = LinHoImageHelper.getImageUrl(text);
        PgLog.info("Piggy Check processLinHoImage: " + result);
        if (result != null && result.length() == 64) {
            this.replyImage(replyToken, result, result);
        }
        else if (result != null) {
            this.replyText(replyToken, result);
        }
    }

    private boolean isStringIncludeNumber(String text) {
        boolean hasNum = false;
        if(text.matches(".*\\d+.*")) {
            hasNum = true;
        }
        return hasNum;
    }
    private static boolean isStringIncludeEnglish(String text){
        boolean hasEng = false;
        for(int i=0; i<text.length(); i++) {
            String test = text.substring(i, i+1);
            if(test.matches("[a-zA-Z]+")) {
                return true;
            }
        }
        return hasEng;
    }

    private boolean isStringIncludeChinese(String text) {
        for(int i=0; i<text.length(); i++) {  
            String test = text.substring(i, i+1);  
            if(test.matches("[\\u4E00-\\u9FA5]+")){  
                return true;
            }
        }
        return false;
    }

    // Sheet Feature
    private void processSheetOpen(String replyToken, String senderId, String userId, String text) {
        text = text.replace("開表單", "").replace(":", "").replace("：", "").trim();
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
            return;
        }
        if (!mSheetListMap.containsKey(senderId)) {
            SheetList sl = new SheetList(userId, text);
            mSheetListMap.put(senderId, sl);
            this.replyText(replyToken, sl.getGuideString());
        }
        else {
            SheetList sl = mSheetListMap.get(senderId);
            this.replyText(replyToken, "此群組已開啟了一個表單名為:\n" + sl.getSubject());
        }
    }
    
    private void processSheetDump(String replyToken, String senderId, String userId) {
        if (mSheetListMap.containsKey(senderId)) {
            SheetList sl = mSheetListMap.get(senderId);
            String result = sl.getDumpResult();
            this.replyText(replyToken, result);
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟任何表單");
        }
    }
    
    private void processSheetClose(String replyToken, String senderId, String userId) {
        if (mSheetListMap.containsKey(senderId)) {
            SheetList sl = mSheetListMap.get(senderId);
            if (sl.getHolder().equals(userId)) {
                String result = sl.close();
                this.replyText(replyToken, result);
                mSheetListMap.remove(senderId);
            }
            else {
                this.replyText(replyToken, "表單只能由發起人\n" + getUserDisplayName(sl.getHolder()) + "\n做收單操作");
            }
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟任何表單");
        }
    }
    
    private void processSheetAdd(String replyToken, String senderId, String userId, String text) {
        text = text.replace("登記", "").replace(":", "").replace("：", "").trim();
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
            return;
        }
        if (mSheetListMap.containsKey(senderId)) {
            SheetList sl = mSheetListMap.get(senderId);
            sl.updateData(userId, text);
            String result = "購買人:" + getUserDisplayName(userId) + "\n";
            result += "品項:" + text + "\n";
            result += "登記成功";
            this.replyText(replyToken, result);
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟任何表單");
        }
    }

    // PlusPlus Sheet Feature

    private void processPlusPlusOpen(String replyToken, String senderId, String userId, String text) {
        text = text.replace("要的加加", "").trim();
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
            return;
        }
        if (!mPlusPlusListMap.containsKey(senderId)) {
            PlusPlusList sl = new PlusPlusList(userId, text);
            mPlusPlusListMap.put(senderId, sl);
            this.replyText(replyToken, sl.getGuideString());
        }
        else {
            PlusPlusList sl = mPlusPlusListMap.get(senderId);
            this.replyText(replyToken, "此群組已開啟了一個加加表單名為:\n" + sl.getSubject());
        }
    }
    
    private void processPlusPlusDump(String replyToken, String senderId, String userId) {
        if (mPlusPlusListMap.containsKey(senderId)) {
            PlusPlusList sl = mPlusPlusListMap.get(senderId);
            String result = sl.getDumpResult();
            this.replyText(replyToken, result);
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟加加表單");
        }
    }
    
    private void processPlusPlusClose(String replyToken, String senderId, String userId) {
        if (mPlusPlusListMap.containsKey(senderId)) {
            PlusPlusList sl = mPlusPlusListMap.get(senderId);
            if (sl.getHolder().equals(userId)) {
                String result = sl.close();
                this.replyText(replyToken, result);
                mPlusPlusListMap.remove(senderId);
            }
            else {
                this.replyText(replyToken, "表單只能由加加發起人\n" + getUserDisplayName(sl.getHolder()) + "\n做截止操作");
            }
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟加加表單");
        }
    }
    
    private void processPlusPlusAdd(String replyToken, String senderId, String userId, String text) {
        text = text.replace(" ", "").trim();
        
        if (mPlusPlusListMap.containsKey(senderId)) {
            if (getUserDisplayName(userId).equals("")) {
                this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
                return;
            }
            if (text.equals("+0.5")||text.equals("+1")||text.equals("-1")) {
                PlusPlusList sl = mPlusPlusListMap.get(senderId);
                if (text.equals("-1")) {
                    sl.updateData(userId, "");
                    String result = "" + getUserDisplayName(userId) + " -1";
                    this.replyText(replyToken, result);
                }
                else {
                    text = text.replace("+", "");
                    sl.updateData(userId, text);
                    String result = "" + getUserDisplayName(userId) + " +" + text;
                    this.replyText(replyToken, result);
                }
            }
        }
        else {
            //this.replyText(replyToken, "此群組尚未開啟加加表單");
        }
    }

    // PlusPlus Sheet Feature End

    private void processJetLag(String replyToken, String text) {
        String result = "";
        String output = "";
        try {
            text = URLEncoder.encode(text, "UTF-8");
            String url = "http://www.google.com.tw/search?q="+text+"時差";
            PgLog.info("JetLagUrl: " + url);
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpHost proxy = new HttpHost("113.254.114.24",8197);   // 元朗 proxy
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, br");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();
            String result_title = "";
            String result_local_time = "";
            String result_remote_time = "";
            output = EntityUtils.toString(httpEntity, "utf-8");
            
            this.replyText(replyToken, output);
            //PgLog.info("output: " + output);
            //result = output;
            result_title = output.substring(output.indexOf("<div class=\"Mv3Zsd vk_bk dDoNo\">   ") + 35, output.length());
            // Copy
            result_local_time = result_title;
            result_remote_time = result_title;
            // Copy end
            result_title = result_title.substring(0, result_title.indexOf("    </div>"));
            PgLog.info("result_title: " + result_title);
            result_title = result_title.replace("台北市內湖區港墘里", "台灣的時間");
            PgLog.info("result_title: " + result_title);

            result_local_time = result_local_time.substring(result_local_time.indexOf("<span class=\"KfQeJ\">") + 20, result_local_time.indexOf("</b> 是"));
            result_local_time = result_local_time.replace("</span><b>", " ");
            PgLog.info("result_local_time: " + result_local_time);
            result_local_time = "台灣 " + result_local_time;
            PgLog.info("result_local_time: " + result_local_time);

            result_remote_time = result_remote_time.substring(result_remote_time.indexOf("class=\"DcFqyf\"><span class=\"fJY1Ee\">") + 36, result_remote_time.length());
            String result_remote_time_temp_remote_name = result_remote_time.substring(0, result_remote_time.indexOf("</span>的"));
            PgLog.info("result_remote_time_temp_remote_name: " + result_remote_time_temp_remote_name);
            result_remote_time = result_remote_time.substring(result_remote_time.indexOf("</span>的<span class=\"KfQeJ\">") + 28, result_remote_time.indexOf("</b></div>"));
            PgLog.info("result_remote_time: " + result_remote_time);
            result_remote_time = result_remote_time.replace("</span><b>", " ");
            PgLog.info("result_remote_time: " + result_remote_time);

            result = result_title + "\n" + result_local_time + "\n是\n" + result_remote_time;

        }catch (Exception e) {
            //PgLog.info("" + e);
            e.printStackTrace();
            this.replyText(replyToken, output);
        }

        if (result.equals("")) {
            this.replyText(replyToken, "查不到與此地區的時差");
        }
        else {
            this.replyText(replyToken, result);
        }
    }

    private void bullyModeTrigger(String replyToken) throws IOException {

        if (mBullyModeCount > 0) {
            String source = mBullyModeCount == 1 ? IMAGE_NO_CONSCIENCE : mBullyModeTarget;
            mBullyModeCount--;
            this.replyImage(replyToken, source, source);    
        }

    }

    private void replyOkFineImage(String replyToken) throws IOException {
        String source = IMAGE_OK_FINE;
        this.replyImage(replyToken, source, source);
    }

    private void replyGiveSalmonNoSwordFishImage(String replyToken) throws IOException {
        String source = IMAGE_GIVE_SALMON_NO_SWORDFISH;
        this.replyImage(replyToken, source, source);
    }

    private void initBullyMode(String text, String replyToken) throws IOException {
        text = text.replace("霸凌模式:", "");
        mBullyModeCount = 10;
        mBullyModeTarget = text;
    }

    private void interruptBullyMode(String replyToken) throws IOException {
        String source = IMAGE_NO_CONSCIENCE;
        mBullyModeCount = 0;
        this.replyImage(replyToken, source, source);   
    }

    private void keywordImage(String text, String replyToken) throws IOException {

        String source = "";
        if (text.equals("Chuiyi")) {
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(3);
            switch (random_num) {
                case 0:
                    source = "https://i.imgur.com/4bEHYOm.jpg";
                    break;
                case 1:
                    source = "https://i.imgur.com/ifkhGyu.jpg";
                    break;
                case 2:
                    source = "https://i.imgur.com/BsavJHK.jpg";
                    break;
            }
        }
        if (text.equals("EarthQuakeSeek")) {
            source = getRandomSourceFromList(mEarthQuakeMeMeImgurLinkList);
        }
        if (text.equals("kofat")) {
            source = getRandomSourceFromList(mKofatCosplayImgurLinkList);
        }
        if (text.equals("TragicWorld")) {
            source = "https://i.imgur.com/1Ap4Qka.jpg";
        }
        if (text.equals("IfYouAngry")) {
            source = IMAGE_IF_YOU_ANGRY;
        }
        if (text.equals("GPNUDD")) {
            source = IMAGE_GPNUDD;
        }
        if (text.equals("EG")) {
            //List<String> mEgDevilImgurLinkList = Arrays.asList("https://i.imgur.com/6qN9GI1.jpg", "https://i.imgur.com/qHbEBjN.jpg", "https://i.imgur.com/NFbnbSs.jpg", "https://i.imgur.com/68KRiAj.jpg", "https://i.imgur.com/dHEEBcU.jpg", "https://i.imgur.com/OMqBsOl.jpg", "https://i.imgur.com/JBuBhqr.jpg", "https://i.imgur.com/O5o7tD3.jpg", "https://i.imgur.com/PYZ4v9V.jpg", "https://i.imgur.com/GRD3yXF.jpg");
            List<String> mEgDevilImgurLinkList = Arrays.asList("https://i.imgur.com/6qN9GI1.jpg", 
                "https://i.imgur.com/Vr2TgNk.jpg", 
                "https://i.imgur.com/eKa5nvK.jpg", 
                "https://i.imgur.com/fAT6i90.jpg", 
                "https://i.imgur.com/ojXWlEF.jpg", 
                "https://i.imgur.com/MODtrta.jpg", 
                "https://i.imgur.com/MquMx2y.jpg", 
                "https://i.imgur.com/NZx5qOk.jpg", 
                "https://i.imgur.com/1GuQBD7.jpg", 
                "https://i.imgur.com/TaQ4WcZ.jpg", 
                "https://i.imgur.com/6IO0KZH.jpg", 
                "https://i.imgur.com/xNnLjyW.jpg", 
                "https://i.imgur.com/MIQ4FL7.jpg");
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mEgDevilImgurLinkList.size());
            int random_result = randomGenerator.nextInt(30);
            if (random_result == 15) {
                source = "https://i.imgur.com/kQrWoal.jpg";
            }
            else {
                source = mEgDevilImgurLinkList.get(random_num);
            }
        }
        if (text.equals("FattyCathy")) {
            List<String> mCathyImgurLinkList = Arrays.asList("https://i.imgur.com/Z5ANVH8.jpg", "https://i.imgur.com/h7w7Tf5.jpg", "https://i.imgur.com/SnVoayh.jpg", "https://i.imgur.com/HDMVB7b.jpg", "https://i.imgur.com/FBf3jBj.jpg", "https://i.imgur.com/zOsCpM9.jpg", "https://i.imgur.com/rvpbeBu.jpg", "https://i.imgur.com/Zdutf4L.jpg", "https://i.imgur.com/ADVhL9m.jpg", "https://i.imgur.com/ehWNONr.jpg", "https://i.imgur.com/coHvFWI.jpg", "https://i.imgur.com/Cjyk751.jpg");
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mCathyImgurLinkList.size());
            int random_result = randomGenerator.nextInt(100);
            if (random_result == 50) {
                source = "https://i.imgur.com/Ow6MgCO.jpg";
            }
            else {
                source = mCathyImgurLinkList.get(random_num);
            }
        }
        this.replyImage(replyToken, source, source);
    }

    private void startUserIdDetectMode(String senderId, String replyToken) {
        mUserIdDetectModeGroupId = senderId;
        mIsUserIdDetectMode = true;
        this.replyText(replyToken, "好的 PG 大人");
    }

    private void stopUserIdDetectMode(String senderId, String replyToken) {
        mUserIdDetectModeGroupId = "";
        mIsUserIdDetectMode = false;
        this.replyText(replyToken, "好的 PG 大人");
    }

    private boolean replyUserId(String userId, String senderId, String replyToken) {
        if (userId.equals(USER_ID_PIGGY) || userId.equals(USER_ID_TEST_MASTER)) {
            return false;
        }
        if (mIsUserIdDetectMode && mUserIdDetectModeGroupId.equals(senderId)) {
            this.replyText(replyToken, userId);
            return true;
        }
        return false;
    }

    private void startTotallyBully(String replyToken) {
        mIsTotallyBullyEnable = true;
        this.replyText(replyToken, "好的 PG 大人\n對象是: " + getUserDisplayName(mTotallyBullyUserId));
    }

    private void stopTotallyBully(String replyToken) {
        mIsTotallyBullyEnable = false;
        String source = IMAGE_NO_CONSCIENCE;
        this.replyImage(replyToken, source, source);
    }

    private void setTotallyBullyUser(String text, String replyToken) {
        text = text.replace("PgCommand設定徹底霸凌對象:", "");
        mTotallyBullyUserId = text;
        this.replyText(replyToken, "好的 PG 大人\n對象是: " + getUserDisplayName(mTotallyBullyUserId));
    }

    private void setTestAdminUser(String text, String replyToken) {
        text = text.replace("PgCommand設定代理管理員:", "");
        if (text.equals(USER_ID_CATHY)) {
            this.replyText(replyToken, "死肥豬不能當管理員");
        }
        USER_ID_TEST_MASTER = text;
        this.replyText(replyToken, "好的 PG 大人\n對象是: " + getUserDisplayName(mTotallyBullyUserId));
    }

    private void setTotallyBullyString(String text, String replyToken) {
        text = text.replace("PgCommand設定徹底霸凌字串:", "");
        mTotallyBullyReplyString = text;
        this.replyText(replyToken, "好的 PG 大人");
    }

    private void forceStopRPS(String replyToken) {
        mStartRPSUserId = "";
        mStartRPSGroupId = "";
        mRPSGameUserList.clear();
        this.replyText(replyToken, "好的 PG 大人");
    }

    private void startRPS(String userId, String senderId, String replyToken) {
        if (!mStartRPSGroupId.equals("") && !mStartRPSGroupId.equals(senderId)) {
            this.replyText(replyToken, "別的群組正在玩唷");
            return;
        }
        if (!mStartRPSUserId.equals("")) {return;}
        mStartRPSGroupId = senderId;
        mStartRPSUserId = userId;
        this.replyText(replyToken, "猜拳遊戲開始囉!\n請說「參加猜拳」來加入比賽");
    }

    private void stopRPS(String userId, String senderId, String replyToken) {
        if (!senderId.equals(mStartRPSGroupId)) {return;}
        if (!userId.equals(mStartRPSUserId)) {return;}
        Random randomGenerator = new Random();
        int random_num = randomGenerator.nextInt(mRPSGameUserList.size());
        String winnerUserId = mRPSGameUserList.get(random_num);
        String winner = getUserDisplayName(winnerUserId);
        mStartRPSUserId = "";
        mStartRPSGroupId = "";
        mRPSGameUserList.clear();
        this.replyText(replyToken, winner + " 把中指插進所有人的鼻孔贏得了比賽");
    }

    private void joinRPS(String userId, String senderId, String replyToken) {
        if (!senderId.equals(mStartRPSGroupId)) {return;}
        if (mRPSGameUserList.contains(userId)) {
            this.replyText(replyToken, "你已經出過了啦北七!");
            return;
        }
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "你要先加我好友才可以玩唷!");
            return;
        }
        mRPSGameUserList.add(userId);

        Random randomGenerator = new Random();
        int random_num = randomGenerator.nextInt(mDefaultRockPaperScissors.size());
        String result = mDefaultRockPaperScissors.get(random_num);

        this.replyText(replyToken, "" + getUserDisplayName(userId) + " 出了 " + result);
    }

    private void getHtml(String replyToken, String url) {
        String result = "";
        try {
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());
            CloseableHttpClient httpClient = HttpClients.createDefault();
            PgLog.info("getHtml:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            //httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","text/html");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");

            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            result = EntityUtils.toString(httpEntity, "utf-8");

        } catch (Exception e) {
        }
        if (!result.equals("")) {
            this.replyText(replyToken, result);
        }
        else {
            this.replyText(replyToken, "ＰＧ大人 getHtml fail.");
        }
    }

    private void startRandomSort(String userId, String senderId, String replyToken) {
        
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
            return;
        }
        if (!mRandomSortMap.containsKey(senderId)) {
            RandomSortList rsl = new RandomSortList(userId, senderId);
            mRandomSortMap.put(senderId, rsl);
            this.replyText(replyToken, rsl.getGuideString());
        }
        else {
            this.replyText(replyToken, "此群組已經開始了一個隨機排序\n");
        }
    }

    private void stopRandomSort(String userId, String senderId, String replyToken) {
        if (mRandomSortMap.containsKey(senderId)) {
            RandomSortList rsl = mRandomSortMap.get(senderId);
            if (rsl.getHolder().equals(userId)) {
                String result = rsl.stopJoin();
                this.replyText(replyToken, result);
            }
            else {
                this.replyText(replyToken, "表單只能由發起人\n" + getUserDisplayName(rsl.getHolder()) + "\n做截止操作");
            }
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟隨機排序");
        }
    }

    private void joinRandomSort(String userId, String senderId, String replyToken) {
        if (getUserDisplayName(userId).equals("")) {
            this.replyText(replyToken, "請將 BOT 加為好友後方可使用此功能");
            return;
        }
        if (mRandomSortMap.containsKey(senderId)) {
            RandomSortList rsl = mRandomSortMap.get(senderId);
            rsl.addUserId(userId);
            String result = rsl.getDumpResult();
            this.replyText(replyToken, result);
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟隨機排序");
        }
    }

    private void sortAllRandomSort(String userId, String senderId, String replyToken) {
        if (mRandomSortMap.containsKey(senderId)) {
            RandomSortList rsl = mRandomSortMap.get(senderId);
            if (!rsl.isJoinFinished()) {
                this.replyText(replyToken, "表單尚未結束登記, 請說\"結束隨機排序\"來結束登記.");
            }
            if (rsl.getHolder().equals(userId)) {
                String result = rsl.runRandomSortAll();
                this.replyText(replyToken, result);
            }
            else {
                this.replyText(replyToken, "表單只能由發起人\n" + getUserDisplayName(rsl.getHolder()) + "\n做隨機排序");
            }
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟隨機排序");
        }
    }

    private void sortOneRandomSort(String userId, String senderId, String replyToken) {
        if (mRandomSortMap.containsKey(senderId)) {
            RandomSortList rsl = mRandomSortMap.get(senderId);
            if (!rsl.isJoinFinished()) {
                this.replyText(replyToken, "表單尚未結束登記, 請說\"結束隨機排序\"來結束登記.");
            }
            if (rsl.getHolder().equals(userId)) {
                String result = rsl.runRandomSortOne();
                this.replyText(replyToken, result);
            }
            else {
                this.replyText(replyToken, "表單只能由發起人\n" + getUserDisplayName(rsl.getHolder()) + "\n做隨機排序");
            }
        }
        else {
            this.replyText(replyToken, "此群組尚未開啟隨機排序");
        }
    }

    private void checkNeedTotallyBullyReply(String userId, String replyToken) {
        if (mIsTotallyBullyEnable && userId.equals(mTotallyBullyUserId)) {
            this.replyText(replyToken, mTotallyBullyReplyString);
        }
    }

    private void printUserDisplayName(String text, String replyToken) {
        text = text.replace("PgCommand使用者顯示名稱:", "");
        this.replyText(replyToken, "" + getUserDisplayName(text));
    }

    private void printUserDisplayPicture(String text, String replyToken) {
        text = text.replace("PgCommand使用者顯示圖片:", "");
        URI source = getUserDisplayPicture(text);
        this.replyImage(replyToken, source, source);
    }

    private void setDefaultExchanged(String text, String replyToken) {
        text = text.replace("PgCommand設定預設匯率:", "");

        if (text.equals("USD")) {
            mExchangedDefaultText="美金";
            mExchangedDefaultCountry="USD";
        }
        else if (text.equals("JPY")) {
            mExchangedDefaultText="日圓";
            mExchangedDefaultCountry="JPY";
        }
        else if (text.equals("CNY")) {
            mExchangedDefaultText="人民幣";
            mExchangedDefaultCountry="CNY";
        }
        else if (text.equals("EUR")) {
            mExchangedDefaultText="歐元";
            mExchangedDefaultCountry="EUR";
        }
        else if (text.equals("HKD")) {
            mExchangedDefaultText="港幣";
            mExchangedDefaultCountry="HKD";
        }
        else if (text.equals("GBP")) {
            mExchangedDefaultText="英鎊";
            mExchangedDefaultCountry="GBP";
        }
        else if (text.equals("KRW")) {
            mExchangedDefaultText="韓元";
            mExchangedDefaultCountry="KRW";
        }
        else if (text.equals("VND")) {
            mExchangedDefaultText="越南盾";
            mExchangedDefaultCountry="VND";
        }
        else if (text.equals("AUD")) {
            mExchangedDefaultText="澳幣";
            mExchangedDefaultCountry="AUD";
        }
        else if (text.equals("THB")) {
            mExchangedDefaultText="泰銖";
            mExchangedDefaultCountry="THB";
        }
        else if (text.equals("IDR")) {
            mExchangedDefaultText="印尼盾";
            mExchangedDefaultCountry="IDR";
        }
        else if (text.equals("CHF")) {
            mExchangedDefaultText="法郎";
            mExchangedDefaultCountry="CHF";
        }
        else if (text.equals("PHP")) {
            mExchangedDefaultText="披索";
            mExchangedDefaultCountry="PHP";
        }
        else if (text.equals("SGD")) {
            mExchangedDefaultText="新幣";
            mExchangedDefaultCountry="SGD";
        }
        else if (text.equals("金加隆")) {
            mExchangedDefaultText="金加隆";
            mExchangedDefaultCountry="GGL";
        }
        else if (text.equals("GGL")) {
            mExchangedDefaultText="金加隆";
            mExchangedDefaultCountry="GGL";
        }
        else {
            String strResult = "設定失敗! 不可識別的貨幣代號: " + text;
            this.replyText(replyToken, strResult);
            return;
        }

        String strResult = "成功設定預設匯率\n貨幣代號: " + mExchangedDefaultCountry + "\n中文幣名: " + mExchangedDefaultText + "\n感恩 PG 讚美 PG";
        this.replyText(replyToken, strResult);
    }

    private void exchangeDefault(String text, String replyToken) throws IOException {
        text = text.replace("?", "").replace("？", "").trim();
        try {
            if (mExchangedDefaultText.equals("金加隆")) {
                exchangeFromGoldGalleon(text, replyToken);
                return;
            }

            String strResult = text + mExchangedDefaultText;
            String country = mExchangedDefaultCountry;

            double inputNumber = 0.0;
                try {
                    inputNumber = Double.parseDouble(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://www.findrate.tw/"+country+"/?type="+country+"&order=in1";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<td>台幣換")+4, tempParseNumber.indexOf("</table>")); // cut table    
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("\">")+2, tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</td>"));
                
                float rateNumber = 0f;
                
                try {
                    rateNumber = Float.parseFloat(tempParseNumber);
                }
                catch(java.lang.NumberFormatException e2) {
                    return;
                }

                if (rateNumber > 0) {
                    int numResult = (int) ((float)inputNumber * rateNumber);
                    strResult += "換算台幣大概 $" + numResult;
                    this.replyText(replyToken, strResult);
                }
                else {
                    return;
                }
        }catch (IOException e2) {
            throw e2;
        }


    }

    private void exchangeBitcon(String text, String replyToken) throws IOException {
        String numberText = text.substring(0, text.indexOf("比特幣")).trim();
        text = text.replace("比特幣換算", "").replace("?", "").replace("？", "").replace(" ", "").replace(numberText, "").trim();
        PgLog.info(text);
        double number = 1.0;
        try {
            number = Double.parseDouble(numberText);
        }
        catch(java.lang.NumberFormatException e) {
            return;
        }
        try {
            String strResult = text;    
            String country ="";

            if (text.length() >= 1) {

                if (text.endsWith("人民幣")) {
                    country="CNY";
                    text = text.replace("人民幣","").trim();
                }
                else if (text.endsWith("盧比")) {
                    country="INR";
                    text = text.replace("盧比","").trim();
                }
                else if (text.endsWith("日圓") || text.endsWith("日元") || text.endsWith("日幣")) {
                    country="JPY";
                    text = text.replace("日圓","").replace("日元","").replace("日幣","").trim();
                }
                else if (text.endsWith("台幣") || text.endsWith("新台幣")||text.endsWith("臺幣") || text.endsWith("新臺幣")) {
                    country="TWD";
                    text = text.replace("台幣","").replace("新台幣","").trim();
                }
                else if (text.endsWith("歐元")) {
                    country="EUR";
                    text = text.replace("歐元","").trim();
                }
                else if (text.endsWith("美金") || text.endsWith("美元")) {
                    country="USD";
                    text = text.replace("美金","").replace("美元","").trim();
                }
                else if (text.endsWith("英鎊")) {
                    country="GBP";
                    text = text.replace("英鎊","").trim();
                }
                else {
                    text = "";
                }

            }

            PgLog.info("country: " + country);
            if(country.equals("")){
                strResult = "義大利?維大力? \n請輸入 這些幣別：\n人民幣 盧比 日圓 台幣\n歐元 美金 英鎊";
                this.replyText(replyToken, strResult);
                return;
            }
            else{                

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://zt.coinmill.com/BTC_" + country + ".html?BTC=1";

                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<div id=\"currencyBox1\">"), tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("value=\"")+7, tempParseNumber.indexOf("\">\n<a"));
                
                PgLog.info(tempParseNumber);

                float rateNumber = 0f;
                // Pattern pattern = Pattern.compile("[\\d]{1,}\\.[\\d]{1,}");
                // Matcher matcher = pattern.matcher(tempParseNumber);
                // while(matcher.find()){
                //     tempParseNumber = matcher.group();
                // }
                
                try {
                    rateNumber = Float.parseFloat(tempParseNumber);
                }
                catch(java.lang.NumberFormatException e2) {
                    return;
                }

                if (rateNumber > 0) {
                    if (number > 0) {
                        strResult = "" + number + " 比特幣換算" + strResult + "大概 $" + (int)(rateNumber * number);
                        this.replyText(replyToken, strResult);
                        return;
                    }
                    /*int numResult = (int) (rateNumber);
                    strResult = "1 比特幣換算" + strResult + "大概 $" + numResult;
                    this.replyText(replyToken, strResult);*/
                }
                else {
                    return;
                }
            }
            
        } catch (IOException e) {
            PgLog.info(e.toString());
            throw e;
        }
    }

    private void exchangeFromGoldGalleon(String text, String replyToken) throws IOException {
        text = text.replace("金加隆", "").replace("?", "").replace("？", "").trim();
        try {
            int inputNumber = Integer.parseInt(text);
            double goldGallenRate1 = 0.4125;      // 80G    $33
            double goldGallenRate2 = 0.4;         // 425G   $170
            double goldGallenRate3 = 0.319047619; // 900G   $330
            double goldGallenRate4 = 0.274166667; // 2100G  $670
            double goldGallenRate5 = 0.312962963; // 5400G  $1690
            double goldGallenRate6 = 0.274166667; // 12000G $3290
            String strResult = inputNumber + "金加隆換算台幣:\n";
            strResult += "   +80 黃金組大概 $" + (int)(inputNumber * goldGallenRate1) + "\n";
            strResult += "  +425 黃金組大概 $" + (int)(inputNumber * goldGallenRate2) + "\n";
            strResult += "  +900 黃金組大概 $" + (int)(inputNumber * goldGallenRate3) + "\n";
            strResult += " +2100 黃金組大概 $" + (int)(inputNumber * goldGallenRate4) + "\n";
            strResult += " +5400 黃金組大概 $" + (int)(inputNumber * goldGallenRate5) + "\n";
            strResult += "+12000 黃金組大概 $" + (int)(inputNumber * goldGallenRate6) + "\n";
            strResult += "謝謝各位乾爹/乾娘" + EmojiUtils.emojify(":heart:");
            this.replyText(replyToken, strResult);
        }
        catch(java.lang.NumberFormatException e1) {
            return;
        }
    }

    private void exchangeFromIngressCMU(String text, String replyToken) throws IOException {
        text = text.replace("CMU", "").replace("cmu", "").replace("ＣＭＵ", "").replace("?", "").replace("？", "").trim();
        try {
            int inputNumber = Integer.parseInt(text);
            double cmuRate1 = 0.028;     // 2500   CMU  $70
            double cmuRate2 = 0.0242857; // 7000   CMU  $170
            double cmuRate3 = 0.022;     // 15000  CMU  $330
            double cmuRate4 = 0.0209375; // 32000  CMU  $670
            double cmuRate5 = 0.0187778; // 90000  CMU  $1690
            double cmuRate6 = 0.01645;   // 200000 CMU  $3290
            String strResult = inputNumber + " Ingress CMU 換算台幣:\n";
            strResult += "  2,500 CMU 組大概 $" + (int)(inputNumber * cmuRate1) + "\n";
            strResult += "  7,000 CMU 組大概 $" + (int)(inputNumber * cmuRate2) + "\n";
            strResult += " 15,000 CMU 組大概 $" + (int)(inputNumber * cmuRate3) + "\n";
            strResult += " 32,000 CMU 組大概 $" + (int)(inputNumber * cmuRate4) + "\n";
            strResult += " 90,000 CMU 組大概 $" + (int)(inputNumber * cmuRate5) + "\n";
            strResult += "200,000 CMU 組大概 $" + (int)(inputNumber * cmuRate6) + "\n";
            strResult += "謝謝各位乾爹/乾娘" + EmojiUtils.emojify(":heart:");
            this.replyText(replyToken, strResult);
        }
        catch(java.lang.NumberFormatException e1) {
            return;
        }
    }

    private void exchangeToTwd(String text, String replyToken) throws IOException {
        text = text.replace("換算台幣", "").replace("換算臺幣", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);
        try {
            String strResult = text;    
            String country ="";

            if (text.length() >= 3) {

                if (text.endsWith("美金")) {
                    country="USD";
                    text = text.replace("美金","").trim();
                }
                else if (text.endsWith("日圓") || text.endsWith("日幣") ) {
                    country="JPY";
                    text = text.replace("日圓","").replace("日幣", "").trim();
                }
                else if (text.endsWith("人民幣")) {
                    country="CNY";
                    text = text.replace("人民幣","").trim();
                }
                else if (text.endsWith("歐元")) {
                    country="EUR";
                    text = text.replace("歐元","").trim();
                }
                else if (text.endsWith("港幣")) {
                    country="HKD";
                    text = text.replace("港幣","").trim();
                }
                else if (text.endsWith("英鎊")) {
                    country="GBP";
                    text = text.replace("英鎊","").trim();
                }
                else if (text.endsWith("韓元")) {
                    country="KRW";
                    text = text.replace("韓元","").trim();
                }
                else if (text.endsWith("越南盾")) {
                    country="VND";
                    text = text.replace("越南盾","").trim();
                }
                else if (text.endsWith("澳幣")) {
                    country="AUD";
                    text = text.replace("澳幣","").trim();
                }
                else if (text.endsWith("泰銖")) {
                    country="THB";
                    text = text.replace("泰銖","").trim();
                }
                else if (text.endsWith("印尼盾")) {
                    country="IDR";
                    text = text.replace("印尼盾","").trim();
                }
                else if (text.endsWith("法郎")) {
                    country="CHF";
                    text = text.replace("法郎","").trim();
                }
                else if (text.endsWith("披索")) {
                    country="PHP";
                    text = text.replace("披索","").trim();
                }
                else if (text.endsWith("新幣")) {
                    country="SGD";
                    text = text.replace("新幣","").trim();
                }
                else {
                    text = "";
                }

            }

            if(text.equals("")){
                strResult = "義大利?維大力? \n請輸入 這些幣別：\n美金 日圓 人民幣 歐元 \n港幣 英鎊 韓元 越南盾\n澳幣 泰銖 印尼盾 法郎\n披索 新幣";
                this.replyText(replyToken, strResult);
                return;
            }else{

                double inputNumber = 0;
                try {
                    inputNumber = Double.parseDouble(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://www.findrate.tw/"+country+"/?type="+country+"&order=in1";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<td>台幣")+6, tempParseNumber.indexOf("</table>"));
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("\">")+2, tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</td>"));
                
                float rateNumber = 0f;
                
                try {
                    rateNumber = Float.parseFloat(tempParseNumber);
                }
                catch(java.lang.NumberFormatException e2) {
                    return;
                }

                if (rateNumber > 0) {
                    int numResult = (int) ((float)inputNumber * rateNumber);
                    strResult += "換算台幣大概 $" + numResult;
                    this.replyText(replyToken, strResult);
                }
                else {
                    return;
                }

            }
            
        } catch (IOException e) {
            throw e;
        }
    }

        private void exchangeFromTwd(String text, String replyToken) throws IOException {
        text = text.replace("台幣換算", "").replace("臺幣換算", "").replace("?", "").replace("？", "").trim();
        PgLog.info(text);
        try {
            String strResult = text;    
            String country = "";
            String countryText = "";

            if (text.length() >= 3) {

                if (text.endsWith("美金")) {
                    country="USD";
                    countryText="美金";
                    text = text.replace("美金","").trim();
                }
                else if (text.endsWith("日圓") || text.endsWith("日幣") ) {
                    country="JPY";
                    countryText="日圓";
                    text = text.replace("日圓","").replace("日幣", "").trim();
                }
                else if (text.endsWith("人民幣")) {
                    country="CNY";
                    countryText="人民幣";
                    text = text.replace("人民幣","").trim();
                }
                else if (text.endsWith("歐元")) {
                    country="EUR";
                    countryText="歐元";
                    text = text.replace("歐元","").trim();
                }
                else if (text.endsWith("港幣")) {
                    country="HKD";
                    countryText="港幣";
                    text = text.replace("港幣","").trim();
                }
                else if (text.endsWith("英鎊")) {
                    country="GBP";
                    countryText="英鎊";
                    text = text.replace("英鎊","").trim();
                }
                else if (text.endsWith("韓元")) {
                    country="KRW";
                    countryText="韓元";
                    text = text.replace("韓元","").trim();
                }
                else if (text.endsWith("越南盾")) {
                    country="VND";
                    countryText="越南盾";
                    text = text.replace("越南盾","").trim();
                }
                else if (text.endsWith("泰銖")) {
                    country="THB";
                    countryText="泰銖";
                    text = text.replace("泰銖","").trim();
                }
                else if (text.endsWith("印尼盾")) {
                    country="IDR";
                    countryText="印尼盾";
                    text = text.replace("印尼盾","").trim();
                }
                else if (text.endsWith("法郎")) {
                    country="CHF";
                    countryText="法郎";
                    text = text.replace("法郎","").trim();
                }
                else if (text.endsWith("披索")) {
                    country="PHP";
                    countryText="披索";
                    text = text.replace("披索","").trim();
                }
                else if (text.endsWith("新幣")) {
                    country="SGD";
                    countryText="新幣";
                    text = text.replace("新幣","").trim();
                }
                else {
                    text = "";
                }

            }


            if(text.equals("")){
                strResult = "義大利?維大力? \n請輸入 這些幣別：\n美金 日圓 人民幣 歐元 \n港幣 英鎊 韓元 越南盾\n澳幣 泰銖 印尼盾 法郎\n披索 新幣";
                this.replyText(replyToken, strResult);
                return;
            }else{

                double inputNumber = 0;
                try {
                    inputNumber = Double.parseDouble(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://www.findrate.tw/"+country+"/?type="+country+"&order=in1";
                PgLog.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("換台幣</td>")+8, tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("\">")+2, tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</td>"));
                
                float rateNumber = 0f;
                
                try {
                    rateNumber = Float.parseFloat(tempParseNumber);
                }
                catch(java.lang.NumberFormatException e2) {
                    return;
                }

                if (rateNumber > 0) {
                    int numResult = (int) ((float)inputNumber / rateNumber);
                    strResult += "換算大概 " + country + " $" + numResult;
                    strResult = "" + inputNumber + "台幣換算" + countryText + "大概 $" + numResult;
                    this.replyText(replyToken, strResult);
                }
                else {
                    return;
                }

            }
            
        } catch (IOException e) {
            throw e;
        }
    }

    private void tse(String text, String replyToken) throws IOException {
        PgLog.info(text);
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://www.tse.com.tw/api/get.php?method=home_summary";
            PgLog.info(url);
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");
            httpget.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpget.setHeader("Cache-Control", "max-age=0");
            httpget.setHeader("Connection", "keep-alive");
            httpget.setHeader("Host", "mis.twse.com.tw");
            httpget.setHeader("Upgrade-Insecure-Requests", "1");
            httpget.setHeader("User-Agent",
                              "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpget);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            Gson gson = new GsonBuilder().create();
            String content = EntityUtils.toString(response.getEntity(), "utf-8");
            TseStock tseStock = gson.fromJson(content, TseStock.class);
            if (tseStock.getTSE_D() > 0) {
                strResult = "加權 : " + tseStock.getTSE_I() + EmojiUtils.emojify(":chart_with_upwards_trend:") +
                            tseStock.getTSE_D() + EmojiUtils.emojify(":chart_with_upwards_trend:") + tseStock.getTSE_P() +
                            "% \n成交金額(億) : " + tseStock.getTSE_V() + "\n";
            } else {
                strResult = "加權 : " + tseStock.getTSE_I() + EmojiUtils.emojify(":chart_with_downwards_trend:") +
                            tseStock.getTSE_D() + EmojiUtils.emojify(":chart_with_downwards_trend:") + tseStock.getTSE_P() +
                            "% \n成交金額(億) : " + tseStock.getTSE_V() + "\n";
            }
            if (tseStock.getOTC_D() > 0) {
                strResult = strResult + "櫃買 : " + tseStock.getOTC_I() + EmojiUtils.emojify(":chart_with_upwards_trend:") +
                            tseStock.getOTC_D() + EmojiUtils.emojify(":chart_with_upwards_trend:") + tseStock.getOTC_P() +
                            "% \n成交金額(億) : " + tseStock.getOTC_V() + "\n";
            } else {
                strResult = strResult + "櫃買 : " + tseStock.getOTC_I() + EmojiUtils.emojify(":chart_with_downwards_trend:") +
                            tseStock.getOTC_D() + EmojiUtils.emojify(":chart_with_downwards_trend:") + tseStock.getOTC_P() +
                            "% \n成交金額(億) : " + tseStock.getOTC_V() + "\n";
            }

            this.replyText(replyToken, strResult);
        } catch (IOException e) {
            throw e;
        }
    }

    private void help(String text, String replyToken) throws IOException {
        String imageUrl = "https://p1.bqimg.com/524586/f7f88ef91547655cs.png";
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(URI.create(imageUrl),"安安","你好",
                Arrays.asList(
                        new MessageAction("查個股股價","輸入 @2331? 或 @台積電?"),
                        new MessageAction("查加權上櫃指數","輸入 呆股?"),
                        new MessageAction("查匯率","輸入 美金匯率? 或 匯率? 檢視可查匯率"),
                        new PostbackAction("更多","more:1")
                )
        );
        TemplateMessage templateMessage = new TemplateMessage("The function Only on mobile device ! ", buttonsTemplate);
        LineMessagePrimitive.reply(replyToken, templateMessage);
    }

    private void help2(String text, String replyToken) throws IOException {
        String imageUrl = "https://p1.bqimg.com/524586/f7f88ef91547655cs.png";
        CarouselTemplate carouselTemplate = new CarouselTemplate(
                Arrays.asList(
                        new CarouselColumn(URI.create(imageUrl), "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查個股股價", "查個股股價 輸入 @2331? 或 @台積電?"),
                                                   new MessageAction("查加權上櫃指數", "查加權上櫃指數 輸入 呆股?"),
                                                   new MessageAction("查匯率", "查匯率 輸入 美金匯率? 或 匯率? 檢視可查匯率")
                                           )
                        ),
                        new CarouselColumn(URI.create(imageUrl), "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查天氣", "查天氣　輸入 台北市天氣?"),
                                                   new MessageAction("查氣象", "查氣象　輸入 台北市氣象?"),
                                                   new MessageAction("查空氣品質", "查空氣品質　輸入 北部空氣?")
                                           )
                        ),
                        new CarouselColumn(URI.create(imageUrl), "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查油價", "查天氣　輸入 油價?"),
                                                   new MessageAction("查星座", "查氣象　輸入 天蠍座?"),
                                                   new MessageAction("查星座", "查氣象　輸入 牡羊座?")
                                           )
                        )
                )
        );
        TemplateMessage templateMessage = new TemplateMessage("The function Only on mobile device ! ", carouselTemplate);
        LineMessagePrimitive.reply(replyToken, templateMessage);
    }

    private String decodeJandanImageUrl(String input) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="http://jandan.net/ooxx";
            PgLog.info(url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            try {
                // 不敢爬太快 
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String maxPage = "";
            int maxPageInt = 0;

            String jsPath = "";

            jsPath = EntityUtils.toString(httpEntity, "utf-8");

            jsPath = jsPath.substring(jsPath.indexOf("<script src=\"//cdn.jandan.net/static/min/")+13, jsPath.length());
            jsPath = jsPath.substring(0, jsPath.indexOf("\"></script>"));

            jsPath = "http:" + jsPath;
            
            //PgLog.info("Piggy Check js path: " + jsPath);

            httpGet = new HttpGet(jsPath);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36" );
            httpGet.addHeader( "Accept","*/*" );
            httpGet.addHeader( "Accept-Encoding","gzip, deflate" );
            httpGet.addHeader( "Accept-Language","en-US,en;q=0.8" );
            httpGet.addHeader( "Host","cdn.jandan.net" );
            httpGet.addHeader( "Referer","http://jandan.net" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            response = httpClient.execute(httpGet);
            ////PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();

            String js_response = EntityUtils.toString(httpEntity, "utf-8");

            //PgLog.info("Piggy Check js_response: " + js_response);

            String js_x = js_response.substring(js_response.indexOf("f.remove();var c=")+17, js_response.length());
            js_x = js_x.substring(js_x.indexOf("(e,\"")+4, js_x.length());
            js_x = js_x.substring(0, js_x.indexOf("\");"));

            PgLog.info("Piggy Check js_x: " + js_x);

            return decryptJanDanImagePath(input, js_x);


        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void startFetchJanDanGirlImages() {
        if (mIsStartJandanParsing) {
            PgLog.info("Piggy Check isStartJandanParsing");
            return;
        }
        else {
            mIsStartJandanParsing = true;
        }
        mJanDanGirlList.clear();
        mJanDanParseCount = 0;
        mJanDanGifCount = 0;
        mJanDanMaxPage = 0;
        mJanDanProgressingPage = 0;
        String nextPage = getJanDanNextPage("");
        try {
            
            // String maxPage = getJanDanJsPath("max");
            

            // try {
            //     mJanDanMaxPage = Integer.parseInt(maxPage);
            // }
            // catch(java.lang.NumberFormatException e1) {
            //     PgLog.info("NumberFormatException " + e1);
            //     mIsStartJandanParsing = false;
            //     return;
            // }

            //PgLog.info("Piggy Check max page int: " + mJanDanMaxPage);


            PgLog.info("1秒後開始抓取煎蛋妹子圖...");
            while(true) {
                mJanDanProgressingPage++;
                try {
                    // 不敢爬太快 
                    Thread. sleep(1000);
                     // 網頁內容解析
                    new Thread( new JanDanHtmlParser(nextPage)).start();
                    
                } catch (Exception e1) {
                    e1.printStackTrace();
                    break;
                }
                nextPage = getJanDanNextPage(nextPage);
            }


        }catch (Exception e2) {
            e2.printStackTrace();
        }
        mIsStartJandanParsing = false;
        PgLog.info("抓取煎蛋妹子圖 Finished.");
    }

    private String getJanDanJsPath(String target) {
        return getJanDanJsPath(target, "0");
    }

    private String getJanDanNextPage(String current) {

        try {
            String url="";
            if (current.length() == 0) {
                url="http://jandan.net/ooxx/";
            }
            else {
                url="http://jandan.net/ooxx/page-"+current;
            }
            
            CloseableHttpClient httpClient = HttpClients.createDefault();
            
            PgLog.info(url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            
            String xml = EntityUtils.toString(httpEntity, "utf-8");
            xml = xml.substring(xml.indexOf("Older Comments\" href=\"//jandan.net/ooxx/page-")+45, xml.length());
            xml = xml.substring(0, xml.indexOf("#comments\""));


            PgLog.info("Piggy Check next page string: " + xml);
            return xml;
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        PgLog.info("Piggy Check parse next page string fail.");
        return "";
    }

    // target(max or js)
    private String getJanDanJsPath(String target, String page) {

        try {
            String url="http://jandan.net/ooxx/";
            CloseableHttpClient httpClient = HttpClients.createDefault();
            if (target.equals("js")) {
                url += ("page-"+page);
            }
            
            PgLog.info(url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String jsPath = "";
            String maxPage = "";

            if (target.equals("max")) {
                maxPage = EntityUtils.toString(httpEntity, "utf-8");
                maxPage = maxPage.substring(maxPage.indexOf("current-comment-page\">[")+23, maxPage.length());
                maxPage = maxPage.substring(0, maxPage.indexOf("]<"));
                // try {
                //     maxPageInt = Integer.parseInt(maxPage);
                // }
                // catch(java.lang.NumberFormatException e1) {
                //     PgLog.info("NumberFormatException " + e1);
                //     mIsStartJandanParsing = false;
                //     return;
                // }
                PgLog.info("Piggy Check max page string: " + maxPage);
                return maxPage;
            }
            else if (target.equals("js")) {
                jsPath = EntityUtils.toString(httpEntity, "utf-8");
                while (jsPath.contains("<script src=\"//cdn.jandan.net/static/min/")) {
                    jsPath = jsPath.substring(jsPath.indexOf("<script src=\"//cdn.jandan.net/static/min/")+13, jsPath.length());
                }
                jsPath = jsPath.substring(0, jsPath.indexOf("\"></script>"));
                jsPath = "http:" + jsPath;
                PgLog.info("Piggy Check js path: " + jsPath);
                httpGet = new HttpGet(jsPath);
                httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
                httpGet.addHeader( "Accept","*/*" );
                httpGet.addHeader( "Accept-Encoding","gzip, deflate" );
                httpGet.addHeader( "Accept-Language","en-US,en;q=0.8" );
                httpGet.addHeader( "Host","cdn.jandan.net" );
                httpGet.addHeader( "Referer","http://jandan.net" );
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

                response = httpClient.execute(httpGet);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                String js_response = EntityUtils.toString(httpEntity, "utf-8");

                //PgLog.info("Piggy Check js_response: " + js_response);

                String js_x = js_response.substring(js_response.indexOf("f.remove();var c=")+17, js_response.length());
                //PgLog.info("Piggy Check js_x1: " + js_x);
                js_x = js_x.substring(js_x.indexOf("(e,\"")+4, js_x.length());
                //PgLog.info("Piggy Check js_x2: " + js_x);
                js_x = js_x.substring(0, js_x.indexOf("\");"));

                PgLog.info("Piggy Check js_x: " + js_x);
                return js_x;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    
    class PttBeautyGirl {
        private List<String> picUrlList;
        private String resultUrl;
        private String describeString;
        private String titleString;
        private String rankString;
        public PttBeautyGirl(List<String> list, String url, String title, String describe, String rank) {
            picUrlList = list;
            resultUrl = url;
            describeString = describe;
            titleString = title;
            rankString = rank;
        }
        
        public List<String> getPicUrlList() {
            return picUrlList;
        }
        
        public List<String> getLimitedList(int max) {
            return (List<String>) getRandomItemFromList(picUrlList, max);
        }
        
        public String getResultUrl() {
            return resultUrl;
        }
        
        public String getDescribeString() {
            return describeString;
        }
        
        public String getTitle() {
            return titleString;
        }
        
        public String getRank() {
            return rankString;
        }
    }

    private PttBeautyGirl getRandomPttBeautyImageUrl(String userId, String senderId, boolean isHot) {
        try{

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.ptt.cc/bbs/Beauty/index.html";
            
            Random randomGenerator = new Random();
            int random_agent_num = randomGenerator.nextInt(mUserAgentList.size());

            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484; over18=1");
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String maxPage = "";
            int maxPageInt = -1;

            maxPage = EntityUtils.toString(httpEntity, "utf-8");
            maxPage = maxPage.substring(maxPage.indexOf("<a class=\"btn wide\" href=\"/bbs/Beauty/index")+50, maxPage.length());
            maxPage = maxPage.substring(maxPage.indexOf("<a class=\"btn wide\" href=\"/bbs/Beauty/index")+43, maxPage.indexOf(".html"));
            
            try {
                maxPageInt = Integer.parseInt(maxPage);
            }catch(java.lang.NumberFormatException e1) {
                PgLog.info("NumberFormatException " + e1);
            }
            //PgLog.info("Piggy Check maxPageInt: " + maxPageInt);
            
            String result_url = "";
            int tryCount = 10;
            String numberCount = "";
            String resultTitle = "";
            while (tryCount > 0){
                tryCount--;
                // Parse from list page
                int random_num = randomGenerator.nextInt(maxPageInt-1500)+1500;
                random_agent_num = randomGenerator.nextInt(mUserAgentList.size());
                String target_url = "https://www.ptt.cc/bbs/Beauty/index" + random_num + ".html";
                //PgLog.info("Piggy Check target PTT beauty list page: " + target_url);
                httpGet = new HttpGet(target_url);
                httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484; over18=1" );
                httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpGet.setHeader("Cache-Control", "max-age=0");
                httpGet.setHeader("Connection", "keep-alive");


                response = httpClient.execute(httpGet);
                //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                result_url = EntityUtils.toString(httpEntity, "utf-8");
                if (isHot) {
                    if (result_url.indexOf("hl f1\">爆</span>")<0) {
                        PgLog.info("Piggy Check can't find BURST in page: " + random_num);
                        result_url = "";
                        continue;
                    }
                    else {
                        result_url = result_url.substring(result_url.indexOf("hl f1\">爆</span>"), result_url.length());
                        resultTitle = result_url;
                        result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                        result_url = "https://www.ptt.cc" + result_url + ".html";
                        numberCount = "爆";

                        resultTitle = resultTitle.substring(resultTitle.indexOf("<a href=\"")+9, resultTitle.length());
                        resultTitle = resultTitle.substring(resultTitle.indexOf("\">")+2, resultTitle.indexOf("</a>"));
                    }

                    if (result_url.equals("")) {
                        continue;
                    }
                }
                else {
                    
                    Pattern pattern = Pattern.compile("<span class=\"hl f3\">.*?<\\/span>");
                    Matcher matcher = pattern.matcher(result_url);
                    List<String> resultNumberCountList = new ArrayList<String> ();
                    List<String> resultTitleList = new ArrayList<String> ();

                    while(matcher.find()){
                        String result = matcher.group();
                        result = result.substring(result.indexOf("hl f3\">")+7, result.indexOf("</span>"));
                        try {
                            int number = Integer.parseInt(result);
                            if (number >= mPttBeautyRandomCountMin) {
                                resultNumberCountList.add(result);
                            }
                        } catch (java.lang.NumberFormatException e) {
                            PgLog.info("NumberFormatException: " + e);
                            continue;
                        }
                    }

                    // Pick a burst article
                    if (result_url.indexOf("hl f1\">爆</span>")>0) {
                        resultNumberCountList.add("爆");
                    }

                    if (resultNumberCountList.size() > 0) {
                        // get random count number
                        int randomNum = randomGenerator.nextInt(resultNumberCountList.size());
                        numberCount = resultNumberCountList.get(randomNum);

                        // generator result url
                        if (numberCount.equals("爆")) {
                            result_url = result_url.substring(result_url.indexOf("hl f1\">爆</span>"), result_url.length());
                            resultTitle = result_url;

                            result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                            result_url = "https://www.ptt.cc" + result_url + ".html";
                        }
                        else {
                            result_url = result_url.substring(result_url.indexOf("hl f3\">" + numberCount + "</span>"), result_url.length());
                            resultTitle = result_url;

                            result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                            result_url = "https://www.ptt.cc" + result_url + ".html";
                        }

                        resultTitle = resultTitle.substring(resultTitle.indexOf("<a href=\"")+9, resultTitle.length());
                        resultTitle = resultTitle.substring(resultTitle.indexOf("\">")+2, resultTitle.indexOf("</a>"));
                    }
                    else {
                        continue;
                    }
                }

                // Process result save to history table
                //PgLog.info("Piggy Check result_url: " + result_url);
                String rank = (numberCount.equals("爆") ? "爆" : (numberCount + "推"));
                String historyString = resultTitle + "\n\n" + result_url + " " + rank;


                random_agent_num = randomGenerator.nextInt(mUserAgentList.size());

                // Process get image from result url.
                httpGet = new HttpGet(result_url);
                httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
                httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484; over18=1");
                httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpGet.setHeader("Cache-Control", "max-age=0");
                httpGet.setHeader("Connection", "keep-alive");


                response = httpClient.execute(httpGet);
                PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                String result_image_image = EntityUtils.toString(httpEntity, "utf-8");

                result_image_image = result_image_image.substring(0, result_image_image.indexOf("--"));

                List<String> resultImageList = new ArrayList<String> ();

                if (result_image_image.indexOf("http://imgur.com/") > 0) {
                    Pattern patternJp = Pattern.compile("http:\\/\\/imgur.com\\/.*");
                    Matcher matcherJp = patternJp.matcher(result_image_image);
                    while(matcherJp.find()){
                        String result = matcherJp.group();
                        //PgLog.info("Piggy Check Ptt Beauty result: " + result);
                        result = result.replace("http:","https:");
                        result = result.replace("imgur.com","i.imgur.com");
                        if (result.contains("</a>")) {
                            result = result.substring(0, result.indexOf("</a>"));
                        }
                        result = result + ".jpg";
                        //PgLog.info("Piggy Check Ptt Beauty imgur url: " + result_url + " img_link: " + result);
                        if (resultImageList.indexOf(result) == -1) {
                            resultImageList.add(result);
                            //PgLog.info("Piggy Check Ptt Beauty imgur Add!!");
                        }
                        
                    }
                }
                else {
                    Pattern patternJp = Pattern.compile("http.*?:.*?.jp.*?g");
                    Matcher matcherJp = patternJp.matcher(result_image_image);
                    while(matcherJp.find()){
                        String result = matcherJp.group();
                        //PgLog.info("Piggy Check Ptt Beauty url: " + result_url + " img_link: " + result);
                        result = result.replace("http:","https:");
                        if (resultImageList.indexOf(result) == -1) {
                            resultImageList.add(result);
                            //PgLog.info("Piggy Check Ptt Beauty Add!!");
                        }
                    }
                }

                
                if (resultImageList.size() > 0) {
                    //random_num = randomGenerator.nextInt(resultImageList.size());
                    //return resultImageList.get(random_num);
                    PttBeautyGirl girl = new PttBeautyGirl(resultImageList, result_url, resultTitle, historyString, rank);
                    if (userId != null) {
                        mWhoImPickRandomGirlMap.put(userId, girl);
                    }
                    if (senderId != null) {
                        if (!mWhoTheyPickRandomGirlMap.containsKey(senderId)) {
                            PttBeautyHistory history = new PttBeautyHistory();
                            history.addHistory(girl);
                            
                                mWhoTheyPickRandomGirlMap.put(senderId, history);
                            
                        }
                        else {
                            PttBeautyHistory history = mWhoTheyPickRandomGirlMap.get(senderId);
                            history.addHistory(girl);
                            mWhoTheyPickRandomGirlMap.put(senderId, history);
                        }
                    }
                    return girl;
                }
                else {
                    continue;
                }
            }
            
            if (result_url.equals("")) {
                PgLog.info("Piggy Check Ptt Beauty parse fail");
                return null;
            }

            
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;//TODO
    }

    private String getInstagramImageUrl(String userId, String senderId, String target) {
        try {

            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.instagram.com/" + target + "/";
            PgLog.info("getInstagramImageUrl:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            //httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.addHeader("Cookie","ig_did=D45DC129-A40D-4D95-A4AC-174BF54CEEA3; mid=XxVmGQAEAAF9tNYLvLQIVIPl_D-O; csrftoken=BQOSjekM3X65ZZpGkmPDy3TgsPIIXEQS; ds_user_id=408814719; sessionid=408814719%3A3MedSzsyp4lGUt%3A4; shbid=15807; shbts=1598417659.60127; rur=VLL; urlgen=\"{}:1kAxZO:cPqNn67DQjd-ohreHqMB3D81ML8");
            httpGet.setHeader("Accept","text/html");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");
            httpGet.setHeader("Dnt", "1");
            httpGet.setHeader("Sec-Fetch-Dest", "1");
            httpGet.setHeader("Sec-Fetch-Mode", "navigate");
            httpGet.setHeader("Sec-Fetch-Site", "document");
            httpGet.setHeader("Sec-Fetch-User", "?1");
            httpGet.setHeader("Upgrade-Insecure-Requests", "same-origin");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            String html = "";
            int maxPageInt = -1;

            html = EntityUtils.toString(httpEntity, "utf-8");

            if (!html.contains("display_url")) {
                PgLog.info("Piggy Check html: " + html);
                return "N/A";
            }

            List<String> tempImgList = new ArrayList<String> ();
            List<String> tempIgList = new ArrayList<String> ();
            List<String> tempIgLikeCountList = new ArrayList<String> ();

            Pattern pattern = Pattern.compile("display_url\":\".*?\",");
            Matcher matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(14, result.length());
                result = result.substring(0, result.length()-2);
                //PgLog.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempImgList.add(result);
            }


            pattern = Pattern.compile("shortcode\":\".*?\",");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(12, result.length());
                result = result.substring(0, result.length()-2);
                //PgLog.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempIgList.add(result);
            }

            pattern = Pattern.compile("[e][d][g][e][_][l][i][k][e][d][_][b][y][\"][:][{]\"count\":.*?},\"edg");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(24, result.length());
                result = result.substring(0, result.length()-6);
                //PgLog.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempIgLikeCountList.add(result);
            }

            if (tempImgList.size() > 0) {
                random_num = randomGenerator.nextInt(tempImgList.size());

                String result_url = tempImgList.get(random_num);
                String ig_url = "https://www.instagram.com/p/" + tempIgList.get(random_num);
                String like_count = tempIgLikeCountList.get(random_num);
                PgLog.info("Piggy Check ig_url: " + ig_url);
                /*mWhoImPickRandomGirlMap.put(userId, (ig_url + " " + like_count + EmojiUtils.emojify(":heart:")));
                mWhoTheyPickRandomGirlMap.put(senderId, (ig_url + " " + like_count + EmojiUtils.emojify(":heart:")));*/
                return result_url;
            }
            else {
                PgLog.info("Piggy Check parse IG fail!");
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private List<InstagramItem> getRandomInstagramImageUrl(String userId, String senderId, String target, boolean isLocation, boolean isHot) {
        try {

            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url;
            if (isLocation) {
                url = target;
            }
            else {
                url="https://www.instagram.com/explore/tags/" + target + "/";
            }
            PgLog.info("getRandomInstagramImageUrl:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            String html = "";
            int maxPageInt = -1;

            html = EntityUtils.toString(httpEntity, "utf-8");

            if (isHot) {
                html = html.substring(html.indexOf("edge_hashtag_to_top_posts"), html.length());
            }
            if (isLocation) {
                html = html.substring(html.indexOf("edge_location_to_top_post"), html.length());
            }

            List<String> tempImgList = new ArrayList<String> ();
            List<String> tempIgList = new ArrayList<String> ();
            List<String> tempIgLikeCountList = new ArrayList<String> ();
            List<InstagramItem> resultImgList = new ArrayList<InstagramItem> ();

            Pattern pattern = Pattern.compile("display_url\":\".*?\",");
            Matcher matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(14, result.length());
                result = result.substring(0, result.length()-2);
                //PgLog.info("Piggy Check IG " + target + " jpg img_link: " + result);
                if (result.contains("\\u0026")) {
                    result = result.replace("\\u0026","&");
                }
                tempImgList.add(result);
            }


            pattern = Pattern.compile("shortcode\":\".*?\",");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(12, result.length());
                result = result.substring(0, result.length()-2);
                
                tempIgList.add(result);
            }

            pattern = Pattern.compile("[e][d][g][e][_][l][i][k][e][d][_][b][y][\"][:][{]\"count\":.*?},\"edg");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(24, result.length());
                result = result.substring(0, result.length()-6);
                //PgLog.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempIgLikeCountList.add(result);
            }

            if (tempImgList.size() > 0) {
                
                for (int i=0;i<tempImgList.size();i++) {
                    String imgUrl = tempImgList.get(i);
                    String igUrl = "https://www.instagram.com/p/" + tempIgList.get(i);
                    String likeCount = tempIgLikeCountList.get(i);
                    //PgLog.info("Piggy Check imgUrl: " + imgUrl);
                    resultImgList.add(new InstagramItem(imgUrl, igUrl, likeCount));
                }
                
                /*random_num = randomGenerator.nextInt(tempImgList.size());

                String result_url = tempImgList.get(random_num);
                String ig_url = "https://www.instagram.com/p/" + tempIgList.get(random_num);
                String like_count = tempIgLikeCountList.get(random_num);
                PgLog.info("Piggy Check ig_url: " + ig_url);
                mWhoImPickRandomGirlMap.put(userId, (ig_url + " " + like_count + EmojiUtils.emojify(":heart:")));
                mWhoTheyPickRandomGirlMap.put(senderId, (ig_url + " " + like_count + EmojiUtils.emojify(":heart:")));
                return result_url;*/
                return resultImgList;
            }
            else {
                PgLog.info("Piggy Check parse IG fail!");
            }
            
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    class InstagramItem {
        String imgUrl;
        String itemUrl;
        String itemLikeCount;
        public InstagramItem(String img, String url, String like) {
            imgUrl = img;
            itemUrl = url;
            itemLikeCount = like;
        }
        public String getImgUrl() {
            return imgUrl;
        }
        public String getUrl() {
            return itemUrl;
        }
        
        public String getLike() {
            return itemLikeCount;
        }
    }

    private String getRandomPexelsImageUrl(String target) {
        try {

            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.pexels.com/search/" + target;
            PgLog.info("getRandomPexelsImageUrl:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String maxPage = "";
            int maxPageInt = -1;

            maxPage = EntityUtils.toString(httpEntity, "utf-8");
            // maxPage = maxPage.substring(maxPage.indexOf("</span> <a href=\"/search/\"" + target), maxPage.length());

            // maxPage = maxPage.substring(maxPage.indexOf("</span> <a href=\"/search/\"" + target)+25+target.length()+7, maxPage.length());
            
            // maxPage = maxPage.substring(0, maxPage.indexOf("\">"));

            Pattern pattern = Pattern.compile("page=[\\d]{1,}\">([\\d]{1,})<\\/a> <a class=\"next_page\"");
            Matcher matcher = pattern.matcher(maxPage);
            while(matcher.find()){
                maxPage = matcher.group();
                //PgLog.info("Piggy Check matcher: " + maxPage);
                maxPage = maxPage.substring(5, maxPage.length());
                maxPage = maxPage.substring(0, maxPage.indexOf("\">"));
            }

            try {
                maxPageInt = Integer.parseInt(maxPage);
            }
            catch(java.lang.NumberFormatException e1) {
                PgLog.info("NumberFormatException");
            }
            PgLog.info("Piggy Check maxPageInt: " + maxPageInt);

            if (maxPageInt > 0) {
                random_num = randomGenerator.nextInt(maxPageInt);
            }

            if (maxPageInt > 0) {
                httpGet = new HttpGet("https://www.pexels.com/search/"+target+"/?page=" + random_num);
            }
            else {
                httpGet = new HttpGet("https://www.pexels.com/search/"+target);
            }
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            response = httpClient.execute(httpGet);
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();
            String html = EntityUtils.toString(httpEntity, "utf-8");

            List<String> tempList = new ArrayList<String> ();

            pattern = Pattern.compile("<img srcset=\".*?h=");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(13, result.length());
                result = result.substring(0, result.length()-3);
                result = result.substring(0, result.indexOf("?"));
                //PgLog.info("Piggy Check Pexel " + target + " jpg img_link: " + result);
                tempList.add(result);
            }

            if (tempList.size() > 0) {
                random_num = randomGenerator.nextInt(tempList.size());

                String result_url = tempList.get(random_num);
                PgLog.info("Piggy Check result_url: " + result_url);
                return result_url;
            }
            else {
                PgLog.info("Piggy Check parse fail!");
            }
            
            



        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public class PexelHtmlParser implements Runnable {

        private String html;
        private String target;
        private int page;

        public PexelHtmlParser(String html, int page, String tr) {
            this.html = html;
            this.page = page;
            this.target = tr;
        }

        @Override
        public  void run() {
            System.out.println("Downliading Pexel target: " + target + " Page: " + page);
            
            html = html.substring(html.indexOf("commentlist"));
            
            Pattern patternJpg = Pattern.compile("<img srcset=\".*?.jpg?");
            Pattern patternJpeg = Pattern.compile("<img srcset=\".*?.jpeg?");
            Matcher matcherJpg = patternJpg.matcher(html);
            Matcher matcherJpeg = patternJpeg.matcher(html);
            while(matcherJpg.find()){
                String result = matcherJpg.group();
                result = result.substring(13, result.length());
                PgLog.info("Piggy Check Pexel " + target + " img_link: " + result);
            }
            while(matcherJpeg.find()){
                String result = matcherJpeg.group();
                result = result.substring(13, result.length());
                PgLog.info("Piggy Check Pexel " + target + " img_link: " + result);
            }
        }
    }

    public class JanDanHtmlParser implements Runnable {

        private String html;
        private String js;
        private String page;

        public JanDanHtmlParser(String page) {
            this.page = page;
        }

        @Override
        public  void run() {
            try {
                System.out.println("Downliading Jandan Page: " + page);

                RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD). setConnectionRequestTimeout(6000).setConnectTimeout(6000 ).build();
                CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).build();

                // 發送請求，並執行 
                HttpGet httpGet = new HttpGet( "http://jandan.net/ooxx/page-" + page);
                httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
                CloseableHttpResponse response = httpClient.execute(httpGet);
                InputStream in = response.getEntity().getContent();
                String html = Utils.convertStreamToString(in);
                
                html = html.substring(html.indexOf("list-style-type"));
                
                Pattern pattern = Pattern.compile("class=\"img-hash\">.*?</span>");
                Matcher matcher = pattern.matcher(html);

                String js_x = "";
                    
                if (!mLastWorkableJsX.equals("")) {
                    js_x = mLastWorkableJsX;
                }
                else {
                    js_x = getJanDanJsPath("js", page);
                }

                if (js_x.equals("")){
                    PgLog.info("Backup jandan js_x is null and parse js_x failed. Drop this page.");
                    return;
                }

                while(matcher.find()){
                    String result = matcher.group();
                    result = result.substring(result.indexOf("class=\"img-hash\">")+17, result.length());
                    result = result.substring(0, result.indexOf("</span>"));

                    String result_final = decryptJanDanImagePath(result,js_x);
                    mJanDanParseCount++;
                    // PgLog.info("Piggy Check img_link: " + result_final);
                    result_final.replaceAll(" ", "");
                    if (!result_final.endsWith(".jpg")&&!result_final.endsWith(".png")&&!result_final.endsWith(".jpeg")&&!result_final.endsWith(".gif")){
                        PgLog.info("Parse error? result_final: " + result_final);
                        if (!mLastWorkableJsX.equals("")&&!js_x.equals(mLastWorkableJsX)) {
                            // Workaround, try last workable js_x and decrypt again.
                            PgLog.info("Try backup js_x: " + mLastWorkableJsX);
                            js_x = mLastWorkableJsX;
                            result_final = decryptJanDanImagePath(result,js_x);
                            if (!result_final.endsWith(".jpg")&&!result_final.endsWith(".png")&&!result_final.endsWith(".jpeg")&&!result_final.endsWith(".gif")){
                                PgLog.info("Still Parse error? result_final: " + result_final);
                                mLastWorkableJsX = "";
                            }
                            else {
                                if (!result_final.endsWith(".gif")) {
                                    // Filter out gif
                                    mJanDanGirlList.add(result_final);
                                }
                                else {
                                    mJanDanGifCount++;
                                }
                            }
                        }
                    }
                    else {
                        mLastWorkableJsX = js_x;
                        if (!result_final.endsWith(".gif")) {
                            // Filter out gif
                            mJanDanGirlList.add(result_final);
                        }
                        else {
                            mJanDanGifCount++;
                        }
                    }
                    
                        
                }
            }catch (Exception e2) {
                e2.printStackTrace();
            }
                
        }    
    }

    private String decryptJanDanImagePath(String n, String x) {
        int g = 4;
        x = toHexString(md5(getUtf8String(x)));
        String w = toHexString(md5(getUtf8String(x.substring(0, 16))));
        String u = toHexString(md5(getUtf8String(x.substring(16,32))));
                
        String t = n.substring(0, g);
        String r = w + toHexString(md5(getUtf8String(w+t)));
        
        n = n.substring(4, n.length());     
        while(n.length() % 4 != 0) {
            n += "=";
        }
        
        //byte[] temp_m = Base64.decode(n, Base64.DEFAULT);

        Base64.Decoder decoder = Base64.getDecoder();

        byte[] temp_m = decoder.decode(n);

        char[] m = new char[temp_m.length];
        for (int i=0;i<temp_m.length;i++) {
            m[i] = (char)(temp_m[i] & 0xFF);
        }
        
        char[] h = new char[256];
        char[] q = new char[256];
        for (int i=0;i<h.length;i++) {
            h[i] = (char)i;
        }
        
        byte r_ord[] = r.getBytes();
        for (int i=0;i<q.length;i++) {
            q[i] = (char) r_ord[i%64];
        }
        
        int o = 0;
        for (int i=0;i<q.length;i++) {
            o = (o + h[i] + q[i]) & 0xFF;
            char temp = h[o];
            h[o] = h[i];
            h[i] = temp;
        }
        
        String l = "";
        int v = 0;
        o = 0;
        
        for (int i=0;i<m.length;i++) {
            v = (v + 1) & 0xFF;
            o = (o + h[v]) & 0xFF;
            
            char temp = h[o];
            h[o] = h[v];
            h[v] = temp;
            l += (char) ((char)(m[i] & 0xFF) ^ h[(h[v]+h[o])& 0xFF]);
        }
        l = l.substring(26);
        if (!l.startsWith("http:")) {
            l = "http:" + l;
        }
        return l;
    }
    
    public String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
    
    private String getUtf8String(String input) {
        byte ptext[] = input.getBytes();
        try {
            return new String(ptext, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return "";
    }
    
    private byte[] md5(String input) {
        byte[] barr = {};
        try {
            MessageDigest md=MessageDigest.getInstance("MD5");
            barr=md.digest(input.getBytes());   
            
            String md5String = "";
            StringBuffer sb=new StringBuffer();  //將 byte 陣列轉成 16 進制
            for (int i=0; i < barr.length; i++) {
                sb.append(byte2Hex(barr[i]));
            }
            String hex=sb.toString();
            md5String=hex.toUpperCase(); //一律轉成大寫
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return barr;
    }
    
    public String byte2Hex(byte b) {
        String[] h={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
        int i=b;
        if (i < 0) {i += 256;}
        return h[i/16] + h[i%16];
    }

    /*public static HttpClient getHttpsClient() throws Exception {

        if (client != null) {
            return client;
        }
        SSLContext sslcontext = getSSLContext();
        SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslcontext,
                SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        client = HttpClients.custom().setSSLSocketFactory(factory).build();

        return client;
    }*/

    private void dumpClassMethod(Class c) {
        for (Method method : c.getDeclaredMethods()) {
            PgLog.info("Method name: " + method.getName());
        }
    }

    private String getGroupSourcePrivateString(Source source, String name) {
        PgLog.info("getGroupSourcePrivateString: " + source + " name: " + name);
        try {
            Field field = GroupSource.class.getDeclaredField(name);
            PgLog.info("field: " + field);
            field.setAccessible(true);
            Object value = field.get((GroupSource)source);
            PgLog.info("value: " + value);
            return (String) value;
        } catch(Exception e) {
            PgLog.info("Exception: " + e);
            return "";
        }
    }

    private String getUserSourcePrivateString(Source source, String name) {
        try {
            Field field = UserSource.class.getDeclaredField(name);
            field.setAccessible(true);
            Object value = field.get((UserSource)source);
            return (String) value;
        } catch(Exception e) {
            PgLog.info("Exception: " + e);
            return "";
        }
    }

    private boolean isAdminUserId(String userId, String replyToken) {

        if (!userId.equals(USER_ID_PIGGY) && !userId.equals(USER_ID_TEST_MASTER) ) {
            this.replyText(replyToken, "你以為你是偉大的 PG 大人嗎？\n\n滾！！！");
            return false;
        }
        return true;
    }

    private boolean isAdminUserId(String userId) {

        if (!userId.equals(USER_ID_PIGGY) && !userId.equals(USER_ID_TEST_MASTER) ) {
            return false;
        }
        return true;
    }

    private String getRandomSourceFromList(List<String> list) {
        Random randomGenerator = new Random();
        int random_num = randomGenerator.nextInt(list.size());
        String source = list.get(random_num);
        return source;
    }

    public class NewestEarthquakeTimeCheckThread extends Thread {
        public void run(){
            while (true) {
                try {
                    Thread.sleep(3000);
                    checkEarthquakeReport();
                } catch (Exception e) {
                    PgLog.info("NewestEarthquakeTimeCheckThread e: " + e);
                }
            }
            
        }
    }

    private NewestEarthquakeTimeCheckThread mEarthquakeCheckThread = null;
    String mNewestEarthquakeTime = "";
    String mNewestEarthquakeReportText = "";
    String mNewestEarthquakeReportImage = "";

    private void checkEarthquakeReport() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://www.cwb.gov.tw/V8/C/E/MOD/EQ_ROW.html");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            String newestEarthquakeTime = strResult.substring(strResult.indexOf("<span>")+6,strResult.indexOf("</span>"));
            if (newestEarthquakeTime.contains("<i class=")) {
                newestEarthquakeTime = newestEarthquakeTime.substring(0, newestEarthquakeTime.indexOf("<i class="));
            }
            //PgLog.info("Newest earth quake time: " + newestEarthquakeTime);
            
            String targetReport = "https://www.cwb.gov.tw";
            targetReport += strResult.substring(strResult.indexOf("<a href=\"")+9,strResult.indexOf("\" aria-label="));

            mNewestEarthquakeReportText = "\n";


            httpget = new HttpGet(targetReport);
            response = httpClient.execute(httpget);
            httpEntity = response.getEntity();
            String tempContext = EntityUtils.toString(httpEntity, "utf-8");

            tempContext = tempContext.substring(tempContext.indexOf("yellow-dot-title\">")+18, tempContext.length());
            mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</")) + "\n"; // Title

            tempContext = tempContext.substring(tempContext.indexOf("fa fa-clock-o\"></i>")+19, tempContext.length());
            mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</li>")) + "\n\n"; // Time

            tempContext = tempContext.substring(tempContext.indexOf("<span>")+6, tempContext.length());
            mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</span>")) + "\n\n"; // Location

            tempContext = tempContext.substring(tempContext.indexOf("icon-earthquake-depth\"></i>")+27, tempContext.length());
            mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</li>")) + "\n"; // Depth

            tempContext = tempContext.substring(tempContext.indexOf("icon-earthquake-scale\"></i>")+27, tempContext.length());
            mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</li>")) + "\n"; // Scale
            mNewestEarthquakeReportText += "\n各地震度級:\n";
            
            int affectCount = 0;
            int maxLevel = 0;
            while (tempContext.contains("href=\"#collapse")) {
                affectCount++;
                tempContext = tempContext.substring(tempContext.indexOf("href=\"#collapse")+15, tempContext.length());
                tempContext = tempContext.substring(tempContext.indexOf("\">")+2, tempContext.length());
                String detailText = tempContext.substring(0, tempContext.indexOf("</a>"));
                mNewestEarthquakeReportText +=  detailText + "\n"; // Scale per location
                String numberString = detailText.substring(detailText.indexOf(" ")+1, detailText.indexOf("級"));
                if (maxLevel == 0) {
                    try {
                        maxLevel = Integer.parseInt(numberString);
                    }
                    catch(java.lang.NumberFormatException e1) {
                        PgLog.info("NumberFormatException e1: " + e1);
                    }
                }
            }
            if (tempContext.contains("\">詳細資料")) {
                tempContext = tempContext.substring(tempContext.indexOf("\">詳細資料"), tempContext.length());
                tempContext = tempContext.substring(tempContext.indexOf("title=\"點此下載"), tempContext.length());
                tempContext = tempContext.substring(tempContext.indexOf("href=\"")+6, tempContext.indexOf("\">"));    
            }
            else {
                tempContext = tempContext.substring(tempContext.indexOf("title=\"點此下載"), tempContext.length());
                tempContext = tempContext.substring(tempContext.indexOf("href=\"")+6, tempContext.indexOf("\">"));
            }
            
            mNewestEarthquakeReportImage = "https://www.cwb.gov.tw";
            mNewestEarthquakeReportImage += tempContext;
            
            if (!mNewestEarthquakeTime.equals("") && !mNewestEarthquakeTime.equals(newestEarthquakeTime)) {
                notifyAllNeedEarthquakeEventRoom();
                PgLog.info("Piggy Check: maxLevel: " + maxLevel + " affectCount: " + affectCount);
                if (affectCount >= 6 && maxLevel > 3) {
                    // Notify all group when maxLevel over 4 and affect count over 6
                    if (!AnnouncementManager.isSameAnnounceMessage(mNewestEarthquakeReportImage)) {
                        AnnouncementManager.announceNewMessage(mNewestEarthquakeReportImage, true, 30);
                        LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, "觸發通知所有群組地震訊息, 時間區間為 30 分鐘內");
                    }
                }
            }
            mNewestEarthquakeTime = newestEarthquakeTime;
        } catch (Exception e) {
            PgLog.info("checkEarthquakeReport e: " + e);
        }
    }

    String mNewestDgpaReportTime = "";
    String mNorthAreaReportText = "";
    String mMiddleAreaReportText = "";
    String mSouthAreaReportText = "";
    String mEastAreaReportText = "";
    String mSeaAreaReportText = "";

    public class NewestDgpaReportCheckThread extends Thread {
        public void run(){
            while (true) {
                try {
                    Thread.sleep(5000);
                    checkNeedToWorkOrSchoolReport();
                } catch (Exception e) {
                    PgLog.info("NewestEarthquakeTimeCheckThread e: " + e);
                }
            }
            
        }
    }

    private NewestDgpaReportCheckThread mNewestDgpaReportCheckThread = null;

    private String getDgpaReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mNorthAreaReportText + "\n";
        result += mMiddleAreaReportText + "\n";
        result += mSouthAreaReportText + "\n";
        result += mEastAreaReportText + "\n";
        result += mSeaAreaReportText;
        return result;
    }

    private String getDgpaNorthReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mNorthAreaReportText;
        return result;
    }

    private String getDgpaMiddleReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mMiddleAreaReportText;
        return result;
    }

    private String getDgpaSouthReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mSouthAreaReportText;
        return result;
    }

    private String getDgpaEastReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mEastAreaReportText;
        return result;
    }

    private String getDgpaSeaReportText() {
        String result = mNewestDgpaReportTime + "\n";
        result += mSeaAreaReportText;
        return result;
    }

    private void checkNeedToWorkOrSchoolReport() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://www.dgpa.gov.tw/typh/daily/nds.html");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            String newestDgpaReportTime = strResult.substring(strResult.indexOf("更新時間："), strResult.length());
            newestDgpaReportTime = newestDgpaReportTime.substring(0, newestDgpaReportTime.indexOf("<br/>"));
            newestDgpaReportTime = newestDgpaReportTime.substring(0, newestDgpaReportTime.indexOf("\r"));
            
            //PgLog.info("Newest DGPA time: " + newestDgpaReportTime);
            
            String dgpaTableBody = strResult.substring(strResult.indexOf("<TBODY class=\"Table_Body\">"), strResult.length());

            String northArea = dgpaTableBody.substring(dgpaTableBody.indexOf("<FONT>北部地區</FONT>")+17, dgpaTableBody.indexOf("<FONT>中部地區</FONT>"));
            String middleArea = dgpaTableBody.substring(dgpaTableBody.indexOf("<FONT>中部地區</FONT>")+17, dgpaTableBody.indexOf("<FONT>南部地區</FONT>"));
            String southArea = dgpaTableBody.substring(dgpaTableBody.indexOf("<FONT>南部地區</FONT>")+17, dgpaTableBody.indexOf("<FONT>東部地區</FONT>"));
            String eastArea = dgpaTableBody.substring(dgpaTableBody.indexOf("<FONT>東部地區</FONT>")+17, dgpaTableBody.indexOf("<FONT>外島地區</FONT>"));
            String seaArea = dgpaTableBody.substring(dgpaTableBody.indexOf("<FONT>外島地區</FONT>")+17, dgpaTableBody.indexOf("備註："));

            mNorthAreaReportText = EmojiUtils.emojify(":moyai:") + "北部地區\n" + getDgpaTableElementString(northArea);
            mMiddleAreaReportText = EmojiUtils.emojify(":moyai:") + "中部地區\n" + getDgpaTableElementString(middleArea);
            mSouthAreaReportText = EmojiUtils.emojify(":moyai:") + "南部地區\n" + getDgpaTableElementString(southArea);
            mEastAreaReportText = EmojiUtils.emojify(":moyai:") + "東部地區\n" + getDgpaTableElementString(eastArea);
            mSeaAreaReportText = EmojiUtils.emojify(":moyai:") + "外島地區\n" + getDgpaTableElementString(seaArea);
            /*if (!mNewestDgpaReportTime.equals("") && !mNewestDgpaReportTime.equals(newestDgpaReportTime)) {
                notifyAllNeedDgpaEventRoom();
            }*/
            mNewestDgpaReportTime = newestDgpaReportTime;

        } catch (Exception e) {
            //PgLog.info("checkNeedToWorkOrSchoolReport e: " + e);
        }
    }

    private String getDgpaTableElementString(String text) {
        String result = "";
        while(text.indexOf("</TR>") > 0) {
            String tempTrString = text.substring(0, text.indexOf("</TR>"));
            result += EmojiUtils.emojify(":office:") + tempTrString.substring(tempTrString.indexOf("<FONT>")+6, tempTrString.indexOf("</FONT>")) + " ";
            tempTrString = tempTrString.substring(tempTrString.indexOf("</FONT>")+7, tempTrString.length());

            while (tempTrString.indexOf("<FONT color=") > 0) {
                tempTrString = tempTrString.substring(tempTrString.indexOf("<FONT color="), tempTrString.length());
                if (tempTrString.startsWith("<FONT color=#FF0000 >")) {
                    result += EmojiUtils.emojify(":exclamation:");
                }
                result += tempTrString.substring(tempTrString.indexOf("<FONT color=")+21, tempTrString.indexOf("</FONT>"));
                tempTrString = tempTrString.substring(tempTrString.indexOf("</FONT>"), tempTrString.length());
                result += "\n";
            }
            text = text.substring(text.indexOf("</TR>")+5, text.length());
        }
        return result;
    }

    public class NewestIngressCheckThread extends Thread {
        public void run(){
            while (true) {
                try {
                    Thread.sleep(5000);
                    if (isIngressTwitterUpdated()) {
                        notifyAllNeedIngressTwitterEventRoom();
                    }
                } catch (Exception e) {
                    PgLog.info("NewestIngressCheckThread e: " + e);
                }
            }
        }
    }

    private NewestIngressCheckThread mIngressCheckThread = null;

    private String mNewestIngressTwitterTime = "";

    private boolean isIngressTwitterUpdated() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://twitter.com/ingress");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            String result = strResult.substring(strResult.indexOf("data-time-ms=\"")+14,strResult.indexOf("data-time-ms=\"")+27);

            boolean isNeedUpdate = false;
            if (!mNewestIngressTwitterTime.equals("") && !mNewestIngressTwitterTime.equals(result)) {
                isNeedUpdate = true;
            }

            mNewestIngressTwitterTime = result;
            return isNeedUpdate;
        } catch (Exception e) {
            PgLog.info("checkEarthquakeReport e: " + e);
        }
        return false;

    }
    private String getIngressNewestTwitter() {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://twitter.com/ingress");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            mNewestIngressTwitterTime = strResult.substring(strResult.indexOf("data-time-ms=\"")+14,strResult.indexOf("data-time-ms=\"")+27);
            
            PgLog.info("Newest ingress twitter time: " + mNewestIngressTwitterTime);

            strResult = strResult.substring(strResult.indexOf("<small class=\"time\">")+20,strResult.length());

            strResult = strResult.substring(strResult.indexOf("<a href=\"")+9,strResult.length());

            String twitterUrl = "https://twitter.com" + strResult.substring(0, strResult.indexOf("\""));

            PgLog.info("Newest ingress twitter Url: " + twitterUrl);

            strResult = strResult.substring(strResult.indexOf("title=\"")+7,strResult.length());

            String titleTime = strResult.substring(0, strResult.indexOf("\""));

            PgLog.info("Newest ingress twitter time: " + titleTime);

            strResult = strResult.substring(strResult.indexOf("data-aria-label-part=\"0\">")+25,strResult.length());

            String twitterContext = strResult.substring(0, strResult.indexOf("</p>"));

            PgLog.info("Newest ingress twitter context: " + twitterContext);

            String result = "Ingress Newest Twitter\n";

            result += ("" + titleTime + "\n");
            result += ("\n" + twitterContext + "\n");
            result += ("\n" + twitterUrl);

            return result;


        } catch (Exception e) {
            PgLog.info("checkEarthquakeReport e: " + e);
        }
        return "抓取 Ingress Twitter 失敗";
    }

    private List<String> mEarthquakeEventRoomList = new ArrayList<String> (
        Arrays.asList(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM)
    );

    private void notifyAllNeedEarthquakeEventRoom() {
        for (String room : mEarthquakeEventRoomList){
            LineNotify.callEvent(room, mNewestEarthquakeReportText);
            LineNotify.callEvent(room, " ", mNewestEarthquakeReportImage);
        }        
    }

    private List<String> mDgpaEventRoomList = new ArrayList<String> (
        Arrays.asList(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM)
    );

    private void notifyAllNeedDgpaEventRoom() {
        for (String room : mDgpaEventRoomList){
            LineNotify.callEvent(room, getDgpaReportText());
        }        
    }

    private List<String> mIngressTwitterEventRoonList = new ArrayList<String> (
        Arrays.asList(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM)
    );
    private void notifyAllNeedIngressTwitterEventRoom() {
        for (String room : mIngressTwitterEventRoonList){
            LineNotify.callEvent(room, getIngressNewestTwitter());
        }
    }

    private ArrayList<ArrayList> mAurorWaitingList = new ArrayList<ArrayList>();
    private ArrayList<ArrayList> mAnimalWaitingList = new ArrayList<ArrayList>();
    private ArrayList<ArrayList> mProfessorWaitingList = new ArrayList<ArrayList>();
    private int MAX_WIZARD_LEVEL = 20;
    private boolean isWizardWaitingListInited = false;
    private void initWizardWaitingList() {
        if (!isWizardWaitingListInited) {
            int count = MAX_WIZARD_LEVEL;
            while(count > 0) {
                // each index means Level
                mAurorWaitingList.add(new ArrayList<String>());
                mAnimalWaitingList.add(new ArrayList<String>());
                mProfessorWaitingList.add(new ArrayList<String>());
                count--;
            }
            isWizardWaitingListInited = true;
        }
    }

    private boolean isAddWizardWaitingTextValid(String text) {
        text = text.replace(" ", "").replace("　", "").trim();
        text = text.replace("等", "").replace("求組", "");
        text = text.replace("正氣", "").replace("魔動", "").replace("教授", "");
        long number = -1;
        try {
            number = Long.parseLong(text);
        } catch (java.lang.NumberFormatException e) {
            return false;
        }
        return true;
    }

    private synchronized String processAddToWizardWaitingList(String userId, String text) {
        String result = "";
        text = text.replace(" ", "").replace("　", "").trim();
        text = text.replace("等", "");
        text = text.replace("正氣", "正氣PG").replace("魔動", "魔動PG").replace("教授", "教授PG");
        initWizardWaitingList();
        while(text.indexOf("PG") > 0) {
            String temp = text.substring(0,text.indexOf("PG"));
            if (temp.indexOf("正氣") > 0) {
                String levelString = text.substring(0,text.indexOf("正氣"));
                int level = -1;
                try {
                    level = Integer.parseInt(levelString);
                    if (level > MAX_WIZARD_LEVEL) {
                        return "";
                    }
                    ArrayList<String> list = mAurorWaitingList.get(level);
                    list.add(userId);
                    result += "" + level + " 等正氣師";
                } catch (java.lang.NumberFormatException e) {
                    return "NumberFormatException";
                }
            }
            else if (temp.indexOf("魔動") > 0) {
                String levelString = text.substring(0,text.indexOf("魔動"));
                int level = -1;
                try {
                    level = Integer.parseInt(levelString);
                    if (level > MAX_WIZARD_LEVEL) {
                        return "";
                    }
                    ArrayList<String> list = mAnimalWaitingList.get(level);
                    list.add(userId);
                    result += "" + level + " 等魔法動物學家";
                } catch (java.lang.NumberFormatException e) {
                    return "NumberFormatException";
                }

            }
            else if (temp.indexOf("教授") > 0) {
                String levelString = text.substring(0,text.indexOf("教授"));
                int level = -1;
                try {
                    level = Integer.parseInt(levelString);
                    if (level > MAX_WIZARD_LEVEL) {
                        return "";
                    }
                    ArrayList<String> list = mProfessorWaitingList.get(level);
                    list.add(userId);
                    result += "" + level + " 等教授";
                } catch (java.lang.NumberFormatException e) {
                    return "NumberFormatException";
                }
            }
            text = text.substring(text.indexOf("PG")+2, text.length());
            result += "\n";
        }
        return result;
    }

    private synchronized String processRemoveFromWizardWaitingList(String userId) {
        String result = "";
        initWizardWaitingList();
        // Auror
        int level = MAX_WIZARD_LEVEL - 1;
        while (level > 0) {
            ArrayList<String> list = mAurorWaitingList.get(level);
            if (!list.isEmpty()) {
                Iterator<String> i = list.iterator();
                while (i.hasNext()) {
                    String user = i.next();
                    if (user.equals(userId)) {
                        result += "" + level + " 等正氣師\n";
                        i.remove();
                    }
                }
            }
            level--;
        }

        // Animal
        level = MAX_WIZARD_LEVEL - 1;
        while (level > 0) {
            ArrayList<String> list = mAnimalWaitingList.get(level);
            if (!list.isEmpty()) {
                Iterator<String> i = list.iterator();
                while (i.hasNext()) {
                    String user = i.next();
                    if (user.equals(userId)) {
                        result += "" + level + " 等魔法動物學家\n";
                        i.remove();
                    }
                }
            }
            level--;
        }

        // Professor
        level = MAX_WIZARD_LEVEL - 1;
        while (level > 0) {
            ArrayList<String> list = mProfessorWaitingList.get(level);
            if (!list.isEmpty()) {
                Iterator<String> i = list.iterator();
                while (i.hasNext()) {
                    String user = i.next();
                    if (user.equals(userId)) {
                        result += "" + level + " 等教授\n";
                        i.remove();
                    }
                }
            }
            level--;
        }
        return result;
    }

    private void processDumpAurorWaitingList(String replyToken) {
        String result = "";
        initWizardWaitingList();
        result = getWaitingListDumpString(mAurorWaitingList);
        this.replyText(replyToken, "正氣師求組清單:\n" + result);
    }

    private void processDumpAnimalWaitingList(String replyToken) {
        String result = "";
        initWizardWaitingList();
        result = getWaitingListDumpString(mAnimalWaitingList);
        this.replyText(replyToken, "魔法動物學家求組清單:\n" + result);
    }

    private void processDumpProfessorWaitingList(String replyToken) {
        String result = "";
        initWizardWaitingList();
        result = getWaitingListDumpString(mProfessorWaitingList);
        this.replyText(replyToken, "教授求組清單:\n" + result);
    }

    private String getWaitingListDumpString(ArrayList<ArrayList> al) {
        String result = "";
        int level = MAX_WIZARD_LEVEL - 1;
        while (level > 0) {
            ArrayList<String> list = al.get(level);
            if (!list.isEmpty()) {
                result += "" + level + " 等:\n";
                for (String user : list) {
                    result += getUserDisplayName(user) + "\n";
                }
                result += "-----\n";
            }
            level--;
        }
        return result;
    }

    int origin_confirm = -1;
    int origin_heal = -1;
    int origin_dead = -1;
    int origin_inspection = -1;
    int origin_exclude = -1;
    private String getChinaVirusTaiwanData() {
        String result = "";
        String url = "https://covid19dashboard.cdc.gov.tw/dash3";
        try {
            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());
            CloseableHttpClient httpClient = HttpClients.createDefault();
            PgLog.info("getChinaVirusTaiwanData:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            //httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","application/json, text/plain, */*");
            httpGet.setHeader("Origin","https://www.cdc.gov.tw");
            httpGet.setHeader("Sec-Fetch-Site","same-site");
            httpGet.setHeader("Sec-Fetch-Mode","cors");
            httpGet.setHeader("Sec-Fetch-Dest","empty");
            httpGet.setHeader("Referer", "https://www.cdc.gov.tw/");
            httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7");
            httpGet.setHeader("Connection", "keep-alive");
            httpGet.setHeader("DNT", "1");

            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity httpEntity = response.getEntity();

            String data = EntityUtils.toString(httpEntity, "utf-8");
            data = data.replace("{","").replace("}","").replace("\"0\":", "").trim();

            String confirmString = data.substring(data.indexOf("\"確診\":")+5, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String healString = data.substring(data.indexOf("\"解除隔離\":")+7, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String deadString = data.substring(data.indexOf("\"死亡\":")+5, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String inspectionString = data.substring(data.indexOf("\"送驗\":")+5, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String excludeString = data.substring(data.indexOf("\"排除\":")+5, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String yesterday_confirmString = data.substring(data.indexOf("\"昨日確診\":")+7, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String yesterday_excludeString = data.substring(data.indexOf("\"昨日排除\":")+7, data.indexOf(",\""));
            data = data.substring(data.indexOf(",\"")+1, data.length());

            String yesterday_inspectionString = data.substring(data.indexOf("\"昨日送驗\":")+7, data.length());

            confirmString = confirmString.replace("\"", "").replace(",", "");
            healString = healString.replace("\"", "").replace(",", "");
            deadString = deadString.replace("\"", "").replace(",", "");
            inspectionString = inspectionString.replace("\"", "").replace(",", "");
            excludeString = excludeString.replace("\"", "").replace(",", "");
            yesterday_confirmString = yesterday_confirmString.replace("\"", "").replace(",", "");
            yesterday_excludeString = yesterday_excludeString.replace("\"", "").replace(",", "");
            yesterday_inspectionString = yesterday_inspectionString.replace("\"", "").replace(",", "");

            int confirm = -1;
            int heal = -1;
            int dead = -1;
            int inspection = -1;
            int exclude = -1;
            int yesterday_confirm = -1;
            int yesterday_exclude = -1;
            int yesterday_inspection = -1;

            try {
                confirm = Integer.parseInt(confirmString);
                heal = Integer.parseInt(healString);
                dead = Integer.parseInt(deadString);
                inspection = Integer.parseInt(inspectionString);
                exclude = Integer.parseInt(excludeString);
                yesterday_confirm = Integer.parseInt(yesterday_confirmString);
                yesterday_exclude = Integer.parseInt(yesterday_excludeString);
                yesterday_inspection = Integer.parseInt(yesterday_inspectionString);

            } catch (java.lang.NumberFormatException e) {
                e.printStackTrace();
                return "";
            }

            if (origin_confirm == -1) {
                origin_confirm = confirm;
            }
            if (origin_heal == -1) {
                origin_heal = heal;
            }
            if (origin_dead == -1) {
                origin_dead = dead;
            }
            if (origin_inspection == -1) {
                origin_inspection = inspection;
            }
            if (origin_exclude == -1) {
                origin_exclude = exclude;
            }
            result += EmojiUtils.emojify(":bomb:") + "確診: " + confirm + (origin_confirm>0&&origin_confirm!=confirm? ("(+" + (confirm-origin_confirm) + ")" ): "") + "\n";
            result += EmojiUtils.emojify(":pill:") + "痊癒: "+heal+(origin_heal>0&&origin_heal!=heal? ("(+" + (heal-origin_heal) + ")" ): "") + "\n";
            result += EmojiUtils.emojify(":syringe:") + "治療中: "+ (confirm-heal-dead) + "\n";
            result += EmojiUtils.emojify(":skull:") + "死亡: "+dead+(origin_dead>0&&origin_dead!=dead? ("(+" + (dead-origin_dead) + ")" ): "") + "\n";

            result +="\n";

            result += EmojiUtils.emojify(":microscope:") + "送驗: "+inspection+(origin_inspection>0&&origin_inspection!=inspection? ("(+" + (inspection-origin_inspection) + ")" ): "") + "\n";
            result += EmojiUtils.emojify(":mag:") + "檢驗中: "+ (inspection-exclude) + "\n";
            result += EmojiUtils.emojify(":ok_hand:") + "排除: "+exclude+(origin_exclude>0&&origin_exclude!=exclude? ("(+" + (exclude-origin_exclude) + ")" ): "") + "\n";
            
            result +="\n";

            result += EmojiUtils.emojify(":calendar:") + EmojiUtils.emojify(":bomb:") + "昨日確診: "+yesterday_confirm+"\n";
            result += EmojiUtils.emojify(":calendar:") + EmojiUtils.emojify(":microscope:") + "昨日送驗: "+yesterday_inspection+"\n";
            result += EmojiUtils.emojify(":calendar:") + EmojiUtils.emojify(":ok_hand:") + "昨日排除: "+yesterday_exclude;

            result += "\n\n資料來源:衛福部疾管署";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return result;
    }

    private AvWikiInfo getAvWiki(String data) {

        // parser exception, XD has only one result, ignore it.
        data = data.toUpperCase();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String website = "http://av-wiki.net/?s="+data;
            HttpGet httpget = new HttpGet(website);
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            if (!strResult.contains(">1件<")) {
                return null;
            }
            //video title
            String tempString = strResult;
            String title = "Unknown";
            while(strResult.contains("title=\"")){
                tempString = tempString.substring(tempString.indexOf("title=\"")+7, tempString.length());
                if (!tempString.startsWith("AV女優の名前が知りたい")&&!tempString.startsWith("RSD")) {
                    title = tempString.substring(0, tempString.indexOf("\""));
                    break;
                }
                tempString = tempString.substring(7, tempString.length());
            }

            // video serial code
            tempString = strResult.substring(strResult.indexOf("<li>品番")+7, strResult.length());
            String code = tempString.substring(0, tempString.indexOf("</li>"));

            if (!code.toUpperCase().equals(data)) {
                return null;
            }

            // video artists
            String artists = "";
            tempString = strResult.substring(strResult.indexOf("AV女優名：")+6, strResult.length());
            tempString = tempString.substring(0, tempString.indexOf("</span>"));

            while(tempString.contains("rel=\"tag\">")){
                tempString = tempString.substring(tempString.indexOf("rel=\"tag\">")+10, tempString.length());
                artists += tempString.substring(0, tempString.indexOf("</a>")) + "\n";
                tempString = tempString.substring(tempString.indexOf("</a>"), tempString.length());
            }
            // video start date
            String date = "";
            tempString = strResult.substring(strResult.indexOf("配信開始日：")+6, strResult.length());
            date = tempString.substring(0, tempString.indexOf("</li>"));
            
            //PgLog.info("Newest DGPA time: " + newestDgpaReportTime);

            // Cover imgage

            String img = "";
            tempString = strResult.substring(strResult.indexOf("loading=\"lazy\" src=\"")+20, strResult.length());
            img = tempString.substring(0, tempString.indexOf(".jpg\"")+5);

            ArrayList<String> imgs = new ArrayList<String> ();


            if (strResult.contains("\"luminous\">")) {
                tempString = strResult.substring(strResult.indexOf("\"luminous\">")+11, strResult.length());
                while (strResult.contains("https://pics.dmm.co.jp/digital/video/")) {
                    String result = tempString.substring(tempString.indexOf("https://pics.dmm.co.jp/digital/video/"), tempString.indexOf(".jpg\"")+5);
                    PgLog.info("Piggy Check result: " + result);
                    imgs.add(result);
                    tempString = tempString.substring(tempString.indexOf(".jpg\"")+5, tempString.length());
                }
            }
            AvWikiInfo info = new AvWikiInfo(website, title, code, date, artists, img, imgs);

            return info;

        } catch (Exception e) {
            //PgLog.info("checkNeedToWorkOrSchoolReport e: " + e);
        }

        return null;
    }

    private String checkAvWiki(String data) {

        // parser exception, XD has only one result, ignore it.
        data = data.toUpperCase();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String website = "http://av-wiki.net/?s="+data;
            HttpGet httpget = new HttpGet(website);
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            if (!strResult.contains(">1件<")) {
                return null;
            }
            //video title
            String tempString = strResult;
            String title = "Unknown";
            while(strResult.contains("title=\"")){
                tempString = tempString.substring(tempString.indexOf("title=\"")+7, tempString.length());
                if (!tempString.startsWith("AV女優の名前が知りたい")&&!tempString.startsWith("RSD")) {
                    title = tempString.substring(0, tempString.indexOf("\""));
                    break;
                }
                tempString = tempString.substring(7, tempString.length());
            }

            // video serial code
            tempString = strResult.substring(strResult.indexOf("<li>品番")+7, strResult.length());
            String code = tempString.substring(0, tempString.indexOf("</li>"));

            if (!code.toUpperCase().equals(data)) {
                return null;
            }

            // video artists
            String artists = "";
            tempString = strResult.substring(strResult.indexOf("AV女優名：")+6, strResult.length());
            tempString = tempString.substring(0, tempString.indexOf("</span>"));

            while(tempString.contains("rel=\"tag\">")){
                tempString = tempString.substring(tempString.indexOf("rel=\"tag\">")+10, tempString.length());
                artists += tempString.substring(0, tempString.indexOf("</a>")) + "\n";
                tempString = tempString.substring(tempString.indexOf("</a>"), tempString.length());
            }
            // video start date
            String date = "";
            tempString = strResult.substring(strResult.indexOf("配信開始日：")+6, strResult.length());
            date = tempString.substring(0, tempString.indexOf("</li>"));
            
            //PgLog.info("Newest DGPA time: " + newestDgpaReportTime);

            String result = "";

            result += (website + "\n\n");

            result += ("片名: " + title + "\n\n");

            result += ("番號: " + code + "\n\n");

            result += ("配信開始日: " + date + "\n\n");

            result += ("女優: " + artists + "\n\n");

            return result;

        } catch (Exception e) {
            //PgLog.info("checkNeedToWorkOrSchoolReport e: " + e);
        }

        return null;
    }


    private String getFeatureListString(String userId, boolean isAdmin) {
        String result = "功能指令集\n\n";
        if(isAdminUserId(userId) && isAdmin) {
            result = "管理員功能指令集\n\n";
            result += "PgCommand關閉全功能\n";
            result += "PgCommand開啟全功能\n";
            result += "PgCommand新增吃什麼:Ｘ\n";
            result += "PgCommand新增吃什麼:Ｘ\n";
            result += "PgCommand刪除吃什麼:Ｘ\n";
            result += "PgCommand清空吃什麼\n";
            result += "PgCommand列出吃什麼\n";
            result += "PgCommand煎蛋進度\n";
            result += "PgCommand煎蛋數量\n";
            result += "PgCommand煎蛋解碼:X (undecode string\n";
            result += "PgCommand煎蛋解碼圖:X (undecode string\n";
            result += "PgCommand圖片:X (image path\n";
            result += "PgCommand開始煎蛋\n";
            result += "PgCommand新增隨機地點:X\n";
            result += "PgCommand刪除隨機地點:X\n";
            result += "PgCommand清空隨機地點\n";
            result += "PgCommand列出隨機地點\n";
            result += "PgCommand新增隨機動作:X\n";
            result += "PgCommand刪除隨機動作:X\n";
            result += "PgCommand清空隨機動作\n";
            result += "PgCommand列出隨機動作\n";
            result += "PgCommand設定預設匯率:JPY\n";
            result += "PgCommand開始徹底霸凌\n";
            result += "PgCommand停止徹底霸凌\n";
            result += "PgCommand設定徹底霸凌對象:userId\n";
            result += "PgCommand設定徹底霸凌字串:X\n";
            result += "PgCommand設定代理管理員:X\n";
            result += "PgCommand使用者顯示名稱:X\n";
            result += "PgCommand使用者顯示圖片:X\n";
            result += "PgCommandNotifyMessage:X\n";
            result += "PgCommandNotifyImage:X\n";
            result += "PgCommand設定MD地圖:X\n";
            result += "PgCommand開啟生日快樂廣告\n";
            result += "PgCommand關閉生日快樂廣告\n";
            result += "霸凌模式:https:xxxxxx.jpg\n";
            result += "霸凌不好\n";
            result += "PgCommand表特最小推數設定值\n";
            result += "PgCommand表特最小推數設定為X\n";
            result += "PgCommand最新地震報告圖網址\n";
            result += "PgCommand發公告:XXX\n";
            return result;
        }

        result += "奴隸滾\n";
        result += "Ｘ天氣？（Ｘ需為地區\n";
        result += "Ｘ氣象？（Ｘ需為地區\n";
        result += "Ｘ座？（Ｘ需為星座\n";
        result += "Ｘ座運勢？（Ｘ需為星座\n";
        result += "Ｘ空氣？（Ｘ需為地區\n";
        result += "Ｘ匯率？（Ｘ需為幣名\n";
        result += "比特幣換算？\n";
        result += "ＸＹ換算台幣？（Ｘ需為數字Ｙ需為幣名\n";
        result += "Ｘ金加隆？（Ｘ需為數字 以最優惠包計算\n";
        result += "呆股？\n";
        result += "每日一句？\n";
        result += "今日我最美？\n";
        result += "今日我最美是誰？\n";
        result += "吃什麼？\n";
        result += "抽 （抽 PTT 表特）\n";
        result += "抽IG: （抽 IG 公開帳號）\n";
        result += "爆抽 （抽 PTT 表特爆文）\n";
        result += "抽Ｘ（抽 IG X tag 最新）\n";
        result += "熱抽Ｘ（抽 IG X tag 熱門）\n";
        result += "我剛抽了誰?\n";
        result += "他剛抽了誰?\n";
        /*result += "抽Ｘ（為英文抽 Pexel）\n";*/
        result += "*蛙*哪*\n";
        result += "開始猜拳\n";
        result += "結束猜拳\n";
        result += "參加猜拳\n";
        result += "開始隨機排序\n";
        result += "結束隨機排序\n";
        result += "參加隨機排序\n";
        result += "全部隨機排\n";
        result += "隨機排序抽\n";
        result += "天氣雲圖?\n";
        result += "累積雨量圖?\n";
        result += "雷達回波圖?\n";
        result += "溫度分佈圖?\n";
        result += "紫外線圖?\n";
        result += "許願:X\n";
        result += "投稿:X\n";
        result += "最新地震報告圖\n";
        result += "最新地震報告\n";
        result += "隨機取圖:https:xxxxxx\n";
        result += "年號:X (X 限制為兩個字\n";
        result += "XX幾歲?\n";
        result += "民國XX幾歲?\n";
        result += "XX幾台?(麻將)\n";
        result += "XX人口？(國家)\n";
        result += "開表單：XXX\n";
        result += "XX的平假名?\n";
        result += "XX的片假名?\n";
        result += "XX的羅馬拼音?\n";
        result += "X站? (X 限制為捷運站名\n";
        result += "北部停班停課?\n";
        result += "中部停班停課?\n";
        result += "南部停班停課?\n";
        result += "東部停班停課?\n";
        result += "離島停班停課?\n";
        result += "我的LineId\n";
        result += "我的Line群組Id\n";
        result += "Ingress Twitter\n";
        return result;
    }
    private String getWizardFeatureListString() {
        String result = "巫師功能指令集\n\n";
        result += "Ｘ金加隆？\n";
        result += "X等正氣求組\n";
        result += "例如: 10等正氣求組\n";
        result += "X等魔動求組\n";
        result += "例如: 8等魔動求組\n";
        result += "X等教授求組\n";
        result += "例如: 7等教授求組\n";
        result += "取消登記 或 取消\n";
        result += "正氣求組清單 或 正氣?\n";
        result += "魔動求組清單 或 魔動?\n";
        result += "教授求組清單 或 教授?\n";
        return result;
    }
    void testPttBeautyGirl(String replyToken, String url) {
        Random randomGenerator = new Random();
        int random_num = randomGenerator.nextInt(mUserAgentList.size());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        PgLog.info("getChinaVirusTaiwanData:" + url);
        HttpGet httpGet = new HttpGet(url);
        // Process get image from result url.
        httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent",mUserAgentList.get(0));
        httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484; over18=1");
        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
        httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
        httpGet.setHeader("Cache-Control", "max-age=0");
        httpGet.setHeader("Connection", "keep-alive");


        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            
            
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
    
            String result_image_image = EntityUtils.toString(httpEntity, "utf-8");
    
            result_image_image = result_image_image.substring(0, result_image_image.indexOf("--"));
    
            List<String> resultImageList = new ArrayList<String> ();
    
            if (result_image_image.indexOf("http://imgur.com/") > 0) {
                Pattern patternJp = Pattern.compile("http:\\/\\/imgur.com\\/.*");
                Matcher matcherJp = patternJp.matcher(result_image_image);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    //PgLog.info("Piggy Check Ptt Beauty result: " + result);
                    result = result.replace("http:","https:");
                    result = result.replace("imgur.com","i.imgur.com");
                    if (result.contains("</a>")) {
                        result = result.substring(0, result.indexOf("</a>"));
                    }
                    result = result + ".jpg";
                    //PgLog.info("Piggy Check Ptt Beauty imgur url: " + result_url + " img_link: " + result);
                    if (resultImageList.indexOf(result) == -1) {
                        resultImageList.add(result);
                        //PgLog.info("Piggy Check Ptt Beauty imgur Add!!");
                    }
                    
                }
            }
            else {
                Pattern patternJp = Pattern.compile("http.*?:.*?.jp.*?g");
                Matcher matcherJp = patternJp.matcher(result_image_image);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    //PgLog.info("Piggy Check Ptt Beauty url: " + result_url + " img_link: " + result);
                    if (resultImageList.indexOf(result) == -1) {
                        resultImageList.add(result);
                        //PgLog.info("Piggy Check Ptt Beauty Add!!");
                    }
                }
            }
    
            
            if (resultImageList.size() > 0) {
                //random_num = randomGenerator.nextInt(resultImageList.size());
                //return resultImageList.get(random_num);
                PttBeautyGirl girl = new PttBeautyGirl(resultImageList, url, "Test", "Test", "rank");
                processReplyPttBeautyGirl(replyToken, girl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void test(String replyToken) {
        URI imageUrl = createUri("/i.imgur.com/guYQMo8.jpg");
        ImageCarouselTemplate imageCarouselTemplate = new ImageCarouselTemplate(
                Arrays.asList(
                        new ImageCarouselColumn(imageUrl,
                                                new URIAction("Goto line.me",
                                                              URI.create("https://line.me"), null)
                        ),
                        new ImageCarouselColumn(imageUrl,
                                                new MessageAction("Say message",
                                                                  "Rice=米")
                        ),
                        new ImageCarouselColumn(imageUrl,
                                                new PostbackAction("言 hello2",
                                                                   "hello こんにちは",
                                                                   "hello こんにちは")
                        )
                ));
        TemplateMessage templateMessage = new TemplateMessage("ImageCarousel alt text",
                                                              imageCarouselTemplate);
        LineMessagePrimitive.reply(replyToken, templateMessage);
    }
    
    private static URI createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .path(path).build()
                                          .toUri();
    }
    
    public static TimeZone DEFAULT_SERVER_TIME_ZONE = TimeZone.getTimeZone("GMT+08:00");
    public static long getTimeInMillis(int year, int month, int day, int hours, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(DEFAULT_SERVER_TIME_ZONE);
        calendar.set(year, month - 1, day, hours, minutes, seconds);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        format.setTimeZone(DEFAULT_SERVER_TIME_ZONE);
        try {
            return format.parse(format.format(calendar.getTime())).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    public String getUSPresidentVoie2020() {
        Random randomGenerator = new Random();
        String url = "https://graphics.thomsonreuters.com/2020-US-elex/20201103/20201103-summary_zh.json?cache=1604456972612";
        int random_num = randomGenerator.nextInt(mUserAgentList.size());
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //PgLog.info("getUSPresidentVoie2020:" + url);
        HttpGet httpGet = new HttpGet(url);
        // Process get image from result url.
        httpGet = new HttpGet(url);
        httpGet.addHeader("User-Agent",mUserAgentList.get(0));
        httpGet.addHeader("Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484; over18=1");
        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
        httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
        httpGet.setHeader("Cache-Control", "max-age=0");
        httpGet.setHeader("Connection", "keep-alive");
        
        CloseableHttpResponse response;
        int biden = 0;
        int trump = 0;
        int notYet = 0;
        try {
            response = httpClient.execute(httpGet);
            
            
            //PgLog.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
    
            String resultData = EntityUtils.toString(httpEntity, "utf-8");
            
            resultData = resultData.substring(resultData.indexOf("balanceVals"), resultData.length());
            String rWin = resultData.substring(resultData.indexOf("\"r_win\":")+8, resultData.indexOf(",\"d_win\":"));
            String dWin = resultData.substring(resultData.indexOf("\"d_win\":")+8, resultData.indexOf(",\"o_win\":"));
            String noResult = resultData.substring(resultData.indexOf("\"no_result\":")+12, resultData.indexOf(",\"r_gain\":"));
            try {
                trump = Integer.parseInt(rWin);
                biden = Integer.parseInt(dWin);
                notYet = Integer.parseInt(noResult);
            } catch (java.lang.NumberFormatException e) {
            }
    
            
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
        String result = "270 to win.\n";
        String bidenString = "拜登: ";
        
        bidenString+=biden;
        bidenString+=" ";
        if (270-biden > 0) {
            bidenString+="("+(270-biden)+" to win)";
        }
        else {
            bidenString+=EmojiUtils.emojify(":trophy:");
        }
        
        String trumpString = "川普: ";
        trumpString+=trump;
        trumpString+=" ";
        if (270-trump > 0) {
            trumpString+="("+(270-trump)+" to win)";
        }
        else {
            trumpString+=EmojiUtils.emojify(":trophy:");
        }
        
        if (biden > trump) {
            result += (bidenString + "\n" + trumpString);
        }
        else {
            result += (trumpString + "\n" + bidenString);
        }
        result+="\n未開: " + notYet;
        //PgLog.info("result: " + result);
        
        return result;
    }
    
    List<?> getRandomItemFromList(List<? extends Object> list, int max) {
        if (list != null && list.size() <= max) {
            return list;
        }
        List<Object> resultList = new ArrayList<Object>(max);
        Set<Integer> pickedSet = new HashSet<Integer>();
        int count = 0;
        Random randomGenerator = new Random();
        while(count < max) {
            int random_num = randomGenerator.nextInt(max);
            if (!pickedSet.contains(random_num)) {
                pickedSet.add(random_num);
                count++;
            }
        }
        
        Iterator iterator = pickedSet.iterator(); 
        
        while (iterator.hasNext()){
            resultList.add(list.get(((Integer)iterator.next()).intValue()));
        }
        
        return resultList;
    }
    
    private Customsearch search;

    public void initGoogleCSE() {
        if (search == null) {
            search = new Customsearch.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                .setApplicationName("PiggyTestGoogleSearch").build();
        }
    }
    
    private Customsearch getCustomsearch() {
        return new Customsearch(new NetHttpTransport(), new JacksonFactory(), null);
    }
    
    String API_KEY = "AIzaSyBWpknMcS02RhR42Gp5g7odK5hQLdJqK-A";
    String CX_INSTAGRAM_LOCATION = "e2c451811ef6e4847";
    
    List<InstagramLocation> searchIgLocation(String query, int numOfResults) throws IOException {
        initGoogleCSE();
        List<Result> results = new ArrayList<Result>();
        List<InstagramLocation> igLocationResults = new ArrayList<InstagramLocation>();
        Customsearch.Cse.List list = search.cse().list(query);

        list.setKey(API_KEY);
        list.setC2coff("1");
        list.setCx(CX_INSTAGRAM_LOCATION);

        //Exact terms
        for(long i = 1 ; i < numOfResults ; i+=10){
            list.setStart(i);
            List<Result> temp = list.execute().getItems();
            if (temp != null) {
                results.addAll(temp);
            }
            else {
                PgLog.info("Piggy Check result null");
                return null;
            }
        }
        PgLog.info("Piggy Check result size " + results.size());
        for (int i=0;i<results.size();i++) {
            Result r = results.get(i);
            String title = r.getTitle();
            String link = r.getLink();
            if (!link.contains("/.../")) {
                InstagramLocation il = new InstagramLocation(link, title);
                igLocationResults.add(il);
            }
        }
        return igLocationResults;
    }
    
    class InstagramLocation {
        String ig_url = "";
        String ig_title = "";
        public InstagramLocation(String url, String title) {
            ig_url = url;
            ig_title = title;
        }
        public String getUrl() {
            return ig_url;
        }
        public String getTitle() {
            return ig_title;
        }
        public String toString() {
            return ig_title + ": " + ig_url;
        }
    }
    
    /*List<Result> googleSearchImage(String query) throws IOException {
        try {
            Customsearch customsearch = getCustomsearch();
            Customsearch.Cse.List list = customsearch.cse().list(query);
            list.setKey(API_KEY);
            list.setCx
            list.setSearchType("image");
            list.setImgSize("xlarge");
            list.setImgType("photo");

            Search results = list.execute();
            List<Image> images = new ArrayList<Image>();

            for (Result result : results.getItems()) {
                Image image = new Image();
                image.setSource("google");
                image.setUrl(result.getLink());
                image.setTitle(result.getTitle());
                images.add(image);
            }
            return images;

        } catch (IOException e) {
            throw new ImageSearchException(e.getMessage(), e.getCause());
        }
    }*/

    


    public static String getUserDisplayName(String userid) {
        String strResult="";
        
        UserProfileResponse userProfileResponse = LineMessagePrimitive.getUserProfile(userid);
        if (userProfileResponse == null) {
            return "";
        }
        strResult = userProfileResponse.getDisplayName();
        
        return strResult;
    }
    public static URI getUserDisplayPicture(String userid) {
        URI strResult=null;
        
        UserProfileResponse userProfileResponse = LineMessagePrimitive.getUserProfile(userid);
        if (userProfileResponse == null) {
            return null;
        }
        strResult = userProfileResponse.getPictureUrl();
        
        return strResult;
    }
    
    private BeautyFightingProcessor mBeautyFightingProcessor = new BeautyFightingProcessor() {
        
        @Override
        public boolean sendTextReply(String replyToken, String text) {
            replyText(replyToken, text);
            return true;
        }
        
        @Override
        public boolean sendImageReply(String replyToken, String leftPic, String rightPic, int rounds) {
            List<ImageCarouselColumn> columnsList = new ArrayList<>();
            columnsList.add(getBeautyFightingImagePostbackColumn(leftPic, BeautyFightingInfo.LEFT));
            columnsList.add(getBeautyFightingImagePostbackColumn(rightPic, BeautyFightingInfo.RIGHT));
            //replyText(replyToken, "leftPic: "+leftPic+" rightPic:"+rightPic);
            LineMessagePrimitive.replyImageCarouselTemplate(replyToken, "表特對決第 " + rounds + " 回合", columnsList);
            return true;
        }
        
        @Override
        public PttBeautyGirl getPttBeautyData() {
            PttBeautyGirl data = null;
            try {
                data = getRandomPttBeautyGirl(null, null, null, false, MULTI_PIC);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }
    };
}
