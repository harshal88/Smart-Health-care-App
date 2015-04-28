package com.unique.smarthealthcare;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class SpeedVsTimeGraphActivity extends Activity {

	private Spinner mTripSpinner;
	private LinearLayout mChartView;
	private DbAdapter mDbAdapter;
	private static final double MILE_TO_METER = 1609.34;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_vs_time_graph);
		mTripSpinner = (Spinner) findViewById(R.id.trip);
		mChartView = (LinearLayout) findViewById(R.id.graph);
		mDbAdapter = new DbAdapter(this);
		mDbAdapter.open();
		Cursor cursor = mDbAdapter.fetchAllTrips();
		String[] queryColumns = new String[] { DbAdapter.K_TRIP_ROWID,
				DbAdapter.K_TRIP_START };
		String[] adapterColumns = new String[] { DbAdapter.K_TRIP_FANCYSTART };
		int[] adapterRowViews = new int[] { android.R.id.text1 };
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, cursor, adapterColumns,
				adapterRowViews, 0);
		sca.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTripSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				TripData tripData = TripData.fetchTrip(
						SpeedVsTimeGraphActivity.this, id);
				openSpeedVSTimeChart(tripData);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		mTripSpinner.setAdapter(sca);
		mDbAdapter.close();
	}

	private void openSpeedVSTimeChart(TripData tripData) {
		
	
		TimeSeries distanceTimeSeries = new TimeSeries("Speed VS Time");
		// Set the first date to appear on the chart
		// Assume old date to start with
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.set(1970, 1, 1);

		// To adjust chart y column values.
		List<RunningPoint> points = tripData.getPoints();
		double highestSpeed = 0.0;

		for (int i = 0; i < points.size(); i++) {
			RunningPoint currentPoint = points.get(i);
			distanceTimeSeries.add(new Date((long) currentPoint.time),
					round(currentPoint.speed, 1));
			if(currentPoint.speed > highestSpeed){
				highestSpeed = round(currentPoint.speed, 1);
			}
		}
		// Creating a dataset to hold each series
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// Adding Duration Series to the dataset
		dataset.addSeries(distanceTimeSeries);
		// Adding Distance Series to dataset

		// Creating XYSeriesRenderer to customize visitsSeries
		XYSeriesRenderer distanceOverTimeRendrer = new XYSeriesRenderer();
		distanceOverTimeRendrer.setColor(Color.rgb(130, 130, 230));
		distanceOverTimeRendrer.setPointStyle(PointStyle.CIRCLE);
		distanceOverTimeRendrer.setFillPoints(true);
		distanceOverTimeRendrer.setLineWidth(3);
		distanceOverTimeRendrer.setDisplayChartValues(true);
		distanceOverTimeRendrer.setChartValuesTextSize(20);

		// Creating a XYMultipleSeriesRenderer to customize the whole chart
		XYMultipleSeriesRenderer multiRenderer = new XYMultipleSeriesRenderer();
		multiRenderer.setZoomButtonsVisible(true);
		multiRenderer.setZoomEnabled(true);
		multiRenderer.setPanEnabled(true);
		multiRenderer.setChartTitle("Speed over Time Chart");
		multiRenderer.setYTitle("Distance In Miles");
		multiRenderer.setXLabelsPadding(10);
		multiRenderer.setXLabelsAngle(90);
		multiRenderer.setDisplayValues(true);
		multiRenderer.setYAxisMin(0);
		multiRenderer.setYAxisMax(((int) (highestSpeed)) + 1);
		multiRenderer.setLabelsTextSize(20);
		multiRenderer.setAxisTitleTextSize(20);
		multiRenderer.setChartTitleTextSize(20);
		multiRenderer.setLegendTextSize(20);
		multiRenderer.setXLabels(10);
		multiRenderer.setDisplayChartValues(true);
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
		mChartView.removeAllViews();
		mChartView.addView(ChartFactory.getTimeChartView(this, dataset,
				multiRenderer, "hh:mm:ss"));
		mChartView.invalidate();
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
