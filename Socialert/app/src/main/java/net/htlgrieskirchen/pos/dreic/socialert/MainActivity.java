package net.htlgrieskirchen.pos.dreic.socialert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskMasterFragment.OnSelectionChangedListener {

    private AppBarConfiguration mAppBarConfiguration;

    // Observer Pattern
    private boolean showRight = false;

    //Save instance state
    private String selectedTask;
    public static final String STATE_TASK = "task_state";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar = "new version" of ActionBar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with new Task Action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_schedule_task, R.id.nav_auto_reply, R.id.nav_preferences)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        int orientation = getResources().getConfiguration().orientation;
        showRight = orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onSelectionChanged(String task) {
        this.selectedTask = task;
        if (showRight) {
                NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                if (isScheduleTask(navHostFragment)) {
                    ScheduleTaskFragment scheduleTaskFragment = (ScheduleTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    TaskDetailFragment taskDetailFragment = (TaskDetailFragment) scheduleTaskFragment.getChildFragmentManager().getFragments().get(1); // only way to get the TaskDetailFragment??
                    taskDetailFragment.show(task);
                } else {
                    AutoReplyTaskFragment autoReplyTaskFragment = (AutoReplyTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                    TaskDetailFragment taskDetailFragment = (TaskDetailFragment) autoReplyTaskFragment.getChildFragmentManager().getFragments().get(1);
                    taskDetailFragment.show(task);
                }
        } else {
            startRightActivity(task);
        }
    }

    private boolean isScheduleTask(NavHostFragment navHostFragment) {
        try {
            ScheduleTaskFragment scheduleTaskFragment = (ScheduleTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }

    private void startRightActivity(String task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (selectedTask != null) {
            savedInstanceState.putSerializable(STATE_TASK, selectedTask);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTask = (String) savedInstanceState.getSerializable(STATE_TASK);
            if (selectedTask != null) {
                if (showRight) {
                    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
                    if (isScheduleTask(navHostFragment)) {
                        ScheduleTaskFragment scheduleTaskFragment = (ScheduleTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                        TaskDetailFragment taskDetailFragment = (TaskDetailFragment) scheduleTaskFragment.getChildFragmentManager().getFragments().get(1); // only way to get the TaskDetailFragment??
                        taskDetailFragment.show(selectedTask);
                    } else {
                        AutoReplyTaskFragment autoReplyTaskFragment = (AutoReplyTaskFragment) navHostFragment.getChildFragmentManager().getFragments().get(0);
                        TaskDetailFragment taskDetailFragment = (TaskDetailFragment) autoReplyTaskFragment.getChildFragmentManager().getFragments().get(1);
                        taskDetailFragment.show(selectedTask);
                    }
                } else {
                    startRightActivity(selectedTask);
                }
            }
        }
    }
}