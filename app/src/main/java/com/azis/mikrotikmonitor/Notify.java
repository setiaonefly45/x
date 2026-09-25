
package com.azis.mikrotikmonitor;

import android.app.*;
import android.content.*;
import android.os.Build;

public class Notify {
    public static final String CHANNEL="user_status";
    public static void channel(Context c){
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel ch=new NotificationChannel(CHANNEL,"PPPoE User Status",NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Status online/offline PPPoE");
            ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }
    public static void send(Context c,String title,String text,int id){
        channel(c);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,CHANNEL):new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.stat_notify_sync).setContentTitle(title).setContentText(text).setAutoCancel(true);
        ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(id,b.build());
    }
}
