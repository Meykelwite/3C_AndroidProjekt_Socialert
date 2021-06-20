package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
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


import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;


import net.htlgrieskirchen.pos.dreic.socialert.BaseActivity;
import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SendSMS;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class ScheduleTaskActivity extends BaseActivity implements TaskMasterFragment.OnSelectionChangedListener, TaskListener {
    private static final int PICK_CONTACT = 123456;
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_GMAIL = 78;

    private static final int REQUEST_ACCOUNT_PICKER = 4;
    private static ScheduleTaskActivity instance;
    private static final int REQUEST_AUTHORIZATION = 32794;

    private GoogleAccountCredential mCredential;

    private ScheduleTaskManager taskManager;

    //send automatic SMS
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

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
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = findViewById(R.id.content_frame);
        View view = inflater.inflate(R.layout.activity_schedule_task, container);
        instance = this;

        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(new String[]{GmailScopes.GMAIL_SEND}))
                .setBackOff(new ExponentialBackOff());


        getSupportActionBar().setTitle(R.string.navigation_drawer_title_1);

        // init TaskManager
        initTaskManager();

        fab_parent = view.findViewById(R.id.add_fab);
        fab_addSMSTask = view.findViewById(R.id.add_sms_task);
        fab_addEmailTask = view.findViewById(R.id.add_email_task);

        tv_addSMSTask = view.findViewById(R.id.add_sms_task_text);
        tv_addEmailTask = view.findViewById(R.id.add_email_task_text);

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

        navigationView.setCheckedItem(R.id.nav_schedule_task);

        int orientation = getResources().getConfiguration().orientation;
        showRight = orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void initTaskManager() {
        taskManager = new ScheduleTaskManager(this);
    }

    public static ScheduleTaskActivity getInstance() {
        return instance;
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
                checkPermission();
                if (checkSelfPermission(Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    SmsDialogFragment.display(getSupportFragmentManager());
                    refresh();
                }
            }
        });

        fab_addEmailTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAccount(mCredential);
                // EmailDialogFragment is then shown in onActivityResult
            }
        });
    }

    // Storing Mail ID using Shared Preferences
    public void chooseAccount(GoogleAccountCredential credential) {
        if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Start a dialog from which the user can choose an account
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
            //}
        } else {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, 6567641);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ACCOUNT_PICKER:
                    if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                        String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            mCredential.setSelectedAccountName(accountName);

                            HttpTransport transport = AndroidHttp.newCompatibleTransport();
                            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                            Gmail service = new Gmail.Builder(
                                    transport, jsonFactory, mCredential)
                                    .setApplicationName(getString(R.string.app_name))
                                    .build();
                            // Dieser AsyncTask dient nur dazu, dass die User-Authorisierung vor dem Senden einer Email stattfindet
                            // keine bessere Lösung gefunden
                            // https://stackoverflow.com/questions/41387992/gmail-api-grant-permissions-before-userrecoverableauthuiexception
                            boolean showDialog = false;
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected String doInBackground(Void... voids) {
                                    try {
                                        service.users().messages().get("me", "id").execute();
                                    } catch (Exception e) {
                                        if (e instanceof UserRecoverableAuthIOException) {
                                            startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), REQUEST_AUTHORIZATION);
                                        } else {
                                            // Hier tritt immer eine Exception auf, da wir die authentication scopes für den Befehl im try-Block gar nicht besitzen
                                            // wir fangen sie ab
                                            if (e instanceof GoogleJsonResponseException) {
                                                return "showDialog";
                                            } else {
                                                return e.getMessage();
                                            }
                                            //e.printStackTrace();
                                        }
                                    }
                                    return "";
                                }

                                @Override
                                protected void onPostExecute(String s) {
                                    super.onPostExecute(s);
                                    if (s.equals("showDialog")) {
                                        EmailDialogFragment.display(getSupportFragmentManager());
                                        refresh();
                                    } else if (!s.isEmpty()) {
                                        Toast.makeText(getApplicationContext(), "Fehler: " + s, Toast.LENGTH_LONG).show();
                                    }

                                }
                            }.execute();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    Toast.makeText(this, "User " + mCredential.getSelectedAccountName() + " wurde erfolgreich authorisiert!", Toast.LENGTH_SHORT).show();
                    EmailDialogFragment.display(getSupportFragmentManager());
                    refresh();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission required!",
                        Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_GMAIL) {
            if (grantResults.length > 0 &&
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Permission required!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public void checkPermissionGmail() {
        if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_SEND_GMAIL);
        } else {

        }
    }

    public void checkPermission() {
        if (checkSelfPermission(Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {

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
        initTaskManager();
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

    public String getSelectedAccountName() {
        return mCredential.getSelectedAccountName();
    }


}