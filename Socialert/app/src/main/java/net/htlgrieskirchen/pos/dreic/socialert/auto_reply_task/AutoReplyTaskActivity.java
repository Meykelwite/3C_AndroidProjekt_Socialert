package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import net.htlgrieskirchen.pos.dreic.socialert.BaseActivity;
import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms.SmsDialogFragment;

public class AutoReplyTaskActivity extends BaseActivity implements TaskMasterFragment.OnSelectionChangedListener, TaskListener {

    private static AutoReplyTaskActivity instance;

    private AutoReplyTaskManager taskManager;

    // SMS permissions
    private static final int RQ_RECEIVE_READ_SEND_SMS = 4712;
    private boolean isSMSAllowed = false;


    // to refresh the shown tasks in the MasterFragment
    private FragmentRefreshListener fragmentRefreshListenerOngoingTasks;
    private FragmentRefreshListener fragmentRefreshListenerCompletedTasks;

    private static final String STATE_TASK = "taskState";
    private static AutoReplyTask selectedTask;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter pagerAdapter;


    private FloatingActionButton fab_addSMSTask;
    private ExtendedFloatingActionButton fab_parent;
    private TextView tv_addSMSTask;

    // Observer Pattern
    private boolean showRight = false;

    // to check whether sub FABs are visible or not
    boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = findViewById(R.id.content_frame);
        View view = inflater.inflate(R.layout.activity_auto_reply_task, container);
        instance = this;


        getSupportActionBar().setTitle(R.string.navigation_drawer_title_2);

        // init TaskManager
        initTaskManager();

        fab_parent = view.findViewById(R.id.add_fab);
        fab_addSMSTask = view.findViewById(R.id.add_sms_task);

        tv_addSMSTask = view.findViewById(R.id.add_sms_task_text);

        setUpFabButtons();

        viewPager = view.findViewById(R.id.view_pager);

        tabLayout = view.findViewById(R.id.tab_layout);

        tabLayout.setupWithViewPager(viewPager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        TabTaskFragment ongoingTasksFragment = new TabTaskFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt("type", 0); // 0 for ongoing tasks
        ongoingTasksFragment.setArguments(bundle1); //
        pagerAdapter.addFragment(ongoingTasksFragment, getString(R.string.text_tab_ongoing_tasks));
        TabTaskFragment completedTasksFragment = new TabTaskFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putInt("type", 1); // 1 for completed tasks
        completedTasksFragment.setArguments(bundle2);
        pagerAdapter.addFragment(completedTasksFragment, getString(R.string.text_tab_completed_tasks));
        viewPager.setAdapter(pagerAdapter);

        navigationView.setCheckedItem(R.id.nav_auto_reply);

        int orientation = getResources().getConfiguration().orientation;
        showRight = orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void initTaskManager() {
        taskManager = new AutoReplyTaskManager(this);
    }

    public static AutoReplyTaskActivity getInstance() {
        return instance;
    }

    private void setUpFabButtons() {
        fab_addSMSTask.setVisibility(View.GONE);
        tv_addSMSTask.setVisibility(View.GONE);
        isAllFabsVisible = false;
        fab_parent.shrink();

        fab_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animateFab();
                if (!isAllFabsVisible) {
                    fab_addSMSTask.show();
                    tv_addSMSTask.setVisibility(View.VISIBLE);
                    fab_parent.startAnimation(rotateForward);
                    // fab_addSMSTask.startAnimation(fabOpen);
                    fab_parent.extend();
                    //animateFab();
                    isAllFabsVisible = true;
                } else { // isOpen
                    fab_addSMSTask.hide();
                    tv_addSMSTask.setVisibility(View.GONE);
                    fab_parent.startAnimation(rotateBackward);
                    // fab_addSMSTask.startAnimation(fabClose);
                    fab_parent.shrink();
                    isAllFabsVisible = false;
                }
            }
        });

        fab_addSMSTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animateFab();
                checkPermission();
                if (isSMSAllowed) {
                    SmsDialogFragment.display(getSupportFragmentManager());
                    refresh();
                }
            }
        });

    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            // RQ_RECEIVE_SEND_SMS ist just any constant value to identify the request
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS},
                    RQ_RECEIVE_READ_SEND_SMS);
        } else {
            isSMSAllowed = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == RQ_RECEIVE_READ_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS-Berechtigungen wurden verweigert!", Toast.LENGTH_LONG).show();
            } else {
                isSMSAllowed = true;
            }
        }
    }

    private void addTask(AutoReplyTask task) {
        taskManager.addTask(task);
        refresh();
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public void refresh() {
        initTaskManager();
        if (getFragmentRefreshListenerCompletedTasks() != null) {
            getFragmentRefreshListenerCompletedTasks().onRefresh();
        }
        if (getFragmentRefreshListenerOngoingTasks() != null) {
            getFragmentRefreshListenerOngoingTasks().onRefresh();
        }
    }

    private void startRightActivity(AutoReplyTask task) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onSelectionChanged(AutoReplyTask task) {
        this.selectedTask = task;
        if (showRight) {
            ViewPager viewPager = findViewById(R.id.view_pager);
            int currentItem = viewPager.getCurrentItem();
            DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().getFragments().get(currentItem).getChildFragmentManager().getFragments().get(1);
            detailFragment.show(task);
        } else {
            startRightActivity(task);
        }
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
            selectedTask = (AutoReplyTask) savedInstanceState.getSerializable(STATE_TASK);
            if (selectedTask != null) {
                if (showRight) {
                    ViewPager viewPager = findViewById(R.id.view_pager);
                    int currentItem = viewPager.getCurrentItem();
                    DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager().getFragments().get(currentItem).getChildFragmentManager().getFragments().get(1);
                    detailFragment.show(selectedTask);
                } else {
                    startRightActivity(selectedTask);
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity();
            } else {
                super.onBackPressed();
            }
        }
    }

    // for Sms Dialogfragment
    @Override
    public void onAddTask(AutoReplyTask task) {
        addTask(task);
        refresh();
    }

    @Override
    public void onEditTask(int position, AutoReplyTask newTask) {
        taskManager.setTask(position, newTask);
        refresh();
    }


    // for activity fragment commuinication
    // https://www.legendblogs.com/refresh-a-fragment-list-from-activity
    public interface FragmentRefreshListener {
        void onRefresh();
    }

    public FragmentRefreshListener getFragmentRefreshListenerOngoingTasks() {
        return fragmentRefreshListenerOngoingTasks;
    }

    public void setFragmentRefreshListenerOngoingTasks(FragmentRefreshListener fragmentRefreshListenerOngoingTasks) {
        this.fragmentRefreshListenerOngoingTasks = fragmentRefreshListenerOngoingTasks;
    }

    public FragmentRefreshListener getFragmentRefreshListenerCompletedTasks() {
        return fragmentRefreshListenerCompletedTasks;
    }

    public void setFragmentRefreshListenerCompletedTasks(FragmentRefreshListener fragmentRefreshListenerCompletedTasks) {
        this.fragmentRefreshListenerCompletedTasks = fragmentRefreshListenerCompletedTasks;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public AutoReplyTaskManager getTaskManager() {
        return taskManager;
    }

}