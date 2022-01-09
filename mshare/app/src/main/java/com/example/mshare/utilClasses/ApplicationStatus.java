package com.example.mshare.utilClasses;

import android.app.Application;

public class ApplicationStatus extends Application {
    private static boolean isApplicationRunning;
    private static String currentToken;
    public static void setIsApplicationRunning(boolean value){
        isApplicationRunning = value;
    }
    public static boolean isIsApplicationRunning(){
        return isApplicationRunning;
    }

    public static String getCurrentToken() {
        return currentToken;
    }

    public static void setCurrentToken(String currentToken) {
        ApplicationStatus.currentToken = currentToken;
    }
}
