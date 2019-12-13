package com.example.tiltspot_starter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class RecordsAdapter extends RecyclerView.Adapter<RecordsHolder> {

    Context context;
    ArrayList<Record> records;

    public RecordsAdapter(Context context, ArrayList<Record> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public RecordsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record, null);
        final RecordsHolder recordsHolder = new RecordsHolder(view);
        return recordsHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsHolder holder, int position) {
        holder.tvPitch.setText(records.get(position).getPitch());
        holder.tvRoll.setText(records.get(position).getRoll());
        holder.tvTimestamp.setText(records.get(position).getTimestamp());
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
