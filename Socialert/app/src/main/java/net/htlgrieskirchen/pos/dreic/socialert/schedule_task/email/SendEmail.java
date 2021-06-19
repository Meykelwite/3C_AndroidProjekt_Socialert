package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;


import com.google.api.client.util.Base64;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;


public class SendEmail extends Service {
//    private String subject;
//    private String message;
//    GoogleSignInAccount account;
//
//    Gmail service;

    public SendEmail(String subject, String message) {
        //this.subject = subject;
        //this.message = message;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;


    }

    @Override
    public void onCreate() {
        super.onCreate();


    }


}
