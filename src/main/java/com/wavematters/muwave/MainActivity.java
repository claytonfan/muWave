package com.wavematters.muwave;
//
// Wavematters muWavs
//
// Music generation by evolution
//
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public final int
            NORMAL          =   0,
            ERR_COMPUTE     = -01,
            ERR_SPEC        = -10,
            ERR_MIDI_OPEN   = -81,
            ERR_MIDI_READ   = -84,
            ERR_MIDI_WRITE  = -86,
            ERR_MIDI_CLOSE  = -88,
            ERR_UNKNOWN     = -99;
    private final int
            REQUEST_QUICK_STORAGE = 4,
            REQUEST_MIDI_STORAGE  = 8;
    private final int DefaultSongLength = 30; // in sec
    private final String dstDir = "wavematters";
    private final String exeWM  = "wm";
    private final String midiFileName = "wm.mid";
    private final String nameFileName = "wmnamecq.txt";

    private Context  muContext;
    private Activity muActivity;
    private Message Msg;

    Integer songLen = DefaultSongLength;
    List<Genre> genres = null;
    ListView genreList;
    String muSpec         = "";
    String specPath       = "";
    String songName       = "";
    String songMidiPrefix = "";
    String muUrl;
    String testStr = "Original";
    // Default -- will overwrite
    private String thisPath = "/data/data/com.wavematters.muwave";
    private String midiFile;
    private String nameFile;

    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get the application context
        muContext   = getApplicationContext();
        muActivity  = MainActivity.this;
        //
        Msg = new Message( muActivity );
        // Get up directrories
        thisPath    = muContext.getFilesDir().getPath();
        Msg.log( "Path = " + thisPath );
        midiFile = thisPath + "/" + midiFileName;
        nameFile = thisPath + "/" + nameFileName;
        muUrl = "file://" + midiFile;
        // Setup objects
        muPlayerInit();
        setupQuickSave();
        setupSongBar();
        //
        // Allow user to select musical genre from list
        //
        genreList = (ListView) findViewById(R.id.genreList);
        try {
            GenreParse parser = new GenreParse();
            genres = parser.parse(getAssets().open("genres.xml"));
            ArrayAdapter<Genre> adapter = new ArrayAdapter<Genre>(
                    this, R.layout.list_item, R.id.itemText, genres);
            genreList.setAdapter(adapter);
        } catch (IOException e) {
            Msg.log( "Error. Specification not fetched.");
            e.printStackTrace();
        }
        genreList.setClickable(true);
        genreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int index, long id) {
          String dialogTitle;
          if( index == 0 ) {
              dialogTitle = "Create Song of Surprise";
              position = (int)((genres.size()-1)*Math.random()) + 1;
          }
          else {
              dialogTitle = "Create Song for " + genres.get(index).getName();
              position = index;
          }
          songName       = genres.get(position).getName();
          songMidiPrefix = genres.get(position).getPrefix();
          muSpec         = genres.get(position).getSpec();
          specPath       = thisPath + "/" + muSpec;
          if (muSpec != null && !muSpec.equals("none")) {
              // Bring up confirmatio dialogue
              muStop();   //stop playing before popping up dialog
              diagCreateWM( dialogTitle );
          }
            }
        });
    }                                // onCreate()
    //
    // Setup and call music genration engine WM muWave
    //
    private void createWM( boolean playWM ) {
        try {
            // Copy spec file from Assets to internal storage
            // and include it ib command for WM muWave
            if( !copySpec( muSpec ) ) {
                Msg.log( specPath + " not copied" );
            } else {
                Msg.show("Creating song for " + songName );
                String command = exeWM + " -i " + specPath + " -t " + songLen
                        + " -o " + midiFile;
                Msg.log( command );
                // Call WM muWave
                int muStat = muWave(command);
                switch (muStat) {
                    case NORMAL:
                        Msg.show( "Song for " + songName + " created" );
                        saveSongName(songName, songMidiPrefix );
                        if( playWM ) muStart(); // prepare and then play
                        resetSongBar( false );
                        break;
                    case ERR_COMPUTE:
                        Msg.broadcast("Computation error for " + songName );
                        break;
                    case ERR_MIDI_OPEN:
                    case ERR_MIDI_WRITE:
                    case ERR_MIDI_CLOSE:
                        Msg.broadcast("Song " + songName + " not created");
                        break;
                    case ERR_UNKNOWN:
                        Msg.broadcast( "muWave system error");
                        break;
                    default:
                        Msg.broadcast( "muWave error exit");
                }
                // Delete possibly obsolete files
                File fspec = new File( specPath );
                if (fspec.delete()) {
                    Msg.log( "Spec File "+ specPath +" deleted");
                } else {
                    Msg.log( "Spec File "+ specPath +" NOT deleted");
                }
            }
        } catch (Exception e) {
            Msg.log(  "WM execution system error");
            e.printStackTrace();
        }
    }                                              // createWM()
    //
    // Save song name and default MIDI file name to be restored later
    // TODO: Eventually read from MIDI file. Method will be removed
    //
    private void saveSongName(String name, String prefix ) throws IOException {
        // Overwrite old file
        String fileName = prefix + "-" + hexTime();  // save without ".mid"
        OutputStream os = new FileOutputStream( nameFile, false );
        Msg.log("Name File " + nameFile + " created");
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(name + "\n");
        writer.write(  fileName + "\n" );
        writer.flush();
        writer.close();
    }
    private String readSongName() {
        // TODO: instead of saving song name, get it from MIFI file
        String songNameRead = "muWave Song";
        try {
            FileInputStream is = muContext.openFileInput( nameFileName );
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(is));
            songNameRead = reader.readLine();
            if ( songName == null) songNameRead = "muWave Song";
            reader.close();
        } catch (Exception e) {
            Msg.log( "Error. Failed to read " + nameFileName );
            e.printStackTrace();
        }
        return songNameRead;
    }
    private String readMidiName() {
        // TODO: instead of saving song prefix, get it from MIFI file
        String fileNameRead = "wmsong";
        try {
            FileInputStream is = muContext.openFileInput( nameFileName );
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(is));
            String throwAway = reader.readLine();
            fileNameRead   = reader.readLine();
            if ( fileNameRead == null) fileNameRead = "wmsong";
            reader.close();
        } catch (Exception e) {
            Msg.log( "Error. Failed to read " + nameFileName );
            e.printStackTrace();
        }
        return fileNameRead;   // return without ".mid"
    }
    //
    // Copy MIDI to external storage and rename
    //
    private void saveMidi( String savedSongName, String fileName ) {
        // Save MIDI from internal to external storage with a retrieved file name
        String extDir  = Environment.getExternalStorageDirectory().toString();
        String dstMidiName = fileName + ".mid";
        String dstPath =  extDir + File.separator + dstMidiName;
        if( FileUtil.copyFileStream( midiFile, dstPath ) ) {
            Msg.show("File "+ fileName + ".mid" +
                    " saved for song "+ savedSongName );
        } else {
            Msg.show("Failed to save " + fileName
                    + ".mid" + " for song " + savedSongName );
       }
    }
    //
    // Copy spec file from asset directoty to internal storage
    //
    private Boolean copySpec( String fileSpec ) {
        InputStream is;
        OutputStream os;
        String dstPath;
        try {
            AssetManager assetFiles = getAssets();
            dstPath = thisPath + "/" + fileSpec;
            is = assetFiles.open( fileSpec );
            os = new FileOutputStream( dstPath );
            if( FileUtil.copyFileText( is, os ) == 0 ) {
                return false;
            }

        } catch (FileNotFoundException e) {
            Msg.show( "File " + fileSpec + " not found");
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Msg.show( "Unkown error in copying " + fileSpec );
            e.printStackTrace();
            return false;
        }
        Msg.log( "File " + fileSpec + " copied");
        return true;
    }
    //
    // Quick-save button
    //
    public void setupQuickSave() {
        Button quickSave = (Button)  findViewById(R.id.quickSave);
        quickSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestExternalStorage(REQUEST_QUICK_STORAGE); }
        });
    }
    //
    // Song length seekbar
    // Allow user to select the duration of the song
    //
    private TextView lengSong;
    private SeekBar songBar;
    public void setupSongBar() {
        Button lengDefault = (Button)  findViewById(R.id.defaultLen);
        lengSong    = (TextView)findViewById(R.id.lengSong);
        songBar     = (SeekBar) findViewById(R.id.songBar);
        songBar.setMax( 75 );
        songBar.setProgress( songLen - 4 );
        songBar.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar arg0, int progress6, boolean arg2  ) {
                // Changes faster above default of 30 sec rang 4-128 sec
                if( progress6 <= 26 ) {
                    songLen = progress6 + 4;
                } else {
                    songLen = (2*progress6)-22;  // 30+(2(progress6-26))
                }
                lengSong.setText( String.format(Locale.US, "%d sec", songLen) );
            }
            @Override
            public void onStopTrackingTouch( SeekBar songBar ) {}
            @Override
            public void onStartTrackingTouch( SeekBar songBar ) {}
        });
        lengDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { resetSongBar( true ); }
        });
    }
    public void resetSongBar( Boolean showTime ) {
        songLen = DefaultSongLength;
        songBar.setProgress( songLen - 4 );
        if( showTime ) {
            lengSong.setText( String.format(Locale.US, "%d sec", songLen) );
        } else {
            lengSong.setText(" ");
        }
    }
    //
    // Media Player methods
    // TODO: Create a class for them
    //
    static final int
        IDLE       = 0,
        INITIAL    = 1,
        PREPARING  = 2,
        PREPARED   = 3,
        STARTED    = 4,
        PAUSED     = 5,
        STOPPED    = 6,
        PLAYED     = 7,
        END        = 8,
        DESTROYED  = 9;
    private int muState = 0;
    private MediaPlayer muPlayer = null;
    private Button muToggle, muReset;
    private TextView timeCurrent, timeFinal, muName;
    private SeekBar progBar;
    private double currentTime = 0, finalTime = 0;
    private int position = 0;
    private Handler  muHandler  = null;
    private Runnable muRunnable = null;
    // Setup methods
    protected void muPlayerInit() {
        setPlayer();
        setViews();
        setHandler();
        displayViews( INITIAL );
    }
    private void setPlayer() {
        if( muPlayer != null ) {
            muPlayer.release();
            muPlayer = null;
        }
        muPlayer = new MediaPlayer();
        //muPlayer.reset();
        muState = INITIAL;
        muPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String errorWhat;
                switch(what){
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        errorWhat = "MEDIA_ERROR_UNKNOWN";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        errorWhat = "MEDIA_ERROR_SERVER_DIED";
                        break;
                    default:
                        errorWhat = "!";
                }
                String errorExtra;
                switch(extra){
                    case MediaPlayer.MEDIA_ERROR_IO:
                        errorExtra = "MEDIA_ERROR_IO";
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        errorExtra = "MEDIA_ERROR_MALFORMED";
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        errorExtra = "MEDIA_ERROR_UNSUPPORTED";
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        errorExtra = "MEDIA_ERROR_TIMED_OUT";
                        break;
                    default:
                        errorExtra = "!";
                }
                Msg.show( "Error" + "\n" + errorWhat + "\n" + errorExtra );
                mp.release();
                setPlayer();
                return true;
            }
        });
        muPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer mp) {
                //
                // If within a second to the end, end of play
                //
                muState = PLAYED;
                if( muPlayer == null || muPlayer.getDuration() -
                        muPlayer.getCurrentPosition() < 1000 ) {
                    muEnd();
                }
                // Otherwise continue playing
            }
        });
    }                                         // setPlayer()
    private void setHandler() {
        /*if( muHandler == null ) */ muHandler = new Handler();
    }
    private void setViews() {
        muToggle    = (Button)  findViewById(R.id.butToggle);
        muReset     = (Button)  findViewById(R.id.butRewind);
        progBar     = (SeekBar) findViewById(R.id.playBar);
        timeCurrent = (TextView)findViewById(R.id.timeCurrent);
        timeFinal   = (TextView)findViewById(R.id.timeFinal);
        muName      = (TextView)findViewById(R.id.nameSong);
        // whenever wm.mid exists -- also color of button green
        progBar.setClickable(true);
        muToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { muToggle(v); }
        });
        muReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { muStop(); }
        });
        progBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    SeekBar progBar, int progress, boolean fromUser) {
                if( fromUser ) muProgress(progress); }
            @Override
            public void onStartTrackingTouch(SeekBar progBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar progBar) { }
        });
    }
    void muProgress( int prog ) {
        if( muPlayer != null && (
            muState  == PREPARED ||
            muState  == STARTED  ||
            muState  == PAUSED   ||
            muState  == PLAYED   ) ) {
            muPlayer.seekTo(prog);
        }
    }
    public void muToggle(View v) {
        if( muPlayer != null && v == muToggle ) {
             if (muPlayer.isPlaying()) {
                 muPause();
             } else if( muState == PAUSED ) {
                 muContinue();
             } else {
                 muStart();
             }
        }
    }
    public void muPause() {
        if( muPlayer != null && (
            muState  == STARTED ||
            muState  == PAUSED   ||
            muState  == PLAYED   ) ) {
            muPlayer.pause();
            muState = PAUSED;
            displayViews(PAUSED);
        }
    }
    public void muContinue() {
        // may need to set listener, stop and prepare
        if( muPlayer != null && (
            muState  == PREPARED ||
            muState  == STARTED  ||
            muState  == PAUSED   ) ) {
            if( muPlayer.isPlaying() ) muPlayer.stop();
            muState = STARTED;
            displayViews(STARTED);
            muPlayer.start();
            muPlay();
        }
    }
    public void muStart() {
        setPlayer();
        setHandler();
        try {
            if( muPlayer != null && (
                muState  == INITIAL  ||
                muState  == PREPARED ||
                muState  == STARTED  ||
                muState  == STOPPED  ||
                muState  == PAUSED   ||
                muState  == PLAYED   ) ) {
                muUrl = "file://" + thisPath + "/wm.mid"; // need to do it here
                muStop();   // for some states, stop before prepare
                muPlayer.setDataSource(muUrl);
                // Set listener before prepareAsync()
                muPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        displayViews(PREPARED);
                        muState = STARTED;
                        muPlayer.start();
                        muPlay();
                    }
                });
                muPlayer.prepareAsync();
            }
        }
        catch (IOException e) {
            Msg.log( "Media player exception on prepare - " + muUrl);
            e.printStackTrace();
        }
     }
     public void muPlay() {   // cycling through play
        //displayViews( STARTED );
        if( muPlayer != null && muPlayer.isPlaying() ) {
            displayViews( STARTED );
            muRunnable = new Runnable() {
                @Override
                public void run() {
                    muPlay();
                }
            };
            muHandler.postDelayed( muRunnable, 1000);
        }
    }
    void muEnd() {
        if( muPlayer != null && (
            muState  == PREPARED ||
            muState  == STARTED  ||
            muState  == PAUSED   ||
            muState  == PLAYED   ) ) {
            muPlayer.seekTo(0);
            muStop();
        }
    }
    public void muStop() {
        if( muPlayer != null && (
            muState  == PREPARED ||
            muState  == STARTED  ||
            muState  == STOPPED  ||
            muState  == PAUSED   ||
            muState  == PLAYED  ) ) {
            muState = STOPPED;
            displayViews(STOPPED); // before stop
            muPlayer.stop();
        }
    }
    protected void displayProgress(){
        if( muPlayer != null ) {
            int duration = muPlayer.getDuration() / 1000; // In milliseconds
            int due = (muPlayer.getDuration() -
                    muPlayer.getCurrentPosition())/ 1000;
            int pass = duration - due;
            timeCurrent.setText( String.format(Locale.US, "%d sec", pass) );
            timeFinal.setText(   String.format(Locale.US, "%d sec", duration) );
         }
        else {
            timeCurrent.setText( " " );
            timeFinal.setText(   " " );
        }
    }
    private void displayViews( int state ) {
        switch (state) {
            case PREPARED:
                muName.setText( readSongName() );
                progBar.setMax(muPlayer.getDuration() / 1000);
                muToggle.setBackgroundColor(
                        getResources().getColor(R.color.colorPauseYellow));
                break;
            case STARTED:
                finalTime = muPlayer.getDuration();
                currentTime = muPlayer.getCurrentPosition();
                progBar.setMax((int) finalTime );
                progBar.setProgress((int) currentTime );
                timeCurrent.setText(String.format(Locale.US, "%d sec",
                        TimeUnit.MILLISECONDS.toSeconds((long) currentTime)));
                timeFinal.setText(String.format(Locale.US, "%d sec",
                        TimeUnit.MILLISECONDS.toSeconds((long) finalTime)));
                muToggle.setBackgroundColor(
                        getResources().getColor(R.color.colorPauseYellow));
                displayProgress();
                break;
            case PAUSED:
                muToggle.setBackgroundColor(
                        getResources().getColor(R.color.colorPlayGreen));
                break;
            case STOPPED:
            case INITIAL:
                progBar.setProgress( 0 );
                timeCurrent.setText(" ");
                timeFinal.setText(" ");
                // TODO: remove song name only if MIDI file is not there
                muName.setText(" ");
                muToggle.setBackgroundColor(
                        getResources().getColor(R.color.colorPlayGreen));
                break;
        }
    }                                   // displayViews()
    @Override
    public void onResume() {
        super.onResume();
        super.onPause();
        muPause();
     }
    @Override
    public void onPause() {
        super.onPause();
        muPause();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if( muPlayer != null ) {
            muPlayer.release();
            muPlayer = null;
        }
        if( muHandler != null ) {
            muHandler.removeCallbacks(muRunnable);
        }
        muState = DESTROYED;
    }
    //
    // Dialogue to create Song
    //
    AlertDialog muCreateWM;
    void diagCreateWM( String title ) {
        AlertDialog.Builder muBuildWM = new AlertDialog.Builder(this);
        muBuildWM.setTitle( title )
                .setItems( R.array.muCreatWM,
                        new DialogInterface.OnClickListener() {
                    public void onClick( DialogInterface dialog, int which ){
                        switch( which ) {
                            case 0:      // Create and Play
                                createWM( true );
                                break;   // Create now. Play later.
                            case 1:
                                createWM( false );
                                break;   // Cancel
                            case 2:
                                break;
                        }
                        muCreateWM.dismiss();
                    }
                });
        muCreateWM = muBuildWM.create();
        muCreateWM.show();
    }
    //
    // Dialog to save song
    //
    String dstSongName;
    String dstMidiName;
    void diagSaveWM() {
        // Create input field with default file name
        dstSongName = readSongName();
        dstMidiName = readMidiName();
        final EditText inputSaveSong = new EditText( muContext );
        AlertDialog.Builder diagSave = new AlertDialog.Builder(this);
        diagSave.setTitle( "Save song " + dstSongName );
        diagSave.setMessage("Save MIDI with the following file name, or edit:");
        diagSave.setView(inputSaveSong);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT );
        inputSaveSong.setLayoutParams( lp );
        inputSaveSong.setText(  dstMidiName );
        diagSave.setPositiveButton(
            "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialo, int whichButton) {
                        String userInputFileName = inputSaveSong.getText().toString();
                        saveMidi( dstSongName, userInputFileName );
                    }
                });
        diagSave.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Msg.show("File not saved");
            }
        });
        diagSave.show();
    }
    //
    // Options Menu
    //
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu );
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_midi:
                // Check for write permission before saving MIDI
                requestExternalStorage(REQUEST_MIDI_STORAGE);
                return true;
             default:
                return super.onOptionsItemSelected(item);
        }
    }
    //
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public void requestExternalStorage( int reason ) {
        Msg.log( "requestExternalStorage()");
        if (ContextCompat.checkSelfPermission( muActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?'
            Msg.log( "Answered!");
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    muActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Msg.show( "If you want to save MIDI files to SD card. You " +
                "will need to go to settings for this app to grant permission" );
            } else {
                // Request permission
                ActivityCompat.requestPermissions(
//                  muActivity, PERMISSIONS_STORAGE, reason );
                    muActivity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    reason );
                // Callback method onRequestPermissionsResult() gets the
                // result of the request.
            }
        }
        else {
            // Persmission already granted
            switch (reason) {
                case REQUEST_QUICK_STORAGE:
                    String qsSongName = readSongName();
                    String qsMidiName = readMidiName();
                    saveMidi(qsSongName, qsMidiName);
                    break;
                case REQUEST_MIDI_STORAGE:
                    diagSaveWM();
                    break;
            }
        }
    }                               // requestExternalStorage()
    @Override
    public void onRequestPermissionsResult(int requestCode,
                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        switch (requestCode) {
            case REQUEST_QUICK_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String qsSongName = readSongName();
                    String qsMidiName = readMidiName();
                    saveMidi( qsSongName, qsMidiName );
                } else if ( Build.VERSION.SDK_INT >= 23 &&
                        !shouldShowRequestPermissionRationale(permissions[0])) {
                    Msg.show( "Go to settings for this app and grant " +
                            " permission to save MIDI files to the SD card" );
                } else {
                    Msg.show("Permission not granted to save MIDI files");
                }
                break;
            case REQUEST_MIDI_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    diagSaveWM();
                } else if ( Build.VERSION.SDK_INT >= 23 &&
                        !shouldShowRequestPermissionRationale(permissions[0])) {
                    Msg.show( "Go to settings for this app and grant " +
                            " permission to save MIDI files to the SD card" );
                } else {
                    Msg.show("Permission not granted to save MIDI files");
                }
                break;
        }
        return;
    }
    //
    // Utilities
    //
    // . . .
    //
    // Native library functions
    //
    // Method to run muWave engine
    //
    public native int muWave(String command);
    //
    // Get MIDI file suffix (time in hexadeciomal)
    public native String hexTime();
}

