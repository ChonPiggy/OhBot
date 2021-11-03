package ohbot;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;

import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;

import lombok.NonNull;
import ohbot.utils.PgLog;

public class LineMessagePrimitive {

    private static LineMessagingClient lineMessagingClient;
    private static LineBlobClient lineBlobClient;
    
    public static void setClient(LineMessagingClient client) {
    	if (lineMessagingClient == null) {
    		lineMessagingClient = client;
    	}
    }
    
    public static void handleHeavyContent(String replyToken, String messageId,
    		Consumer<MessageContentResponse> messageConsumer) {
    	final MessageContentResponse response;
    	try {
    		response = lineBlobClient.getMessageContent(messageId)
    				.get();
    	} catch (Exception e) {
    		PgLog.error("Cannot get image: " + e.getMessage());
    		throw new RuntimeException(e);
    	}
    	messageConsumer.accept(response);
}
    
    public static CompletableFuture<BotApiResponse> pushMessage(PushMessage message) {
    	return lineMessagingClient.pushMessage(message);
    }
    
    public static void replyImageCarouselTemplate(@NonNull String replyToken, String altText, @NonNull List<ImageCarouselColumn> columns) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        reply(replyToken, new TemplateMessage(altText, new ImageCarouselTemplate(columns)));
    }

    public static void replyLocation(@NonNull String replyToken, @NonNull String title, @NonNull String address, double latitude, double longitude) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }        
        reply(replyToken, new LocationMessage(title, address, latitude, longitude));
    }

    public static void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    public static void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        
        CompletableFuture<BotApiResponse> apiResponse = lineMessagingClient
                .replyMessage(new ReplyMessage(replyToken, messages));
        //PgLog.info("Sent messages: {} {}", apiResponse.message(), apiResponse.code());
        
    }
    
    public static String getUserDisplayName(String userid) {
        String strResult="";
        
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        if (userProfileResponse == null) {
            return "";
        }
        strResult = userProfileResponse.getDisplayName();
        
        return strResult;
    }
    public static URI getUserDisplayPicture(String userid) {
        URI strResult=null;
        
        UserProfileResponse userProfileResponse = getUserProfile(userid);
        if (userProfileResponse == null) {
            return null;
        }
        strResult = userProfileResponse.getPictureUrl();
        
        return strResult;
    }
    

    public static void leaveGroup(@NonNull String replyToken, @NonNull String groupId) {

        final BotApiResponse botApiResponse;
        try {
            botApiResponse = lineMessagingClient.leaveGroup(groupId).get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void leaveRoom(@NonNull String replyToken, @NonNull String roomId) {
        
        final BotApiResponse botApiResponse;
        try {
            botApiResponse = lineMessagingClient.leaveRoom(roomId).get();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static UserProfileResponse getUserProfile(@NonNull String userId) {
        try {
            CompletableFuture<UserProfileResponse> response = lineMessagingClient
                    .getProfile(userId);
                    //PgLog.info("Piggy Check response: " + response);
            return response.get();//TODO
        }catch (Exception e) {
            PgLog.info("Exception: " + e);
        }
        return null;
    }
}
