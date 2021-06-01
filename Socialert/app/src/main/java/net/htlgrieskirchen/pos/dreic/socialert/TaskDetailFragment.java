package net.htlgrieskirchen.pos.dreic.socialert;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

public class TaskDetailFragment extends Fragment {
    private TextView fragment_task_detail_textview;

    public TaskDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail, container, false);
        fragment_task_detail_textview = view.findViewById(R.id.fragment_task_detail_textview);


        return view;
    }

    public void show(String task) {
        fragment_task_detail_textview.setText("Details: "+task);
    }
}