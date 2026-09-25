
package com.azis.mikrotikmonitor;
import android.app.*; import android.os.*; import android.graphics.Color; import android.widget.*; import java.util.*;
public class SettingsActivity extends Activity {
 EditText host,port,user,pass;CheckBox insecure;Switch notifySwitch;Spinner interval;TextView status;RouterConfig cfg;
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_settings);cfg=new RouterConfig(this);
  host=findViewById(R.id.host);port=findViewById(R.id.port);user=findViewById(R.id.user);pass=findViewById(R.id.pass);insecure=findViewById(R.id.insecure);notifySwitch=findViewById(R.id.notifySwitch);interval=findViewById(R.id.interval);status=findViewById(R.id.status);
  host.setText(cfg.host);port.setText(cfg.port);user.setText(cfg.user);pass.setText(cfg.pass);insecure.setChecked(cfg.insecure);notifySwitch.setChecked(MonitorPrefs.enabled(this));
  String[] vals={"15 menit","30 menit","60 menit"};interval.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,vals));int idx=MonitorPrefs.interval(this)==60?2:MonitorPrefs.interval(this)==30?1:0;interval.setSelection(idx);
  findViewById(R.id.test).setOnClickListener(v->test(false));findViewById(R.id.save).setOnClickListener(v->test(true));
 }
 void fill(){cfg.host=host.getText().toString().trim();cfg.port=port.getText().toString().trim();cfg.user=user.getText().toString().trim();cfg.pass=pass.getText().toString();cfg.insecure=insecure.isChecked();}
 void test(boolean save){fill();status.setText("Menghubungkan...");new Thread(()->{try{new RouterApi(cfg).test();if(save){cfg.save(this);int m=interval.getSelectedItemPosition()==2?60:interval.getSelectedItemPosition()==1?30:15;MonitorPrefs.save(this,notifySwitch.isChecked(),m);if(notifySwitch.isChecked())MonitorScheduler.schedule(this);else MonitorScheduler.stop(this);}
   runOnUiThread(()->status.setText("✓ Terhubung"));if(save)runOnUiThread(this::finish);}catch(Exception e){runOnUiThread(()->{status.setTextColor(Color.RED);status.setText("Gagal: "+e.getMessage());});}}).start();}
}
