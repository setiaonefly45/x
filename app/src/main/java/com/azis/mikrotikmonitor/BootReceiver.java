
package com.azis.mikrotikmonitor;

import android.content.*;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    public void onReceive(Context c,Intent i){
        if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()) && MonitorPrefs.enabled(c))
            MonitorScheduler.schedule(c);
    }
}
