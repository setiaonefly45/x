
package com.azis.mikrotikmonitor;

import android.app.*; import android.os.*; import android.widget.*; import java.util.*; import org.json.*;

public class NewUserActivity extends Activity {
    RouterApi api; EditText name, pass; Spinner profile; TextView status; ArrayList<String> profiles=new ArrayList<>();
    public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_new_user);
        api=new RouterApi(new RouterConfig(this)); name=findViewById(R.id.name); pass=findViewById(R.id.password);
        profile=findViewById(R.id.profile);status=findViewById(R.id.status);
        loadProfiles(); findViewById(R.id.create).setOnClickListener(v->create());
    }
    void loadProfiles(){new Thread(()->{try{JSONArray a=api.profiles();for(int i=0;i<a.length();i++)profiles.add(a.getJSONObject(i).optString("name","default"));if(profiles.isEmpty())profiles.add("default");
        runOnUiThread(()->profile.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,profiles)));
        }catch(Exception e){profiles.add("default");runOnUiThread(()->profile.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,profiles)));}}).start();}
    void create(){String n=name.getText().toString().trim();String pw=pass.getText().toString();if(pw.isEmpty())pw=n;String pr=profile.getSelectedItem()==null?"default":profile.getSelectedItem().toString();
        if(n.isEmpty()){status.setText("Nama user wajib diisi");return;} status.setText("Membuat...");
        final String fpw=pw, fpr=pr;new Thread(()->{try{api.createSecret(n,fpw,fpr);runOnUiThread(()->{status.setText("✓ User "+n+" berhasil dibuat");name.setText("");pass.setText("");});}
        catch(Exception e){runOnUiThread(()->status.setText("Gagal: "+e.getMessage()));}}).start();}
}
