package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms.SmsTask;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AutoReplyTaskManager {
    private static final String SAVE_FILE_NAME = "auto_reply_tasks.json";
    private Context context;
    private List<AutoReplyTask> tasks = new ArrayList<>();

    public AutoReplyTaskManager(Context context) {
        this.context = context;
        loadTasks();
    }

    public void addTask(AutoReplyTask task) {
        tasks.add(task);
        saveTasks();
    }

    public void removeTask(AutoReplyTask task) {
        tasks.remove(task);
        saveTasks();
    }

    public void markAsCompleted(AutoReplyTask task) {
        task.setCompleted(true);
        saveTasks();
    }

    public void setTask(int index, AutoReplyTask task) {
        tasks.set(index, task);
        saveTasks();
    }

    public void setTimeWhenSent(AutoReplyTask task, LocalDateTime time) {
        task.setTimeWhenSent(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(time));
        saveTasks();
    }

    public void setTimeWhenSent(AutoReplyTask task, String time) {
        task.setTimeWhenSent(time);
        saveTasks();
    }

    public void setActive(AutoReplyTask task, boolean isActive) {
        task.setActive(isActive);
        saveTasks();
    }

    public void saveTasks() {
        String filename = SAVE_FILE_NAME;
        context.deleteFile(SAVE_FILE_NAME);
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .create();

            List<SmsTask> smsTasks = smsTasks();
            String sms_tasks_json = gson.toJson(smsTasks);

            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.print(sms_tasks_json);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
        }
    }

    public List<SmsTask> smsTasks() {
        List<SmsTask> result = new ArrayList<>();
        for (AutoReplyTask task : tasks) {
            if (task instanceof SmsTask) {
                result.add((SmsTask) task);
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


        } catch (IOException ex) {
            //Toast.makeText(this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
        }
    }

    public List<AutoReplyTask> getTasks() {
        return tasks;
    }

    public AutoReplyTask getTaskAt(int index) {
        return tasks.get(index);
    }

    public void updateMessage(AutoReplyTask task, String message) {
        tasks.get(tasks.indexOf(task)).setMessage(message);
    }


}
