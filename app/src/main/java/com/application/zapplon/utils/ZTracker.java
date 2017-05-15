package com.application.zapplon.utils;

import android.content.Context;

import com.application.zapplon.ZApplication;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by apoorvarora on 28/12/15.
 */
public class ZTracker {

    // Custom Categories
    public  final static String CATEGORY_WIDGET_ACTION = "CATEGORY_WIDGET_ACTION";

    // Custom Screen Names
    public final static String SCREEN_NAME_ONLINE_ORDERING_ADD_ADDRESS_ME_TAB = "Online Ordering-Add Address From Me Tab";

    // Custom Widget Actions
    public final static String ACTION_GOOGLE_LOGIN_PRESSED = "ACTION_GOOGLE_LOGIN_PRESSED";
    public final static String ACTION_FACEBOOK_LOGIN_PRESSED = "ACTION_FACEBOOK_LOGIN_PRESSED";
    public final static String ACTION_FOOD_AND_BEVERAGES_PRESSED = "ACTION_FOOD_AND_BEVERAGES_PRESSED";
    public final static String ACTION_CABS_BOOKING_PRESSED = "ACTION_CABS_BOOKING_PRESSED";
    public final static String ACTION_SALON_AND_SPA_PRESSED = "ACTION_SALON_AND_SPA_PRESSED";
    public final static String ACTION_FAB_PRESSED = "ACTION_FAB_PRESSED";
    public final static String ACTION_SEARCH_BUTTON_PRESSED = "ACTION_SEARCH_BUTTON_PRESSED";
    public final static String ACTION_SEARCH_BUTTON_PRESSED_STORE = "ACTION_SEARCH_BUTTON_PRESSED_STORE_LIST";
    public final static String ACTION_FILTER_BUTTON_PRESSED_STORE = "ACTION_FILTRE_BUTTON_PRESSED_STORE_LIST";
    public final static String ACTION_INVITE_PRESSED = "ACTION_INVITE_PRESSED";
    public final static String ACTION_RATE_US_PRESSED = "ACTION_RATE_US_PRESSED";
    public final static String ACTION_MY_DEALS_PRESSED = "ACTION_MY_DEALS_PRESSED";
    public final static String ACTION_CONNECTED_ACCOUNTS_PRESSED = "ACTION_CONNECTED_ACCOUNTS_PRESSED";
    public final static String ACTION_REDEEM_PRESSED = "ACTION_REDEEM_PRESSED";
    public final static String ACTION_FEEDBACK_PRESSED = "ACTION_FEEDBACK_PRESSED";
    public final static String ACTION_CHANGE_PHONE_NUMBER_PRESSED = "ACTION_CHANGE_PHONE_NUMBER_PRESSED";
    public final static String ACTION_MY_BOOKINGS_PRESSED = "ACTION_MY_BOOKINGS_PRESSED";
    public final static String ACTION_ABOUT_PRESSED = "ACTION_ABOUT_PRESSED";
    public final static String ACTION_SETTINGS_DRAWER_PRESSED = "ACTION_SETTINGS_DRAWER_PRESSED";
    public final static String ACTION_TERMS_OF_SERVICE_PRESSED = "ACTION_TERMS_OF_SERVICE_PRESSED";
    public final static String ACTION_PRIVACY_POLICY_PRESSED = "ACTION_PRIVACY_POLICY_PRESSED";
    public final static String ACTION_SIGN_OUT_PRESSED = "ACTION_SIGN_OUT_PRESSED";
    public final static String ACTION_STORE_DETAILS_PRESSED_NEARBY = "ACTION_STORE_DETAILS_PRESSED_NEARBY";
    public final static String ACTION_STORE_DETAILS_PRESSED_SEARCH = "ACTION_STORE_DETAILS_PRESSED_SEARCH";
    public final static String ACTION_STORE_DETAILS_PRESSED_SEARCH_RESULTS = "ACTION_STORE_DETAILS_PRESSED_SEARCH_RESULTS";
    public final static String ACTION_STORE_DETAILS_PRESSED_TYPE = "ACTION_STORE_DETAILS_PRESSED_TYPE";
    public final static String ACTION_BUY_DEAL_PRESSED = "ACTION_BUY_DEAL_PRESSED";
    public final static String ACTION_CALL_CUSTOMER_CARE_PRESSED = "ACTION_CALL_CUSTOMER_CARE_PRESSED";
    public final static String ACTION_CALL_ICON_PRESSED = "ACTION_CALL_ICON_PRESSED";
    public final static String ACTION_SHARE_ICON_PRESSED = "ACTION_SHARE_ICON_PRESSED";
    public final static String ACTION_STORE_CABS_BOOKING_PRESSED = "ACTION_STORE_CABS_BOOKING_PRESSED";
    public final static String ACTION_CAB_BOOKING_PRESSED = "ACTION_CAB_BOOKING_PRESSED";
    public final static String ACTION_REFERRED_INSTALLED = "ACTION_REFERRED_INSTALLED";
    public final static String ACTION_INVITE_INSTALLED = "ACTION_INVITE_INSTALLED";
    public final static String ACTION_INVITE_SIGNUP = "ACTION_INVITE_SIGNUP";

    // Google Analytics Event
    public static void logGAEvent(Context ctx, String categoryStr, String actionStr, String labelStr) {

        try {

            // Get tracker.
            Tracker tracker = ((ZApplication) ctx.getApplicationContext()).getTracker(CommonLib.TrackerName.APPLICATION_TRACKER);

            // Build and send an Event.
            tracker.send(new HitBuilders
                    .EventBuilder()
                    .setCategory(categoryStr)
                    .setAction(actionStr)
                    .setLabel(labelStr)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Google Analytics Screen View
    public static void logGAScreen(Context ctx, String screenName) {

        try {

            // Get tracker.
            Tracker tracker = ((ZApplication) ctx.getApplicationContext()).getTracker(CommonLib.TrackerName.APPLICATION_TRACKER);

            // Set screen name.
            tracker.setScreenName(screenName);

            // Send a screen view.
            tracker.send(new HitBuilders.AppViewBuilder().build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
