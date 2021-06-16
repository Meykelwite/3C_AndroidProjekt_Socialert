package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailTask extends ScheduleTask implements Serializable {

    public EmailTask(String message, String time, HashMap<String, String> receivers) {
        super(message, time, Task_Type.EMAIL, receivers);
    }
}
