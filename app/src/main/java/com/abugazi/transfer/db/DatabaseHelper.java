package com.abugazi.transfer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abugazi.transfer.models.Category;
import com.abugazi.transfer.models.Company;
import com.abugazi.transfer.models.Customer;
import com.abugazi.transfer.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "abugazi.db";
    private static final int DB_VERSION = 1;

    public static final String TBL_COMPANIES = "companies";
    public static final String TBL_CATEGORIES = "categories";
    public static final String TBL_CUSTOMERS = "customers";
    public static final String TBL_TRANSACTIONS = "transactions";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper get(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBL_COMPANIES + " ^(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "color TEXT DEFAULT '#1565C0', " +
            "active INTEGER DEFAULT 1^)");

        db.execSQL("CREATE TABLE " + TBL_CATEGORIES + " ^(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "company_id INTEGER NOT NULL, " +
            "name TEXT NOT NULL, " +
            "value REAL DEFAULT 0, " +
            "price REAL DEFAULT 0, " +
            "active INTEGER DEFAULT 1^)");

        db.execSQL("CREATE TABLE " + TBL_CUSTOMERS + " ^(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "phone TEXT, " +
            "balance REAL DEFAULT 0, " +
            "created_at TEXT^)");

        db.execSQL("CREATE TABLE " + TBL_TRANSACTIONS + " ^(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "customer_id INTEGER, " +
            "company_id INTEGER, " +
            "category_id INTEGER, " +
            "amount REAL DEFAULT 0, " +
            "price REAL DEFAULT 0, " +
            "type TEXT, " +
            "note TEXT, " +
            "created_at TEXT^)");

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + TBL_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TBL_COMPANIES);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        long c1 = insertCompany(db, "سيرياتيل", "#1565C0");
        long c2 = insertCompany(db, "MTN", "#FFC107");
        insertCategory(db, c1, "فئة 500", 500, 550);
        insertCategory(db, c1, "فئة 1000", 1000, 1100);
        insertCategory(db, c1, "فئة 2000", 2000, 2200);
        insertCategory(db, c2, "فئة 500", 500, 550);
        insertCategory(db, c2, "فئة 1000", 1000, 1100);
        insertCategory(db, c2, "فئة 2000", 2000, 2200);
    }

    private long insertCompany(SQLiteDatabase db, String name, String color) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("color", color);
        return db.insert(TBL_COMPANIES, null, v);
    }

    private long insertCategory(SQLiteDatabase db, long companyId, String name, double value, double price) {
        ContentValues v = new ContentValues();
        v.put("company_id", companyId);
        v.put("name", name);
        v.put("value", value);
        v.put("price", price);
        return db.insert(TBL_CATEGORIES, null, v);
    }

    public List<Company> getAllCompanies() {
        List<Company> list = new ArrayList<Company>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, color, active FROM " + TBL_COMPANIES + " WHERE active=1 ORDER BY id", null);
        while (c.moveToNext()) {
            Company co = new Company();
            co.id = c.getInt(0);
            co.name = c.getString(1);
            co.color = c.getString(2);
            co.active = c.getInt(3);
            list.add(co);
        }
        c.close();
        return list;
    }

    public long addCompany(String name, String color) {
        SQLiteDatabase db = getWritableDatabase();
        return insertCompany(db, name, color);
    }

    public void updateCompany(int id, String name, String color) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("color", color);
        db.update(TBL_COMPANIES, v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteCompany(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TBL_COMPANIES, "id=?", new String[]{String.valueOf(id)});
        db.delete(TBL_CATEGORIES, "company_id=?", new String[]{String.valueOf(id)});
    }

    public List<Category> getCategoriesByCompany(int companyId) {
        List<Category> list = new ArrayList<Category>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, company_id, name, value, price, active FROM " + TBL_CATEGORIES + " WHERE company_id=? AND active=1 ORDER BY value", new String[]{String.valueOf(companyId)});
        while (c.moveToNext()) {
            Category cat = new Category();
            cat.id = c.getInt(0);
            cat.companyId = c.getInt(1);
            cat.name = c.getString(2);
            cat.value = c.getDouble(3);
            cat.price = c.getDouble(4);
            cat.active = c.getInt(5);
            list.add(cat);
        }
        c.close();
        return list;
    }

    public long addCategory(int companyId, String name, double value, double price) {
        SQLiteDatabase db = getWritableDatabase();
        return insertCategory(db, companyId, name, value, price);
    }

    public void updateCategory(int id, String name, double value, double price) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("value", value);
        v.put("price", price);
        db.update(TBL_CATEGORIES, v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TBL_CATEGORIES, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<Customer>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, phone, balance FROM " + TBL_CUSTOMERS + " ORDER BY name", null);
        while (c.moveToNext()) {
            Customer cu = new Customer();
            cu.id = c.getInt(0);
            cu.name = c.getString(1);
            cu.phone = c.getString(2);
            cu.balance = c.getDouble(3);
            list.add(cu);
        }
        c.close();
        return list;
    }

    public Customer findCustomerByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, phone, balance FROM " + TBL_CUSTOMERS + " WHERE phone=?", new String[]{phone.trim()});
        Customer cu = null;
        if (c.moveToFirst()) {
            cu = new Customer();
            cu.id = c.getInt(0);
            cu.name = c.getString(1);
            cu.phone = c.getString(2);
            cu.balance = c.getDouble(3);
        }
        c.close();
        return cu;
    }

    public long addCustomer(String name, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("phone", phone);
        v.put("balance", 0);
        v.put("created_at", now());
        return db.insert(TBL_CUSTOMERS, null, v);
    }

    public void updateCustomer(int id, String name, String phone) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("phone", phone);
        db.update(TBL_CUSTOMERS, v, "id=?", new String[]{String.valueOf(id)});
    }

    public void deleteCustomer(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TBL_CUSTOMERS, "id=?", new String[]{String.valueOf(id)});
    }

    public void updateCustomerBalance(int customerId, double delta) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TBL_CUSTOMERS + " SET balance = balance + ? WHERE id = ?", new Object[]{delta, customerId});
    }

    public long addTransaction(int customerId, int companyId, int categoryId,
                               double amount, double price, String type, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("customer_id", customerId);
        v.put("company_id", companyId);
        v.put("category_id", categoryId);
        v.put("amount", amount);
        v.put("price", price);
        v.put("type", type);
        v.put("note", note);
        v.put("created_at", now());
        return db.insert(TBL_TRANSACTIONS, null, v);
    }

    public List<Transaction> getAllTransactions() {
        return getTransactionsByType(null);
    }

    public List<Transaction> getTransactionsByType(String type) {
        List<Transaction> list = new ArrayList<Transaction>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c;
        if (type == null) {
            c = db.rawQuery("SELECT id, customer_id, company_id, category_id, amount, price, type, note, created_at FROM " + TBL_TRANSACTIONS + " ORDER BY id DESC", null);
        } else {
            c = db.rawQuery("SELECT id, customer_id, company_id, category_id, amount, price, type, note, created_at FROM " + TBL_TRANSACTIONS + " WHERE type=? ORDER BY id DESC", new String[]{type});
        }
        while (c.moveToNext()) {
            Transaction t = new Transaction();
            t.id = c.getInt(0);
            t.customerId = c.getInt(1);
            t.companyId = c.getInt(2);
            t.categoryId = c.getInt(3);
            t.amount = c.getDouble(4);
            t.price = c.getDouble(5);
            t.type = c.getString(6);
            t.note = c.getString(7);
            t.createdAt = c.getString(8);
            list.add(t);
        }
        c.close();
        return list;
    }

    public Customer getCustomerById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, phone, balance FROM " + TBL_CUSTOMERS + " WHERE id=?", new String[]{String.valueOf(id)});
        Customer cu = null;
        if (c.moveToFirst()) {
            cu = new Customer();
            cu.id = c.getInt(0);
            cu.name = c.getString(1);
            cu.phone = c.getString(2);
            cu.balance = c.getDouble(3);
        }
        c.close();
        return cu;
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
    }
}
