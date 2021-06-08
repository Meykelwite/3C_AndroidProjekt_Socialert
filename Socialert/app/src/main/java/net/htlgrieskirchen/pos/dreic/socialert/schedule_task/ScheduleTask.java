package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ScheduleTask implements Serializable {
    private String message;
    private String time; // LocalDateTime

    public ScheduleTask(String message, String time) {
        this.message = message;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
