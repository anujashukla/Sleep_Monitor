package com.example.anuja.sleep_monitor;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import java.util.LinkedList;
import java.util.Queue;

public class SleepLogAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private ArrayList<StillTimePeriod> mLogsArray;

    public SleepLogAdapter(Context context, ArrayList<StillTimePeriod> logsArray) {
        mContext = context;
        mLogsArray = logsArray;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mLogsArray.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        ViewHolder holder;

        if (convertView == null) {
            row = mInflater.inflate(R.layout.list_item, parent , false);
            holder = new ViewHolder();
            holder.duration = (TextView) row.findViewById(R.id.duration);
            holder.startTime = (TextView) row.findViewById(R.id.start_time);
            holder.endTime = (TextView) row.findViewById(R.id.end_time);
            row.setTag(holder);
        } else {
            row = convertView;
            holder = (ViewHolder) row.getTag();
        }

        holder.duration.setText(mLogsArray.get(position).getStillPeriodStr());
        holder.startTime.setText(mLogsArray.get(position).getStartTimeStr());
        holder.endTime.setText(mLogsArray.get(position).getEndTimeStr());
        return row;
    }

    private class ViewHolder {
        TextView duration;
        TextView startTime;
        TextView endTime;
    }

}
