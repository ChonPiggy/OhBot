package ohbot;

public class AvWikiInfo {

    public static String POSTBACK_PREFIX = "AvWikiInfo_Prefix";

    private String mLink;
    private String mTitle;
    private String mCode;
    private String mDate;
    private String mArtists;
    private String mImg;
    private ArrayList<String> mImgs;

    public AvWikiInfo (String link, String title, String code, String date, String artists, String img, ArrayList<String> imgs) {
        mLink = link;
        mTitle = title;
        mCode = code;
        mDate = date;
        mArtists = artists;
        mImg = img;
        mImgs = imgs;
    }

    public ArrayList<String> getImageList() {
        return mImgs;
    }

    public String getCoverImage() {
        return mImg;
    }

    public String getTitle() {
        return mTitle;
    }


    public String toString() {
        String result = "";

        result += (mLink + "\n\n");

        result += ("片名: " + mTitle + "\n\n");

        result += ("番號: " + mCode + "\n\n");

        result += ("配信開始日: " + mDate + "\n\n");

        result += ("女優: " + mArtists);

        return result;
    }
}