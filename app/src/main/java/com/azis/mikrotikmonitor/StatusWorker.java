
package com.azis.mikrotikmonitor;

import android.content.*;
import androidx.annotation.NonNull;
import androidx.work.*;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;

public class StatusWorker extends Worker {
    public StatusWorker(@NonNull Context c,@NonNull WorkerParameters p){super(c,p);}
    @NonNull public Result doWork(){
        Context c=getApplicationContext();
        if(!MonitorPrefs.enabled(c)) return Result.success();
        RouterConfig cfg=new RouterConfig(c); if(!cfg.valid()) return Result.success();
        try{
            RouterApi api=new RouterApi(cfg); JSONArray sec=api.secrets(), act=api.active();
            HashSet<String> online=new HashSet<>();
            for(int i=0;i<act.length();i++) online.add(act.getJSONObject(i).optString("name"));
            SharedPreferences sp=c.getSharedPreferences("status_snapshot",0);
            boolean baseline=sp.getBoolean("baseline",false);
            SharedPreferences.Editor ed=sp.edit();
            int id=1000;
            for(int i=0;i<sec.length();i++){
                JSONObject x=sec.getJSONObject(i); String n=x.optString("name"); boolean disabled="yes".equalsIgnoreCase(x.optString("disabled"));
                boolean now=!disabled && online.contains(n); String key="u_"+n; boolean old=sp.getBoolean(key,now);
                if(baseline && old!=now){
                    Notify.send(c, now?"PPPoE ONLINE":"PPPoE OFFLINE", n+(now?" sudah online":" sudah offline"),id++);
                }
                ed.putBoolean(key,now);
            }
            ed.putBoolean("baseline",true).apply();
            return Result.success();
        }catch(Exception e){return Result.retry();}
    }
}
