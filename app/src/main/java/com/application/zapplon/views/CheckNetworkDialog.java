package com.application.zapplon.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;

/**
 * Created by Harsh on 6/3/2016.
 */
public class CheckNetworkDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_dialog_layout);

        if (!CommonLib.isNetworkAvailable(getApplicationContext()))
        {
            Toast.makeText(getApplicationContext(),"Starting Offline Mode",Toast.LENGTH_SHORT).show();
           // Intent intent = new Intent(CheckNetworkDialog.this,OfflineDialog.class);
            //startActivity(intent);
            //finish();
        }
        else {
            Toast.makeText(getApplicationContext(),"Starting Online Mode",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CheckNetworkDialog.this,SpeechDialog.class);
            startActivity(intent);
            finish();
        }
    }

}