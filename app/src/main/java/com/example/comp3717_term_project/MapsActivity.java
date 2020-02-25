package com.example.comp3717_term_project;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<SpeedSign> speedSigns;
    private ArrayList<WarningSign> warnSigns;

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
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(49.249612, -123.000830);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    public void getAssetJsonData(Context context) {
        String jsonSpeed = null;
        String jsonWarn = null;
        try {
            InputStream iss = context.getAssets().open("SPEED_SIGNS_AND_TABS.json");
            InputStream isw = context.getAssets().open("WARNING_SIGNS.json");
            int sizeS = iss.available();
            byte[] bufferS = new byte[sizeS];
            iss.read(bufferS);
            iss.close();
            jsonSpeed = new String(bufferS, "UTF-8");

            int sizeW = isw.available();
            byte[] bufferW = new byte[sizeW];
            isw.read(bufferW);
            isw.close();
            jsonWarn = new String(bufferW, "UTF-8");

            Gson gson = new Gson();
            SpeedSignsArray speedArr = gson.fromJson(jsonSpeed, SpeedSignsArray.class);
            WarningSignsArray warnArr = gson.fromJson(jsonWarn, WarningSignsArray.class);

            speedSigns = speedArr.getSpeedSigns();
            warnSigns = warnArr.getWarnSigns();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("SpeedData", jsonSpeed);
        Log.e("WarnData", jsonWarn);

    }

}
