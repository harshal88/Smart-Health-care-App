package com.unique.smarthealthcare;

import com.unique.smarthealthcare.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

public class Splashscreen extends Activity {

	private AnimationDrawable animationDrawable = null;
	private ImageView mRunningAthlete;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splashscreen);
		mRunningAthlete = (ImageView) findViewById(R.id.running_athlete);
		MediaPlayer oursong = MediaPlayer.create(Splashscreen.this,
				R.raw.sisfus);
		oursong.start();
		Thread timer = new Thread() {
			public void run() {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					Intent loginscreen = new Intent(
							"com.unique.smarthealthcare.MAINACTIVITY");
					startActivity(loginscreen);
				}
			}
		};
		timer.start();
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
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
