package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.htlgrieskirchen.pos.dreic.socialert.ScheduleTaskBroadcastReceiver;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ScheduleTaskManager {
    private static final String SAVE_FILE_NAME = "schedule_tasks.json";
    private Context context;
    private List<ScheduleTask> tasks = new ArrayList<>();

    public ScheduleTaskManager(Context context) {
        this.context = context;
        loadTasks();
    }

    public void addTask(ScheduleTask task) {
        tasks.add(task);
        startAlert(task);
        saveTasks();
    }

    public void removeTask(ScheduleTask task) {
        tasks.remove(task);
        cancelAlert(task);
        saveTasks();
    }

    public void markAsCompleted(ScheduleTask task) {
        task.setCompleted(true);
        cancelAlert(task);
        saveTasks();
    }

    public void setTask(int index, ScheduleTask task) {
        cancelAlert(tasks.get(index));
        tasks.set(index, task);
        startAlert(task);
        saveTasks();
    }

    public void saveTasks() {
        String filename = SAVE_FILE_NAME;
        context.deleteFile(SAVE_FILE_NAME);
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .create();

            String sms_tasks_json = gson.toJson(smsTasks());
            String email_tasks_json = gson.toJson(emailTasks());

            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.println(sms_tasks_json);
            out.println(email_tasks_json);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
        }
    }

    private List<SmsTask> smsTasks() {
        List<SmsTask> result = new ArrayList<>();
        for (ScheduleTask task : tasks) {
            if (task instanceof SmsTask) {
                result.add((SmsTask) task);
            }
        }
        return result;
    }

    private List<EmailTask> emailTasks() {
        List<EmailTask> result = new ArrayList<>();
        for (ScheduleTask task : tasks) {
            if (task instanceof EmailTask) {
                result.add((EmailTask) task);
            }
        }
        return result;
    }


    public void loadTasks() {
        try {
            FileInputStream fis = context.openFileInput(SAVE_FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String sms_tasks_json = br.readLine();
            Gson gson = new Gson();
            TypeToken<List<SmsTask>> smsToken = new TypeToken<List<SmsTask>>() {
            };
            tasks.addAll(gson.fromJson(sms_tasks_json, smsToken.getType()));

            String email_tasks_gson = br.readLine();
            TypeToken<List<EmailTask>> token = new TypeToken<List<EmailTask>>() {
            };
            tasks.addAll(gson.fromJson(email_tasks_gson, token.getType()));


        } catch (IOException ex) {
            //Toast.makeText(this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
        }
    }

    public List<ScheduleTask> getTasks() {
        tasks.sort((o1, o2) -> o1.getTimeAsLocalDateTime().compareTo(o2.getTimeAsLocalDateTime()));
        return tasks;
    }

    public ScheduleTask getTaskAt(int index) {
        tasks.sort((o1, o2) -> o1.getTimeAsLocalDateTime().compareTo(o2.getTimeAsLocalDateTime()));
        return tasks.get(index);
    }

    public void updateMessage(ScheduleTask task, String message) {
        tasks.get(tasks.indexOf(task)).setMessage(message);
    }

    // https://www.javatpoint.com/android-alarmmanager
    public void startAlert(ScheduleTask task) {
        if (!task.isCompleted()) {
            PendingIntent pendingIntent = getPendingIntent(task);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            // RTC - Fires the pending intent at the specified time but does not wake up the device.
            //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
            //+ (3 * 1000), pendingIntent);
            alarmManager.set(AlarmManager.RTC_WAKEUP, task.getTimeAsLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pendingIntent);
        }
    }

    private PendingIntent getPendingIntent(ScheduleTask task) {
        Intent intent = new Intent(context, ScheduleTaskBroadcastReceiver.class);
        intent.putExtra("pos", tasks.indexOf(task));
//        intent.putExtra("receivers", new ArrayList<>(task.getReceivers().keySet()));
//        intent.putExtra("message", task.getMessage());
//        intent.putExtra("task_type", task.getTask_type().toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, task.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    public void cancelAlert(ScheduleTask task) {
        if (!task.isCompleted()) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = getPendingIntent(task);
            alarmManager.cancel(pendingIntent);
        }
    }


}
