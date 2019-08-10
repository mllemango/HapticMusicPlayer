package com.example.hapticmusicplayer;

import android.content.res.Resources;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = (ImageView) findViewById(R.id.playBtn);
        nextBtn = (ImageView) findViewById(R.id.nextBtn);
        prevBtn = (ImageView) findViewById(R.id.prevBtn);
        elapsedTimeLabel = (TextView) findViewById(R.id.elapsedTimeLabel);
        remainingTimeLabel = (TextView) findViewById(R.id.remainingTimeLabel);
        title = (TextView) findViewById(R.id.songTitle);
        LinearLayout myView = (LinearLayout) findViewById(R.id.myView);

        // Media Player
        mp = MediaPlayer.create(this, R.raw.music2);
        mp.setLooping(true);
        mp.seekTo(0);
        mp.setVolume(0.5f, 0.5f);
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

        myView.setOnTouchListener(handleTouch);

//        //the on release motions
//        playBtn.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                if (event.getAction()== MotionEvent.ACTION_UP){
//                    playBtnClick(view);
//                }
//                return true;
//            }
//        });

    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private View.OnTouchListener handleTouch = new View.OnTouchListener() {

        int width = getScreenWidth();
        int height = getScreenHeight();

        //play button is top ~50 percent
        double playBtnHeight = height * 0.48;

        //next and back buttons are 50-70 percent
        double skipBtnsHeight = height*0.7;
        double skipBtnsWidth = width*0.5;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    Log.i("TAG", "touched up" + " (" + x + ", " + y + ")");

                    if(y < playBtnHeight){
                        playpause(v);
                        Log.i("TAG", "play/pause");
                    }
                    else if(y < skipBtnsHeight){
                        if(x<skipBtnsWidth){
                            //back button is pressed
                            prevBtnClick(v);
                            Log.i("TAG", "back");
                        }
                        else{
                            nextBtnClick(v);
                            Log.i("TAG", "next");
                        }
                    }
                    break;
            }



            return true;
        }
    };

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



    public void playBtnClick(View view) {

        if (!mp.isPlaying()) {
            // Stopping
            mp.start();
            playBtn.setBackgroundResource(R.drawable.stop);

        } else {
            // Playing
            mp.pause();
            playBtn.setBackgroundResource(R.drawable.play);
        }

    }

    public void playpause(View view){
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
        mp.pause();
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

        setPositionBar();
        mp.start();
        playBtn.setImageResource(R.drawable.stop);
    }

    public void prevBtnClick(View view){
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
}