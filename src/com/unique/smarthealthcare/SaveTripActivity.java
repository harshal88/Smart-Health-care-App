package com.unique.smarthealthcare;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Map.Entry;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SaveTripActivity extends Activity {
	long tripid;
	HashMap<Integer, ToggleButton> purposeButtons = new HashMap<Integer, ToggleButton>();
	String purpose = "";

	HashMap<Integer, String> purposeDescriptions = new HashMap<Integer, String>();

	// Set up the purpose buttons to be one-click only
	void preparePurposeButtons() {
		purposeButtons.put(R.id.ToggleCommute,
				(ToggleButton) findViewById(R.id.ToggleCommute));
		purposeButtons.put(R.id.ToggleSchool,
				(ToggleButton) findViewById(R.id.ToggleSchool));
		purposeButtons.put(R.id.ToggleWorkRel,
				(ToggleButton) findViewById(R.id.ToggleWorkRel));
		purposeButtons.put(R.id.ToggleExercise,
				(ToggleButton) findViewById(R.id.ToggleExercise));
		purposeButtons.put(R.id.ToggleSocial,
				(ToggleButton) findViewById(R.id.ToggleSocial));
		purposeButtons.put(R.id.ToggleShopping,
				(ToggleButton) findViewById(R.id.ToggleShopping));
		purposeButtons.put(R.id.ToggleErrand,
				(ToggleButton) findViewById(R.id.ToggleErrand));
		purposeButtons.put(R.id.ToggleOther,
				(ToggleButton) findViewById(R.id.ToggleOther));

		purposeDescriptions
				.put(R.id.ToggleCommute,
						"<b>Commute:</b> this Running trip was primarily to get between home and your main workplace.");
		purposeDescriptions
				.put(R.id.ToggleSchool,
						"<b>School:</b> this Running trip was primarily to go to or from school or college.");
		purposeDescriptions
				.put(R.id.ToggleWorkRel,
						"<b>Work-Related:</b> this Running trip was primarily to go to or from a business related meeting, function, or work-related errand for your job.");
		purposeDescriptions
				.put(R.id.ToggleExercise,
						"<b>Exercise:</b> this Running trip was primarily for exercise, or biking for the sake of biking.");
		purposeDescriptions
				.put(R.id.ToggleSocial,
						"<b>Social:</b> this Running trip was primarily for going to or from a social activity, e.g. at a friend's house, the park, a restaurant, the movies.");
		purposeDescriptions
				.put(R.id.ToggleShopping,
						"<b>Shopping:</b> this Running trip was primarily to purchase or bring home goods or groceries.");
		purposeDescriptions
				.put(R.id.ToggleErrand,
						"<b>Errand:</b> this Running trip was primarily to attend to personal business such as banking, a doctor  visit, going to the gym, etc.");
		purposeDescriptions
				.put(R.id.ToggleOther,
						"<b>Other:</b> if none of the other reasons applied to this trip, you can enter comments below to tell us more.");

		CheckListener cl = new CheckListener();
		for (Entry<Integer, ToggleButton> e : purposeButtons.entrySet()) {
			e.getValue().setOnCheckedChangeListener(cl);
		}
	}

	// Called every time a purpose togglebutton is changed:
	class CheckListener implements CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton v, boolean isChecked) {
			// First, uncheck all purpose buttons
			if (isChecked) {

				for (Entry<Integer, ToggleButton> e : purposeButtons.entrySet()) {
					e.getValue().setChecked(false);
				}
				v.setChecked(true);
				purpose = v.getText().toString();
				((TextView) findViewById(R.id.TextPurpDescription))
						.setText(Html.fromHtml(purposeDescriptions.get(v
								.getId())));
				// Report Google analytics
				Tracker t = ((SmartHealthCareApplication) getApplication())
						.getTracker(TrackerName.APP_TRACKER);
				// Build and send an Event.
				t.send(new HitBuilders.EventBuilder().setCategory("ACTIONS")
						.setAction("CHECK_TRIP_PURPOSE")
						.setLabel("Trip Purpose #"+purpose).build());
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);

		finishRecording();

		// Set up trip purpose buttons
		purpose = "";
		preparePurposeButtons();

		// User prefs btn
		final Button prefsButton = (Button) findViewById(R.id.ButtonPrefs);
		final Intent pi = new Intent(this, UserPrefActivity.class);
		prefsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(pi);
			}
		});

		SharedPreferences settings = getSharedPreferences("PREFS", 0);
		if (settings.getAll().size() >= 1) {
			prefsButton.setVisibility(View.GONE);
		}

		// Discard btn
		final Button btnDiscard = (Button) findViewById(R.id.ButtonDiscard);
		btnDiscard.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Trip discarded",
						Toast.LENGTH_SHORT).show();

				cancelRecording();

				Intent i = new Intent(SaveTripActivity.this, HomeActivity.class);
				i.putExtra("keepme", true);
				startActivity(i);
				SaveTripActivity.this.finish();
			}
		});

		// Submit btn
		final Button btnSubmit = (Button) findViewById(R.id.ButtonSubmit);
		btnSubmit.setEnabled(false);

		// Don't pop up the soft keyboard until user clicks!
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	// submit btn is only activated after the service.finishedRecording() is
	// completed.
	void activateSubmitButton() {
		final Button btnSubmit = (Button) findViewById(R.id.ButtonSubmit);
		final Intent xi = new Intent(this, ShowMapActivity.class);
		btnSubmit.setEnabled(true);

		btnSubmit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				TripData trip = TripData.fetchTrip(SaveTripActivity.this,
						tripid);
				trip.populateDetails();

				// Make sure trip purpose has been selected
				if (purpose.equals("")) {
					// Oh no! No trip purpose!
					Toast.makeText(
							getBaseContext(),
							"You must select a Running trip purpose before submitting! Choose from the purposes above.",
							Toast.LENGTH_SHORT).show();
					return;
				}

				EditText notes = (EditText) findViewById(R.id.NotesField);

				String fancyStartTime = DateFormat.getInstance().format(
						trip.startTime);

				// "3.5 miles in 26 minutes"
				SimpleDateFormat sdf = new SimpleDateFormat("m");
				sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
				String minutes = sdf.format(trip.endTime - trip.startTime);
				String fancyEndInfo = String.format("%1.1f miles, %s minutes",
						(0.0006212f * trip.distance), minutes);

				// Save the trip details to the phone database. W00t!
				trip.updateTrip(purpose, fancyStartTime, fancyEndInfo, notes
						.getEditableText().toString());
				trip.updateTripStatus(TripData.STATUS_COMPLETE);
				resetService();

				// Force-drop the soft keyboard for performance
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

				// // Now create the MainInput Activity so BACK btn works
				// properly
				// Intent i = new Intent(getApplicationContext(),
				// MainInput.class);
				// startActivity(i);

				// And, show the map!
				xi.putExtra("showtrip", trip.tripid);
				xi.putExtra("uploadTrip", true);
				startActivity(xi);
				SaveTripActivity.this.finish();
			}
		});

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

	void resetService() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.reset();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}

	void finishRecording() {
		Intent rService = new Intent(this, RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				tripid = rs.finishRecording();
				SaveTripActivity.this.activateSubmitButton();
				unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		bindService(rService, sc, Context.BIND_AUTO_CREATE);
	}
}
