package com.example.tiltspot_starter;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RecordsHolder extends RecyclerView.ViewHolder  {

    ImageView ivBubble;
    TextView tvPitch, tvRoll, tvTimestamp;

    public RecordsHolder(@NonNull View itemView) {
        super(itemView);

        ivBubble = (ImageView) itemView.findViewById(R.id.ivBubble);
        tvPitch = (TextView) itemView.findViewById(R.id.tvPitch);
        tvRoll = (TextView) itemView.findViewById(R.id.tvRoll);
        tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimeStamp);
    }
}
