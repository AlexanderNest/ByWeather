package com.example.byweather;

import android.app.AlertDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView v = findViewById(R.id.icon0);
        v.setImageResource(R.drawable.ic_location);

        try {
            this.update_weather(); // обновление погоды и доп информации на экране
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String weatherInJSON = null;  // тут лежит полученная погода в json

    AlertDialog alert; // окно с подробной информацией о том, что надо надеть

    public void close_dialog_info(View view){
        this.alert.dismiss();
    }

    public void open_dialog_info(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(R.layout.dialog_info);
        this.alert = builder.create();
        this.alert.show();
    }


    public void update_weather_btn(View view){ // обработчик нажатия клавиши обновления погоды
        try {
            this.update_weather();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   private int name_to_id(String name){
        if (name.equals("clearsunny")){
            return R.drawable.ic_clearsunny;
        }
        if (name.equals("overcast")){
            return R.drawable.ic_overcast;
        }
        if (name.equals("overcast-thunderstorms-with-rain")){
            return R.drawable.ic_overcast_thunderstorms_with_rain;
        }
        if (name.equals("cloudy-and-light-rain")){
            return R.drawable.ic_cloudy_and_light_rain;
        }
        if (name.equals("overcast-and-light-rain")){
            return R.drawable.ic_overcast_and_light_rain;
        }
        if (name.equals("partly-cloudy")){
            return R.drawable.ic_partly_cloudy;
        }
        if (name.equals("cloudy-and-light-snow")){
            return R.drawable.ic_cloudy_and_snow;
        }
        if (name.equals("overcast-and-light-snow")){
            return R.drawable.ic_overcast_and_snow;
        }
        if (name.equals("partly-cloudy-and-light-rain")){
            return R.drawable.ic_partly_cloudy_and_light_rain;
        }
        if (name.equals("cloudy-and-rain")){
            return R.drawable.ic_cloudy_and_rain;
        }
        if (name.equals("overcast-and-rain")){
            return R.drawable.ic_overcast_and_rain;
        }
        if (name.equals("partly-cloudy-and-light-snow")){
            return R.drawable.ic_partly_cloudy_and_light_snow;
        }
        if (name.equals("cloudy-and-snow")){
            return R.drawable.ic_cloudy_and_snow;
        }
        if (name.equals("overcast-and-snow")){
            return R.drawable.ic_overcast_and_snow;
        }
        if (name.equals("partly-cloudy-and-rain")){
            return R.drawable.ic_partly_cloudy_and_rain;
        }
        if (name.equals("cloudycloudy")){
            return R.drawable.ic_cloudycloudy;
        }
        if (name.equals("overcast-and-wet-snow")){
            return R.drawable.ic_overcast_and_wet_snow;
        }
        if (name.equals("partly-cloudy-and-snow")){
            return R.drawable.ic_partly_cloudy_and_snow;
        }

        return R.drawable.ic_partly_cloudy_and_snow;
    }

    public void update_weather() throws JSONException {
        /*
         *  обновление погоды и доп информации на экране
         */

        Weather w = new Weather(); // получаем погоду
        w.execute();

        while (weatherInJSON == null){  // ждем пока погода придет
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        JSONObject weatherObject = new JSONObject(weatherInJSON);  // погода в JSON

        JSONObject fact = weatherObject.getJSONObject("fact"); // JSON object fact

        int fact_temp = fact.getInt("temp"); // фактическая температура
        int fact_wind_speed = fact.getInt("wind_speed");  // фактическая скорость ветра
        int fact_humidity = fact.getInt("humidity");

        TextView fact_humidity_view = findViewById(R.id.humidity);
        fact_humidity_view.setText(String.valueOf(fact_humidity) + " %");  // вывод температуры на экран

        TextView fact_temp_view = findViewById(R.id.fact_temp);
        fact_temp_view.setText(String.valueOf(fact_temp) + "°");  // вывод температуры на экран

        TextView fact_wind_speed_view = findViewById(R.id.fact_wind_speed);
        fact_wind_speed_view.setText(String.valueOf(fact_wind_speed) + " m/s");


        JSONArray forecast = weatherObject.getJSONArray("forecasts");
        JSONObject f = (JSONObject)forecast.get(0);
        JSONObject parts = f.getJSONObject("parts");
        JSONObject day_forecasts = parts.getJSONObject("day");
        JSONObject night_forecasts = parts.getJSONObject("night");

        int prec_mm = night_forecasts.getInt("prec_mm"); // ожидаемые осадки
        int min_temp = night_forecasts.getInt("temp_min");  // ожидаемая минимальная температура
        int max_temp = day_forecasts.getInt("temp_min");  // ожидаемая максимальная температура

        TextView prec_mm_view = findViewById(R.id.prec_mm);
        prec_mm_view.setText(String.valueOf(prec_mm) + " mm");

        TextView min_max_view = findViewById(R.id.min_max_temp);
        min_max_view.setText(String.valueOf(max_temp) + "°/" + String.valueOf(min_temp) + "°");




        ArrayList<Integer> forecastsTemp = new ArrayList<Integer>(); // температура на день почасовая
        ArrayList<String> forecasticons = new ArrayList<>();

        JSONArray hours = f.getJSONArray("hours");

        for (int i = 0; i <= 23; i++){
            JSONObject hour;
            hour = (JSONObject) hours.get(i);
            int temp = hour.getInt("temp");
            String icon = hour.getString("condition");
            forecastsTemp.add(temp);
            forecasticons.add(icon);
        }


        // установка почасовой температуры на графическом интерфейсе

        TextView temp;
        ImageView icon;

        temp = findViewById(R.id.temp0);
        temp.setText(forecastsTemp.get(0) + "°");
        temp = findViewById(R.id.temp1);
        temp.setText(forecastsTemp.get(1) + "°");
        temp = findViewById(R.id.temp2);
        temp.setText(forecastsTemp.get(2) + "°");
        temp = findViewById(R.id.temp3);
        temp.setText(forecastsTemp.get(3) + "°");
        temp = findViewById(R.id.temp4);
        temp.setText(forecastsTemp.get(4) + "°");
        temp = findViewById(R.id.temp5);
        temp.setText(forecastsTemp.get(5) + "°");
        temp = findViewById(R.id.temp6);
        temp.setText(forecastsTemp.get(6) + "°");
        temp = findViewById(R.id.temp7);
        temp.setText(forecastsTemp.get(7) + "°");
        temp = findViewById(R.id.temp8);
        temp.setText(forecastsTemp.get(8) + "°");
        temp = findViewById(R.id.temp9);
        temp.setText(forecastsTemp.get(9) + "°");
        temp = findViewById(R.id.temp10);
        temp.setText(forecastsTemp.get(10) + "°");
        temp = findViewById(R.id.temp11);
        temp.setText(forecastsTemp.get(11) + "°");
        temp = findViewById(R.id.temp12);
        temp.setText(forecastsTemp.get(12) + "°");
        temp = findViewById(R.id.temp13);
        temp.setText(forecastsTemp.get(13) + "°");
        temp = findViewById(R.id.temp14);
        temp.setText(forecastsTemp.get(14) + "°");
        temp = findViewById(R.id.temp15);
        temp.setText(forecastsTemp.get(15) + "°");
        temp = findViewById(R.id.temp16);
        temp.setText(forecastsTemp.get(16) + "°");
        temp = findViewById(R.id.temp17);
        temp.setText(forecastsTemp.get(17) + "°");
        temp = findViewById(R.id.temp18);
        temp.setText(forecastsTemp.get(18) + "°");
        temp = findViewById(R.id.temp19);
        temp.setText(forecastsTemp.get(19) + "°");
        temp = findViewById(R.id.temp20);
        temp.setText(forecastsTemp.get(20) + "°");
        temp = findViewById(R.id.temp21);
        temp.setText(forecastsTemp.get(21) + "°");
        temp = findViewById(R.id.temp22);
        temp.setText(forecastsTemp.get(22) + "°");
        temp = findViewById(R.id.temp23);
        temp.setText(forecastsTemp.get(23) + "°");

        // установка иконок


        icon = findViewById(R.id.icon0);
        icon.setImageResource(this.name_to_id(forecasticons.get(0)));
        icon = findViewById(R.id.icon1);
        icon.setImageResource(this.name_to_id(forecasticons.get(1)));
        icon = findViewById(R.id.icon2);
        icon.setImageResource(this.name_to_id(forecasticons.get(2)));
        icon = findViewById(R.id.icon3);
        icon.setImageResource(this.name_to_id(forecasticons.get(3)));
        icon = findViewById(R.id.icon4);
        icon.setImageResource(this.name_to_id(forecasticons.get(4)));
        icon = findViewById(R.id.icon5);
        icon.setImageResource(this.name_to_id(forecasticons.get(5)));
        icon = findViewById(R.id.icon6);
        icon.setImageResource(this.name_to_id(forecasticons.get(6)));
        icon = findViewById(R.id.icon7);
        icon.setImageResource(this.name_to_id(forecasticons.get(7)));
        icon = findViewById(R.id.icon8);
        icon.setImageResource(this.name_to_id(forecasticons.get(8)));
        icon = findViewById(R.id.icon9);
        icon.setImageResource(this.name_to_id(forecasticons.get(9)));
        icon = findViewById(R.id.icon10);
        icon.setImageResource(this.name_to_id(forecasticons.get(10)));
        icon = findViewById(R.id.icon11);
        icon.setImageResource(this.name_to_id(forecasticons.get(11)));
        icon = findViewById(R.id.icon12);
        icon.setImageResource(this.name_to_id(forecasticons.get(12)));
        icon = findViewById(R.id.icon13);
        icon.setImageResource(this.name_to_id(forecasticons.get(13)));
        icon = findViewById(R.id.icon14);
        icon.setImageResource(this.name_to_id(forecasticons.get(14)));
        icon = findViewById(R.id.icon15);
        icon.setImageResource(this.name_to_id(forecasticons.get(15)));
        icon = findViewById(R.id.icon16);
        icon.setImageResource(this.name_to_id(forecasticons.get(16)));
        icon = findViewById(R.id.icon17);
        icon.setImageResource(this.name_to_id(forecasticons.get(17)));
        icon = findViewById(R.id.icon18);
        icon.setImageResource(this.name_to_id(forecasticons.get(18)));
        icon = findViewById(R.id.icon19);
        icon.setImageResource(this.name_to_id(forecasticons.get(19)));
        icon = findViewById(R.id.icon20);
        icon.setImageResource(this.name_to_id(forecasticons.get(20)));
        icon = findViewById(R.id.icon21);
        icon.setImageResource(this.name_to_id(forecasticons.get(21)));
        icon = findViewById(R.id.icon22);
        icon.setImageResource(this.name_to_id(forecasticons.get(22)));
        icon = findViewById(R.id.icon23);
        icon.setImageResource(this.name_to_id(forecasticons.get(23)));





        // установка текущей даты
        String date = weatherObject.getString("now_dt");
        date = date.substring(0, date.indexOf("T"));

        TextView current_date = findViewById(R.id.current_date);
        current_date.setText(date);

        //прокрутка погоды дня до текущего времени
        //TODO доработать, рассчитать прокрутку
        HorizontalScrollView day_temp = findViewById(R.id.day_temp);

        day_temp.scrollTo(1000, 0);

        // установка погоды на каждый день (второй блок)

        ArrayList<String> temps_for_a_week = new ArrayList<>();
        ArrayList<String> icons_for_a_week = new ArrayList<>();

        for (int i = 0; i < 7; i++){
            JSONObject day = (JSONObject) forecast.get(i);
            parts = (JSONObject) day.getJSONObject("parts");
            JSONObject day_short = parts.getJSONObject("day_short");
            temps_for_a_week.add(String.valueOf(day_short.getInt("temp")) + "°");
            icons_for_a_week.add(day_short.getString("condition"));
        }

        Calendar c = Calendar.getInstance();
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == 1){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(6));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
        }
        if (dayOfWeek == 2){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(0));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
        }
        if (dayOfWeek == 3){

            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(1));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
        }
        if (dayOfWeek == 4){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(2));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
        }
        if (dayOfWeek == 5){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(3));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
        }
        if (dayOfWeek == 6){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(5));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(4));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
        }
        if (dayOfWeek == 7){
            temp = findViewById(R.id.monday_temp);
            temp.setText(temps_for_a_week.get(6));
            temp = findViewById(R.id.tuesday_temp);
            temp.setText(temps_for_a_week.get(0));
            temp = findViewById(R.id.wednesday_temp);
            temp.setText(temps_for_a_week.get(1));
            temp = findViewById(R.id.thursday_temp);
            temp.setText(temps_for_a_week.get(2));
            temp = findViewById(R.id.friday_temp);
            temp.setText(temps_for_a_week.get(3));
            temp = findViewById(R.id.saturday_temp);
            temp.setText(temps_for_a_week.get(4));
            temp = findViewById(R.id.sunday_temp);
            temp.setText(temps_for_a_week.get(5));
            icon = findViewById(R.id.monday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(6)));
            icon = findViewById(R.id.tuesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(0)));
            icon = findViewById(R.id.wednesday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(1)));
            icon = findViewById(R.id.thursday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(2)));
            icon = findViewById(R.id.friday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(3)));
            icon = findViewById(R.id.saturday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(4)));
            icon = findViewById(R.id.sunday_icon);
            icon.setImageResource(this.name_to_id(icons_for_a_week.get(5)));
        }
/*
        Toast toast = Toast.makeText(getApplicationContext(),
                String.valueOf(day_short.getInt("temp")), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();*/



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
                        //toast.show();
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
