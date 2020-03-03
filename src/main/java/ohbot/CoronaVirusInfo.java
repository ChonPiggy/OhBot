package ohbot;

import java.util.HashMap;
import emoji4j.EmojiUtils;

public class CoronaVirusInfo {

      private int mRate = 0;
      private String mCountry = "N/A";
      private int mConfirm = -1;
      private int mDead = -1;
      private int mHeal = -1;
      static private HashMap<String, Integer> mOrignalConfirmDataMap = new HashMap<>(); // country, confirm
      static private HashMap<String, Integer> mOrignalDeadDataMap = new HashMap<>(); // country, dead
      static private HashMap<String, Integer> mOrignalHealDataMap = new HashMap<>(); // country, heal

      public CoronaVirusInfo(String country, int confirm, int dead, int heal) {
            if (country.equals("阿拉伯聯合大公國")) {
                  mCountry = "阿聯";
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
            int oriConfirm = -1;
            if (mOrignalConfirmDataMap.containsKey(mCountry)) {
                  oriConfirm = mOrignalConfirmDataMap.get(mCountry);
            }
            return getFormatNumberString(oriConfirm, mConfirm);
      }

      public String getDead() {
            int oriDead = -1;
            if (mOrignalDeadDataMap.containsKey(mCountry)) {
                  oriDead = mOrignalDeadDataMap.get(mCountry);
            }
            return getFormatNumberString(oriDead, mDead);
      }

      public String getHeal() {
            int oriHeal = -1;
            if (mOrignalHealDataMap.containsKey(mCountry)) {
                  oriHeal = mOrignalHealDataMap.get(mCountry);
            }
            return getFormatNumberString(oriHeal, mHeal);
      }

      private String getFormatNumberString(int ori, int data) {
            if (ori == -1 || ori == data) {
                  return "" + data;
            }
            if (data > ori) {
               return "" + data + "(+" + (data - ori) + ")";
            }
            if (data < ori) {
               return "" + data + "(-" + (ori - data) + ")";
            }
            return "???"
      }

      public String toString() {
            if (mCountry.equals("中國大陸")) {
                  return "瘟疫大陸\n" + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + "\n" + EmojiUtils.emojify(":skull:") + " " + getDead();
            }
            String result = mCountry + " " + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + " " + EmojiUtils.emojify(":skull:") + " " + getDead();;
            if (result.length() > 16) {
                  result = mCountry + "\n" + EmojiUtils.emojify(":bomb:") + " " + getConfirm() + "\n" + EmojiUtils.emojify(":skull:") + " " + getDead();
            }
            return result;
      }
}