package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import net.htlgrieskirchen.pos.dreic.socialert.GetAddressTask;
import net.htlgrieskirchen.pos.dreic.socialert.Variables;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.SendEmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SendSMS;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ScheduleTaskBroadcastReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
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
                break;
            case EMAIL:
                EmailTask emailTask = (EmailTask) task;
                //Toast.makeText(context, "GUTEN EMAIL" + message, Toast.LENGTH_SHORT).show();
                SendEmailTask asyncTask = new SendEmailTask(context, emailTask, taskManager);
                asyncTask.execute();
                break;
        }


    }


}
