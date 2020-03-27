package com.example.comp3717_term_project;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpeedSign implements Serializable {

    @SerializedName("properties")
    @Expose
    public SpeedProperties properties;
    class SpeedProperties implements Serializable {
        @SerializedName("Speed")
        @Expose
        private int speed;
        public int getSpeed() {
            return speed;
        }
        public void setSpeed(String speed) {
            this.speed = Integer.valueOf(speed);
        }

        @SerializedName("X")
        @Expose
        private double coord_long;
        public double getCoord_long() {
            return coord_long;
        }
        public void setCoord_long(String coord_long) {
            this.coord_long = Double.valueOf(coord_long);
        }

        @SerializedName("Y")
        @Expose
        private double coord_lat;
        public double getCoord_lat() {
            return coord_lat;
        }
        public void setCoord_lat(String coord_lat) {
            this.coord_lat = Double.valueOf(coord_lat);
        }
    }

    @SerializedName("street")
    @Expose
    private String street;
    public String getStreet() { return street; }
    public void setStreet(String street) {
        this.street = street;
    }
}
