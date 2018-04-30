package com.wavematters.muwave;

import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WM";
    Integer songLen = 30;    // song duration in seconds
    List<Genre> genres = null;
    ListView genreList;
    String songName = "";

    private muPlayer muPlay;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //
        // Activity
        // Present a list of musical genres which allows the user to select.
        // Then issue command to generate music in MIDI format.
        // Allows use to playback.
        //
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        //
        // Create media play and genre list
        //
        muPlay = new muPlayer();
        genreList = (ListView) findViewById(R.id.genreList);
        try {
            GenreParse parser = new GenreParse();
            genres = parser.parse(getAssets().open("genres.xml"));
            for (int i = 0; i < genres.size(); i++) {
                Log.i(TAG, genres.get(i).getName() +
                        " - " + genres.get(i).getDesc() +
                        " - " + genres.get(i).getSpec() +
                        " - " + genres.get(i).getCmd());
            }
            ArrayAdapter<Genre> adapter = new ArrayAdapter<Genre>(
                    this, R.layout.list_item, R.id.itemText, genres);
            genreList.setAdapter(adapter);
        } catch (IOException e) {
            Log.i(TAG, "Error. Specification not fetched.");
            e.printStackTrace();
        }
        //
        // Wait for user to make selection
        // TODO: Move listener to a new package, adding new genre list methods
        //
        genreList.setClickable(true);
        genreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //
                // TODO: Get directories from context
                //
                String url = "file:///data/data/com.wavematters.muwave/wm.mid";
                Log.i(TAG, "position clicked = " + position);
                Object ItemObj = genreList.getItemAtPosition(position);

                songName = genres.get(position).getName();
                String muSpec = genres.get(position).getSpec();
                String fpath = "/data/data/com.wavematters.muwave/" + muSpec;
                // Context.getFilesDir().getPath();
                File fspec = new File(fpath);

                Log.i(TAG, "Command = " + genres.get(position).getCmd() + " Spec = " + muSpec);

                if (fspec.delete()) {
                    Log.i(TAG, "Spec File " + fpath + " deleted");
                } else {
                    Log.i(TAG, "Spec File " + fpath + " NOT deleted");
                }
                if (muSpec != null && !muSpec.equals("none")) {
                    try {
                        if (copySpec(muSpec) <= 0) {
                            Log.i(TAG, fpath + " not copied");
                        } else {
                            String command = "wm -i " + fpath + " -t " + songLen
                                    + " -o /data/data/com.wavematters.muwave/wm.mid";
                            //
                            // TODO: Pop up dialogoe box for 3 choice
                            //   -- Play after music is generated
                            //   -- Do not Play, or
                            //   -- Cancel
                            //
                            int muStat = muWave(command);
                            switch (muStat) {
                                case 0:
                                    Log.i(TAG, "muWave normal exit");
                                    //
                                    // Future: Instead of saving song name.
                                    // Read song name from MIDI file when needed
                                    //
                                    writeName(songName);
                                    // Prepare and then play music
                                    muPlay.prepare();
                                    break;
                                case 1:
                                    Log.i(TAG,
                                      "Program Error. Syntax error muWave spec.");
                                    break;
                                default:
                                    Log.i(TAG, "Error. muWave error exit");
                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "Exception raised on click to start");
                        e.printStackTrace();
                    }
                }
             }
        });
    }

    private void writeName(String name) throws IOException {
        // Overwrite old file
        // Temporary method to store song name
        // TODO: read name from MIDI file
        OutputStream os = new FileOutputStream(
                "/data/data/com.wavematters.muwave/wmnamecq.txt");
        Log.i(TAG, "Name File name.txt created");
        OutputStreamWriter writer = new OutputStreamWriter(os);
        writer.write(name + "\n");
        writer.flush();
        writer.close();
    }
    private Integer copySpec(String fileSpec) {
        //
        // Copy specification from asset directory to
        // program file directory for reading
        //
        InputStream is;
        OutputStream os;
        try {
            AssetManager specFiles = getAssets();
            is = specFiles.open(fileSpec);
            os = new FileOutputStream("/data/data/com.wavematters.muwave/" + fileSpec);
            InputStreamReader inreader = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(inreader);
            OutputStreamWriter writer = new OutputStreamWriter(os);
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return (0);
        }
        Log.i(TAG, "File " + fileSpec + " copied");
        return (1);
    }
    public native int muWave(String command);
}
