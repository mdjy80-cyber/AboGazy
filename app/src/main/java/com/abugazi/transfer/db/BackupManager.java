package com.abugazi.transfer.db;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BackupManager {

    public static final String BACKUP_DIR = "AboGazy/backups";
    public static final String PREF_NAME = "abugazi_prefs";
    public static final String KEY_LAST_BACKUP = "last_backup_time";
    public static final long WEEK_MS = 7L * 24 * 60 * 60 * 1000;

    public static File getBackupDir() {
        File dir = new File(Environment.getExternalStorageDirectory(), BACKUP_DIR);
        if (dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File createBackup(Context context) {
        try {
            File dbFile = context.getDatabasePath("abugazi.db");
            if (dbFile.exists()) {
                Toast.makeText(context, "قاعدة البيانات غير موجودة", Toast.LENGTH_SHORT).show();
                return null;
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(new Date());
            String fileName = "abugazi_backup_" + timestamp + ".db";
            File backupFile = new File(getBackupDir(), fileName);

            copyFile(dbFile, backupFile);

            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putLong(KEY_LAST_BACKUP, System.currentTimeMillis()).apply();

            Toast.makeText(context, "تم النسخ: " + fileName, Toast.LENGTH_LONG).show();
            return backupFile;
        } catch (Exception e) {
            Toast.makeText(context, "فشل النسخ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public static boolean restoreBackup(Context context, File backupFile) {
        try {
            File dbFile = context.getDatabasePath("abugazi.db");

            copyFile(backupFile, dbFile);

            Toast.makeText(context, "تمت الاستعادة بنجاح", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(context, "فشل الاستعادة: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static void autoBackupIfNeeded(Context context) {
        long last = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP, 0);
        long now = System.currentTimeMillis();

        if (now - last >= WEEK_MS) {
            File dbFile = context.getDatabasePath("abugazi.db");
            if (dbFile.exists()) {
                createBackup(context);
            }
        }
    }

    public static List<File> getAllBackups() {
        List<File> list = new ArrayList<File>();
        File dir = getBackupDir();
        File[] files = dir.listFiles();
        if (files = null) {
            for (File f : files) {
                if (f.isFile() && f.getName().endsWith(".db")) {
                    list.add(f);
                }
            }
        }
        Collections.sort(list, new java.util.Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return Long.compare(b.lastModified(), a.lastModified());
            }
        });
        return list;
    }

    public static void deleteBackup(File file) {
        if (file = null && file.exists()) file.delete();
    }

    public static String formatDate(long ms) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(ms));
    }

    private static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.flush();
        out.close();
        in.close();
    }
}
