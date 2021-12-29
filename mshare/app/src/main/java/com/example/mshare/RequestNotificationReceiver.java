package com.example.mshare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class RequestNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        FirebaseNotificationService.timer.cancel();
        Bundle bundle = intent.getExtras();
        if(bundle == null) Toast.makeText(context, "No Response!", Toast.LENGTH_SHORT).show();
        else{
            boolean isAccept = bundle.getBoolean("accept");
            if(isAccept) Toast.makeText(context, "Accept!", Toast.LENGTH_SHORT).show();
            else Toast.makeText(context, "Decline!", Toast.LENGTH_SHORT).show();
        }
    }
}