package com.example.comp3717_term_project;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SpeedSign implements Serializable {

    @SerializedName("properties")
    @Expose
    public PropertiesClass properties;
    class PropertiesClass implements Serializable {
        @SerializedName("Speed")
        @Expose
        private String speed;
        public String getSpeed() {
            return speed;
        }
        public void setSpeed(String speed) {
            this.speed = speed;
        }

        @SerializedName("X")
        @Expose
        private String coord_long;
        public String getCoord_long() {
            return coord_long;
        }
        public void setCoord_long(String coord_long) {
            this.coord_long = coord_long;
        }

        @SerializedName("Y")
        @Expose
        private String coord_lat;
        public String getCoord_lat() {
            return coord_lat;
        }
        public void setCoord_lat(String coord_lat) {
            this.coord_lat = coord_lat;
        }
    }

}
