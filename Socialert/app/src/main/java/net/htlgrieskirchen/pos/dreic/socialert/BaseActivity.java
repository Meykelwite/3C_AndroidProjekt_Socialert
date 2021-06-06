package net.htlgrieskirchen.pos.dreic.socialert;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.AutoReplyTaskActivity;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.ScheduleTaskActivity;

public class BaseActivity extends AppCompatActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

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
