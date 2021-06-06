package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import net.htlgrieskirchen.pos.dreic.socialert.R;

import java.util.ArrayList;
import java.util.List;

public class TaskMasterFragment extends Fragment {

    private ListView lv_tasks;
    private ArrayAdapter<String> adapter;
    private List<String> tasks = new ArrayList<>();


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
        } else {
            Toast.makeText(getContext(), "onAttach: Activity does not implement OnSelectionChangedListener", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_master_auto_reply_task, container, false);
        lv_tasks = view.findViewById(R.id.lv_tasks);

        tasks.add("Element 1");
        tasks.add("Element 2");
        tasks.add("Element 3");

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, tasks);
        lv_tasks.setAdapter(adapter);

        lv_tasks.setOnItemClickListener((parent, view1, position, id) -> itemSelected(position));

        return view;
    }


    private void itemSelected(int position) {
        String task = tasks.get(position);
        listener.onSelectionChanged(task);
    }

    @Override
    public void onStart() {
        // Das Fragment ist in der gebundenen Activity bereits vollständig sichtbar
        super.onStart();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(String task);
    }
}