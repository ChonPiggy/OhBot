package ohbot;

import java.util.HashMap;
import emoji4j.EmojiUtils;

public class CoronaVirusInfo {

      private int mRate = 0;
      private String mCountry = "N/A";
      private String mConfirm = "?";
      private String mDead = "?";
      private String mHeal = "?";

      public CoronaVirusInfo(String country, String confirm, String dead, String heal) {
            if (country.equals("阿拉伯聯合大公國")) {
                  mCountry = "阿聯";
            }
            else {
                  mCountry = country;      
            }
            
            mConfirm = confirm;
            mDead = dead;
            mHeal = heal;
      }

      public String getCountry() {
            return mCountry;
      }

      public String getConfirm() {
            return mConfirm;
      }

      public String getDead() {
            return mDead;
      }

      public String getHeal() {
            return mHeal;
      }

      public String toString() {
            if (mCountry.equals("中國大陸")) {
                  return "瘟疫大陸\n" + EmojiUtils.emojify(":bomb:") + " " + mConfirm + "\n" + EmojiUtils.emojify(":skull:") + " " + mDead;
            }
            return mCountry + " " + EmojiUtils.emojify(":bomb:") + " " + mConfirm + " " + EmojiUtils.emojify(":skull:") + " " + mDead;
      }
}