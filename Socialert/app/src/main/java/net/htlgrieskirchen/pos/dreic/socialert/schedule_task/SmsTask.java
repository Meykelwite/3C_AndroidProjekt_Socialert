package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SmsTask extends ScheduleTask implements Serializable {
    private String phoneNumber;

    public SmsTask(String message, String time, String phoneNumber) {
        super(message, time);
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
