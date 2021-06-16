package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import net.htlgrieskirchen.pos.dreic.socialert.BaseActivity;
import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SendSMS;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ScheduleTaskActivity extends BaseActivity implements TaskMasterFragment.OnSelectionChangedListener, TaskListener {
    private static final int PICK_CONTACT = 123456;

    private ScheduleTaskManager taskManager;

    //send automatic SMS
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final SendSMS sendSMS = new SendSMS();
    String phoneNo;
    String message;

    // to refresh the shown tasks in the MasterFragment
    private FragmentRefreshListener fragmentRefreshListenerOngoingTasks;
    private FragmentRefreshListener fragmentRefreshListenerCompletedTasks;

    private static final String STATE_TASK = "taskState";
    private static ScheduleTask selectedTask;

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter pagerAdapter;


    private FloatingActionButton fab_addSMSTask, fab_addEmailTask;
    private ExtendedFloatingActionButton fab_parent;
    private TextView tv_addSMSTask, tv_addEmailTask;

    // Observer Pattern
    private boolean showRight = false;

    // to check whether sub FABs are visible or not
    boolean isAllFabsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //inflate your activity layout here!
        //View view = inflater.inflate(R.layout.activity_schedule_task, null, false);

        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = findViewById(R.id.content_frame);
        View view = inflater.inflate(R.layout.activity_schedule_task, container);

        getSupportActionBar().setTitle(R.string.navigation_drawer_title_1);

        // init TaskManager
        taskManager = new ScheduleTaskManager(this);

        fab_parent = view.findViewById(R.id.add_fab);
        fab_addSMSTask = view.findViewById(R.id.add_sms_task);
        fab_addEmailTask = view.findViewById(R.id.add_email_task);

        tv_addSMSTask = view.findViewById(R.id.add_sms_task_text);
        tv_addEmailTask = view.findViewById(R.id.add_email_task_text);

        setUpFabButtons();

        viewPager = view.findViewById(R.id.view_pager);
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                refresh();
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

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

        //set the icons
        //tabLayout.getTabAt(0).setIcon(R.drawable.android);
        //tabLayout.getTabAt(1).setIcon(R.drawable.google_play);


        navigationView.setCheckedItem(R.id.nav_schedule_task);

        int orientation = getResources().getConfiguration().orientation;
        showRight = orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void setUpFabButtons() {
        fab_addSMSTask.setVisibility(View.GONE);
        fab_addEmailTask.setVisibility(View.GONE);
        tv_addSMSTask.setVisibility(View.GONE);
        tv_addEmailTask.setVisibility(View.GONE);
        isAllFabsVisible = false;
        fab_parent.shrink();

        fab_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //animateFab();
                if (!isAllFabsVisible) {
                    fab_addSMSTask.show();
                    fab_addEmailTask.show();
                    tv_addSMSTask.setVisibility(View.VISIBLE);
                    tv_addEmailTask.setVisibility(View.VISIBLE);
                    fab_parent.startAnimation(rotateForward);
                    // fab_addSMSTask.startAnimation(fabOpen);
                    // fab_addEmailTask.startAnimation(fabOpen);
                    fab_parent.extend();
                    //animateFab();
                    isAllFabsVisible = true;
                } else { // isOpen
                    fab_addSMSTask.hide();
                    fab_addEmailTask.hide();
                    tv_addSMSTask.setVisibility(View.GONE);
                    tv_addEmailTask.setVisibility(View.GONE);
                    fab_parent.startAnimation(rotateBackward);
                    // fab_addSMSTask.startAnimation(fabClose);
                    // fab_addEmailTask.startAnimation(fabClose);
                    fab_parent.shrink();
                    isAllFabsVisible = false;
                }
            }
        });

        fab_addSMSTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tasks.add(new SmsTask("SMS", "Juni", "0664"));
                SmsDialogFragment.display(getSupportFragmentManager());
                //registerForActivityResult(ActivityResultContracts.PickContact);
                refresh();
            }
        });

        fab_addEmailTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tasks.add(new EmailTask("Email", "Juni", "johndoe@fortnite.com"));
                EmailDialogFragment.display(getSupportFragmentManager());
                refresh();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult( int requestCode,
                                            String[] permissions,
                                            int[] grantResults ) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode==MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length>0 &&
                    grantResults[0]!=PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission required!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void checkPermission()
    {
        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                !=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        }
        else
        {
            sendSMS.sendSMSMessage("069913106148", "Somebody toucha ma spaghett");
        }
    }

    private void addTask(ScheduleTask task) {
        taskManager.addTask(task);
        refresh();
    }

    public TabLayout getTabLayout() {
        return tabLayout;
    }

    public void refresh() {
        if (getFragmentRefreshListenerCompletedTasks() != null) {
            getFragmentRefreshListenerCompletedTasks().onRefresh();
        }
        if (getFragmentRefreshListenerOngoingTasks() != null) {
            getFragmentRefreshListenerOngoingTasks().onRefresh();
        }
    }

    private void startRightActivity(ScheduleTask task) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onSelectionChanged(ScheduleTask task) {
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
            selectedTask = (ScheduleTask) savedInstanceState.getSerializable(STATE_TASK);
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
    public void onAddTask(ScheduleTask task) {
        addTask(task);
        refresh();
    }

    @Override
    public void onEditTask(int position, ScheduleTask newTask) {
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

    public ScheduleTaskManager getTaskManager() {
        return taskManager;
    }


}