package com.example.mshare.utilClasses;

import android.app.Application;

public class ApplicationStatus extends Application {
    private static boolean isApplicationRunning;
    public static void setIsApplicationRunning(boolean value){
        isApplicationRunning = value;
    }
    public static boolean isIsApplicationRunning(){
        return isApplicationRunning;
    }
}
