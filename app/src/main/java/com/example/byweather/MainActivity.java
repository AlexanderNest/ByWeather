package com.example.byweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            this.update_weather(); // обновление погоды и доп информации на экране
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String weatherInJSON = null;  // тут лежит полученная погода в json

    public void update_weather_btn(View view){ // обработчик нажатия клавиши обновления погоды
        try {
            this.update_weather();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void update_weather() throws JSONException { // обновление погоды и доп информации на экране

        Weather w = new Weather(); // получаем погоду
        w.execute();

        while (weatherInJSON == null){  // ждем пока погода придет
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int fact_temp;  // фактическая температура

        JSONObject weatherObject = new JSONObject(weatherInJSON);  // погода в JSON
        JSONObject fact = weatherObject.getJSONObject("fact"); // JSON object fact

        fact_temp = fact.getInt("temp");

        TextView fact_temp_view = findViewById(R.id.fact_temp);
        fact_temp_view.setText(String.valueOf(fact_temp) + "°");  // вывод температуры на экран



        Toast toast = Toast.makeText(getApplicationContext(),
                "Информация о погоде обновлена", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    class Weather extends AsyncTask<Void, Void, Void> {  // получение погоды в фоне

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            String url = "https://api.weather.yandex.ru/v1/forecast?lat=55.75396&lon=37.620393";

            try{
                URL obj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Yandex-API-Key","25141b0c-7494-4083-a458-b5aa83515461");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                final StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                weatherInJSON = response.toString();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                response.toString(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                });
            }
            catch (Exception ex){


            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
