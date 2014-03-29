package com.amusementlabs.whatsthescore.util;

import android.util.Log;

/*Logger class. Wraps googles logcat interface to reduce params, and allows app wide editing of
 * logger behavior.
 */

public class Logr {

    private static final String APPLICATION_NAME = "WhatsTheScore";

    private static final String LN_EXCEPTION_MESSAGE = "Exception caught:";

    public Logr() {} //ctor

    public static void v(String message) {
        Log.v(APPLICATION_NAME, message);
    }

    public static void d(String message) {
        Log.d(APPLICATION_NAME, message);
    }

    public static void i(String message) {
        Log.i(APPLICATION_NAME, message);
    }

    public static void w(String message) {
        Log.w(APPLICATION_NAME, message);
    }

    public static void e(String message) {
        Log.e(APPLICATION_NAME, message);
    }

    public static void x(Throwable e) {  //exception handler
        Log.e(APPLICATION_NAME, LN_EXCEPTION_MESSAGE, e);
    }

}
