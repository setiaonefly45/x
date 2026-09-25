
package com.azis.mikrotikmonitor;
import android.content.*;
public class UiPrefs {
 public static boolean dark(Context c){return c.getSharedPreferences("ui",0).getBoolean("dark",false);}
 public static void setDark(Context c,boolean v){c.getSharedPreferences("ui",0).edit().putBoolean("dark",v).apply();}
}
