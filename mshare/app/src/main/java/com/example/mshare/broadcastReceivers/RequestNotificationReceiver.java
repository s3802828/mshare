package com.example.mshare.broadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mshare.MediaPlayerActivity;
import com.example.mshare.SongListActivity;
import com.example.mshare.services.FirebaseNotificationService;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class RequestNotificationReceiver extends BroadcastReceiver {
    protected FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        FirebaseNotificationService.timer.cancel();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        Bundle bundle = intent.getExtras();
        String roomId = bundle.getString("room_id");
        System.out.println(bundle.containsKey("room_id"));
        if(!bundle.containsKey("accept")) {
            db.collection("rooms").document(roomId)
                    .collection("request_response")
                    .document(roomId)
                    .update("response", "no_response");
        }
        else {
            boolean isAccept = bundle.getBoolean("accept");
            if(isAccept) {
                Intent intent1 = new Intent(context, MediaPlayerActivity.class);
                intent1.putExtra("isSharingMode", true);
                intent1.putExtra("room_id", roomId);
                context.startActivity(intent1);
            }
            else {
                db.collection("rooms").document(roomId)
                        .collection("request_response")
                        .document(roomId)
                        .update("response", "decline");
            }
        }
    }
}