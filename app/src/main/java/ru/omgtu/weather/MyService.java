package ru.omgtu.weather;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class MyService extends Service {
    String cityName;
    Thread tr;
    SQLiteDatabase sq;
    ContentValues cv;
    final String LOG_TAG = "MyLOG";
    private boolean isAlive = true;
    DBHelper dbHelper;
    int[] temp = new int[1];
    int temperatureDB;
    String cityDB;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        cv = new ContentValues();
        dbHelper = new DBHelper(getApplicationContext());
        cityName = intent.getStringExtra("ForServisName");
        sq = dbHelper.getWritableDatabase();
        tr = new Thread() {
            @Override
            public void run() {
                while(true){
                    if(alive()) {
                        loadWeather(cityName);
                        try {
                            TimeUnit.SECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        cv.put("DBCityName", cityName);
                        cv.put("DBTemp", temp[0]);
                        long rowID = sq.insert("mytable", null, cv);
                        Log.d(LOG_TAG, "row inserted, ID = " + rowID);

                        Log.d(LOG_TAG, "Worked");
                        Log.d(LOG_TAG, " temp is - " + temp[0] + " City - " + cityName);
                        cityDB = (String) cv.get("DBCityName");
                        temperatureDB = (int) cv.get("DBTemp");
                        Log.d(LOG_TAG, " temp from DB is - " + temperatureDB + " CityDB - " + cityDB);

                        NotificationCompat.Builder builder =
                                new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle("Weather is updated!")
                                        .setContentText("It`s " + temp[0] + " in " + cityName);

                        Notification notification = builder.build();

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        notificationManager.notify(1, notification);
                    }
                    else{
                        return;
                    }

                }
            }
        };
        tr.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private boolean alive() {
        return isAlive;
    }

    public void loadWeather(final String CityName) {
        Log.d(LOG_TAG, "City name from loadWeather " + CityName);
        String urlOpenWeatherMap = "http://api.openweathermap.org/data/2.5/weather?q=" + CityName + "&APPID=a03dcc3a8c6f789e63442b9b9fdf6847";
        RequestQueue mRequestQueue;

        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        // Setup the network to use the HTTPURLConnection client
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the request queue
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlOpenWeatherMap, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(LOG_TAG, "отправляем запрос");
                    JSONArray weather_array = response.getJSONArray("weather");
                    JSONObject weather = weather_array.getJSONObject(0);
                    String description = weather.getString("description");

                    JSONObject main = response.getJSONObject("main");

                    temp[0] = (int) Math.round(main.getDouble("temp")); //Это в кельвинах
                    temp[0] = temp[0] - 273;

                    Log.d(LOG_TAG, "temp ");

                } catch (JSONException je) {
                    Log.d(LOG_TAG, "Error while parsing JSON: " + je.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
                error.printStackTrace();

                VolleyLog.e("Error: ", error.toString());
                VolleyLog.e("Error: ", error.getLocalizedMessage());
            }

        });

        Log.d(LOG_TAG, "Конец");
        mRequestQueue.add(jsonObjectRequest);

    }

    public void onDestroy() {
        super.onDestroy();
        isAlive = false;
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
