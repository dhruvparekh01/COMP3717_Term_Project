package com.example.comp3717_term_project;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SpeedSignsArray {

    @SerializedName("features")
    @Expose
    private ArrayList<SpeedSign> speedSigns = new ArrayList<>();

    public ArrayList<SpeedSign> getSpeedSigns() {
        return speedSigns;
    }

    public void setSpeedSigns(ArrayList<SpeedSign> speedSigns) {
        this.speedSigns = speedSigns;
    }
}
