package com.nplusnapps.hotels;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_DOWNLOAD_DATA = "download_data";
    public static final String EXTRA_DOWNLOAD_URL = "download_url";
    public static final String EXTRA_SELECTED_HOTEL = "selected_hotel";
    public static final String EXTRA_HOTELS_LIST = "hotels_list";

    public static final int ORDER_DEFAULT = -1;
    public static final int ORDER_STARS = 0;
    public static final int ORDER_SUITES = 1;
    public static final int ORDER_DISTANCE = 2;

    private static final String BUNDLE_ACTIVITY_TITLE = "activity_title";
    private static final String PREFERENCES_SORT_ORDER = "sort_order";

    private SharedPreferences mPreferences;
    private ConnectivityManager mConnectManager;
    private LocalBroadcastManager mBroadcastManager;
    private BroadcastReceiver mDownloadReceiver;
    private SwipeRefreshLayout mSwipeLayout;
    private MenuItem mItemStars, mItemSuites, mItemDistance;
    private HotelAdapter mHotelAdapter;
    private ArrayList<Hotel> mHotelsList = new ArrayList<>();
    private String mActivityTitle = null;
    private int mSortOrder = ORDER_DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mSortOrder = getOrderPref();

        mHotelAdapter = new HotelAdapter(this, R.layout.list_item, R.id.name_label, new ArrayList<Hotel>());
        mHotelAdapter.setOrder(mSortOrder);

        ListView hotelsList = (ListView) findViewById(android.R.id.list);
        hotelsList.setEmptyView(findViewById(android.R.id.empty));
        hotelsList.setAdapter(mHotelAdapter);
        hotelsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(MainActivity.this,
                        DetailsActivity.class).putExtra(EXTRA_SELECTED_HOTEL,
                        (Hotel) parent.getItemAtPosition(position)));
            }
        });

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeLayout.setColorSchemeResources(R.color.color_primary_dark);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadData();
            }
        });

        mConnectManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);

        mDownloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!MainActivity.this.isFinishing()) {
                    switch (intent.getAction()) {
                        case DownloadService.ACTION_DOWNLOAD_FINISHED:
                            ArrayList<Hotel> downloadedList =
                                    intent.getParcelableArrayListExtra(EXTRA_HOTELS_LIST);
                            if (downloadedList != null) {
                                mHotelsList = downloadedList;
                                updateAdapter();
                            } else {
                                showSnackbar(getString(R.string.snackbar_data));
                            }

                            mSwipeLayout.setRefreshing(false);

                            setActivityTitle(getString(R.string.app_name));

                            unregisterReceiver();

                            break;
                        case DownloadService.ACTION_DOWNLOAD_UPDATE:
                            setActivityTitle(getString(R.string.title_activity_download,
                                    intent.getIntExtra(DownloadService.EXTRA_DOWNLOAD_NUMBER, 0),
                                    intent.getIntExtra(DownloadService.EXTRA_DOWNLOADS_COUNT, 0)));
                            break;
                        default:
                            break;
                    }
                }
            }
        };

        if (savedInstanceState != null) {
            ArrayList<Hotel> savedList = savedInstanceState.
                    getParcelableArrayList(EXTRA_HOTELS_LIST);
            if (savedList != null) {
                mHotelsList = savedList;
            }
            updateAdapter();

            if (savedInstanceState.containsKey(BUNDLE_ACTIVITY_TITLE)) {
                mActivityTitle = savedInstanceState.getString(BUNDLE_ACTIVITY_TITLE);
                setActivityTitle(mActivityTitle);
            }

            if (DownloadService.isDownloading()) {
                registerReceiver();

                mSwipeLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeLayout.setRefreshing(true);
                    }
                });
            }
        } else {
            mSwipeLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeLayout.setRefreshing(true);
                    downloadData();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(EXTRA_HOTELS_LIST, mHotelsList);

        if (mActivityTitle != null) {
            outState.putString(BUNDLE_ACTIVITY_TITLE, mActivityTitle);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        mItemStars = menu.findItem(R.id.action_sort_stars);
        mItemSuites = menu.findItem(R.id.action_sort_suites);
        mItemDistance = menu.findItem(R.id.action_sort_distance);

        checkMenuItems();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_stars:
                mSortOrder = ORDER_STARS;
                checkMenuItems();
                setOrderPref();
                updateAdapter();
                return true;
            case R.id.action_sort_suites:
                mSortOrder = ORDER_SUITES;
                checkMenuItems();
                setOrderPref();
                updateAdapter();
                return true;
            case R.id.action_sort_distance:
                mSortOrder = ORDER_DISTANCE;
                checkMenuItems();
                setOrderPref();
                updateAdapter();
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadData() {
        if (!DownloadService.isDownloading()) {
            NetworkInfo networkInfo = mConnectManager.getActiveNetworkInfo();

            if (networkInfo != null && networkInfo.isConnected()) {
                setActivityTitle(getString(R.string.title_activity_update));

                registerReceiver();

                startService(new Intent(this, DownloadService.class).
                        setAction(ACTION_DOWNLOAD_DATA).
                        putExtra(EXTRA_DOWNLOAD_URL, getString(R.string.hotels_url)));
            } else {
                showSnackbar(getString(R.string.snackbar_connection));
                mSwipeLayout.setRefreshing(false);
            }
        }
    }

    private void updateAdapter() {
        if (!mHotelsList.isEmpty()) {
            switch (mSortOrder) {
                case ORDER_STARS:
                    Collections.sort(mHotelsList, new StarsComparator());
                    break;
                case ORDER_SUITES:
                    Collections.sort(mHotelsList, new SuitesComparator());
                    break;
                case ORDER_DISTANCE:
                    Collections.sort(mHotelsList, new DistanceComparator());
                    break;
                default:
                    break;
            }
        }

        if (mHotelAdapter != null) {
            mHotelAdapter.setOrder(mSortOrder);
            mHotelAdapter.addItems(mHotelsList, true);
        }
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadService.ACTION_DOWNLOAD_UPDATE);
        filter.addAction(DownloadService.ACTION_DOWNLOAD_FINISHED);

        mBroadcastManager.registerReceiver(mDownloadReceiver, filter);
    }

    private void unregisterReceiver() {
        mBroadcastManager.unregisterReceiver(mDownloadReceiver);
    }

    private void setOrderPref() {
        if (getOrderPref() != mSortOrder) {
            mPreferences.edit().putInt(PREFERENCES_SORT_ORDER, mSortOrder).apply();
        }
    }

    private int getOrderPref() {
        return mPreferences.getInt(PREFERENCES_SORT_ORDER, ORDER_DEFAULT);
    }

    private void checkMenuItems() {
        mItemStars.setChecked(mSortOrder == ORDER_STARS);
        mItemSuites.setChecked(mSortOrder == ORDER_SUITES);
        mItemDistance.setChecked(mSortOrder == ORDER_DISTANCE);
    }

    private void showSnackbar(String text) {
        Snackbar.make(mSwipeLayout, text, Snackbar.LENGTH_LONG).setAction(
                getString(R.string.snackbar_retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSwipeLayout.setRefreshing(true);
                        downloadData();
                    }
                }).show();
    }

    private void setActivityTitle(String title) {
        mActivityTitle = title;
        setTitle(title);
    }

    private static class StarsComparator implements Comparator<Hotel> {
        @Override
        public int compare(Hotel o1, Hotel o2) {
            return Double.compare(o2.getStars(), o1.getStars());
        }
    }

    private static class SuitesComparator implements Comparator<Hotel> {
        @Override
        public int compare(Hotel o1, Hotel o2) {
            return o2.getSuitesCount() - o1.getSuitesCount();
        }
    }

    private static class DistanceComparator implements Comparator<Hotel> {
        @Override
        public int compare(Hotel o1, Hotel o2) {
            return Double.compare(o1.getDistance(), o2.getDistance());
        }
    }
}
