package com.tscore.locationtrail.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tscore.locationtrail.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HistoryLocationApapter extends RecyclerView.Adapter<HistoryLocationApapter.HistoryViewHolder> {
    Context context;
    ArrayList<String> locations=new ArrayList<>();


    public HistoryLocationApapter(Context context, ArrayList<String> locations) {
        this.context = context;
        this.locations = locations;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.location_view_holder,parent,false);

        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String[] loc=locations.get(position).toString().split("#",3);
        holder.lat.setText(loc[0]);
        holder.longi.setText(loc[1]);
        holder.time.setText(loc[2]);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView lat,longi,time;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            lat=itemView.findViewById(R.id.lat);
            longi=itemView.findViewById(R.id.longi);
            time=itemView.findViewById(R.id.current_time);
        }
    }
}
