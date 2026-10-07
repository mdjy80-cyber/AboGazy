package com.abugazi.transfer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.abugazi.transfer.db.BackupManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackupManager.autoBackupIfNeeded(this);

        Button btnTransfer = (Button) findViewById(R.id.btn_transfer);
        Button btnCustomers = (Button) findViewById(R.id.btn_customers);
        Button btnDebts = (Button) findViewById(R.id.btn_debts);
        Button btnSham = (Button) findViewById(R.id.btn_sham_cash);
        Button btnSettings = (Button) findViewById(R.id.btn_settings);
        Button btnBackup = (Button) findViewById(R.id.btn_backup);

        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, TransferActivity.class)); }
        });
        btnCustomers.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, CustomersActivity.class)); }
        });
        btnDebts.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, DebtsActivity.class)); }
        });
        btnSham.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, ShamCashActivity.class)); }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, SettingsActivity.class)); }
        });
        btnBackup.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(MainActivity.this, BackupActivity.class)); }
        });
    }
}
