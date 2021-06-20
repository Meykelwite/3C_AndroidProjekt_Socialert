package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task.sms.SmsDialogFragment;

import java.util.List;
import java.util.stream.Collectors;

public class TaskMasterFragment extends Fragment {

    private ListView lv_tasks;
    private AutoReplyTaskListAdapter adapter_ongoingTasks;
    private AutoReplyTaskListAdapter adapter_completedTasks;
    private ViewPager viewPager;
    private int type;
    private AutoReplyTaskManager taskManager;
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
        if (context instanceof TaskMasterFragment.OnSelectionChangedListener) {
            listener = (OnSelectionChangedListener) context;

            type = ((TabTaskFragment) getParentFragment()).getType();
            switch (type) {
                case 0: // ongoing tasks
                    ((AutoReplyTaskActivity) getActivity()).setFragmentRefreshListenerOngoingTasks(new AutoReplyTaskActivity.FragmentRefreshListener() {
                        @Override
                        public void onRefresh() {
                            initOngoingTasksFragment();
                        }
                    });
                    break;
                case 1: // completed tasks
                    ((AutoReplyTaskActivity) getActivity()).setFragmentRefreshListenerCompletedTasks(new AutoReplyTaskActivity.FragmentRefreshListener() {
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
        List<AutoReplyTask> ongoingTasks = getOngoingTasks();
        adapter_ongoingTasks = new AutoReplyTaskListAdapter(getActivity(), R.layout.list_auto_reply_task, ongoingTasks);
        lv_tasks.setAdapter(adapter_ongoingTasks);
        //set the badge
        setUpBadges();
    }

    private void initCompletedTasksFragment() {
        List<AutoReplyTask> completedTasks = getCompletedTasks();
        adapter_completedTasks = new AutoReplyTaskListAdapter(getActivity(), R.layout.list_auto_reply_task, completedTasks);
        lv_tasks.setAdapter(adapter_completedTasks);
        //set the badge
        setUpBadges();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_master_auto_reply_task, container, false);
        lv_tasks = view.findViewById(R.id.lv_tasks);
        lv_tasks.setOnItemClickListener((parent, view1, position, id) -> itemSelected(position));
        registerForContextMenu(lv_tasks);

        viewPager = ((AutoReplyTaskActivity) getActivity()).getViewPager();
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
        taskManager = ((AutoReplyTaskActivity) getActivity()).getTaskManager();
        tabLayout = ((AutoReplyTaskActivity) getActivity()).getTabLayout();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.lv_tasks:
                getActivity().getMenuInflater().inflate(R.menu.context_menu_auto_reply_task, menu);
                if (type == 0) { // ongoing Task
                    MenuItem context_edit = menu.getItem(0);
                    context_edit.setVisible(true);
                    MenuItem context_markAsCompleted = menu.getItem(2);
                    context_markAsCompleted.setVisible(true);
                }
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        AutoReplyTask task = null;
        int currItem = getCurrentViewPagerItem();
        int pos = -1;
        if (info != null) {
            pos = info.position;
            if (currItem == 0 && type == 0) { // ongoing Task
                task = (AutoReplyTask) adapter_ongoingTasks.getItem(pos);
            } else if (currItem == 1 && type == 1) { // completed Task
                task = (AutoReplyTask) adapter_completedTasks.getItem(pos);
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
                    }
                    refresh();
                    break;
                case R.id.context_delete:
                    taskManager.removeTask(task);
                    refresh();
                    break;
                case R.id.context_markAsCompleted:
                    taskManager.markAsCompleted(task);
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
        ((AutoReplyTaskActivity) getActivity()).refresh();
    }


    private List<AutoReplyTask> getOngoingTasks() {
        taskManager = ((AutoReplyTaskActivity) getActivity()).getTaskManager();
        return taskManager.getTasks().stream().filter(task -> !task.isCompleted()).collect(Collectors.toList());
    }

    private List<AutoReplyTask> getCompletedTasks() {
        taskManager = ((AutoReplyTaskActivity) getActivity()).getTaskManager();
        return taskManager.getTasks().stream().filter(task -> task.isCompleted()).collect(Collectors.toList());
    }

    private void itemSelected(int position) {
        AutoReplyTask task = null;
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
        void onSelectionChanged(AutoReplyTask task);
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