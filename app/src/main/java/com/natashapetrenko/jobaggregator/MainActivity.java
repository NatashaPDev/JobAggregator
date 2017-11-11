package com.natashapetrenko.jobaggregator;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.natashapetrenko.jobaggregator.data.JobsContracts;
import com.natashapetrenko.jobaggregator.data.JobsDbHelper;
import com.natashapetrenko.jobaggregator.data.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements FeedAdapter.OnItemClickListener {
    private static final String TAG = "MainActivity";
    private TextView tvCount;
    private String feedURLHeadHunter = "https://spb.hh.ru/search/vacancy/rss?no_magic=true&items_on_page=100&order_by=publication_time&area=2&enable_snippets=true&text=%s&clusters=true&search_field=name";
    private String feedURLCareer = "https://career.ru/search/vacancy/rss?no_magic=true&order_by=publication_time&specialization=15&area=2&enable_snippets=true&text=%s&clusters=true&employment=probation";
    private String cachedFeedURLHeadHunter = "INVALIDATE";
    private String jobTitle = "android+developer";
    private final String STATE_URL_HEAD_HUNTER = "FeedURL";
    private final String STATE_JOB_TITLE = "JobTitle";
    private final String STATE_CACHED_URL_HEAD_HUNTER = "CachedFeedURLHeadHunter";
    private final String STATE_COUNT = "Count";
    private final String STATE_SHOW_HIDDEN = "Show_hidden";
    private ArrayList<FeedEntry> jobs = new ArrayList<>();
    private SQLiteDatabase db;
    private FeedAdapter feedAdapter;
    private boolean showHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView listJobs = findViewById(R.id.xmlListView);
        tvCount = findViewById(R.id.tvCount);

        if (savedInstanceState != null) {
            feedURLHeadHunter = savedInstanceState.getString(STATE_URL_HEAD_HUNTER);
            cachedFeedURLHeadHunter = savedInstanceState.getString(STATE_CACHED_URL_HEAD_HUNTER);
            jobTitle = savedInstanceState.getString(STATE_JOB_TITLE);
            tvCount.setText(savedInstanceState.getString(STATE_COUNT));
            showHidden = savedInstanceState.getBoolean(STATE_SHOW_HIDDEN);
        }

        feedAdapter = new FeedAdapter(this);

        listJobs.setLayoutManager(new LinearLayoutManager(this));
        listJobs.setHasFixedSize(true);
        listJobs.setAdapter(feedAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                if (showHidden) {
                    return;
                }
                int id = (int) viewHolder.itemView.getTag();
                ContentValues contentValues = new ContentValues();
                contentValues.put(JobsContracts.JobsEntry.COLUMN_STATUS, Status.NOT_MATCH.toString());
                db.update(JobsContracts.JobsEntry.TABLE_JOBS, contentValues, "_id=?", new String[]{String.valueOf(id)});
                refresh();

            }
        }).attachToRecyclerView(listJobs);

        SQLiteOpenHelper dbHelper = new JobsDbHelper(getApplicationContext());
        db = dbHelper.getWritableDatabase();

        downloadUrl(String.format(feedURLHeadHunter, jobTitle));

    }

    private void refresh() {
        Cursor cursor = db.query(JobsContracts.JobsEntry.TABLE_JOBS,
                null,
                showHidden ? "status=?" : "status<>?",
                new String[]{Status.NOT_MATCH.toString()},
                null,
                null,
                null);
        // TODO cursor.close()
        feedAdapter.setCursor(cursor);
        tvCount.setText(String.format(getResources().getText(R.string.jobs_count).toString(), cursor.getCount()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_show_hidden:
                showHidden = !showHidden;
                refresh();
                setShowHiddenIcon(item);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setShowHiddenIcon(MenuItem item) {
        if (showHidden) {
            item.setIcon(R.mipmap.ic_star_border_white_24dp);
        } else {
            item.setIcon(R.mipmap.ic_star_white_24dp);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL_HEAD_HUNTER, feedURLHeadHunter);
        outState.putString(STATE_CACHED_URL_HEAD_HUNTER, cachedFeedURLHeadHunter);
        outState.putString(STATE_JOB_TITLE, jobTitle);
        outState.putString(STATE_COUNT, tvCount.getText().toString());
        outState.putBoolean(STATE_SHOW_HIDDEN, showHidden);
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String feedURL) {
        if (!cachedFeedURLHeadHunter.equals(feedURL)) {
            jobs.clear();
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedURL);
            cachedFeedURLHeadHunter = feedURL;
            Log.d(TAG, "downloadUrl: done");
        }
    }

    @Override
    public void OnItemClick(int id, View view) {
        if (view.getId() == R.id.imgFavorite) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(JobsContracts.JobsEntry.COLUMN_STATUS, Status.FAVORITE.toString());
            db.update(JobsContracts.JobsEntry.TABLE_JOBS, contentValues, "_id=?", new String[]{String.valueOf(id)});
            ((ImageView) view).setImageResource(android.R.drawable.star_on);
            refresh();
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            jobs = parseApplications.getJobs();

            for (FeedEntry job : jobs) {
                Cursor cursor = db.query(JobsContracts.JobsEntry.TABLE_JOBS, null, "_id=?", new String[]{String.valueOf(job.getId())}, null, null, null);
                if (cursor.getCount() == 0) {
                    job.setStatus(com.natashapetrenko.jobaggregator.data.Status.NEW);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(JobsContracts.JobsEntry._ID, job.getId());
                    contentValues.put(JobsContracts.JobsEntry.COLUMN_TITLE, job.getTitle());
                    contentValues.put(JobsContracts.JobsEntry.COLUMN_DESCRIPTION, job.getDescription());
                    contentValues.put(JobsContracts.JobsEntry.COLUMN_STATUS, job.getStatus().toString());
                    contentValues.put(JobsContracts.JobsEntry.COLUMN_LINK, job.getLink());
                    db.insert(JobsContracts.JobsEntry.TABLE_JOBS, null, contentValues);
                }
                cursor.close();
            }
            refresh();

        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: starts with " + strings[0]);
            String rssFeed = downloadXML(strings[0]);
            if (rssFeed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return rssFeed;
        }

        private String downloadXML(String urlPath) {
            StringBuilder xmlResult = new StringBuilder();

            try {
                URL url = new URL(urlPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                int response = connection.getResponseCode();
                Log.d(TAG, "downloadXML: The response code was " + response);
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        xmlResult.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }
                reader.close();

                return xmlResult.toString();
            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXML: Invalid URL " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXML: IO Exception reading data: " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXML: Security Exception.  Needs permisson? " + e.getMessage());
            }

            return null;
        }
    }
}


















