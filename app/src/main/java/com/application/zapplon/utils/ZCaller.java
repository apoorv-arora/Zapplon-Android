package com.application.zapplon.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class ZCaller {
	private Activity mContext;
	private String mCallerString;
	private ArrayList<String> mCallList;
	private int mSelectedIndex = 0;
	private Intent intent;
	private ZApplication mZapp;
	private int mRestId;
	private SharedPreferences mPref;
	private final int MAKE_CALL_INTENT = 20;

	// constructor
	public ZCaller(Activity context, String callerString, int restId) {
		this.mContext = context;// pass the activity context
		this.mCallerString = callerString;// rest.getPhone
		this.mZapp = (ZApplication) mContext.getApplication();
		this.mRestId = restId;// pass rest id
		this.mPref = mContext.getSharedPreferences("application_settings", 0);
	}

	// public methord to call for showing the dialog
	public void callDialog() {
		mCallList = getCallerArrayList(mCallerString);
		if (mCallerString.length() > 3) {
			AlertDialog dialog = buildDialog(mCallList);
			if (dialog != null) {
				WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
				wmlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
				wmlp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
				dialog.getWindow().setAttributes(wmlp);
				dialog.show();
			}
		}

	}

	// extracts upto three no from the phone string and returns an array list
	private ArrayList<String> getCallerArrayList(String str) {
		StringTokenizer st = new StringTokenizer(str, ",");
		ArrayList<String> strCallList;
		int phoneCount = 0, total;

		if (st.hasMoreTokens()) {
			total = st.countTokens();
			strCallList = new ArrayList<String>();
			while (phoneCount < total && phoneCount < 3) {
				phoneCount++;
				strCallList.add(st.nextToken());
			}
			if (strCallList != null) {
				return strCallList;
			}

		}
		return null;

	}

	// method in which dialog gets build
	private AlertDialog buildDialog(final ArrayList<String> callList) {
		AlertDialog callDialog;
		if (callList != null) {
			AlertDialog.Builder builder;
			if (Build.VERSION.SDK_INT < 21)
				builder = new AlertDialog.Builder(mContext, R.style.AlertDialog);
			else
				builder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);
			// = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" +
			// phoneNumberToDisplay));
			builder.setCancelable(true).setInverseBackgroundForced(true)
					.setSingleChoiceItems(callList.toArray(new String[mCallList.size()]), 0,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									mSelectedIndex = which;
								}
							})
					.setPositiveButton(mContext.getResources().getString(R.string.dialog_call),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									intent = new Intent(Intent.ACTION_CALL,
											Uri.parse("tel:" + callList.get(mSelectedIndex)));
									try {
										mContext.startActivityForResult(intent, MAKE_CALL_INTENT);
									} catch (ActivityNotFoundException e) {
										e.printStackTrace();
									}
									// send call event to server stuff t be done
									// intent for making a call
								}
							})
					.setNegativeButton(mContext.getResources().getString(R.string.dialog_cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
			callDialog = builder.create();
			return callDialog;
		} else {
			return null;
		}
	}
}
