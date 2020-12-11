package ohbot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;


public class AnnouncementManager {
	
	private static String sAnnounceMessage = "";
	private static boolean sIsNeedExpired = false;
	
	private static class AnnounceGroup {
		private String groupId = "";
		private String announcedMessage = "";
		private boolean isAnnounced = false;
		private boolean isAnnounceImage = false;
		
		public AnnounceGroup(String group, String message) {
			groupId = group;
			if (message != null) {
				if (message.startsWith("http") && 
						(message.endsWith(".jpg") 
								|| message.endsWith(".jpeg") 
								|| message.endsWith(".png"))) {
					isAnnounceImage = true;
				}
				announcedMessage = message;
			}
			announcedMessage = "N/A";
		}
	}
	
	private static HashMap<String, AnnounceGroup> sAnnouncedGroups = new HashMap<>();
	private static Calendar sAnnounceExpiredTime;
	
	// expired time with minute
	public static void announceNewMessage(String message, boolean isNeedExpire, int expiredTime) {
		sAnnouncedGroups.clear();
		sAnnounceMessage = message;
		sIsNeedExpired = isNeedExpire;
		if (isNeedExpire) {
			sAnnounceExpiredTime = Calendar.getInstance(TimeZone.getDefault());
			sAnnounceExpiredTime.setTimeInMillis(System.currentTimeMillis()+((expiredTime)*60*1000));
		}
	}
	
	public static String removeAnnouncement() {
		sAnnouncedGroups.clear();
		String message = sAnnounceMessage;
		sAnnounceMessage = null;
		if (message == null) {
			return null;
		}
		return message;
		
	}
	
	public static String processAnnounceMessage(String groupId) {
		if (sAnnounceMessage == null) {
			return null;
		}
		
		if (!sAnnouncedGroups.containsKey(groupId)) {
			// Need announce
			if (sIsNeedExpired && sAnnounceExpiredTime != null) {
				Calendar current = Calendar.getInstance(TimeZone.getDefault());
	            current.setTimeInMillis(System.currentTimeMillis());
				if (current.getTimeInMillis() - sAnnounceExpiredTime.getTimeInMillis() > 0) {
					return null;
				}
				// not expired, need announce if needed.
				sAnnouncedGroups.put(groupId, new AnnounceGroup(groupId, sAnnounceMessage));
				return sAnnounceMessage;
			}
			else if (!sIsNeedExpired) {
				sAnnouncedGroups.put(groupId, new AnnounceGroup(groupId, sAnnounceMessage));
				return sAnnounceMessage;
			}
		}
		return null;
	}

}
