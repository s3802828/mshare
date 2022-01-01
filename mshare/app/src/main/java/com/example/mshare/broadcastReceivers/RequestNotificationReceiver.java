package com.example.mshare.broadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
        if(bundle == null) {
//            Toast.makeText(context, "No Response!", Toast.LENGTH_SHORT).show();
            db.collection("rooms").document("c50rQqDlVtRcwgm5tG41")
                    .collection("request_response")
                    .document("BvlYluwhVXjvbm5Ww4tu")
                    .update("response", "no_response");
        }
        else{
            boolean isAccept = bundle.getBoolean("accept");
            if(isAccept) {
//                Toast.makeText(context, "Accept!", Toast.LENGTH_SHORT).show();
                db.collection("rooms").document("c50rQqDlVtRcwgm5tG41")
                        .collection("request_response")
                        .document("BvlYluwhVXjvbm5Ww4tu")
                        .update("response", "accept").addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        Intent intent1 = new Intent(context, SongListActivity.class);
                        context.startActivity(intent1);
                    }
                });
            }
            else {
//                Toast.makeText(context, "Decline!", Toast.LENGTH_SHORT).show();
                db.collection("rooms").document("c50rQqDlVtRcwgm5tG41")
                        .collection("request_response")
                        .document("BvlYluwhVXjvbm5Ww4tu")
                        .update("response", "decline");
            }
        }
    }
}