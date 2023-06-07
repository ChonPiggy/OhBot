package ohbot;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.util.*;

import java.util.ArrayList;

public class WorldCountryPeopleCountCrawl {
    private static boolean isUpdating = false;

    private static boolean isInited = false;

    private static List<WorldCountryPeopleInfo> sWCPIList = new ArrayList<WorldCountryPeopleInfo> ();
    private static List<WorldCountryPeopleInfo> sTempWCPIList = new ArrayList<WorldCountryPeopleInfo> ();
    private static HashMap<String, Double> sCountryPeopleCountMap = new HashMap<>(); // country, people
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
        String strResult = "";
        try {
            System.out.println("Start update world people count from wiki.");
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://zh.wikipedia.org/zh-tw/%E5%90%84%E5%9B%BD%E5%AE%B6%E5%92%8C%E5%9C%B0%E5%8C%BA%E4%BA%BA%E5%8F%A3%E5%88%97%E8%A1%A8");
            CloseableHttpResponse response = httpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            strResult = EntityUtils.toString(httpEntity, "utf-8");

            strResult = strResult.substring(strResult.indexOf(">國家/地區")+6, strResult.length());

            String country = "世界";
            
            strResult = strResult.substring(strResult.indexOf("<td></td>")+9, strResult.length());
            
            String people = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            String date = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            String percentage = strResult.substring(strResult.indexOf("<td>")+4, strResult.indexOf("</td>"));

            strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());

            addWCPI(sRank, country, people, date, percentage);



            while (strResult.contains("<td align=\"left\">")) {

                country = "";
                people = "";
                date = "";
                percentage = "";

                // get country
                strResult = strResult.substring(strResult.indexOf("<td align=\"left\">")+17, strResult.length());
                strResult = strResult.substring(strResult.indexOf("\" title=\"")+9, strResult.length());
                strResult = strResult.substring(strResult.indexOf("\">")+2, strResult.length());
                country = strResult.substring(0, strResult.indexOf("</a>"));

                // next
                strResult = strResult.substring(strResult.indexOf("</a>")+4, strResult.length());
                strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());
                
                // get people count
                people = strResult.substring(0, strResult.indexOf("</td>"));

                // next
                strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());
                strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());

                // get update date
                date = strResult.substring(0, strResult.indexOf("</td>"));

                // next
                strResult = strResult.substring(strResult.indexOf("</td>")+5, strResult.length());
                strResult = strResult.substring(strResult.indexOf("<td>")+4, strResult.length());

                // get percentage
                percentage = strResult.substring(0, strResult.indexOf("</td>"));


                addWCPI(sRank, country, people, date, percentage);
            }

        } catch (Exception e) {
            System.out.println("checkWorldPeopleCountFromWiki e: " + e);
            System.out.println("checkWorldPeopleCountFromWiki strResult: " + strResult.substring(0,100));
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
            //System.out.println("Update WCPI: " + info.getCountry());
        }
    }

    public static String dumpList(int type, int range) {
        String result = "";
        synchronized (lock) {
        }
        return result;
    }

    public static double getCountryPeopleCount(String country) {
        country = transferCountryName(country);
        synchronized (lock) {
            if (sCountryPeopleCountMap.containsKey(country)) {
                return sCountryPeopleCountMap.get(country);
            }
        }
        return -1;
    }

    public static WorldCountryPeopleInfo getCountryPeopleInfo(String country) {
        country = transferCountryName(country);
        synchronized (lock) {
            if (sCountryPeopleInfoMap.containsKey(country)) {
                return sCountryPeopleInfoMap.get(country);
            }
        }
        return null;
    }

    private static String transferCountryName(String country) {
        if (country.equals("台灣")||country.equals("中華民國")) {
            return "臺灣";
        } else if (country.equals("中華人民共和國")||
            country.equals("中國")||
            country.equals("中國大陸")||
            country.equals("瘟疫大陸")||
            country.equals("大陸")) {
            return "中國大陸";
        } else if (country.equals("南韓")||
            country.equals("大韓民國")) {
            return "韓國";
        }
        return country;
    }

}

