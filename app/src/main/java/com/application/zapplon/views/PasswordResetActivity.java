package com.application.zapplon.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.CryptoHelper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

/**
 * Created by pratiksaxena on 07/06/16.
 */
public class PasswordResetActivity extends ActionBarActivity implements UploadManagerCallback {

    int width;
    private boolean destroyed = false;
    private ProgressDialog zProgressDialog;
    private Activity mContext;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password_dialog);
        mContext = this;
        prefs = getSharedPreferences("application_settings", 0);
        ((TextView)findViewById(R.id.old_password)).setVisibility(View.VISIBLE);

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = ((TextView)findViewById(R.id.old_password)).getText().toString();
                String newPassword = ((TextView)findViewById(R.id.new_password)).getText().toString();

                if(oldPassword.length() < 1)
                    Toast.makeText(mContext, "Invalid Old Password", Toast.LENGTH_SHORT).show();

                if(newPassword.length() < 1)
                    Toast.makeText(mContext, "Invalid New Password", Toast.LENGTH_SHORT).show();

                zProgressDialog = ProgressDialog.show(mContext, null, getResources().getString(R.string.verifying_creds));

                CryptoHelper helper = new CryptoHelper();
                try {
                    newPassword = helper.encrypt(newPassword, null, null);
                    oldPassword = helper.encrypt(oldPassword, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                UploadManager.updatePassword(prefs.getString("email", ""), oldPassword, newPassword, "");

            }
        });
        UploadManager.addCallback(this);
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
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.SET_PASSWORD) {
            if(destroyed)
                return;
            if(zProgressDialog != null && zProgressDialog.isShowing())
                zProgressDialog.dismiss();
            if(status)
                finish();
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
