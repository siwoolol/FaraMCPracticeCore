package lol.siwoo.drizzyPracticeCore.status;

import com.eduardomcb.discord.webhook.WebhookClient;
import com.eduardomcb.discord.webhook.WebhookManager;
import com.eduardomcb.discord.webhook.models.Message;

public class WebhookMessage {
    public static void webhookMessage(String status) {
        String webhookUrl = "https://discord.com/api/webhooks/1360812857964630036/GSK9_79hvlJKwrPIzo5rwxb011ezCTRrCPRianhlc505ABan4SvVabMypL7lWUu0sbBE";
        String pfpUrl = "https://siwoo.lol/";

        Message message = new Message()
                .setAvatarUrl(pfpUrl)
                .setUsername("Server Status")
                .setContent("**Practice** is Currently " + status);

        WebhookManager webhookManager = new WebhookManager()
                .setChannelUrl(webhookUrl)
                .setMessage(message);

        webhookManager.setListener(new WebhookClient.Callback() {
            @Override
            public void onSuccess(String response) {
                System.out.println("message sent to webhook successfully");
            }

            @Override
            public void onFailure(int statusCode, String errorMessage) {
                System.out.println("error while trying to send webhook (code: " + statusCode + " error: " + errorMessage + ")");
            }
        });

        // webhookManager.exec();
    }
}
