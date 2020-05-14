package com.tscore.locationtrail.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.tscore.locationtrail.GPSHelper;
import com.tscore.locationtrail.ListActivity;
import com.tscore.locationtrail.Login_Screen;
import com.tscore.locationtrail.MainActivity;
import com.tscore.locationtrail.MapsActivity;
import com.tscore.locationtrail.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.Context.LOCATION_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;


public class CurrentLocationFragment extends Fragment  implements android.location.LocationListener {

    TextView txtLat,txtLong,time,logout;
Button seeMap,seeDevices;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Inflated view
        View view = inflater.inflate(R.layout.fragment_current_location, container, false);
        //for logout feature
        googleSignInClient = GoogleSignIn.getClient(getContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();

      getCurrentLocation(getContext()); //finds current location
      seeMap=view.findViewById(R.id.seeMap);
      seeDevices=view.findViewById(R.id.scan_bluetooth);
      logout=view.findViewById(R.id.logout);

      // on click of show in map -> intent to maps activity
      seeMap.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              startActivity(new Intent(getContext(), MapsActivity.class));
          }
      });
        seeDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(), ListActivity.class));
            }
        });

      //onclick of logout button
      logout.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              firebaseAuth.signOut();

              // Google sign out
              googleSignInClient.signOut().addOnCompleteListener(getActivity(),
                      new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              // Google Sign In failed, update UI appropriately
                              Intent intent=new Intent(getContext(), Login_Screen.class);
                              intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                              startActivity(intent); //intent to login activity


                          }
                      });
          }
      });


        LottieAnimationView lottieAnimationView = view.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(R.raw.current_loc);
        //PLAY LOTTIE ANIMATION
        lottieAnimationView.playAnimation();

        //GET ID FOR ALL EDIT TEXT FIELD
        txtLat=view.findViewById(R.id.lat);
        txtLong=view.findViewById(R.id.longuitude);
        time=view.findViewById(R.id.time);
        //FETCHING CURRENT LOCATION
        Location l=getCurrentLocation(getContext());

        if(l!=null) //SET THE VALUES IN ALL EDIT TEXT
        {
            txtLat.setText(""+l.getLatitude());
            txtLong.setText(""+l.getLongitude());
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            time.setText(formatter.format(date));
        }
        return view;
    }


        //IF LOCATION IS CHANGED, SET THE NEW VALUES INTO THE EDIT TEXT
    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationAtt", "getCurrentLocation2: " + location.getLatitude() + ", " + location.getLongitude());
        txtLat.setText(""+location.getLatitude());
        txtLong.setText(""+location.getLongitude());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        time.setText(formatter.format(date));
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
    //Extra Code
    public Location getCurrentLocation(Context mContext){
        int MIN_TIME_BW_UPDATES = 1; //1 SEC
        int MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;  //1 METER
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
                            txtLat.setText(""+loc.getLatitude());
                            txtLong.setText(""+loc.getLongitude());
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                            Date date = new Date();
                            time.setText(formatter.format(date));
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                        }
                    }
                } catch (SecurityException e) {

                }
            }
        }
        Location locErr = null;
        return locErr;
    }
}
