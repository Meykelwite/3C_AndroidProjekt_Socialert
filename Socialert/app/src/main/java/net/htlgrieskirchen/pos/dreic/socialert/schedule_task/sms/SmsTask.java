package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsTask extends ScheduleTask implements Serializable {

    public SmsTask(String message, String time, HashMap<String, String> receivers) {
        super(message, time, Task_Type.SMS, receivers);
    }

}
