package com.tscore.locationtrail;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tscore.locationtrail.fragments.CurrentLocationFragment;
import com.tscore.locationtrail.fragments.LocationHistoryFragment;

public class MainActivity extends AppCompatActivity implements LocationListener {
private BottomNavigationView bottomNavigationView;
    private static final int PERMISSION_CODE = 101;
    //defining variables
    String[] permissions_all={Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    LocationManager locationManager;
    boolean isGpsLocation;
    Location loc;
    boolean isNetworklocation;

    //1st method which is called in activity life cycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //addLayout
        setContentView(R.layout.activity_main);
        //checking permission and fetching location
       getLocation();
        bottomNavigationView=findViewById(R.id.bottom_nav);
        //setting OnNavigationItemSelectedListener on bottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavMethod);
        //add fragment over this activity
        getSupportFragmentManager().beginTransaction().replace(R.id.container,new CurrentLocationFragment()).commit();
    }

    //OnNavigationItemSelectedListener
    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavMethod=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment=null;
            switch(item.getItemId())
            {
               case  R.id.current_loaction:
                {
                    fragment = new CurrentLocationFragment();

                }break;
                case R.id.location_history:
                {
                    fragment=new LocationHistoryFragment();break;
                }

            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();

            return true;
        }
    };

    //check permission (if access location is granted)
    private void getLocation() {
        if(Build.VERSION.SDK_INT>=23){
            if(checkPermission()){
                getDeviceLocation(); // if yes then call getDeviceLocation()
            }
            else{
                requestPermission();
            }
        }
        else{
            getDeviceLocation();
        }
    }
    //request for location permission - widget for allowing location
    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,permissions_all,PERMISSION_CODE);
    }

    //check if granted
    private boolean checkPermission() {
        for(int i=0;i<permissions_all.length;i++){ //till all permission is checked
            int result= ContextCompat.checkSelfPermission(MainActivity.this,permissions_all[i]);
            if(result== PackageManager.PERMISSION_GRANTED){
                continue;
            }
            else {
                return false;
            }
        }
        return true;
    }

    private void getDeviceLocation() {

        //now all permission part are completed, let's fetch location now using
        locationManager=(LocationManager)getSystemService(Service.LOCATION_SERVICE); //device location using in built function
        isGpsLocation=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworklocation=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!isGpsLocation && !isNetworklocation){

            showSettingForLocation(); //request again for location permission
            getLastlocation(); // get last location
        }
        else{
            getFinalLocation();
        }
    }

    private void getLastlocation() {
        if(locationManager!=null) {
            try {
                Criteria criteria = new Criteria();
                String provider = locationManager.getBestProvider(criteria,false);
                Location location=locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                    getFinalLocation();
                }
                else{
                    Toast.makeText(this, "Permission Failed", Toast.LENGTH_SHORT).show();
                }
        }
    }

    //takes GPS location or network location
    private void getFinalLocation() {
        //one thing i missed in permission let's complete it
        try{
            //Name of the GPS location provider. This provider determines location using satellites.
            // Depending on conditions, this provider may take a while
            //to return a location fix. Requires the permission android.permission.ACCESS_FINE_LOCATION.
            if(isGpsLocation){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,MainActivity.this);
                if(locationManager!=null){
                    loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(loc!=null){
                        updateUi(loc);
                    }
                }
            }
           // Name of the network location provider. This provider determines location based on availability of cell tower and WiFi access points.
            // Results are retrieved by means of a network lookup. Requires either of the permissions android.permission.
            // ACCESS_COARSE_LOCATION or android.permission.ACCESS_FINE_LOCATION.
            else if(isNetworklocation){
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,MainActivity.this);
                if(locationManager!=null){
                    loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(loc!=null){
                        updateUi(loc);
                    }
                }
            }
            else{
              //  Toast.makeText(this, "Can't Get Location", Toast.LENGTH_SHORT).show();
            }

        }catch (SecurityException e){
            Toast.makeText(this, "Can't Get Location", Toast.LENGTH_SHORT).show();
        }

    }

    //for updating UI if needed
    private void updateUi(Location loc) {
        if(loc.getLatitude()==0 && loc.getLongitude()==0){
            getDeviceLocation();
        }
    }

    //dialog box for enabling location
    private void showSettingForLocation() {
        AlertDialog.Builder al=new AlertDialog.Builder(MainActivity.this);
        al.setTitle("Location Not Enabled!");
        al.setMessage("Enable Location ?");
        al.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        al.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        al.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateUi(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}