package com.wavematters.muwave;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
//
// Class: Media Player
//
public class muPlayer extends Activity implements
        OnCompletionListener, OnClickListener {

    private static final String TAG = "WM";
    // TODO: Add new package abd get URL from method
    String muUrl = "file:///data/data/com.wavematters.muwave/wm.mid";

    MediaPlayer mediaPlayer;
    View theView;
    private Button muToggle, muRewind;
    private TextView timeCurrent, timeFinal, muName;
    private SeekBar progBar;
    private double currentTime = 0, finalTime = 0;
    private int position = 0;
    private int oneTimeOnly = 0;
    private Handler myHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        muToggle = (Button) this.findViewById(R.id.butToggle);
        muRewind = (Button) this.findViewById(R.id.butRewind);
        progBar = (SeekBar) findViewById(R.id.playBar);

        muToggle.setOnClickListener(this);
        muRewind.setOnClickListener(this);
        progBar.setOnClickListener(this);
        progBar.setClickable(false);

        timeCurrent = (TextView) findViewById(R.id.timeCurrent);
        timeFinal = (TextView) findViewById(R.id.timeFinal);
        muName = (TextView) findViewById(R.id.nameSong);

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.reset();
    }
    public void onCompletion(MediaPlayer mp) {
        mediaPlayer.start();
        rewind();
    }
    @Override
    public void onClick(View v) {
        if (v == muToggle) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            } else {
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (v == muRewind) {
            mediaPlayer.seekTo(position);
            mediaPlayer.reset();
        }
    }
    public void prepare() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(muUrl);
            //mediaPlayer.prepare(); // might take long! (for buffering, etc)
            mediaPlayer.prepareAsync();
        }                         // when to rest?
        catch (IOException e) {
            Log.i(TAG, "Media player exception on prepare");
        }
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play();
            }
        });
    }
    public void play() {
        String songName = "muWave Song";
        try {
            InputStream is = openFileInput(
                    "/data/data/com.wavematters.muwave/wmnamecq.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            if ((songName = reader.readLine()) == null) {
                songName = "muWave Song";
            }
        } catch (Exception e) {
            Log.i(TAG, "Error. Failed to open name file -- defaulting name");
        }
        muName.setText(songName);
        mediaPlayer.start();
        finalTime = mediaPlayer.getDuration();
        currentTime = mediaPlayer.getCurrentPosition();

        if (oneTimeOnly == 0) {
            progBar.setMax((int) finalTime);
            oneTimeOnly = 1;
        }
        timeCurrent.setText(String.format(Locale.US, "%d sec",
                TimeUnit.MILLISECONDS.toSeconds((long) finalTime)));
        timeFinal.setText(String.format(Locale.US, "%d sec",
                TimeUnit.MILLISECONDS.toSeconds((long) currentTime)));
        progBar.setProgress((int) currentTime);
        myHandler.postDelayed(UpdateSongTime, 100);
        muToggle.setBackgroundColor(
                getResources().getColor(R.color.colorPauseYellow));
    }
    public void rewind() {
        mediaPlayer.seekTo(position);
        mediaPlayer.stop();
        mediaPlayer.release();
        progBar.setProgress((int) 0);
        timeCurrent.setText("");
        timeFinal.setText("");
        muName.setText("");
    }
    //
    //
    //
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            currentTime = mediaPlayer.getCurrentPosition();
            timeCurrent.setText(String.format(Locale.US, "%d sec",
                    TimeUnit.MILLISECONDS.toSeconds((long) currentTime)));
            progBar.setProgress((int) currentTime);
            myHandler.postDelayed(this, 100);
        }
    };
}
