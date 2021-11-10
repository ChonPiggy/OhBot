package dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderContinueErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import ohbot.utils.PgLog;

public class DropBoxAuthorizer {
	
	static String AppInfoContent = "{\n" + 
								   "   \"key\": \""+System.getenv("DROPBOX_API_KEY")+"\",\"\n" +
								   "   \"secret\": \""+System.getenv("DROPBOX_API_SECRET")+"\",\"\n" +
								   "}";
	static String AppInfoPath = "DropBoxAppInfo.auth";
	static String AuthFileOutputPath = "AuthFileOutput.auth";
	
	public final static int SHORT_LIVE_TOKEN = 1;
	public final static int PKCE = 2;
	public final static int SCOPE = 3;
	
	
	public static void generateAuthFileOutput(int type) {
		
		
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
