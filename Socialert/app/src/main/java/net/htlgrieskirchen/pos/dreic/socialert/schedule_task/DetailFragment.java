package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.io.Serializable;

public class DetailFragment extends Fragment implements Serializable {
    private LinearLayout linearLayout;

    // private TextView tv_details_receivers;
    private ListView lv_receivers;

    // private TextView tv_message_content;
    private TextView tv_details_message;

    // private TextView tv_time;
    private TextView tv_details_time;

    // EMAIL
    private ConstraintLayout layout_subject;
    private TextView tv_details_subject;


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
        View view = inflater.inflate(R.layout.fragment_task_detail_schedule_task, container, false);
        linearLayout = view.findViewById(R.id.linearLayout);
        lv_receivers = view.findViewById(R.id.lv_receivers);
        tv_details_message = view.findViewById(R.id.tv_details_message);
        tv_details_time = view.findViewById(R.id.tv_details_time);

        // Email
        layout_subject = view.findViewById(R.id.layout_subject);
        tv_details_subject = view.findViewById(R.id.tv_details_subject);

        return view;
    }

    public void show(ScheduleTask task) {
        linearLayout.setVisibility(View.VISIBLE);

        ReceiversAdapter receiversAdapter = new ReceiversAdapter(getContext(), R.layout.list_receivers, task.getReceivers());
        lv_receivers.setAdapter(receiversAdapter);

        tv_details_message.setText(task.getMessage());

        tv_details_time.setText(task.getTime());

        if (task.getTask_type() == ScheduleTask.Task_Type.EMAIL) {
            layout_subject.setVisibility(View.VISIBLE);
            tv_details_subject.setText(((EmailTask) task).getSubject());
        } else {
            layout_subject.setVisibility(View.GONE);
        }

    }
}