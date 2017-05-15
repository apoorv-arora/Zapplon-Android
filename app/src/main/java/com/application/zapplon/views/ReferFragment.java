package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ReferFragment extends Fragment {

    boolean appInviteWorking = true;
    private static final int REQUEST_INVITE = 1010;

    boolean isInstalled;

    private ZApplication zapp;
    private Activity activity;
    private View getView;
    private SharedPreferences prefs;
    private int width, height;
    private LayoutInflater vi;
    private boolean destroyed = false;
    private ProgressDialog zProgressDialog;

    private boolean isAppInstalled(String uri) {
        PackageManager pm = getActivity().getPackageManager();
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

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            CommonLib.ZLog("", "UTF-8 should always be supported");
            return "";
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.refertab_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activity = getActivity();
        getView = getView();
         destroyed=false;
        prefs = activity.getSharedPreferences("application_settings", 0);
        zapp = (ZApplication) activity.getApplication();
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        height = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        vi = LayoutInflater.from(activity.getApplicationContext());

        try {
            ((ImageView) getView.findViewById(R.id.fbInvite)).setImageBitmap(CommonLib.getBitmap(activity, R.drawable.facebook, width, height));
            ((ImageView) getView.findViewById(R.id.whatsappInvite)).setImageBitmap(CommonLib.getBitmap(activity, R.drawable.whatsapp, width, height));
            ((ImageView) getView.findViewById(R.id.mailInvite)).setImageBitmap(CommonLib.getBitmap(activity, R.drawable.email, width, height));
            ((ImageView) getView.findViewById(R.id.twitterInvite)).setImageBitmap(CommonLib.getBitmap(activity, R.drawable.twitter, width, height));
            ((ImageView) getView.findViewById(R.id.pinInvite)).setImageBitmap(CommonLib.getBitmap(activity, R.drawable.pinterest, width, height));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        setListeners();
        new GetPoints().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    @Override
    public void onDestroy(){
        destroyed=true;
        if( zProgressDialog != null && zProgressDialog.isShowing() )
            zProgressDialog.dismiss();
        super.onDestroy();
    }


    @Override
    public void onResume(){

        if( zProgressDialog != null && zProgressDialog.isShowing() )
            zProgressDialog.dismiss();
        super.onResume();
    }



    private void setListeners() {
        getView.findViewById(R.id.fbInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInstalled = isAppInstalled("com.facebook.katana");
                String appUrl = "http://zapplon.com/app?referrer=" + prefs.getInt("uid", -1) + " ";
                if (isInstalled) {

                    zProgressDialog = ProgressDialog.show(activity, null, "Please wait!!!");
                    Intent fbIntent = new Intent(Intent.ACTION_SEND);
                    fbIntent.setType("text/plain");
                    fbIntent.setPackage("com.facebook.katana");
                    fbIntent.putExtra(Intent.EXTRA_TEXT, "Hi there!\nInstall this awesome app\n" + appUrl);

                    startActivity(fbIntent);
                }
                else
                {
                    String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + appUrl;
                    Intent fbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                    startActivity(fbIntent);
                }
            }
        });

        getView.findViewById(R.id.twitterInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInstalled = isAppInstalled("com.twitter.android");
                if (isInstalled) {

                        zProgressDialog = ProgressDialog.show(activity, null, "Please wait!!!");
                        Intent twitterIntent = new Intent(Intent.ACTION_SEND);
                        twitterIntent.setType("text/plain");
                        twitterIntent.setPackage("com.twitter.android");
                        String appUrl = "http://zapplon.com/app?referrer=trackingId_" + prefs.getInt("uid", -1) + " ";
                        String tweet = "http://www.twitter.com/intent/tweet?url="+appUrl+"&text=Hi there!\n"+"Install this awesome app\n";
                        twitterIntent.putExtra(Intent.EXTRA_TEXT, tweet);

                    startActivity(twitterIntent);
                }
                else {
                    Toast.makeText(getActivity(),"Twitter not installed",Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.twitter.android"));
                    startActivity(openStoreIntent);

                }
            }
        });

        getView.findViewById(R.id.pinInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInstalled = isAppInstalled("com.pinterest");
                if (isInstalled) {
                    zProgressDialog = ProgressDialog.show(activity, null, "Please wait!!!");
                    String shareUrl = "http://zapplon.com";
                    String mediaUrl = "https://s3-ap-southeast-1.amazonaws.com/zimage.com/cab_icons/Screen+Shot+2016-06-01+at+5.31.11+PM.png";
                    String description = "Hi there!\nInstall this awesome app\n"+
                            "http://zapplon.com/app?referrer=trackingId_";
                    String url = String.format(
                            "https://www.pinterest.com/pin/create/button/?url=%s&media=%s&description=%s",
                            urlEncode(shareUrl), urlEncode(mediaUrl), description);
                    Intent pinIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
                    pinIntent.setType("text/plain");
                    pinIntent.setPackage("com.pinterest");
                    startActivity(pinIntent);
                }
                else
                {
                    Toast.makeText(getActivity(),"Pinterest not installed",Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.pinterest"));
                    startActivity(openStoreIntent);
                }
            }
        });

        getView.findViewById(R.id.whatsappInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInstalled = isAppInstalled("com.whatsapp");
                if (isInstalled) {

                    zProgressDialog = ProgressDialog.show(activity, null, "Please wait!!!");
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");
                    String appUrl = "http://zapplon.com/app?referrer=trackingId_" + prefs.getInt("uid", -1) + " ";
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Hi there!\nInstall this awesome app\n" + appUrl);

                    startActivity(whatsappIntent);
                }
                else
                {
                    Toast.makeText(getActivity(), "Whatsapp not installed", Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp"));
                    startActivity(openStoreIntent);
                }
            }
        });

        getView.findViewById(R.id.mailInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appInviteWorking) {
                    int resId = R.drawable.invite;
                    zProgressDialog = ProgressDialog.show(activity, null, "Please wait!!!");
                    Intent intent = new AppInviteInvitation.IntentBuilder(getResources().getString(R.string.app_invite_title))
                            .setMessage(getResources().getString(R.string.app_invite_description))
                            .setDeepLink(Uri.parse("www.zapplon.com/appInvite/"))
                            .setCustomImage(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId)))
                            .setCallToActionText(getResources().getString(R.string.invitation_cta))
                            .build();
                    startActivityForResult(intent, REQUEST_INVITE);
                } else {
                    String shortUrl = "\nhttp://zapplon.com/app?";
                    String shareText = getResources().getString(R.string.app_invite_description) + shortUrl;

                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_TEXT, shareText);
                    startActivity(Intent.createChooser(i, getResources().getString(R.string.toast_share_longpress)));
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == Activity.RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                StringBuilder invitationIds = new StringBuilder();
                for(int i=0; i<ids.length;i++) {
                    invitationIds.append(ids[i]);
                    invitationIds.append(",");
                    CommonLib.ZLog("ids", ids[i]);
                }
                if(invitationIds.length() > 1) {
                    String invites = invitationIds.toString().substring(0, invitationIds.length() - 1);
                    UploadManager.updateInvitationId(invites);
                }
            } else {
                CommonLib.ZLog("ids", "failed");
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private class GetPoints extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            // set the text as loading points
            ((TextView)getView.findViewById(R.id.earned_points)).setText(getResources().getString(R.string.loading_points));
            ((TextView)getView.findViewById(R.id.earned_points)).setTextColor(getResources().getColor(R.color.zhl_darkest));
            ((TextView)getView.findViewById(R.id.earned_points)).setTextSize(getResources().getDimensionPixelSize(R.dimen.size14));
            super.onPreExecute();
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "user/viewPoints?";
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.GET_POINTS, RequestWrapper.FAV);
                CommonLib.ZLog("url", url);
                return info;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            if (destroyed)
                return;

            if (result != null && result instanceof Integer && ((int)result) != -1) {
                ((TextView)getView.findViewById(R.id.earned_points)).setText(String.valueOf(result));
                ((TextView)getView.findViewById(R.id.earned_points)).setTextColor(getResources().getColor(R.color.zhl_darkest));
                ((TextView)getView.findViewById(R.id.earned_points)).setTextSize(getResources().getDimensionPixelSize(R.dimen.size30));
            }
        }
    }



    @Override
    public void onDestroyView() {
        destroyed = true;
        super.onDestroyView();
    }
}
