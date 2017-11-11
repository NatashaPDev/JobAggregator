package com.natashapetrenko.jobaggregator;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.natashapetrenko.jobaggregator.data.JobsContracts;
import com.natashapetrenko.jobaggregator.data.Status;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private Cursor cursor;
    private final OnItemClickListener clickListener;


    public FeedAdapter(OnItemClickListener clickListener) {
        super();
        this.clickListener = clickListener;
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_record, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FeedViewHolder holder, int position) {
        int id;
        if (cursor.moveToPosition(position)) {
            id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(JobsContracts.JobsEntry._ID)));
            holder.itemView.setTag(id);
            holder.tvTitle.setText(cursor.getString(cursor.getColumnIndex(JobsContracts.JobsEntry.COLUMN_TITLE)));
            holder.tvLink.setText(cursor.getString(cursor.getColumnIndex(JobsContracts.JobsEntry.COLUMN_LINK)));
            holder.tvDescription.setText(cursor.getString(cursor.getColumnIndex(JobsContracts.JobsEntry.COLUMN_DESCRIPTION)));

            if (cursor.getString(cursor.getColumnIndex(JobsContracts.JobsEntry.COLUMN_STATUS)).equals(Status.FAVORITE.toString())) {
                holder.imgFavorite.setImageResource(android.R.drawable.star_on);
            } else {
                holder.imgFavorite.setImageResource(android.R.drawable.star_off);
            }
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public interface OnItemClickListener {
        void OnItemClick(int id, View view);
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvTitle;
        final TextView tvLink;
        final TextView tvDescription;
        final ImageView imgFavorite;

        public FeedViewHolder(View h) {
            super(h);
            this.tvTitle = h.findViewById(R.id.tvTitle);
            this.tvLink = h.findViewById(R.id.tvLink);
            this.tvDescription = h.findViewById(R.id.tvDescription);
            this.imgFavorite = h.findViewById(R.id.imgFavorite);
            this.imgFavorite.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.OnItemClick((int) itemView.getTag(), view);
        }
    }
}





















