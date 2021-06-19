package net.htlgrieskirchen.pos.dreic.socialert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskManager;

import java.util.List;

// https://stackoverflow.com/questions/17673746/start-alarmmanager-if-device-is-rebooted
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Toast.makeText(context, "Text hallo ZEIT", Toast.LENGTH_LONG).show();
            // Do your work related to alarm manager
            //setUpScheduleTasksAlerts(context);
        }

    }

    private void setUpScheduleTasksAlerts(Context context) {
        ScheduleTaskManager scheduleTaskManager = new ScheduleTaskManager(context);
        List<ScheduleTask> scheduleTasks = scheduleTaskManager.getTasks();
        for (ScheduleTask scheduleTask : scheduleTasks) {
            scheduleTaskManager.startAlert(scheduleTask);
        }
    }

    //private class SetAlertsService


}
