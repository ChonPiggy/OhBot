package ohbot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
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
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import emoji4j.EmojiUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ohbot.aqiObj.AqiResult;
import ohbot.aqiObj.Datum;
import ohbot.stockObj.*;
import ohbot.utils.Utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
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

/**
 * Created by lambertyang on 2017/1/13.
 */
@LineMessageHandler
@Slf4j
@RestController
public class OhBotController {

    private ArrayList<String> mEatWhatArray = new ArrayList<String>();
    private List<String> mJanDanGirlList = new ArrayList<String> ();
    private List<String> mPexelFoodList = new ArrayList<String> ();
    private List<String> mPexelBoyList = new ArrayList<String> ();
    private List<String> mPexelGirlList = new ArrayList<String> ();
    private List<String> mPexelManList = new ArrayList<String> ();
    private List<String> mPexelWomenList = new ArrayList<String> ();
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
    private List<String> mRandamLocationTitleList = new ArrayList<String> (mDefaultRandamLocationTitleList);
    private List<String> mRandamLocationAddressList = new ArrayList<String> (mDefaultRandamLocationAddressList);
    private boolean mIsStartJandanParsing = false;
    private boolean mIsStartJandanStarted = false;

    private int mJanDanParseCount = 0;
    private int mJanDanGifCount = 0;
    private int mJanDanMaxPage = 0;
    private int mJanDanProgressingPage = 0;
    private String mLastWorkableJsX = "";

    @Autowired
    private LineMessagingService lineMessagingService;

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

        Response<BotApiResponse> apiResponse = null;
        try {
            apiResponse = lineMessagingService.pushMessage(pushMessage).execute();
            return String.format("Sent messages: %s %s", apiResponse.message(), apiResponse.code());
        } catch (IOException e) {
            e.printStackTrace();
            return String.format("Error in sending messages : %s", e.toString());
        }
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
                url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + stock +
                      ".tw&_=" + Instant.now().toEpochMilli();
                log.info(url);
                httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
    public String user(@RequestParam(value = "userid") String userid) throws IOException {
        String strResult="";
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        strResult = userProfileResponse.getDisplayName() + "\n" + userProfileResponse.getPictureUrl();
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

        if (mJanDanGirlList.size() == 0 && !mIsStartJandanStarted) {
            //mIsStartJandanStarted = true;
            //startFetchJanDanGirlImages();
        }

        if (text.endsWith("天氣?") || text.endsWith("天氣？")) {
            weatherResult(text, replyToken);
        }

        if (text.endsWith("氣象?") || text.endsWith("氣象？")) {
            weatherResult2(text, replyToken);
        }

        if (text.endsWith("座?") || text.endsWith("座？")) {
            star(text, replyToken);
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

        if (text.endsWith("換算台幣?") || text.endsWith("換算台幣？")) {
            exchange(text, replyToken);
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

        if (text.startsWith("抽") && text.length() > 1) {
            pexelsTarget(text, replyToken);
        }
        else if (text.equals("抽")) {
            randomGirl(text, replyToken);
        }
        
        if (text.startsWith("PgCommand新增吃什麼:")) {
            updateEatWhat(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除吃什麼:")) {
            deleteEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand清空吃什麼")) {
            cleanEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand列出吃什麼")) {
            dumpEatWhat(text, replyToken);
        }
        if (text.equals("PgCommand煎蛋進度")) {
            randomGirlProgressing(text, replyToken);
        }
        if (text.equals("PgCommand煎蛋數量")) {
            randomGirlCount(text, replyToken);
        }
        if (text.startsWith("PgCommand煎蛋解碼:")) {
            randomGirlDecode(text, replyToken);
        }
        if (text.startsWith("PgCommand煎蛋解碼圖:")) {
            randomGirlDecodeImage(text, replyToken);
        }
        if (text.startsWith("PgCommand圖片:")) {
            replyInputImage(text, replyToken);
        }
        if (text.equals("PgCommand開始煎蛋")) {
            startFetchJanDanGirlImages();
        }

        if (text.startsWith("PgCommand新增隨機地點:")) {
            updateRandomAddress(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除隨機地點:")) {
            deleteRandomAddress(text, replyToken);
        }
        if (text.equals("PgCommand清空隨機地點")) {
            cleanRandomAddress(text, replyToken);
        }
        if (text.equals("PgCommand列出隨機地點")) {
            dumpRandomAddress(text, replyToken);
        }

        if (text.startsWith("PgCommand新增隨機動作:")) {
            updateRandomTitle(text, replyToken);
        }
        if (text.startsWith("PgCommand刪除隨機動作:")) {
            deleteRandomTitle(text, replyToken);
        }
        if (text.equals("PgCommand清空隨機動作")) {
            cleanRandomTitle(text, replyToken);
        }
        if (text.equals("PgCommand列出隨機動作")) {
            dumpRandomTitle(text, replyToken);
        }

        if (text.contains("蛙")) {
            whereIsMyFrog(text, replyToken);
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
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .execute();
            log.info("Sent messages: {} {}", apiResponse.message(), apiResponse.code());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private UserProfileResponse getUserProfile(@NonNull String userId) throws IOException {
        Response<UserProfileResponse> response = lineMessagingService
                .getProfile(userId)
                .execute();
        return response.body();
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

    private void weatherResult(String text, String replyToken) throws IOException {
        text = text.replace("天氣", "").replace("?", "").replace("？", "").replace("臺", "台").trim();
        log.info(text);
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
                    default:
                        strResult = "義大利?維大力? \nSorry 我不知道" + text + "是哪裡...";
                }
                strResult = strResult.replace("<BR><BR>", "\n");
                strResult = strResult.replaceAll("<[^<>]*?>", "");
                this.replyText(replyToken, strResult);
            }
        } catch (IOException e) {
            throw e;
        }
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                    log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            url = "http://mis.twse.com.tw/stock/api/getStockInfo.jsp?ex_ch=" + companyType + "_" + text + ".tw&_=" +
                  Instant.now().toEpochMilli();
            log.info(url);
            httpget = new HttpGet(url);
            response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
//            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                    default:
                        text="";

                }
                if(text.equals("")){
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
                    this.replyText(replyToken, EmojiUtils.emojify(strResult));
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
                    log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String strDate = sdFormat.format(date);
        String beautyLink = "https://unayung.cc/links/" + strDate;

        //this.replyText(replyToken, beautyLink);

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
    }

    private void dailyBeautyName(String text, String replyToken) throws IOException {

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String strDate = sdFormat.format(date);
        String beautyLink = "https://unayung.cc/links/" + strDate;

        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            String url= beautyLink;
            log.info(url);
            HttpGet httpget = new HttpGet(url);
            CloseableHttpResponse response = httpClient.execute(httpget);
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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

    private void pexelsTarget(String text, String replyToken) throws IOException {
        text = text.replace("抽", "");
        text = text.replace(" ", "%20");
        // try {
        //     if (mPexelFoodList.size() > 0) {
        //         Random randomGenerator = new Random();

        //         int index = randomGenerator.nextInt(mPexelFoodList.size());
        //         String item = mPexelFoodList.get(index);
        //         this.replyImage(replyToken, item, item);
        //     }
        //     else {
        //         this.replyText(replyToken, "餐廳準備中..");
        //     }

        // }catch (IndexOutOfBoundsException e2) {
        //     throw e2;
        // }

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

        this.replyText(replyToken, "" + mPexelFoodList.size());
        
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

    

    private void exchangeDefault(String text, String replyToken) throws IOException {
        text = text.replace("?", "").replace("？", "").trim();
        try {
            String strResult = text + "日圓";
            String country ="JPY";

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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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

    private void exchange(String text, String replyToken) throws IOException {
        text = text.replace("換算台幣", "").replace("?", "").replace("？", "").trim();
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            //log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
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
                log.info("NumberFormatException " + e1);
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
            log.info(String.valueOf(response.getStatusLine().getStatusCode()));
            httpEntity = response.getEntity();
            String html = EntityUtils.toString(httpEntity, "utf-8");

            List<String> tempList = new ArrayList<String> ();

            pattern = Pattern.compile("<img srcset=\".*?h=");
            matcher = pattern.matcher(html);
            while(matcher.find()){
                String result = matcher.group();
                result = result.substring(13, result.length());
                result = result.substring(0, result.length()-3);
                //log.info("Piggy Check Pexel " + target + " jpg img_link: " + result);
                tempList.add(result);
            }

            if (tempList.size() > 0) {
                random_num = randomGenerator.nextInt(tempList.size());
                return tempList.get(random_num);
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

        private void insertImage(String target, String img_url) {
            if (target.equals("food")) {
                    mPexelFoodList.add(img_url);
            }
            else if (target.equals("girl")) {
                    mPexelFoodList.add(img_url);
            }
            else if (target.equals("boy")) {
                    mPexelFoodList.add(img_url);
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
}