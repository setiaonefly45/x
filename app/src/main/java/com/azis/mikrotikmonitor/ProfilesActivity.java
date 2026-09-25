package com.azis.mikrotikmonitor;

import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import org.json.*;
import java.util.*;

public class ProfilesActivity extends Activity {
    RouterApi api;
    LinearLayout list;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_profiles);
        api = new RouterApi(new RouterConfig(this));
        list = findViewById(R.id.list);
        load();
    }

    void load() {
        new Thread(() -> {
            try {
                JSONArray p = api.profiles();
                JSONArray s = api.secrets();
                HashMap<String, Integer> count = new HashMap<>();

                for (int i = 0; i < s.length(); i++) {
                    String pr = s.getJSONObject(i).optString("profile", "default");
                    count.put(pr, count.getOrDefault(pr, 0) + 1);
                }

                runOnUiThread(() -> {
                    for (int i = 0; i < p.length(); i++) {
                        JSONObject x = p.optJSONObject(i);
                        if (x == null) continue;

                        String n = x.optString("name", "default");
                        LinearLayout c = new LinearLayout(this);
                        c.setOrientation(LinearLayout.VERTICAL);
                        c.setPadding(18, 14, 18, 14);
                        c.setBackgroundResource(R.drawable.bg_card);

                        TextView a = new TextView(this);
                        a.setText(n);
                        a.setTextSize(19);
                        a.setTypeface(null, 1);

                        TextView z = new TextView(this);
                        z.setText("Users: " + count.getOrDefault(n, 0)
                                + "   Rate: " + x.optString("rate-limit", "unlimited"));

                        c.addView(a);
                        c.addView(z);

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, 90);
                        lp.setMargins(4, 5, 4, 5);
                        list.addView(c, lp);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        this, e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
