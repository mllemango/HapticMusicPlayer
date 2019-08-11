package com.example.hapticmusicplayer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.puredata.android.io.AudioParameters;
import org.puredata.android.io.PdAudio;
import org.puredata.android.utils.PdUiDispatcher;
import org.puredata.core.PdBase;

import java.io.File;
import java.io.IOException;

public class PDPatch {

    EditText sineText;
    EditText sawText;
    EditText sqrText;
    EditText sawVol;

    int sine_progress_val = 65;
    int sqr_progress_val = 20;
    int saw_progress_val = 20;
    int saw_vol_progress_val = 20;

    protected Context context;



    public PDPatch(Context context, File dir){
        this.context = context;
        try{
            initPD();
            loadPDPatch(dir);
        }
        catch (IOException e) {
            Log.i("onCreate", "initialization and loading gone wrong :(");
        }

    }

    private PdUiDispatcher dispatcher;

    private void initPD() throws IOException {
        int samplerate = AudioParameters.suggestSampleRate();
        PdAudio.initAudio(samplerate, 0 , 2, 8, true);

        dispatcher = new PdUiDispatcher();
        PdBase.setReceiver(dispatcher);

    }

    private void loadPDPatch(File dir){

        File pdPatch = new File(dir, "test_all_waves.pd");
        try {
            PdBase.openPatch(pdPatch.getAbsolutePath());
        } catch (IOException e) {
            Log.i("opening patch", "error opening patch");
            Log.i("opening patch", e.toString());
        }

    }

    //toggle button to control sine on/off:
    public void playSine(View view) {
        PdAudio.startAudio(context);
        PdBase.sendFloat("sineonOff", 1.0f);
        PdBase.sendFloat("sinefreqNum", sine_progress_val);


    }

    public void stopSine(View view){
        PdBase.sendFloat("sineonOff", 0.0f);
        PdAudio.stopAudio();
    }
}
