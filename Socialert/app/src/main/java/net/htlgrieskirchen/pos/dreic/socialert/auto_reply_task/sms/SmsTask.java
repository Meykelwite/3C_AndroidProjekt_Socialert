package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms;

import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.AutoReplyTask;

import java.io.Serializable;
import java.util.HashMap;

public class SmsTask extends AutoReplyTask implements Serializable {

    public SmsTask(String message, HashMap<String, String> receiver) {
        super(message, Task_Type.SMS, receiver);
    }

}
