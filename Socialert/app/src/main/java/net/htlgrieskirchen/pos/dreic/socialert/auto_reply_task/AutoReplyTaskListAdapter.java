package net.htlgrieskirchen.pos.dreic.socialert.auto_reply_task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.htlgrieskirchen.pos.dreic.socialert.R;

import java.util.List;

public class AutoReplyTaskListAdapter extends BaseAdapter {
    private List<AutoReplyTask> tasks;
    private int layoutId;
    private LayoutInflater inflater;

    public AutoReplyTaskListAdapter(Context context, int layoutId, List<AutoReplyTask> tasks) {
        this.tasks = tasks;
        this.layoutId = layoutId;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        AutoReplyTask task = tasks.get(position);
        View listItem = (view == null) ? inflater.inflate(this.layoutId, null) : view;
        TextView tv_receivers = listItem.findViewById(R.id.tv_receivers);
        TextView tv_message = listItem.findViewById(R.id.tv_message);
        ImageView imageView_task_icon = listItem.findViewById(R.id.imageView_task_icon);

        tv_message.setText(task.getMessage());
        tv_receivers.setText(task.getReceiverFormatted());
        if (task.getTask_type() == AutoReplyTask.Task_Type.SMS) { // if (task instanceof SmsTask) {
            imageView_task_icon.setImageResource(R.drawable.ic_baseline_sms_24);
        }


        //tv_time.setText(task.getMessage());

        return listItem;

    }
}
