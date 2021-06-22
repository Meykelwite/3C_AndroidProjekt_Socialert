package net.htlgrieskirchen.pos.dreic.socialert;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.AutoReplyTaskActivity;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskActivity;

public class BaseActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    public static final String CHANNEL_ID = "12345678";
    public static boolean showNotifications;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private static final int RQ_PREFERENCES = 12345;

    protected DrawerLayout drawer;
    protected NavigationView navigationView;

    protected Animation fabOpen, fabClose, rotateForward, rotateBackward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fabOpen = AnimationUtils.loadAnimation
                (this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation
                (this, R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation
                (this, R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation
                (this, R.anim.rotate_backward);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        Button img_btn_preferences = findViewById(R.id.nav_preferences);
        img_btn_preferences.setOnClickListener(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        initShowNotifications();
        preferenceChangeListener = (sharedPrefs, key) -> preferenceChanged(sharedPrefs, key);
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

    }

    private void preferenceChanged(SharedPreferences sharedPrefs, String key) {
        if ("notification_preference".equals(key)) {
            showNotifications = prefs.getBoolean("notification_preference", true);
            if (showNotifications) {
                createNotificationChannel();
            }
            Toast.makeText(this, "Notification Einstellung wurde geändert!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initShowNotifications() {
        showNotifications = prefs.getBoolean("notification_preference", true);
        if (showNotifications) {
            createNotificationChannel();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_schedule_task:
                finish();
                startActivity(new Intent(getApplicationContext(), ScheduleTaskActivity.class));
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                break;
            case R.id.nav_auto_reply:
                //startActivity(new Intent(getApplicationContext(), SecondActivity.class));
                finish();
                startActivity(new Intent(getApplicationContext(), AutoReplyTaskActivity.class));
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                break;

        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_preferences:
                Intent intent = new Intent(this, PreferenceActivity.class);
                startActivityForResult(intent, RQ_PREFERENCES);
                break;
        }
    }


}
