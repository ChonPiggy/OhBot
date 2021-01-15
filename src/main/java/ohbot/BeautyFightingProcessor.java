package ohbot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ohbot.OhBotController.PttBeautyGirl;
import ohbot.utils.PgLog;

public abstract class BeautyFightingProcessor {
	
	public static String PREFIX = "BeautyFightingData:";
	public static String VOTE = "Vote:";

	/*
	 * String senderId
	 * BeautyFightingInfo each instance for each group
	 */
	private HashMap<String, BeautyFightingInfo> mBeautyFightingMap = new HashMap<>(); 
	private byte[] lock = new byte[0];

	// senderId means groupId
	public boolean processBeautyFightingString(String replyToken, String senderId, String userId, String text) {
		if (text.equals("開始表特好球帶")){
			if (!mBeautyFightingMap.containsKey(senderId)) {
				// This room didn't start a fight
				mBeautyFightingMap.put(senderId, new BeautyFightingInfo(userId, senderId));
				sendTextReply(replyToken, BeautyFightingProcessor.getGuideString(userId));
				return true;
			}
			else {
				// This room already start a fight
				sendTextReply(replyToken, "這個房間已經開始一場囉");
				return true;
			}
		}
		if (mBeautyFightingMap.containsKey(senderId)) {
			BeautyFightingInfo info = mBeautyFightingMap.get(senderId);
			if (text.equals("參加")) {
				if (LineMessagePrimitive.getUserDisplayName(userId).equals("")) {
					sendTextReply(replyToken, "請先將 BOT 加為好友.");
		            return true;
		        }
				if (info.isPlayerJoined(userId)) {
					sendTextReply(replyToken, "您已經參加了");
					return true;
				}
				else {
					if (info.isStarted()) {
						sendTextReply(replyToken, "遊戲已經開始囉");
						return true;
					}
					info.join(userId);
					sendTextReply(replyToken, "加入成功!\n目前名單:\n" + info.getPlayerListDump());
					return true;
				}
			}
			/*else if (text.equals("退出")) {
				if (LineMessagePrimitive.getUserDisplayName(userId).equals("")) {
					sendTextReply(replyToken, "請先將 BOT 加為好友.");
		            return true;
		        }
				if (info.isPlayerJoined(userId)) {
					info.leave(userId);
					sendTextReply(replyToken, "退出成功! 目前名單:\n" + info.getPlayerListDump());
					return true;
				}
				else {
					sendTextReply(replyToken, "您之前沒有加入過遊戲唷");
					return true;
				}
			}*/
			else if (text.equals("開始")) {
				if (info.isStarted()) {
					sendTextReply(replyToken, "遊戲已經開始囉");
					return true;
				}
				if (userId.equals(info.getStarter())) {
					info.setBeautyData(getPttBeautyData(), BeautyFightingInfo.LEFT);
					info.setBeautyData(getPttBeautyData(), BeautyFightingInfo.RIGHT);
					info.setStarted();
					sendImageReply(replyToken, info.getLeftPicUrl(), info.getRightPicUrl(), info.getRounds());
					return true;
				}
				else {
					sendTextReply(replyToken, "只能由遊戲發起者 " + LineMessagePrimitive.getUserDisplayName(info.getStarter()) + " 開始遊戲");
					return true;
				}
			}	
			else if (text.equals("結束")) {
				if (userId.equals(info.getStarter())) {
					String result = info.close();
					mBeautyFightingMap.remove(senderId);
					sendTextReply(replyToken, result);
					return true;
				}
				else {
					sendTextReply(replyToken, "只能由遊戲發起者 " + LineMessagePrimitive.getUserDisplayName(info.getStarter()) + " 終止遊戲");
					return true;
				}
			}			
			else if (text.startsWith("設定回合:")) {
				if (userId.equals(info.getStarter())) {
					if (info.isStarted()) {
						sendTextReply(replyToken, "遊戲已經開始囉");
						return true;
					}
					int rounds = -1;
					try {
						rounds = Integer.parseInt(text.replace("設定回合:", ""));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					if (rounds == -1) {
						sendTextReply(replyToken, "指令錯誤");
					}
					if (rounds > 100) {
						rounds = 100;
					}
					if (rounds < 5) {
						rounds = 5;
					}
					info.setMaxRounds(rounds);
					sendTextReply(replyToken, "設定成功! 目前最大回合數為 " + rounds);
					return true;
				}
				else {
					sendTextReply(replyToken, "只能由遊戲發起者 " + info.getStarter() + " 設定回合數");
					return true;
				}
			}
			else if (text.equals("開啟局中進度顯示")) {
				if (userId.equals(info.getStarter())) {
					info.setIsShowDetailEveryRound(true);
					sendTextReply(replyToken, "設定成功");
					return true;
				}
				else {
					sendTextReply(replyToken, "只能由遊戲發起者 " + info.getStarter() + " 做設定");
					return true;
				}
			}
			else if (text.equals("關閉局中進度顯示")) {
				if (userId.equals(info.getStarter())) {
					info.setIsShowDetailEveryRound(false);
					sendTextReply(replyToken, "設定成功");
				}
				else {
					sendTextReply(replyToken, "只能由遊戲發起者 " + info.getStarter() + " 做設定");
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean processBeautyFightingPostback(String replyToken, String senderId, String userId, String data) {
		PgLog.info("Piggy Check processBeautyFightingPostback: " + data);
		if (data.startsWith(BeautyFightingProcessor.PREFIX)) {
			synchronized (lock) {
				if (mBeautyFightingMap.containsKey(senderId)) {
					BeautyFightingInfo info = mBeautyFightingMap.get(senderId);
					data = data.substring(BeautyFightingProcessor.PREFIX.length());
					if (data.startsWith(BeautyFightingProcessor.VOTE)) {
						data = data.substring(BeautyFightingProcessor.VOTE.length());
						/*if (data.equals(BeautyFightingInfo.LEFT)) {
							info.updateAnswer(userId, BeautyFightingInfo.LEFT);
						}
						else if (data.equals(BeautyFightingInfo.RIGHT)) {
							info.updateAnswer(userId, BeautyFightingInfo.RIGHT);
						}*/
						if (!info.isPlayerAnswered(userId)) {
							info.updateAnswer(userId, data);
							PgLog.info("Piggy Check list: " + info.getPlayerListDump());
						}
						else {
							return true;
						}
						if (info.isThisRoundFinished()) {
							// Send next Q
							if (info.getRounds()>=info.getMaxRounds()) {
								String result = info.close();
								sendTextReply(replyToken, result);
							}
							else {
								info.updateRound(getPttBeautyData());
								sendImageReply(replyToken, info.getLeftPicUrl(), info.getRightPicUrl(), info.getRounds());
							}
						}
						else {
							if (info.isShowDetailEveryRound()) {
								sendTextReply(replyToken, info.getPlayerVoteList());
							}
						}
					}
				}
			}
			return true;
		}
		
		return false;
	}
	
	public abstract PttBeautyGirl getPttBeautyData();
	public abstract boolean sendTextReply(String replyToken, String text);
	public abstract boolean sendImageReply(String replyToken, String leftPic, String rightPic, int rounds);
	
	public static String getGuideString(String starter) {
		String result = "發起人:" + LineMessagePrimitive.getUserDisplayName(starter) + "\n";
		result += "遊戲:表特好球帶\n\n";
		result += "玩家指令:\n";
		result += "說出\"參加\"可以加入遊戲(開始後不能加入)\n\n";
		/*result += "說出\"退出\"可以退出遊戲(遊戲中可退出)\n\n";*/
		result += "發起人指令:\n";
		result += "說出\"設定回合:10\"可設定回合數\n";
		result += "最大值為100 最小值5\n\n";
		result += "說出\"設定回合:數字\"可設定回合數\n";
		result += "最大值為100 最小值5\n\n";
		result += "說出\"開啟局中進度顯示\"可設定狀態\n";
		result += "\"關閉局中進度顯示\"可關閉(預設為關)\n";
		result += "說出\"開始\"可以開始遊戲(遊戲中可退出)\n\n";
		result += "發起人說出\"結束\"可強制結束遊戲\n\n";
		result += "遊戲開始後點擊你喜歡的照片\n\n";
		result += "全部回合結束後選出這回合的優勝者\n\n";
		return result;
	}
	
	public class BeautyFightingInfo {
		private String mStarter = "";
		private String mGroupId = "";
		private int mRounds = 1;
		private int mMaxRounds = 15;
		private boolean mIsShowDetailEveryRound = true;
		public static final String LEFT = "LEFT";
		public static final String RIGHT = "RIGHT";
		public static final String NONE = "NONE";
		
		private String mLeftBeautyPicUrl = "";
		private String mRightBeautyPicUrl = "";
		
		private String mLeftBeautyUrl = "";
		private String mRightBeautyUrl = "";
		
		private String mLastRoundWinner = NONE;
				
		/*
		 * String userId
		 * String answer
		 */
		private HashMap<String, String> mPlayerMap = new HashMap<>();
		private boolean mIsFinished = false;
		private boolean mIsStart = false;
		private BeautyFightingInfo(String starter, String group) {
			mStarter = starter;
			mGroupId = group;
			join(starter);
		}
		
		public void join(String userId) {
			synchronized (lock) {
				mPlayerMap.put(userId, NONE);
			}
		}
		
		public void leave(String userId) {
			synchronized (lock) {
				mPlayerMap.remove(userId);
			}
		}

		public boolean updateAnswer(String userId, String answer) {
			synchronized (lock) {
				if (mPlayerMap.containsKey(userId)) {
					mPlayerMap.put(userId, answer);
					return true;
				}
			}
			return false;
		}
		
		public boolean isPlayerAnswered(String userId) {
			synchronized (lock) {
				if (mPlayerMap.containsKey(userId)) {
					if (!mPlayerMap.get(userId).equals(NONE)) {
						return true;
					}
				}
			}
			return false;
		}
		
		public boolean isPlayerJoined(String userId) {
			synchronized (lock) {
				return mPlayerMap.containsKey(userId);
			}
		}
		
		public void resetAnswerMap() {
			synchronized (lock) {
				for (String key : mPlayerMap.keySet()) {
				    mPlayerMap.put(key, NONE);
				}
			}

		}
		
		public void setMaxRounds(int round) {
			mMaxRounds = round;
		}
		
		public int getMaxRounds() {
			return mMaxRounds;
		}
		
		public boolean isShowDetailEveryRound() {
			return mIsShowDetailEveryRound;
		}
		
		public void setIsShowDetailEveryRound(boolean enable) {
			mIsShowDetailEveryRound = enable;
		}
		
		public String getPlayerListDump() {
			String result = "";
			result += "發起者:" + LineMessagePrimitive.getUserDisplayName(mStarter) + "\n";
			result += "玩家清單:\n";
			synchronized (lock) {
				for (String key : mPlayerMap.keySet()) {
					result += "" + LineMessagePrimitive.getUserDisplayName(key) + "\n";
				}
			}
			return result;
		}
		
		public String getPlayerVoteList() {
			String roundsInfo = "第 " + mRounds + " 回合\n";
			synchronized (lock) {
				String leftVotes = "左邊:\n";
				for (String key : mPlayerMap.keySet()) {
					if (mPlayerMap.get(key).equals(LEFT)) {
						leftVotes += "" + LineMessagePrimitive.getUserDisplayName(key) + "\n";
					}
				}
				
				String rightVotes = "右邊:\n";
				for (String key : mPlayerMap.keySet()) {
					if (mPlayerMap.get(key).equals(RIGHT)) {
						rightVotes += "" + LineMessagePrimitive.getUserDisplayName(key) + "\n";
					}
				}
				
				String noVotes = "未投票:\n";
				for (String key : mPlayerMap.keySet()) {
					if (mPlayerMap.get(key).equals(NONE)) {
						noVotes += "" + LineMessagePrimitive.getUserDisplayName(key) + "\n";
					}
				}	
				return roundsInfo+"\n"+leftVotes+"\n"+rightVotes+"\n"+noVotes;
			}
		}
		
		public int getLeftVotes() {
			int count = 0;
			for (String key : mPlayerMap.keySet()) {
				if (mPlayerMap.get(key).equals(LEFT)) {
					count++;
				}
			}
			return count;
		}
		
		public int getRightVotes() {
			int count = 0;
			synchronized (lock) {
				for (String key : mPlayerMap.keySet()) {
					if (mPlayerMap.get(key).equals(RIGHT)) {
						count++;
					}
				}
				return count;
			}
		}
		
		public boolean isThisRoundFinished() {
			synchronized (lock) {
				int leftVotes = getLeftVotes();
				int rightVotes = getRightVotes();
				int players = getPlayerCount();
				int win = players/2;
				boolean isPlayersEven = players%2==0;
				if (!isPlayersEven) {
					win++;
				}
				if(leftVotes>=win||rightVotes>=win) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		
		public boolean updateRound(PttBeautyGirl beauty) {
			int leftVotes = getLeftVotes();
			int rightVotes = getRightVotes();
			int players = getPlayerCount();
			int win = players/2;
			boolean isPlayersEven = players%2==0;
			if (!isPlayersEven) {
				win++;
			}
			if(leftVotes>=win) {
				// Left win
				mLastRoundWinner = LEFT;
			}
			if(rightVotes>=win) {
				// Right win
				mLastRoundWinner = RIGHT;
			}
			if(leftVotes == rightVotes) {
				// Newest win
				if (mLastRoundWinner.equals(RIGHT)) {
					mLastRoundWinner = LEFT;
				}
				else if (mLastRoundWinner.equals(LEFT)) {
					mLastRoundWinner = RIGHT;
				}
			}
			// Update Q
			resetAnswerMap();
			if (mLastRoundWinner.equals(LEFT)) {
				setBeautyData(beauty, RIGHT);
				mRounds++;
				return true;
			}
			else if (mLastRoundWinner.equals(RIGHT)) {
				setBeautyData(beauty, LEFT);
				mRounds++;
				return true;
			}
			else {
				PgLog.info("Piggy Check updateRound error");
				return false;
			}
		}
		
		public int getPlayerCount() {
			return mPlayerMap.size();
		}

		public String getStarter() {
			return mStarter;
		}
		
		public boolean isStarted() {
			return mIsStart;
		}
		
		public void setStarted() {
			mIsStart = true;
		}

		public String close() {
			mIsFinished = true;
			String result = getPlayerVoteList();
			result += "最終獲勝者:\n";
			if (getLeftVotes() > getRightVotes()) {
				result+=getLeftDataUrl();
			}
			else if (getLeftVotes() < getRightVotes()) {
				result+=getRightDataUrl();
			}
			else if (getLeftVotes() == getRightVotes()) {
				if (mLastRoundWinner.equals(LEFT)) {
					result+=getRightDataUrl();
				}
				else if (mLastRoundWinner.equals(RIGHT)) {
					result+=getLeftDataUrl();
				}
				else {
					result+="從缺";
				}
			}
			return result;
		}
		
		public void setBeautyData(PttBeautyGirl beauty, String position) {
			List<String> list = beauty.getPicUrlList();
			String url = list.get(0);
			int count = 10;
			if (!position.equals(LEFT) && !position.equals(RIGHT)) {
				return;
			}
			do {
				Random randomGenerator = new Random();
	            int index = randomGenerator.nextInt(list.size());
	            url = list.get(index);
	            if (count < 0) {
	            	PgLog.info("setBeautyData reach max count, get data again.");
	            	beauty = getPttBeautyData();
	            	count = 10;
	            	continue;
	            }
	            count--;
			}while (url.contains(".gif"));
			
            if (position.equals(LEFT)) {
            	setLeftPicUrl(url);
            	setLeftDataUrl(beauty.getResultUrl());
            }
            if (position.equals(RIGHT)) {
            	setRightPicUrl(url);
            	setRightDataUrl(beauty.getResultUrl());
            }
		}
		
		public int getRounds() {
			return mRounds;
		}
		
		public String getLeftPicUrl() {
			return mLeftBeautyPicUrl;
		}
		
		public String getRightPicUrl() {
			return mRightBeautyPicUrl;
		}
		
		public String getLeftDataUrl() {
			return mLeftBeautyUrl;
		}
		
		public String getRightDataUrl() {
			return mRightBeautyUrl;
		}
		
		public void setLeftPicUrl(String url) {
			mLeftBeautyPicUrl = url;
		}
		
		public void setRightPicUrl(String url) {
			mRightBeautyPicUrl = url;
		}
		
		public void setLeftDataUrl(String url) {
			mLeftBeautyUrl = url;
		}
		
		public void setRightDataUrl(String url) {
			mRightBeautyUrl = url;
		}
	}
}
