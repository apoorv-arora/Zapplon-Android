package com.application.zapplon.views;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;

import java.util.List;

/**
 * Created by apoorvarora on 11/01/16.
 */
public class UrlRouter extends Activity {

    private final String TAG = "URLRouter";
    Intent intent;
    String firstSegment = "";
    String thirdSegment = "";
    String mType, mId;
    private Uri mUrl;
    private String mTitle;
    private String mDescription;

    boolean mFromExternalSource = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_router);

        CommonLib.ZLog(TAG, "URLRouter");


        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        // check if it came here from any image sharing source
        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (getIntent().getAction() != null
                        && getIntent().getAction().equals(Intent.ACTION_SEND)) {
                    mFromExternalSource = true;
                }
            }
        }

        try {

            if ((prefs != null && prefs.getInt("uid", 0) > 0)) {
                urlNavigation();
            } else
                navigateToTour();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void urlNavigation() {

        try {
            intent = this.getIntent();
            final String action = intent.getAction();

            CommonLib.ZLog(TAG, action + " " + intent.getDataString());

            if (Intent.ACTION_VIEW.equals(action)) {
                final List<String> segments = intent.getData()
                        .getPathSegments();

                if (segments.size() > 0) {

                    firstSegment = segments.get(0);

                    if (firstSegment.contains("home")) {
                        navigateToHome();
                    } else if (firstSegment.contains("appInvite")) {
                        appInvite();
                    } else {
                        navigateToTour();
                    }

                } else {
                    navigateToTour();
                }

            } else {
                navigateToTour();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToHome() {
        intent = new Intent(UrlRouter.this, Home.class);
        Bundle bundle = new Bundle();
        bundle.putString("Source", "Router");
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    private void appInvite() {
        intent = new Intent(UrlRouter.this, SplashScreen.class);
        Bundle bundle = new Bundle();
        bundle.putString("Source", "Router");
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }

    private void navigateToTour() {
        intent = new Intent(UrlRouter.this, SplashScreen.class);
        startActivity(intent);
        finish();
    }

}
