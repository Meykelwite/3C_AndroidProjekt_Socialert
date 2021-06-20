package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;

import java.io.Serializable;

public class DetailFragment extends Fragment implements Serializable {
    private LinearLayout linearLayout;

    // private TextView tv_details_receivers;
    private TextView tv_receiver;

    // private TextView tv_message_content;
    private TextView tv_details_message;


    private ConstraintLayout layout_time;
    private TextView tv_details_time;


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
        linearLayout = view.findViewById(R.id.linearLayout);
        tv_receiver = view.findViewById(R.id.tv_receiver);
        tv_details_message = view.findViewById(R.id.tv_details_message);

        tv_details_time = view.findViewById(R.id.tv_details_time);
        layout_time = view.findViewById(R.id.layout_time);

        return view;
    }

    public void show(AutoReplyTask task) {
        linearLayout.setVisibility(View.VISIBLE);

        tv_receiver.setText(task.getReceiverFormatted());
        tv_details_message.setText(task.getMessage());

        String getTimeWhenSent = task.getTimeWhenSent();
        if (getTimeWhenSent != null) {
            layout_time.setVisibility(View.VISIBLE);
            tv_details_time.setText(getTimeWhenSent);
        } else {
            layout_time.setVisibility(View.GONE);
        }

    }
}