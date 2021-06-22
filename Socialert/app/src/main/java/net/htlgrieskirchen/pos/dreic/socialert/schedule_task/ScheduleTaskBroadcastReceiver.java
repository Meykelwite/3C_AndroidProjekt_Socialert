package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import net.htlgrieskirchen.pos.dreic.socialert.BaseActivity;
import net.htlgrieskirchen.pos.dreic.socialert.GetAddressTask;
import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.Variables;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.SendEmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SendSMS;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import static android.provider.Settings.System.getString;

public class ScheduleTaskBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean showNotifications = BaseActivity.showNotifications;

        int pos = intent.getIntExtra("pos", -1);
        ScheduleTaskManager taskManager = new ScheduleTaskManager(context);
        ScheduleTask task = taskManager.getTaskAt(pos);
        ScheduleTask.Task_Type task_type = task.getTask_type();

        if (task.getMessage().contains(Variables.ADDRESS) || task.getMessage().contains(Variables.LOCATION)) {
            // Variablen durch aktuelle Werte ersetzen.
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setCostAllowed(true);
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = null;
            location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                }
            });
            double lat = location == null ? -1 : location.getLatitude();
            double lon = location == null ? -1 : location.getLongitude();
            String newMessage = task.getMessage();
            if (taskManager.getTaskAt(pos).getMessage().contains(Variables.ADDRESS)) {
                GetAddressTask getAddressTask = new GetAddressTask();
                getAddressTask.execute(new String[]{String.valueOf(lat), String.valueOf(lon)});
                try {
                    String apiResult = getAddressTask.get();
                    JSONObject jsonObject = new JSONObject(apiResult);
                    JSONObject address = jsonObject.getJSONObject("address");
                    String road = address.optString("road");
                    String house_number = address.optString("house_number");
                    String postcode = address.optString("postcode");
                    String city = address.optString("city");
                    String village = address.optString("village");
                    String town = address.optString("town");
                    String addressString = road + " " + house_number + ", " + postcode + " " + city + village + town;

                    newMessage = newMessage.replaceAll(Variables.ADDRESS, addressString);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (taskManager.getTaskAt(pos).getMessage().contains(Variables.LOCATION)) {
                newMessage = newMessage.replaceAll(Variables.LOCATION, "Latitude: " + lat + ", Longitude: " + lon);
                taskManager.updateMessage(task, newMessage);
            }
            taskManager.updateMessage(task, newMessage);
            task = taskManager.getTaskAt(pos);

        }


        switch (task_type) {
            case SMS:
                SmsTask smsTask = (SmsTask) task;
                //Toast.makeText(context, "GUTEN SMS" + task.getMessage(), Toast.LENGTH_SHORT).show();
                SendSMS sendSMS = new SendSMS();
                for (String receiver : smsTask.getReceivers().keySet()) {
                    sendSMS.sendSMSMessage(receiver, smsTask.getMessage());
                }
                taskManager.markAsCompleted(smsTask);
                ScheduleTaskActivity scheduleTaskActivity = ScheduleTaskActivity.getInstance();
                if (scheduleTaskActivity != null) {
                    scheduleTaskActivity.refresh();
                }
                if (showNotifications) {
                    showNotification(context, smsTask);
                }
                break;
            case EMAIL:
                EmailTask emailTask = (EmailTask) task;
                //Toast.makeText(context, "GUTEN EMAIL" + message, Toast.LENGTH_SHORT).show();
                SendEmailTask asyncTask = new SendEmailTask(context, emailTask, taskManager);
                asyncTask.execute();
                if (showNotifications) {
                    showNotification(context, emailTask);
                }
                break;
        }
    }

    private void showNotification(Context context, ScheduleTask task) {
        String text = getNotificationText(task);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, BaseActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_schedule_24)
                .setColor(Color.MAGENTA)
                .setContentTitle(context.getString(R.string.navigation_drawer_title_1))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(createDetailActivityIntent(context, task))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        int notificationId = task.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }

    private String getNotificationText(ScheduleTask task) {
        String type = Character.toUpperCase(task.getTask_type().toString().charAt(0)) + (task.getTask_type().toString().substring(1)).toLowerCase();
        return type + " Task an " + task.getReceiversFormatted() + " wurde abgeschlossen!";
    }

    // Erzeugen der Tap-Action für die Notifications
    private PendingIntent createDetailActivityIntent(Context context, ScheduleTask task) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("task", task);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // https://developer.android.com/reference/android/content/Intent#FLAG_ACTIVITY_NEW_TASK
        // If set, this activity will become the start of a new task on this history stack
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return pendingIntent;
    }


}
