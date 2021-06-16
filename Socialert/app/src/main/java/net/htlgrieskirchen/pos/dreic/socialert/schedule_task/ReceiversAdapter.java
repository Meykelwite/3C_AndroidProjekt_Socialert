package net.htlgrieskirchen.pos.dreic.socialert.schedule_task;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.htlgrieskirchen.pos.dreic.socialert.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceiversAdapter extends BaseAdapter {

    private Map<String, String> receivers;
    private int layoutId;
    private LayoutInflater inflater;

    public ReceiversAdapter(Context context, int layoutId, Map<String, String> receivers) {
        this.receivers = receivers;
        this.layoutId = layoutId;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return receivers.size();
    }

    @Override
    public Object getItem(int position) {
        int i = 0;
        for (Map.Entry<String, String> entry : receivers.entrySet()) {
            if (i == position) {
                return entry;
            }
            i++;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Map.Entry<String, String> entry = (Map.Entry<String, String>) getItem(position);
        View listItem = (view == null) ? inflater.inflate(this.layoutId, null) : view;

        TextView tv_contact_info_1 = listItem.findViewById(R.id.tv_contact_info_1);
        TextView tv_contact_info_2 = listItem.findViewById(R.id.tv_contact_info_2);

        if (entry.getValue().isEmpty()) {
            tv_contact_info_1.setText(entry.getKey());
            tv_contact_info_2.setVisibility(View.GONE);
        } else {
            tv_contact_info_1.setText(entry.getValue());
            tv_contact_info_2.setVisibility(View.VISIBLE);
            tv_contact_info_2.setText(entry.getKey());
        }

        return listItem;

    }


}
