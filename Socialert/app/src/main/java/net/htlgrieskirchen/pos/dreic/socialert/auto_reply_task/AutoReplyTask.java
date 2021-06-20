package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AutoReplyTask implements Serializable {
    private HashMap<String, String> receiver; // K: phoneNumber, V: Name
    private String message; // reply message
    private boolean isCompleted;
    private Task_Type task_type;
    private boolean isActive;
    private String timeWhenSent;


    public AutoReplyTask(String message, Task_Type task_type, HashMap<String, String> receiver) {
        this.message = message;
        this.isCompleted = false;
        this.task_type = task_type;
        this.receiver = receiver;
        this.isActive = true;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public enum Task_Type {
        SMS
    }


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Task_Type getTask_type() {
        return task_type;
    }

    public HashMap<String, String> getReceiver() {
        return receiver;
    }

    public void setReceiver(HashMap<String, String> receiver) {
        this.receiver = receiver;
    }

    public String getReceiverFormatted() {
        if (receiver.isEmpty()) {
            return "Jeder";
        } else { // size is always 1
            String result = "";
            for (Map.Entry<String, String> entry : receiver.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    result = entry.getValue();
                } else {
                    result = entry.getKey();
                }
            }
            return result;
        }
    }

    public void setTask_type(Task_Type task_type) {
        this.task_type = task_type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoReplyTask that = (AutoReplyTask) o;
        return isCompleted == that.isCompleted &&
                Objects.equals(receiver, that.receiver) &&
                Objects.equals(message, that.message) &&
                task_type == that.task_type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiver, message, isCompleted, task_type);
    }

    public String getTimeWhenSent() {
        return timeWhenSent;
    }

    public void setTimeWhenSent(String timeWhenSent) {
        this.timeWhenSent = timeWhenSent;
    }
}
