package com.unique.smarthealthcare;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class TripData {

	public long tripid;
	public long startTime = 0;
	public long endTime = 0;
	public int numpoints = 0;
	public double lathigh, lgthigh, latlow, lgtlow, latestlat, latestlgt;
	public int status;
	public float distance;
	public String tripPurpose, fancystart, info;
	// private ItemizedOverlayTrack gpspoints;
	public RunningPoint startpoint, endpoint;
	public List<RunningPoint> points;
	public long totalPauseTime = 0;
	public long pauseStartedAt = 0;

	DbAdapter mDb;

	public static int STATUS_INCOMPLETE = 0;
	public static int STATUS_COMPLETE = 1;
	public static int STATUS_SENT = 2;

	public static TripData createTrip(Context c) {
		TripData t = new TripData(c.getApplicationContext(), 0);
		t.createTripInDatabase(c);
		t.initializeData();
		return t;
	}

	public static TripData fetchTrip(Context c, long tripid) {
		TripData t = new TripData(c.getApplicationContext(), tripid);
		t.populateDetails();
		return t;
	}

	public static TripData fetchAllCoordsForTrip(Context c, long tripid) {
		TripData t = new TripData(c.getApplicationContext(), tripid);
		t.populateDetails();
		return t;
	}

	public TripData(Context ctx, long tripid) {
		Context context = ctx.getApplicationContext();
		this.tripid = tripid;
		mDb = new DbAdapter(context);
	}

	void initializeData() {
		startTime = System.currentTimeMillis();
		endTime = System.currentTimeMillis();
		numpoints = 0;
		latestlat = 800;
		latestlgt = 800;
		distance = 0;

		lathigh = (-100 * 1E6);
		latlow = (100 * 1E6);
		lgtlow = (180 * 1E6);
		lgthigh = (-180 * 1E6);
		tripPurpose = fancystart = info = "";

		updateTrip();
	}

	public List<RunningPoint> getPoints() {
		return points;
	}

	public String getTripPurpose() {
		return tripPurpose;
	}

	// Get lat/long extremes, etc, from trip record
	void populateDetails() {

		mDb.openReadOnly();

		Cursor tripdetails = mDb.fetchTrip(tripid);
		startTime = tripdetails.getLong(tripdetails.getColumnIndex("start"));
		lathigh = tripdetails.getDouble(tripdetails.getColumnIndex("lathi"));
		latlow = tripdetails.getDouble(tripdetails.getColumnIndex("latlo"));
		lgthigh = tripdetails.getDouble(tripdetails.getColumnIndex("lgthi"));
		lgtlow = tripdetails.getDouble(tripdetails.getColumnIndex("lgtlo"));
		status = tripdetails.getInt(tripdetails.getColumnIndex("status"));
		endTime = tripdetails.getLong(tripdetails.getColumnIndex("endtime"));
		distance = tripdetails.getFloat(tripdetails.getColumnIndex("distance"));

		tripPurpose = tripdetails.getString(tripdetails.getColumnIndex("purp"));
		fancystart = tripdetails.getString(tripdetails
				.getColumnIndex("fancystart"));
		info = tripdetails.getString(tripdetails.getColumnIndex("fancyinfo"));

		tripdetails.close();

		Cursor pointsCursor = mDb.fetchAllCoordsForTrip(tripid);
		if (pointsCursor != null) {
			numpoints = pointsCursor.getCount();
			points = new ArrayList<RunningPoint>();
			while (pointsCursor.moveToNext()) {
				RunningPoint point = new RunningPoint();
				// Extract point information.
				double lat = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_LAT));
				double lng = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_LGT));
				double accuracy = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_ACC));
				double altitude = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_ALT));
				double speed = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_SPEED));
				double time = pointsCursor.getDouble(pointsCursor
						.getColumnIndex(DbAdapter.K_POINT_TIME));
				// Set information
				point.latLng = new LatLng(lat, lng);
				point.accuracy = accuracy;
				point.altitude = altitude;
				point.speed = speed;
				point.time = time;
				points.add(point);
			}

			pointsCursor.close();
		}

		mDb.close();
	}

	void createTripInDatabase(Context c) {
		mDb.open();
		tripid = mDb.createTrip();
		mDb.close();
	}

	void dropTrip() {
		mDb.open();
		mDb.deleteAllCoordsForTrip(tripid);
		mDb.deleteTrip(tripid);
		mDb.close();
	}

	// public ItemizedOverlayTrack getPoints(Drawable d) {
	// // If already built, don't build again!
	// if (gpspoints != null && gpspoints.size()>0) {
	// return gpspoints;
	// }
	//
	// // Otherwise, we need to query DB and build points from scratch.
	// gpspoints = new ItemizedOverlayTrack(d);
	//
	// try {
	// mDb.openReadOnly();
	//
	// Cursor points = mDb.fetchAllCoordsForTrip(tripid);
	// int COL_LAT = points.getColumnIndex("lat");
	// int COL_LGT = points.getColumnIndex("lgt");
	// int COL_TIME = points.getColumnIndex("time");
	// int COL_ACC = points.getColumnIndex(DbAdapter.K_POINT_ACC);
	// int COL_SPD = points.getColumnIndex(DbAdapter.K_POINT_SPEED);
	//
	// numpoints = points.getCount();
	//
	// points.moveToLast();
	// this.endpoint = new RunningPoint(points.getDouble(COL_LAT),
	// points.getDouble(COL_LGT), points.getDouble(COL_TIME));
	//
	// points.moveToFirst();
	// this.startpoint = new RunningPoint(points.getInt(COL_LAT),
	// points.getInt(COL_LGT), points.getDouble(COL_TIME));
	//
	// while (!points.isAfterLast()) {
	// int lat = points.getInt(COL_LAT);
	// int lgt = points.getInt(COL_LGT);
	// double time = points.getDouble(COL_TIME);
	// float acc = (float) points.getDouble(COL_ACC);
	// float spd = (float) points.getDouble(COL_SPD);
	// addPointToSavedMap(lat, lgt, time, acc , spd);
	// points.moveToNext();
	// }
	// points.close();
	// mDb.close();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// gpspoints.repopulate();
	//
	// return gpspoints;
	// }

	// private void addPointToSavedMap(int lat, int lgt, double currentTime,
	// float acc, float spd) {
	// RunningPoint pt = new RunningPoint(lat, lgt, currentTime, acc , spd);
	//
	// OverlayItem opoint = new OverlayItem(pt, null, null);
	// gpspoints.addOverlay(opoint);
	// }

	boolean addPointNow(Location loc, long currentTime, float dst) {
		double lat = loc.getLatitude();
		double lgt = loc.getLongitude();

		// Skip duplicates
		if (latestlat == lat && latestlgt == lgt)
			return true;

		double accuracy = loc.getAccuracy();
		double altitude = loc.getAltitude();
		double speed = loc.getSpeed();

		RunningPoint pt = new RunningPoint(lat, lgt, currentTime, accuracy,
				altitude, speed);

		numpoints++;
		endTime = currentTime - this.totalPauseTime;
		distance = dst;

		latlow = Math.min(latlow, lat);
		lathigh = Math.max(lathigh, lat);
		lgtlow = Math.min(lgtlow, lgt);
		lgthigh = Math.max(lgthigh, lgt);

		latestlat = lat;
		latestlgt = lgt;

		mDb.open();
		boolean rtn = mDb.addCoordToTrip(tripid, pt);
		rtn = rtn
				&& mDb.updateTrip(tripid, "", startTime, "", "", "", lathigh,
						latlow, lgthigh, lgtlow, distance);
		mDb.close();

		return rtn;
	}

	public boolean updateTripStatus(int tripStatus) {
		boolean rtn;
		mDb.open();
		rtn = mDb.updateTripStatus(tripid, tripStatus);
		mDb.close();
		return rtn;
	}

	public boolean getStatus(int tripStatus) {
		boolean rtn;
		mDb.open();
		rtn = mDb.updateTripStatus(tripid, tripStatus);
		mDb.close();
		return rtn;
	}

	public void updateTrip() {
		updateTrip("", "", "", "");
	}

	public void updateTrip(String purpose, String fancyStart, String fancyInfo,
			String notes) {
		// Save the trip details to the phone database. W00t!
		mDb.open();
		mDb.updateTrip(tripid, purpose, startTime, fancyStart, fancyInfo,
				notes, lathigh, latlow, lgthigh, lgtlow, distance);
		mDb.close();
	}
}
