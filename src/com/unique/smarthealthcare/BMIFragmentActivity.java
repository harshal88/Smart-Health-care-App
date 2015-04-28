package com.unique.smarthealthcare;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.unique.smarthealthcare.SmartHealthCareApplication.TrackerName;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BMIFragmentActivity extends Fragment {

	private String IMPERIAL_UNIT;
	private String METRIC_UNIT;
	private final String IMPERIAL_WEIGHT_DESC = "Enter your Weight(lb)";
	private final String METRIC_WEIGHT_DESC = "Enter your Weight(kg)";
	private final String IMPERIAL_HEIGHT_DESC = "Enter your Height(ft)";
	private final String METRIC_HEIGHT_DESC = "Enter your Height(m)";
	private final double CONVERTION_RATE_WEIGHT = 2.20462;
	private final double CONVERTION_RATE_HEIGHT = 3.28084;
	private EditText txtWeight;
	private Spinner weightSpinner;
	private Spinner heightSpinner;
	private EditText txtHeight;
	private EditText txtvResults;
	private Button btnCalcIdeal;
	private Button btnCalculate;
	private Button btnClear;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_bmi, container,
				false);
		IMPERIAL_UNIT = getString(R.string.spnrItemImperial);
		METRIC_UNIT = getString(R.string.spnrItemMetric);
		// sets Units to be used at start up
		// Gets units preferences
		String unitsType = getUnitSettings();

		txtWeight = (EditText) rootView.findViewById(R.id.txtbWeight);
		txtHeight = (EditText) rootView.findViewById(R.id.txtHeight);
		weightSpinner = (Spinner) rootView.findViewById(R.id.weight_unit);
		heightSpinner = (Spinner) rootView.findViewById(R.id.height_unit);
		txtvResults = (EditText) rootView.findViewById(R.id.bmi);
		btnCalcIdeal = (Button) rootView.findViewById(R.id.btnCalcIdeal);
		btnCalculate = (Button) rootView.findViewById(R.id.btnCalculate);
		btnClear= (Button) rootView.findViewById(R.id.btnClear);
		
		btnCalcIdeal.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCalcIdeal();
			}
		});
		
		btnCalculate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCalculate();
			}
		});
		btnClear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickClear();
			}
		});
		return rootView;
	}
	@Override
	public void onStart() {
		super.onStart();
		
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Tracker t = ((SmartHealthCareApplication) getActivity().getApplication()).getTracker(
	            TrackerName.APP_TRACKER);
		t.setScreenName("BMI");
		t.send(new HitBuilders.AppViewBuilder().build());
	}
	public String getUnitSettings() {
		// Gets units preferences
		SharedPreferences settings = getActivity().getSharedPreferences(
				"units", Activity.MODE_PRIVATE);
		String unitsType = settings.getString("units", METRIC_UNIT);
		return unitsType;

	}

	public void setUnitsDesc() {

	}

	public void onClickClear() {
		txtWeight.setText("");
		txtHeight.setText("");

	}

	public void onClickCalculate() {
		double[] input = getInput();

		// First element of input array will be -1 if any of the validation
		// checks have failed
		if (input[0] != -1) {
			// Gets unit prefs
			String unitsType = getUnitSettings();

			// Calculates bmi and passes it to a method that det the category in
			// which the bmi falls and outputs the results
			double bmi = calculateBmi(input, unitsType);
			resultOfBmi(bmi);
		}
	}

	public void onClickCalcIdeal() {
		double[] input = getInput();
		double[] idealWeight = new double[2];
		String idealWeightAdjustment;
		String unitsType = getUnitSettings();

		// First element of the array will be -1 if any of the validation checks
		// have failed
		if (input[0] != -1) {
			idealWeight = calculateIdealWeight(input[1], unitsType);
			idealWeightAdjustment = calcIdealWeightAdjustment(input[0],
					idealWeight, unitsType);

			// Checks what units to express the results in
			if (weightSpinner.getSelectedItemPosition() == 0) {
				txtvResults.setText("Your Ideal Weight is " + idealWeight[0]
						+ "kg -> " + idealWeight[1] + "kg. \n"
						+ idealWeightAdjustment);
			} else {
				txtvResults.setText("Your Ideal Weight is " + idealWeight[0]
						+ "lbs -> " + idealWeight[1] + "lbs. \n"
						+ idealWeightAdjustment);
			}
		}
	}

	public void resultOfBmi(double bmi) {
		String clasification = "";

		if (bmi < 15)
			clasification = "Starving";
		else if (bmi > 14.9 && bmi < 17.5)
			clasification = "Anorexic";
		else if (bmi > 17.4 && bmi < 18.5)
			clasification = "Underweight";
		else if (bmi > 18.4 && bmi < 25)
			clasification = "Ideal";
		else if (bmi > 24.9 && bmi < 30)
			clasification = "Overweight";
		else if (bmi > 29.9 && bmi < 40)
			clasification = "Obese";
		else if (bmi == 40 || bmi > 40)
			clasification = "Morbidly Obese";

		txtvResults.setText("Your BMI: " + bmi + ". You are " + clasification);
	}

	public double calculateBmi(double[] input, String unitsType) {
		double bmi = 0;

		// Converts to Metric units before calc instead of using Imperial
		// formulae
		if (weightSpinner.getSelectedItemPosition() == 1) {
			//Imperial Units
			input[0] = input[0] / CONVERTION_RATE_WEIGHT;
			
		}
		if(heightSpinner.getSelectedItemPosition() == 1){
			input[1] = input[1] / CONVERTION_RATE_HEIGHT;
		}

		bmi = input[0] / (input[1] * input[1]);
		bmi = round(bmi, 2);
		return bmi;

	}

	// calculates ideal weight range ie min Ideal and max Ideal
	public double[] calculateIdealWeight(double height, String unitsType) {
		double[] idealWeight = new double[2];

		// Checks units pref and converst height to metric for calculation
		if (heightSpinner.getSelectedItemPosition() == 1)
			height = height / CONVERTION_RATE_HEIGHT;
		double minWeight = (18.4 * (height * height)); // min Ideal Weight
		double maxWeight = (24.9 * (height * height)); // max Ideal Weight
		// round to 2 deimal places
		idealWeight[0] = round(2, minWeight);
		idealWeight[1] = round(2, maxWeight);
		// Check units pref to return calculation in correct units
		if (weightSpinner.getSelectedItemPosition() == 1) {
			idealWeight[0] = round(2, (idealWeight[0] * CONVERTION_RATE_WEIGHT));
			idealWeight[1] = round(2, (idealWeight[1] * CONVERTION_RATE_WEIGHT));
		}
		return idealWeight;
	}
	// Round a value to x decimal places
	public double round(int places, double value) {
		double roundedValue = new BigDecimal(value).setScale(2,
				RoundingMode.HALF_UP).doubleValue();
		return roundedValue;
	}
	// calculates how much weight the user should lose to fit ideal weight
	public String calcIdealWeightAdjustment(double actualWeight,
			double[] idealWeight, String unitsType) {
		String idealWeightAdjustment;
		double minAdjustment;
		double maxAdjustment;
		if (actualWeight > idealWeight[1]) // overweight
		{
			minAdjustment = round(2, (actualWeight - idealWeight[1]));
			maxAdjustment = round(2, (actualWeight - idealWeight[0]));
			if (unitsType.equalsIgnoreCase(IMPERIAL_UNIT)) {
				idealWeightAdjustment = "You should loose " + minAdjustment
						+ "lbs -> " + maxAdjustment + "lbs.";
			} else {
				idealWeightAdjustment = "You should loose " + minAdjustment
						+ "kg -> " + maxAdjustment + "kg.";
			}
		} else if (actualWeight < idealWeight[0]) // underweight
		{
			minAdjustment = round(2, (idealWeight[0] - actualWeight));
			maxAdjustment = round(2, (idealWeight[1] - actualWeight));
			if (unitsType == IMPERIAL_UNIT) {
				idealWeightAdjustment = "You should gain " + minAdjustment
						+ "lbs -> " + maxAdjustment + "lbs.";
			} else {
				idealWeightAdjustment = "You should gain " + minAdjustment
						+ "kg -> " + maxAdjustment + "kg.";
			}
		} else
			idealWeightAdjustment = "You do not need to loose or gain weight, you are ideal.";
		// unit conversion not required because calculation would stay the same
		// irespective of metric or imperial
		return idealWeightAdjustment;
	}
	@TargetApi(11)
	public double[] getInput() {
		double[] input = new double[2]; // weight and hight values from the user
		// validation
		try {
			boolean valid = true;
			// Checks for empty
			if (txtWeight.getText().toString().isEmpty()) {
				Toast.makeText(getActivity(),
						"You have not entered a value for the Weight field",
						Toast.LENGTH_SHORT).show();
				valid = false;
			}
			if (txtHeight.getText().toString().isEmpty()) {
				Toast.makeText(getActivity(),
						"You have not entered a value for the Height field",
						Toast.LENGTH_SHORT).show();
				valid = false;
			}
			double weight = 0;
			double height = 0;
			// Checks that the checks above have been successful then Converts
			// the text input from the use into doubles
			if (valid) {
				weight = Double.parseDouble(txtWeight.getText().toString());
				height = Double.parseDouble(txtHeight.getText().toString());
				String unitsType = getUnitSettings();
				String unit;
				// Checks for invalid numbers
				if (weight < 1) {
					if (weightSpinner.getSelectedItemPosition() == 0)
						unit = "kg";
					else
						unit = "lbs";
					Toast.makeText(
							getActivity(),
							"You have specified an incorrect weight. Weight must be more that 1"
									+ unit, Toast.LENGTH_SHORT).show();
					valid = false;
				}
				if (height < 1) {
					if (heightSpinner.getSelectedItemPosition() == 0)
						unit = "m";
					else
						unit = "ft";
					Toast.makeText(
							getActivity(),
							"You have specified an incorrect height. Height must be more that 1"
									+ unit, Toast.LENGTH_SHORT).show();
					valid = false;
				}
			}
			if (valid) {
				input[0] = weight;
				input[1] = height;
			} else
				input[0] = -1; // sets first element of array to -1 if any of
								// the validation checks failed
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			Toast.makeText(
					getActivity(),
					"An Error has occured, an invalid character was detectected in one of the fields. /nPlease contact the developer",
					Toast.LENGTH_SHORT).show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return input;
	}
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
}