package com.application.zapplon.db;

import android.content.Context;

import com.application.zapplon.data.OfflineAddress;

import java.util.ArrayList;

/**
 * Created by dell on 30-Jul-16.
 */
public class LocationDBWrapper {

    public static LocationDBManager helper;

    public static void Initialize(Context context) {
        helper = new LocationDBManager(context);
    }

    public static int addAddresses(ArrayList<OfflineAddress> locations, int userId, long timestamp) {
        return helper.addAddresses(locations, userId, timestamp);
    }

    public static ArrayList<OfflineAddress> getAddresses(int userId) {
        return helper.getAddresses(userId);
    }

    public static void removeAddresses() {
        helper.removeAddresses();
    }
}
