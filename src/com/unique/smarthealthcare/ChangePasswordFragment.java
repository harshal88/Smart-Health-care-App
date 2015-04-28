package com.unique.smarthealthcare;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class ChangePasswordFragment extends DialogFragment {

	private EditText mCurrentPasswordEditText;
	private EditText mNewPasswordEditText;
	private EditText mNewPasswordConfirmEditText;
	private ChangePasswordListener listener;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Change Password");
		builder.setPositiveButton("Change Password", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing, will be override in onStart
			}
		});
		builder.setNegativeButton("Cancel", null);
		LayoutInflater inflator = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflator.inflate(R.layout.fragment_change_password, null);
		mCurrentPasswordEditText = (EditText) view
				.findViewById(R.id.current_password);
		mNewPasswordEditText = (EditText) view.findViewById(R.id.new_password);
		mNewPasswordConfirmEditText = (EditText) view
				.findViewById(R.id.new_password_confirm);
		builder.setView(view);
		return builder.create();

	}
	public void setChangePasswordListener(ChangePasswordListener listener){
		this.listener = listener;
	}
	@Override
	public void onStart() {
		super.onStart();
		AlertDialog alertDialog = (AlertDialog) getDialog();
		if(alertDialog != null){
				alertDialog.getButton(Dialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						boolean dataValid = true;
						if(TextUtils.isEmpty(mCurrentPasswordEditText.getText())){
							dataValid = false;
							mCurrentPasswordEditText.setError("Enter Your Current Password");
						}else if(TextUtils.isEmpty(mNewPasswordEditText.getText())){
							dataValid = false;
							mNewPasswordEditText.setError("Enter Your New Password");
						}else if(TextUtils.isEmpty(mNewPasswordConfirmEditText.getText())){
							dataValid = false;
							mNewPasswordConfirmEditText.setError("Confirm Your New Password");
						}else if(!TextUtils.equals(mNewPasswordEditText.getText(), mNewPasswordConfirmEditText.getText())){
							dataValid = false;
							mNewPasswordConfirmEditText.setError("Passwords not matched");
						}
						if(dataValid){
							listener.changePassword(mCurrentPasswordEditText.getText().toString(), mNewPasswordEditText.getText().toString());
							dismiss();
						}
					}
				});
		}
	}
}
