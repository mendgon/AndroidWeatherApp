package ru.omgtu.weather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView ChangedCityName_View;
    String ChangedCityName;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button change_city_button = (Button)findViewById(R.id.ChangeCityButton);
         ChangedCityName_View = (TextView) findViewById(R.id.editCityView);

        View.OnClickListener OnBtn = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangedCityName = ChangedCityName_View.getText().toString();
                Log.d("MyLOG", "Cnme " + ChangedCityName);
                Intent intent = new Intent (MainActivity.this, change_sity.class);
                intent.putExtra("CName", ChangedCityName);
                startActivity(intent);
            }
        };
        change_city_button.setOnClickListener(OnBtn);
    }


}
