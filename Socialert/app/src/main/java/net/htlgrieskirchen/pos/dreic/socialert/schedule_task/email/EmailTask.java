package net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmailTask extends ScheduleTask implements Serializable {

    private String subject;
    private String accountName;
    private String sendResult;

    public EmailTask(String message, String time, HashMap<String, String> receivers, String subject, String accountName) {
        super(message, time, Task_Type.EMAIL, receivers);
        this.subject = subject;
        this.accountName = accountName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmailTask emailTask = (EmailTask) o;
        return Objects.equals(subject, emailTask.subject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subject);
    }

    public String getAccountName() {
        return accountName;
    }

    public String getSendResult() {
        return sendResult;
    }

    public void setSendResult(String sendResult) {
        this.sendResult = sendResult;
    }
}
