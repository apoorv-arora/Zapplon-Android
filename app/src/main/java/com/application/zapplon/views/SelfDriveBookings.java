package com.application.zapplon.views;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;

/**
 * Created by dell on 14-Sep-16.
 */
public class SelfDriveBookings extends Fragment {


    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private Activity mContext;
    private boolean destroyed = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.selfdrive_bookings, null);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
        mContext = activity;
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        getView.findViewById(R.id.content).setVisibility(View.GONE);
        getView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
        ((TextView) getView.findViewById(R.id.empty_view_text)).setText("Nothing here yet");
    }

}

