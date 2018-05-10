package com.wavematters.muwave;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FileUtil {

    public static int copyStream(InputStream in, OutputStream out ) {
        try {
            int bytesum = 0;
            int byteread = 0;
            byte[] buffer = new byte[1444];
            while ((byteread = in.read(buffer)) != -1) {
                bytesum += byteread;
                out.write(buffer, 0, byteread);
            }
            in.close();
            out.flush();
            out.close();
            return bytesum;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public static boolean copyFileStream(String from, String to) {
        try {
            int bytes = 0;
            File oldfile = new File(from);
            if( oldfile.exists()) {
                InputStream inStream = new FileInputStream(from);
                Message.log("Source file " + from + " open");
                Message.log( "destion = " + to );
                OutputStream outStream = new FileOutputStream(to);
                Message.log("Dst file " + to + " opened");
                bytes = copyStream(inStream, outStream);
                Message.log("Bytes copied = " + bytes);
            }
            else {
                Message.log( "Source " + from + " does not exist" );
            }
        } catch (SecurityException e) {
            Message.log( "Access denied to " + to );
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Message.log( "Copy error from " + from + " to " + to );
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static int copyFileText( InputStream is, OutputStream os ) {
        int linecount = 0;
        try {
            InputStreamReader inreader = new InputStreamReader(is);
            OutputStreamWriter writer = new OutputStreamWriter(os);
            BufferedReader reader = new BufferedReader(inreader);
            String line;
            while( (line = reader.readLine() ) != null) {
                writer.write(line + "\n");
                linecount += 1;
            }
            writer.flush();
            writer.close();
            reader.close();
        } catch (Exception e) {
            Message.log("Error in copying text file");
            e.printStackTrace();
            return (0);
        }
        return( linecount );
    }
    //
    public static Boolean createDirectory( File fp ) {
        if (!fp.exists()) {
            if( !fp.mkdir() ) {
                Message.log( "Failed to create directory");
                return false;
            }
            return true;
        } else {
            Message.log( "Directory alreay exists");
            return true;
        }
    }
    public Boolean fileExists( String filePath ) {
        File fileDesc = new File( filePath );
        if (fileDesc.exists()) {
            Message.log(filePath + " exists" );
            return true;
        } else {
            Message.log( filePath + " does NOT exist");
            return false;
        }
    }
}
