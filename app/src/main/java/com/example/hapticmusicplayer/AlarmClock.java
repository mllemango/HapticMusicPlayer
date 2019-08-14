package com.example.hapticmusicplayer;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

public class AlarmClock extends AppCompatActivity {

    Boolean haptics = false;
    int sine_progress_val = 65;
    int sqr_progress_val = 20;
    int saw_progress_val = 50;
    int saw_vol_progress_val = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock);

        // set up for pd patches
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        haptics = preferences.getBoolean("haptics",false);//it returns stored boolean value else returns false
        Log.i("HapticMusicPlayer", "main haptics: " + haptics);

        LinearLayout myView = (LinearLayout) findViewById(R.id.myView);

        myView.setOnTouchListener(handleTouch);

        try {
            initPD();
            loadPDPatch();

        } catch (IOException e) {
            Log.i("HapticMusicPlayer", "initialization and loading gone wrong :(");
            finish();
        }
    }

    /*
    methods for setting up PD
    initPD(): initialize PD
    loadPDPatch(): loads the specific patch into the phone for usage
     */

    private PdUiDispatcher dispatcher;

    private void initPD() throws IOException {
        int samplerate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(samplerate, 0 , 2, 8, true);

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

    }

    private void loadPDPatch(){
        File dir = getFilesDir();
        try {
            IoUtils.extractZipResource(getResources().openRawResource(R.raw.test_all_waves), dir, true);
            Log.i("HapticMusicPlayer", dir.getAbsolutePath());
        } catch (IOException e) {
            Log.i("HapticMusicPlayer", "error unzipping");
        }
        File pdPatch = new File(dir, "test_all_waves.pd");
        try {
            PdBase.openPatch(pdPatch.getAbsolutePath());
        } catch (IOException e) {
            Log.i("HapticMusicPlayer", "error opening patch");
            Log.i("HapticMusicPlayer", e.toString());
        }

    }

    /*
    methods of playing specific waves
     */
    private void playHaptics(String wave){
        //when you play a wave, gotta stop the other waves
        if (haptics) {
            if (wave.contains("sine")) {
                playSine();
                stopSaw();
                stopSquare();
            } else if (wave.contains("square")) {
                playSquare();
                stopSaw();
                stopSine();
            } else {
                playSaw();
                stopSquare();
                stopSine();
            }
        }
    }

    private void stopHaptics(){
        //stopping all waves
        stopSine();
        stopSquare();
        stopSaw();
    }

    //specifying on touch behaviour
    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        int width = getScreenWidth();
        int height = getScreenHeight();

        double offBtnHeight = width*0.5;
        double snoozeBtnHeight = width*0.5;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            //event.getAction tells us if what the finger is doing
            switch (event.getAction()) {
                //if user lifts finger up, they want to "press" the button
                case MotionEvent.ACTION_UP:
//                    Log.i("TAG", "touched up" + " (" + x + ", " + y + ")");
                    stopHaptics(); //stopping all the haptics when the user lifts up finger

                    // determining where the user lifted their finger
                    if(y < offBtnHeight){
                        Log.i("HapticMusicPlayer", "alarm off");

                    }
                    else{
                        Log.i("HapticMusicPlayer", "alarm snooze");
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(y < offBtnHeight){
                        Log.i("HapticMusicPlayer", "alarm off");

                    }
                    else{
                        Log.i("HapticMusicPlayer", "alarm snooze");
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(y < offBtnHeight){
                        Log.i("HapticMusicPlayer", "alarm off");

                    }
                    else{
                        Log.i("HapticMusicPlayer", "alarm snooze");
                    }
                    break;
            }
            return true;
        }
    };

    /*
    small misc methods used in previous methods
     */
    private void playSine(){
        PdBase.sendFloat("sineonOff", 1.0f);
        PdBase.sendFloat("sinefreqNum", sine_progress_val);
    }

    private void stopSine(){
        PdBase.sendFloat("sineonOff", 0.0f);
    }

    private void playSquare(){
        PdBase.sendFloat("sqronOff", 1.0f);
        PdBase.sendFloat("sqrfreqNum", sqr_progress_val);
    }

    private void stopSquare(){
        PdBase.sendFloat("sqronOff", 0.0f);
    }

    private void playSaw(){
        PdBase.sendFloat("sawonOff", 1.0f);
        PdBase.sendFloat("sawfreqNum", saw_progress_val);
        PdBase.sendFloat("sawvolNum", saw_vol_progress_val);

    }

    private void stopSaw(){
        PdBase.sendFloat("sawonOff", 0.0f);
    }

    @Override
    protected void onResume(){
        super.onResume();
        PdAudio.startAudio(this);

    }

    @Override
    protected void onPause(){
        super.onPause();
        PdAudio.stopAudio();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

}
