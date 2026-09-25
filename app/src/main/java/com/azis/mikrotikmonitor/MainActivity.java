package com.azis.mikrotikmonitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    RouterConfig cfg;
    RouterApi api;
    Handler h = new Handler(Looper.getMainLooper());

    TextView online, offline, disabled, total, cpu, memory, uptime, connection,
            routerName, isp1, isp2, rxText, txText, chartText;
    Switch dark;

    long lastRx = -1, lastTx = -1, lastT = -1;
    ArrayList<Double> rh = new ArrayList<>();
    Runnable poll;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        cfg = new RouterConfig(this);
        api = new RouterApi(cfg);

        poll = new Runnable() {
            @Override
            public void run() {
                refresh();
                h.postDelayed(this, 5000);
            }
        };

        online = findViewById(R.id.online);
        offline = findViewById(R.id.offline);
        disabled = findViewById(R.id.disabled);
        total = findViewById(R.id.total);
        cpu = findViewById(R.id.cpu);
        memory = findViewById(R.id.memory);
        uptime = findViewById(R.id.uptime);
        connection = findViewById(R.id.connection);
        routerName = findViewById(R.id.routerName);
        isp1 = findViewById(R.id.isp1);
        isp2 = findViewById(R.id.isp2);
        rxText = findViewById(R.id.rxText);
        txText = findViewById(R.id.txText);
        chartText = findViewById(R.id.chartText);
        dark = findViewById(R.id.darkSwitch);

        dark.setChecked(UiPrefs.dark(this));
        dark.setOnCheckedChangeListener((v, c) -> {
            UiPrefs.setDark(this, c);
            Toast.makeText(this, c ? "Dark mode ON" : "Dark mode OFF", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.settingsBtn).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        findViewById(R.id.usersBtn).setOnClickListener(v ->
                startActivity(new Intent(this, UsersActivity.class)));
        findViewById(R.id.newBtn).setOnClickListener(v ->
                startActivity(new Intent(this, NewUserActivity.class)));
        findViewById(R.id.profilesBtn).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilesActivity.class)));
        findViewById(R.id.ispOn).setOnClickListener(v -> setIsp(false));
        findViewById(R.id.ispOff).setOnClickListener(v -> setIsp(true));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cfg = new RouterConfig(this);
        api = new RouterApi(cfg);
        h.removeCallbacks(poll);
        poll.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        h.removeCallbacks(poll);
    }

    void refresh() {
        if (!cfg.valid()) {
            connection.setText("● SETUP");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject r = api.resource();
                JSONArray s = api.secrets();
                JSONArray a = api.active();

                int dis = 0;
                for (int i = 0; i < s.length(); i++) {
                    if ("yes".equalsIgnoreCase(
                            s.getJSONObject(i).optString("disabled"))) {
                        dis++;
                    }
                }

                final int fDis = dis;
                final int fOn = a.length();
                final int fTot = s.length();
                final int fOff = Math.max(0, fTot - fDis - fOn);
                final String fVersion = r.optString("version", "7.x");
                final String fCpu = r.optString("cpu-load", "0");
                final String fMemory = r.optString("free-memory", "0");
                final String fUptime = r.optString("uptime", "-");

                runOnUiThread(() -> {
                    connection.setText("● ONLINE");
                    routerName.setText("RouterOS " + fVersion);
                    online.setText("" + fOn);
                    offline.setText("" + fOff);
                    disabled.setText("" + fDis);
                    total.setText("" + fTot);
                    cpu.setText("CPU\n" + fCpu + "%");
                    memory.setText("RAM\n" + fMemory);
                    uptime.setText("UPTIME\n" + fUptime);
                });

                loadInterfaces();
            } catch (Exception e) {
                runOnUiThread(() -> connection.setText("● OFFLINE"));
            }
        }).start();
    }

    void loadInterfaces() {
        new Thread(() -> {
            try {
                JSONObject x = api.interfaceByName("MCN-vlan721");
                JSONObject y = api.interfaceByName("Backup-vlan313");

                long rx = lng(x.optString("rx-byte", "0"));
                long tx = lng(x.optString("tx-byte", "0"));
                long now = System.currentTimeMillis();

                double rb = 0;
                double tb = 0;
                if (lastT > 0) {
                    double sec = (now - lastT) / 1000.0;
                    if (sec > 0) {
                        rb = Math.max(0, (rx - lastRx) * 8 / sec);
                        tb = Math.max(0, (tx - lastTx) * 8 / sec);
                    }
                }

                lastRx = rx;
                lastTx = tx;
                lastT = now;

                rh.add(rb / 1e6);
                if (rh.size() > 12) rh.remove(0);

                StringBuilder bars = new StringBuilder();
                for (double v : rh) {
                    int n = (int) Math.max(1, Math.min(9, v / 10));
                    bars.append("▁▂▃▄▅▆▇█".charAt(Math.min(7, n - 1)));
                }

                final JSONObject fx = x;
                final JSONObject fy = y;
                final double frb = rb;
                final double ftb = tb;
                final String fBars = bars.toString();

                runOnUiThread(() -> {
                    isp1.setText("● MCN-vlan721  " + (isDisabled(fx) ? "DISABLED" : "UP"));
                    isp2.setText("● Backup-vlan313  " + (isDisabled(fy) ? "DISABLED" : "UP"));
                    rxText.setText(String.format(Locale.US, "↓ %.1f Mbps", frb / 1e6));
                    txText.setText(String.format(Locale.US, "↑ %.1f Mbps", ftb / 1e6));
                    chartText.setText(fBars.length() == 0 ? "▁" : fBars);
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    boolean isDisabled(JSONObject x) {
        return "true".equalsIgnoreCase(x.optString("disabled", "false"))
                || "yes".equalsIgnoreCase(x.optString("disabled", "no"));
    }

    long lng(String s) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return 0;
        }
    }

    void setIsp(boolean off) {
        new Thread(() -> {
            try {
                api.setInterface("MCN-vlan721", off);
                runOnUiThread(() -> Toast.makeText(
                        this, off ? "ISP1 OFF" : "ISP1 ON", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
