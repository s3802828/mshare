package com.example.mshare.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.mshare.ChatActivity;
import com.example.mshare.R;
import com.example.mshare.broadcastReceivers.RequestNotificationReceiver;
import com.example.mshare.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseNotificationService extends FirebaseMessagingService {
    private NotificationManager notificationManager;
    public static CountDownTimer timer;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//        System.out.println("Hello");
        String receiverId = remoteMessage.getData().get("receiverId");
        String body = remoteMessage.getData().get("body");
        System.out.println(body);
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currUser != null) {
            assert receiverId != null;
            if (receiverId.equals(currUser.getUid())) {
                if (body == null) {
                    sendNotification(remoteMessage);
                } else sendMessageNotification(remoteMessage);
            }
        }

    }

    //Create a notification channels
    private void createNotificationChannels() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel("request_notification",
                "Request Notification", NotificationManager.IMPORTANCE_HIGH);
        channel1.setDescription("This is Request Notification Channel");
        notificationManager.createNotificationChannel(channel1);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        if (notificationManager == null) createNotificationChannels();
        String senderId = remoteMessage.getData().get("senderId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intent noResponseIntent = new Intent(this, RequestNotificationReceiver.class);
        Intent acceptIntent = new Intent(this, RequestNotificationReceiver.class);
        acceptIntent.putExtra("accept", true);
        PendingIntent pendingAcceptIntent = PendingIntent.getBroadcast(this, 100, acceptIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent declineIntent = new Intent(this, RequestNotificationReceiver.class);
        declineIntent.putExtra("accept", false);
        PendingIntent pendingDeclineIntent = PendingIntent.getBroadcast(this, 200, declineIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(null, "ACCEPT", pendingAcceptIntent)
                .build();
        NotificationCompat.Action declineAction = new NotificationCompat.Action.Builder(null, "DECLINE", pendingDeclineIntent)
                .build();

        assert senderId != null;
        db.collection("users").document(senderId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String senderName = documentSnapshot.getString("name");
                Notification notification = new NotificationCompat.Builder(FirebaseNotificationService.this,
                        "request_notification")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("NEW REQUEST")
                        .setContentText(senderName + " has sent you request")
                        .addAction(acceptAction)
                        .addAction(declineAction)
                        .build();
                notificationManager.notify(0, notification);
                timer = new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        notificationManager.cancel(0);
                        sendBroadcast(noResponseIntent);
                    }
                }.start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void sendMessageNotification(RemoteMessage remoteMessage) {
        //create channel
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel1 = new NotificationChannel("message_notification",
                    "Message Notification", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is Request Message Notification Channel");
            notificationManager.createNotificationChannel(channel1);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String senderId = remoteMessage.getData().get("senderId");
        Intent intent = new Intent(this, ChatActivity.class);
        db.collection("users").document(senderId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                        String senderName = documentSnapshot.getString("name");
                        User user = new User();
                        user.setId(senderId);
                        String body = remoteMessage.getData().get("body");
                        user.setName(senderName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("User", user);

                        Notification notification = new NotificationCompat.Builder(FirebaseNotificationService.this,
                                "message_notification")
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setContentTitle(senderName)
                                .setContentText(body)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setContentIntent(PendingIntent.getActivity(FirebaseNotificationService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                .setAutoCancel(true)
                                .build();
                        notificationManager.notify(1, notification);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

}