package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

// https://medium.com/@royanimesh2211/android-dialogfragment-to-activity-communication-fb652112850e
public interface TaskListener {
    void onAddTask(AutoReplyTask task);

    void onEditTask(int position, AutoReplyTask newTask);
}
