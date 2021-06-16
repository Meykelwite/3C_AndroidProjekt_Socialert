package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ScheduleTask implements Serializable {
    private HashMap<String, String> receivers; // K: phoneNumber, V: Name bei SMSTask bzw. K: E-Mail, V: Name bei EmailTask
    private String message;
    private String time; // LocalDateTime
    private boolean isCompleted;
    private Task_Type task_type;


    public ScheduleTask(String message, String time, Task_Type task_type, HashMap<String, String> receivers) {
        this.message = message;
        this.time = time;
        this.isCompleted = false;
        this.task_type = task_type;
        this.receivers = receivers;
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

    public LocalDateTime getTimeAsLocalDateTime() {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public enum Task_Type {
        EMAIL,
        SMS
    }

    public Task_Type getTask_type() {
        return task_type;
    }

    public HashMap<String, String> getReceivers() {
        return receivers;
    }

    public void setReceivers(HashMap<String, String> receivers) {
        this.receivers = receivers;
    }

    public String getReceiversFormatted() {
        String result = new String();
        int i = 0;
        for (Map.Entry<String, String> entry : receivers.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result += entry.getValue();
            } else {
                result += entry.getKey();
            }
            if (i < receivers.size() - 1) {
                result += ", ";
            }
            i++;
        }
        return result;
    }

    public void setTask_type(Task_Type task_type) {
        this.task_type = task_type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        ScheduleTask that = (ScheduleTask) o;
        return isCompleted == that.isCompleted &&
                receivers.equals(that.receivers) &&
                message.equals(that.message) &&
                time.equals(that.time) &&
                task_type == that.task_type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(receivers, message, time, isCompleted, task_type);
    }
}
