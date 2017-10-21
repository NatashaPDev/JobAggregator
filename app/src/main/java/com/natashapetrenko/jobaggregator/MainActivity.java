package com.natashapetrenko.jobaggregator;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView tvCount;
    private ListView listJobs;
    private String feedURLHeadHunter = "https://spb.hh.ru/search/vacancy/rss?no_magic=true&items_on_page=100&order_by=publication_time&area=2&enable_snippets=true&text=%s&clusters=true&search_field=name";
    private String feedURLCareer = "https://career.ru/search/vacancy/rss?no_magic=true&order_by=publication_time&specialization=15&area=2&enable_snippets=true&text=%s&clusters=true&employment=probation";
    private String cachedFeedURLHeadHunter = "INVALIDATE";
    private String jobTitle = "android+developer";
    private final String STATE_URL_HEAD_HUNTER = "FeedURL";
    private final String STATE_JOB_TITLE = "JobTitle";
    private final String STATE_CACHED_URL_HEAD_HUNTER = "CachedFeedURLHeadHunter";
    private final String STATE_COUNT = "Count";
    private ArrayList<FeedEntry> jobs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listJobs = (ListView) findViewById(R.id.xmlListView);
        tvCount = (TextView) findViewById(R.id.tvCount);

        if (savedInstanceState != null) {
            feedURLHeadHunter = savedInstanceState.getString(STATE_URL_HEAD_HUNTER);
            cachedFeedURLHeadHunter = savedInstanceState.getString(STATE_CACHED_URL_HEAD_HUNTER);
            jobTitle = savedInstanceState.getString(STATE_JOB_TITLE);
            tvCount.setText(savedInstanceState.getString(STATE_COUNT));
        }

        downloadUrl(String.format(feedURLHeadHunter, jobTitle));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (jobTitle.equals("android+developer"))
            menu.findItem(R.id.mnuAndroid).setChecked(true);
        else
            menu.findItem(R.id.mnuJavaJunior).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mnuAndroid:
                item.setChecked(true);
                jobTitle = "android+developer";
                break;
            case R.id.mnuJavaJunior:
                item.setChecked(true);
                jobTitle = "java+junior";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadUrl(String.format(feedURLHeadHunter, jobTitle));
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL_HEAD_HUNTER, feedURLHeadHunter);
        outState.putString(STATE_CACHED_URL_HEAD_HUNTER, cachedFeedURLHeadHunter);
        outState.putString(STATE_JOB_TITLE, jobTitle);
        outState.putString(STATE_COUNT, tvCount.getText().toString());
        super.onSaveInstanceState(outState);
    }

    private void downloadUrl(String feedURL) {
        if (!cachedFeedURLHeadHunter.equals(feedURL)) {
            jobs.clear();
            Log.d(TAG, "downloadUrl: starting Asynctask");
            DownloadData downloadData = new DownloadData();
            downloadData.execute(feedURL);
            cachedFeedURLHeadHunter = feedURL;
            Log.d(TAG, "downloadUrl: done");
        }
    }

    private class DownloadData extends AsyncTask<String, Void, String> {
        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            jobs.addAll(parseApplications.getApplications());

            FeedAdapter<FeedEntry> feedAdapter = new FeedAdapter<>(MainActivity.this, R.layout.list_record, jobs);
            listJobs.setAdapter(feedAdapter);
            tvCount.setText("Vacancies: " + jobs.size());

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


















