package net.htlgrieskirchen.pos.dreic.socialert;

import android.os.AsyncTask;

import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetAddressTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... strings) {
        String sJson = "";
        try {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL("https://eu1.locationiq.com/v1/reverse.php?key=" + Variables.LOCATION_IQ_API_KEY + "&lat=" + strings[0] + "&lon=" + strings[1] + "&format=json").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                sJson = readResponseStream(reader);
            }
        } catch (IOException e) {

        }
        return sJson;
    }

    private String readResponseStream(BufferedReader reader) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
