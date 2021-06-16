package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsDialogFragment;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TaskMasterFragment extends Fragment {
    private ListView lv_tasks;
    private ScheduleTaskListAdapter adapter_ongoingTasks;
    private ScheduleTaskListAdapter adapter_completedTasks;
    private ViewPager viewPager;
    private int type;
    private ScheduleTaskManager taskManager;
    private TabLayout tabLayout;

    // Referenz auf die Activity mithilfe eines Objekts vom Typ OnSelectionChangedListener
    private OnSelectionChangedListener listener;

    public TaskMasterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        // Fragment wird an die Activity gebunden
        super.onAttach(context);


        // Activity als Listener für die "Klick-Events" registieren. Allerdings sollten wir prüfen, ob die Activity auch tatsächlich das Interface implementiert.
        if (context instanceof OnSelectionChangedListener) {
            listener = (OnSelectionChangedListener) context;

            type = ((TabTaskFragment) getParentFragment()).getType();
            switch (type) {
                case 0: // ongoing tasks
                    ((ScheduleTaskActivity) getActivity()).setFragmentRefreshListenerOngoingTasks(new ScheduleTaskActivity.FragmentRefreshListener() {
                        @Override
                        public void onRefresh() {
                            initOngoingTasksFragment();
                        }
                    });
                    break;
                case 1: // completed tasks
                    ((ScheduleTaskActivity) getActivity()).setFragmentRefreshListenerCompletedTasks(new ScheduleTaskActivity.FragmentRefreshListener() {
                        @Override
                        public void onRefresh() {
                            initCompletedTasksFragment();
                        }
                    });
                    break;
            }
        } else {
            Toast.makeText(getContext(), "onAttach: Activity does not implement OnSelectionChangedListener", Toast.LENGTH_SHORT).show();
        }
    }

    private void initOngoingTasksFragment() {
        List<ScheduleTask> ongoingTasks = getOngoingTasks();
        adapter_ongoingTasks = new ScheduleTaskListAdapter(getActivity(), R.layout.list_schedule_task, ongoingTasks);
        lv_tasks.setAdapter(adapter_ongoingTasks);
        //set the badge
        setUpBadges();
    }

    private void initCompletedTasksFragment() {
        List<ScheduleTask> completedTasks = getCompletedTasks();
        adapter_completedTasks = new ScheduleTaskListAdapter(getActivity(), R.layout.list_schedule_task, completedTasks);
        lv_tasks.setAdapter(adapter_completedTasks);
        //set the badge
        setUpBadges();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_master_schedule_task, container, false);
        lv_tasks = view.findViewById(R.id.lv_tasks);
        lv_tasks.setOnItemClickListener((parent, view1, position, id) -> itemSelected(position));
        registerForContextMenu(lv_tasks);

        viewPager = (ViewPager) ((ScheduleTaskActivity) getActivity()).getViewPager();
        // boolean a = this.getParentFragment.equals(viewPagerAdapter.getItem(0)));

        switch (type) {
            case 0: // ongoing tasks
                initOngoingTasksFragment();
                break;
            case 1: // completed tasks
                initCompletedTasksFragment();
                break;
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskManager = ((ScheduleTaskActivity) getActivity()).getTaskManager();
        tabLayout = ((ScheduleTaskActivity) getActivity()).getTabLayout();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.lv_tasks:
                getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
                if (type == 0) { // ongoing Task
                    MenuItem context_markAsCompleted = menu.getItem(3);
                    context_markAsCompleted.setVisible(true);
                }
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ScheduleTask task = null;
        int currItem = getCurrentViewPagerItem();
        int pos = -1;
        if (info != null) {
            pos = info.position;
            if (currItem == 0 && type == 0) { // ongoing Task
                task = (ScheduleTask) adapter_ongoingTasks.getItem(pos);
            } else if (currItem == 1 && type == 1) { // completed Task
                task = (ScheduleTask) adapter_completedTasks.getItem(pos);
            }
        }

        if (task != null) {
            switch (item.getItemId()) {
                case R.id.context_edit:
                    switch (task.getTask_type()) {
                        case SMS:
                            SmsDialogFragment editSMSDialog = new SmsDialogFragment();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("task", task);
                            bundle.putInt("position", pos);
                            editSMSDialog.setArguments(bundle);
                            editSMSDialog.show(getActivity().getSupportFragmentManager(), SmsDialogFragment.TAG);
                            break;
                        case EMAIL:
                            EmailDialogFragment editEmailDialog = new EmailDialogFragment();
                            Bundle bundle2 = new Bundle();
                            bundle2.putSerializable("task", task);
                            bundle2.putInt("position", pos);
                            editEmailDialog.setArguments(bundle2);
                            editEmailDialog.show(getActivity().getSupportFragmentManager(), EmailDialogFragment.TAG);
                            break;

                    }
                    refresh();
                    break;
                case R.id.context_copy:
                    ScheduleTask newTask = null;
                    switch (task.getTask_type()) {
                        case SMS:
                            newTask = new SmsTask(task.getMessage(), task.getTime(), task.getReceivers());
                            break;
                        case EMAIL:
                            newTask = new EmailTask(task.getMessage(), task.getTime(), task.getReceivers());
                            break;
                    }
                    taskManager.addTask(newTask);
                    refresh();
                    break;
                case R.id.context_delete:
                    taskManager.removeTask(task);
                    refresh();
                    break;
                case R.id.context_markAsCompleted:
                    taskManager.setCompleted(task, true);
                    refresh();
                    break;
            }
        }
        return super.onContextItemSelected(item);
    }

    private int getCurrentViewPagerItem() {
        return viewPager.getCurrentItem();
    }

    private void refresh() {
        ((ScheduleTaskActivity) getActivity()).refresh();
    }

    private List<ScheduleTask> getTasks() {
        return taskManager.getTasks();
    }

    private List<ScheduleTask> getOngoingTasks() {
        return getTasks().stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    private List<ScheduleTask> getCompletedTasks() {
        return getTasks().stream().filter(task -> task.isCompleted()).collect(Collectors.toList());
    }

    private void itemSelected(int position) {
        ScheduleTask task = null;
        //int currentItem = getCurrentViewPagerItem();
        //refresh();
        boolean a = getParentFragment().equals(((ViewPagerAdapter) viewPager.getAdapter()).getItem(0));
        //Toast.makeText(getContext(), "" + a, Toast.LENGTH_SHORT).show();
        if (type == 0) {
            task = getOngoingTasks().get(position);
        } else if (type == 1) {
            task = getCompletedTasks().get(position);
        }
        listener.onSelectionChanged(task);
    }

    @Override
    public void onStart() {
        // Das Fragment ist in der gebundenen Activity bereits vollständig sichtbar
        super.onStart();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(ScheduleTask task);
    }

    private void setUpBadges() {
        BadgeDrawable badgeDrawableOngoingTasks = tabLayout.getTabAt(0).getOrCreateBadge();
        badgeDrawableOngoingTasks.setVisible(true);
        badgeDrawableOngoingTasks.setNumber(getOngoingTasks().size());

        BadgeDrawable badgeDrawableCompletedTasks = tabLayout.getTabAt(1).getOrCreateBadge();
        badgeDrawableCompletedTasks.setVisible(true);
        badgeDrawableCompletedTasks.setNumber(getCompletedTasks().size());
    }
}