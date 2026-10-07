package com.abugazi.transfer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.abugazi.transfer.db.DatabaseHelper;
import com.abugazi.transfer.models.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ShamCashActivity extends Activity {

    private EditText etSearch;
    private ListView lv;
    private TextView tvTotal;
    private DatabaseHelper db;
    private List<Transaction> all = new ArrayList<Transaction>();
    private List<String> display = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sham_cash);
        setTitle("سجل شام كاش");

        db = DatabaseHelper.get(this);
        etSearch = (EditText) findViewById(R.id.et_search);
        lv = (ListView) findViewById(R.id.lv_transactions);
        tvTotal = (TextView) findViewById(R.id.tv_total);
        Button btnBack = (Button) findViewById(R.id.btn_back);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, display);
        lv.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { applyFilter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        load();
    }

    private void load() {
        all.clear();
        all.addAll(db.getTransactionsByType("sham_cash"));
        applyFilter(etSearch.getText().toString());
    }

    private void applyFilter(String q) {
        display.clear();
        q = q.trim().toLowerCase();
        double total = 0;
        for (Transaction t : all) {
            String line = "#" + t.id + " | " + t.amount + " / " + t.price + " | " + t.createdAt;
            if (q.isEmpty() || line.toLowerCase().contains(q)) {
                display.add(line);
                total += t.price;
            }
        }
        adapter.notifyDataSetChanged();
        tvTotal.setText("عدد العمليات: " + display.size() + "  |  المجموع: " + total);
    }
}
