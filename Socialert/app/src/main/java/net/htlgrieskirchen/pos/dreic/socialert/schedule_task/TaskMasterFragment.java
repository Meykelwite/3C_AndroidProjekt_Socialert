package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.ViewPagerAdapter;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.util.ArrayList;
import java.util.List;

public class TaskMasterFragment extends Fragment {
    private ListView lv_tasks;
    private ScheduleTaskListAdapter adapter;
    private List<ScheduleTask> tasks = new ArrayList<>();
    private ViewPagerAdapter viewPagerAdapter;
    private int type;

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
                            tasks = getTasks();
                            adapter = new ScheduleTaskListAdapter(getActivity(), R.layout.list_schedule_task, tasks);
                            lv_tasks.setAdapter(adapter);
                            //adapter.notifyDataSetChanged();
                        }
                    });
                    break;
                case 1: // completed tasks
                    ((ScheduleTaskActivity) getActivity()).setFragmentRefreshListenerCompletedTasks(new ScheduleTaskActivity.FragmentRefreshListener() {
                        @Override
                        public void onRefresh() {
                            tasks = getTasks();
                            List<ScheduleTask> sms = new ArrayList<>();
                            for (int i = 0; i < tasks.size(); i++) {
                                if (tasks.get(i) instanceof SmsTask) {
                                    sms.add(tasks.get(i));
                                }
                            }

                            adapter = new ScheduleTaskListAdapter(getActivity(), R.layout.list_schedule_task, sms);
                            lv_tasks.setAdapter(adapter);
                            //adapter.notifyDataSetChanged();
                        }
                    });
                    break;


            }
        } else {
            Toast.makeText(getContext(), "onAttach: Activity does not implement OnSelectionChangedListener", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_master_schedule_task, container, false);
        lv_tasks = view.findViewById(R.id.lv_tasks);

        // viewPagerAdapter = (ViewPagerAdapter) ((ScheduleTaskActivity) getActivity()).getViewPager().getAdapter();
        // boolean a = this.getParentFragment.equals(viewPagerAdapter.getItem(0)));

        lv_tasks.setOnItemClickListener((parent, view1, position, id) -> itemSelected(position));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    private List<ScheduleTask> getTasks() {
        return ((ScheduleTaskActivity) getActivity()).getTasks();
    }


    private void itemSelected(int position) {
        ScheduleTask task = tasks.get(position);
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
}