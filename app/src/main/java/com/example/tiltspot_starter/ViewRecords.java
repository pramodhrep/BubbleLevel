package com.example.tiltspot_starter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class ViewRecords extends AppCompatActivity {

    RecyclerView mRecyclerView;
    RecordsAdapter recordsAdapter;
    DatabaseReference db;
    public List<Record> lstRecords;
    String pitch, roll, timestamp;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseDatabase.getInstance().getReference("records");
        setContentView(R.layout.activity_view_records);
        getRecordsList();
    }

    private void getRecordsList() {
        final ArrayList<Record> records = new ArrayList<>();
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lstRecords = new ArrayList<Record>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Record records = child.getValue(Record.class);

                    if(records != null){
                        pitch = records.getPitch();
                        roll = records.getRoll();
                        timestamp = records.getTimestamp();

                        Record record = new Record();

                        record.setPitch(pitch);
                        record.setRoll(roll);
                        record.setTimestamp(timestamp);

                        lstRecords.add(record);
                    }
                }

                mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                recordsAdapter = new RecordsAdapter(context, records);
                mRecyclerView.setAdapter(recordsAdapter);

                Intent intent = new Intent(getApplicationContext(), ViewRecords.class);
                intent.putExtra("Meetings", (Serializable) lstRecords);
                startActivity(intent);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
