package com.example.mshare.broadcastReceivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mshare.services.FirebaseNotificationService;

public class RequestNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        FirebaseNotificationService.timer.cancel();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        Bundle bundle = intent.getExtras();
        if(bundle == null) Toast.makeText(context, "No Response!", Toast.LENGTH_SHORT).show();
        else{
            boolean isAccept = bundle.getBoolean("accept");
            if(isAccept) Toast.makeText(context, "Accept!", Toast.LENGTH_SHORT).show();
            else Toast.makeText(context, "Decline!", Toast.LENGTH_SHORT).show();
        }
    }
}