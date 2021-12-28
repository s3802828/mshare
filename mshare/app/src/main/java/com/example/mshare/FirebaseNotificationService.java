package com.example.mshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

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
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String receiverId = remoteMessage.getData().get("receiverId");
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currUser != null) {
            assert receiverId != null;
            if (receiverId.equals(currUser.getUid())) sendNotification(remoteMessage);
        }

    }
    //Create a notification channels
    private void createNotificationChannels() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel1 = new NotificationChannel("request_notification",
                "Request Notification", NotificationManager.IMPORTANCE_HIGH);
        channel1.setDescription("This is Site Change Channel");
        notificationManager.createNotificationChannel(channel1);
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        if(notificationManager == null) createNotificationChannels();
//                Intent intent = new Intent(this, MapsActivity.class);
//                intent.setAction(Intent.ACTION_MAIN); //When user click on notification, the map won't start again
//                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //Put more info to intent to launch action after user click on notification
//                intent.putExtra("from_notification", true);
//                intent.putExtra("to_volunteers", false);
//                intent.putExtra("notification_id", lCursor.getInt(0));
//                intent.putExtra("receiver_id", lCursor.getInt(1));
//                intent.putExtra("site_id", Integer.parseInt(lCursor.getString(2)
//                        .substring(lCursor.getString(2).lastIndexOf("(NO.") + 4,
//                                lCursor.getString(2).length() - 1)));
//                //For user to click on notification
//                PendingIntent pendingIntent = PendingIntent.getActivity(this, lCursor.getInt(0), intent, PendingIntent.FLAG_IMMUTABLE);
                //Show all leader notifications to current user
        String senderId = remoteMessage.getData().get("senderId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        assert senderId != null;
        db.collection("users").document(senderId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                String senderName = documentSnapshot.getString("name");
                Notification notification = new NotificationCompat.Builder(FirebaseNotificationService.this,
                        "request_notification")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("NEW REQUEST")
                        .setContentText(senderName + "has sent you request")
//                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_lead_site))
//                        .setStyle(new NotificationCompat.BigTextStyle().bigText(lCursor.getString(2)))
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setContentIntent(pendingIntent)
//                        .setAutoCancel(true)
//                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManager.notify(0, notification);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

            }
}