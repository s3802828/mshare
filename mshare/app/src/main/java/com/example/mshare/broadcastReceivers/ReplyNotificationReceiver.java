package com.example.mshare.broadcastReceivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.mshare.ChatActivity;
import com.example.mshare.R;
import com.example.mshare.models.User;
import com.example.mshare.services.FirebaseNotificationService;
import com.example.mshare.utilClasses.ApplicationStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;

public class ReplyNotificationReceiver extends BroadcastReceiver {
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();
    protected FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String content;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(101);
        Bundle bundle = intent.getExtras();
        Boolean noReply = bundle.getBoolean("noReply");
        if (noReply) {
            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            chatIntent.putExtra("User", bundle.getSerializable("User"));
//            if(!ApplicationStatus.isIsApplicationRunning()) {
//                chatIntent.putExtra("From Background",true);
//            }
            context.startActivity(chatIntent);
        } else {
            String senderId = bundle.getString("senderId");
            String receiverId = bundle.getString("receiverId");
            Bundle results = RemoteInput.getResultsFromIntent(intent);
            if (results != null) {
                CharSequence quickReplyResult = results.getCharSequence("key_reply");
                content = quickReplyResult.toString();

            }
            String receiverName = bundle.getString("receiverName");
            String conversationId =
                    senderId.compareTo(receiverId) > 0 ? senderId + receiverId
                            : receiverId + senderId;
            if (!content.trim().isEmpty()) {
                HashMap<String, Object> message = new HashMap<>();
                message.put("senderId", firebaseAuth.getUid());
                message.put("receiverId", receiverId);
                message.put("content", content);
                message.put("timestamp", new Date());

                //add message to Firestore
                db.collection("conversation").document(conversationId)
                        .collection("messages").add(message);

                //update the last message fields in Conversation Collection
                HashMap<String, Object> lastMessage = new HashMap<>();
                lastMessage.put("lastMessage_senderId", firebaseAuth.getUid());
                lastMessage.put("lastMessage_senderName", firebaseAuth.getCurrentUser().getDisplayName());
                lastMessage.put("lastMessage_receiverId", receiverId);
                lastMessage.put("lastMessage_receiverName", receiverName);
                lastMessage.put("lastMessage", content);
                lastMessage.put("timestamp", new Date());
                lastMessage.put("lastMessage_receiverAvatar",
                        "https://firebasestorage.googleapis.com/v0/b/androiddev-cbbc9.appspot.com/o/linux-avatar-by-qubodup-just-a-normal-tux-penguin-Z4TPDs-clipart.png?alt=media&token=4808957a-e786-4f03-b8c1-800d50d93b7d");
                db.collection("conversation").document(conversationId).set(lastMessage);
//                notificationManager.cancelAll();
                notificationManager.notify(101, new NotificationCompat.Builder(context,
                        "message_notification")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setAutoCancel(true)
                        .build());
                notificationManager.cancel(101);

            }
        }


    }
}