package com.abugazi.transfer;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.bluetooth.*;import android.view.*;import android.widget.*;import java.util.*;import java.io.*;

public class MainActivity extends Activity {
    LinearLayout root, list; Spinner company, method, denomination; EditText amount, price; TextView result; ArrayAdapter<String> denAdapter, compAdapter;
    ArrayList<String> companies=new ArrayList<>(), dens=new ArrayList<>(); android.content.SharedPreferences sp;
    int blue=Color.rgb(21,101,192);
    public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(13,71,161)); sp=getSharedPreferences("data",0); load(); build();}
    void load(){ String cs=sp.getString("companies","سيريتل,MTN,وفا,شام كاش"); companies.addAll(Arrays.asList(cs.split(","))); String ds=sp.getString("denoms","1000,2000,5000,10000,25000,50000,100000,200000"); dens.addAll(Arrays.asList(ds.split(","))); }
    TextView tv(String s,int size){ TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.DKGRAY);t.setPadding(16,14,16,8);t.setGravity(Gravity.RIGHT);return t; }
    Button btn(String s){ Button b=new Button(this);b.setText(s);b.setTextSize(15);return b; }
    public void build(){
      root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(18,12,18,12); root.setBackgroundColor(Color.rgb(245,247,250)); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
      TextView title=tv("أبو غازي\nتحويل الرصيد",23); title.setTextColor(blue); title.setGravity(Gravity.CENTER); root.addView(title,new LinearLayout.LayoutParams(-1,-2));
      company=new Spinner(this); compAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,companies);company.setAdapter(compAdapter); root.addView(tv("شركة الاتصالات / الخدمة",16));root.addView(company);
      method=new Spinner(this);ArrayAdapter<String> ma=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"بالدين","شام كاش"});method.setAdapter(ma);root.addView(tv("طريقة التحويل",16));root.addView(method);
      denomination=new Spinner(this);denAdapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,dens);denomination.setAdapter(denAdapter);root.addView(tv("الفئة",16));root.addView(denomination);
      amount=new EditText(this);amount.setHint("العدد");amount.setInputType(2);root.addView(amount,new LinearLayout.LayoutParams(-1,-2));
      price=new EditText(this);price.setHint("سعر التحويل / العمولة");price.setInputType(2|8192);root.addView(price,new LinearLayout.LayoutParams(-1,-2));
      Button calc=btn("احسب التحويل");root.addView(calc);result=tv("النتيجة: -",19);result.setTextColor(Color.rgb(0,100,0));root.addView(result);
      Button settings=btn("⚙ الإعدادات");root.addView(settings);Button bt=btn("بلوتوث - ربط هاتف آخر");root.addView(bt);
      calc.setOnClickListener(v->{try{double a=Double.parseDouble(amount.getText().toString());double p=Double.parseDouble(price.getText().toString());result.setText("النتيجة: "+fmt(a*p));}catch(Exception e){result.setText("أدخل العدد والسعر");}});
      settings.setOnClickListener(v->settingsDialog()); bt.setOnClickListener(v->bluetoothDialog()); setContentView(root);
    }
    String fmt(double n){ if(n==(long)n)return String.valueOf((long)n);return String.format(Locale.US,"%.2f",n); }
    void settingsDialog(){
      LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(30,10,30,10);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
      EditText c=new EditText(this);c.setHint("الشركات مفصولة بفواصل");c.setText(join(companies));l.addView(c);EditText d=new EditText(this);d.setHint("الفئات مفصولة بفواصل");d.setText(join(dens));l.addView(d);
      new AlertDialog.Builder(this).setTitle("إعدادات أبو غازي").setView(l).setPositiveButton("حفظ",(x,w)->{companies.clear();companies.addAll(Arrays.asList(c.getText().toString().split(",")));dens.clear();dens.addAll(Arrays.asList(d.getText().toString().split(",")));sp.edit().putString("companies",join(companies)).putString("denoms",join(dens)).apply();build();}).setNegativeButton("إلغاء",null).show();
    }
    String join(ArrayList<String>a){StringBuilder s=new StringBuilder();for(String x:a){if(s.length()>0)s.append(",");s.append(x.trim());}return s.toString();}
    void bluetoothDialog(){
      BluetoothAdapter a=BluetoothAdapter.getDefaultAdapter(); if(a==null){new AlertDialog.Builder(this).setMessage("هذا الهاتف لا يدعم البلوتوث").setPositiveButton("حسناً",null).show();return;}
      if(!a.isEnabled()){startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),10);return;}
      final ArrayList<String> names=new ArrayList<>();final ArrayList<String> macs=new ArrayList<>();
      for(BluetoothDevice d:a.getBondedDevices()){names.add(d.getName()+"\n"+d.getAddress());macs.add(d.getAddress());}
      if(names.size()==0)names.add("لا يوجد هاتف مقترن. اقترن بالهاتف الآخر من إعدادات البلوتوث أولاً.");
      new AlertDialog.Builder(this).setTitle("الهواتف المقترنة").setItems(names.toArray(new String[0]),(d,w)->{ if(w<macs.size()) sendBluetooth(macs.get(w));}).setNegativeButton("إغلاق",null).show();
    }
    void sendBluetooth(String mac){ try{BluetoothDevice d=BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mac); final UUID id=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); final BluetoothSocket s=d.createRfcommSocketToServiceRecord(id); new Thread(()->{try{s.connect();String data=company.getSelectedItem()+"|"+method.getSelectedItem()+"|"+denomination.getSelectedItem()+"|"+amount.getText()+"|"+price.getText();s.getOutputStream().write(data.getBytes("UTF-8"));s.close();runOnUiThread(()->Toast.makeText(this,"تم إرسال بيانات التحويل",Toast.LENGTH_LONG).show());}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"تعذر الاتصال بالهاتف الآخر",Toast.LENGTH_LONG).show());}}).start();}catch(Exception e){Toast.makeText(this,"تعذر إنشاء اتصال البلوتوث",Toast.LENGTH_LONG).show();}}
}
