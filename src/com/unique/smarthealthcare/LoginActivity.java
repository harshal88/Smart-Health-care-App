package com.unique.smarthealthcare;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

public class LoginActivity extends Activity {

	private LoginDataBaseAdapter loginDataBaseAdapter;
	private Button login;
	private Button registerr;
	private EditText enterpassword;
	private TextView forgetpass;
	private SharedPreferences sharedPreference;
	private ImageView mRunningAthlete;
	private AnimationDrawable animationDrawable = null;
	public static final String PASSWORD_PREF = "PASSWORD";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		login = (Button) findViewById(R.id.login_btn);
		registerr = (Button) findViewById(R.id.reg_btn);
		enterpassword = (EditText) findViewById(R.id.enterpassword);
		forgetpass = (TextView) findViewById(R.id.forget_password_text_view);
		mRunningAthlete = (ImageView) findViewById(R.id.running_athlete);
		sharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
		loginDataBaseAdapter = new LoginDataBaseAdapter(getApplicationContext());
		registerr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginActivity.this,
						RegisterActivity.class);
				startActivity(i);
			}
		});
		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						login();
					}
				}).start();
			}
		});
		forgetpass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(LoginActivity.this);
				dialog.getWindow();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.forget_search);
				dialog.show();
				final EditText security = (EditText) dialog
						.findViewById(R.id.securityhint_edt);
				final TextView getpass = (TextView) dialog
						.findViewById(R.id.textView3);
				Button ok = (Button) dialog.findViewById(R.id.getpassword_btn);
				Button cancel = (Button) dialog.findViewById(R.id.cancel_btn);
				ok.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						String securityHint = security.getText().toString();
						if (securityHint.equals("")) {
							Toast.makeText(getApplicationContext(),
									"Please, enter your security hint",
									Toast.LENGTH_SHORT).show();
						} else {
							String storedPassword;
							storedPassword = loginDataBaseAdapter.getAllTags(securityHint);
							if (storedPassword == null) {
								Toast.makeText(getApplicationContext(),
										"Please, enter correct security hint",
										Toast.LENGTH_SHORT).show();
							} else {
								Log.d("GET PASSWORD", storedPassword);
								getpass.setText(storedPassword);
							}
						}
					}
				});
				cancel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}
		});
		// Attempt automatic login
		if (sharedPreference.contains(PASSWORD_PREF)) {
			enterpassword
					.setText(sharedPreference.getString(PASSWORD_PREF, ""));
			login();
		}
	}

	private void login() {
		reportEvent();
		String Password = enterpassword.getText().toString();
		loginDataBaseAdapter.open();
		String storedPassword = loginDataBaseAdapter.getSinlgeEntry(Password);
//Error		sharedPreference.edit().putString(PASSWORD_PREF, storedPassword)
//				.commit();
		if (Password.equals(storedPassword)) {
			displayToastMessage("Congrats: login is successful",
					Toast.LENGTH_LONG);
			sharedPreference.edit().putString(PASSWORD_PREF, storedPassword)
			.commit();
			Intent ii = new Intent(LoginActivity.this, HomeActivity.class);
			startActivity(ii);
			finish();
		} else if (Password.equals("")) {
			displayToastMessage("Please, enter your password", Toast.LENGTH_LONG);
		} else {

			displayToastMessage("Incorrect password", Toast.LENGTH_LONG);
		}
		loginDataBaseAdapter.close();
	}
	
	private void reportEvent(){
		Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
	            TrackerName.APP_TRACKER);
	        // Build and send an Event.
	        t.send(new HitBuilders.EventBuilder()
	            .setCategory("ACTIONS")
	            .setAction("LOGIN")
	            .setLabel("Login")
	            .build());
	}

	private void displayToastMessage(final String message, final int duration) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(LoginActivity.this, message, duration).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		animationDrawable = (AnimationDrawable) getResources().getDrawable(
				R.drawable.athlete);
		mRunningAthlete.setBackground(animationDrawable);
		animationDrawable.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		animationDrawable.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Close The Database
		
	}

}