package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.UploadManager;
import com.google.android.gms.appinvite.AppInviteInvitation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ReferActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private int width,height;
    private ImageView fbInvite,whatsappInvite, mailInvite, twitterInvite, pinInvite;
    boolean appInviteWorking = true;
    private static final int REQUEST_INVITE = 1010;
    private ProgressDialog zProgressDialog;
    private Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer);

        mContext = this;
        prefs = getSharedPreferences("application_settings", 0);
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        fbInvite = (ImageView) findViewById(R.id.fbInvite);
        whatsappInvite = (ImageView) findViewById(R.id.whatsappInvite);
        mailInvite = (ImageView) findViewById(R.id.mailInvite);
        twitterInvite = (ImageView) findViewById(R.id.twitterInvite);
        pinInvite = (ImageView) findViewById(R.id.pinInvite);

        try {
            fbInvite.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.facebook, width, height));
            whatsappInvite.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.whatsapp, width, height));
            mailInvite.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.email, width, height));
            twitterInvite.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.twitter, width, height));
            pinInvite.setImageBitmap(CommonLib.getBitmap(mContext, R.drawable.pinterest, width, height));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        setUpActionBar();
        install();
        setListeners();
    }

    private void install() {
       if (CommonLib.isAppInstalled(mContext, "com.facebook.katana")) {
            findViewById(R.id.fbInvite).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.fbInvite).setVisibility(View.GONE);
        }

        if (CommonLib.isAppInstalled(mContext, "com.twitter.android")) {
            findViewById(R.id.twitterInvite).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.twitterInvite).setVisibility(View.GONE);
        }

        if (CommonLib.isAppInstalled(mContext, "com.pinterest")) {
            findViewById(R.id.pinInvite).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.pinInvite).setVisibility(View.GONE);
        }

        if (CommonLib.isAppInstalled(mContext, "com.whatsapp")) {
            findViewById(R.id.whatsappInvite).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.whatsappInvite).setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        findViewById(R.id.fbInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInstalled = CommonLib.isAppInstalled(mContext, "com.facebook.katana");
                //boolean isInstalled = true;
                String appUrl = "http://zapplon.com/app?referrer=" + prefs.getInt("uid", -1) + " ";
                if (isInstalled) {

                    zProgressDialog = ProgressDialog.show(mContext, null, "Please wait!!!");
                    Intent fbIntent = new Intent(Intent.ACTION_SEND);
                    fbIntent.setType("text/plain");
                    fbIntent.setPackage("com.facebook.katana");
                    fbIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.refer_app)+"\n" + appUrl);

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

        findViewById(R.id.twitterInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInstalled = CommonLib.isAppInstalled(mContext, "com.twitter.android");
                //boolean isInstalled = true;
                if (isInstalled) {

                    zProgressDialog = ProgressDialog.show(mContext, null, "Please wait!!!");
                    Intent twitterIntent = new Intent(Intent.ACTION_SEND);
                    twitterIntent.setType("text/plain");
                    twitterIntent.setPackage("com.twitter.android");
                    String appUrl = "http://zapplon.com/app?referrer=trackingId_" + prefs.getInt("uid", -1) + " ";
                    String tweet = "http://www.twitter.com/intent/tweet?url="+appUrl+"&text="+getResources().getString(R.string.refer_app);
                    twitterIntent.putExtra(Intent.EXTRA_TEXT, tweet);
                    startActivity(twitterIntent);
                }
                else {
                    Toast.makeText(mContext,"Twitter not installed", Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.twitter.android"));
                    startActivity(openStoreIntent);

                }
            }
        });

        findViewById(R.id.pinInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInstalled = CommonLib.isAppInstalled(mContext, "com.pinterest");
                //boolean isInstalled = true;
                if (isInstalled) {
                    zProgressDialog = ProgressDialog.show(mContext, null, "Please wait!!!");
                    String shareUrl = "http://zapplon.com";
                    String mediaUrl = "https://s3-ap-southeast-1.amazonaws.com/zimage.com/cab_icons/Screen+Shot+2016-06-01+at+5.31.11+PM.png";
                    String description = getResources().getString(R.string.refer_app)+"\n"+
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
                    Toast.makeText(mContext,"Pinterest not installed",Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.pinterest"));
                    startActivity(openStoreIntent);
                }
            }
        });

        findViewById(R.id.whatsappInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isInstalled = CommonLib.isAppInstalled(mContext, "com.whatsapp");
                //boolean isInstalled = true;
                if (isInstalled) {

                    zProgressDialog = ProgressDialog.show(ReferActivity.this, null, "Please wait!!!");
                    Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                    whatsappIntent.setType("text/plain");
                    whatsappIntent.setPackage("com.whatsapp");
                    String appUrl = "http://zapplon.com/app?referrer=trackingId_" + prefs.getInt("uid", -1) + " ";
                    whatsappIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.refer_app)+"\n" + appUrl);

                    startActivity(whatsappIntent);
                }
                else
                {
                    Toast.makeText(mContext, "Whatsapp not installed", Toast.LENGTH_SHORT).show();
                    Intent openStoreIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp"));
                    startActivity(openStoreIntent);
                }
            }
        });

        findViewById(R.id.mailInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appInviteWorking) {
                    int resId = R.drawable.invite;
                    zProgressDialog = ProgressDialog.show(mContext, null, "Please wait!!!");
                    Intent intent = new AppInviteInvitation.IntentBuilder(getResources().getString(R.string.app_invite_title))
                            .setMessage(getResources().getString(R.string.app_invite_description))
                            .setDeepLink(Uri.parse("www.zapplon.com/appInvite/"))
                            .setCustomImage(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(resId) + '/' + getResources().getResourceTypeName(resId) + '/' + getResources().getResourceEntryName(resId)))
                            .setCallToActionText(getResources().getString(R.string.invitation_cta))
                            .build();
                    try{
                    startActivityForResult(intent, REQUEST_INVITE);
                    }
                    catch (android.content.ActivityNotFoundException ex) {  // added due to crashlytics bug #183
                        String shortUrl = "\nhttp://zapplon.com/app?";
                        String shareText = getResources().getString(R.string.app_invite_description) + shortUrl;

                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(Intent.EXTRA_TEXT, shareText);
                        startActivity(Intent.createChooser(i, getResources().getString(R.string.toast_share_longpress)));
                    }
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
            if (resultCode == RESULT_OK) {
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

    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.refer));
        s.setSpan(
                new TypefaceSpan(mContext, CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);
    }

    @Override
    public void onDestroy() {
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            CommonLib.ZLog("", "UTF-8 should always be supported");
            return "";
        }
    }

}
