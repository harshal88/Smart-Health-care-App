package com.unique.smarthealthcare;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

public class RecordingActivity extends FragmentActivity {
	Intent fi;
	TripData trip;
	boolean isRecording = false;
	Button pauseButton;
	Button finishButton;
	Timer timer;
	float curDistance;

	TextView txtStat;
	TextView txtDistance;
	TextView txtDuration;
	TextView txtCurSpeed;
	TextView txtMaxSpeed;
	TextView txtAvgSpeed;
	private ToggleButton showMap;
	private ToggleButton showStats;
	private GoogleMap mMap;
	private LatLng routeLastPoint;
	LatLngBounds routeBoundary;
	private MapFragment mMapFragment;
	private ScrollView mStatLayout;
	final SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss", Locale.getDefault());

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	final Runnable mUpdateTimer = new Runnable() {
		public void run() {
			updateTimer();
		}
	};
	private final int MAP_ZOOM_LEVEL = 18;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_recording);
		((SmartHealthCareApplication) getApplication()).getTracker(SmartHealthCareApplication.TrackerName.APP_TRACKER);
		txtStat = (TextView) findViewById(R.id.TextRecordStats);
		txtDistance = (TextView) findViewById(R.id.TextDistance);
		txtDuration = (TextView) findViewById(R.id.TextDuration);
		txtCurSpeed = (TextView) findViewById(R.id.TextSpeed);
		txtMaxSpeed = (TextView) findViewById(R.id.TextMaxSpeed);
		txtAvgSpeed = (TextView) findViewById(R.id.TextAvgSpeed);

		pauseButton = (Button) findViewById(R.id.btn_pause);
		finishButton = (Button) findViewById(R.id.btn_finished);

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		showMap = (ToggleButton) findViewById(R.id.show_map);
		showStats = (ToggleButton) findViewById(R.id.show_stat);
		mStatLayout = (ScrollView) findViewById(R.id.stat_container);
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map);
		mMap = mMapFragment.getMap();
		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		showMap.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					//Report to google analytics
					Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
				            TrackerName.APP_TRACKER);
				        // Build and send an Event.
				        t.send(new HitBuilders.EventBuilder()
				            .setCategory("ACTIONS")
				            .setAction("SHOW_MAP")
				            .setLabel("Show Trip Map")
				            .build());
					mMapFragment.getView().setVisibility(View.VISIBLE);
				} else {
					//Report to google analytics
					Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
				            TrackerName.APP_TRACKER);
				        // Build and send an Event.
				        t.send(new HitBuilders.EventBuilder()
				            .setCategory("ACTIONS")
				            .setAction("HIDE_MAP")
				            .setLabel("Hide Trip Map")
				            .build());
					mMapFragment.getView().setVisibility(View.GONE);
				}

			}
		});

		showStats.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					//Report to google analytics
					Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
				            TrackerName.APP_TRACKER);
				        // Build and send an Event.
				        t.send(new HitBuilders.EventBuilder()
				            .setCategory("ACTIONS")
				            .setAction("SHOW_STATS")
				            .setLabel("Show Trip Statistics")
				            .build());
					mStatLayout.setVisibility(View.VISIBLE);
				} else {
					//Report to google analytics
					Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
				            TrackerName.APP_TRACKER);
				        // Build and send an Event.
				        t.send(new HitBuilders.EventBuilder()
				            .setCategory("ACTIONS")
				            .setAction("HIDE_STATS")
				            .setLabel("Hide Trip Statistics")
				            .build());
					mStatLayout.setVisibility(View.GONE);
				}
			}
		});

		// Query the RecordingService to figure out what to do.
		Intent rService = new Intent(this, RecordingService.class);
		startService(rService);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;

				switch (rs.getState()) {
				case RecordingService.STATE_IDLE:
					trip = TripData.createTrip(RecordingActivity.this);
					rs.startRecording(trip);
					isRecording = true;
					RecordingActivity.this.pauseButton.setEnabled(true);
					RecordingActivity.this
							.setTitle("Running Tracks - Recording...");
					break;
				case RecordingService.STATE_RECORDING:
					long id = rs.getCurrentTrip();
					trip = TripData.fetchTrip(RecordingActivity.this, id);
					isRecording = true;
					RecordingActivity.this.pauseButton.setEnabled(true);
					RecordingActivity.this
							.setTitle("Running Tracks - Recording...");
					break;
				case RecordingService.STATE_PAUSED:
					long tid = rs.getCurrentTrip();
					isRecording = false;
					trip = TripData.fetchTrip(RecordingActivity.this, tid);
					RecordingActivity.this.pauseButton.setEnabled(true);
					RecordingActivity.this.pauseButton.setText("Resume");
					RecordingActivity.this
							.setTitle("Running Tracks - Paused...");
					break;
				case RecordingService.STATE_FULL:
					// Should never get here, right?
					break;
				}
				rs.setListener(RecordingActivity.this);
				unbindService(this);
			}
		};
		bindService(rService, sc, Context.BIND_AUTO_CREATE);

		// Pause button
		pauseButton.setEnabled(false);
		pauseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				isRecording = !isRecording;
				if (isRecording) {
					pauseButton.setText("Pause");
					RecordingActivity.this
							.setTitle("Running Tracks - Recording...");
					// Don't include pause time in trip duration
					if (trip.pauseStartedAt > 0) {
						trip.totalPauseTime += (System.currentTimeMillis() - trip.pauseStartedAt);
						trip.pauseStartedAt = 0;
					}
					Toast.makeText(getBaseContext(),
							"GPS restarted. It may take a moment to resync.",
							Toast.LENGTH_LONG).show();
				} else {
					pauseButton.setText("Resume");
					RecordingActivity.this
							.setTitle("Running Tracks - Paused...");
					trip.pauseStartedAt = System.currentTimeMillis();
					Toast.makeText(getBaseContext(),
							"Recording paused; GPS now offline",
							Toast.LENGTH_LONG).show();
				}
				RecordingActivity.this.setListener();
			}
		});

		// Finish button
		finishButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// If we have points, go to the save-trip activity
				if (trip.numpoints > 0) {
					// Handle pause time gracefully
					if (trip.pauseStartedAt > 0) {
						trip.totalPauseTime += (System.currentTimeMillis() - trip.pauseStartedAt);
					}
					if (trip.totalPauseTime > 0) {
						trip.endTime = System.currentTimeMillis()
								- trip.totalPauseTime;
					}
					// Save trip so far (points and extent, but no purpose or
					// notes)
					fi = new Intent(RecordingActivity.this,
							SaveTripActivity.class);
					trip.updateTrip("", "", "", "");
				}
				// Otherwise, cancel and go back to main screen
				else {
					Toast.makeText(getBaseContext(),
							"No GPS data acquired; nothing to submit.",
							Toast.LENGTH_SHORT).show();

					cancelRecording();

					// Go back to main screen
					fi = new Intent(RecordingActivity.this, HomeActivity.class);
					fi.putExtra("keep", true);
				}

				// Either way, activate next task, and then kill this task
				startActivity(fi);
				RecordingActivity.this.finish();
			}
		});
		
	}

	public void updateStatus(int points, Location location, float distance,
			float spdCurrent, float spdMax) {
		this.curDistance = distance;
		if (points > 0) {
			txtStat.setText("" + points + " data points received...");
		} else {
			txtStat.setText("Waiting for GPS fix...");
		}
		txtCurSpeed.setText(String.format("%1.1f mph", spdCurrent));
		txtMaxSpeed.setText(String.format("%1.1f mph", spdMax));

		float miles = 0.0006212f * distance;
		txtDistance.setText(String.format("%1.1f miles", miles));
		LatLng point = new LatLng(location.getLatitude(),
				location.getLongitude());
		// If first point, draw the start marker on the map
		if (points == 1) {
			LatLng startPosition = point;
			mMap.addMarker(new MarkerOptions().position(startPosition).icon(
					BitmapDescriptorFactory.fromResource(R.drawable.pingreen)));
			routeBoundary = new LatLngBounds.Builder().include(startPosition)
					.build();
			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					routeBoundary, 10));
			routeLastPoint = startPosition;
		} else {

			int routeColor = Color.GREEN;
			if (spdCurrent <= 2) {
				routeColor = Color.GREEN;
			} else if (spdCurrent > 2 && spdCurrent <= 3) {
				routeColor = Color.YELLOW;
			} else if (spdCurrent > 3 && spdCurrent <= 4) {
				routeColor = Color.RED;
			} else if (spdCurrent > 4 && spdCurrent <= 5) {
				routeColor = Color.CYAN;
			} else if (spdCurrent > 5) {
				routeColor = Color.MAGENTA;
			}
			// Add the point as a polyline
			mMap.addPolyline(new PolylineOptions().add(routeLastPoint, point)
					.width(20).color(routeColor));
			// Adapt the route boundary on the map
			routeBoundary.including(point);

			mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
					routeBoundary, 10));
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point,
					MAP_ZOOM_LEVEL));
			routeLastPoint = point;
		}
	}

	void setListener() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				if (RecordingActivity.this.isRecording) {
					rs.resumeRecording();
				} else {
					rs.pauseRecording();
				}
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes, but
		// doesn't
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	void cancelRecording() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.cancelRecording();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	// onResume is called whenever this activity comes to foreground.
	// Use a timer to update the trip duration.
	@Override
	public void onResume() {
		super.onResume();

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(mUpdateTimer);
			}
		}, 0, 1000); // every second
	}

	void updateTimer() {
		if (trip != null && isRecording) {
			double dd = System.currentTimeMillis() - trip.startTime
					- trip.totalPauseTime;

			txtDuration.setText(sdf.format(dd));

			double avgSpeed = 3600.0 * 0.6212 * this.curDistance / dd;
			txtAvgSpeed.setText(String.format("%1.1f mph", avgSpeed));
		}
	}

	// Don't do pointless UI updates if the activity isn't being shown.
	@Override
	public void onPause() {
		super.onPause();
		if (timer != null)
			timer.cancel();
	}

	@Override
	public void onBackPressed() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				final IRecordService rs = (IRecordService) service;
				int recordingStatus = rs.getState();
				if (recordingStatus == RecordingService.STATE_RECORDING) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							RecordingActivity.this)
							.setMessage(
									"You have an active recording now. What do you want to do with it?")
							.setPositiveButton("Finish", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(
											RecordingActivity.this,
											SaveTripActivity.class);
									startActivity(intent);
									finish();

								}
							})
							.setNegativeButton("Cancel", new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(RecordingActivity.this,
											"Canceling the trips",
											Toast.LENGTH_LONG).show();
									rs.cancelRecording();
									RecordingActivity.super.onBackPressed();

								}
							})
							.setNeutralButton("Continue",
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).create();
					alertDialog.show();
					unbindService(this);
				} else {
					RecordingActivity.super.onBackPressed();
				}

			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);

	}
}
