package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsMessage;

import androidx.annotation.NonNull;

import net.htlgrieskirchen.pos.dreic.socialert.GetAddressTask;
import net.htlgrieskirchen.pos.dreic.socialert.Variables;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.AutoReplyTaskActivity;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.AutoReplyTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class StaticSMSReceiver extends BroadcastReceiver {

    private double lat = -1;
    private double lon = -1;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean initializedGPSData = false;

        AutoReplyTaskManager taskManager = new AutoReplyTaskManager(context);
        SendSMS sendSMS = new SendSMS();

        // https://www.youtube.com/watch?v=y-UXGMZk92E
        String action = intent.getAction();
        if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            AutoReplyTaskActivity autoReplyTaskActivity = AutoReplyTaskActivity.getInstance();
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages;
            String msg_from;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = messages[i].getOriginatingAddress();
                        List<SmsTask> ongoingTasks = taskManager.smsTasks().stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
                        for (SmsTask task : ongoingTasks) {
                            String taskNumber = "";
                            for (String receiver : task.getReceiver().keySet()) {
                                taskNumber = receiver.trim().replaceAll(" ", "");
                            }
                            if (taskNumber.equals(msg_from) || taskNumber.isEmpty()) {
                                if (task.getMessage().contains(Variables.ADDRESS) || task.getMessage().contains(Variables.LOCATION)) {
                                    // Standortfeatures
                                    if (!initializedGPSData) {
                                        initLocationAndAddress(context);
                                    }
                                    String newMessage = task.getMessage();
                                    if (newMessage.contains(Variables.ADDRESS)) {
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
                                    if (newMessage.contains(Variables.LOCATION)) {
                                        newMessage = newMessage.replaceAll(Variables.LOCATION, "Latitude: " + lat + ", Longitude: " + lon);
                                    }
                                    taskManager.updateMessage(task, newMessage);
                                }
                                sendSMS.sendSMSMessage(msg_from, task.getMessage());
                                taskManager.setTimeWhenSent(task, LocalDateTime.now());
                                taskManager.markAsCompleted(task);
                                if (autoReplyTaskActivity != null) {
                                    autoReplyTaskActivity.refresh();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


    }

    @SuppressLint("MissingPermission")
    private void initLocationAndAddress(Context context) {
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
        lat = location == null ? -1 : location.getLatitude();
        lon = location == null ? -1 : location.getLongitude();
    }
}
