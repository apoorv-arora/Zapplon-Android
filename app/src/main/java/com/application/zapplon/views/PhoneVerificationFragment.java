package com.application.zapplon.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;

/**
 * Created by apoorvarora on 15/02/16.
 */
public class PhoneVerificationFragment extends Fragment implements UploadManagerCallback {

    private View getView;
    private boolean destroyed = false;
    private int width;

    public static PhoneVerificationFragment newInstance(Bundle bundle) {
        PhoneVerificationFragment fragment = new PhoneVerificationFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.phone_verification_fragment, null);
        destroyed = false;
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int type = 0;
        getView = getView();
        destroyed = false;
        width = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        ((EditText)getView.findViewById(R.id.phone_number)).setInputType(InputType.TYPE_CLASS_PHONE);

        UploadManager.addCallback(this);

        View verifyButton = getView.findViewById(R.id.proceed_button);
        ((LinearLayout.LayoutParams) verifyButton.getLayoutParams()).setMargins(0, width / 20, 0, width / 20);
        verifyButton.getLayoutParams().height = 3 * width / 20;

        setListeners();
    }

    private void setListeners() {
        getView.findViewById(R.id.proceed_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mobile = ((TextView)getView.findViewById(R.id.phone_number)).getText().toString();

                if(mobile == null || mobile.length() < 10 || mobile.length() > 10) {
                    Toast.makeText(getActivity(), "Invalid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                UploadManager.phoneVerification( CommonLib.getIMEI(getActivity()), mobile, "");

                //show loader till the response is completed
                getView.findViewById(R.id.proceed_button_progress).setVisibility(View.VISIBLE);
                getView.findViewById(R.id.proceed_button_text).setVisibility(View.GONE);

                CommonLib.hideKeyBoard(getActivity(), getView.findViewById(R.id.phone_number));
            }
        });

    }

    @Override
    public void onDestroyView() {
        destroyed = true;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        destroyed = true;
        UploadManager.removeCallback(this);
        super.onDestroy();
    }

    @Override
    public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId) {
        if(requestType == CommonLib.PHONE_VERIFICATION) {
            if(!destroyed) {
                getView.findViewById(R.id.proceed_button_progress).setVisibility(View.GONE);
                getView.findViewById(R.id.proceed_button_text).setVisibility(View.VISIBLE);
                if(status) {
                    ((CheckPhoneVerificationActivity)getActivity()).setSubCategoryFragment(((TextView)getView.findViewById(R.id.phone_number)).getText().toString());
                } else {
                    Toast.makeText(getActivity(), "Something went wrong in the phone verification. Please try after some time.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void uploadStarted(int requestType, int objectId, String stringId, Object object) {

    }
}
