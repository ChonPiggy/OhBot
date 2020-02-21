package ohbot;

import java.util.HashMap;

public class CoronaVirusInfo {

      private int mRate = 0;
      private String mCountry = "N/A";
      private String mConfirm = "?";
      private String mDead = "?";
      private String mHeal = "?";

      public CoronaVirusInfo(String country, String confirm, String dead, String heal) {
            mCountry = country;
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
            return mCountry + " 確: " + mConfirm + " 死: " + mDead + " 癒: " + mHeal;
      }
}