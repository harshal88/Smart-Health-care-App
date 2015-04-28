package com.unique.smarthealthcare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

public class ListTrackFragment extends Fragment {

	private final static int CONTEXT_RETRY = 0;
	private final static int CONTEXT_DELETE = 1;
	private ListView mTracksList;
	private TextView mTripcCount;
	private String title;
	
	private Context context;
	
	public ListTrackFragment() {
	}
	public ListTrackFragment(String title) {
		this.title = title;
	}
	@Override
	public void onStart() {
		super.onStart();
		
	}
	
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_tracks, container, false);
        mTracksList =  (ListView) rootView.findViewById(android.R.id.list);
        mTripcCount = (TextView) rootView.findViewById(R.id.trips_stat);
        getActivity().setTitle(title);
        registerForContextMenu(mTracksList);
        SharedPreferences settings = getActivity().getSharedPreferences("PREFS", 0);
        if (settings.getAll().isEmpty()) {
			showWelcomeDialog();
		}
        return rootView;
    }
	
	@Override
	public void onResume() {
		super.onResume();
		populateList(mTracksList);
		Tracker t = ((SmartHealthCareApplication) getActivity().getApplication()).getTracker(
	            TrackerName.APP_TRACKER);
		t.setScreenName("HISTORY");
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	void populateList(ListView lv) {
		// Get list from the real phone database. W00t!
		DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		// Clean up any bad trips & coords from crashes
		int cleanedTrips = mDb.cleanTables();
		if (cleanedTrips > 0) {
			Toast.makeText(getActivity(),
					"" + cleanedTrips + " bad trip(s) removed.",
					Toast.LENGTH_SHORT).show();
		}

		try {
			Cursor allTrips = mDb.fetchAllTrips();

			@SuppressWarnings("deprecation")
			SimpleCursorAdapter sca = new SimpleCursorAdapter(getActivity(),
					R.layout.twolinelist, allTrips, new String[] { "purp",
							"fancystart", "fancyinfo" }, new int[] {
							R.id.TextView01, R.id.TextView03, R.id.TextInfo });

			lv.setAdapter(sca);

			int numtrips = allTrips.getCount();
			switch (numtrips) {
			case 0:
				mTripcCount.setText("No saved trips.");
				break;
			case 1:
				mTripcCount.setText("1 saved trip:");
				break;
			default:
				mTripcCount.setText("" + numtrips + " saved trips:");
			}
			// allTrips.close();
		} catch (SQLException sqle) {
			// Do nothing, for now!
		}
		mDb.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				Intent i = new Intent(getActivity(), ShowMapActivity.class);
				i.putExtra("showtrip", id);
				startActivity(i);
				 Toast.makeText(getActivity(), "Loading the trip from the records",
						   Toast.LENGTH_LONG).show();
			}
		});
		registerForContextMenu(lv);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_DELETE, 0, "Delete the record");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case CONTEXT_DELETE:
			AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this.getActivity());
			 myAlertDialog.setTitle("--- Verify ---");
			 myAlertDialog.setMessage("Are you sure you want to delete the record ");
			 myAlertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

			  public void onClick(DialogInterface arg0, int arg1) {
			  // do something when the OK button is clicked
				  
				 deleteTrip(info.id);
				 
				
				 
			  }});
			 myAlertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
			       
			  public void onClick(DialogInterface arg0, int arg1) {
			  // do something when the Cancel button is clicked
			  }});
			 myAlertDialog.show();
			
			
		//	deleteTrip(info.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void deleteTrip(long tripId) {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		mDbHelper.deleteAllCoordsForTrip(tripId);
		mDbHelper.deleteTrip(tripId);
		mDbHelper.close();
		mTracksList.invalidate();
		 Toast.makeText(getActivity(), "Record is deleted",
				   Toast.LENGTH_LONG).show();
		populateList(mTracksList);
	}

	

	private void showWelcomeDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Please enter your personal details ")
				.setCancelable(false).setTitle("Welcome to Running Tracks!")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int id) {
						startActivity(new Intent(getActivity(),
								UserPrefActivity.class));
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	public void setTitle(String title){
		this.title = title;
	}
}
