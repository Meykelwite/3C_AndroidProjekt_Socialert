package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
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
        saveTasks();
    }

    public void removeTask(ScheduleTask task) {
        tasks.remove(task);
        saveTasks();
    }

    public void removeTask(int index) {
        tasks.remove(index);
        saveTasks();
    }

    public void markAsCompleted(ScheduleTask task) {
        task.setCompleted(true);
        saveTasks();
    }

    public void setTask(int index, ScheduleTask task) {
        tasks.set(index, task);
        saveTasks();
    }

    public void saveTasks() {
        String filename = SAVE_FILE_NAME;
        context.deleteFile(SAVE_FILE_NAME);
        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder
                    .create();

            String tasks_json = gson.toJson(tasks);

            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_APPEND);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
            out.print(tasks_json);
            out.flush();
            out.close();
        } catch (FileNotFoundException exp) {
            Toast.makeText(context, "Fehler beim Speichern", Toast.LENGTH_SHORT).show();
        }
    }


    public void loadTasks() {
        try {
            FileInputStream fis = context.openFileInput(SAVE_FILE_NAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String jsonString = br.readLine();
            Gson gson = new Gson();
            TypeToken<List<ScheduleTask>> token = new TypeToken<List<ScheduleTask>>() {
            };
            this.tasks = gson.fromJson(jsonString, token.getType());
        } catch (IOException ex) {
            //Toast.makeText(this, "Fehler beim Laden", Toast.LENGTH_SHORT).show();
        }
    }

    public List<ScheduleTask> getTasks() {
        return tasks;
    }

    public void setCompleted(ScheduleTask task, boolean completed) {
        task.setCompleted(completed);
        saveTasks();
    }
}
