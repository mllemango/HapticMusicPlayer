package com.example.hapticmusicplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    ImageView playBtn;
    ImageView nextBtn;
    ImageView prevBtn;
    SeekBar positionBar;
    SeekBar volumeBar;
    TextView elapsedTimeLabel;
    TextView remainingTimeLabel;
    MediaPlayer mp;
    int totalTime;
    String currentSong = null;
    TextView title;

    Boolean haptics = false;
    int sine_progress_val = 65;
    int sqr_progress_val = 20;
    int saw_progress_val = 50;
    int saw_vol_progress_val = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        //set up for the rest of the screen

        playBtn = (ImageView) findViewById(R.id.playBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);
        prevBtn = (ImageView) findViewById(R.id.prevBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        title = (TextView) findViewById(R.id.songTitle);


        // Media Player
        mp = MediaPlayer.create(this, R.raw.music2);
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0f, 0.5f);
        currentSong = "music2";
        title.setText("Butterfly Kiss");

        // Position Bar
        setPositionBar();


        // Volume Bar
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float volumeNum = progress / 100f;
                        mp.setVolume(volumeNum, volumeNum);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );

        // Thread (Update positionBar & timeLabel)
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mp != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mp.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {}
                }
            }
        }).start();

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

        //play button is top ~50 percent
        double playBtnHeight = height * 0.48;
        double settingsBtnWidth = width * 0.11;
        double settingsBtnHeight = height * 0.11;

        //next and back buttons are 50-70 percent
        double skipBtnsHeight = height*0.7;
        double skipBtnsWidth = width*0.5;

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
                    if(y < playBtnHeight){
                        if(x<settingsBtnWidth && y<settingsBtnHeight){
                            //settings button is selected
                            settingsBtnClick(v);
                            Log.i("TAG", "settings");
                        }
                        else{
                            //play button is selected otherwise
                            playBtnClick(v);
                            Log.i("TAG", "play/pause");
                        }

                    }
                    else if(y < skipBtnsHeight){
                        if(x<skipBtnsWidth){
                            //back button is selected
                            prevBtnClick(v);
                            Log.i("TAG", "back");
                        }
                        else{
                            //next button is selected
                            nextBtnClick(v);
                            Log.i("TAG", "next");
                        }
                    }
                    break;
                case MotionEvent.ACTION_DOWN:
                    if(y < playBtnHeight) {
                        //play button - sine
                        playHaptics("sine");
                    }
                    else if(y < skipBtnsHeight){
                        if(x < skipBtnsWidth){
                            //back button is pressed - square
                            playHaptics("square");
                        }
                        else{
                            //next button is pressed - saw
                            playHaptics("saw");
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(y < playBtnHeight) {
                        //play button - sine
                        playHaptics("sine");
                    }
                    else if(y < skipBtnsHeight){
                        if(x < skipBtnsWidth){
                            //back button is pressed - square
                            playHaptics("square");
                        }
                        else{
                            //next button is pressed - saw
                            playHaptics("saw");
                        }
                    }
                    break;
            }
            return true;
        }
    };

    public void settingsBtnClick(View view){
        //goes to the settings page
        Intent intent = new Intent(this, PDSettings.class);
        startActivity(intent);
    }

    public void playBtnClick(View view) {
        //starts playing music, else stops playing music
        if (!mp.isPlaying()) {
            // Stopping
            mp.start();
            playBtn.setImageResource(R.drawable.stop);

        } else {
            // Playing
            mp.pause();
            playBtn.setImageResource(R.drawable.play);
        }

    }

    public void nextBtnClick(View view){
        //skipping to the next song
        mp.pause(); //have to stop the current song
        //hardcoding the next song lol
        if (currentSong.contains("2")){
            mp = MediaPlayer.create(this, R.raw.music3);
            mp.setLooping(true);
            currentSong = "music3";
            title.setText("Confession/Secret");
        }
        else if (currentSong.contains("3")){
            mp = MediaPlayer.create(this, R.raw.music4);
            mp.setLooping(true);
            currentSong = "music4";
            title.setText("Layer Cake");
        }
        else if (currentSong.contains("4")){
            mp = MediaPlayer.create(this, R.raw.music);
            mp.setLooping(true);
            currentSong = "music";
            title.setText("Alleycat");
        }
        else{
            mp = MediaPlayer.create(this, R.raw.music2);
            mp.setLooping(true);
            currentSong = "music2";
            title.setText("Butterfly Kiss");
        }

        setPositionBar(); //resetting the position bar
        mp.start(); //starting new song
        playBtn.setImageResource(R.drawable.stop); //play button now shows its playing
    }

    public void prevBtnClick(View view){
        //similar to nextBtnClick()
        mp.pause();

        if (currentSong.contains("2")){
            mp = MediaPlayer.create(this, R.raw.music);
            mp.setLooping(true);
            currentSong = "music";
            title.setText("Alleycat");
        }
        else if (currentSong.contains("3")){
            mp = MediaPlayer.create(this, R.raw.music2);
            mp.setLooping(true);
            currentSong = "music2";
            title.setText("Butterfly Kiss");
        }
        else if (currentSong.contains("4")){
            mp = MediaPlayer.create(this, R.raw.music3);
            mp.setLooping(true);
            currentSong = "music3";
            title.setText("Confession/Secret");
        }
        else{
            mp = MediaPlayer.create(this, R.raw.music4);
            mp.setLooping(true);
            currentSong = "music4";
            title.setText("Layer Cake");
        }

        setPositionBar();
        mp.start();
        playBtn.setImageResource(R.drawable.stop);
    }

    /*
    methods for the actual music player
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            // Update positionBar.
            positionBar.setProgress(currentPosition);

            // Update Labels.
            String elapsedTime = createTimeLabel(currentPosition);
            elapsedTimeLabel.setText(elapsedTime);

            String remainingTime = createTimeLabel(totalTime-currentPosition);
            remainingTimeLabel.setText("- " + remainingTime);
        }
    };

    private void setPositionBar(){
        totalTime = mp.getDuration();

        positionBar = (SeekBar) findViewById(R.id.positionBar);
        positionBar.setEnabled(false);
        positionBar.setMax(totalTime);
        positionBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            positionBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }


    public String createTimeLabel(int time) {
        String timeLabel = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;

        return timeLabel;
    }
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