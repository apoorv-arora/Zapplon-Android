package com.application.zapplon.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.application.zapplon.R;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class CommonLib {

    public final static boolean ZapplonLog = false;
    private static SharedPreferences prefs;

    public static final String SERVER_PREFIX = "http://";

	public static String SERVER_WITHOUT_VERSION = "http://192.168.0.69:8080/ZapplonServer/rest/";
    public static String API_VERSION = "";
    public static String SERVER = SERVER_WITHOUT_VERSION + API_VERSION;

    public static int REQUEST_CODE_OLA_WEB_VIEW = 500;
    public static int REQUEST_CODE_UBER_WEB_VIEW = 501;
    public static int REQUEST_CODE_AHA_WEB_VIEW = 503;
    public static int START_LOCATION_CODE = 10000, DROP_LOCATION_CODE = 10002;

    /**
     * Preferences
     */
    public final static String APP_SETTINGS = "application_settings";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";

    public static String Thin = "fonts/zapplon_Thin.otf";
    public static String Light = "fonts/zapplon_Light.otf";
    public static String Bold = "fonts/zapplon_Bold.otf";
    public static String Icons = "fonts/zapplon_Icon.ttf";
    public static String BOLD_FONT_FILENAME = "fonts/zapplon_Bold.otf";

    /**
     * Places API Key
     */
    public static final String GOOGLE_PLACES_API = "AIzaSyCPCQOBTl-PhTzvn7FP34rLqf_stNHDUjk";

    /**
     * GCM Sender ID
     */
    public static final String GCM_SENDER_ID = "481732547877";

    // status flags for trip BOOKING
    public static final int TRACK_STAGE_BOOKING_FAILED = 97;
    public static final int TRACK_STAGE_BOOKING_PENDING = 98;
    public static final int TRACK_STAGE_BOOKING_RETRYING = 99;
    public static final int TRACK_STAGE_NO_BOOKING = 100;

    // status flags for trip TRACKING
    public static final int TRACK_STAGE_CALL_DRIVER = 101;
    public static final int TRACK_STAGE_CLIENT_LOCATED = 102;
    public static final int TRACK_STAGE_TRIP_START = 103;
    public static final int TRACK_STAGE_TRIP_END = 104;
    public static final int TRACK_STAGE_INVOICE = 108;

    // status flags for trip end
    public static final int TRACK_STAGE_TRIP_END_CASHBACK = 106;
    public static final int TRACK_STAGE_BOOKING_CANCELLED = 105;

    public static final int TRACK_STAGE_TRIP_PAYMENT_DONE = 107;

    public static final long POLLING_TIMER = 1000 * 10;

    public static final int REVEAL_ANIM_TIMER = 400;

    // billing status
    public static final int BILLING_STATUS_BOOKED = 101;
    public static final int BILLING_STATUS_VERIFIED = 102;
    public static final int BILLING_STATUS_RATED = 103;

    public static final int SORT_INVALID = 0;
    public static final int SORT_ASC = 1;
    public static final int SORT_DESC = 2;
    public static final int ANIMATION_LOGIN = 200;
    public static final int ANIMATION_DURATION_SIGN_IN = 300;

    // Broadcasts generated after gcm messages are recieved.
    public static final String LOCAL_SMS_BROADCAST = "sms-phone-verification-message";

    public static final String LOCAL_PUSH_PROMOTIONAL_BROADCAST = "LOCAL_PUSH_PROMOTIONAL_BROADCAST";

    public static final String LOCAL_CAB_TRACKING_BROADCAST = "cab-location-broadcast";

    public static final String LOCAL_INTERNET_CONNECTIVITY_BROADCAST = "internet-broadcast-receiver";

    /**
     * Application version
     */
    public static final int VERSION = 35;
    public static final String VERSION_STRING = "2.58";

    /**
     * Authorization params
     */
    public static final String SOURCE = "&source=android_market&version=" + android.os.Build.VERSION.RELEASE
            + "&app_version=" + VERSION;
    public static final String CLIENT_ID = "bt_android_client";
    public static final String APP_TYPE = "bt_android";

    public static final String UBER_CLIENT_ID = "YA36hEXTQ3o4SVWI9yADTXv9wb2J0GyQ";
    public static final String UBER_SERVER = "https://api.uber.com/";
    public static final String UBER_SERVER_TOKEN = "VexZiPPs0esJHZmGRBYtw9ZwtJZYfChTNXsAMdIa";

    /**
     * Thread pool executors
     */
    private static final int mImageAsyncsMaxSize = 4;
    public static final BlockingQueue<Runnable> sPoolWorkQueueImage = new LinkedBlockingQueue<Runnable>(128);
    private static ThreadFactory sThreadFactoryImage = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR_IMAGE = new ThreadPoolExecutor(mImageAsyncsMaxSize,
            mImageAsyncsMaxSize, 1, TimeUnit.SECONDS, sPoolWorkQueueImage, sThreadFactoryImage);

    /**
     * Upload status tracker
     */
    public static final int REQUEST_PRE_SIGNUP = 201;
    public static final int GOOGLE_LOGIN = 202;
    public static final int LOGOUT = 203;
    public static final int WISH_ADD = 204;
    public static final int WISH_REMOVE = 205;
    public static final int HARDWARE_REGISTER = 206;
    public static final int UPDATE_INSTITUTION = 207;
    public static final int WISH_UPDATE_STATUS = 208;
    public static final int SEND_MESSAGE = 209;
    public static final int LOCATION_UPDATE = 210;
    public static final int BILLING_UPDATE = 212;
    public static final int SEND_FEEDBACK = 213;
    public static final int CAB_BOOKING = 215;
    public static final int PHONE_VERIFICATION = 216;
    public static final int SEND_CAB_TOKEN = 217;
    public static final int GET_CONNECTED_ACCOUNTS = 218;
    public static final int DISCONNECT_CONNECTED_ACCOUNTS = 219;
    public static final int CAB_BOOKING_REQUEST = 220;
    public static final int CAB_CANCELLATION_REQUEST = 221;
    public static final int INVITATION_ID = 222;
    public static final int GET_VOUCHER = 223;
    public static final int AVAIL_VOUCHER = 224;
    public static final int ADD_ADDRESS = 225;
    public static final int DELETE_ADDRESS = 226;
    public static final int VALIDATE_COUPON = 227;
    public static final int LOGIN = 228;
    public static final int SIGNUP = 229;
    public static final int FORGOT_PASSWORD = 230;
    public static final int SET_PASSWORD = 231;

    public static final int CAB_BOOKING_TRACKING = 232;
    public static final int CAB_BOOKING_UPDATE_REQUEST = 233;
    public static final int ADD_RATING = 234;

    public static final int GET_REASON = 235;
    public static final int INTERCITY_BOOKING_REQUEST = 236;
    public static final int INTERCITY_GET_BOOKING_REQUEST = 237;  // triggered for getting booking details for intercity booking fragment
    public static final int INTERCITY_CANCELLATION_REQUEST = 238;
    public static final int CONFIRM_INTERCITY_BOOKING = 239;

    public static final int CAB_SEDAN = 13;
    public static final int CAB_COMPACT = 14;
    public static final int CAB_LUXURY = 15;
    public static final int BIKE = 16;
    public static final int AUTO = 17;
    public static final int SUV = 18;
    public static final int TEMPO = 19;

    public static final int TYPE_UBER = 5;
    public static final int TYPE_OLA = 6;
    public static final int TYPE_EASY = 7;
    public static final int TYPE_JUGNOO = 8;
    public static final int TYPE_MEGA = 9;
    public static final int TYPE_RIDZ = 10;

    /*
    * intercity
    */
    public static final int TYPE_AHA = 31;
    public static final int TYPE_RODER = 32;
    public static final int TYPE_MYTAXI = 33;
    public static final int TYPE_GETMECAB = 34;
    public static final int TYPE_SAVAARI = 35;


    /*
    * intercity types
    */
    public static final int TYPE_AHA_BUSINESS = 41;
    public static final int TYPE_AHA_PREMIUM = 42;
    public static final int TYPE_AHA_ECONOMY = 43;

    public static final int TYPE_MYTAXI_INDIGO = 44;
    public static final int TYPE_MYTAXI_INNOVA = 45;
    public static final int TYPE_MYTAXI_TEMPO_12 = 46;
    public static final int TYPE_MYTAXI_TEMPO_15 = 47;

    public static final int TYPE_RODER_SUV_7 = 48;
    public static final int TYPE_RODER_SUV_8 = 49;
    public static final int TYPE_RODER_SEDAN = 50;
    public static final int TYPE_RODER_HATCHBACK = 51;

    public static final int TYPE_GETMECAB_SEDAN = 52;
    public static final int TYPE_GETMECAB_SUV = 53;

    public static final int TYPE_SAVAARI_INDIGO = 56;
    public static final int TYPE_SAVAARI_INDICA = 57;

    /*
    * payment status
    */

    public static final int TYPE_PAYMENT_PENDING = 21;
    public static final int TYPE_PAYMENT_FAILED = 22;
    public static final int TYPE_PAYMENT_DONE = 23;


    public static final int PAYMENT_REQUEST_CODE_PAYMENT_COLLECTOR = 400;
    public static final int PAYMENT_REQUEST_CODE_THIRD_PARTY_COLLECTOR = 401;
    public static final int PAYMENT_RESULT_CODE = 402;

    /**
     * Databases
     * */
    public static final String CACHE_DB = "CACHE";
    public static final String ADDRESS_DB = "ADDRESSDB";
    public static final String LOCATION_DB = "LOCATIONDB";

    /*
     * Intercity payment status
     */
    public static final int INTERCITY_PAYMENT_PENDING = 21;
    public static final int INTERCITY_PAYMENT_FAILED = 22;
    public static final int INTERCITY_PAYMENT_DONE = 23;

    public static final int INTERCITY= 901;
    public static final int INTRACITY= 902;
    public static final int SELFDRIVE= 903;


    /**
     * Constant to track location identification progress
     */
    public static final int LOCATION_NOT_ENABLED = 0;
    /**
     * Constant to track location identification progress
     */
    public static final int LOCATION_NOT_DETECTED = 1;
    /**
     * Constant to track location identification progress
     */
    public static final int LOCATION_DETECTED = 2;
    /**
     * Constant to track location identification progress
     */
    public static final int GETZONE_CALLED = 3;
    /**
     * Constant to track location identification progress
     */
    public static final int CITY_IDENTIFIED = 4;
    /**
     * Constant to track location identification progress
     */
    public static final int CITY_NOT_IDENTIFIED = 5;
    public static final int LOCATION_DETECTION_RUNNING = 6;
    public static final int DIFFERENT_CITY_IDENTIFIED = 7;

    public static final String NOTIFICATION_TYPE_PROMOTIONAL = "NOTIFICATION_TYPE_PROMOTIONAL";

    public static final String NOTIFICATION_TYPE_CASHBACK_POINTS_ADDED = "NOTIFICATION_TYPE_CASHBACK_POINTS_ADDED";
    public static final String NOTIFICATION_TYPE_REFERRAL_POINTS_ADDED = "NOTIFICATION_TYPE_REFERRAL_POINTS_ADDED";

    public static final String NOTIFICATION_TYPE_BOOKING_CONFIRMED = "NOTIFICATION_TYPE_BOOKING_CONFIRMED";
    public static final String NOTIFICATION_TYPE_BOOKING_CANCELLED = "NOTIFICATION_TYPE_BOOKING_CANCELLED";

    public static final int ADDRESS_TYPE_HOME = 201;
    public static final int ADDRESS_TYPE_WORK = 202;

    /**
     * Phone number for offline booking
     */
    public static final String OFFLINE_MESSAGE = "+918447753585";
    public static final String OFFLINE_CALL = "+918447753585";


    // Return this string for every call
    public static String getVersionString(Context context) {
        String uuidString = "";

        if (prefs == null && context != null)
            prefs = context.getSharedPreferences(APP_SETTINGS, 0);

        if (prefs != null)
            uuidString = "&uuid=" + prefs.getString("app_id", "");

        return SOURCE + uuidString;
    }

    // Calculate the sample size of bitmaps
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;
        double ratioH = (double) options.outHeight / reqHeight;
        double ratioW = (double) options.outWidth / reqWidth;

        int h = (int) Math.round(ratioH);
        int w = (int) Math.round(ratioW);

        if (h > 1 || w > 1) {
            if (h > w) {
                inSampleSize = h >= 2 ? h : 2;

            } else {
                inSampleSize = w >= 2 ? w : 2;
            }
        }
        return inSampleSize;
    }

    public static final Hashtable<String, Typeface> typefaces = new Hashtable<String, Typeface>();

    public static Typeface getTypeface(Context c, String name) {
        synchronized (typefaces) {
            if (!typefaces.containsKey(name)) {
                try {
                    InputStream inputStream = c.getAssets().open(name);
                    File file = createFileFromInputStream(inputStream, name);
                    if (file == null) {
                        return Typeface.DEFAULT;
                    }
                    Typeface t = Typeface.createFromFile(file);
                    typefaces.put(name, t);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Typeface.DEFAULT;
                }
            }
            return typefaces.get(name);
        }
    }

    private static File createFileFromInputStream(InputStream inputStream, String name) {

        try {
            File f = File.createTempFile("font", null);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return f;
        } catch (Exception e) {
            // Logging exception
            e.printStackTrace();
        }

        return null;
    }

    public static int getStatusBarHeight(Context mContext) {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    // Zapplon Logging end points
    public static void ZLog(String Tag, String Message) {
        if (ZapplonLog && Message != null)
            Log.i(Tag, Message);
    }

    public static void ZLog(String Tag, float Message) {
        if (ZapplonLog)
            Log.i(Tag, Message + "");
    }

    public static void ZLog(String Tag, boolean Message) {
        if (ZapplonLog)
            Log.i(Tag, Message + "");
    }

    public static void ZLog(String Tag, int Message) {
        if (ZapplonLog)
            Log.i(Tag, Message + "");
    }

    public static InputStream getStream(HttpResponse response) throws IllegalStateException, IOException {
        InputStream instream = response.getEntity().getContent();
        Header contentEncoding = response.getFirstHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
            instream = new GZIPInputStream(instream);
        }
        return instream;
    }

    // Checks if network is available
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return distance in km
     */

    public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist;
    }

    // Returns the Network State
    public static String getNetworkState(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String returnValue = "";
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                returnValue = "wifi";
            else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                returnValue = "mobile" + "_" + getNetworkType(context);
            else
                returnValue = "Unknown";
        } else
            returnValue = "Not connected";
        return returnValue;
    }

    // Returns the Data Network type
    public static String getNetworkType(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        switch (telephonyManager.getNetworkType()) {

            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xRTT";

            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";

            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE ";

            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "EHRPD ";

            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "EVDO_0 ";

            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "EVDO_A ";

            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "EVDO_B ";

            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS ";

            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA ";

            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA ";

            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPAP ";

            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA ";

            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "IDEN ";

            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE ";

            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS ";

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "UNKNOWN ";

            default:
                return "UNKNOWN ";
        }
    }

    // check done before storing the bitmap in the memory
    public static boolean shouldScaleDownBitmap(Context context, Bitmap bitmap) {
        if (context != null && bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            return ((width != 0 && width / bitmap.getWidth() < 1) || (height != 0 && height / bitmap.getHeight() < 1));
        }
        return false;
    }

    public static boolean isAndroidL() {
        return android.os.Build.VERSION.SDK_INT >= 21;
    }

    public static String getDateFromUTC(long timestamp) {
        Date date = new Date(timestamp);
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        return (cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR) + ":"
                + cal.get(Calendar.MINUTE) + (cal.get(Calendar.AM_PM) == 0 ? "AM" : "PM"));
    }

    public static String getUberTimeString(long seconds) {
        StringBuilder builder = new StringBuilder();

        if (seconds < 60) {
            builder.append(seconds);
            if (seconds == 1)
                builder.append(" sec");
            else
                builder.append(" secs");
            return builder.toString();
        } else if (seconds < (60 * 60)) {
            builder.append((seconds / 60));
            if ((seconds / 60) == 1)
                builder.append(" min");
            else
                builder.append(" mins");
            return builder.toString();
        } else if (seconds < (60 * 60 * 60)) {
            builder.append((seconds / (60 * 60)));
            if ((seconds / (60 * 60)) == 1)
                builder.append(" hour");
            else
                builder.append(" hours");
            return builder.toString();
        } else
            return "";
    }

    public static String getUberTimeStringShort(long estimate, String timeUnit) {
        StringBuilder builder = new StringBuilder();

        if(timeUnit != null) {
            if(timeUnit.startsWith("s")) {
                if (estimate < 60) {
                    builder.append(estimate);
                    builder.append("s");
                    return builder.toString();
                } else if (estimate < (60 * 60)) {
                    builder.append((estimate / 60));
                    builder.append("m");
                    return builder.toString();
                } else if (estimate < (60 * 60 * 60)) {
                    builder.append((estimate / (60 * 60)));
                    builder.append("h");
                    return builder.toString();
                } else
                    return "";
            }
            else if(timeUnit.startsWith("m")) {
                if (estimate < 60) {
                    builder.append(estimate);
                    builder.append("m");
                    return builder.toString();
                } else if (estimate < (60 * 60)) {
                    builder.append((estimate / 60));
                    builder.append("h");
                    return builder.toString();
                } else
                    return "";
            }
            else if(timeUnit.startsWith("h")) {
                builder.append(estimate);
                builder.append("h");
                return builder.toString();
            } else
                return "";
        } else if (estimate < 60) {
            builder.append(estimate);
            builder.append("s");
            return builder.toString();
        } else if (estimate < (60 * 60)) {
            builder.append((estimate / 60));
            builder.append("m");
            return builder.toString();
        } else if (estimate < (60 * 60 * 60)) {
            builder.append((estimate / (60 * 60)));
            builder.append("h");
            return builder.toString();
        } else
            return "";
    }

    public static String getPriceString(String currencyCode, String value, boolean isLeft) {

        if (currencyCode == null)
            return "Rs. " + value;

        if (currencyCode != null && (currencyCode.equals("INR") || currencyCode.equals("")))
            return "Rs. " + value;

        if (isLeft) {
            return currencyCode + " " + value;
        } else {
            return value + " " + currencyCode;
        }
    }


    /**
     * Returns the bitmap associated
     */
    public static Bitmap getBitmap(Context mContext, int resId, int width, int height) throws OutOfMemoryError {
        if (mContext == null)
            return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(mContext.getResources(), resId, options);
        options.inSampleSize = CommonLib.calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Config.RGB_565;

        if (!CommonLib.isAndroidL())
            options.inPurgeable = true;

        Bitmap bitmap = null;

        bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, options);

        return bitmap;
    }

    /**
     * Blur a bitmap with the radius associated
     */
    public static Bitmap fastBlur(Bitmap bitmap, int radius) {
        try {
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();

            int[] pix = new int[w * h];
            CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            Bitmap blurBitmap = bitmap.copy(bitmap.getConfig(), true);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi += w;
                }
            }

            CommonLib.ZLog("pix", w + " " + h + " " + pix.length);
            blurBitmap.setPixels(pix, 0, w, 0, 0, w, h);
            return blurBitmap;

        } catch (OutOfMemoryError e) {
            return bitmap;
        } catch (Exception e) {
            return bitmap;
        }
    }

    public static Bitmap getBitmapFromDisk(String url, Context ctx) {

        Bitmap defautBitmap = null;
        try {
            String filename = constructFileName(url);
            File filePath = new File(ctx.getCacheDir(), filename);

            if (filePath.exists() && filePath.isFile() && !filePath.isDirectory()) {
                FileInputStream fi;
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inPreferredConfig = Config.RGB_565;
                fi = new FileInputStream(filePath);
                defautBitmap = BitmapFactory.decodeStream(fi, null, opts);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        return defautBitmap;
    }

    public static String constructFileName(String url) {
        return url.replaceAll("/", "_");
    }


    public static void addBitmapToDisk(String url, Bitmap bmp, Context ctx) {
        writeBitmapToDisk(url, bmp, ctx, CompressFormat.PNG);
    }

    public static void writeBitmapToDisk(String url, Bitmap bmp, Context ctx, CompressFormat format) {
        FileOutputStream fos;
        String fileName = constructFileName(url);
        try {
            if (bmp != null) {
                fos = new FileOutputStream(new File(ctx.getCacheDir(), fileName));
                bmp.compress(format, 75, fos);
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //GA
    public enum TrackerName {
        GLOBAL_TRACKER,
        APPLICATION_TRACKER
    }

    public static Bitmap getRoundedCornerBitmap(final Bitmap bitmap, final float roundPx) {

        if (bitmap != null) {
            try {
                final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
                Canvas canvas = new Canvas(output);

                final Paint paint = new Paint();
                final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                final RectF rectF = new RectF(rect);

                paint.setAntiAlias(true);
                canvas.drawARGB(0, 0, 0, 0);
                canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

                paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);

                return output;

            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static final int STORE_TYPE_1 = 1;
    public static final int STORE_TYPE_2 = 2;

    public static final int DEAL_TYPE_1 = 1;
    public static final int DEAL_TYPE_2 = 2;
    public static final int DEAL_TYPE_3 = 3;
    public static final int DEAL_TYPE_4 = 4;

    public static String getDealType(Activity mContext, int storeType) {
        switch (storeType) {
            case DEAL_TYPE_1:
                return mContext.getResources().getString(R.string.deal_1);
            case DEAL_TYPE_2:
                return mContext.getResources().getString(R.string.deal_2);
            case DEAL_TYPE_3:
                return mContext.getResources().getString(R.string.deal_3);
            case DEAL_TYPE_4:
                return mContext.getResources().getString(R.string.deal_4);
            default:
                return "";
        }
    }

    public static final int DEAL_SUB_TYPE_FLAT = 1;
    public static final int DEAL_SUB_TYPE_AMOUNT = 2;

    public static String getTimeDifferenceString(Date fromDate) {

        Date currentDate = new Date(System.currentTimeMillis());

        if (currentDate.after(fromDate)) {
            currentDate = new Date(System.currentTimeMillis());
        } else {
            currentDate = fromDate;
            fromDate = new Date(System.currentTimeMillis());
        }

        StringBuilder builder = new StringBuilder();
        int year = currentDate.getYear() - fromDate.getYear();
        int month = currentDate.getMonth() - fromDate.getMonth();
        int date = currentDate.getDate() - fromDate.getDate();
        int hour = currentDate.getHours() - fromDate.getHours();
        int minute = currentDate.getMinutes() - fromDate.getMinutes();

        if (year > 1)
            builder.append(year + " years, ");

        if (month > 1)
            builder.append(month + " months, ");

        if (date > 1)
            builder.append(date + " days, ");
        else if (date == 1)
            builder.append(date + " day, ");

        if (hour > 1)
            builder.append(hour + " hours, ");
        else if (hour == 1)
            builder.append(hour + " hour, ");

        if (minute > 1)
            builder.append(minute + " minutes, ");
        else if (minute == 1)
            builder.append(minute + " minute, ");

        return builder.toString().substring(0, builder.toString().length() - 2);
    }

    /**
     * Remove the keyboard explicitly.
     */
    public static void hideKeyBoard(Activity mActivity, View mGetView) {
        try {
            ((InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(mGetView.getRootView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStatus(int status) {
        StringBuilder returnString = new StringBuilder();

        if (status == CommonLib.BILLING_STATUS_VERIFIED) {
            returnString.append("Billed");
        } else if (status == CommonLib.BILLING_STATUS_BOOKED) {
            returnString.append("Redeemed");
        } else if (status == CommonLib.BILLING_STATUS_RATED) {
            returnString.append("Rated");
        }

        return returnString.toString();
    }

    public static int getStatusColor(Context mContext, int status) {
        int color = 0;

        switch (status) {
            case TRACK_STAGE_BOOKING_PENDING:
            case TRACK_STAGE_BOOKING_FAILED:
            case TRACK_STAGE_NO_BOOKING:
                color = mContext.getResources().getColor(R.color.zdhl);
                break;
            case TRACK_STAGE_BOOKING_RETRYING:
            case TRACK_STAGE_CALL_DRIVER:
            case TRACK_STAGE_CLIENT_LOCATED:
            case TRACK_STAGE_TRIP_START:
                color = mContext.getResources().getColor(R.color.zapplon_blue);
                break;
            case TRACK_STAGE_TRIP_END:
            case TRACK_STAGE_INVOICE:
            case TRACK_STAGE_TRIP_END_CASHBACK:
                color = mContext.getResources().getColor(R.color.active_color);
                break;
            case TRACK_STAGE_BOOKING_CANCELLED:
                color = mContext.getResources().getColor(R.color.zapplon_red);
                break;
        }
        return color;
    }

    //IMEISV
    public static String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imeisv = telephonyManager.getDeviceId();
        if (imeisv == null)
            imeisv = "Unknown";
        return imeisv;
    }


    public static void showSoftKeyboard(Context context, View v) {
        v.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public static String getCabCompanyName(int type) {
        String retValue;
        switch (type) {
            case CommonLib.TYPE_OLA:
                retValue = "OLA";
                break;
            case CommonLib.TYPE_UBER:
                retValue = "uber";
                break;
            default:
                retValue = "";
        }
        return retValue;
    }

    public static boolean isDayTime() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            return true;
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            return true;
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            return true;
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            return false;
        }
        return true;
    }

    public static String getStatusString(int status) {
        StringBuilder builder = new StringBuilder();
        switch (status) {
            case TRACK_STAGE_BOOKING_FAILED:
                builder.append("Booking Failed");
                break;
            case TRACK_STAGE_BOOKING_PENDING:
                builder.append("Booking Pending");
                break;
            case TRACK_STAGE_BOOKING_RETRYING:
                builder.append("Booking Retrying");
                break;
            case TRACK_STAGE_NO_BOOKING:
                builder.append("Not Booked");
                break;
            case TRACK_STAGE_CALL_DRIVER:
                builder.append("Booked");
                break;
            case TRACK_STAGE_CLIENT_LOCATED:
                builder.append("Pickup Complete");
                break;
            case TRACK_STAGE_TRIP_START:
                builder.append("Started");
                break;
            case TRACK_STAGE_TRIP_END:
                builder.append("Finished");
                break;
            case TRACK_STAGE_INVOICE:
                builder.append("Invoiced");
                break;
            case TRACK_STAGE_TRIP_END_CASHBACK:
                builder.append("Finished");
                break;
            case TRACK_STAGE_BOOKING_CANCELLED:
                builder.append("Cancelled");
                break;
        }
        return builder.toString();
    }

    public static int getBrandBitmap(int type) {
        if(type == CommonLib.TYPE_OLA)
            return R.drawable.olaicon;
        else if(type == CommonLib.TYPE_EASY)
            return R.drawable.easycabs_logo;
        else if(type == CommonLib.TYPE_JUGNOO)
            return R.drawable.jugnoo;
        else if(type == CommonLib.TYPE_UBER)
            return R.drawable.uber;
        else if(type == CommonLib.TYPE_MEGA)
            return R.drawable.megacabs;
        else if(type == CommonLib.TYPE_RIDZ)
            return R.drawable.ridz;
        else if(type == CommonLib.TYPE_AHA)
            return R.drawable.aha;
        else if(type == CommonLib.TYPE_MYTAXI)
            return R.drawable.mti;
        else if(type == CommonLib.TYPE_RODER)
            return R.drawable.roder;
        else if(type == CommonLib.TYPE_GETMECAB)
            return R.drawable.gmc;
        else
            return 0;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Proxy white hat hacking check.
     * TODO: add this at every api call and try to own a black hat
     * */
    public static boolean proxyCheck (Context context)
    {
        String proxyAddress = null;
        try {
            if (Build.VERSION.SDK_INT < 14) {
                proxyAddress = android.net.Proxy.getHost(context);
                if (proxyAddress == null || proxyAddress.equals("")) {

                }
                proxyAddress += ":" + android.net.Proxy.getPort(context);
            } else {
                proxyAddress = System.getProperty("http.proxyHost");
                proxyAddress += ":" + System.getProperty("http.proxyPort");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return !(proxyAddress != null && proxyAddress.equalsIgnoreCase("null:null"));
    }

    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static String getFormattedDate (long timeInMillis, boolean timeRequired) {
        Date date = new Date(timeInMillis);
        SimpleDateFormat sdf;
        if(timeRequired)
            sdf = new SimpleDateFormat("dd MMM , hh:mm aa"); // Set your date format
        else
            sdf = new SimpleDateFormat("dd MMM"); // Set your date format
        return sdf.format(date);
    }

}
