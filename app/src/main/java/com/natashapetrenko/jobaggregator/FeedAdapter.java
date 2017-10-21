package com.natashapetrenko.jobaggregator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by natashapetrenko on 14/07/2016.
 */

public class FeedAdapter<T extends FeedEntry> extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    private final int layoutResource;
    private final LayoutInflater layoutInflater;
    private List<T> applications;

    public FeedAdapter(Context context, int resource, List<T> applications) {
        super(context, resource);
        this.layoutResource = resource;
        this.layoutInflater = LayoutInflater.from(context);
        this.applications = applications;
    }

    @Override
    public int getCount() {
        return applications.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        FeedEntry currentApp = applications.get(position);

        viewHolder.tvTitle.setText(currentApp.getTitle());
        viewHolder.tvLink.setText(currentApp.getLink());
        viewHolder.tvDescription.setText(currentApp.getDescription());

        return convertView;
    }

    private class ViewHolder {
        final TextView tvTitle;
        final TextView tvLink;
        final TextView tvDescription;

        public ViewHolder(View h) {
            this.tvTitle = (TextView) h.findViewById(R.id.tvTitle);
            this.tvLink = (TextView) h.findViewById(R.id.tvLink);
            this.tvDescription = (TextView) h.findViewById(R.id.tvDescription);
        }
    }
}





















