package com.application.zapplon.views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Window;

import com.application.zapplon.R;

/**
 * Created by apoorvarora on 15/02/16.
 */
public class CheckPhoneVerificationActivity extends ActionBarActivity {


    private ActionBarActivity mActivity;
    private Bundle mBundle;
    SharedPreferences prefs;
    LayoutInflater vi;
    int width;
    private boolean destroyed = false;
    int fragmentType;
    private boolean is_cancellable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_container);

        if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("finish_on_touch_outside"))
            is_cancellable = getIntent().getBooleanExtra("finish_on_touch_outside", false);

        setFinishOnTouchOutside(is_cancellable);

        setCategoryFragment();
    }

    Fragment fragment;

    public void setCategoryFragment() {
        // FrameLayout container = (FrameLayout) findViewById(R.id.container);
        Bundle bundle = new Bundle();
        fragmentType = 0;
        bundle.putInt("type", 0);
        fragment = PhoneVerificationFragment.newInstance(bundle);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .setCustomAnimations(R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_left_exit,
                        R.anim.fragment_slide_left_enter,
                        R.anim.fragment_slide_right_exit).commit();
    }

    public void setSubCategoryFragment(String mobile) {
        Bundle bundle = new Bundle();
        fragmentType = 1;
        fragment = CheckPhoneVerificationFragment.newInstance(bundle);
        bundle.putString("mobile", mobile);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .setCustomAnimations(R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_left_exit,
                        R.anim.fragment_slide_left_enter,
                        R.anim.fragment_slide_right_exit).addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void setPreviousFragment(Bundle mobile) {
        Bundle bundle = mobile;
        fragmentType = 1;
        fragment = PhoneVerificationFragment.newInstance(bundle);
        getSupportFragmentManager().popBackStackImmediate();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment)
                .setCustomAnimations(R.anim.fragment_slide_right_enter,
                        R.anim.fragment_slide_left_exit,
                        R.anim.fragment_slide_left_enter,
                        R.anim.fragment_slide_right_exit).addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if(is_cancellable){
            super.onBackPressed();
        }
    }

    public boolean is_cancellable() {
        return is_cancellable;
    }

    public void setIs_cancellable(boolean is_cancellable) {
        this.is_cancellable = is_cancellable;
    }
}
