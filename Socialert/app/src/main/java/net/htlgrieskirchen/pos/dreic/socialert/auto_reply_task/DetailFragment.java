package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import net.htlgrieskirchen.pos.dreic.socialert.R;

import java.io.Serializable;

public class DetailFragment extends Fragment implements Serializable {
    private TextView fragment_task_detail_textview;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_detail_auto_reply_task, container, false);
        fragment_task_detail_textview = view.findViewById(R.id.fragment_task_detail_textview);


        return view;
    }

    public void show(String task) {
        fragment_task_detail_textview.setText("Details: "+task);
    }
}