package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.htlgrieskirchen.pos.dreic.socialert.R;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.email.EmailTask;
import net.htlgrieskirchen.pos.dreic.socialert.schedule_task.sms.SmsTask;

import java.io.Serializable;

public class DetailFragment extends Fragment implements Serializable {
    private TextView tv_message;
    private TextView tv_time;
    private TextView tv_phone_number;
    private TextView tv_email;
    private LinearLayout linearLayout_phone_number;
    private LinearLayout linearLayout_email;

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
        tv_message = view.findViewById(R.id.tv_message);
        tv_time = view.findViewById(R.id.tv_time);
        tv_phone_number = view.findViewById(R.id.tv_phone_number);
        tv_email = view.findViewById(R.id.tv_email);
        linearLayout_phone_number = view.findViewById(R.id.linearLayout_phone_number);
        linearLayout_email = view.findViewById(R.id.linearLayout_email);


        return view;
    }

    public void show(ScheduleTask task) {
        tv_message.setText(task.getMessage());
        tv_time.setText(task.getTime());

        if (task instanceof SmsTask) {
            linearLayout_email.setVisibility(View.GONE);
            linearLayout_phone_number.setVisibility(View.VISIBLE);
            tv_phone_number.setText(((SmsTask) task).getPhoneNumber());
        } else if (task instanceof EmailTask) {
            linearLayout_phone_number.setVisibility(View.GONE);
            linearLayout_email.setVisibility(View.VISIBLE);
            tv_email.setText(((EmailTask) task).getEmail());

        }

    }
}