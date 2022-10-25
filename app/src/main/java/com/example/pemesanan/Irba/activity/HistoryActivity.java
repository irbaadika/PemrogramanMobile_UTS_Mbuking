package com.example.pemesanan.Irba.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.pemesanan.Irba.R;
import com.example.pemesanan.Irba.adapter.HistoryAdapter;
import com.example.pemesanan.Irba.database.DatabaseHelper;
import com.example.pemesanan.Irba.model.HistoryModel;
import com.example.pemesanan.Irba.session.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity {

    protected Cursor cursor;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    SessionManager session;
    String id_book = "", asal, tujuan, tanggal, dewasa, anak, riwayat, total;
    String email;
    TextView tvNotFound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        tvNotFound = findViewById(R.id.noHistory);

        session = new SessionManager(getApplicationContext());

        HashMap<String, String> user = session.getUserDetails();

        email = user.get(SessionManager.KEY_EMAIL);

        refreshList();
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.tbHistory);
        toolbar.setTitle("Mbuking History");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshList() {
        final ArrayList<HistoryModel> hasil = new ArrayList<>();
        cursor = db.rawQuery("SELECT * FROM TB_BOOK, TB_HARGA WHERE TB_BOOK.id_book = TB_HARGA.id_book AND username='" + email + "'", null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            id_book = cursor.getString(0);
            asal = cursor.getString(1);
            tujuan = cursor.getString(2);
            tanggal = cursor.getString(3);
            dewasa = cursor.getString(4);
            anak = cursor.getString(5);
            total = cursor.getString(10);
            if (asal.equalsIgnoreCase("CINEPOLIS") || asal.equalsIgnoreCase("ARAYA XXI") ||
                    asal.equalsIgnoreCase("MOVIMAX") || asal.equalsIgnoreCase("MANDALA XXI") ||
                    asal.equalsIgnoreCase("CGV")){
                riwayat = "Booking Success!\nBioskop : " + asal + "\nKursi : " + tujuan + "\nTanggal : " + tanggal + ". " +
                        "\nTiket : " + dewasa + "\nSnack : " + anak;
                hasil.add(new HistoryModel(id_book, tanggal, riwayat, total, R.drawable.profile));
            }else {
                riwayat = "Booking Success!\nTujuan : " + asal + " - " + tujuan + "\nTanggal : " + tanggal + ". " +
                        "\nTiket : " + dewasa + " Dewasa & " + anak + " Anak-anak.";
                hasil.add(new HistoryModel(id_book, tanggal, riwayat, total, R.drawable.profile));
            }
        }

        ListView listBook = findViewById(R.id.list_booking);
        HistoryAdapter arrayAdapter = new HistoryAdapter(this, hasil);
        listBook.setAdapter(arrayAdapter);

        //delete data
        listBook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String selection = hasil.get(i).getIdBook();
                final CharSequence[] dialogitem = {"Hapus Data"};
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("Pilihan");
                builder.setItems(dialogitem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        try {
                            db.execSQL("DELETE FROM TB_BOOK where id_book = " + selection + "");
                            id_book = "";
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        refreshList();
                    }
                });
                builder.create().show();
            }
        });

        if (id_book.equals("")) {
            tvNotFound.setVisibility(View.VISIBLE);
            listBook.setVisibility(View.GONE);
        } else {
            tvNotFound.setVisibility(View.GONE);
            listBook.setVisibility(View.VISIBLE);
        }

    }
}
