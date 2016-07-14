package com.nplusnapps.hotels;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

public class DownloadService extends IntentService {

    public static final String ACTION_DOWNLOAD_FINISHED = "download_finished";
    public static final String ACTION_DOWNLOAD_UPDATE = "download_update";
    public static final String EXTRA_DOWNLOAD_NUMBER = "download_number";
    public static final String EXTRA_DOWNLOADS_COUNT = "downloads_count";

    private static final String KEY_PREFERENCES_CACHED_IMAGES = "cached_images";

    private Handler mHandler;
    private SharedPreferences mPreferences;
    private LocalBroadcastManager mBroadcastManager;

    private static volatile boolean sDownloading = false;

    public DownloadService() {
        super(DownloadService.class.getName());

        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(getMainLooper());
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case MainActivity.ACTION_DOWNLOAD_DATA:
                    sDownloading = true;

                    ArrayList<Hotel> hotelsList = new ArrayList<>();
                    Set<String> updatedImages = new HashSet<>();

                    String hotelsUrl = intent.getStringExtra(MainActivity.EXTRA_DOWNLOAD_URL);
                    String hotelsStr = getJSONString(hotelsUrl);
                    if (hotelsStr != null) {
                        try {
                            JSONArray hotelsArray = new JSONArray(hotelsStr);

                            for (int i = 0, hotelsCount = hotelsArray.length(); i < hotelsCount; i++) {
                                final Intent updateIntent = new Intent(ACTION_DOWNLOAD_UPDATE).
                                        putExtra(EXTRA_DOWNLOAD_NUMBER, i + 1).putExtra(EXTRA_DOWNLOADS_COUNT, hotelsCount);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mBroadcastManager.sendBroadcast(updateIntent);
                                    }
                                });


                                JSONObject hotelObject = hotelsArray.getJSONObject(i);
                                String hotelUrl = hotelsUrl.substring(
                                        0, hotelsUrl.lastIndexOf('/') + 1) +
                                        hotelObject.getInt("id") + ".json";

                                String hotelStr = getJSONString(hotelUrl);
                                if (hotelStr != null) {
                                    hotelObject = new JSONObject(hotelStr);

                                    String hotelImage = hotelObject.getString("image");
                                    if (!TextUtils.isEmpty(hotelImage)) {
                                        String imageUrl = hotelsUrl.substring(
                                                0, hotelsUrl.lastIndexOf('/') + 1) + hotelImage;
                                        if (saveImageToDisk(imageUrl, hotelImage)) {
                                            updatedImages.add(hotelImage);
                                        } else {
                                            hotelImage = null;
                                        }
                                    } else {
                                        hotelImage = null;
                                    }

                                    Hotel hotel = new Hotel(
                                            hotelObject.getInt("id"),
                                            hotelObject.getString("name"),
                                            hotelObject.getString("address"),
                                            hotelObject.getDouble("stars"),
                                            hotelObject.getDouble("distance"),
                                            hotelImage,
                                            hotelObject.getString("suites_availability").split(":"),
                                            hotelObject.getDouble("lat"), hotelObject.getDouble("lon"));

                                    hotelsList.add(hotel);
                                }
                            }
                        } catch (JSONException e) {
                            logException("JSON exception", e);
                        }
                    }

                    final Intent finishedIntent = new Intent(ACTION_DOWNLOAD_FINISHED);

                    if (!hotelsList.isEmpty()) {
                        Set<String> currentImages = getCachedImages();

                        for (String currentImage : currentImages) {
                            if (!updatedImages.contains(currentImage)) {
                                deleteFile(currentImage);
                            }
                        }

                        setCachedImages(updatedImages);

                        finishedIntent.putParcelableArrayListExtra(MainActivity.EXTRA_HOTELS_LIST, hotelsList);
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mBroadcastManager.sendBroadcast(finishedIntent);
                        }
                    });

                    sDownloading = false;

                    break;
                default:
                    break;
            }
        }
    }

    static boolean isDownloading() {
        return sDownloading;
    }

    private String getJSONString(String url) {
        String jsonStr = null;

        try {
            URL jsonUrl = new URL(url);

            if (jsonUrl.getProtocol().equals("https")) {
                HttpsURLConnection urlConn = (HttpsURLConnection)
                        jsonUrl.openConnection();
                urlConn.setConnectTimeout(15000);
                urlConn.setReadTimeout(10000);
                urlConn.setRequestMethod("GET");
                urlConn.setDoInput(true);
                urlConn.connect();

                if (urlConn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream inStream = urlConn.getInputStream();

                    BufferedReader buffReader = new BufferedReader(
                            new InputStreamReader(inStream, "utf-8"), 8);

                    StringBuilder strBuilder = new StringBuilder();
                    String strLine;

                    while ((strLine = buffReader.readLine()) != null) {
                        strBuilder.append(strLine + "\n");
                    }

                    jsonStr = strBuilder.toString();

                    inStream.close();
                }

                urlConn.disconnect();
            }
        } catch (IOException e) {
            logException("I/O exception", e);
        }

        return jsonStr;
    }

    private boolean saveImageToDisk(String url, String name) {
        boolean imageSaved = false;

        try {
            URL drawableUrl = new URL(url);

            if (drawableUrl.getProtocol().equals("https")) {
                HttpsURLConnection urlConn = (HttpsURLConnection)
                        drawableUrl.openConnection();
                urlConn.setConnectTimeout(15000);
                urlConn.setReadTimeout(10000);
                urlConn.setRequestMethod("GET");
                urlConn.setDoInput(true);
                urlConn.connect();

                if (urlConn.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                    InputStream inStream = urlConn.getInputStream();

                    Bitmap imageBitmap = BitmapFactory.decodeStream(inStream);
                    if (imageBitmap != null) {
                        imageBitmap = Bitmap.createBitmap(imageBitmap, 1, 1,
                                imageBitmap.getWidth() - 2, imageBitmap.getHeight() - 2);

                        FileOutputStream outStream = openFileOutput(name, Context.MODE_PRIVATE);
                        imageSaved = imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outStream);

                        outStream.close();
                        imageBitmap.recycle();
                    }

                    inStream.close();
                }

                urlConn.disconnect();
            }
        } catch (IOException e) {
            logException("I/O exception", e);
        }

        return imageSaved;
    }

    private void setCachedImages(Set<String> set) {
        mPreferences.edit().putStringSet(KEY_PREFERENCES_CACHED_IMAGES, set).apply();
    }

    private Set<String> getCachedImages() {
        return mPreferences.getStringSet(KEY_PREFERENCES_CACHED_IMAGES, new HashSet<String>());
    }

    private void logException(String tag, Exception e) {
        Log.e(tag, e.getMessage(), e);
    }
}
