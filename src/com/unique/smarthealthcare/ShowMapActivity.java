package com.unique.smarthealthcare;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ShowMapActivity extends Activity {

	DbAdapter tripcords;

	private MapView mapView;
	private GoogleMap mGoogleMaps;
	private MapFragment mMapFragment;
	private TextView tripPurpose;
	private TextView tripDate;
	private TextView tripDistance;
	private TextView tripDuration;

	// List<Overlay> mapOverlays;
	// Drawable drawable;
	// ItemizedOverlayTrack gpspoints;
	// float[] lineCoords;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		tripPurpose = (TextView) findViewById(R.id.purpose_of_trip);
		tripDate = (TextView) findViewById(R.id.trip_date);
		tripDistance = (TextView) findViewById(R.id.trip_distance);
		tripDuration = (TextView) findViewById(R.id.trip_duration);
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mGoogleMaps = mMapFragment.getMap();
		mGoogleMaps.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		mGoogleMaps.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				setUpMapIfNeeded();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setUpMapIfNeeded() {

		Bundle cmds = getIntent().getExtras();

		long tripid = cmds.getLong("showtrip");

		TripData trip = TripData.fetchTrip(this, tripid);
		tripPurpose.setText(trip.tripPurpose);
		tripDate.setText(trip.fancystart);
		tripDistance.setText(trip.distance + " meters                ");
		tripDuration.setText(trip.info);
		// float latcenter = (float) (trip.lathigh + trip.latlow);
		// float lgtcenter = (float) (trip.lgthigh + trip.lgtlow);

		// LatLng center = new LatLng(latcenter, lgtcenter);
		// LatLng zoom = new LatLng(500+trip.lathigh - trip.latlow,
		// 500+trip.lgthigh - trip.lgtlow);
		if (trip.getPoints() != null && trip.getPoints().size() > 1) {
			for (int i = 1; i < trip.getPoints().size(); i++) {
				RunningPoint previousPoint = trip.getPoints().get(i - 1);
				RunningPoint currentPoint = trip.getPoints().get(i);

				int routeColor = Color.GREEN;
				if (currentPoint.speed <= 2) {
					routeColor = Color.GREEN;
				} else if (currentPoint.speed > 2 && currentPoint.speed <= 3) {
					routeColor = Color.YELLOW;
				} else if (currentPoint.speed > 3 && currentPoint.speed <= 4) {
					routeColor = Color.RED;
				} else if (currentPoint.speed > 4 && currentPoint.speed <= 5) {
					routeColor = Color.CYAN;
				} else if (currentPoint.speed > 5) {
					routeColor = Color.MAGENTA;
				}

				mGoogleMaps.addPolyline(
						new PolylineOptions().add(previousPoint.latLng,
								currentPoint.latLng).width(10)).setColor(
						routeColor);
			}
			// Set the start and finish markets
			RunningPoint startPoint = trip.getPoints().get(0);
			RunningPoint finishPoint = trip.getPoints().get(
					trip.getPoints().size() - 1);
			mGoogleMaps.addMarker(new MarkerOptions()
					.position(startPoint.latLng)
					.title("Starting Point")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.pingreen)));
			mGoogleMaps.addMarker(new MarkerOptions()
					.position(finishPoint.latLng)
					.title("Finish Point")
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.pinpurple)));

			// Make the map fit to include the start and finish points.

			LatLngBounds mapBoundary = new LatLngBounds.Builder()
					.include(startPoint.latLng).include(finishPoint.latLng)
					.build();

			mGoogleMaps.animateCamera(CameraUpdateFactory.newLatLngBounds(
					mapBoundary, 20));
		}

	}

	// Make sure overlays get zapped when we go BACK
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mapView != null) {
			mGoogleMaps.clear();
		}
		return super.onKeyDown(keyCode, event);
	}
}
