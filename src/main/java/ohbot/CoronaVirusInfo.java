package ohbot;

import java.util.HashMap;
import emoji4j.EmojiUtils;

public class CoronaVirusInfo {

      private int mRate = 0;
      private String mCountry = "N/A";
      private int mConfirm = -4;
      private int mDead = -5;
      private int mHeal = -6;
      public static final int TYPE_DEFAULT = 0;
      public static final int TYPE_CONFIRM = 1;
      public static final int TYPE_DEAD = 2;
      public static final int TYPE_HEAL = 3;
      static private HashMap<String, Integer> mOrignalConfirmDataMap = new HashMap<>(); // country, confirm
      static private HashMap<String, Integer> mOrignalDeadDataMap = new HashMap<>(); // country, dead
      static private HashMap<String, Integer> mOrignalHealDataMap = new HashMap<>(); // country, heal

      public CoronaVirusInfo(String country, int confirm, int dead, int heal) {
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
            return getFormatNumberString(oriConfirm, mConfirm);
      }

      public String getDead() {
            int oriDead = -8;
            if (mOrignalDeadDataMap.containsKey(mCountry)) {
                  oriDead = mOrignalDeadDataMap.get(mCountry);
            }
            return getFormatNumberString(oriDead, mDead);
      }

      public String getHeal() {
            int oriHeal = -9;
            if (mOrignalHealDataMap.containsKey(mCountry)) {
                  oriHeal = mOrignalHealDataMap.get(mCountry);
            }
            return getFormatNumberString(oriHeal, mHeal);
      }

      private String getFormatNumberString(int ori, int data) {
            if (ori < 0 || ori == data) {
                  return "" + data;
            }
            if (data > ori) {
               return "" + data + "(+" + (data - ori) + ")";
            }
            if (data < ori) {
               return "" + data + "(-" + (ori - data) + ")";
            }
            return "???";
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