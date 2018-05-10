package com.wavematters.muwave;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

public class Message {
    private static final String TAG = "WM";
    private Activity MessageActivity;

    public Message( Activity activity ) { // OnCreate?
        MessageActivity = activity;
    }
    public void show(  String msg ) {
        Toast.makeText( MessageActivity, msg, Toast.LENGTH_LONG).show();
    }
    public static void log( String msg ) {
        Log.i(TAG, msg );
    }
    public void broadcast( String msg ) {
        show( msg );
        log( msg );
    }
    public void distribute( String toast, String log ) {
        show( toast );
        log( log );
    }
}
