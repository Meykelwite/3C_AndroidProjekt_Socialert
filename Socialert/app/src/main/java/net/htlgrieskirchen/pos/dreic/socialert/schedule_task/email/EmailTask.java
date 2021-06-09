package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EmailTask extends ScheduleTask implements Serializable {
    private String email;

    public EmailTask(String message, String time, String email) {
        super(message, time);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
