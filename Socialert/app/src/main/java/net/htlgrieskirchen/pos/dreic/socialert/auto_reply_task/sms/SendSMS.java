package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms;

import android.telephony.SmsManager;

public class SendSMS {

    public void sendSMSMessage(String phoneNo, String message)
    {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
    }

}
