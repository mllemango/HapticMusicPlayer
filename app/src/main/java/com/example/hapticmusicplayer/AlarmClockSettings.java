package com.example.hapticmusicplayer;

//following this tut https://javapapers.com/android/android-alarm-clock-tutorial/
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class AlarmClockSettings extends AppCompatActivity {

    Button thirty_s;
    Button two_min;
    Button cancel;

    AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private TimePicker alarmTimePicker;
    private static AlarmClockSettings inst;

    public static AlarmClockSettings instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock_settings);

        thirty_s = (Button) findViewById(R.id.thirty_s);
        two_min = (Button) findViewById(R.id.two_min);
        cancel = (Button) findViewById(R.id.cancel);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

    }

    public void set_alarm(int addition){
        //setting alarm
        Calendar calendar = Calendar.getInstance();
//        Log.i("HapticCalendar", String.valueOf(calendar.getTimeInMillis()));
        long alarmTime = calendar.getTimeInMillis() + addition * 1000;
//        Log.i("HapticCalendar", String.valueOf(alarmTime));
        Intent myIntent = new Intent(AlarmClockSettings.this, AlarmClockSettings.class);
        pendingIntent = PendingIntent.getBroadcast(AlarmClockSettings.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC, alarmTime, pendingIntent);

        Log.i("HapticAlarm", "alarm set for " + addition + " seconds");
    }

    public void five_s_alarm(View view){
        set_alarm(5);

    }

    public void thirty_s_alarm(View view){
        set_alarm(30);

    }

    public void two_m_alarm(View view){
        set_alarm(120);
    }




}
