package com.unique.smarthealthcare;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartTripActivity extends Activity {

	private AnimationDrawable animationDrawable = null;
	private Button mStartTripBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_trip);
		mStartTripBtn = (Button) findViewById(R.id.ButtonStart);
		mStartTripBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startNewTrip();
			}
		});
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				int state = rs.getState();
				if (state > RecordingService.STATE_IDLE) {
					if (state == RecordingService.STATE_FULL) {
						startActivity(new Intent(StartTripActivity.this,
								SaveTripActivity.class));
					} else { // RECORDING OR PAUSED:
						startActivity(new Intent(StartTripActivity.this,
								RecordingActivity.class));
					}
					StartTripActivity.this.finish();
				} else {
					// Idle. First run? Switch to user prefs screen if there are
					// no prefs stored yet
					SharedPreferences settings = 
							getSharedPreferences("PREFS", 0);
					if (settings.getAll().isEmpty()) {
						showWelcomeDialog();
					}
					// // Not first run - set up the list view of saved trips
					// ListView listSavedTrips = (ListView)
					// findViewById(R.id.ListSavedTrips);
					// populateList(listSavedTrips);
				}
				unbindService(this); // race? this says we no
													// longer care
			}
		};
		// This needs to block until the onServiceConnected (above) completes.
		// Thus, we can check the recording status before continuing on.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		animationDrawable = (AnimationDrawable) getResources().getDrawable(
				R.drawable.athlete);
		mStartTripBtn.setCompoundDrawablesWithIntrinsicBounds(
				animationDrawable, null, null, null);
		animationDrawable.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		animationDrawable.stop();
		for (int i = 0; i < animationDrawable.getNumberOfFrames(); ++i) {
			Drawable frame = animationDrawable.getFrame(i);
			if (frame instanceof BitmapDrawable) {
				Bitmap bitmap = ((BitmapDrawable) frame).getBitmap();
				if (bitmap != null && bitmap.isRecycled()) {
					bitmap.recycle();
					bitmap = null;
				}
			}
			frame.setCallback(null);
		}
		animationDrawable.setCallback(null);
		animationDrawable = null;
	}
	private void startNewTrip() {
		final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			buildAlertMessageNoGps();
		} else {
			//Report to google analytics
			Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
		            TrackerName.APP_TRACKER);
		        // Build and send an Event.
		        t.send(new HitBuilders.EventBuilder()
		            .setCategory("ACTIONS")
		            .setAction("START_TRIP")
		            .setLabel("Start New Trip")
		            .build());
			final Intent i = new Intent(this, RecordingActivity.class);
			startActivity(i);
		}

	}

	private void buildAlertMessageNoGps() {
		final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(
				"Your phone's GPS is disabled. Smart Health Care needs GPS to determine your location.\n\nGo to System Settings now to enable GPS?")
				.setCancelable(false)
				.setPositiveButton("GPS Settings...",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								final Intent intent = new Intent(
										action);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, 0);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void showWelcomeDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Please enter your personal details ")
				.setCancelable(false).setTitle("Welcome to Running Tracks!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						startActivity(new Intent(StartTripActivity.this,
								UserPrefActivity.class));
					}
				});

		final AlertDialog alert = builder.create();
		alert.show();
	}

}
