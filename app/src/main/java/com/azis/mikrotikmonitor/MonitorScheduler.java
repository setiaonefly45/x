
package com.azis.mikrotikmonitor;

import android.content.Context;
import androidx.work.*;
import java.util.concurrent.TimeUnit;

public class MonitorScheduler {
    public static void schedule(Context c){
        int min=Math.max(15,MonitorPrefs.interval(c));
        PeriodicWorkRequest r=new PeriodicWorkRequest.Builder(StatusWorker.class,min,TimeUnit.MINUTES)
            .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build();
        WorkManager.getInstance(c).enqueueUniquePeriodicWork("pppoe_status",ExistingPeriodicWorkPolicy.UPDATE,r);
    }
    public static void stop(Context c){WorkManager.getInstance(c).cancelUniqueWork("pppoe_status");}
}
