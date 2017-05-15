package com.application.zapplon.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.ZTracker;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Nitin on 4/25/2016.
 */
public class UserSetting extends ActionBarActivity {

    private int width;
    private ZApplication zapp;
    private SharedPreferences prefs;
    private static final int REQUEST_INVITE = 1010;
    private boolean destroyed = false;

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_settings);

        width = getWindowManager().getDefaultDisplay().getWidth();
        prefs = getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) getApplication();

        if (Build.VERSION.SDK_INT > 19) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.active_color_transparent_forty));
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(UserSetting.this).inflateTransition(R.transition.shared_element_transition));
        }
        ((RelativeLayout.LayoutParams)findViewById(R.id.cross).getLayoutParams()).setMargins(0,CommonLib.getStatusBarHeight(UserSetting.this),0,0);

        fixSizes();
        setListeners();
        findViewById(R.id.root_parent).post(new Runnable() {
            @Override
            public void run() {
                if(!destroyed) {
                    if (Build.VERSION.SDK_INT > 20)
                        enterReveal();
                    else
                        findViewById(R.id.root_parent).setVisibility(View.VISIBLE);
                }
            }
        });

        if(prefs.getBoolean("offlineVisibility",false)){
            findViewById(R.id.offline).setVisibility(View.VISIBLE);
            findViewById(R.id.offline_separator).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.offline).setVisibility(View.GONE);
            findViewById(R.id.offline_separator).setVisibility(View.GONE);
        }

        if(prefs.getBoolean("INTERCITY_VISIBIILTY",false) && zapp.cities.size() > 0){
            findViewById(R.id.intercity).setVisibility(View.VISIBLE);
            findViewById(R.id.intercity_separator).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.intercity).setVisibility(View.GONE);
            findViewById(R.id.intercity_separator).setVisibility(View.GONE);
        }

        if(prefs.getBoolean("SELF_DRIVE_VISIBILITY",false)){
            findViewById(R.id.selfdrive).setVisibility(View.VISIBLE);
            findViewById(R.id.selfdrive_separator).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.selfdrive).setVisibility(View.GONE);
            findViewById(R.id.selfdrive_separator).setVisibility(View.GONE);
        }
    }

    private void fixSizes() {
        ((TextView) findViewById(R.id.drawer_user_name)).setText("Hi " + prefs.getString("username", ""));
        ImageView imageBlur = (ImageView) findViewById(R.id.drawer_user_info_blur_background_image);
        setImageFromUrlOrDisk(prefs.getString("profile_pic", ""), imageBlur, "", width, width, false);
    }

    private void setListeners() {
        findViewById(R.id.cross).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void aboutus(View view) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_ABOUT_PRESSED, "");
        Intent intent = new Intent(this, AboutUs.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void settings(View view) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_SETTINGS_DRAWER_PRESSED, "");
        Intent intent = new Intent(this, Settings.class);
        intent.putExtra("finish_on_touch_outside", false);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void offline(View view){
        Intent intent = new Intent(this, OfflineDialog.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void intercity(View view){
        Intent intent = new Intent(this, IntercityActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void selfdrive(View view){
        Intent intent = new Intent(this, Selfdrive.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    private GoogleCloudMessaging gcm;

    private void unregisterInBackground() {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(UserSetting.this);
                    }

                    gcm.unregister();

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void logoutConfirm(View V) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_SIGN_OUT_PRESSED, "");
        final AlertDialog logoutDialog;
        logoutDialog = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.logout))
                .setMessage(getResources().getString(R.string.logout_confirm))
                .setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // disconnect facebook
                        try {

                            com.facebook.Session fbSession = com.facebook.Session.getActiveSession();
                            if (fbSession != null) {
                                fbSession.closeAndClearTokenInformation();
                            }
                            com.facebook.Session.setActiveSession(null);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        String accessToken = prefs.getString("access_token", "");
                        UploadManager.logout(accessToken);

                        zapp.logout();

                        //To stop getting notifications after logout
                        unregisterInBackground();

                        if (prefs.getInt("uid", 0) == 0) {
                            Intent intent = new Intent(zapp, SplashScreen.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).setNegativeButton(getResources().getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                .create();
        logoutDialog.show();
    }

    void enterReveal() {
        // previously invisible view
        final View myView = findViewById(R.id.root_parent);
        final View crossView = findViewById(R.id.cross);

        // get the center for the clipping circle
        int cx = (int) findViewById(R.id.cross).getX() + crossView.getWidth() / 2;
        int cy = (int) findViewById(R.id.cross).getY() + crossView.getHeight() / 2;

        // get the final radius for the clipping circle
        int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

        // make the view visible and start the animation
        myView.setVisibility(View.VISIBLE);
        anim.setDuration(CommonLib.REVEAL_ANIM_TIMER);
        anim.start();
    }

    void exitReveal() {
        // previously visible view
        final View myView = findViewById(R.id.root_parent);

        final View crossView = findViewById(R.id.cross);

        // get the center for the clipping circle
        int cx = (int) findViewById(R.id.cross).getX() + crossView.getWidth() / 2;
        int cy = (int) findViewById(R.id.cross).getY() + crossView.getHeight() / 2;

        // get the initial radius for the clipping circle
        int initialRadius = Math.min(myView.getWidth(), myView.getHeight());

        // create the animation (the final radius is zero)
        Animator anim =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                myView.setVisibility(View.INVISIBLE);
            }
        });

        // start the animation
        anim.setDuration(CommonLib.REVEAL_ANIM_TIMER);
        anim.start();
    }

    /*public void feedback(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_FEEDBACK_PRESSED, "");
        startActivity(new Intent(this, FeedbackPage.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }*/

    public void myBookings(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_MY_BOOKINGS_PRESSED, "");
        startActivity(new Intent(this, MyBookings.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void connectedAccounts(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_CONNECTED_ACCOUNTS_PRESSED, "");
        startActivity(new Intent(this, ConnectedAccounts.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    public void redeem(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_REDEEM_PRESSED, "");
        startActivity(new Intent(this, Redeem.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    boolean appInviteWorking = false;

    public void inviteFriends(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_INVITE_PRESSED, "");
        if (appInviteWorking) {
            int resId = R.drawable.invite;
            Intent intent = new AppInviteInvitation.IntentBuilder(getResources().getString(R.string.app_invite_title))
                    .setMessage(getResources().getString(R.string.app_invite_description))
                    .setDeepLink(Uri.parse("www.zapplon.com/appInvite/"))
                    .setCustomImage(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId)))
                    .setCallToActionText(getResources().getString(R.string.invitation_cta))
                    .build();
            startActivityForResult(intent, REQUEST_INVITE);
        } else {
            startActivity(new Intent(this, ReferActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    public void rate(View v) {
        ZTracker.logGAEvent(UserSetting.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_RATE_US_PRESSED, "");
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            startActivity(browserIntent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void setImageFromUrlOrDisk(final String url, final ImageView imageView, final String type, int width,
                                       int height, boolean useDiskCache) {

        if (cancelPotentialWork(url, imageView)) {

            GetImage task = new GetImage(url, imageView, width, height, useDiskCache, type);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(getResources(), zapp.cache.get(url + type), task);
            imageView.setImageDrawable(asyncDrawable);
            if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                    && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
            }
            if (zapp.cache.get(url + type) == null) {
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1L);
            } else if (imageView != null && imageView.getDrawable() != null
                    && ((BitmapDrawable) imageView.getDrawable()).getBitmap() != null) {
                imageView.setBackgroundResource(0);
                if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                }
            }
        }
    }

    private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<GetImage> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, GetImage bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<GetImage>(bitmapWorkerTask);
        }

        public GetImage getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public boolean cancelPotentialWork(String data, ImageView imageView) {
        final GetImage bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.url;
            if (!bitmapData.equals(data)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was
        // cancelled
        return true;
    }

    private GetImage getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    private class GetImage extends AsyncTask<Object, Void, Bitmap> {

        String url = "";
        private WeakReference<ImageView> imageViewReference;
        private int width;
        private int height;
        boolean useDiskCache;
        String type;

        public GetImage(String url, ImageView imageView, int width, int height, boolean useDiskCache, String type) {
            this.url = url;
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.useDiskCache = true;// useDiskCache;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null && imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                        && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar)
                    ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.VISIBLE);
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            try {

                String url2 = url + type;

                if (destroyed && (imageViewReference.get() != findViewById(R.id.user_image))) {
                    return null;
                }

                if (useDiskCache) {
                    bitmap = CommonLib.getBitmapFromDisk(url2, getApplicationContext());
                }

                if (bitmap == null) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        opts.inSampleSize = CommonLib.calculateInSampleSize(opts, width, height);
                        opts.inJustDecodeBounds = false;

                        bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent(), null, opts);

                        if (useDiskCache) {
                            CommonLib.writeBitmapToDisk(url2, bitmap, getApplicationContext(),
                                    Bitmap.CompressFormat.JPEG);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (Error e) {
                        e.printStackTrace();

                    }
                }

                if (bitmap != null) {
                    synchronized (zapp.cache) {
                        zapp.cache.put(url2, bitmap);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (!destroyed) {
                if (isCancelled()) {
                    bitmap = null;
                }
                if (imageViewReference != null && bitmap != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        if (imageView.getParent() != null && imageView.getParent() instanceof ViewGroup
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) != null
                                && ((ViewGroup) imageView.getParent()).getChildAt(2) instanceof ProgressBar) {
                            ((ViewGroup) imageView.getParent()).getChildAt(2).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Build.VERSION.SDK_INT > 20)
            exitReveal();
        else
            overridePendingTransition(R.anim.slide_in_right, R.anim.abc_slide_out_bottom);
    }


    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }

    }

}
