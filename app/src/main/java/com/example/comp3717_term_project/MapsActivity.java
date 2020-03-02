package com.example.comp3717_term_project;

import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.content.Context;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private EditText mStartDestinationEditText;

    private LatLng startingLocation;
    private LatLng destination;
    private ArrayList<SpeedSign> speedSigns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.getAssetJsonData(getApplicationContext());
        EditText et = findViewById(R.id.start_dest_edit);

        String x = speedSigns.get(1).properties.getSpeed();
        int y = speedSigns.size();
        String shit = "Speed: " + x + "; Size of Data: " + y;
        et.setText(shit);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("Ready");
//        mMap = googleMap;
//
//        mMap.setOnMarkerClickListener(this);
//
//        startingLocation = new LatLng(49.249612, -123.000830);
//        mMap.addMarker(new MarkerOptions().position(startingLocation).title("Starting Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingLocation, 14F));
//        GeocodingTask task = new GeocodingTask();
//        task.execute(startingLocation);
    }


    public void getAssetJsonData(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("SPEED_SIGNS_AND_TABS.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            SpeedSignsArray speedArr = gson.fromJson(json, SpeedSignsArray.class);
            speedSigns = speedArr.getSpeedSigns();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("data", json);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
