package ohbot;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class WorldCountryPeopleCountCrawl {
    private static boolean isUpdating = false;

    private static boolean isInited = false;

    private static List<WorldCountryPeopleInfo> sWCPIList = new ArrayList<WorldCountryPeopleInfo> ();
    private static List<WorldCountryPeopleInfo> sTempWCPIList = new ArrayList<WorldCountryPeopleInfo> ();
    private static HashMap<String, Integer> sCountryPeopleCountMap = new HashMap<>(); // country, people
    private static HashMap<String, WorldCountryPeopleInfo> sCountryPeopleInfoMap = new HashMap<>(); // country, people

    private static byte[] lock = new byte[0];

    private static int sRank = 0;

    public static void init() {
        if (!isInited) {
            startUpdateThread();
            isInited = true;
        }
    }

    private static void checkWorldPeopleCountFromWiki() {
        isUpdating = true;
        //log.info("checkCoronaVirusWiki update started.");
        try {
            System.out.println("Start update world people count from wiki.");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://zh.wikipedia.org/zh-tw/%E5%90%84%E5%9B%BD%E5%AE%B6%E5%92%8C%E5%9C%B0%E5%8C%BA%E4%BA%BA%E5%8F%A3%E5%88%97%E8%A1%A8");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            strResult = strResult.substring(strResult.indexOf("<td align=\"left\"><b>世界</b></td>")+32, strResult.length());

            String country = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            String people = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            String date = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            String percentage = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            addWCPI(sRank, "世界", people, date, percentage);



            while (strResult.contains("<td align=\"left\">")) {

                country = "";
                people = "";
                date = "";
                percentage = "";

                // get country
                strResult = strResult.substring(strResult.indexOf("<td align=\"left\">")+17, strResult.length());
                country = strResult.substring(strResult.indexOf("\" title=\"")+9, strResult.indexOf("\">"));

                // next
                strResult = strResult.substring(strResult.indexOf("\" title=\"")+9, strResult.length());
                
                // get people count
                people = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

                // next
                strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

                // get update date
                date = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

                // next
                strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

                // get percentage
                percentage = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));


                addWCPI(sRank, country, people, date, percentage);
            }

        } catch (Exception e) {
            System.out.println("checkWorldPeopleCountFromWiki e: " + e);
            e.printStackTrace();
        }
        //log.info("checkCoronaVirusWiki update finished.");
        isUpdating = false;
    }

    private static void startUpdateThread() {
        if (!isUpdating) {
            isUpdating = true;
            Thread t = new Thread() {
                public void run(){
                    updateList();
                }
            };
            t.start();
        }
    }

    private static void updateList() {
        synchronized (lock) {
            List<WorldCountryPeopleInfo> tempList = sWCPIList;
            sWCPIList = sTempWCPIList;
            tempList.clear();
            tempList = null;
            sTempWCPIList = new ArrayList<WorldCountryPeopleInfo> ();
            sRank = 0;
            sCountryPeopleCountMap.clear();
            sCountryPeopleInfoMap.clear();
            checkWorldPeopleCountFromWiki();
        }
    }

    private static void addWCPI(int rank, String country, String people, String date, String percentage) {
        synchronized (lock) {
            WorldCountryPeopleInfo info = new WorldCountryPeopleInfo(rank, country, people, date, percentage);
            sTempWCPIList.add(info);
            sCountryPeopleCountMap.put(info.getCountry(), info.getPeople());
            sCountryPeopleInfoMap.put(info.getCountry(), info);
            sRank++;
            System.out.println("Update WCPI: " + info.getCountry());
        }
    }

    public static String dumpList(int type, int range) {
        String result = "";
        synchronized (lock) {
        }
        return result;
    }

    public static int getCountryPeopleCount(String country) {
        synchronized (lock) {
            if (sCountryPeopleCountMap.containsKey(country)) {
                return sCountryPeopleCountMap.get(country);
            }
        }
        return -1;
    }

    public static WorldCountryPeopleInfo getCountryPeopleInfo(String country) {
        synchronized (lock) {
            if (sCountryPeopleInfoMap.containsKey(country)) {
                return sCountryPeopleInfoMap.get(country);
            }
        }
        return null;
    }

}

