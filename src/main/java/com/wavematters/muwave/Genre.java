package com.wavematters.muwave;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.IntStream;

public class Genre {

    private String name;
    private String desc;
    private String spec;
    private String cmd;
    private String prefix;

    public String getName()   { return name;   }
    public String getDesc()   { return desc;   }
    public String getSpec()   { return spec;   }
    public String getCmd()    { return cmd;    }
    public String getPrefix() { return prefix; }
    public void setName(   String   name ) { this.name   = name;   }
    public void setDesc(   String   desc ) { this.desc   = desc;   }
    public void setSpec(   String   spec ) { this.spec   = spec;   }
    public void setCmd(    String    cmd ) { this.cmd    = cmd;    }
    public void setPrefix( String prefix ) {
        this.prefix = prefix;
    }

    public int length() {
        return cmd.length();
    }

    public boolean isEmpty() {
        return cmd.isEmpty();
    }

    public char charAt(int i) {
        return cmd.charAt(i);
    }

    public int codePointAt(int index) {
        return cmd.codePointAt(index);
    }

    public int codePointBefore(int index) {
        return cmd.codePointBefore(index);
    }

    public int codePointCount(int beginIndex, int endIndex) {
        return cmd.codePointCount(beginIndex, endIndex);
    }

    public int offsetByCodePoints(int index, int codePointOffset) {
        return cmd.offsetByCodePoints(index, codePointOffset);
    }

    public void getChars(int srcBegin, int srcEnd, @NonNull char[] dst, int dstBegin) {
        cmd.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

     public boolean contentEquals(@NonNull StringBuffer sb) {
        return cmd.contentEquals(sb);
    }

    public boolean contentEquals(@NonNull CharSequence cs) {
        return cmd.contentEquals(cs);
    }

    public boolean equalsIgnoreCase(String anotherString) {
        return cmd.equalsIgnoreCase(anotherString);
    }

    public int compareTo(@NonNull String s) {
        return cmd.compareTo(s);
    }

    public int compareToIgnoreCase(String str) {
        return cmd.compareToIgnoreCase(str);
    }

     public boolean startsWith(@NonNull String prefix, int toffset) {
        return cmd.startsWith(prefix, toffset);
    }

    public boolean startsWith(@NonNull String prefix) {
        return cmd.startsWith(prefix);
    }

    public boolean endsWith(@NonNull String suffix) {
        return cmd.endsWith(suffix);
    }


    public String substring(int beginIndex) {
        return cmd.substring(beginIndex);
    }

    public String substring(int beginIndex, int endIndex) {
        return cmd.substring(beginIndex, endIndex);
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        return cmd.subSequence(beginIndex, endIndex);
    }

    public String concat(@NonNull String s) {
        return cmd.concat(s);
    }

    public String replace(char oldChar, char newChar) {
        return cmd.replace(oldChar, newChar);
    }

    public boolean matches(@NonNull String regex) {
        return cmd.matches(regex);
    }

    public boolean contains(@NonNull CharSequence s) {
        return cmd.contains(s);
    }

    public String[] split(@NonNull String regex, int limit) {
        return cmd.split(regex, limit);
    }

    public String[] split(@NonNull String regex) {
        return cmd.split(regex);
    }

    public String toLowerCase(@NonNull Locale locale) {
        return cmd.toLowerCase(locale);
    }

    public String toLowerCase() {
        return cmd.toLowerCase();
    }

    public String toUpperCase(@NonNull Locale locale) {
        return cmd.toUpperCase(locale);
    }

    public String toUpperCase() {
        return cmd.toUpperCase();
    }

    public String trim() {
        return cmd.trim();
    }

    public char[] toCharArray() {
        return cmd.toCharArray();
    }

    public static String format(@NonNull String format, Object... args) {
        return String.format(format, args);
    }
    public static String format(Locale l, @NonNull String format, Object... args) {
        return String.format(l, format, args);
    }
    public static String valueOf(Object obj) {
        return String.valueOf(obj);
    }

    public static String valueOf(@NonNull char[] data) {
        return String.valueOf(data);
    }

    public static String valueOf(@NonNull char[] data, int offset, int count) {
        return String.valueOf(data, offset, count);
    }
    public static String copyValueOf(@NonNull char[] data, int offset, int count) {
        return String.copyValueOf(data, offset, count);
    }
    public static String copyValueOf(@NonNull char[] data) {
        return String.copyValueOf(data);
    }
    public static String valueOf(boolean b) {
        return String.valueOf(b);
    }

    public static String valueOf(char c) {
        return String.valueOf(c);
    }

    public static String valueOf(int i) {
        return String.valueOf(i);
    }

    public static String valueOf(long l) {
        return String.valueOf(l);
    }

    public static String valueOf(float f) {
        return String.valueOf(f);
    }

    public static String valueOf(double d) {
        return String.valueOf(d);
    }

    @Override
    public String toString() { return name + " -- " + desc;  }
}
