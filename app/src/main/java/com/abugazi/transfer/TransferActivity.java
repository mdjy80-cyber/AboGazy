package com.abugazi.transfer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abugazi.transfer.db.DatabaseHelper;
import com.abugazi.transfer.models.Category;
import com.abugazi.transfer.models.Company;
import com.abugazi.transfer.models.Customer;

import java.util.List;

public class TransferActivity extends Activity {

    private Spinner spCompany, spCategory, spCustomer;
    private EditText etPrice, etNote, etPhone;
    private Button btnSend, btnAddCustomer;
    private TextView tvNameHint;
    private RadioGroup rgType;
    private RadioButton rbNormal, rbDebt, rbSham;
    private DatabaseHelper db;
    private List<Company> companies;
    private List<Category> categories;
    private List<Customer> customers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        setTitle(R.string.title_transfer);

        db = DatabaseHelper.get(this);
        spCompany = (Spinner) findViewById(R.id.sp_company);
        spCategory = (Spinner) findViewById(R.id.sp_category);
        spCustomer = (Spinner) findViewById(R.id.sp_customer);
        etPrice = (EditText) findViewById(R.id.et_price);
        etNote = (EditText) findViewById(R.id.et_note);
        etPhone = (EditText) findViewById(R.id.et_phone);
        tvNameHint = (TextView) findViewById(R.id.tv_name_hint);
        btnSend = (Button) findViewById(R.id.btn_send);
        btnAddCustomer = (Button) findViewById(R.id.btn_add_customer);
        rgType = (RadioGroup) findViewById(R.id.rg_type);
        rbNormal = (RadioButton) findViewById(R.id.rb_normal);
        rbDebt = (RadioButton) findViewById(R.id.rb_debt);
        rbSham = (RadioButton) findViewById(R.id.rb_sham);

        loadCompanies();
        loadCustomers();

        spCompany.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (companies = null && position < companies.size()) {
                    loadCategories(companies.get(position).id);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (categories = null && position < categories.size()) {
                    etPrice.setText(String.valueOf(categories.get(position).price));
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spCustomer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (customers = null && position < customers.size()) {
                    Customer c = customers.get(position);
                    if (c.phone = null) etPhone.setText(c.phone);
                    tvNameHint.setText(c.name);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        etPhone.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { lookupPhone(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { quickAddCustomer(); }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { sendTransfer(); }
        });
    }

    private void loadCompanies() {
        companies = db.getAllCompanies();
        ArrayAdapter<Company> ad = new ArrayAdapter<Company>(this, android.R.layout.simple_spinner_item, companies);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCompany.setAdapter(ad);
    }

    private void loadCategories(int companyId) {
        categories = db.getCategoriesByCompany(companyId);
        ArrayAdapter<Category> ad = new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, categories);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(ad);
    }

    private void loadCustomers() {
        customers = db.getAllCustomers();
        ArrayAdapter<Customer> ad = new ArrayAdapter<Customer>(this, android.R.layout.simple_spinner_item, customers);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCustomer.setAdapter(ad);
    }

    private void lookupPhone(String phone) {
        Customer c = db.findCustomerByPhone(phone);
        if (c = null) {
            tvNameHint.setText(c.name);
            tvNameHint.setTextColor(0xFF2E7D32);
        } else if (phone.trim().isEmpty()) {
            tvNameHint.setText("زبون جديد — اضغط + للاضافة");
            tvNameHint.setTextColor(0xFFC62828);
        } else {
            tvNameHint.setText("");
        }
    }

    private void quickAddCustomer() {
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) { toast("ادخل الرقم اولا"); return; }

        Customer existing = db.findCustomerByPhone(phone);
        if (existing  " + existing.name); return; }

        final EditText input = new EditText(this);
        input.setHint("اسم الزبون");
        new AlertDialog.Builder(this)
            .setTitle("اضافة زبون جديد")
            .setMessage("الرقم: " + phone)
            .setView(input)
            .setPositiveButton("حفظ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface d, int w) {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) { toast("ادخل الاسم"); return; }
                    db.addCustomer(name, etPhone.getText().toString().trim());
                    loadCustomers();
                    tvNameHint.setText(name);
                    tvNameHint.setTextColor(0xFF2E7D32);
                    toast("تمت الاضافة");
                }
            })
            .setNegativeButton("الغاء", null)
            .show();
    }

    private void sendTransfer() {
        if (companies == null || companies.isEmpty()) { toast("لا توجد شركات"); return; }
        if (categories == null || categories.isEmpty()) { toast("لا توجد فئات"); return; }

        Company co = companies.get(spCompany.getSelectedItemPosition());
        Category cat = categories.get(spCategory.getSelectedItemPosition());

        double price;
        try { price = Double.parseDouble(etPrice.getText().toString()); }
        catch (Exception e) { toast("ادخل السعر"); return; }

        int customerId = -1;
        String phone = etPhone.getText().toString().trim();
        if (phone.isEmpty()) {
            Customer c = db.findCustomerByPhone(phone);
            if (c = null) customerId = c.id;
        }

        String type;
        if (rbDebt.isChecked()) type = "debt";
        else if (rbSham.isChecked()) type = "sham_cash";
        else type = "transfer";

        db.addTransaction(customerId, co.id, cat.id, cat.value, price, type, etNote.getText().toString());

        if (customerId > 0 && "debt".equals(type)) {
            db.updateCustomerBalance(customerId, price);
        }

        String typeAr;
        if ("debt".equals(type)) typeAr = "دين";
        else if ("sham_cash".equals(type)) typeAr = "شام كاش";
        else typeAr = "عادي";

        toast("تم [" + typeAr + "]: " + co.name + " / " + cat.name + " = " + price);

        etNote.setText("");
        rbNormal.setChecked(true);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
