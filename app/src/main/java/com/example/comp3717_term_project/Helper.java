package com.example.comp3717_term_project;

import android.location.Location;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper static class to have helper method(s)
 */
class Helper {
    /**
     * Takes in a list of speed signs and returns the speed sign object applicable to the user.
     * Eg: User is on 10th Avenue and there are 5 different speed signs of different values on
     * this street. This function returns the best applicable speed sign based on user location and
     * direction of movement.
     * @param oldLoc location of user 3 seconds ago
     * @param newLoc current location of the user
     * @param speedSigns list of speed signs on the street that the user is on
     * @return the best applicable speed sign object or null if none of them are applicable
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    static SpeedSign getBestSign(Location oldLoc, Location newLoc, ArrayList<SpeedSign> speedSigns) {
        // return null is speedSigns is an empty list
        if (speedSigns.size() == 0)
                return null;

        // make a hash map of possible valid speed signs as key and distance of user from it as value
        HashMap<SpeedSign, Double> valid = new HashMap<>(0);
        for(SpeedSign s: speedSigns) {
            // get a location object from the speed sign
            Location target = new Location("");
            target.setLatitude(s.properties.getCoord_lat());
            target.setLongitude(s.properties.getCoord_long());

            // distance from oldLoc and newLoc
            double d1 = target.distanceTo(oldLoc);
            double d2 = target.distanceTo(newLoc);

            // the speed sign is valid if the user is not approaching it but moving away from it
            if (d2 > d1)
                valid.put(s, d2);
        }
        // if there are no valid signs, return null
        if (valid.size() == 0)
            return null;

        // Return the closest valid speed sign
        return Collections.min(valid.entrySet(), Map.Entry.comparingByValue()).getKey();

    }
}
