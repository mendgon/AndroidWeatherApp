package ru.omgtu.weather;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataBase extends AppCompatActivity {
private ListView BDinfoView;
    public static final String TAG = "MyLOG";
    ArrayList<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_base);

        BDinfoView = (ListView) findViewById(R.id.ListV);
        List<String> myList = (List<String>) getIntent().getSerializableExtra("mylist");
        Log.d(TAG,"принятие в базе активити ") ;
         //создаем адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
               android.R.layout.simple_list_item_1, (myList));
        Log.d(TAG,"Прошло создание адаптера ") ;
        // присваиваем адаптер списку
         BDinfoView.setAdapter(adapter);
        Log.d(TAG,"Прошло создание адаптефыфыффра ") ;

    }
}
