package ru.omgtu.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import static ru.omgtu.weather.change_sity.TAG;


public class change_sity extends Activity implements View.OnClickListener {
    public static final String TAG = "MyLOG";
    String CityName;
    TextView CityView;
    TextView TempView;
    DBHelper dbHelper;
    Button BD_save;
    Button btnClear;
    Button readBD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_sity);

        TempView = (TextView) findViewById(R.id.TempView);
        CityView = (TextView) findViewById(R.id.CityView);

        readBD = (Button) findViewById(R.id.read_BD) ;
        readBD.setOnClickListener(this);
        BD_save = (Button) findViewById(R.id.saveBD);
        BD_save.setOnClickListener(this);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);



        CityName = getIntent().getStringExtra("CName");
        loadWeather(CityName);
        Log.d(TAG, "City name " + CityName);
        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);

    }
    //Работа с сервисом
    public void onClickStart(View v) {
        startService(new Intent(this, MyService.class).putExtra("ForServisName",CityName));
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, MyService.class));
    }


    public void loadWeather(final String CityName) {
        Log.d(TAG, "City name from loadWeather " + CityName);
        String urlOpenWeatherMap = "http://api.openweathermap.org/data/2.5/weather?q=" + CityName + "&APPID=a03dcc3a8c6f789e63442b9b9fdf6847";
        RequestQueue mRequestQueue;

        final int[] temp = new int[1];

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
                            Log.d(TAG, "отправляем запрос");
                            JSONArray weather_array = response.getJSONArray("weather");
                            JSONObject weather = weather_array.getJSONObject(0);
                            String description = weather.getString("description");

                            JSONObject main = response.getJSONObject("main");

                            temp[0] = (int) Math.round(main.getDouble("temp")); //Это в кельвинах

                            int temperature;
                            temperature = temp[0] - 273;

                            Log.d(TAG, "temp " + temp[0]);

                            CityView.setText(String.format("Город %s", CityName));
                            TempView.setText(String.format("Температура %d градусов", temperature));

                        } catch (JSONException je) {
                            Log.d(TAG, "Error while parsing JSON: " + je.getMessage());
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

        Log.d(TAG, "Конец");
        mRequestQueue.add(jsonObjectRequest);

    }

    @Override
    public void onClick(View view) {
        // создаем объект для данных
        Log.d(TAG, "--- Вошел в метод: ---");
        ContentValues cv = new ContentValues();

      String BD_City=  CityView.getText().toString();
      String BD_Temp =  TempView.getText().toString();

        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (view.getId()) {
            case R.id.saveBD:
                Log.d(TAG, "--- Insert in mytable: ---");
                // подготовим данные для вставки в виде пар: наименование столбца - значение

                cv.put("DBCityName", BD_City);
                cv.put("DBTemp", BD_Temp);
                // вставляем запись и получаем ее ID
                long rowID = db.insert("mytable", null, cv);
                Log.d(TAG, "row inserted, ID = " + rowID);
                Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show();
                break;
            case R.id.read_BD:
                Log.d(TAG, "--- Rows in mytable: ---");
                // делаем запрос всех данных из таблицы mytable, получаем Cursor
                Cursor c = db.query("mytable", null, null, null, null, null, null);

                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (c.moveToFirst()) {

                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("id");
                    int CnameColIndex = c.getColumnIndex("DBCityName");
                    int tempColIndex = c.getColumnIndex("DBTemp");
                    ArrayList<String> DB_info = new ArrayList<String>();
                    Intent intent = new Intent (change_sity.this, DataBase.class);

                    do {
                        // получаем значения по номерам столбцов и пишем все в лог

                       DB_info.add( c.getString(CnameColIndex) + " " +
                                  c.getString(tempColIndex));
                        Log.d(TAG,"Добавление в лист") ;
                      /*  Log.d(TAG,
                                "ID = " + c.getInt(idColIndex) +
                                        ", DBCityName = " + c.getString(CnameColIndex) +
                                        ", DBTemp = " + c.getString(tempColIndex));
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла*/
                    } while (c.moveToNext());

                     intent.putExtra("mylist", DB_info);
                    Log.d(TAG,"передача на другую активити ") ;
                    startActivity(intent);
                } else
                    Log.d(TAG, "0 rows");

                c.close();
                break;
            case R.id.btnClear:
                Log.d(TAG, "--- Clear mytable: ---");
                // удаляем все записи
                int clearCount = db.delete("mytable", null, null);
                Log.d(TAG, "deleted rows count = " + clearCount);
                break;
        }
        // закрываем подключение к БД
        dbHelper.close();
    }
    }

class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table mytable ("
                + "id integer primary key autoincrement,"
                + "DBCityName text,"
                + "DBTemp text" + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}




