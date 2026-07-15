package kr.co.kworks.goodmorning.utils;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

import kr.co.kworks.goodmorning.BuildConfig;

public class Logger {
    private static Logger instance;

    private Logger() {
    }


    public static Logger getInstance() {
        if(instance == null) instance = new Logger();
        return instance;
    }

    public void info(String tag, String str) {

        if (BuildConfig.IS_PRODUCTION) {
            // operation
            Utils.get().writeLog(str, "");
        } else {
            // debug
            Log.i(tag, str);
        }
    }

    public void info(String str) {

        if (BuildConfig.IS_PRODUCTION) {
            // operation
            Utils.get().writeLog(str, "");
        } else {
            // debug
            Log.i("this", str);
        }
    }

    public void error(String str, Exception e) {
        if (BuildConfig.IS_PRODUCTION) {
            // operation
            StringWriter sw = new StringWriter();

            if(e != null) e.printStackTrace(new PrintWriter(sw));
            Utils.get().writeLog(str+": "+sw, "");
        } else {
            // debug
            Log.e("error", str, e);
        }
    }

    public void error(String str, Throwable e) {
        if (BuildConfig.IS_PRODUCTION) {
            // operation
            StringWriter sw = new StringWriter();

            if(e != null) e.printStackTrace(new PrintWriter(sw));
            Utils.get().writeLog(str+": "+sw, "");
        } else {
            // debug
            Log.e("error", str, e);
        }
    }

    public void error(String tag, String str, Exception e) {
        if (BuildConfig.IS_PRODUCTION) {
            // operation
            StringWriter sw = new StringWriter();

            if (e != null) e.printStackTrace(new PrintWriter(sw));
            Utils.get().writeLog(String.format(Locale.KOREA, "(%s) %s: %s", tag, str, sw), "");
        } else {
            // debug
            Log.e(tag, str, e);
        }
    }

    public void error(String tag, String str, Throwable t) {
        if (BuildConfig.IS_PRODUCTION) {
            // operation
            StringWriter sw = new StringWriter();

            if(t != null) t.printStackTrace(new PrintWriter(sw));
            Utils.get().writeLog(String.format(Locale.KOREA, "(%s) %s: %s", tag, str, sw), "");
        } else {
            // debug
            Log.e(tag, str, t);
        }
    }
}
