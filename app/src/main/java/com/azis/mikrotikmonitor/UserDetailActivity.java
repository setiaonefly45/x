
package com.azis.mikrotikmonitor;
import android.app.*; import android.os.*; import android.widget.*; import org.json.*; import java.util.*;
public class UserDetailActivity extends Activity {
 RouterApi api; String name; TextView title,info;
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_detail);name=getIntent().getStringExtra("name");api=new RouterApi(new RouterConfig(this));title=findViewById(R.id.name);info=findViewById(R.id.info);title.setText(name);
 findViewById(R.id.kick).setOnClickListener(v->act(1));findViewById(R.id.off).setOnClickListener(v->act(2));findViewById(R.id.on).setOnClickListener(v->act(3));load();}
 void load(){new Thread(()->{try{JSONArray s=api.get("ppp/secret?name="+java.net.URLEncoder.encode(name,"UTF-8"));JSONArray a=api.activeByName(name);JSONObject x=s.length()>0?s.getJSONObject(0):new JSONObject();JSONObject y=a.length()>0?a.getJSONObject(0):new JSONObject();String text="Status: "+(a.length()>0?"ONLINE":"OFFLINE")+"\nIP Address: "+y.optString("address","-")+"\nCaller ID: "+y.optString("caller-id","-")+"\nUptime: "+y.optString("uptime","-")+"\nProfile: "+x.optString("profile","default")+"\nService: "+x.optString("service","pppoe")+"\nDisabled: "+x.optString("disabled","no")+"\nSession RX: "+y.optString("bytes-in","0")+"\nSession TX: "+y.optString("bytes-out","0");runOnUiThread(()->info.setText(text));}catch(Exception e){runOnUiThread(()->info.setText(e.getMessage()));}}).start();}
 void act(int a){new Thread(()->{try{if(a==1)api.kick(name);if(a==2)api.setSecretDisabled(name,true);if(a==3)api.setSecretDisabled(name,false);runOnUiThread(()->{Toast.makeText(this,"Berhasil",Toast.LENGTH_SHORT).show();load();});}catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}
}
