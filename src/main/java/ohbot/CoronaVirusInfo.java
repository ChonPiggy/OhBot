package ohbot;

import java.util.HashMap;
import emoji4j.EmojiUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CoronaVirusInfo {

      private int mRank = -1;
      private String mCountry = "N/A";
      private int mConfirm = -1;
      private int mDead = -1;
      private int mHeal = -1;
      public static final int TYPE_DEFAULT = 0;
      public static final int TYPE_CONFIRM = 1;
      public static final int TYPE_DEAD = 2;
      public static final int TYPE_HEAL = 3;
      static private HashMap<String, Integer> mOrignalConfirmDataMap = new HashMap<>(); // country, confirm
      static private HashMap<String, Integer> mOrignalDeadDataMap = new HashMap<>(); // country, dead
      static private HashMap<String, Integer> mOrignalHealDataMap = new HashMap<>(); // country, heal

      public CoronaVirusInfo(int rank, String country, int confirm, int dead, int heal) {
            if (country.equals("阿拉伯聯合大公國")) {
                  mCountry = "阿聯";
            } else if (country.equals("中華民國")) {
                  mCountry = "臺灣";
            } else if (country.equals("台灣")) {
                  mCountry = "臺灣";
            }
            else {
                  mCountry = country;      
            }

            if (!mOrignalConfirmDataMap.containsKey(mCountry)) {
                  mOrignalConfirmDataMap.put(mCountry, confirm);
            }
            if (!mOrignalDeadDataMap.containsKey(mCountry)) {
                  mOrignalDeadDataMap.put(mCountry, dead);
            }
            if (!mOrignalHealDataMap.containsKey(mCountry)) {
                  mOrignalHealDataMap.put(mCountry, heal);
            }
            mRank = rank;
            mConfirm = confirm;
            mDead = dead;
            mHeal = heal;
      }

      public String getCountry() {
            return mCountry;
      }

      public String getConfirm() {
            int oriConfirm = -7;
            if (mOrignalConfirmDataMap.containsKey(mCountry)) {
                  oriConfirm = mOrignalConfirmDataMap.get(mCountry);
            }
            return getFormatNumberString(CoronaVirusInfo.TYPE_CONFIRM, oriConfirm, mConfirm);
      }

      public String getDead() {
            int oriDead = -8;
            int oriConfirm = -7;
            if (mOrignalConfirmDataMap.containsKey(mCountry)) {
                  oriConfirm = mOrignalConfirmDataMap.get(mCountry);
            }
            if (mOrignalDeadDataMap.containsKey(mCountry)) {
                  oriDead = mOrignalDeadDataMap.get(mCountry);
            }
            return getFormatNumberString(CoronaVirusInfo.TYPE_DEAD, oriDead, mDead);
      }

      public String getHeal() {
            int oriHeal = -9;
            int oriConfirm = -7;
            if (mOrignalConfirmDataMap.containsKey(mCountry)) {
                  oriConfirm = mOrignalConfirmDataMap.get(mCountry);
            }
            if (mOrignalHealDataMap.containsKey(mCountry)) {
                  oriHeal = mOrignalHealDataMap.get(mCountry);
            }
            if (mHeal < 0) {
              return "拒絕提供";
            }
            return getFormatNumberString(CoronaVirusInfo.TYPE_HEAL, oriHeal, mHeal);
      }

      private String getFormatNumberString(int type, int ori, int data) {
            String result = "";
            switch (type) {
                case CoronaVirusInfo.TYPE_CONFIRM:
                    
                    if (ori < 0 || ori == data) {
                          result = "" + data;
                    }
                    if (data > ori) {
                       result = "" + data + "(+" + (data - ori) + ")";
                    }
                    if (data < ori) {
                       result = "" + data + "(-" + (ori - data) + ")";
                    }
                    double people = WorldCountryPeopleCountCrawl.getCountryPeopleCount(mCountry);
                    System.out.println("country: " + mCountry + " people: " + people);
                    if (people > 0) {
                      return result + getPeoplePercentageString(people, data);
                    }
                    return result;
                case CoronaVirusInfo.TYPE_DEAD:
                case CoronaVirusInfo.TYPE_HEAL:
                    if (ori < 0 || ori == data) {
                          return "" + data + getPercentageString(data);
                    }
                    if (data > ori) {
                       return "" + data + "(+" + (data - ori) + ")" + getPercentageString(data);
                    }
                    if (data < ori) {
                       return "" + data + "(-" + (ori - data) + ")" + getPercentageString(data);
                    }
                    break;
                default:
                    return "???";
            }
            return "???";
      }

    private String getPeoplePercentageString(double all, int data) {
        double dResult = (double)data / (double)all;
        double result = (double)(dResult * 100.0);
        String resultString = (new BigDecimal("" + result)).toPlainString();;
        
        if(resultString.startsWith("0.")) {
            // ex: 0.0123456 to 0.012
            resultString = resultString.substring(0,5);
            if (resultString.equals("0.000")) {
                resultString = "<0.001";
            }    
        }
        else if (resultString.indexOf(".") == 1) {
            // ex: 1.2345678 to 1.23
            resultString = resultString.substring(0,4);
        }
        else if (resultString.indexOf(".") == 2) {
            // ex: 12.345678 to 12.34
            resultString = resultString.substring(0,5);
        }

        while (resultString.contains(".")&&resultString.endsWith("0")) {
            resultString = resultString.substring(0,resultString.length()-1);
        }
        return "["+resultString+"%]";
    }
    private String getPercentageString(int data) {
        String resultString = "";
        double dConfirm = (double)mConfirm;
        double dData = (double)data;
        double dResult = dData / dConfirm;
        int result = (int)(dResult * 1000);
        if (data == 0) {
            resultString = "[0%]";
        }
        else if (mConfirm <= 0) {
            resultString = "[???]";
        }
        else if ((int)result == 0) {
            resultString = "[<0.1%]";
        }
        else if (result > 0){
            resultString = "["+ (double)((double)result / 10.0)+"%]";
        }

        if (resultString.endsWith(".0%]")) {
            resultString = resultString.substring(0, resultString.length()-4) + "%]";
        }
        return resultString;
    }

      public String toString() {
            if (mCountry.equals("中國大陸")) {
                return "瘟疫大陸\n" + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + "\n" + EmojiUtils.emojify(":skull:") + " " + getDead();
            }
            String result = mCountry + " " + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + " " + EmojiUtils.emojify(":skull:") + " " + getDead();;
            if (result.length() > 17) {
                result = mCountry + "\n" + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + "\n" + EmojiUtils.emojify(":skull:") + " " + getDead();
            }
            return result;
      }

      public String getDetailString() {
            String result = mCountry + " #" + mRank + "\n" + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + "\n" + 
                      EmojiUtils.emojify(":skull:") + " " + getDead() + "\n" + 
                      EmojiUtils.emojify(":pill:") + " " + getHeal();
            return result;
      }

        public String toString(int type) {
            String emojiString = "";
            String number = "";
            String result = "???";
            switch (type) {
                case CoronaVirusInfo.TYPE_CONFIRM:
                    emojiString = ":bomb:";
                    number = getConfirm();
                    break;
                case CoronaVirusInfo.TYPE_DEAD:
                    emojiString = ":skull:";
                    number = getDead();
                  break;
                case CoronaVirusInfo.TYPE_HEAL:
                    emojiString = ":pill:";
                    number = getHeal();
                    break;
                default:
                    return "";
            }
            result = (mCountry.equals("中國大陸") ? "瘟疫大陸" : mCountry) + 
                            (result.length() > 16 ? "\n" : " ") + 
                            EmojiUtils.emojify(emojiString) + " " + number;
            
            return result;
      }

}