
package com.azis.mikrotikmonitor;
import android.app.*; import android.os.*; import android.content.*; import android.graphics.Color; import android.text.*; import android.view.*; import android.widget.*; import org.json.*; import java.util.*;
public class UsersActivity extends Activity {
 RouterApi api; LinearLayout list; EditText search; JSONArray secrets=new JSONArray(),active=new JSONArray(); int filter=0;
 public void onCreate(Bundle b){super.onCreate(b);setContentView(R.layout.activity_users);api=new RouterApi(new RouterConfig(this));list=findViewById(R.id.list);search=findViewById(R.id.search);
 search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int d){}public void onTextChanged(CharSequence s,int a,int b,int c){render(s.toString());}public void afterTextChanged(Editable e){}});
 findViewById(R.id.all).setOnClickListener(v->{filter=0;render(search.getText().toString());});findViewById(R.id.onlineOnly).setOnClickListener(v->{filter=1;render(search.getText().toString());});findViewById(R.id.offlineOnly).setOnClickListener(v->{filter=2;render(search.getText().toString());});findViewById(R.id.disabledOnly).setOnClickListener(v->{filter=3;render(search.getText().toString());});load();}
 void load(){new Thread(()->{try{secrets=api.secrets();active=api.active();runOnUiThread(()->render(""));}catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}
 boolean online(String n){for(int i=0;i<active.length();i++)if(n.equals(active.optJSONObject(i).optString("name")))return true;return false;}
 void render(String q){list.removeAllViews();q=q.toLowerCase(Locale.ROOT);for(int i=0;i<secrets.length();i++){JSONObject x=secrets.optJSONObject(i);if(x==null)continue;String n=x.optString("name");boolean dis="yes".equalsIgnoreCase(x.optString("disabled"));boolean on=!dis&&online(n);if(!n.toLowerCase(Locale.ROOT).contains(q))continue;if(filter==1&&!on||filter==2&&(on||dis)||filter==3&&!dis)continue;
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);row.setPadding(16,12,16,12);row.setBackgroundResource(R.drawable.bg_card);TextView t=new TextView(this);t.setText((on?"🟢 ":dis?"🔴 ":"⚪ ")+n);t.setTextSize(17);t.setTypeface(null,1);row.addView(t);
  TextView z=new TextView(this);String extra=""; if(on){for(int j=0;j<active.length();j++){JSONObject aq=active.optJSONObject(j);if(aq!=null&&n.equals(aq.optString("name"))){extra="   •   ↓"+aq.optString("bytes-in","0")+" / ↑"+aq.optString("bytes-out","0");break;}}}
  z.setText((dis?"DISABLED":on?"ONLINE":"OFFLINE")+"   •   Profile: "+x.optString("profile","default")+extra);z.setTextColor(dis?Color.RED:on?Color.rgb(18,183,106):Color.GRAY);row.addView(z);
  row.setOnClickListener(v->{Intent in=new Intent(this,UserDetailActivity.class);in.putExtra("name",n);startActivity(in);});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,92);lp.setMargins(4,4,4,4);list.addView(row,lp);
 }}
}
