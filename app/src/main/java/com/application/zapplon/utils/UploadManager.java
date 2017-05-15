package com.application.zapplon.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Debug;
import android.widget.Toast;

import com.application.zapplon.ZApplication;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class UploadManager {

    public static Hashtable<Integer, AsyncTask> asyncs = new Hashtable<Integer, AsyncTask>();
    public static Context context;
    private static SharedPreferences prefs;
    private static ArrayList<UploadManagerCallback> callbacks = new ArrayList<UploadManagerCallback>();
    private static ZApplication zapp;

    public static void setContext(Context context) {
        UploadManager.context = context;
        prefs = context.getSharedPreferences("application_settings", 0);

        if (context instanceof ZApplication) {
            zapp = (ZApplication) context;
        }
    }

    public static void addCallback(UploadManagerCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }

        // this is here because its called from a lot of places.
        if ((double) Debug.getNativeHeapAllocatedSize() / Runtime.getRuntime().maxMemory() > .70) {
            if (zapp != null) {

                if (zapp.cache != null)
                    zapp.cache.clear();
            }
        }
    }

    public static void removeCallback(UploadManagerCallback callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback);
        }
    }

    public static void updateRegistrationId(String regId) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.HARDWARE_REGISTER, 0, regId, null);
        }

        new UpdateRegistrationId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{regId});

    }

    public static void disconnectCabBooking(int type, String token) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.DISCONNECT_CONNECTED_ACCOUNTS, 0, token, null);
        }

        new DisconnectCabBooking().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{type, token});

    }

    public static void cabBookingRequest(int type, String token, String category, double latitude, double longitude, String startLocationText, String endLocationText, double dropLatitude, double dropLongitude, String coupon, String formattedDate, String displayName ) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.CAB_BOOKING_REQUEST, 0, token, null);
        }
        new CabBookingRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{type, token, category, latitude, longitude, startLocationText, endLocationText, dropLatitude, dropLongitude, coupon, formattedDate,displayName});

    }

    public static void intercityBookingRequest(int type, int cabType, String fromCity, String toCity, Double bookingAmt, Double totalCost, String fromDate, String toDate, String displayName, String bookingId , String paymentMode , String refNum, int action) {

        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.INTERCITY_BOOKING_REQUEST, action, "", null);
        }
        new IntercityBookingRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{type, cabType, fromCity, toCity, bookingAmt, totalCost, fromDate, toDate, displayName, bookingId, paymentMode, refNum, action});
    }

    public static void addAddress(int addressType, double latitude, double longitude, String address) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.ADD_ADDRESS, 0, address, null);
        }
        new AddAddress().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{addressType, address, latitude, longitude});

    }

    public static void deleteAddress(int addressType) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.DELETE_ADDRESS, 0, addressType+"", null);
        }
        new DeleteAddress().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{addressType});

    }

    public static void availVoucher(int voucherId) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.AVAIL_VOUCHER, 0, voucherId+"", null);
        }
        new AvailVoucher().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{voucherId});

    }

    public static void cabCancellationRequest(int type, String token, String crn, String callId, double latitude, double longitude,int bookingId, String reason, String caller) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.CAB_CANCELLATION_REQUEST, 0, caller, null);
        }
        new CabCancellationRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{type, token, crn, callId, latitude, longitude,bookingId, reason});
    }

    public static void intercityCancellationRequest( String bookingCode, String cancelReason) {

        new IntercityCancellationRequest().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{bookingCode, cancelReason});

    }

    public static void updateLocation(double latitude, double longitude) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.LOCATION_UPDATE, 0, latitude + "", null);
        }

        new UpdateLocation().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{latitude, longitude});

    }

    public static void sendFeedback(String message, String log) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.SEND_FEEDBACK, 0, message, null);
        }

        new SendFeedback().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{message, log});

    }

    public static void updateInvitationId(String invitationId) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.INVITATION_ID, 0, invitationId, null);
        }
        new UpdateInvitationId().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{invitationId});

    }

    public static void login(String name, String email, String photoUrl, String token, String deviceId, String invitationId,String invited_by) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.GOOGLE_LOGIN, 0, email, null);
        }
        new GoogleLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{name, email, photoUrl, token, deviceId, invitationId, invited_by});

    }

    public static void login(String username, String email, String password, String invited_by) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.LOGIN, 0, email, null);
        }
        new Login().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{username, email, password, invited_by});

    }

    public static void signup(String username, String email, String password, String invited_by) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.SIGNUP, 0, email, null);
        }
        new SignUp().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{username, email, password, invited_by});

    }

    public static void updatePassword(String email, String oldPassword, String newPassword, String pin){
        new updatePassword().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{email, oldPassword, newPassword, pin});
    }

    public static void updateBooking(String drop_lat, String drop_lon, String hash){
        new UpdateBooking().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{drop_lat, drop_lon, hash});
    }

    public static void addCampaign (String deviceId, int campaingNo) {
        new SendCampaignData().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                new Object[]{deviceId,campaingNo});

    }

    public static void logout(String accessToken) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.LOGOUT, 0, accessToken, null);
        }

        new Logout().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{accessToken});

    }


    public static void updateTripRating(String rating,String hash, int type) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.LOGOUT, 0, rating, null);
        }

        new AddTripRating().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{rating,hash, type});

    }

    public static void validateCoupon(String token, String couponCode, String category, int type) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.VALIDATE_COUPON, 0, token, null);
        }
        new ValidateCoupon().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{token, couponCode, category, type});
    }

    //0 for ola, 1 for uber
    public static void sendCabToken(String token, long deletionTime, int company, String stringId, int position) {

        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.SEND_CAB_TOKEN, 0, token, null);
        }

        new SendCabToken().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{token, company, stringId, position, deletionTime});

    }

    public static void phoneVerification(String deviceId, String phoneNumber, String otp) {
        for (UploadManagerCallback callback : callbacks) {
            callback.uploadStarted(CommonLib.PHONE_VERIFICATION, 0, deviceId + "", null);
        }

        new PhoneVerification().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{phoneNumber, otp, deviceId});

    }

    public static void getIntercityBookings(String bookingId) {

        new GetIntercityBooking().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{bookingId});

    }

    private static class UpdateRegistrationId extends AsyncTask<Object, Void, Object[]> {

        private String regId;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            regId = (String) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("pushId", regId));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "user/registrationId?", nameValuePairs,
                        PostWrapper.HARDWARE_REGISTER, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.HARDWARE_REGISTER, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class UpdateLocation extends AsyncTask<Object, Void, Object[]> {

        private double latitude;
        private double longitude;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            latitude = (Double) params[0];
            longitude = (Double) params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude + ""));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude + ""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "user/location?", nameValuePairs,
                        PostWrapper.LOCATION_UPDATE, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.LOCATION_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class AddTripRating extends AsyncTask<Object, Void, Object[]> {

        private String rating;
        private String hash;
        private int type;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            rating = (String) params[0];
            hash = (String) params[1];
            type = (Integer) params[2];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("rating", rating));
            nameValuePairs.add(new BasicNameValuePair("hash", hash));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "appConfig/addRating?", nameValuePairs,
                        PostWrapper.ADD_RATING, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.ADD_RATING, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class UpdateBooking extends AsyncTask<Object, Void, Object[]> {

        private String drop_lat;
        private String drop_lon;
        private String hash;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            drop_lat = (String) params[0];
            drop_lon = (String) params[1];
            hash = (String) params[2];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("drop_lat", drop_lat + ""));
            nameValuePairs.add(new BasicNameValuePair("drop_lon", drop_lon + ""));
            nameValuePairs.add(new BasicNameValuePair("hash", hash));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/update?", nameValuePairs,
                        PostWrapper.CAB_BOOKING_UPDATE, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.CAB_BOOKING_UPDATE_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }


    private static class Logout extends AsyncTask<Object, Void, Object[]> {

        private String accessToken;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            accessToken = (String) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "auth/logout?", nameValuePairs, PostWrapper.LOGOUT,
                        context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.LOGOUT, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class UpdateBillAmount extends AsyncTask<Object, Void, Object[]> {

        private String billAmount;
        private String key;
        private String billlingId;
        private int rating;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            billAmount = (String) params[0];
            key = (String) params[1];
            billlingId = (String) params[2];
            rating = (Integer) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("bill_amount", billAmount));
            nameValuePairs.add(new BasicNameValuePair("key", key));
            nameValuePairs.add(new BasicNameValuePair("billlingId", billlingId));
            nameValuePairs.add(new BasicNameValuePair("rating", "" + rating));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "billing/update?", nameValuePairs,
                        PostWrapper.BILLING_UPDATE, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.BILLING_UPDATE, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class UpdateInvitationId extends AsyncTask<Object, Void, Object[]> {

        private String invitationIds;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            invitationIds = (String) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("invitationId", invitationIds));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "appInvite/invitationId?", nameValuePairs,
                        PostWrapper.INVITATION_ID, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.INVITATION_ID, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class GoogleLogin extends AsyncTask<Object, Void, Object[]> {

        private String name, email, photoUrl, token, deviceId, invitationId,invited_by;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            email = (String) params[1];
            photoUrl = (String) params[2];
            token = (String) params[3];
            deviceId = (String) params[4];
            invitationId = (String) params[5];
            invited_by = (String) params[6];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("google_login", true + ""));
            nameValuePairs.add(new BasicNameValuePair("user_name", name));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("profile_pic", "" + photoUrl));
            nameValuePairs.add(new BasicNameValuePair("token", "" + token));
            nameValuePairs.add(new BasicNameValuePair("deviceId", deviceId));
            nameValuePairs.add(new BasicNameValuePair("referred_by", invitationId));
            nameValuePairs.add(new BasicNameValuePair("invited_by", invited_by));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "auth/login?", nameValuePairs,
                        PostWrapper.GOOGLE_LOGIN, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();
            else {
                SharedPreferences sp = context.getSharedPreferences("invited_by", 0);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.apply();
            }


            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.GOOGLE_LOGIN, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class Login extends AsyncTask<Object, Void, Object[]> {

        private String name, email, password ,invited_by;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            email = (String) params[1];
            password = (String) params[2];
            invited_by = (String) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("user_name", name));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("password", password));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("invited_by", invited_by));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "auth/login?", nameValuePairs,
                        PostWrapper.LOGIN, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.LOGIN, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class updatePassword extends AsyncTask<Object, Void, Object[]> {

        private String email;
        private String oldPassword;
        private String newPassword;
        private String pin;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            email = (String) params[0];
            oldPassword = (String) params[1];
            newPassword = (String) params[2];
            pin = (String) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("old_password", oldPassword));
            nameValuePairs.add(new BasicNameValuePair("new_password", newPassword));
            nameValuePairs.add(new BasicNameValuePair("pin", pin));
            nameValuePairs.add(new BasicNameValuePair("deviceId",CommonLib.getIMEI(context)));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "auth/forgotPassword?", nameValuePairs,
                        PostWrapper.FORGOT_PASSWORD, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            if(pin != null && !pin.isEmpty()) {
                if(newPassword != null && !newPassword.isEmpty()) {
                    for (UploadManagerCallback callback : callbacks) {
                        callback.uploadFinished(CommonLib.SET_PASSWORD, prefs.getInt("uid", 0), 0, true, 0,
                                arg[0].equals("success"), pin);
                    }
                } else {
                    for (UploadManagerCallback callback : callbacks) {
                        callback.uploadFinished(CommonLib.SET_PASSWORD, prefs.getInt("uid", 0), 0, email, 0,
                                arg[0].equals("success"), pin);
                    }
                }
            } else if (oldPassword != null && !oldPassword.isEmpty()) {
                for (UploadManagerCallback callback : callbacks) {
                    callback.uploadFinished(CommonLib.SET_PASSWORD, prefs.getInt("uid", 0), 0, arg[1], 0,
                            arg[0].equals("success"), email);
                }
            } else {
                for (UploadManagerCallback callback : callbacks) {
                    callback.uploadFinished(CommonLib.FORGOT_PASSWORD, prefs.getInt("uid", 0), 0, arg[1], 0,
                            arg[0].equals("success"), email);
                }
            }
        }
    }



    private static class SignUp extends AsyncTask<Object, Void, Object[]> {

        private String name, email, password, invited_by;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            name = (String) params[0];
            email = (String) params[1];
            password = (String) params[2];
            invited_by = (String) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("user_name", name));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("password", "" + password));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("invited_by", invited_by));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "auth/login?", nameValuePairs,
                        PostWrapper.SIGNUP, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.SIGNUP, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }



    private static class SendCampaignData extends AsyncTask<Object, Void, Object[]> {

        private String deviceId;
        private int campaignNo;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;

            deviceId = (String) params[0];
            campaignNo = (Integer) params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("deviceId", deviceId));
            nameValuePairs.add(new BasicNameValuePair("campaignNo", campaignNo+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "campaign/add?", nameValuePairs,
                        PostWrapper.REFERRER, context);
            }
            catch (Exception e) {
                CommonLib.ZLog("error","error in sending camapaign data ");
                e.printStackTrace();
                return result;
            }
            return result;
        }
    }

    private static class SendFeedback extends AsyncTask<Object, Void, Object[]> {

        private String message;
        private String log;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            message = (String) params[0];
            log = (String) params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("message", message));
            nameValuePairs.add(new BasicNameValuePair("log", log));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "user/feedback?", nameValuePairs,
                        PostWrapper.SEND_FEEDBACK, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.SEND_FEEDBACK, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class SendCabToken extends AsyncTask<Object, Void, Object[]> {

        private String token;
        int company;
        private String stringId;
        private int position;
        long deletionTime;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            token = (String) params[0];
            company = (int) params[1];
            stringId = (String) params[2];
            position = (Integer) params[3];
            deletionTime = (Long) params[4];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("cab_token", "" + token));
            nameValuePairs.add(new BasicNameValuePair("type", company +""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("deletionTime", deletionTime+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/session?", nameValuePairs,
                        PostWrapper.OLA_ACCESS_TOKEN, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.SEND_CAB_TOKEN, prefs.getInt("uid", 0), company, arg[1], position,
                        arg[0].equals("success"), stringId);
            }
        }
    }


    private static class PhoneVerification extends AsyncTask<Object, Void, Object[]> {

        private String phoneNumber;
        private String otp;
        private String deviceId;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            phoneNumber = (String) params[0];
            otp = (String) params[1];
            deviceId = (String) params[2];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("phone", phoneNumber));
            nameValuePairs.add(new BasicNameValuePair("device_id", deviceId));
            nameValuePairs.add(new BasicNameValuePair("otp", otp));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "appConfig/verify?", nameValuePairs,
                        PostWrapper.PHONE_VERFICATION, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
			if (arg[0].equals("failure"))
				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.PHONE_VERIFICATION, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class GetIntercityBooking extends AsyncTask<Object, Void, Object[]> {

        private String bookingId;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            bookingId = (String) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("bookingId", bookingId));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "intercity/bookings/getBooking?", nameValuePairs,
                        PostWrapper.INTERCITY_BOOKING_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
//			if (arg[0].equals("failure"))
//				Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.INTERCITY_GET_BOOKING_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }


    private static class DisconnectCabBooking extends AsyncTask<Object, Void, Object[]> {

        private int type;
        private String token;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            type = (Integer) params[0];
            token = (String) params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            nameValuePairs.add(new BasicNameValuePair("token", token+""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/disconnectAccount?", nameValuePairs,
                        PostWrapper.DISCONNECT_CONNECTED_ACCOUNT, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.DISCONNECT_CONNECTED_ACCOUNTS, prefs.getInt("uid", 0), type, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class CabBookingRequest extends AsyncTask<Object, Void, Object[]> {

        private int type;
        private String token;
        private String category, formattedDate;
        private double latitude, longitude;
        private String startLocationText, endLocationText, coupon;
        private double dropLatitude, dropLongitude;
        private String displayName;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            type = (Integer) params[0];
            token = (String) params[1];
            category = (String) params[2];
            latitude = (Double) params[3];
            longitude = (Double) params[4];
            startLocationText = (String) params[5];
            endLocationText = (String) params[6];
            dropLatitude = (Double) params[7];
            dropLongitude = (Double) params[8];
            coupon = (String) params[9];
            formattedDate = (String) params[10];
            displayName = (String) params[11];
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude+""));
            nameValuePairs.add(new BasicNameValuePair("category", category));
            nameValuePairs.add(new BasicNameValuePair("token", token+""));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("pickupAddress", startLocationText+""));
            nameValuePairs.add(new BasicNameValuePair("destinationAddress", endLocationText+""));
            nameValuePairs.add(new BasicNameValuePair("drop_latitude", dropLatitude+""));
            nameValuePairs.add(new BasicNameValuePair("drop_longitude", dropLongitude+""));
            nameValuePairs.add(new BasicNameValuePair("coupon", coupon+""));
            nameValuePairs.add(new BasicNameValuePair("bookingTime", formattedDate));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/book?", nameValuePairs,
                        PostWrapper.CAB_BOOKING_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.CAB_BOOKING_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), displayName);
            }
        }
    }

    private static class IntercityBookingRequest extends AsyncTask<Object, Void, Object[]> {

        private int type;
        private int cabType;
        private String fromCity;
        private String toCity;
        private Double bookingAmt;
        private String displayName;
        private Double totalCost;
        private String fromDate;
        private String toDate;
        private String bookingId;
        private String paymentMode;
        private String refNumber;
        private int action;


        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            type = (Integer) params[0];
            cabType = (Integer)params[1];
            fromCity = (String)params[2];
            toCity = (String)params[3];
            bookingAmt = (Double) params[4];
            totalCost = (Double)params[5];
            fromDate = (String)params[6];
            toDate = (String)params[7];
            displayName = (String) params[8];
            bookingId = (String) params[9];
            paymentMode = (String) params[10];
            refNumber = (String) params[11];
            action = (Integer) params[12];
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("cabType", cabType+""));
            nameValuePairs.add(new BasicNameValuePair("fromCity", fromCity+""));
            nameValuePairs.add(new BasicNameValuePair("toCity", toCity+""));
            nameValuePairs.add(new BasicNameValuePair("bookingAmt", bookingAmt+""));
            nameValuePairs.add(new BasicNameValuePair("totalCost", totalCost+""));
            nameValuePairs.add(new BasicNameValuePair("startDate", fromDate));
            nameValuePairs.add(new BasicNameValuePair("returnDate", toDate));
            nameValuePairs.add(new BasicNameValuePair("bookingId", bookingId));
            nameValuePairs.add(new BasicNameValuePair("paymentMode", paymentMode));
            nameValuePairs.add(new BasicNameValuePair("refNum", refNumber));
            nameValuePairs.add(new BasicNameValuePair("action", action+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "intercity/bookings/book?", nameValuePairs,
                        PostWrapper.INTERCITY_BOOKING_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.INTERCITY_BOOKING_REQUEST, cabType, action, arg[1], 0,
                        arg[0].equals("success"), displayName);
            }
        }
    }

    private static class ConsumerCabBookingRequest extends AsyncTask<Object, Void, Object[]> {

        private int type;
        private String category, formattedDate;
        private double latitude, longitude;
        private String startLocationText, endLocationText, coupon,token;
        private double dropLatitude, dropLongitude;
        private String clientId,password,userName,email,phone;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            type = (Integer) params[0];
            category = (String) params[1];
            latitude = (Double) params[2];
            longitude = (Double) params[3];
            startLocationText = (String) params[4];
            endLocationText = (String) params[5];
            dropLatitude = (Double) params[6];
            dropLongitude = (Double) params[7];
            formattedDate = (String) params[8];
            clientId = (String) params[9];
            password = (String) params[10];
            userName = (String) params[11];
            email = (String) params[12];
            phone = (String) params[13];
            token = (String) params[14];


            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
            nameValuePairs.add(new BasicNameValuePair("app_type", password));
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude+""));
            nameValuePairs.add(new BasicNameValuePair("category", category));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("pickupAddress", startLocationText+""));
            nameValuePairs.add(new BasicNameValuePair("destinationAddress", endLocationText+""));
            nameValuePairs.add(new BasicNameValuePair("drop_latitude", dropLatitude+""));
            nameValuePairs.add(new BasicNameValuePair("drop_longitude", dropLongitude+""));
            nameValuePairs.add(new BasicNameValuePair("bookingTime", formattedDate));
            nameValuePairs.add(new BasicNameValuePair("userName", userName));
            nameValuePairs.add(new BasicNameValuePair("email", email));
            nameValuePairs.add(new BasicNameValuePair("phone", phone));
            nameValuePairs.add(new BasicNameValuePair("token", token));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/book?", nameValuePairs,
                        PostWrapper.CAB_BOOKING_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.CAB_BOOKING_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }


    private static class AvailVoucher extends AsyncTask<Object, Void, Object[]> {

        private int voucherId;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            voucherId = (Integer) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("voucher_id", voucherId+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "voucher/update?", nameValuePairs,
                        PostWrapper.AVAIL_VOUCHER, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.AVAIL_VOUCHER, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class CabCancellationRequest extends AsyncTask<Object, Void, Object[]> {

        private int type;
        private String token;
        private String crn;
        private String callId;
        private double lat;
        private double lon;
        private int bookingId;
        private String reason;
        private String caller;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            type = (Integer) params[0];
            token = (String) params[1];
            crn = (String) params[2];
            callId = (String) params[3];
            lat = (Double) params[4];
            lon = (Double) params[5];
            bookingId = (Integer) params[6];
            reason = (String) params[7];
            caller = (String) params[8];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("crn", crn));
            nameValuePairs.add(new BasicNameValuePair("callId", callId));
            nameValuePairs.add(new BasicNameValuePair("token", token+""));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI(context)));
            nameValuePairs.add(new BasicNameValuePair("latitude", lat+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", lon+""));
            nameValuePairs.add(new BasicNameValuePair("bookingId", bookingId+""));
            nameValuePairs.add(new BasicNameValuePair("reason", reason));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/cancel?", nameValuePairs,
                        PostWrapper.CAB_CANCELLATION_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.CAB_CANCELLATION_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), caller);
            }
        }
    }

    private static class IntercityCancellationRequest extends AsyncTask<Object, Void, Object[]> {

        private String bookingCode, cancelReason;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            bookingCode = (String) params[0];
            cancelReason = (String) params[1];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("bookingCode", bookingCode));
            nameValuePairs.add(new BasicNameValuePair("cancel_reason", cancelReason));
            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "intercity/bookings/cancel?", nameValuePairs,
                        PostWrapper.INTERCITY_CANCELLATION_REQUEST, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.INTERCITY_CANCELLATION_REQUEST, prefs.getInt("uid", 0), 0, arg[1], 0,
                        arg[0].equals("success"), "");
            }
        }
    }


    private static class AddAddress extends AsyncTask<Object, Void, Object[]> {

        private int addressType;
        private double latitude, longitude;
        private String address;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            addressType = (Integer) params[0];
            address = (String) params[1];
            latitude = (Double) params[2];
            longitude = (Double) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("latitude", latitude+""));
            nameValuePairs.add(new BasicNameValuePair("longitude", longitude+""));
            nameValuePairs.add(new BasicNameValuePair("address_type", addressType+""));
            nameValuePairs.add(new BasicNameValuePair("address", address));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "address/add?", nameValuePairs,
                        PostWrapper.ADD_ADDRESS, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.ADD_ADDRESS, prefs.getInt("uid", 0), 0, new Object[] {address, latitude, longitude}, 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class DeleteAddress extends AsyncTask<Object, Void, Object[]> {

        private int addressType;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            addressType = (Integer) params[0];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("address_type", addressType+""));

            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "address/delete?", nameValuePairs,
                        PostWrapper.DELETE_ADDRESS, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.DELETE_ADDRESS, prefs.getInt("uid", 0), 0, addressType, 0,
                        arg[0].equals("success"), "");
            }
        }
    }

    private static class ValidateCoupon extends AsyncTask<Object, Void, Object[]> {

        private String token;
        private String couponCode;
        private String category;
        private int type;

        @Override
        protected Object[] doInBackground(Object... params) {

            Object result[] = null;
            token = (String) params[0];
            couponCode = (String) params[1];
            category = (String) params[2];
            type = (Integer) params[3];

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
            nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
            nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
            nameValuePairs.add(new BasicNameValuePair("token", token+""));
            nameValuePairs.add(new BasicNameValuePair("coupon_code", couponCode+""));
            nameValuePairs.add(new BasicNameValuePair("category", category+""));
            nameValuePairs.add(new BasicNameValuePair("type", type+""));
            try {
                result = PostWrapper.postRequest(CommonLib.SERVER + "booking/validateCoupon?", nameValuePairs,
                        PostWrapper.VALIDATE_COUPON, context);
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Object[] arg) {
            if (arg[0].equals("failure"))
                Toast.makeText(context, (String) arg[1], Toast.LENGTH_SHORT).show();

            for (UploadManagerCallback callback : callbacks) {
                callback.uploadFinished(CommonLib.VALIDATE_COUPON, prefs.getInt("uid", 0), 0, arg[0], 0,
                        arg[0].equals("success"), "");
            }
        }
    }
}