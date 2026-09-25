package com.azis.mikrotikmonitor;

import android.content.Context;
import android.content.SharedPreferences;

public class RouterConfig {
    private static final String P = "router";
    public String host, port, user, pass;
    public boolean insecure;

    public RouterConfig(Context c) {
        SharedPreferences p = c.getSharedPreferences(P, Context.MODE_PRIVATE);
        host = p.getString("host", "");
        port = p.getString("port", "443");
        user = p.getString("user", "");
        pass = p.getString("pass", "");
        insecure = p.getBoolean("insecure", false);
    }

    public void save(Context c) {
        c.getSharedPreferences(P, Context.MODE_PRIVATE).edit()
            .putString("host", host).putString("port", port)
            .putString("user", user).putString("pass", pass)
            .putBoolean("insecure", insecure).apply();
    }

    public boolean valid() { return !host.isEmpty() && !user.isEmpty(); }

    public String baseUrl() {
        String h = host.startsWith("http://") || host.startsWith("https://") ? host : "https://" + host;
        if (!h.matches("https?://[^/:]+:\\d+.*")) h += ":" + (port.isEmpty() ? "443" : port);
        return h.replaceAll("/+$", "") + "/rest";
    }
}
