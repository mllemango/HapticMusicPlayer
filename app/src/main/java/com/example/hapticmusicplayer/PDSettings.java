package com.example.hapticmusicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.ToggleButton;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;
import org.puredata.core.utils.IoUtils;

import java.io.File;
import java.io.IOException;


public class PDSettings extends AppCompatActivity {

    EditText sineText;
    EditText sawText;
    EditText sqrText;

    SeekBar sineSlider;
    SeekBar sawSlider;
    SeekBar sqrSlider;

    SeekBar sawVolSlider;

    EditText sawVol;
    Switch haptciSwitch;
    Boolean haptics = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdsettings);

        try{
            Log.i("onCreate","initializing PD page");
            initPD();
            loadPDPatch();

        }
        catch (IOException e) {
            Log.i("onCreate", "initialization and loading gone wrong :(");
            finish();
        }
        initSine();
        initSaw();
        initSqr();
        initVolSaw();


        haptciSwitch = (Switch) findViewById(R.id.hapticSwitch);
        haptciSwitch.setChecked(getBooleanFromSP(this));

        haptciSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if(isChecked){
                    editor.putBoolean("haptic",isChecked);
                    Log.i("haptics toggle", "true");
                }
                else
                {
                    editor.putBoolean("haptic",isChecked);
                    Log.i("haptics toggle", "false");

                }

                editor.apply();
                // how do i toggle visibility of mExplanation text in my QuizActivity.java from here?
            }
        });

    }

    public boolean getBooleanFromSP(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SETTINGS", MODE_PRIVATE);
        return preferences.getBoolean("haptic", false);
    }

    public int sine_progress_val;
    public int saw_progress_val;
    public int sqr_progress_val;

    public int saw_vol_progress_val;

    private void initSine(){
        sineText = (EditText) findViewById(R.id.sineNum);
        sineSlider = (SeekBar) findViewById(R.id.sineSlider);

        //final Switch onOffSwitch = findViewById(R.id.onOffSwitch);

        sineSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // updated continuously as the user slides the thumb
                        sineText.setText(String.valueOf(progress*3));
                        sine_progress_val = progress * 3;
                        PdBase.sendFloat("sinefreqNum", sine_progress_val);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // called when the user first touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // called after the user finishes moving the SeekBar
                    }
                }
        );
    }

    private void initVolSaw(){

        sawVolSlider = (SeekBar) findViewById(R.id.sawVolSlider);
        sawVol = (EditText) findViewById(R.id.sawVol);

        sawVolSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // updated continuously as the user slides the thumb
                        sawVol.setText(String.valueOf(progress));
                        saw_vol_progress_val = progress;
                        PdBase.sendFloat("sawvolNum", saw_vol_progress_val);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // called when the user first touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // called after the user finishes moving the SeekBar
                    }
                }
        );
    }

    private void initSaw(){
        sawText = (EditText) findViewById(R.id.sawNum);
        sawSlider = (SeekBar) findViewById(R.id.sawSlider);

        sawSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // updated continuously as the user slides the thumb
                        sawText.setText(String.valueOf(progress));
                        saw_progress_val = progress;
                        PdBase.sendFloat("sawfreqNum", saw_progress_val);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // called when the user first touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // called after the user finishes moving the SeekBar
                    }
                }
        );
    }

    private void initSqr(){
        sqrText = (EditText) findViewById(R.id.sqrNum);
        sqrSlider = (SeekBar) findViewById(R.id.sqrSlider);

        //final Switch onOffSwitch = findViewById(R.id.onOffSwitch);
        sqrSlider.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // updated continuously as the user slides the thumb
                        sqrText.setText(String.valueOf(progress));
                        sqr_progress_val = progress;
                        PdBase.sendFloat("sqrfreqNum", sqr_progress_val);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // called when the user first touches the SeekBar
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // called after the user finishes moving the SeekBar
                    }
                }
        );
    }

    private void loadPDPatch(){
        File dir = getFilesDir();
        try {
            IoUtils.extractZipResource(getResources().openRawResource(R.raw.test_all_waves), dir, true);
            Log.i("unzipping", dir.getAbsolutePath());
        } catch (IOException e) {
            Log.i("unzipping", "error unzipping");
        }
        File pdPatch = new File(dir, "test_all_waves.pd");
        try {
            PdBase.openPatch(pdPatch.getAbsolutePath());
        } catch (IOException e) {
            Log.i("opening patch", "error opening patch");
            Log.i("opening patch", e.toString());
        }

    }

    private PdUiDispatcher dispatcher;

    private void initPD() throws IOException {
        int samplerate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(samplerate, 0 , 2, 8, true);

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

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

    //toggle button to control sine on/off:
    public void sineState(View view) {
        boolean checked = ((ToggleButton)view).isChecked();
        if (checked) {
            PdBase.sendFloat("sineonOff", 1.0f);
            PdBase.sendFloat("sinefreqNum", sine_progress_val);
        } else {
            PdBase.sendFloat("sineonOff", 0.0f);
        }
    }

    //toggle button to control saw on/off:
    public void sawState(View view) {
        boolean checked = ((ToggleButton)view).isChecked();
        if (checked) {
            PdBase.sendFloat("sawonOff", 1.0f);
            PdBase.sendFloat("sawfreqNum", saw_progress_val);
            PdBase.sendFloat("sawvolNum", saw_vol_progress_val);
        } else {
            PdBase.sendFloat("sawonOff", 0.0f);
        }
    }

    //toggle button to control sqr on/off:
    public void sqrState(View view) {
        boolean checked = ((ToggleButton)view).isChecked();
        if (checked) {
            PdBase.sendFloat("sqronOff", 1.0f);
            PdBase.sendFloat("sqrfreqNum", sqr_progress_val);
        } else {
            PdBase.sendFloat("sqronOff", 0.0f);
        }
    }
}

