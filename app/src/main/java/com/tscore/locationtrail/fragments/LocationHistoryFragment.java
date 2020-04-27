package com.tscore.locationtrail.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.opencsv.CSVWriter;
import com.tscore.locationtrail.MainActivity;
import com.tscore.locationtrail.R;
import com.tscore.locationtrail.adapter.HistoryLocationApapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.LOCATION_SERVICE;


public class LocationHistoryFragment extends Fragment implements android.location.LocationListener {

    ArrayList<String> locations=new ArrayList<>();
    RecyclerView recyclerView;
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    Date date = new Date();
    FloatingActionButton clear,add;
    ImageView imageView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v=inflater.inflate(R.layout.fragment_location_history, container, false);
        recyclerView=v.findViewById(R.id.recycler_view);
        Location l=getCurrentLocation(getContext());
        clear=v.findViewById(R.id.clear);
        add=v.findViewById(R.id.add);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locations.clear();
                recyclerView.setAdapter(new HistoryLocationApapter(getContext(),locations));
                Toast.makeText(getContext(),"Clear Location Track History",Toast.LENGTH_LONG).show();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location l=getCurrentLocation(getContext());
                if(l!=null) {
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    Date date = new Date();
                    locations.add(l.getLatitude()+"#"+l.getLongitude()+"#"+formatter.format(date));
                    recyclerView.setAdapter(new HistoryLocationApapter(getContext(),locations));

                }
            }
        });
        imageView=v.findViewById(R.id.download);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(),"Download Started",Toast.LENGTH_SHORT).show();
                export();

            }
        });

        if(l!=null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            locations.add(l.getLatitude()+"#"+l.getLongitude()+"#"+formatter.format(date));
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new HistoryLocationApapter(getContext(),locations));
        return v;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationAtt", "getCurrentLocation2: " + location.getLatitude() + ", " + location.getLongitude());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
       locations.add(location.getLatitude()+"#"+location.getLongitude()+"#"+formatter.format(date));
        recyclerView.setAdapter(new HistoryLocationApapter(getContext(),locations));



    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    public Location getCurrentLocation(Context mContext){
        int MIN_TIME_BW_UPDATES = 1;
        int MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
        Location loc = null;
        Double latitude, longitude;

        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);

        // getting GPS status
        Boolean checkGPS = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        Boolean checkNetwork = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!checkGPS && !checkNetwork) {
            Toast.makeText(mContext, "No Service Provider Available", Toast.LENGTH_SHORT).show();
        } else {
            //this.canGetLocation = true;
            // First get location from Network Provider
            if (checkNetwork) {

                try {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        loc = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }

                    if (loc != null) {
                        Log.d("LocationAtt", "getCurrentLocation: " + loc.getLatitude() + ", " + loc.getLongitude());
                        return loc;
                    }
                } catch (SecurityException e) {

                }
            }
        }
        // if GPS Enabled get lat/long using GPS Services
        if (checkGPS) {

            if (loc == null) {
                try {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        loc = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date date = new Date();
                            locations.add(loc.getLatitude()+"#"+loc.getLongitude()+"#"+formatter.format(date));
                            recyclerView.setAdapter(new HistoryLocationApapter(getContext(),locations));

                        }
                    }
                } catch (SecurityException e) {

                }
            }
        }
        Location locErr = null;
        return locErr;
    }
    public void export(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions((Activity)getContext(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},0);

        }
        else
        {
            File exportDir = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!exportDir.exists())
            {
                exportDir.mkdirs();
            }

            File file = new File(exportDir, "location_data.csv");
            try {
                file.createNewFile();
                CSVWriter csvWriter=new CSVWriter(new FileWriter(file,false));
                String row[]=new String[]{"Latitude","Longitude","Time"};
                csvWriter.writeNext(row);
                for(int i=0;i<locations.size();i++)
                {
                    String[] loc=locations.get(i).toString().split("#",3);
                    csvWriter.writeNext(loc);
                }

                csvWriter.close();
                Toast.makeText(getContext(),"File downloaded in Internal Storage",Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getContext(),"Error in downloading file"+e.toString(),Toast.LENGTH_LONG);
                e.printStackTrace();
            }

        }
    }
}
