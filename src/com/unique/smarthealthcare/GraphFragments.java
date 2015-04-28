package com.unique.smarthealthcare;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GraphFragments extends Fragment {

	private String[] mMonth = new String[] { "Jan", "Feb", "Mar", "Apr", "May",
			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private static final String DATE_FORMAT = "dd/MM";
	private static final DateFormat DATE_FOMRATTER = new SimpleDateFormat(
			DATE_FORMAT);
	private GridView gridView;
	private DbAdapter dbAdapter;
	private static final double MILE_TO_METER = 1609.34;
	final ChartIcon[] ICONS = {
			new ChartIcon(R.drawable.ic_linechart, "Distance v/s Time",
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Report to google analytics
							Tracker t = ((SmartHealthCareApplication)getActivity().getApplication()).getTracker(
						            TrackerName.APP_TRACKER);
						        // Build and send an Event.
						        t.send(new HitBuilders.EventBuilder()
						            .setCategory("ACTIONS")
						            .setAction("CHECK_DURATION_VS_TIME")
						            .setLabel("Check Duration vs Time Graph")
						            .build());
							// Draw the Reason for Running vs Distance
						        Intent intent = new Intent(getActivity(),
										DistanceVsTimeGraphActivity.class);
								startActivity(intent);
						
						}
					}),
			new ChartIcon(R.drawable.ic_piechart, "Purpose Of Trips",
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Report to google analytics
							Tracker t = ((SmartHealthCareApplication)getActivity().getApplication()).getTracker(
						            TrackerName.APP_TRACKER);
							   // Build and send an Event.
					        t.send(new HitBuilders.EventBuilder()
					            .setCategory("ACTIONS")
					            .setAction("CHECK_Purpose_of_Trips")
					            .setLabel("Check Purpose of trips")
					            .build());
							// Draw the Reason for Running vs Distance
							openPurposeOfTripsChart();
						}
					}),
			new ChartIcon(R.drawable.graph1, "Distance v/s Date",
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Report to google analytics
							Tracker t = ((SmartHealthCareApplication)getActivity().getApplication()).getTracker(
						            TrackerName.APP_TRACKER);
						        // Build and send an Event.
						        t.send(new HitBuilders.EventBuilder()
						            .setCategory("ACTIONS")
						            .setAction("CHECK_DISTANCE_VS_DATE")
						            .setLabel("Check Distance vs Date Graph")
						            .build());
							// Draw the Reason for Running vs Distance
							openDistanceVSDateChart();
							//Intent intent = new Intent(getActivity(),
								//	DistanceVsDateGraphActivity.class);
							//startActivity(intent);
						   
						}
					}),
			new ChartIcon(R.drawable.graph2, "Speed v/s Time",
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Report to google analytics
							Tracker t = ((SmartHealthCareApplication)getActivity().getApplication()).getTracker(
						            TrackerName.APP_TRACKER);
						        // Build and send an Event.
						        t.send(new HitBuilders.EventBuilder()
						            .setCategory("ACTIONS")
						            .setAction("CHECK_SPEED_VS_TIME")
						            .setLabel("Check Speed vs Time Graph")
						            .build());
							// Draw the Reason for Running vs Distance
							//openDistanceVSDateChart();
							// Draw the Reason for Running vs Distance
							// openAvgSpeedVSTimeChart();
							Intent intent = new Intent(getActivity(),
									SpeedVsTimeGraphActivity.class);
							startActivity(intent);
						}
					}), };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_graph, container,
				false);
		gridView = (GridView) rootView.findViewById(R.id.charts_grid);
		gridView.setAdapter(new ChartTypeAdapter(getActivity()));
		dbAdapter = new DbAdapter(getActivity());
		return rootView;
	}
	 @Override
		public void onStart() {
			super.onStart();
			
		}
	@Override
	public void onResume() {
		super.onResume();
		dbAdapter.open();
		Tracker t = ((SmartHealthCareApplication) getActivity().getApplication()).getTracker(
	            TrackerName.APP_TRACKER);
		t.setScreenName("GRAPHS");
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	@Override
	public void onPause() {
		super.onPause();
		dbAdapter.close();
	}



	/**
	 * 
	 */
	private void openDistanceVSDateChart() {
		Cursor tripsCursor = dbAdapter.fetchAllTrips();
		List<TripData> trips = getAllTrips(tripsCursor);
		dbAdapter.close();
		if (trips != null && trips.size() > 0) {
			TimeSeries distanceDateSeries = new TimeSeries("Distance Vs Date");
			// Set the first date to appear on the chart
			// Assume old date to start with
			// To adjust chart y column values.
			double longestDistance = 0;
			// Sort the trips according to which occurred first
			Collections.sort(trips, new Comparator<TripData>() {
				@Override
				public int compare(TripData trip1, TripData trip2) {
					return (int) (trip1.startTime - trip2.startTime);
				}
			});
			// Total trips distance done in a single date
			// For every trip, find the other trips in the same date and add
			// their distances
			XYSeriesRenderer distanceOverTimeRendrer = new XYSeriesRenderer();
			XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
			multiRenderer.setChartTitle("Distance Vs Date");
			
			TripData currentTrip = null;
			for (int i = 0; i < trips.size(); i++) {
				currentTrip = trips.get(i);
				if (currentTrip.distance > longestDistance) {
					longestDistance = currentTrip.distance;
				}

				multiRenderer.addXTextLabel(currentTrip.startTime,  DATE_FOMRATTER.format(new Date(
						currentTrip.startTime)));
				distanceDateSeries.add(currentTrip.startTime,
						round(currentTrip.distance / MILE_TO_METER, 1));	
			}
			
			
			
			// Creating a dataset to hold each series
			XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
			// Adding Duration Series to the dataset
			dataset.addSeries(distanceDateSeries);
			
			// Creating XYSeriesRenderer to customize visitsSeries

			distanceOverTimeRendrer.setColor(Color.RED);
			distanceOverTimeRendrer.setDisplayChartValues(true);
			distanceOverTimeRendrer.setChartValuesTextSize(20);
			distanceOverTimeRendrer.setChartValuesSpacing(20);
			// Creating a XYMultipleSeriesRenderer to customize the whole chart
			multiRenderer.setZoomButtonsVisible(true);
			multiRenderer.setZoomEnabled(true);
			multiRenderer.setPanEnabled(true);
			multiRenderer.setYAxisMin(0);
			multiRenderer
					.setYAxisMax((int) longestDistance / MILE_TO_METER + 1);
			//Pad a day to the left and right to make the graphs look better.
			long dayInMilliseconds = 24 * 60 *60 * 1000;
			multiRenderer.setXAxisMin(trips.get(0).startTime - dayInMilliseconds);
			multiRenderer
			.setXAxisMax(trips.get(trips.size() - 1).startTime +dayInMilliseconds);
			multiRenderer.setLabelsTextSize(20);
			multiRenderer.setBarSpacing(10);
			multiRenderer.setAxisTitleTextSize(20);
			multiRenderer.setBarWidth(10);
			multiRenderer.setXLabelsPadding(10);
			multiRenderer.setChartTitleTextSize(0);
			multiRenderer.setLegendTextSize(20);
			multiRenderer.setXLabels(0);
//			multiRenderer.setXLabelsAngle(90);
			
			// for (int i = 0; i < trips.size(); i++) {
			// multiRenderer.addXTextLabel((long)trips.get(i).startTime,
			// formatter.format(new Date(trips.get(i).startTime)));
			// }
			multiRenderer.setXLabelsPadding(10);
			// setting the margin size for the graph in the order top, left,
			// bottom, right
			multiRenderer.setMargins(new int[] { 40, 40, 40, 40 });
			// for (int i = 0; i < x.length; i++) {
			// multiRenderer.addXTextLabel(i, mMonth[i]);
			// }
			// Adding incomeRenderer and expenseRenderer to multipleRenderer
			// Note: The order of adding dataseries to dataset and renderers to
			// multipleRenderer
			// should be same
			multiRenderer.addSeriesRenderer(distanceOverTimeRendrer);
			multiRenderer.setBarSpacing(5);
			Intent intent = ChartFactory.getBarChartIntent(getActivity(),
					dataset, multiRenderer, Type.DEFAULT,"Distance Vs Date");
			// Start Activity
			startActivity(intent);
		}
	}
	private List<TripData> getAllTrips(Cursor tripsCursor) {
		List<TripData> trips = null;
		trips = new ArrayList<TripData>();
		tripsCursor.moveToPosition(-1);
		while (tripsCursor.moveToNext()) {
			long tripId = tripsCursor.getLong(tripsCursor
					.getColumnIndex(DbAdapter.K_TRIP_ROWID));
			TripData tripData = TripData.fetchTrip(getActivity(), tripId);
			trips.add(tripData);
		}
		return trips;
	}

	private void openPurposeOfTripsChart() {
		// Color of each Pie Chart Sections
		int[] colors = { Color.DKGRAY,Color.CYAN,Color.LTGRAY, Color.MAGENTA, Color.GREEN, Color.CYAN,
				Color.RED, Color.YELLOW,   Color.BLUE };
		List<String> purposes = dbAdapter.getTripsPurposes();
		CategorySeries categorySeries = new CategorySeries("Trip Purposes");
		List<TripData> trips = getAllTrips(dbAdapter.fetchAllTrips());
		if (trips == null || trips.size() == 0) {
			Toast.makeText(getActivity(), "You don't have any trips yet",
					Toast.LENGTH_LONG).show();
			return;
		}
		int tripsCount = trips.size();
		if (purposes != null && purposes.size() > 0) {
			for (String purpose : purposes) {
				purpose = purpose.trim();
				if (purpose.length() == 0) {
					continue;
				}
				int count = 0;
				for (int i = 0; i < tripsCount; i++) {
					String currentTripPurpose = trips.get(i).getTripPurpose();
					if (purpose.equalsIgnoreCase(currentTripPurpose)) {
						count++;
					}
				}
				categorySeries.add(purpose, (count * 1.0) / tripsCount);
			}
		}
		DefaultRenderer defaultRenderer = new DefaultRenderer();
		if (purposes != null && purposes.size() > 0) {
			for (int i = 0; i < categorySeries.getItemCount(); i++) {
				SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
				renderer.setColor(colors[i]);
				renderer.setDisplayChartValues(true);
				// Adding a renderer for a slice
				defaultRenderer.addSeriesRenderer(renderer);
			}
		}
		defaultRenderer.setMargins(new int[] { 80, 80, 80, 80 });
		defaultRenderer.setChartTitle("Purpose of trips");
		defaultRenderer.setApplyBackgroundColor(true);
		defaultRenderer.setBackgroundColor(Color.BLACK);
		defaultRenderer.setChartTitleTextSize(30);
		defaultRenderer.setLabelsTextSize(40);
		defaultRenderer.setLegendTextSize(20);
		dbAdapter.close();
		// Creating an intent to plot bar chart using dataset and
		// multipleRenderer
		Intent intent = ChartFactory.getPieChartIntent(getActivity(),
				categorySeries, defaultRenderer, "Trips Purposes");
		// Start Activity
		startActivity(intent);
	}

	static class ChartIcon {
		final String text;
		final int imgId;
		OnClickListener mClickListener;

		public ChartIcon(int imgId, String text, OnClickListener listener) {
			super();
			this.imgId = imgId;
			this.text = text;
			this.mClickListener = listener;
		}
	}

	class ChartTypeAdapter extends BaseAdapter {

		private Context mContext;

		public ChartTypeAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {
			return ICONS.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		class ViewHolder {
			public ImageView icon;
			public TextView text;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			ViewHolder holder;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				v = vi.inflate(R.layout.chart_icon, null);
				holder = new ViewHolder();
				holder.text = (TextView) v.findViewById(R.id.chart_icon_text);
				holder.icon = (ImageView) v.findViewById(R.id.chart_icon_img);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}
			holder.icon.setOnClickListener(ICONS[position].mClickListener);
			holder.icon.setImageResource(ICONS[position].imgId);
			holder.text.setText(ICONS[position].text);

			return v;
		}

	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}