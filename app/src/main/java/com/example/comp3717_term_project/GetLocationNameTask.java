package com.example.comp3717_term_project;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class GetLocationNameTask {
    private static final String REVERSE_GEOCODING_ENDPOINT = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s";
    private static final String API_KEY = "AIzaSyD8J0Avx_iLRDty4Iib3g90r9yilPeMHN4";

    public String getLocationName(LatLng location) {
        double locationLat = location.latitude;
        double locationLong = location.longitude;
        String requestURLString = String.format(REVERSE_GEOCODING_ENDPOINT, locationLat, locationLong, API_KEY);
        String response = HttpHandler.makeGetRequest(requestURLString);
        String locationString = null;
        try {
            JSONObject responseObject = new JSONObject(response);
            locationString = responseObject.getJSONArray("results").getJSONObject(0).getString("formatted_address");
            Log.i("GetLocationNameTask", locationString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return locationString;
    }
}
