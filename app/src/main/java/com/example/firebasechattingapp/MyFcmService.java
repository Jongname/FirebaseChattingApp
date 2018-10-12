package com.example.firebasechattingapp;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.constraint.Constraints.TAG;

public class MyFcmService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG,"onMessageReceived ID" +remoteMessage.getMessageId() );
        Log.e(TAG,"onMessageReceived DATA :" + remoteMessage.getData());
    }
}
