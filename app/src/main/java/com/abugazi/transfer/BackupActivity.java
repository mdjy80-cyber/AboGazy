package com.abugazi.transfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.abugazi.transfer.db.BackupManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupActivity extends Activity {

    private ListView lvBackups;
    private Button btnBackupNow, btnRestore, btnBack;
    private TextView tvInfo;
    private List<File> backups = new ArrayList<File>();
    private ArrayAdapter<String> adapter;
    private List<String> displayList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        setTitle("النسخ الاحتياطي");

        lvBackups = (ListView) findViewById(R.id.lv_backups);
        btnBackupNow = (Button) findViewById(R.id.btn_backup_now);
        btnRestore = (Button) findViewById(R.id.btn_restore);
        btnBack = (Button) findViewById(R.id.btn_back);
        tvInfo = (TextView) findViewById(R.id.tv_info);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList);
        lvBackups.setAdapter(adapter);

        btnBackupNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackupManager.createBackup(BackupActivity.this);
                load();
            }
        });

        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        lvBackups.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showOptions(position);
                return true;
            }
        });

        lvBackups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirmRestore(backups.get(position));
            }
        });

        load();
    }

    private void load() {
        backups.clear();
        backups.addAll(BackupManager.getAllBackups());
        displayList.clear();
        for (File f : backups) {
            displayList.add(f.getName() + "\\n" + BackupManager.formatDate(f.lastModified()));
        }
        adapter.notifyDataSetChanged();

        long last = getSharedPreferences(BackupManager.PREF_NAME, MODE_PRIVATE)
            .getLong(BackupManager.KEY_LAST_BACKUP, 0);
        if (last > 0) {
            tvInfo.setText("النسخ التلقائي: كل 7 أيام\\nاخر نسخة: " + BackupManager.formatDate(last));
        } else {
            tvInfo.setText("النسخ التلقائي: كل 7 أيام\\nلا توجد نسخ بعد");
        }
    }

    private void showOptions(final int position) {
        final File f = backups.get(position);
        new AlertDialog.Builder(this)
            .setTitle(f.getName())
            .setItems(new String[]{"استعادة", "حذف", "مشاركة"}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int which) {
                    if (which == 0) confirmRestore(f);
                    else if (which == 1) deleteBackup(f);
                    else shareBackup(f);
                }
            })
            .show();
    }

    private void confirmRestore(final File f) {
        new AlertDialog.Builder(this)
            .setTitle("تاكيد الاستعادة")
            .setMessage("سيتم استبدال البيانات الحالية بالنسخة:\\n" + f.getName())
            .setPositiveButton("استعادة", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    if (BackupManager.restoreBackup(BackupActivity.this, f)) {
                        Toast.makeText(BackupActivity.this, "اعادة تشغيل التطبيق مطلوبة", Toast.LENGTH_LONG).show();
                    }
                }
            })
            .setNegativeButton("الغاء", null)
            .show();
    }

    private void deleteBackup(File f) {
        BackupManager.deleteBackup(f);
        load();
        Toast.makeText(this, "تم الحذف", Toast.LENGTH_SHORT).show();
    }

    private void shareBackup(File f) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("application/octet-stream");
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
            startActivity(Intent.createChooser(i, "مشاركة النسخة"));
        } catch (Exception e) {
            Toast.makeText(this, "لا يمكن المشاركة", Toast.LENGTH_SHORT).show();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "اختر ملف النسخة"), 1001);
        } catch (Exception e) {
            Toast.makeText(this, "لا يوجد مدير ملفات", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data = null) {
            Uri uri = data.getData();
            try {
                java.io.InputStream in = getContentResolver().openInputStream(uri);
                File tmp = new File(getCacheDir(), "restore_tmp.db");
                java.io.OutputStream out = new java.io.FileOutputStream(tmp);
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                out.close();
                in.close();

                confirmRestore(tmp);
            } catch (Exception e) {
                Toast.makeText(this, "فشل قراءة الملف: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
