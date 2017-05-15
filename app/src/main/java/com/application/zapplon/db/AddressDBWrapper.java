package com.application.zapplon.db;

import android.content.Context;

import com.application.zapplon.data.Address;

import java.util.ArrayList;

public class AddressDBWrapper {


    public static AddressDBManager helper;

    public static void Initialize(Context context) {
        helper = new AddressDBManager(context);
    }

    public static int addAddress(Address location, int userId, long timestamp) {
        return helper.addAddress(location, userId, timestamp);
    }

    public static ArrayList<Address> getAddresses(int userId) {
        return helper.getAddresses(userId);
    }

    public static ArrayList<Address> getAllAddresses(int userId) {
        return helper.getAllAddresses(userId);
    }

    public static int deleteAddress(int userId, int type) {
        return helper.deleteAddress(userId,type);
    }
}
