package com.unique.smarthealthcare;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

public class HomeActivity extends ActionBarActivity implements ChangePasswordListener {

	private String[] mDrawerTitles = new String[] { "Home", "List Tracks",
			"Graphs", "Compass", "Stop Watch", "BMI" };
	private int[] mDrawerIcons = new int[] { R.drawable.home_icon, R.drawable.history_icon,
			R.drawable.graphs, R.drawable.compass,R.drawable.stop_watch_icon, R.drawable.bmi };
	private List<HashMap<String, String>> mList ;
	private SimpleAdapter mDrawerAdapter;
	private final String ITEM="item";
	private final String ICON="icon";
	private Fragment[] fragments = new Fragment[] { new HomeFragment(),
			new ListTrackFragment(), new GraphFragments(),
			new CompassFragmentActivity(), new StopwatchFragmentActivity(), new BMIFragmentActivity(),
			 };
	public static final int HOME_FRAGMENT_INDEX = 0;
	public static final int HISTORY_FRAGMENT_INDEX = 1;
	public static final int GRAPH_FRAGMENT_INDEX = 2;
	public static final int COMPASS_FRAGMENT_INDEX = 3;
	public static final int STOP_WATCH_FRAGMENT_INDEX = 4;
	public static final int BMI_FRAGMENT_INDEX = 5;
	private int activeFragmentIndex = 0;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private boolean doubleBackToExitPressedOnce = false;
	private final static int MENU_USER_INFO = 0;
	private final static int MENU_HELP = 1;
	private final static int MENU_CHANGE_PASSWORD = 2;
	private final static int MENU_SIGNOUT = 3;
	private static final String SCREEN_NAME = "HOME_SCREEN";
	LoginDataBaseAdapter loginDataBaseAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// Send Home Screen View to google analytics
		setContentView(R.layout.activity_home);
		mList = new ArrayList<HashMap<String,String>>();
		for(int i = 0 ; i < mDrawerTitles.length ; i++){
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put(ITEM, mDrawerTitles[i]);
			hashMap.put(ICON, Integer.toString(mDrawerIcons[i]));
			mList.add(hashMap);
		}
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setBackgroundColor(Color.WHITE);
		String[] from = {ICON, ITEM};
		int[] to = {R.id.drawer_item_icon, android.R.id.text1};
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerAdapter = new SimpleAdapter(this, mList, R.layout.drawer_list_item, from, to);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(mDrawerAdapter);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		// enable ActionBar app icon to behave as action to toggle nav drawer
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
		((SmartHealthCareApplication) getApplication()).getTracker(SmartHealthCareApplication.TrackerName.APP_TRACKER);

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
	public void selectItem(int position) {
		if (position <= mDrawerTitles.length) {
			// update the main content by replacing fragments
			Fragment fragment = fragments[position];

			FragmentManager fragmentManager = getSupportFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();
			activeFragmentIndex = position;
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mDrawerTitles[position]);
		}
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_HELP, 0, "Help and FAQ").setIcon(
				android.R.drawable.ic_menu_help);
		menu.add(0, MENU_USER_INFO, 0, "Edit User Info").setIcon(
				android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_CHANGE_PASSWORD, 0, "Change Password").setIcon(
				android.R.drawable.ic_menu_edit);
		menu.add(0, MENU_SIGNOUT, 0, "Signout").setIcon(
				android.R.drawable.ic_menu_edit);
		return true;
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case MENU_USER_INFO:
			startActivity(new Intent(this, UserPrefActivity.class));
			return true;
		case MENU_HELP:
			Intent myIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("https://www.facebook.com/harshal.jaspal"));
			startActivity(myIntent);
			return true;
		case MENU_CHANGE_PASSWORD:
			showChangePasswordDialog();
			return true;
		case MENU_SIGNOUT:
			Intent intent = new Intent(this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			// Clear the credentials in the preference so as not to auto login
			// automatically
			PreferenceManager.getDefaultSharedPreferences(this).edit().clear()
					.commit();
			startActivity(intent);
			finish();
			return true;
			
		}
		return false;
	}

	private void exportDB() {
		try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/" + getPackageName() + "/databases/data";
                String backupDBPath = "smart_health_care_db.db";
                File currentDB = new File(currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// If the current fragment is not home, revert back to home fragment
			if (activeFragmentIndex != HOME_FRAGMENT_INDEX) {
				selectItem(HOME_FRAGMENT_INDEX);
			} else {
				/**
				 * Backward navigation for tabs
				 */
				if (doubleBackToExitPressedOnce) {
					super.onBackPressed();
					return;
				}
				this.doubleBackToExitPressedOnce = true;
				Toast.makeText(this, R.string.please_click_back_again_to_exit,
						Toast.LENGTH_SHORT).show();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						doubleBackToExitPressedOnce = false;
					}
				}, 2000);
			}
		}
	}
	private void showChangePasswordDialog() {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
		changePasswordFragment.setChangePasswordListener(this);
		changePasswordFragment.show(fm, "fragment_change_password");
	}

	public void showHistory(View view) {
		//Report to google analytics
				Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
			            TrackerName.APP_TRACKER);
			        // Build and send an Event.
			        t.send(new HitBuilders.EventBuilder()
			            .setCategory("ACTIONS")
			            .setAction("CHECK_HISTORY")
			            .setLabel("Check History")
			            .build());
		selectItem(HomeActivity.HISTORY_FRAGMENT_INDEX);
	}
	public void showGraphs(View view) {
		//Report to google analytics
				Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
			            TrackerName.APP_TRACKER);
			        // Build and send an Event.
			        t.send(new HitBuilders.EventBuilder()
			            .setCategory("ACTIONS")
			            .setAction("CHECK_GRAPHS")
			            .setLabel("Check Graphs")
			            .build());
		selectItem(HomeActivity.GRAPH_FRAGMENT_INDEX);
	}
	public void showCompass(View view) {
		//Report to google analytics
		Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
	            TrackerName.APP_TRACKER);
	        // Build and send an Event.
	        t.send(new HitBuilders.EventBuilder()
	            .setCategory("ACTIONS")
	            .setAction("CHECK_COMPASS")
	            .setLabel("Check Compass")
	            .build());
		selectItem(HomeActivity.COMPASS_FRAGMENT_INDEX);
	}
	public void showBMI(View view) {
		//Report to google analytics
				Tracker t = ((SmartHealthCareApplication)getApplication()).getTracker(
			            TrackerName.APP_TRACKER);
			        // Build and send an Event.
			        t.send(new HitBuilders.EventBuilder()
			            .setCategory("ACTIONS")
			            .setAction("CHECK_BMI")
			            .setLabel("Check BMI")
			            .build());
		selectItem(HomeActivity.BMI_FRAGMENT_INDEX);
	}
	@Override
	public void changePassword(String currentPassword, String newPassword) {
		loginDataBaseAdapter = new LoginDataBaseAdapter(this);
		loginDataBaseAdapter.open();
		String storedPassword = loginDataBaseAdapter.getSinlgeEntry(currentPassword);
		if("NOT EXIST".equalsIgnoreCase(storedPassword )){
			Toast.makeText(this, "Wrong Password !", Toast.LENGTH_LONG).show();
			loginDataBaseAdapter.close();
			return;
		}
		int rowsAffected = loginDataBaseAdapter.updateEntry(currentPassword, newPassword);
		if(rowsAffected > 0){
			SharedPreferences sharedPreference =PreferenceManager.getDefaultSharedPreferences(this); 
			
			sharedPreference.edit().putString(LoginActivity.PASSWORD_PREF, newPassword)
					.commit();
			Toast.makeText(this, "Password Reset Succeeded", Toast.LENGTH_LONG).show();
		}else{
			Toast.makeText(this, "Wrong Password !", Toast.LENGTH_LONG).show();
		}
		
		loginDataBaseAdapter.close();
		
	}

}