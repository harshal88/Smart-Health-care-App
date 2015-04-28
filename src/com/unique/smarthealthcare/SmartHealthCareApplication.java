package com.unique.smarthealthcare;

import java.util.HashMap;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

public class SmartHealthCareApplication extends Application {

	public static SmartHealthCareApplication instance;
	private Tracker mAppTracker;
	private static final String PROPERTY_ID = "UA-56929548-1";
	
	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}

	public enum TrackerName {
		APP_TRACKER, // Tracker used only in this app.
		GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg:
						// roll-up tracking.
	}

	synchronized Tracker getTracker(TrackerName trackerId) {
		if (!mTrackers.containsKey(trackerId)) {

			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			Tracker t = null;
			if(trackerId == TrackerName.APP_TRACKER){
				t = analytics
				.newTracker(R.xml.app_tracker);
			}else if(trackerId == TrackerName.GLOBAL_TRACKER){
				t = analytics
						.newTracker(PROPERTY_ID);
			}
			mTrackers.put(trackerId, t);

		}
		return mTrackers.get(trackerId);
	}
}
