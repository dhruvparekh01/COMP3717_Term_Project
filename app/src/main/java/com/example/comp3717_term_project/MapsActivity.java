package com.example.comp3717_term_project;

import androidx.fragment.app.FragmentActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private EditText startingLocationEditText;
    private EditText destinationEditText;

    private LatLng startingLocation;
    private LatLng destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startingLocationEditText = findViewById(R.id.starting_position_edit_text);
        destinationEditText = findViewById(R.id.destination_edit_text);
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

        mMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        startingLocation = new LatLng(49.249612, -123.000830);
        mMap.addMarker(new MarkerOptions().position(startingLocation).title("Starting Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingLocation, 14F));
        GeocodingTask task = new GeocodingTask();
        task.execute(startingLocation);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private class GeocodingTask extends AsyncTask<LatLng, Void, String> {
        @Override
        protected String doInBackground(LatLng... arg0) {
            GetLocationNameTask getLocationNameTask = new GetLocationNameTask();
            return getLocationNameTask.getLocationName(arg0[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            startingLocationEditText.setText(result);
        }
    }
}
