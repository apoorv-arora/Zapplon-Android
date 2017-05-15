package com.application.zapplon.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.data.ConnectedAccount;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.TypefaceSpan;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.ZCabWebView;

import java.util.ArrayList;

public class ConnectedAccounts extends AppCompatActivity implements UploadManagerCallback {

    private int width;
    private boolean destroyed;
    private AsyncTask mAsyncRunning;
    ProgressDialog zProgressDialog;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_accounts);
        width = getWindowManager().getDefaultDisplay().getWidth();
        prefs = getSharedPreferences("application_settings", 0);
        setUpActionBar();
        refreshView();
        setListeners();
        UploadManager.addCallback(this);
    }

    private void setUpActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        SpannableString s = new SpannableString(getResources().getString(R.string.connected_accounts));
        s.setSpan(
                new TypefaceSpan(getApplicationContext(), CommonLib.BOLD_FONT_FILENAME,
                        getResources().getColor(R.color.white), getResources().getDimension(R.dimen.size16)),
                0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        final boolean isAndroidL = Build.VERSION.SDK_INT >= 21; // Build.AndroidL
        if (!isAndroidL)
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.zapplon_dark_feedback));

        actionBar.setTitle(s);
    }

    private void refreshView() {
        if (mAsyncRunning != null)
            mAsyncRunning.cancel(true);

        mAsyncRunning = new GetCabSessionObjects().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public void setListeners() {

        findViewById(R.id.ola_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("application_settings", 0);
                String olaToken = prefs.getString("ola_access_token", "");
                if (olaToken != null && olaToken.length() > 0) {
                    //alredy connected
                } else {
                    Intent intent = new Intent(ConnectedAccounts.this, ZCabWebView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", "https://devapi.olacabs.com/oauth2/authorize?response_type=token&client_id=ODA4OGRkZDUtM2EzZS00YWQ5LTlmOTktNDI2MTg1NTU1YTQz&redirect_uri=http://zapplon.com/&scope=profile%20booking&state=state123\n");
                    bundle.putString("title", "Ola");
                    intent.putExtras(bundle);
                    startActivityForResult(intent, CommonLib.REQUEST_CODE_OLA_WEB_VIEW);
                }
            }
        });
        findViewById(R.id.uber_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("application_settings", 0);
                String uberToken = prefs.getString("uber_access_token", "");
                if (uberToken != null && uberToken.length() > 0) {
                    //alredy connected
                } else {
                    Intent intent = new Intent(ConnectedAccounts.this, ZCabWebView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", "https://login.uber.com/oauth/authorize?client_id=YA36hEXTQ3o4SVWI9yADTXv9wb2J0GyQ&name=Zapplon-Android&redirect_uri=https%3A%2F%2Fzapplon.com%2Fuber&scope=profile&response_type=code\n");
                    bundle.putString("title", "Uber");
                    intent.putExtras(bundle);
                    startActivityForResult(intent, CommonLib.REQUEST_CODE_UBER_WEB_VIEW);
                }
            }
        });

        findViewById(R.id.disconnect_ola).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog logoutDialog;
                logoutDialog = new AlertDialog.Builder(ConnectedAccounts.this).setTitle(getResources().getString(R.string.logout_ola))
                        .setMessage(getResources().getString(R.string.logout_confirm_ola))
                        .setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String olaToken = prefs.getString("ola_access_token", "");
                                String cabSessionId = prefs.getString("ola_cab_session_id", "");

                                UploadManager.disconnectCabBooking(CommonLib.TYPE_OLA, olaToken);

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove("ola_access_token");
                                editor.remove("ola_cab_session_id");
                                editor.commit();

                                zProgressDialog = ProgressDialog.show(ConnectedAccounts.this, null, "Disconnecting OLA. Please wait!!!");
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
        });

        findViewById(R.id.disconnect_uber).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog logoutDialog;
                logoutDialog = new AlertDialog.Builder(ConnectedAccounts.this).setTitle(getResources().getString(R.string.logout_uber))
                        .setMessage(getResources().getString(R.string.logout_confirm_uber))
                        .setPositiveButton(getResources().getString(R.string.logout), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String olaToken = prefs.getString("uber_access_token", "");
                                String cabSessionId = prefs.getString("uber_cab_session_id", "");

                                UploadManager.disconnectCabBooking(CommonLib.TYPE_UBER, olaToken);

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove("uber_access_token");
                                editor.remove("uber_cab_session_id");
                                editor.commit();

                                zProgressDialog = ProgressDialog.show(ConnectedAccounts.this, null, "Disconnecting UBER. Please wait!!!");
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
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CommonLib.REQUEST_CODE_OLA_WEB_VIEW && !destroyed) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == CommonLib.REQUEST_CODE_OLA_WEB_VIEW) {
                    Bundle bundle = data.getExtras();
                    String token = bundle.getString("token");
                    long deletionTime = bundle.getLong("deletionTime", 0);
                    zProgressDialog = ProgressDialog.show(ConnectedAccounts.this, null, "Logging you in OLA. Please wait!!!");
                    UploadManager.sendCabToken(token, deletionTime, CommonLib.TYPE_OLA, "ConnectedAccounts", 0);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        if(zProgressDialog != null && zProgressDialog.isShowing())
            zProgressDialog.dismiss();
        UploadManager.removeCallback(this);
        super.onDestroy();
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

    public void actionBarSelected(View v) {

        switch (v.getId()) {

            case R.id.home_icon_container:
                onBackPressed();

            default:
                break;
        }

    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if (requestType == CommonLib.SEND_CAB_TOKEN && stringId != null && stringId.equals("ConnectedAccounts")) {
            if(destroyed)
                return;
            if(zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();
            if (status) {
                //save the token here to preferences.
                if (data != null && data instanceof ConnectedAccount) {
                    SharedPreferences.Editor editor = prefs.edit();
                    ConnectedAccount account = (ConnectedAccount)data;
                    if(objectId == CommonLib.TYPE_OLA) {
                        ((TextView)findViewById(R.id.ola_text)).setText(getResources().getString(R.string.connected_to_ola));
                        findViewById(R.id.disconnect_ola).setVisibility(View.VISIBLE);
                        editor.putString("ola_access_token", account.getAccessToken());
                        editor.putString("ola_cab_session_id", account.getCabSessionId() + "");
                    } else if(objectId == CommonLib.TYPE_UBER) {
                        ((TextView)findViewById(R.id.uber_text)).setText(getResources().getString(R.string.connected_to_uber));
                        findViewById(R.id.disconnect_uber).setVisibility(View.VISIBLE);
                        editor.putString("uber_access_token", account.getAccessToken());
                        editor.putString("uber_cab_session_id", account.getCabSessionId() + "");
                    }
                    editor.commit();
                }
            }
        } else if(requestType == CommonLib.DISCONNECT_CONNECTED_ACCOUNTS) {
            if(destroyed)
                return;
            if(zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();
            if(status) {
                refreshView();
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }

    private class GetCabSessionObjects extends AsyncTask<Object, Void, Object> {

        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_container).setVisibility(View.VISIBLE);

            findViewById(R.id.content).setAlpha(1f);

            findViewById(R.id.content).setVisibility(View.GONE);

            findViewById(R.id.empty_view).setVisibility(View.GONE);
            super.onPreExecute();
        }

        // execute the api
        @Override
        protected Object doInBackground(Object... params) {
            try {
                CommonLib.ZLog("API RESPONSER", "CALLING GET WRAPPER");
                String url = "";
                url = CommonLib.SERVER + "booking/connectedAccounts?deviceId="+CommonLib.getIMEI(ConnectedAccounts.this);
                Object info = RequestWrapper.RequestHttp(url, RequestWrapper.CONNECTED_ACCOUNTS, RequestWrapper.FAV);
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

            findViewById(R.id.progress_container).setVisibility(View.GONE);

            if (result != null) {
                findViewById(R.id.content).setVisibility(View.VISIBLE);
                if (result instanceof ArrayList<?>) {
                    setConnectedAccounts((ArrayList<ConnectedAccount>) result);
                    findViewById(R.id.content).setVisibility(View.VISIBLE);
                    findViewById(R.id.empty_view).setVisibility(View.GONE);
                }
            } else {
                if (CommonLib.isNetworkAvailable(ConnectedAccounts.this)) {
                    Toast.makeText(ConnectedAccounts.this, getResources().getString(R.string.error_try_again),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ConnectedAccounts.this, getResources().getString(R.string.no_internet_message), Toast.LENGTH_SHORT)
                            .show();

                    findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.content).setVisibility(View.GONE);
                }
            }
        }
    }

    private void setConnectedAccounts(ArrayList<ConnectedAccount> connectedAccounts) {

        if(connectedAccounts == null)
            return;
        ((TextView)findViewById(R.id.ola_text)).setText(getResources().getString(R.string.connect_to_ola));
        findViewById(R.id.disconnect_ola).setVisibility(View.GONE);
        ((TextView)findViewById(R.id.uber_text)).setText(getResources().getString(R.string.connect_to_uber));
        findViewById(R.id.disconnect_uber).setVisibility(View.GONE);

        for(ConnectedAccount account: connectedAccounts) {
            switch (account.getCabCompany()) {
                case CommonLib.TYPE_OLA:
                    if(account.getAccessToken() != null && !account.getAccessToken().equals("")) {
                        ((TextView)findViewById(R.id.ola_text)).setText(getResources().getString(R.string.connected_to_ola));
                        findViewById(R.id.disconnect_ola).setVisibility(View.VISIBLE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("ola_access_token", account.getAccessToken());
                        editor.putString("ola_cab_session_id", account.getCabSessionId()+"");
                        editor.commit();
                    }
                    break;
                case CommonLib.TYPE_UBER:
                    if(account.getAccessToken() != null && !account.getAccessToken().equals("")) {
                        ((TextView)findViewById(R.id.uber_text)).setText(getResources().getString(R.string.connected_to_uber));
                        findViewById(R.id.disconnect_uber).setVisibility(View.VISIBLE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("uber_access_token", account.getAccessToken());
                        editor.putString("uber_cab_session_id", account.getCabSessionId() + "");
                        editor.commit();
                    }
                    break;
            }
        }
    }

}
