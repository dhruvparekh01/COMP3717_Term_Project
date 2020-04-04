package com.example.comp3717_term_project.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.comp3717_term_project.R;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapUtils {
    private static final String TAG = "MapUtils";

    @Nullable
    public static String getAddressLineByLatLng(Context ctx, LatLng latLng) {
        Geocoder geocoder = new Geocoder(ctx);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                Log.e(TAG, errorMessage);
            }
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            if (address.getThoroughfare() != null) {
                return address.getFeatureName() + " " + address.getThoroughfare();
            } else {
                return address.getFeatureName();
            }
        }

        return null;
    }

    @Nullable
    public static LatLng getLatLngFromLocationName(Context ctx, String addressLine) {
        Geocoder geocoder = new Geocoder(ctx);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(addressLine, 1);
            Log.d(TAG, "getLatLngFromLocationName: " + addressList.size());
        } catch (IOException e) {
            Log.e(TAG, "getLatLngFromLocationName: Error Occured");
            Log.e(TAG, "getLatLngFromLocationName: " + e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                Log.e(TAG, errorMessage);
            }
        }

        if (addressList.size() > 0) {
            Address foundAddress = addressList.get(0);
            return new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());
        }

        return null;
    }

    public static String getDirectionsAPIRequestURL(Context ctx, LatLng from, LatLng to) {
        String strOrigin = String.format("origin=%f,%f", from.latitude, from.longitude);
        String strDestination = String.format("destination=%f,%f", to.latitude, to.longitude);
        String mode = "mode=driving";
        String parameters = String.format("%s&%s&%s", strOrigin, strDestination, mode);
        String outputFormat = "json";
        String endpoint = String.format("https://maps.googleapis.com/maps/api/directions/%s?%s&key=%s",
                outputFormat, parameters, ctx.getString(R.string.google_maps_key));
        Log.d(TAG, "getDirectionsAPIRequestURL: " + endpoint);
        return endpoint;
    }
}
