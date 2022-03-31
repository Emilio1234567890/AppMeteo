package com.example.appmeteo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRl;
    private ProgressBar loadingPB;
    private TextView CityNameTV, TemperatureTV, ConditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdit;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherModal> weatherModalArrayList;
    private WeatherAdaptater weatherAdaptater;
    private LocationManager locationManager;
    private int PERMISSION_CODE=1;
    private String cityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRl = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        CityNameTV = findViewById(R.id.idTVCityName);
        TemperatureTV = findViewById(R.id.idTemperature);
        ConditionTV = findViewById(R.id.idCondition);
        weatherRV = findViewById(R.id.idWeather);
        cityEdit = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idBack);
        iconIV = findViewById(R.id.idIcon);
        searchIV = findViewById(R.id.idSearch);
        weatherModalArrayList = new ArrayList<>();
        weatherAdaptater = new WeatherAdaptater(this,weatherModalArrayList);
        weatherRV.setAdapter(weatherAdaptater);

        locationManager= (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION )!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getcityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = cityEdit.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    CityNameTV.setText(cityName);
                    getWeatherInfo(city);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Please provide the permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getcityName(double longitude, double latitude){
         String cityName = "Not Found";
        Geocoder gdc = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gdc.getFromLocation(latitude,longitude,10);

            for (Address adr: addresses){
                if(adr!=null){
                    String  city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName = city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND ");
                        Toast.makeText(this, "User CIty Not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=25569d1e01b04ca6a01105534221403&q="+cityName+"&days=1&aqi=yes&alerts=yes";
        CityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRl.setVisibility(View.VISIBLE);
                weatherModalArrayList.clear();
                try {
                    String temperature = response.getJSONObject("current").getString("temp_c");
                    TemperatureTV.setText(temperature+"°C");
                    int isDay = response.getJSONObject("current").getInt("is_day");
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                    ConditionTV.setText(condition);

                    if (isDay==1){
                        //morning
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.dreamstime.com%2Fdramatic-morning-sky-sunrise-panorama-over-mountains-clouds-burning-sun-sunset-fire-image109064989&psig=AOvVaw1g0sMVJ-XQPQ6FH0P7a2Ir&ust=1647613624179000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCMjJyZmtzfYCFQAAAAAdAAAAABAD").into(backIV);

                    }else {
                        //night
                        Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Funsplash.com%2Fs%2Fphotos%2Fnight-sky&psig=AOvVaw0Qq8yOuFZPtEptm_NYQHtX&ust=1647613583300000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCJD7xoStzfYCFQAAAAAdAAAAABAI").into(backIV);
                    }

                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecasrO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = forecasrO.getJSONArray("hour");

                    for(int i=0; i<hourArray.length();i++){
                        JSONObject hourOj = hourArray.getJSONObject(i);
                        String time = hourOj.getString("time");
                        String temper = hourOj.getString("temp_c");
                        String img = hourOj.getJSONObject("condition").getString("icon");
                        String wind = hourOj.getString("wind_kph");
                        weatherModalArrayList.add(new WeatherModal(time,temper,img,wind));
                    }
                    weatherAdaptater.notifyDataSetChanged();

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter a valid city Name", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }


}