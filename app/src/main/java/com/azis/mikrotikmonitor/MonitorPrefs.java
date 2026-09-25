
package com.azis.mikrotikmonitor;

import android.content.Context;
import android.content.SharedPreferences;

public class MonitorPrefs {
    private static final String P="monitor";
    public static boolean enabled(Context c){return c.getSharedPreferences(P,0).getBoolean("notify",false);}
    public static int interval(Context c){return c.getSharedPreferences(P,0).getInt("interval",15);}
    public static void save(Context c, boolean e, int i){c.getSharedPreferences(P,0).edit().putBoolean("notify",e).putInt("interval",i).apply();}
}
