package com.example.comp3717_term_project;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import static android.content.ContentValues.TAG;

class AssetData {
    static Hashtable<String, ArrayList<SpeedSign>> getStreetTable(Context context) {
        Hashtable<String, ArrayList<SpeedSign>> speedTable = new Hashtable<>();

        ArrayList<SpeedSign> speedSigns = getAssetJsonData(context);

        // Create the speedTable map
        for (SpeedSign ss: speedSigns) {
            String key = ss.getStreet();

            ArrayList<SpeedSign> tempList = speedTable.get(key);
            if (tempList == null)
                tempList = new ArrayList<>(1);

            tempList.add(ss);
            speedTable.put(key, tempList);
        }

        Log.d(TAG, speedTable.toString());

        return speedTable;
    }
    /**
     * Return the list of all speed signs from JSON as an arraylist of Java Objects
     * @param context Context needed to access files
     * @return arraylist of all the speed sign objects from JSON
     */
    private static ArrayList<SpeedSign> getAssetJsonData(Context context) {
        String jsonSpeed = null;
        ArrayList<SpeedSign> speedSigns = null;
        try {
            InputStream iss = context.getAssets().open("SPEEDSIGNS.json");
            int sizeS = iss.available();
            byte[] bufferS = new byte[sizeS];
            iss.read(bufferS);
            iss.close();
            jsonSpeed = new String(bufferS, "UTF-8");

            Gson gson = new Gson();
            SpeedSignsArray speedArr = gson.fromJson(jsonSpeed, SpeedSignsArray.class);

            speedSigns = speedArr.getSpeedSigns();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("SpeedData", jsonSpeed);

        return speedSigns;
    }
}
