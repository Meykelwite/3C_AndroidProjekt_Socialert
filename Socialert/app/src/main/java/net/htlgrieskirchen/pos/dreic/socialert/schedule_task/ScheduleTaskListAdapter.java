package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.htlgrieskirchen.pos.dreic.socialert.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTaskListAdapter extends BaseAdapter {
    private List<ScheduleTask> tasks;
    private int layoutId;
    private LayoutInflater inflater;

    public ScheduleTaskListAdapter(Context context, int layoutId, List<ScheduleTask> tasks) {
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
        ScheduleTask task = tasks.get(position);
        View listItem = (view == null) ? inflater.inflate(this.layoutId, null) : view;
        TextView tv_message = listItem.findViewById(R.id.tv_message);
        //TextView tv_time = listItem.findViewById(R.id.tv_time);
        tv_message.setText(task.getMessage());
        //tv_time.setText(task.getMessage());

        return listItem;

    }
}
