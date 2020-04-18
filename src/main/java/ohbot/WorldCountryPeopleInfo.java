package ohbot;

import java.util.HashMap;
import emoji4j.EmojiUtils;

public class WorldCountryPeopleInfo {

      private int mRank = -1;
      private String mCountry = "N/A";
      private String mPeople = "";
      private String mUpdateDate = "";
      private String mPercentage = "";

    public WorldCountryPeopleInfo(int rank, String country, String people, String date, String percentage) {
            if (country.equals("台灣")||country.equals("臺灣")||country.equals("中華民國")) {
                  mCountry = "臺灣";
            } else if (country.equals("中華人民共和國")) {
                  mCountry = "中國大陸";
            }
            else {
                  mCountry = country;
            }

            mRank = rank;
            mPeople = people;
            mUpdateDate = date;
            mPercentage = percentage;
    }

    public String getCountry() {
        return mCountry;
    }

    public int getPeople() {
        int result = -1;
        try {
            result = Integer.parseInt(mPeople.replace(",", "").trim());
        } catch (java.lang.NumberFormatException e) {
        }
        return result;
    }

    private String getPeopleString() {
        return mPeople;
    }

    public int getRank() {
        return mRank;
    }

    public String getUpdateDate() {
        return mUpdateDate;
    }

    public String getPercentage() {
        return mPercentage;
    }

    public String toString() {
        String result = "";
        if (getCountry().equals("世界")) {
            result = "全球\n" +
            "人口: " + getPeopleString() + "\n" +
            "更新日期: " + getUpdateDate() + "\n" +
            "佔世界比: " + getPercentage();
        }
        else {
            result = "國家: " + getCountry() + " #" + getRank() + "\n" +
                "人口: " + getPeopleString() + "\n" +
                "更新日期: " + getUpdateDate() + "\n" +
                "佔世界比: " + getPercentage();
        }
        return result;
    }

}