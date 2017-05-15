package com.application.zapplon.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.application.zapplon.data.OfflineAddress;
import com.application.zapplon.db.LocationDBWrapper;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class AddressService extends IntentService {


    public AddressService() {
        super("AddressService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        CommonLib.ZLog("addressservice","start");
        String addressString = intent.getStringExtra("address");
        String url = "";
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);;

        Object result = null;
        try{
            url = CommonLib.SERVER + "appConfig/addresses?address="+ URLEncoder.encode(addressString, "UTF-8");
            result = RequestWrapper.RequestHttp(url, RequestWrapper.GET_OFFLINE_ADDRESS, RequestWrapper.FAV);
            CommonLib.ZLog("url", url);
        }catch(Exception e){
            e.printStackTrace();
        }

        if (result != null && result instanceof Object[]) {
            Object[] response = (Object[])result;
            if(response[0].equals("success") && response[1] instanceof JSONArray){
                ArrayList<OfflineAddress> locationList=  new ArrayList<OfflineAddress>();
                JSONArray jsonArray = (JSONArray)response[1];
                try {
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject object = jsonArray.getJSONObject(i);
                        OfflineAddress location = new OfflineAddress();
                        location.setId(object.getInt("id"));
                        location.setCityId(object.getInt("cityId"));
                        location.setAddress(object.getString("address"));
                        location.setLatitude(object.getDouble("latitude"));
                        location.setLongitude(object.getDouble("longitude"));
                        locationList.add(location);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LocationDBWrapper.addAddresses(locationList, prefs.getInt("uid", 0), System.currentTimeMillis());
                prefs.edit().putBoolean("LOCAL_ADDRESS_UPDATE", false).commit();
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("offlineVisibility",true);
                editor.commit();
                CommonLib.ZLog("addressservice","db updated: "+locationList.size());
            }
        }
        CommonLib.ZLog("addressservice","stop");
    }
}
