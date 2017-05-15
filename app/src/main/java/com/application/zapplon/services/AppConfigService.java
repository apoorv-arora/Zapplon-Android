package com.application.zapplon.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;

import com.application.zapplon.ZApplication;
import com.application.zapplon.data.AppConfig;
import com.application.zapplon.data.City;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dell on 22-Aug-16.
 */
public class AppConfigService extends IntentService {

    private ZApplication zapp;

    public AppConfigService() {
        super("AppConfigService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
        String url = "";
        url = CommonLib.SERVER + "appConfig/info?";
        Object result = RequestWrapper.RequestHttp(url, RequestWrapper.APP_CONFIG, RequestWrapper.FAV);
        CommonLib.ZLog("url", url);

        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();


        if (result != null) {
            if (result instanceof ArrayList<?>) {
                ArrayList<AppConfig> appConfigs = (ArrayList<AppConfig>) result;
                SharedPreferences sharedPreferences = getSharedPreferences("application_settings", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("ZOMATO_LOGOUT", false);
                editor.putBoolean("ZOMATO_ORDER_LOGOUT", false);
                for(AppConfig appConfig:appConfigs) {

                    if(appConfig.getKey().equalsIgnoreCase("ZOMATO_LOGOUT")) {
                        if(appConfig.getValue().equalsIgnoreCase("1")) {
                            //log out zomato cause it's fun :)
                            Intent newIntent = new Intent("com.application.zomato.LOGOUT_INTENT");
                            sendBroadcast(newIntent);
                            editor.putBoolean("ZOMATO_LOGOUT", true);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("ZOMATO_ORDER_LOGOUT")) {
                        if(appConfig.getValue().equalsIgnoreCase("1")) {
                            //log out order cause it's more fun :)
                            Intent orderingIntent = new Intent("com.application.zomato.ordering.LOGOUT_INTENT");
                            sendBroadcast(orderingIntent);
                            editor.putBoolean("ZOMATO_ORDER_LOGOUT", true);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("SHOW_APPCONFIG_DIALOG")) {
                        // show some custom dialog with info fromm server
                        if(appConfig.getValue() != null) {
                            JSONObject object = null;
                            try {
                                object = new JSONObject(appConfig.getValue());
                                if(object != null) {
                                    String title = "";
                                    String imageUrl = "";
                                    String description = "";
                                    String footer = "";
                                    boolean finishOnTouchOutside = true;
                                    boolean showAlways = false;
                                    int hasChanged = 0;
                                    boolean buttonVisibility = false;
                                    String buttonString = "";
                                    String buttonAction = "";

                                    if(object.has("title"))
                                        title = String.valueOf(object.get("title"));
                                    if(object.has("imageUrl"))
                                        imageUrl = String.valueOf(object.get("imageUrl"));
                                    if(object.has("description"))
                                        description = String.valueOf(object.get("description"));
                                    if(object.has("footer"))
                                        footer = String.valueOf(object.get("footer"));
                                    if(object.has("finishOnTouchOutside"))
                                        finishOnTouchOutside = object.getBoolean("finishOnTouchOutside");
                                    if(object.has("showAlways"))
                                        showAlways = object.getBoolean("showAlways");
                                    if(object.has("hasChanged") && object.get("hasChanged") instanceof Integer)
                                        hasChanged = object.getInt("hasChanged");
                                    if(object.has("buttonVisibility") && object.get("buttonVisibility") instanceof Boolean)
                                        buttonVisibility = object.getBoolean("buttonVisibility");

                                    if(object.has("buttonString") && object.get("buttonString") instanceof String)
                                        buttonString = object.getString("buttonString");

                                    if(object.has("buttonAction") && object.get("buttonAction") instanceof String)
                                        buttonAction = object.getString("buttonAction");

                                    if(title != null && imageUrl != null && description != null && footer != null) {
                                        editor.putString("appConfig_title", title);
                                        editor.putString("appConfig_description", description);
                                        editor.putString("appConfig_imageUrl", imageUrl);
                                        editor.putString("appConfig_footer", footer);
                                        editor.putBoolean("appConfig_finishonTouchOutside", finishOnTouchOutside);
                                        editor.putBoolean("appConfig_showAlways", showAlways);
                                        editor.putInt("appConfig_hasChanged_new", hasChanged);
                                        editor.putString("buttonString",buttonString);
                                        editor.putString("buttonAction",buttonAction);
                                        editor.putBoolean("buttonVisibility",buttonVisibility);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("INTERCITY_CITIES")){
                        if(appConfig.getValue() != null) {
                            String value = appConfig.getValue();
                            try {
                                JSONObject citiesJson = new JSONObject(value);
                                if (citiesJson != null && citiesJson.has("cities") && citiesJson.get("cities") instanceof JSONArray) {

                                    JSONArray cityArray = citiesJson.getJSONArray("cities");
                                    ArrayList<City> cities = new ArrayList<City>();
                                    for(int x=0; x<cityArray.length(); x++) {
                                        JSONObject cityJsonObject = cityArray.getJSONObject(x);
                                        if (cityJsonObject.has("city") && cityJsonObject.get("city") instanceof JSONObject) {
                                            JSONObject cityJson = cityJsonObject.getJSONObject("city");
                                            City city = new City();
                                            if (cityJson != null) {
                                                if (cityJson.has("id") && cityJson.get("id") instanceof Integer) {
                                                    city.setCityId(cityJson.getInt("id"));
                                                }

                                                if (cityJson.has("name")) {
                                                    city.setName(String.valueOf(cityJson.get("name")));
                                                }

                                                if (cityJson.has("latitude") && cityJson.get("latitude") instanceof Double) {
                                                    city.setLatitude(cityJson.getDouble("latitude"));
                                                }

                                                if (cityJson.has("longitude") && cityJson.get("longitude") instanceof Double) {
                                                    city.setLongitude(cityJson.getDouble("longitude"));
                                                }

                                                cities.add(city);
                                            }
                                        }
                                    }
                                    zapp.cities = cities;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            editor.putString("INTERCITY_CITIES",value);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("SELF_DRIVE_VISIBILITY")){
                        if(appConfig.getValue().equalsIgnoreCase("1")) {
                            editor.putBoolean("SELF_DRIVE_VISIBILITY", true);
                        } else
                            editor.putBoolean("SELF_DRIVE_VISIBILITY", false);
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("INTERCITY_VISIBIILTY")){
                        if(appConfig.getValue().equalsIgnoreCase("1")) {
                            editor.putBoolean("INTERCITY_VISIBIILTY", true);
                        } else
                            editor.putBoolean("INTERCITY_VISIBIILTY", false);
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("TAXI_CANCELLATION_REASONS")) {
                        // show some custom dialog with info fromm server
                        if(appConfig.getValue() != null) {
                            String value = appConfig.getValue();
                            editor.putString("cancel_reason",value);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("ANDROID_SERVICES_MESSAGE_CONTACT")) {
                        // show some custom dialog with info fromm server
                        if(appConfig.getValue() != null) {
                            String value = appConfig.getValue();
                            editor.putString("services_message_contact",value);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("ANDROID_SERVICES_CALL_CONTACT")) {
                        // show some custom dialog with info fromm server
                        if(appConfig.getValue() != null) {
                            String value = appConfig.getValue();
                            editor.putString("services_call_contact",value);
                        }
                    }

                    else if(appConfig.getKey().equalsIgnoreCase("LOCAL_ADDRESS_VERSION")) {
                        String localVersion = prefs.getString("LOCAL_ADDRESS_VERSION", "");
                        String value = appConfig.getValue();
                        if (value != null && !localVersion.equals(value)) {
                            editor.putString("LOCAL_ADDRESS_VERSION", value);
                            editor.putBoolean("LOCAL_ADDRESS_UPDATE", true);
                        }
                    }
                }
                editor.commit();
            }
        }
    }
}
