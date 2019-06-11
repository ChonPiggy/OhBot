package ohbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
//import com.linecorp.bot.model.message.template.ImageCarouselColumn;
//import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.model.event.source.*;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import emoji4j.EmojiUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ohbot.aqiObj.AqiResult;
import ohbot.aqiObj.Datum;
import ohbot.stockObj.*;
import ohbot.utils.Utils;
import java.lang.reflect.*;

import java.util.concurrent.CompletableFuture;
import java.lang.reflect.Method;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
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

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.Date;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Random;

import java.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.lang.Integer;

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

    private List<String> mIWillBeLateList = new ArrayList<String> (
        Arrays.asList("https://i.imgur.com/0cNbr9c.jpg",
                      "https://i.imgur.com/XBV3bP6.jpg"));

    private String IMAGE_TAIWAN_WEATHER_CLOUD = "https://www.cwb.gov.tw/V7/observe/satellite/Data/cloud_weather.png";
    private String IMAGE_TAIWAN_WEATHER_RAIN = "https://www.cwb.gov.tw/V7/observe/rainfall/Data/hk.jpg";
    private String IMAGE_TAIWAN_WEATHER_INFRARED_CLOUD = "https://www.cwb.gov.tw/V7/observe/satellite/Data/s1p/s1p.jpg";
    private String IMAGE_TAIWAN_WEATHER_RADAR_ECHO = "https://www.cwb.gov.tw/V7/observe/radar/Data/HD_Radar/CV1_1000.png";
    private String IMAGE_TAIWAN_WEATHER_TEMPERATURE = "https://www.cwb.gov.tw/V7/observe/temperature/Data/temp.jpg";
    private String IMAGE_TAIWAN_WEATHER_ULTRAVIOLET_LIGHT = "https://www.cwb.gov.tw/V7/observe/UVI/Data/UVI.png";

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
    private HashMap<String, String> mWhoImPickRandomGirlMap = new HashMap<>(); // userId, webLink
    private HashMap<String, String> mWhoTheyPickRandomGirlMap = new HashMap<>(); // senderId, webLink
    private HashMap<String, Integer> mTokyoHotRandomGirlLimitationList = new HashMap<>(); // userId, count
    
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

    private HashMap<String, SheetList> mSheetListMap = new HashMap<>(); 
    

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
        
        apiResponse = lineMessagingClient.pushMessage(pushMessage);
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
                log.info(url);
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
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + stock +
                      ".tw&_=" + Instant.now().toEpochMilli();
                log.info(url);
                httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(url);
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
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                strResult = "";

                Gson gson = new GsonBuilder().create();
                Screener screener = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"),Screener.class);
                url="https://news.money-link.com.tw/yahoo/0061_"+stock+".html";
                httpget = new HttpGet(url);
                log.info(url);
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
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(url);
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(url);
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
                    log.info(aqiStyle);
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
                String url="http://m.findrate.tw/"+country+"/";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
    
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        strResult = userProfileResponse.getDisplayName() + "\n" + userProfileResponse.getPictureUrl();
    
        return strResult;
    }

    public String getUserDisplayName(String userid) {
        String strResult="";
        
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        if (userProfileResponse == null) {
            return "";
        }
        strResult = userProfileResponse.getDisplayName();
        
        return strResult;
    }

    public String getUserDisplayPicture(String userid) {
        String strResult="";
        
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        if (userProfileResponse == null) {
            return "";
        }
        strResult = userProfileResponse.getPictureUrl();
        
        return strResult;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws IOException {
        handleTextContent(event.getReplyToken(), event, event.getMessage());
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {

        String text = content.getText();
        log.info(text);
        Source source = event.getSource();
        String senderId = source.getSenderId();
        String userId = source.getUserId();
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

        if (isDisableBot && !isAdminUserId(userId)) {
            return;
        }
        log.info("source: " + source + " name: " + getUserDisplayName(userId) + " text: " + text);

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

        /*if (mIngressCheckThread == null) {
            mIngressCheckThread = new NewestIngressCheckThread();
            mIngressCheckThread.start();
        }*/


        if (replyUserId(userId, senderId, replyToken)) {
            return;
        }
        // log.info("senderId: " + senderId);
        // log.info("userId: " + userId);
        if (UserSource.class.isInstance(source)) {
            // log.info("UserSource.class");
            // log.info("userId: " + userId);
        }
        if (RoomSource.class.isInstance(source)) {
            // log.info("RoomSource.class");
            //String roomId = source.getSenderId();
            // log.info("roomId: " + roomId);
            // log.info("userId: " + userId);
        }
        if (GroupSource.class.isInstance(source)) {
            // log.info("GroupSource.class");
            //String groupId = source.getGroupId();
            // log.info("groupId: " + groupId);
            // log.info("senderId: " + senderId);
            // log.info("userId: " + userId);
        }
        if (UnknownSource.class.isInstance(source)) {
            log.info("UnknownSource.class");
        }

        if (mJanDanGirlList.size() == 0 && !mIsStartJandanStarted) {
            //mIsStartJandanStarted = true;
            //startFetchJanDanGirlImages();
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
                instagramTarget(userId, senderId, mRandomFootIgTargetString, replyToken, false);
                return;
            }
        }

        if ((text.startsWith("抽") || text.startsWith("熱抽") || text.startsWith("爆抽")) && text.length() > 1) {
            if(text.replace("抽", "").replace("爆", "").replace(" ", "").trim().equals("")) {
                boolean isHot = text.startsWith("爆抽");
                randomPttBeautyGirl(userId, senderId, replyToken, isHot);
            }
            else {
                boolean isHot = text.startsWith("熱抽");
                text = text.trim().replace("熱抽", "").replace("抽", "").replace(" ", "");

                instagramTarget(userId, senderId, text, replyToken, isHot);
                /*if (isStringIncludeChinese(text)) {
                    instagramTarget(text, replyToken);
                }
                else if (isStringIncludeEnglish(text)) {
                    pexelsTarget(text, replyToken);    
                }*/
            }
        }
        else if (text.equals("抽")) {
            randomPttBeautyGirl(userId, senderId, replyToken, false);
            //randomGirl(text, replyToken);
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

        if (text.startsWith("比特幣換算") && (text.endsWith("？") || text.endsWith("?"))) {
            exchangeBitcon(text, replyToken);
        }

        if (text.endsWith("換算台幣?") || text.endsWith("換算台幣？")||text.endsWith("換算臺幣?") || text.endsWith("換算臺幣？")) {
            exchangeToTwd(text, replyToken);
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

        if (text.equals("紅外線雲圖?") || text.equals("紅外線雲圖？")) {
            replyTaiwanWeatherInfraredCloudImage(replyToken);
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

        if (text.endsWith("站?")||text.endsWith("站？")) {
            text = text.replace("？", "").replace("?", "").trim();
            text = text.substring(0,text.length()-1);
            String url = MrtPdfUrlMaker.getMrtPdfUrl(text);
            if (url.length() > 0) {
                this.replyText(replyToken, url);
            }
            else {
                this.replyText(replyToken, "沒有這個站.");
            }
        }

        if (text.startsWith("AmazonJp:")) {
            amazonJpSearch(text, replyToken);
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
            notifyMessage(text, replyToken);
        }

        if (text.startsWith("PgCommandNotifyImage:")) {
            if(!isAdminUserId(userId, replyToken)) {return;}
            notifyImage(text, replyToken);
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

        if (text.equals("我的LineId")) {
            this.replyText(replyToken, "您的 Line User Id 為: " + userId);
            return;
        }

        if (text.equals("我的Line群組Id")) {
            this.replyText(replyToken, "您的 Line Group Id 為: " + senderId);
            return;
        }

        if (text.equals("最新地震報告圖")) {
            this.replyImage(replyToken, mNewestEarthquakeReportImage, mNewestEarthquakeReportImage);
        }
        if (text.equals("最新地震報告")) {
            this.replyText(replyToken, mNewestEarthquakeReportText);
        }

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
        
        if (text.contains("Eg")||text.contains("eg")||text.contains("egef")||text.contains("女流氓")||text.contains("蕭婆")||text.contains("EG")) {
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

        if (text.contains("ok") && text.contains("好")||
            text.contains("OK") && text.contains("好")||
            text.contains("Ok") && text.contains("好")||
            text.contains("ＯＫ") && text.contains("好")||
            text.contains("幹妳娘")||text.contains("幹您娘")||text.contains("幹你娘")) {
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
            this.replyText(replyToken, getFeatureListString(userId));
        }

        checkNeedTotallyBullyReply(userId, replyToken);

        if (text.length() > 0) {
            bullyModeTrigger(replyToken);
        }

    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) throws IOException {
        log.info("Got postBack event: {}", event);
        String replyToken = event.getReplyToken();
        String data = event.getPostbackContent().getData();
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
        this.reply(replyToken, new TextMessage(message));
    }

    private void replyImage(@NonNull String replyToken, @NonNull String original, @NonNull String preview) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        this.reply(replyToken, new ImageMessage(original, preview));
    }

    /*private ImageCarouselColumn getImageCarouselColumn(String imageUrl, String label, String url) {
        return new ImageCarouselColumn(imageUrl, new URIAction(label, url));
    }

    private void replyImageCarouselTemplate(@NonNull String replyToken, @NonNull List<ImageCarouselColumn> columns) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        this.reply(replyToken, new TemplateMessage("PG soooo cute!", new ImageCarouselTemplate(columns)));
    }*/

    private void replyLocation(@NonNull String replyToken, @NonNull String title, @NonNull String address, double latitude, double longitude) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }        
        this.reply(replyToken, new LocationMessage(title, address, latitude, longitude));
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        
        CompletableFuture<BotApiResponse> apiResponse = lineMessagingClient
                .replyMessage(new ReplyMessage(replyToken, messages));
        //log.info("Sent messages: {} {}", apiResponse.message(), apiResponse.code());
        
    }

    private UserProfileResponse getUserProfile(@NonNull String userId) {
        try {
            CompletableFuture<UserProfileResponse> response = lineMessagingClient
                    .getProfile(userId);
                    log.info("Piggy Check response: " + response);
            return response.get();//TODO
        }catch (Exception e) {
            log.info("Exception: " + e);
        }
        return null;
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
                log.error("Error in wrapClient : " + ex.toString());
                return null;
            }
        }
    }

    private boolean weatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        log.info("weatherResult: " + text);
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
                        log.info("weatherResult default: " + text);
                        return false;
                    }
                }
                strResult = strResult.replace("<BR><BR>", "\n");
                strResult = strResult.replaceAll("<[^<>]*?>", "");
                this.replyText(replyToken, strResult);

            } else {
                log.info("weatherResult length: " + text.length());
                return false;
            }
        } catch (IOException e) {
            throw e;
        }
        return isHaveResult;
    }

    private boolean worldWeatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        log.info(text);

        HttpGet httpget = new HttpGet("https://www.cwb.gov.tw/V7/forecast/world/world_aa.htm");
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(httpget);
        HttpEntity httpEntity = response.getEntity();
        String strResult = EntityUtils.toString(httpEntity, "utf-8");

        String reportTime = "";
        String availableTime = "";

        if (!strResult.contains(text)) {
            strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
            log.info("worldWeatherResult default: " + text);
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
        log.info(text);
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
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
        log.info(text);
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
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
        log.info(text);
        String target = "";
        try {
            if (text.length() == 2) {
                String strResult;
                String url ="";
                switch (text) {
                    case "牡羊": {
                        target="白羊";
                        break;
                    }
                    case "白羊": {
                        target="白羊";
                        break;
                    }
                    case "金牛": {
                        target=text;
                        break;
                    }
                    case "雙子": {
                        target=text;
                        break;
                    }
                    case "巨蟹": {
                        target=text;
                        break;
                    }
                    case "獅子": {
                        target=text;
                        break;
                    }
                    case "處女": {
                        target=text;
                        break;
                    }
                    case "天秤": {
                        target=text;
                        break;
                    }
                    case "天蠍": {
                        target=text;
                        break;
                    }
                    case "射手": {
                        target=text;
                        break;
                    }
                    case "魔羯": {
                        target=text;
                        break;
                    }
                    case "水瓶": {
                        target=text;
                        break;
                    }
                    case "雙魚": {
                        target=text;
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
                    url = "http://www.astroinfo.com.tw/";
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "big5");
                    strResult = strResult.substring(strResult.indexOf("每日運勢"), strResult.length());
                    strResult = strResult.substring(strResult.indexOf("<a href=\"/")+10, strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("\"><img typeof=\""));

                    String dailyAddress = "http://www.astroinfo.com.tw/" + strResult;

                    log.info("DailyHoroscope: " + dailyAddress);

                    // Then get daily sentense
                    httpClient = HttpClients.createDefault();
                    url = dailyAddress;

                    httpget = new HttpGet(url);
                    response = httpClient.execute(httpget);
                    //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "big5");
                    strResult = strResult.substring(strResult.indexOf(target)+36, strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("</p>"));
                    
                    this.replyText(replyToken, "唐綺陽占星幫 每日運勢 " + target + "座\n" + strResult);

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
            log.info(url);
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + text + ".tw&_=" +
                  Instant.now().toEpochMilli();
            log.info(url);
            httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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

            DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
            defaultHttpClient = (DefaultHttpClient) WebClientDevWrapper.wrapClient(defaultHttpClient);
            String url="https://tw.screener.finance.yahoo.net/screener/ws?f=j&ShowID="+text;
            log.info(url);
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            String strResult = "";
            Gson gson = new GsonBuilder().create();
            Screener screener = gson.fromJson(EntityUtils.toString(httpEntity, "utf-8"),Screener.class);


//            url="https://news.money-link.com.tw/yahoo/0061_"+text+".html";
//            httpget = new HttpGet(url);
//            log.info(url);
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
//            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
        log.info(text);
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
                    log.info(url);
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
                            log.info(datums.getSitename()+" "+datums.getAQI());
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
                            log.info(datums.getSitename()+" "+datums.getAQI());
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
        log.info(text);
        try {
            if (text.length() <= 3) {
                String strResult = "";
                String country ="";
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
                        text="TWD";
                        break;
                    }
                    case "鮭魚": {
                        text="Salmon";
                        break;
                    }
                    default:
                        text="";

                }
                if(text.equals("")){
                    strResult = "義大利?維大力? \n請輸入 這些幣別：\n美金 日圓 人民幣 歐元 \n港幣 英鎊 韓元 越南盾\n澳幣 泰銖 印尼盾 法郎\n披索 新幣";
                    this.replyText(replyToken, strResult);
                } else if (text.equals("TWD")){
                    this.replyText(replyToken, "現鈔賣出去巷口便利商店");
                } else if (text.equals("Salmon")){
                    this.replyText(replyToken, "現鈔買入去爭鮮林森北店");
                }else{
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    String url="http://m.findrate.tw/"+country+"/";
                    log.info(url);
                    HttpGet httpget = new HttpGet(url);
                    CloseableHttpResponse response = httpClient.execute(httpget);
                    //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                    HttpEntity httpEntity = response.getEntity();
                    strResult = EntityUtils.toString(httpEntity, "utf-8");
                    strResult = strResult.substring(strResult.indexOf("<td>現鈔買入</td>"), strResult.length());
                    strResult = strResult.substring(0, strResult.indexOf("</table>"));
                    strResult = strResult.replaceAll("</a></td>", ":moneybag:");
                    strResult = strResult.replaceAll("<[^>]*>", "");
                    strResult = strResult.replaceAll("[\\s]{1,}", "");
                    strResult = strResult.replaceAll("現鈔賣出", "\n:money_with_wings:要賣現鈔去");
                    strResult = strResult.replaceAll("現鈔買入", ":dollar:要買現鈔去");

                    this.replyText(replyToken, EmojiUtils.emojify("" + text + "買賣推薦:\n" + strResult));
                }
            }
        } catch (IOException e) {
            throw e;
        }
    }

        private void dailyBeauty(String text, String replyToken) throws IOException {

        String beautyLink = "https://tw.appledaily.com/search/result?querystrS=%E4%BB%8A%E5%A4%A9%E6%88%91%E6%9C%80%E7%BE%8E&sort=time&searchType=all&dateStart=&dateEnd=";

        //this.replyText(replyToken, beautyLink);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String targetUrl = "";
            targetUrl = EntityUtils.toString(httpEntity, "utf-8");

            targetUrl = targetUrl.substring(targetUrl.indexOf("<h2><a href=\"https://tw.appledaily.com/headline/daily/")+13, targetUrl.length());
            targetUrl = targetUrl.substring(0, targetUrl.indexOf("\" target=\"_blank\">"));

            log.info(targetUrl);
            httpget = new HttpGet(targetUrl);
            response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();

            String dumpSource = "";
            dumpSource = EntityUtils.toString(httpEntity, "utf-8");

            dumpSource = dumpSource.substring(dumpSource.indexOf("\"thumbnailUrl\": \"")+17, dumpSource.length());
            dumpSource = dumpSource.substring(0, dumpSource.indexOf("\","));
                        
            log.info("Piggy Check dailyBeauty image: " + dumpSource);
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
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                        
            log.info("Piggy Check dailyBeauty image: " + dumpSource);
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
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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


    private void amazonJpSearch(String text, String replyToken) throws IOException {

        text = text.replace("AmazonJp:", "").trim();

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=カタカナ&url=search-alias%3Daps&field-keywords="+text;
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String context = "";

            context = EntityUtils.toString(httpEntity, "utf-8");

            int maxCount = 0; // Max: 5
            /*List<ImageCarouselColumn> columnsList = new ArrayList<>();
            while (maxCount<5 && context.indexOf("data-asin=\"")> 0) {
                context = context.substring(context.indexOf("data-asin=\""), context.length());
                context = context.substring(context.indexOf("href=\"https:")+6, context.length());
                String searchResultUrl = context.substring(0, context.indexOf("\"><img"));
                String imgUrl = context.substring(context.indexOf("<img src=\"")+10, context.indexOf("\" srcset="));
                log.info("Piggy Check searchResultUrl: " + searchResultUrl);
                log.info("Piggy Check imgUrl: " + imgUrl);
                columnsList.add(getImageCarouselColumn(imgUrl, "PG Cute!", searchResultUrl));
            }
            if (maxCount>0) {
                this.replyImageCarouselTemplate(replyToken, columnsList);    
            }
            else {
                this.replyText(replyToken, "搜索失敗");
            }*/

        }catch (IOException e2) {
            this.replyText(replyToken, "搜索大失敗");
            throw e2;
        }
    }

    private void whoImPickRandomPttBeautyGirlMap(String userId, String replyToken) {
        if (mWhoImPickRandomGirlMap.containsKey(userId)) {
            this.replyText(replyToken, mWhoImPickRandomGirlMap.get(userId));
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
            this.replyText(replyToken, mWhoTheyPickRandomGirlMap.get(senderId));
        }
        else {
            this.replyText(replyToken, "這群組還沒人抽過唷");
        }
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
                int inputNumber = Integer.parseInt(text);
                int year = Calendar.getInstance().get(Calendar.YEAR);
                if (year - inputNumber > 0) {
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

    private void randomPttBeautyGirl(String userId, String senderId, String replyToken, boolean isHot) throws IOException {
        if (senderId.equals(GROUP_ID_CONNECTION)) {
            if(mConnectionGroupRandomGirlUserIdList.contains(userId)) {
                this.replyImage(replyToken, IMAGE_I_HAVE_NO_SPERM, IMAGE_I_HAVE_NO_SPERM);
                return;
            }
            else {
                mConnectionGroupRandomGirlUserIdList.add(userId);
            }
        }

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

        String url = getRandomPttBeautyImageUrl(userId, senderId, isHot);

        log.info("Piggy Check randomPttBeautyGirl: " + url);
        if (url.equals("")) {
            this.replyText(replyToken, "PTT 表特版 parse 失敗");
            return;
        }
        if (url.endsWith(".gif")) {
            this.replyText(replyToken, "Line 不能顯示 gif 直接貼: " + url);
        }
        if (url.indexOf("http:") >= 0) {
            url = url.replace("http", "https");
        }
        this.replyImage(replyToken, url, url);
    }


    private void randomGirl(String text, String replyToken) throws IOException {
        log.info("Piggy Check randomGirl: " + text);
        try {
            if (mJanDanGirlList.size() > 0) {
                Random randomGenerator = new Random();
                int index = randomGenerator.nextInt(mJanDanGirlList.size());
                String item = mJanDanGirlList.get(index);
                item = item.replace("http", "https");
                log.info("Piggy Check item: " + item);
                this.replyImage(replyToken, item, item);
                // this.replyText(replyToken, item);
            }
            else {
                this.replyText(replyToken, "妹子還在跟PG睡覺喔..");
            }

        }catch (IndexOutOfBoundsException e2) {
            throw e2;
        }
        log.info("Piggy Check 6");
    }

    private void instagramTarget(String userId, String senderId, String text, String replyToken, boolean isHot) throws IOException {
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
        String url = getRandomInstagramImageUrl(userId, senderId, text, isHot);
        if (url.equals("")) {
            return;
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

            this.replyLocation(replyToken, title, address, getRandomLatitude(), getRandomLongitude());
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
        log.info("getRandomLatitude: " + result);
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
        log.info("getRandomLongitude: " + result);
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

    private void replyTaiwanWeatherInfraredCloudImage(String replyToken) throws IOException {
        String source = IMAGE_TAIWAN_WEATHER_INFRARED_CLOUD;
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

    private void notifyMessage(String text, String replyToken) throws IOException {
        text = text.replace("PgCommandNotifyMessage:", "");

        if (LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, text)) {
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

    private void notifyImage(String image, String replyToken) throws IOException {
        image = image.replace("PgCommandNotifyImage:", "");

        if (LineNotify.callEvent(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM, " ", image)) {
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

    private void processRandomeGetImage(String replyToken, String text) throws IOException {
        text = text.replace("隨機取圖:", "");
        if (!text.startsWith("http")) {
            return;
        }

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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String result_image_image = EntityUtils.toString(httpEntity, "utf-8");

            List<String> resultImageList = new ArrayList<String> ();
            if (result_image_image.indexOf("http://imgur.com/") > 0) {
                Pattern patternJp = Pattern.compile("http:\\/\\/imgur.com\\/.*");
                Matcher matcherJp = patternJp.matcher(result_image_image);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    result = result.replace("http:","https:");
                    result = result.replace("imgur.com","i.imgur.com");
                    result = result + ".jpg";
                    resultImageList.add(result);
                    //log.info("Piggy Check get image from website imgur url: " + url + " img_link: " + result);
                }
            }
            else if (result_image_image.indexOf("http://i.imgur.com/") > 0) {
                Pattern patternJp = Pattern.compile("http:\\/\\/i.imgur.com\\/.*");
                Matcher matcherJp = patternJp.matcher(result_image_image);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    result = result.replace("http:","https:");
                    result = result + ".jpg";
                    resultImageList.add(result);
                    //log.info("Piggy Check get image from website imgur url: " + url + " img_link: " + result);
                }
            }
            else {
                Pattern patternJp = Pattern.compile("(http|ftp|https):\\/\\/([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:\\/~+#-]*[\\w@?^=%&\\/~+#-])?.(jpeg|jpg)");
                Matcher matcherJp = patternJp.matcher(result_image_image);
                while(matcherJp.find()){
                    String result = matcherJp.group();
                    resultImageList.add(result);
                    //log.info("Piggy Check get image from website url: " + url + " img_link: " + result);
                }
            }

            
            if (resultImageList.size() > 0) {
                int random_num = randomGenerator.nextInt(resultImageList.size());
                String result = resultImageList.get(random_num);
                if (result == null || result.equals("")) {
                    log.info("Piggy Check get image from website parse fail");
                }
                else {
                    log.info("Piggy Check get image from website result: " + result);
                }
                if (result.indexOf("http:") >= 0) {
                    result = result.replace("http", "https");
                }
                this.replyImage(replyToken, result, result);
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
        log.info("Piggy Check processLinHoImage: " + result);
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
            List<String> mEgDevilImgurLinkList = Arrays.asList("https://i.imgur.com/6qN9GI1.jpg", "https://i.imgur.com/qHbEBjN.jpg", "https://i.imgur.com/NFbnbSs.jpg", "https://i.imgur.com/68KRiAj.jpg", "https://i.imgur.com/dHEEBcU.jpg", "https://i.imgur.com/OMqBsOl.jpg", "https://i.imgur.com/JBuBhqr.jpg", "https://i.imgur.com/O5o7tD3.jpg", "https://i.imgur.com/PYZ4v9V.jpg", "https://i.imgur.com/GRD3yXF.jpg");
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
        String source = getUserDisplayPicture(text);
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
        else {
            String strResult = "設定失敗! 不可識別的貨幣代號: " + text;
            this.replyText(replyToken, strResult);
            return;
        }

        String strResult = "成功設定預設匯率\n貨幣代號: " + mExchangedDefaultCountry + "\n中文幣名: " + mExchangedDefaultText + "\n歌頌 PG 讚美 PG";
        this.replyText(replyToken, strResult);
    }

    private void exchangeDefault(String text, String replyToken) throws IOException {
        text = text.replace("?", "").replace("？", "").trim();
        try {
            String strResult = text + mExchangedDefaultText;
            String country = mExchangedDefaultCountry;

            int inputNumber = -1;
                try {
                    inputNumber = Integer.parseInt(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://m.findrate.tw/"+country+"/";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<td>現鈔買入</td>"), tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</tr>"));
                
                float rateNumber = 0f;
                Pattern pattern = Pattern.compile("[\\d]{1,}\\.[\\d]{1,}");
                Matcher matcher = pattern.matcher(tempParseNumber);
                while(matcher.find()){
                    tempParseNumber = matcher.group();
                }
                
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
        text = text.replace("比特幣換算", "").replace("?", "").replace("？", "").trim();
        log.info(text);
        try {
            String strResult = text;    
            String country ="";

            if (text.length() >= 2) {

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
                else if (text.endsWith("台幣") || text.endsWith("新台幣")) {
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

            log.info("country: " + country);
            if(country.equals("")){
                strResult = "義大利?維大力? \n請輸入 這些幣別：\n人民幣 盧比 日圓 台幣\n歐元 美金 英鎊";
                this.replyText(replyToken, strResult);
                return;
            }
            else{                

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="https://zt.coinmill.com/BTC_" + country + ".html?BTC=1";

                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<div id=\"currencyBox1\">"), tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("value=\"")+7, tempParseNumber.indexOf("\">\n<a"));
                
                log.info(tempParseNumber);

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
                    int numResult = (int) (rateNumber);
                    strResult = "1比特幣換算" + strResult + "大概 $" + numResult;
                    this.replyText(replyToken, strResult);
                }
                else {
                    return;
                }
            }
            
        } catch (IOException e) {
            log.info(e.toString());
            throw e;
        }
    }

    private void exchangeToTwd(String text, String replyToken) throws IOException {
        text = text.replace("換算台幣", "").replace("換算臺幣", "").replace("?", "").replace("？", "").trim();
        log.info(text);
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

                int inputNumber = -1;
                try {
                    inputNumber = Integer.parseInt(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://m.findrate.tw/"+country+"/";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<td>現鈔買入</td>"), tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</tr>"));
                
                float rateNumber = 0f;
                Pattern pattern = Pattern.compile("[\\d]{1,}\\.[\\d]{1,}");
                Matcher matcher = pattern.matcher(tempParseNumber);
                while(matcher.find()){
                    tempParseNumber = matcher.group();
                }
                
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
        log.info(text);
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

                int inputNumber = -1;
                try {
                    inputNumber = Integer.parseInt(text);
                }
                catch(java.lang.NumberFormatException e1) {
                    
                    return;
                }
                if (inputNumber <= 0) {
                    return;
                }

                CloseableHttpClient httpClient = HttpClients.createDefault();
                String url="http://m.findrate.tw/"+country+"/";
                log.info(url);
                HttpGet httpget = new HttpGet(url);
                CloseableHttpResponse response = httpClient.execute(httpget);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                HttpEntity httpEntity = response.getEntity();
                String tempParseNumber = "";
                tempParseNumber = EntityUtils.toString(httpEntity, "utf-8");
                tempParseNumber = tempParseNumber.substring(tempParseNumber.indexOf("<td>現鈔賣出</td>"), tempParseNumber.length());
                tempParseNumber = tempParseNumber.substring(0, tempParseNumber.indexOf("</tr>"));
                
                float rateNumber = 0f;
                Pattern pattern = Pattern.compile("[\\d]{1,}\\.[\\d]{1,}");
                Matcher matcher = pattern.matcher(tempParseNumber);
                while(matcher.find()){
                    tempParseNumber = matcher.group();
                }
                
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
        log.info(text);
        String strResult = "";
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url = "http://www.tse.com.tw/api/get.php?method=home_summary";
            log.info(url);
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
        ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imageUrl,"安安","你好",
                Arrays.asList(
                        new MessageAction("查個股股價","輸入 @2331? 或 @台積電?"),
                        new MessageAction("查加權上櫃指數","輸入 呆股?"),
                        new MessageAction("查匯率","輸入 美金匯率? 或 匯率? 檢視可查匯率"),
                        new PostbackAction("更多","more:1")
                )
        );
        TemplateMessage templateMessage = new TemplateMessage("The function Only on mobile device ! ", buttonsTemplate);
        this.reply(replyToken, templateMessage);
    }

    private void help2(String text, String replyToken) throws IOException {
        String imageUrl = "https://p1.bqimg.com/524586/f7f88ef91547655cs.png";
        CarouselTemplate carouselTemplate = new CarouselTemplate(
                Arrays.asList(
                        new CarouselColumn(imageUrl, "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查個股股價", "查個股股價 輸入 @2331? 或 @台積電?"),
                                                   new MessageAction("查加權上櫃指數", "查加權上櫃指數 輸入 呆股?"),
                                                   new MessageAction("查匯率", "查匯率 輸入 美金匯率? 或 匯率? 檢視可查匯率")
                                           )
                        ),
                        new CarouselColumn(imageUrl, "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查天氣", "查天氣　輸入 台北市天氣?"),
                                                   new MessageAction("查氣象", "查氣象　輸入 台北市氣象?"),
                                                   new MessageAction("查空氣品質", "查空氣品質　輸入 北部空氣?")
                                           )
                        ),
                        new CarouselColumn(imageUrl, "安安", "你好",
                                           Arrays.asList(
                                                   new MessageAction("查油價", "查天氣　輸入 油價?"),
                                                   new MessageAction("查星座", "查氣象　輸入 天蠍座?"),
                                                   new MessageAction("查星座", "查氣象　輸入 牡羊座?")
                                           )
                        )
                )
        );
        TemplateMessage templateMessage = new TemplateMessage("The function Only on mobile device ! ", carouselTemplate);
        this.reply(replyToken, templateMessage);
    }

    private String decodeJandanImageUrl(String input) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="http://jandan.net/ooxx";
            log.info(url);
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String maxPage = "";
            int maxPageInt = 0;

            String jsPath = "";

            jsPath = EntityUtils.toString(httpEntity, "utf-8");

            jsPath = jsPath.substring(jsPath.indexOf("<script src=\"//cdn.jandan.net/static/min/")+13, jsPath.length());
            jsPath = jsPath.substring(0, jsPath.indexOf("\"></script>"));

            jsPath = "http:" + jsPath;
            
            //log.info("Piggy Check js path: " + jsPath);

            httpGet = new HttpGet(jsPath);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36" );
            httpGet.addHeader( "Accept","*/*" );
            httpGet.addHeader( "Accept-Encoding","gzip, deflate" );
            httpGet.addHeader( "Accept-Language","en-US,en;q=0.8" );
            httpGet.addHeader( "Host","cdn.jandan.net" );
            httpGet.addHeader( "Referer","http://jandan.net" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            response = httpClient.execute(httpGet);
            ////log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();

            String js_response = EntityUtils.toString(httpEntity, "utf-8");

            //log.info("Piggy Check js_response: " + js_response);

            String js_x = js_response.substring(js_response.indexOf("f.remove();var c=")+17, js_response.length());
            js_x = js_x.substring(js_x.indexOf("(e,\"")+4, js_x.length());
            js_x = js_x.substring(0, js_x.indexOf("\");"));

            log.info("Piggy Check js_x: " + js_x);

            return decryptJanDanImagePath(input, js_x);


        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void startFetchJanDanGirlImages() {
        if (mIsStartJandanParsing) {
            log.info("Piggy Check isStartJandanParsing");
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
            //     log.info("NumberFormatException " + e1);
            //     mIsStartJandanParsing = false;
            //     return;
            // }

            //log.info("Piggy Check max page int: " + mJanDanMaxPage);


            log.info("1秒後開始抓取煎蛋妹子圖...");
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
        log.info("抓取煎蛋妹子圖 Finished.");
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
            
            log.info(url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            CloseableHttpResponse response = httpClient.execute(httpGet);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();
            
            String xml = EntityUtils.toString(httpEntity, "utf-8");
            xml = xml.substring(xml.indexOf("Older Comments\" href=\"//jandan.net/ooxx/page-")+45, xml.length());
            xml = xml.substring(0, xml.indexOf("#comments\""));


            log.info("Piggy Check next page string: " + xml);
            return xml;
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("Piggy Check parse next page string fail.");
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
            
            log.info(url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

            CloseableHttpResponse response = httpClient.execute(httpGet);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                //     log.info("NumberFormatException " + e1);
                //     mIsStartJandanParsing = false;
                //     return;
                // }
                log.info("Piggy Check max page string: " + maxPage);
                return maxPage;
            }
            else if (target.equals("js")) {
                jsPath = EntityUtils.toString(httpEntity, "utf-8");
                while (jsPath.contains("<script src=\"//cdn.jandan.net/static/min/")) {
                    jsPath = jsPath.substring(jsPath.indexOf("<script src=\"//cdn.jandan.net/static/min/")+13, jsPath.length());
                }
                jsPath = jsPath.substring(0, jsPath.indexOf("\"></script>"));
                jsPath = "http:" + jsPath;
                log.info("Piggy Check js path: " + jsPath);
                httpGet = new HttpGet(jsPath);
                httpGet.addHeader( "User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36" );
                httpGet.addHeader( "Accept","*/*" );
                httpGet.addHeader( "Accept-Encoding","gzip, deflate" );
                httpGet.addHeader( "Accept-Language","en-US,en;q=0.8" );
                httpGet.addHeader( "Host","cdn.jandan.net" );
                httpGet.addHeader( "Referer","http://jandan.net" );
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );

                response = httpClient.execute(httpGet);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                String js_response = EntityUtils.toString(httpEntity, "utf-8");

                //log.info("Piggy Check js_response: " + js_response);

                String js_x = js_response.substring(js_response.indexOf("f.remove();var c=")+17, js_response.length());
                //log.info("Piggy Check js_x1: " + js_x);
                js_x = js_x.substring(js_x.indexOf("(e,\"")+4, js_x.length());
                //log.info("Piggy Check js_x2: " + js_x);
                js_x = js_x.substring(0, js_x.indexOf("\");"));

                log.info("Piggy Check js_x: " + js_x);
                return js_x;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getRandomPttBeautyImageUrl(String userId, String senderId, boolean isHot) {
        try{

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.ptt.cc/bbs/Beauty/index.html";
            
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity httpEntity = response.getEntity();

            String maxPage = "";
            int maxPageInt = -1;

            maxPage = EntityUtils.toString(httpEntity, "utf-8");
            maxPage = maxPage.substring(maxPage.indexOf("<a class=\"btn wide\" href=\"/bbs/Beauty/index")+50, maxPage.length());
            maxPage = maxPage.substring(maxPage.indexOf("<a class=\"btn wide\" href=\"/bbs/Beauty/index")+43, maxPage.indexOf(".html"));
            
            try {
                maxPageInt = Integer.parseInt(maxPage);
            }catch(java.lang.NumberFormatException e1) {
                log.info("NumberFormatException " + e1);
            }
            log.info("Piggy Check maxPageInt: " + maxPageInt);
            
            String result_url = "";
            int tryCount = 10;
            String numberCount = "";
            while (tryCount > 0){
                tryCount--;
                int random_num = randomGenerator.nextInt(maxPageInt-1500)+1500;
                random_agent_num = randomGenerator.nextInt(mUserAgentList.size());
                String target_url = "https://www.ptt.cc/bbs/Beauty/index" + random_num + ".html";
                log.info("Piggy Check target PTT beauty list page: " + target_url);
                httpGet = new HttpGet(target_url);
                httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
                httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpGet.setHeader("Cache-Control", "max-age=0");
                httpGet.setHeader("Connection", "keep-alive");


                response = httpClient.execute(httpGet);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                result_url = EntityUtils.toString(httpEntity, "utf-8");
                if (isHot) {
                    if (result_url.indexOf("hl f1\">爆</span>")<0) {
                        log.info("Piggy Check can't find BURST in page: " + random_num);
                        result_url = "";
                        continue;
                    }
                    else {
                        result_url = result_url.substring(result_url.indexOf("hl f1\">爆</span>"), result_url.length());
                        result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                        result_url = "https://www.ptt.cc" + result_url + ".html";
                        numberCount = "爆";
                    }

                    if (result_url.equals("")) {
                        continue;
                    }
                }
                else {
                    
                    Pattern pattern = Pattern.compile("<span class=\"hl f3\">.*?<\\/span>");
                    Matcher matcher = pattern.matcher(result_url);
                    List<String> resultNumberCountList = new ArrayList<String> ();

                    while(matcher.find()){
                        String result = matcher.group();
                        result = result.substring(result.indexOf("hl f3\">")+7, result.indexOf("</span>"));
                        try {
                            int number = Integer.parseInt(result);
                            if (number >= mPttBeautyRandomCountMin) {
                                resultNumberCountList.add(result);
                            }
                        } catch (java.lang.NumberFormatException e) {
                            log.info("NumberFormatException: " + e);
                            continue;
                        }
                    }
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
                            result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                            result_url = "https://www.ptt.cc" + result_url + ".html";
                        }
                        else {
                            result_url = result_url.substring(result_url.indexOf("hl f3\">" + numberCount + "</span>"), result_url.length());
                            result_url = result_url.substring(result_url.indexOf("<a href=\"")+9, result_url.indexOf(".html\">"));
                            result_url = "https://www.ptt.cc" + result_url + ".html";
                        }
                    }
                    else {
                        continue;
                    }
                }


                log.info("Piggy Check result_url: " + result_url);

                String historyString = result_url + " " + (numberCount.equals("爆") ? "爆" : (numberCount + "推"));

                mWhoImPickRandomGirlMap.put(userId, historyString);
                mWhoTheyPickRandomGirlMap.put(senderId, historyString);

                random_agent_num = randomGenerator.nextInt(mUserAgentList.size());

                httpGet = new HttpGet(result_url);
                httpGet.addHeader("User-Agent",mUserAgentList.get(random_agent_num));
                httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
                httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
                httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
                httpGet.setHeader("Cache-Control", "max-age=0");
                httpGet.setHeader("Connection", "keep-alive");


                response = httpClient.execute(httpGet);
                //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                httpEntity = response.getEntity();

                String result_image_image = EntityUtils.toString(httpEntity, "utf-8");

                result_image_image = result_image_image.substring(0, result_image_image.indexOf("--"));

                List<String> resultImageList = new ArrayList<String> ();

                if (result_image_image.indexOf("http://imgur.com/") > 0) {
                    Pattern patternJp = Pattern.compile("http:\\/\\/imgur.com\\/.*");
                    Matcher matcherJp = patternJp.matcher(result_image_image);
                    while(matcherJp.find()){
                        String result = matcherJp.group();
                        result = result.replace("http:","https:");
                        result = result.replace("imgur.com","i.imgur.com");
                        result = result + ".jpg";
                        resultImageList.add(result);
                        log.info("Piggy Check Ptt Beauty imgur url: " + result_url + " img_link: " + result);
                    }
                }
                else {
                    Pattern patternJp = Pattern.compile("http.*?:.*?.jp.*?g");
                    Matcher matcherJp = patternJp.matcher(result_image_image);
                    while(matcherJp.find()){
                        String result = matcherJp.group();
                        resultImageList.add(result);
                        //log.info("Piggy Check Ptt Beauty url: " + result_url + " img_link: " + result);
                    }
                }

                
                if (resultImageList.size() > 0) {
                    random_num = randomGenerator.nextInt(resultImageList.size());
                    return resultImageList.get(random_num);
                }
                else {
                    continue;
                }
            }
            
            if (result_url.equals("")) {
                log.info("Piggy Check Ptt Beauty parse fail");
                return "";
            }

            
        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";//TODO
    }

    private String getRandomInstagramImageUrl(String userId, String senderId, String target, boolean isHot) {
        try {

            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.instagram.com/explore/tags/" + target + "/";
            log.info("getRandomInstagramImageUrl:" + url);
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

            List<String> tempImgList = new ArrayList<String> ();
            List<String> tempIgList = new ArrayList<String> ();

            Pattern pattern = Pattern.compile("display_url\":\".*?\",");
            Matcher matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(14, result.length());
                result = result.substring(0, result.length()-2);
                //log.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempImgList.add(result);
            }


            pattern = Pattern.compile("shortcode\":\".*?\",");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(12, result.length());
                result = result.substring(0, result.length()-2);
                //log.info("Piggy Check IG " + target + " jpg img_link: " + result);
                tempIgList.add(result);
            }

            if (tempImgList.size() > 0) {
                random_num = randomGenerator.nextInt(tempImgList.size());

                String result_url = tempImgList.get(random_num);
                String ig_url = "https://www.instagram.com/p/" + tempIgList.get(random_num);
                log.info("Piggy Check ig_url: " + ig_url);
                mWhoImPickRandomGirlMap.put(userId, ig_url);
                mWhoTheyPickRandomGirlMap.put(senderId, ig_url);
                return result_url;
            }
            else {
                log.info("Piggy Check parse IG fail!");
            }
            
            



        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getRandomPexelsImageUrl(String target) {
        try {

            Random randomGenerator = new Random();
            int random_num = randomGenerator.nextInt(mUserAgentList.size());

            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url="https://www.pexels.com/search/" + target;
            log.info("getRandomPexelsImageUrl:" + url);
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent",mUserAgentList.get(random_num));
            httpGet.addHeader( "Cookie","_gat=1; nsfw-click-load=off; gif-click-load=on; _ga=GA1.2.1861846600.1423061484" );
            httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpGet.setHeader("Accept-Encoding","gzip, deflate, sdch");
            httpGet.setHeader("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4");
            httpGet.setHeader("Cache-Control", "max-age=0");
            httpGet.setHeader("Connection", "keep-alive");


            CloseableHttpResponse response = httpClient.execute(httpGet);
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                //log.info("Piggy Check matcher: " + maxPage);
                maxPage = maxPage.substring(5, maxPage.length());
                maxPage = maxPage.substring(0, maxPage.indexOf("\">"));
            }

            try {
                maxPageInt = Integer.parseInt(maxPage);
            }
            catch(java.lang.NumberFormatException e1) {
                log.info("NumberFormatException");
            }
            log.info("Piggy Check maxPageInt: " + maxPageInt);

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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                //log.info("Piggy Check Pexel " + target + " jpg img_link: " + result);
                tempList.add(result);
            }

            if (tempList.size() > 0) {
                random_num = randomGenerator.nextInt(tempList.size());

                String result_url = tempList.get(random_num);
                log.info("Piggy Check result_url: " + result_url);
                return result_url;
            }
            else {
                log.info("Piggy Check parse fail!");
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
            
            html = html.substring(html.indexOf("commentlist" ));
            
            Pattern patternJpg = Pattern.compile("<img srcset=\".*?.jpg?");
            Pattern patternJpeg = Pattern.compile("<img srcset=\".*?.jpeg?");
            Matcher matcherJpg = patternJpg.matcher(html);
            Matcher matcherJpeg = patternJpeg.matcher(html);
            while(matcherJpg.find()){
                String result = matcherJpg.group();
                result = result.substring(13, result.length());
                log.info("Piggy Check Pexel " + target + " img_link: " + result);
            }
            while(matcherJpeg.find()){
                String result = matcherJpeg.group();
                result = result.substring(13, result.length());
                log.info("Piggy Check Pexel " + target + " img_link: " + result);
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
                    log.info("Backup jandan js_x is null and parse js_x failed. Drop this page.");
                    return;
                }

                while(matcher.find()){
                    String result = matcher.group();
                    result = result.substring(result.indexOf("class=\"img-hash\">")+17, result.length());
                    result = result.substring(0, result.indexOf("</span>"));

                    String result_final = decryptJanDanImagePath(result,js_x);
                    mJanDanParseCount++;
                    // log.info("Piggy Check img_link: " + result_final);
                    result_final.replaceAll(" ", "");
                    if (!result_final.endsWith(".jpg")&&!result_final.endsWith(".png")&&!result_final.endsWith(".jpeg")&&!result_final.endsWith(".gif")){
                        log.info("Parse error? result_final: " + result_final);
                        if (!mLastWorkableJsX.equals("")&&!js_x.equals(mLastWorkableJsX)) {
                            // Workaround, try last workable js_x and decrypt again.
                            log.info("Try backup js_x: " + mLastWorkableJsX);
                            js_x = mLastWorkableJsX;
                            result_final = decryptJanDanImagePath(result,js_x);
                            if (!result_final.endsWith(".jpg")&&!result_final.endsWith(".png")&&!result_final.endsWith(".jpeg")&&!result_final.endsWith(".gif")){
                                log.info("Still Parse error? result_final: " + result_final);
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
            log.info("Method name: " + method.getName());
        }
    }

    private String getGroupSourcePrivateString(Source source, String name) {
        log.info("getGroupSourcePrivateString: " + source + " name: " + name);
        try {
            Field field = GroupSource.class.getDeclaredField(name);
            log.info("field: " + field);
            field.setAccessible(true);
            Object value = field.get((GroupSource)source);
            log.info("value: " + value);
            return (String) value;
        } catch(Exception e) {
            log.info("Exception: " + e);
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
            log.info("Exception: " + e);
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
                    log.info("NewestEarthquakeTimeCheckThread e: " + e);
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
            //log.info("Newest earth quake time: " + newestEarthquakeTime);
            
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
            
            while (tempContext.contains("href=\"#collapse")) {
                tempContext = tempContext.substring(tempContext.indexOf("href=\"#collapse")+15, tempContext.length());
                tempContext = tempContext.substring(tempContext.indexOf("\">")+2, tempContext.length());
                mNewestEarthquakeReportText += tempContext.substring(0, tempContext.indexOf("</a>")) + "\n"; // Scale per location
            }

            tempContext = tempContext.substring(tempContext.indexOf("\">詳細資料"), tempContext.length());
            tempContext = tempContext.substring(tempContext.indexOf("title=\"點此下載"), tempContext.length());
            tempContext = tempContext.substring(tempContext.indexOf("href=\"")+6, tempContext.indexOf("\">"));
            mNewestEarthquakeReportImage = "https://www.cwb.gov.tw";
            mNewestEarthquakeReportImage += tempContext;
            if (!mNewestEarthquakeTime.equals("") && !mNewestEarthquakeTime.equals(newestEarthquakeTime)) {
                notifyAllNeedEarthquakeEventRoom();
            }
            mNewestEarthquakeTime = newestEarthquakeTime;

        } catch (Exception e) {
            log.info("checkEarthquakeReport e: " + e);
        }
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
                    log.info("NewestEarthquakeTimeCheckThread e: " + e);
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
            log.info("checkEarthquakeReport e: " + e);
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
            
            log.info("Newest ingress twitter time: " + mNewestIngressTwitterTime);

            strResult = strResult.substring(strResult.indexOf("<small class=\"time\">")+20,strResult.length());

            strResult = strResult.substring(strResult.indexOf("<a href=\"")+9,strResult.length());

            String twitterUrl = "https://twitter.com" + strResult.substring(0, strResult.indexOf("\""));

            log.info("Newest ingress twitter Url: " + twitterUrl);

            strResult = strResult.substring(strResult.indexOf("title=\"")+7,strResult.length());

            String titleTime = strResult.substring(0, strResult.indexOf("\""));

            log.info("Newest ingress twitter time: " + titleTime);

            strResult = strResult.substring(strResult.indexOf("data-aria-label-part=\"0\">")+25,strResult.length());

            String twitterContext = strResult.substring(0, strResult.indexOf("</p>"));

            log.info("Newest ingress twitter context: " + twitterContext);

            String result = "Ingress Newest Twitter\n";

            result += ("" + titleTime + "\n");
            result += ("\n" + twitterContext + "\n");
            result += ("\n" + twitterUrl);

            return result;


        } catch (Exception e) {
            log.info("checkEarthquakeReport e: " + e);
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

    private List<String> mIngressTwitterEventRoonList = new ArrayList<String> (
        Arrays.asList(LINE_NOTIFY_TOKEN_HELL_TEST_ROOM)
    );
    private void notifyAllNeedIngressTwitterEventRoom() {
        for (String room : mIngressTwitterEventRoonList){
            LineNotify.callEvent(room, getIngressNewestTwitter());
        }
    }

    private String getFeatureListString(String userId) {
        String result = "功能指令集\n\n";
        if(isAdminUserId(userId)) {
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
            result += "---\n\n";
        }

        result += "Ｘ天氣？（Ｘ需為地區\n";
        result += "Ｘ氣象？（Ｘ需為地區\n";
        result += "Ｘ座？（Ｘ需為星座\n";
        result += "Ｘ座運勢？（Ｘ需為星座\n";
        result += "Ｘ空氣？（Ｘ需為地區\n";
        result += "Ｘ匯率？（Ｘ需為幣名\n";
        result += "比特幣換算？\n";
        result += "ＸＹ換算台幣？（Ｘ需為數字Ｙ需為幣名\n";
        result += "呆股？\n";
        result += "每日一句？\n";
        result += "今日我最美？\n";
        result += "今日我最美是誰？\n";
        result += "吃什麼？\n";
        result += "抽 （抽 PTT 表特）\n";
        result += "我剛抽了誰?\n";
        result += "他剛抽了誰?\n";
        result += "抽Ｘ（抽 IG X tag 最新）\n";
        result += "熱抽Ｘ（抽 IG X tag 熱門）\n";
        /*result += "抽Ｘ（為英文抽 Pexel）\n";*/
        result += "*蛙*哪*\n";
        result += "開始猜拳\n";
        result += "結束猜拳\n";
        result += "參加猜拳\n";
        result += "天氣雲圖?\n";
        result += "累積雨量圖?\n";
        result += "紅外線雲圖?\n";
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
        result += "XX幾台?\n";
        result += "開表單：XXX\n";
        result += "查表單\n";
        result += "登記：XXX\n";
        result += "收單\n";
        result += "X站? (X 限制為捷運站名\n";
        result += "我的LineId\n";
        result += "我的Line群組Id\n";
        result += "Ingress Twitter\n";
        return result;
    }
}
