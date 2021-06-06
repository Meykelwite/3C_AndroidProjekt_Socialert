package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Intent;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import net.htlgrieskirchen.pos.dreic.socialert.BaseActivity;
import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;

public class AutoReplyTaskActivity extends BaseActivity implements TaskMasterFragment.OnSelectionChangedListener {
    private static final String STATE_TASK = "taskState";
    private static String selectedTask;

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

        getSupportActionBar().setTitle(R.string.navigation_drawer_title_2);

        fab_parent = view.findViewById(R.id.add_fab);
        fab_addSMSTask = view.findViewById(R.id.add_sms_task);

        tv_addSMSTask = view.findViewById(R.id.add_sms_task_text);

        setUpFabButtons();

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        tabLayout.setupWithViewPager(viewPager);
        pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 0);
        pagerAdapter.addFragment(new TabTaskFragment(), getString(R.string.text_tab_ongoing_tasks));
        pagerAdapter.addFragment(new TabTaskFragment(), getString(R.string.text_tab_completed_tasks));
        viewPager.setAdapter(pagerAdapter);

        //drawer.addView(view.getRootView(), 0);
        navigationView.setCheckedItem(R.id.nav_auto_reply);

//        FloatingActionButton fab = view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Test", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        //Toast.makeText(this, ""+getSupportFragmentManager().getFragments().get(0).getChildFragmentManager().getFragments().get(0).getChildFragmentManager().getFragments().size(), Toast.LENGTH_SHORT).show();
        //detailFragment = (DetailFragment) get.findFragmentById(R.id.fragDetail);
        int orientation = getResources().getConfiguration().orientation;
        showRight = orientation == Configuration.ORIENTATION_LANDSCAPE;
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
                Toast.makeText(getApplicationContext(), "SMS Added", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void startRightActivity(String task) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onSelectionChanged(String task) {
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
            selectedTask = savedInstanceState.getString(STATE_TASK);
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


}