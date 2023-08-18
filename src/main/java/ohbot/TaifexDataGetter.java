package ohbot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ohbot.utils.PgLog;

public class TaifexDataGetter {

    public static String getCurrentTxData() {
        
        HttpClient httpclient = new DefaultHttpClient();
        String postRequest = "{\"SymbolID\":[\"" + getTxPayloadString() + "\"]}"; 
        PgLog.info("getTxPayloadString: " + getTxPayloadString());
        try {
            String url = "https://mis.taifex.com.tw/futures/api/getQuoteDetail";
            HttpPost request = new HttpPost(url);
            request.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
            request.setHeader("Connection", "Keep-Alive");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Content-Encoding", "gzip");

            // Request body
            StringEntity reqEntity = new StringEntity(postRequest,"UTF-8");
            request.setEntity(reqEntity);
            PgLog.info("Piggy Check request: " + request);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            String jsonString = EntityUtils.toString(entity);
            
            JSONObject json = new JSONObject(jsonString);

            PgLog.info("Piggy Check post json: " + json);
            JSONObject rtData = json.getJSONObject("RtData");
            PgLog.info("Piggy Check post rtData: " + rtData);
            JSONArray quoteList = rtData.getJSONArray("QuoteList");
            PgLog.info("Piggy Check post quoteList: " + quoteList);
            
            String dispName = "";
            String openPrice = "";
            String highPrice = "";
            String lowPrice = "";
            String lastPrice = "";
            
            for (int i = 0; i < quoteList.length(); i++)
            {
                try {
                    dispName = quoteList.getJSONObject(i).getString("DispCName");
                    openPrice = quoteList.getJSONObject(i).getString("COpenPrice");
                    highPrice = quoteList.getJSONObject(i).getString("CHighPrice");
                    lowPrice = quoteList.getJSONObject(i).getString("CLowPrice");
                    lastPrice = quoteList.getJSONObject(i).getString("CLastPrice");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            PgLog.info("Piggy Check post dispName: " + dispName);
            PgLog.info("Piggy Check post openPrice: " + openPrice);
            PgLog.info("Piggy Check post highPrice: " + highPrice);
            PgLog.info("Piggy Check post lowPrice: " + lowPrice);
            PgLog.info("Piggy Check post lastPrice: " + lastPrice);
            

            String result = "";
            
            result += ("\n" + dispName + "\n");
            result += ("開盤: " + openPrice + "\n");
            result += ("最高: " + highPrice + "\n");
            result += ("最低: " + lowPrice + "\n");
            result += ("現價: " + lastPrice + "");
            
            if (result.length() == 0) {
                result = "Error";
            }
            return result;

        } catch (Exception e) {
            PgLog.info("Exception e: " + e);
        }
        return "N/A";
    }
    
    private static String getTxPayloadString() {
        
        Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String year = sdf.format(current.getTime());
        String yearResult = year.substring(year.length()-1, year.length());

        PgLog.info("year: " + year);
        PgLog.info("yearResult: " + yearResult);
        sdf = new SimpleDateFormat("MM");        
        String month = sdf.format(current.getTime());
        String monthResult = "";

        PgLog.info("month: " + month);
        
        if (month.startsWith("0")) {
            month = month.substring(month.length()-1, month.length());
        }
        int monthInt = Integer.parseInt(month);
        
        PgLog.info("monthInt: " + monthInt);
        
        
        // Third Wed of every month need switch to next TX
        
        if (isNeedSwitchToNextMonth()) {
            monthInt++;
            if (monthInt > 12) {
                monthInt = 1;
            }
        }

        PgLog.info("monthInt: after " + monthInt);
        
        // Char A is number 65.
        monthResult = Character.toString((char)(64+monthInt));
        
        // "TXFI3-F" means "臺指期093"
        return "TXF" + monthResult + yearResult + "-F";
        //return "TXFI3-F";
    }
    
    private static boolean isNeedSwitchToNextMonth() {
        // Check is third week
        Calendar current = Calendar.getInstance(TimeZone.getDefault());
        current.setTimeInMillis(System.currentTimeMillis());
        int wk = current.get(Calendar.WEEK_OF_MONTH);
        PgLog.info("Week of Month :" + wk);
        int wed = current.get(Calendar.WEDNESDAY);
        PgLog.info("wed :" + wed);
        return true;
    }
}