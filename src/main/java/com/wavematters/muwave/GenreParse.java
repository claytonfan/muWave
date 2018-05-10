package com.wavematters.muwave;

import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class GenreParse {

    List<Genre> genres;

    private Genre  genre;
    private String text;

    public GenreParse() {
        genres = new ArrayList<Genre>();
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Genre> parse(InputStream is) {

        XmlPullParserFactory factory = null;
        XmlPullParser        parser  = null;

        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            parser.setInput( is, null);
            int eventType = parser.getEventType();
            while( eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                //Log.i( "WM", "Tag  = " + tagname  );  //
                switch( eventType ) {
                    case XmlPullParser.START_TAG:
                        if(tagname.equalsIgnoreCase("genre")) {
                            genre = new Genre();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        //Log.i( "WM", "Text = " + text  );  //
                        break;
                    case XmlPullParser.END_TAG:
                        if(tagname.equalsIgnoreCase("genre")) {
                            genres.add(genre);
                        }
                        else if(tagname.equalsIgnoreCase("name")) {
                            genre.setName(text);
                        }
                        else if(tagname.equalsIgnoreCase("desc")) {
                            genre.setDesc(text);
                        }
                        else if(tagname.equalsIgnoreCase("spec")) {
                            genre.setSpec(text);
                        }
                        else if(tagname.equalsIgnoreCase("cmd")) {
                            genre.setCmd(text);
                        }
                        else if(tagname.equalsIgnoreCase("prefix")) {
                            genre.setPrefix(text);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        return genres;
    }
}
