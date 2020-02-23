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
            HttpGet httpget = new HttpGet("https://zh.wikipedia.org/wiki/2019%E5%86%A0%E7%8B%80%E7%97%85%E6%AF%92%E7%97%85%E7%96%AB%E6%83%85");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String strResult = EntityUtils.toString(httpEntity, "utf-8");

            // Catch update time
            String temp = strResult.substring(strResult.indexOf("截至"), strResult.length());
            mUpdateTime = temp.substring(0, temp.indexOf("日")+1);

            while (strResult.contains("<td><span class=\"flagicon\">")) {
                String country = "";
                String confirm = "";
                String dead = "";
                String heal = "";

                // get country
                strResult = strResult.substring(strResult.indexOf("<td><span class=\"flagicon\">"), strResult.length());
                strResult = strResult.substring(strResult.indexOf("title=\"")+7, strResult.length());
                country = strResult.substring(0, strResult.indexOf("\">"));

                // get confirm
                strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                confirm = strResult.substring(0, strResult.indexOf("\n</td>"));


                // get dead
                strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                dead = strResult.substring(0, strResult.indexOf("\n</td>"));


                // get heal
                strResult = strResult.substring(strResult.indexOf("<td align=\"right\">")+18, strResult.length());
                heal = strResult.substring(0, strResult.indexOf("\n</td>"));

                addCVI(country, confirm, dead, heal);

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

    private void addCVI(String country, String confirm, String dead, String heal) {
        synchronized (lock) {
            mCVIList.add(new CoronaVirusInfo(country, confirm, dead, heal));
        }
    }

    public String dumpList() {
        String result = "N/A";
        synchronized (lock) {
            //result = EmojiUtils.emojify(":warning:") + "中國肺炎全球傷亡人數" + EmojiUtils.emojify(":warning:") + "\n" + mUpdateTime + "\n";
            result = EmojiUtils.emojify(":warning:") + "中國肺炎全球傷亡" + EmojiUtils.emojify(":warning:") + "\n\n";
            for (CoronaVirusInfo info : mCVIList) {
                if (info.getCountry().equals("臺灣")) {
                    result += EmojiUtils.emojify(":exclamation:");
                    result += info;
                    result += EmojiUtils.emojify(":exclamation:");
                    result += "\n";
                }
                else {
                    result += (info + "\n");
                }
            }
            result+=mUpdateTime;
        }
        return result;
    }
}

