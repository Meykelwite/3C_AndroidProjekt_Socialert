package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

// https://medium.com/@royanimesh2211/android-dialogfragment-to-activity-communication-fb652112850e
public interface TaskListener {
    void onAddTask(ScheduleTask task);

    void onEditTask(int position, ScheduleTask newTask);
}
