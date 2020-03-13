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
import java.util.Iterator;
import java.lang.reflect.Method;

import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
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

public class CoronaVirusWikiRankCrawlThread extends Thread {
    private boolean isUpdating = false;

    private List<CoronaVirusInfo> mCVIList = new ArrayList<CoronaVirusInfo> ();

    private byte[] lock = new byte[0];

    private String mUpdateTime = "";

    public void run() {
        while (true) {
            try {
                Thread.sleep(10000);
                if (!isUpdating) {
                    checkCoronaVirusWiki();
                }
            } catch (Exception e) {
                //log.info("CoronaVirusWikiRankCrawlThread e: " + e);
            }
        }
    }

    private void checkCoronaVirusWiki() {
        isUpdating = true;
        clearList();
        //log.info("checkCoronaVirusWiki update started.");
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://zh.wikipedia.org/zh-tw/2019%E5%86%A0%E7%8B%80%E7%97%85%E6%AF%92%E7%97%85%E7%96%AB%E6%83%85");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            // Catch update time
            String temp = strResult.substring(strResult.indexOf("截至"), strResult.length());
            mUpdateTime = temp.substring(0, temp.indexOf("日")+1);

            while (strResult.contains("<td><span class=\"")) {
                String country = "";
                String confirm = "";
                String dead = "";
                String heal = "";

                // get country
                if (strResult.startsWith("<td><span class=\"nowrap\"><a href=\"/wiki/File:Cruise_ship_side_view.png\"")) {
                    strResult = strResult.substring(strResult.indexOf("<td><span class=\"nowrap\"><a href=\"/wiki/File:Cruise_ship_side_view.png\"")+71, strResult.length());
                    country = "鑽石公主號";
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                else {
                    

                    strResult = strResult.substring(strResult.indexOf("<td><span class=\"")+17, strResult.length());

                    // catch country part
                    temp = strResult.substring(0, strResult.indexOf("</td>"));

                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());

                    if (temp.contains("href=\"/wiki/")) {
                        temp = temp.substring(temp.indexOf("href=\"/wiki/")+12, temp.length());
                    }
                    else if (temp.contains("href=\"/w/")) {
                        temp = temp.substring(temp.indexOf("href=\"/w/")+9, temp.length());
                    }                    
                    /*temp = temp.substring(temp.indexOf("\" title=\"")+9, temp.length());
                    temp = temp.substring(temp.indexOf("\">")+2, temp.length());
                    country = temp.substring(0, temp.indexOf("</a>"));*/
                    country = temp.substring(temp.indexOf("\" title=\"")+9, temp.indexOf("\">"));
                }

                // get confirm
                if (strResult.startsWith("<td style=\"color:gray;\">0")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">0")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    confirm = "0";
                } else if (strResult.startsWith("<td style=\"color:gray;\">1")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">1")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    heal = "1";
                } else if (strResult.startsWith("<td align=\"right\">")) {
                    strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                    confirm = strResult.substring(0,strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                else {
                    strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());
                    confirm = strResult.substring(0, strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                


                // get dead
                if (strResult.startsWith("<td style=\"color:gray;\">0")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">0")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    dead = "0";
                } else if (strResult.startsWith("<td style=\"color:gray;\">1")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">1")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    heal = "1";
                } else if (strResult.startsWith("<td align=\"right\">")) {
                    strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                    dead = strResult.substring(0,strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                else {
                    strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());
                    dead = strResult.substring(0, strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }


                // get heal
                if (strResult.startsWith("<td style=\"color:gray;\">0")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">0")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    heal = "0";
                } else if (strResult.startsWith("<td style=\"color:gray;\">1")) {
                    strResult = strResult.substring(strResult.indexOf("<td style=\"color:gray;\">1")+25, strResult.length());
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                    heal = "1";
                } else if (strResult.startsWith("<td align=\"right\">")) {
                    strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                    heal = strResult.substring(0,strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                else {
                    strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());
                    heal = strResult.substring(0, strResult.indexOf("\n</td>"));
                    strResult = strResult.substring(strResult.indexOf("</td>\n")+6, strResult.length());
                }
                strResult = strResult.substring(strResult.indexOf("<tr>\n")+5, strResult.length());


                int confirmInt = -1;
                int deadInt = -2;
                int healInt = -3;
                try {
                    confirmInt = Integer.parseInt(confirm.replace(",", "").trim());
                } catch (java.lang.NumberFormatException e) {
                }
                try {
                    deadInt = Integer.parseInt(dead.replace(",", "").trim());
                } catch (java.lang.NumberFormatException e) {
                }
                try {
                    healInt = Integer.parseInt(heal.replace(",", "").trim());
                } catch (java.lang.NumberFormatException e) {
                }
                addCVI(country, confirmInt, deadInt, healInt);

            }

        } catch (Exception e) {
            //log.info("checkCoronaVirusWiki e: " + e);
        }
        //log.info("checkCoronaVirusWiki update finished.");
        isUpdating = false;
    }

    private void clearList() {
        synchronized (lock) {
            mCVIList.clear();
        }
    }

    private void addCVI(String country, int confirm, int dead, int heal) {
        synchronized (lock) {
            mCVIList.add(new CoronaVirusInfo(country, confirm, dead, heal));
        }
    }

    /*
     *   Type DEFAULT, include confirm and dead.
     *   Other type include sprecific type context.
    */
    public String dumpList(int type) {
        String result = "N/A";
        synchronized (lock) {
            String stringType = "傷亡";
            switch (type) {
                case CoronaVirusInfo.TYPE_CONFIRM:
                    stringType = "確診";
                    break;
                case CoronaVirusInfo.TYPE_DEAD:
                    stringType = "死亡";
                    break;
                case CoronaVirusInfo.TYPE_HEAL:
                    stringType = "痊癒";
                    break;    
            }
            //result = EmojiUtils.emojify(":warning:") + "中國肺炎全球傷亡人數" + EmojiUtils.emojify(":warning:") + "\n" + mUpdateTime + "\n";
            result = EmojiUtils.emojify(":warning:") + "中國肺炎全球" + stringType + EmojiUtils.emojify(":warning:") + "\n";
            for (CoronaVirusInfo info : mCVIList) {
                if (info.getCountry().equals("臺灣")) {
                    result += EmojiUtils.emojify(":exclamation:");
                }
                switch (type) {
                    case CoronaVirusInfo.TYPE_CONFIRM:
                    case CoronaVirusInfo.TYPE_DEAD:
                    case CoronaVirusInfo.TYPE_HEAL:
                        result += info.toString(type);
                        break;
                    default:
                        result += info;
                        break;
                }
                if (info.getCountry().equals("臺灣")) {
                    result += EmojiUtils.emojify(":exclamation:");
                }
                result += "\n";
            }
            result+=mUpdateTime;
        }
        return result;
    }

}

