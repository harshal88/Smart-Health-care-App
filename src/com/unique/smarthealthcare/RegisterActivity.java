package com.unique.smarthealthcare;

import com.google.android.gms.analytics.GoogleAnalytics;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RegisterActivity extends Activity {

	private LoginDataBaseAdapter loginDataBaseAdapter;
	private EditText password, repassword, securityhint;
	private Button register, reg_btn;
	private CheckBox check;
	private ImageView mRunningAthlete;
	private AnimationDrawable animationDrawable = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		loginDataBaseAdapter = new LoginDataBaseAdapter(this);
		loginDataBaseAdapter = loginDataBaseAdapter.open();
		mRunningAthlete = (ImageView) findViewById(R.id.running_athlete);
		password = (EditText) findViewById(R.id.password_edt);
		repassword = (EditText) findViewById(R.id.repassword_edt);
		securityhint = (EditText) findViewById(R.id.security_edt);
		register = (Button) findViewById(R.id.reg_btn);
		check = (CheckBox) findViewById(R.id.checkBox1);

		check.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub

				if (!isChecked) {
					password.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
					repassword.setTransformationMethod(PasswordTransformationMethod
							.getInstance());
				} else {
					password.setTransformationMethod(HideReturnsTransformationMethod
							.getInstance());
					repassword.setTransformationMethod(HideReturnsTransformationMethod
							.getInstance());
				}
			}
		});

		register.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				String Pass = password.getText().toString();
				String Secu = securityhint.getText().toString();
				String Repass = repassword.getText().toString();

				if (Pass.equals("") || Repass.equals("") || Secu.equals("")) {
					Toast.makeText(getApplicationContext(), "Please, fill all fields",
							Toast.LENGTH_LONG).show();
					return;
				}

				if (!Pass.equals(Repass)) {
					Toast.makeText(getApplicationContext(),
							"Passwords do not match", Toast.LENGTH_LONG)
							.show();
					return;
				} else {
					// Save the Data in Database
					loginDataBaseAdapter.insertEntry(Pass, Secu);

					// reg_btn.setVisibility(View.GONE);
					Toast.makeText(getApplicationContext(),
							"Account Successfully Created ", Toast.LENGTH_LONG)
							.show();
					Log.d("PASSWORD", Pass);
					Log.d("RE PASSWORD", Repass);
					Log.d("SECURITY HINT", Secu);
					Intent i = new Intent(RegisterActivity.this,
							LoginActivity.class);
					startActivity(i);
					finish();
				}
			}
		});
		((SmartHealthCareApplication) getApplication())
				.getTracker(SmartHealthCareApplication.TrackerName.APP_TRACKER);
	}

	@Override
	protected void onStart() {
		super.onStart();
		GoogleAnalytics.getInstance(this).reportActivityStart(this);

	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
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
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
	}
}