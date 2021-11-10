package dropbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;

import ohbot.utils.PgLog;

public class DropBoxAuthorizer {
	
	static String AppInfoContent = " {\n" + 
								   "    \"key\": \""+System.getenv("DROPBOX_API_KEY")+"\",\"\n" +
								   "    \"secret\": \""+System.getenv("DROPBOX_API_SECRET")+"\",\"\n" +
								   " }";
	static String AppInfoPath = "DropBoxAppInfo.auth";
	static String AuthFileOutputPath = "AuthFileOutput.auth";
	
	public final static int SHORT_LIVE_TOKEN = 1;
	public final static int PKCE = 2;
	public final static int SCOPE = 3;
	
	public static void generateAuthFileOutput(int type) {
		// Read app info file (contains app key and app secret)
        DbxAppInfo appInfo;
        try {
        	String appInfoPath = generateAppInfoFile();
        	System.err.println("appInfoPath: " + appInfoPath);
            appInfo = DbxAppInfo.Reader.readFromFile(appInfoPath);
        } catch (JsonReader.FileLoadException ex) {
            System.err.println("Error reading <app-info-file>: " + ex.getMessage());
            return;
        }

        // Run through Dropbox API authorization process
        DbxAuthFinish authFinish = null;
        try {
	        switch (type) {
	            case SHORT_LIVE_TOKEN:
					authFinish = new ShortLiveTokenAuthorize().authorize(appInfo);
	                break;
	            case PKCE:
	                authFinish = new PkceAuthorize().authorize(appInfo);
	                break;
	            case SCOPE:
	                authFinish = new ScopeAuthorize().authorize(appInfo);
	                break;
	            default:
	                System.err.println("Error reading <mode> : " + type);
	        }
        } catch (IOException e) {
			e.printStackTrace();
		}

        System.out.println("Authorization complete.");
        System.out.println("- User ID: " + authFinish.getUserId());
        System.out.println("- Account ID: " + authFinish.getAccountId());
        System.out.println("- Access Token: " + authFinish.getAccessToken());
        System.out.println("- Expires At: " + authFinish.getExpiresAt());
        System.out.println("- Refresh Token: " + authFinish.getRefreshToken());
        System.out.println("- Scope: " + authFinish.getScope());

        // Save auth information the new DbxCredential instance. It also contains app_key and
        // app_secret which is required to do refresh call.
        DbxCredential credential = new DbxCredential(authFinish.getAccessToken(), authFinish
            .getExpiresAt(), authFinish.getRefreshToken(), appInfo.getKey(), appInfo.getSecret());
        File output = new File("");
        try {
            DbxCredential.Writer.writeToFile(credential, output);
            System.out.println("Saved authorization information to \"" + output.getCanonicalPath() + "\".");
        } catch (IOException ex) {
            System.err.println("Error saving to <auth-file-out>: " + ex.getMessage());
            System.err.println("Dumping to stderr instead:");
            try {
				DbxCredential.Writer.writeToStream(credential, System.err);
			} catch (IOException e) {
				e.printStackTrace();
			}
            return;
        }
	}
	
	static String generateAppInfoFile() {
		try {
			File appInfo = new File("DropBoxAppInfo.auth");
			if (!appInfo.exists()) {
				appInfo.createNewFile();
			}
			else {
				PgLog.info("DropBoxAppInfo.tmp already exists.");
				return appInfo.getPath();
			}
			PrintWriter out = new PrintWriter(appInfo);
			PgLog.info("Piggy Check AppInfoContent: " + AppInfoContent);		
			out.print(AppInfoContent);
			return appInfo.getPath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
}
