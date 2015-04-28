package com.unique.smarthealthcare;

import com.google.android.gms.maps.model.LatLng;


class RunningPoint  {
	public double accuracy;
	public double altitude;
	public double speed;
	public double time;
	public LatLng latLng;
	public RunningPoint() {
	}
    public RunningPoint(double lat, double lgt, double currentTime) {
    	latLng = new LatLng(lat, lgt);
    	this.time = currentTime;
    }
    

    public RunningPoint(double lat, double lgt, double currentTime, double accuracy , double speed) {
    	latLng = new LatLng(lat, lgt);
        this.time = currentTime;
        this.accuracy = accuracy;
        this.speed = speed;
    }

	public RunningPoint(double lat, double lgt, double currentTime, double accuracy, double altitude, double speed) {
		latLng = new LatLng(lat, lgt);
		this.time = currentTime;
		this.accuracy = accuracy;
		this.altitude = altitude;
		this.speed = speed;
	}
}
