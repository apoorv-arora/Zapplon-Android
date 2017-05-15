package com.application.zapplon.views;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.ZApplication;
import com.application.zapplon.data.AppConfig;
import com.application.zapplon.db.AddressDBWrapper;
import com.application.zapplon.services.AppConfigService;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.CryptoHelper;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.UploadManagerCallback;
import com.application.zapplon.utils.ZTracker;
import com.application.zapplon.utils.ZWebView;
import com.application.zapplon.utils.facebook.FacebookConnect;
import com.application.zapplon.utils.facebook.FacebookConnectCallback;
import com.application.zapplon.utils.location.ZLocationCallback;
import com.crashlytics.android.Crashlytics;
import com.facebook.Session;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class SplashScreen extends Activity
		implements FacebookConnectCallback, UploadManagerCallback, ZLocationCallback, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener {

	private int width;
	private int height;
	private Activity mContext;
	private boolean destroyed = false;
	private ViewPager mViewPager;
	private LinearLayout mSignupContainer;
	ProgressDialog zProgressDialog;
	LayoutInflater inflater;
	// private TourPagerAdapter mTourPagerAdpater;

	private ProgressDialog z_ProgressDialog;
	private boolean dismissDialog = false;

	/** Constant, randomly selected */
	public final int RESULT_FACEBOOK_LOGIN_OK = 1432; // Random numbers
	public final int RESULT_GOOGLE_LOGIN_OK = 1434;
	private String error_responseCode = "";
	private String error_exception = "";
	private String error_stackTrace = "";
	private boolean windowHasFocus = false;
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	String regId;
	int hardwareRegistered = 0;
	private SharedPreferences prefs;
	private ZApplication zapp;
	private String APPLICATION_ID;

	private GoogleApiClient mGoogleApiClient;
	private static final int DIALOG_GET_GOOGLE_PLAY_SERVICES = 1;

	public static final int RC_SIGN_IN = 0;
	public String TAG = "Google Plus Login";
	/* Should we automatically resolve ConnectionResults when possible? */
	protected boolean mShouldResolve = false;
	private boolean mIsResolving = false;
	boolean isActivityRunning;

	private boolean hasSwipedPager = false;

	private boolean insideApp = false;

	AlertDialog fPasswordDialog;
	AlertDialog resetDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		inflater = LayoutInflater.from(this);

		setContentView(R.layout.activity_splash_screen);

		// initialize variables
		mContext = this;
		width = getWindowManager().getDefaultDisplay().getWidth();
		height = getWindowManager().getDefaultDisplay().getHeight();

		prefs = getSharedPreferences("application_settings", 0);
		zapp = (ZApplication) getApplication();
		APPLICATION_ID = prefs.getString("app_id", "");

		loginPage = (RelativeLayout) findViewById(R.id.main_login_container);
		signUpPage = (RelativeLayout) findViewById(R.id.main_signup_container);


		mShouldResolve = false;
		registerGoogleApiClient();

		if(getIntent() != null && getIntent().getExtras() != null)
			insideApp = getIntent().getBooleanExtra("insideApp", false);

		// initialize views
		mViewPager = (ViewPager) findViewById(R.id.tour_view_pager);
		mSignupContainer = (LinearLayout) findViewById(R.id.signup_container);

		TourPagerAdapter mTourPagerAdpater = new TourPagerAdapter();
		((ViewPager) mViewPager).setAdapter(mTourPagerAdpater);

		((ViewPager) mViewPager).setOnPageChangeListener(new OnPageChangeListener() {
			int position = ((ViewPager) mViewPager).getOffscreenPageLimit();

			@Override
			public void onPageSelected(int arg0) {

				LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);

				int index = 5;
				for (int count = 0; count < index; count++) {
					ImageView dots = (ImageView) dotsContainer.getChildAt(count);

					if (count == arg0)
						dots.setImageResource(R.drawable.tour_image_dots_selected);
					else
						dots.setImageResource(R.drawable.tour_image_dots_unselected);
				}

				if (arg0 != 4)
					findViewById(R.id.skip_container).setVisibility(View.VISIBLE);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});


		((ViewPager) mViewPager).setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hasSwipedPager = true;
				return false;
			}
		});

		animate();
		UploadManager.addCallback(this);
		findViewById(R.id.facebook_login).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				facebookAction(v);
			}
		});
		findViewById(R.id.google_login).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				googleAction(v);
			}
		});

		updateDotsContainer();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!destroyed)
					startTimer();
			}
		}, 1000);

		findViewById(R.id.skip_container).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mViewPager != null) {
					hasSwipedPager = true;

					mViewPager.setCurrentItem(4, true);
				}
			}
		});

		findViewById(R.id.skip_login).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SplashScreen.this, Home.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			}
		});

		//initialize views
		if (prefs.getInt("uid", 0) != 0) {
			findViewById(R.id.skip_container).setVisibility(View.GONE);
			findViewById(R.id.skip_login).setVisibility(View.GONE);

		} else {
			findViewById(R.id.skip_container).setVisibility(View.VISIBLE);
			findViewById(R.id.skip_login).setVisibility(View.VISIBLE);

		}

		// start location check
		zapp.zll.forced = true;
		zapp.zll.addCallback(this);
		zapp.startLocationCheck();
		fixSizes();

		//app config
		startAppConfigService();

	}

	private void fixSizes() {
		mViewPager.getLayoutParams().height = 2 * height / 3 + width / 40;
//		mSignupContainer.getLayoutParams().height = height / 3 - width / 10 - width / 40 - width / 20;
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == RC_SIGN_IN) {

			// If the error resolution was not successful we should not
			// resolve further.
			if (resultCode != RESULT_OK) {
				mShouldResolve = false;
				if (z_ProgressDialog != null)
					z_ProgressDialog.dismiss();
			}

			mIsResolving = false;
			if (mGoogleApiClient == null)
				registerGoogleApiClient();
			mGoogleApiClient.connect();
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing()) {
				z_ProgressDialog.dismiss();
				z_ProgressDialog = ProgressDialog.show(this, null, "Getting google login details.Please wait..");
			}

		} else if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
			checkPlayServices();

			if (z_ProgressDialog != null)
				z_ProgressDialog.dismiss();

		} else {
			try {
				super.onActivityResult(requestCode, resultCode, intent);
				Session.getActiveSession().onActivityResult(this, requestCode, resultCode, intent);

			} catch (Exception w) {

				w.printStackTrace();

				try {
					Session fbSession = Session.getActiveSession();
					if (fbSession != null) {
						fbSession.closeAndClearTokenInformation();
					}
					Session.setActiveSession(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		isActivityRunning = true;
		if (dismissDialog) {
			if (z_ProgressDialog != null) {
				z_ProgressDialog.dismiss();
			}
		}
	}

	@Override
	protected void onPause() {
		isActivityRunning = false;
		removeDefaultGoogleLogin();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		destroyed = true;
		if (zProgressDialog != null && zProgressDialog.isShowing()) {
			zProgressDialog.dismiss();
		}
		zapp.zll.removeCallback(this);
		UploadManager.removeCallback(this);
		super.onDestroy();
	}

	private Animation animation1, animation2 ,animation3;

	private void animate() {

		try {
			final View tourDots = findViewById(R.id.tour_dots);

			animation2 = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right);
			animation2.setDuration(500);
			animation2.restrictDuration(700);
			animation2.scaleCurrentDuration(1);

			animation3 = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_bottom);
			animation3.setDuration(400);
			animation3.restrictDuration(700);
			animation3.scaleCurrentDuration(1);
			animation3.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {

					tourDots.setVisibility(View.VISIBLE);
					tourDots.startAnimation(animation2);
					mSignupContainer.setVisibility(View.VISIBLE);

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});


			animation1 = AnimationUtils.loadAnimation(mContext, R.anim.slide_up_center);
			animation1.setDuration(700);
			animation1.restrictDuration(700);
			animation1.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (prefs.getInt("uid", 0) == 0) {
						mSignupContainer.startAnimation(animation3);

					} else {
						checkPlayServices();
					}
				}
			});
			animation1.scaleCurrentDuration(1);
			mViewPager.startAnimation(animation1);


		} catch (Exception e) {
			mViewPager.setVisibility(View.VISIBLE);
			mSignupContainer.setVisibility(View.VISIBLE);
			findViewById(R.id.tour_dots).setVisibility(View.VISIBLE);
		}
	}

	boolean enter=false;
	EditText editText ;
	@Override
	public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status,
							   final String stringId) {
		if (requestType == CommonLib.REQUEST_PRE_SIGNUP) {
			if (zProgressDialog != null) {
				zProgressDialog.dismiss();
			}
		} else if (requestType == CommonLib.GOOGLE_LOGIN) {
			CommonLib.ZLog("splashlogin", "inside uploadfinished");
			if (zProgressDialog != null) {
				zProgressDialog.dismiss();
			}
			CommonLib.ZLog("splashlogin", "inside uploadfinished");
			if(status && !destroyed && data instanceof JSONObject) {
				CommonLib.ZLog("splashlogin", "inside if block");
				JSONObject res = (JSONObject) data;
				if (res != null) {
					String errorMessage = "";
					int uid = 0;
					String uname = "";
					String accessToken = "";
					String thumbUrl = "";
					String profile_pic = "";
					String email = "";
					boolean verified_user = false;

					try {
						if( res != null && res.has("access_token") && res.has("user_id") && res.get("user_id") instanceof Integer) {
							accessToken = res.getString("access_token");
							uid = res.getInt("user_id");
							if(res.has("user")) {
								res = res.getJSONObject("user");
								if(res.has("user")) {
									res = res.getJSONObject("user");
									if(res.has("profile_pic")) {
										profile_pic = String.valueOf(res.get("profile_pic"));
									}
									if(res.has("email")) {
										email = String.valueOf(res.get("email"));
									}
									if(res.has("username")) {
										uname = String.valueOf(res.get("username"));
									} else if(res.has("user_name")) {
										uname = String.valueOf(res.get("user_name"));
									}
								}
							}
						} else {
							errorMessage = "Something went wrong";
						}
					} catch (Exception e) {
						e.printStackTrace();
						CommonLib.ZLog("splashlogin", "inside exception");
					} finally {
						CommonLib.ZLog("splashlogin", "inside finally");
						Bundle bundle = new Bundle();
						bundle.putInt("uid", uid);
						bundle.putString("profile_pic", profile_pic);
						bundle.putString("email", email);
						bundle.putString("username", uname);
						bundle.putString("thumbUrl", thumbUrl);
						bundle.putString("access_token", accessToken);
						bundle.putBoolean("verifiedUser", verified_user);

						Editor editor = prefs.edit();
						editor.putInt("uid", bundle.getInt("uid"));
						if (bundle.containsKey("email"))
							editor.putString("email", bundle.getString("email"));
						if (bundle.containsKey("username"))
							editor.putString("username", bundle.getString("username"));
						if (bundle.containsKey("thumbUrl"))
							editor.putString("thumbUrl", bundle.getString("thumbUrl"));
						if (bundle.containsKey("profile_pic"))
							editor.putString("profile_pic", bundle.getString("profile_pic"));
						if (bundle.containsKey("description"))
							editor.putString("description", bundle.getString("description"));
						if (bundle.containsKey("user_name"))
							editor.putString("username", bundle.getString("username"));
						String token = bundle.getString("access_token");
						System.out.println(token);
						editor.putString("access_token", bundle.getString("access_token"));
						editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
						editor.commit();

						CommonLib.ZLog("login", "FACEBOOK");

						checkPlayServices();

						if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
							z_ProgressDialog.dismiss();

					}
				} else {
					Toast.makeText(SplashScreen.this, getResources().getString(R.string.err_occurred), Toast.LENGTH_SHORT).show();
				}
			}

		} else if (requestType == CommonLib.FORGOT_PASSWORD ){
			if(destroyed)
				return;
			if(z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();

			final View customView = inflater.inflate(R.layout.change_password_dialog, null);
			fPasswordDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
					.setCancelable(true)
					.setView(customView)
					.create();

			((TextView)customView.findViewById(R.id.title)).setText(getResources().getString(R.string.verify));
			((TextView)customView.findViewById(R.id.new_password)).setHint(getResources().getString(R.string.enter_pin));

			final String email = String.valueOf(stringId);
			customView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String pin = ((TextView)customView.findViewById(R.id.new_password)).getText().toString().trim();

					if(pin != null && !pin.isEmpty()) {
						if(z_ProgressDialog != null && z_ProgressDialog.isShowing())
							z_ProgressDialog.dismiss();
						z_ProgressDialog = ProgressDialog.show(mContext, null, "Validating pin");
						fPasswordDialog.dismiss();
						UploadManager.updatePassword(email, "", "", pin);
					} else {
						Toast.makeText(mContext, "Invalid pin", Toast.LENGTH_SHORT).show();
					}
				}
			});
			fPasswordDialog.setCanceledOnTouchOutside(true);
			fPasswordDialog.show();
		} else if (requestType == CommonLib.SET_PASSWORD ){
			if(destroyed)
				return;
			if(z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();

			if(data instanceof Boolean) {
				if(fPasswordDialog != null)
					fPasswordDialog.dismiss();
				return;
			}

			final View customView = inflater.inflate(R.layout.change_password_dialog, null);
			fPasswordDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
					.setCancelable(true)
					.setView(customView)
					.create();

			final String email = String.valueOf(data);
			final String pin = String.valueOf(stringId);
			customView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String password = ((TextView)customView.findViewById(R.id.new_password)).getText().toString().trim();

					CryptoHelper helper = new CryptoHelper();
					try {
						password = helper.encrypt(password, null, null);
					} catch (Exception e) {
						e.printStackTrace();
					}

					if(password != null && !password.isEmpty()) {
						if(z_ProgressDialog != null && z_ProgressDialog.isShowing())
							z_ProgressDialog.dismiss();
						z_ProgressDialog = ProgressDialog.show(mContext, null, "Updating password");
						UploadManager.updatePassword(email, "", password, pin);
					} else {
						Toast.makeText(mContext, "Invalid pin", Toast.LENGTH_SHORT).show();
					}
				}
			});
			fPasswordDialog.setCanceledOnTouchOutside(true);
			fPasswordDialog.show();
		}
		else if (requestType == CommonLib.LOGIN || requestType == CommonLib.SIGNUP) {
			CommonLib.ZLog("splashlogin", "inside uploadfinished");
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();
			if(status && !destroyed && data instanceof JSONObject) {
				CommonLib.ZLog("splashlogin", "inside if block");
				JSONObject res = (JSONObject) data;
				Log.d("data",res+"");
				if (res != null) {
					String errorMessage = "";
					int uid = 0;
					String uname = "";
					String accessToken = "";
					String thumbUrl = "";
					String profile_pic = "";
					String email = "";
					boolean verified_user = false;

					try {
						if( res != null && res.has("access_token") && res.has("user_id") && res.get("user_id") instanceof Integer) {
							accessToken = res.getString("access_token");
							uid = res.getInt("user_id");
							if(res.has("user")) {
								res = res.getJSONObject("user");
								if(res.has("user")) {
									res = res.getJSONObject("user");
									if(res.has("profile_pic")) {
										profile_pic = String.valueOf(res.get("profile_pic"));
									}
									if(res.has("email")) {
										email = String.valueOf(res.get("email"));
									}
									if(res.has("username")) {
										uname = String.valueOf(res.get("username"));
									} else if(res.has("user_name")) {
										uname = String.valueOf(res.get("user_name"));
									}
								}
							}
						} else {
							errorMessage = "Something went wrong";
						}
					} catch (Exception e) {
						e.printStackTrace();
						CommonLib.ZLog("splashlogin", "inside exception");
					} finally {
						CommonLib.ZLog("splashlogin", "inside finally");
						Bundle bundle = new Bundle();
						bundle.putInt("uid", uid);
						bundle.putString("profile_pic", profile_pic);
						bundle.putString("email", email);
						bundle.putString("username", uname);
						bundle.putString("thumbUrl", thumbUrl);
						bundle.putString("access_token", accessToken);
						bundle.putBoolean("verifiedUser", verified_user);

						Editor editor = prefs.edit();
						editor.putInt("uid", bundle.getInt("uid"));
						if (bundle.containsKey("email"))
							editor.putString("email", bundle.getString("email"));
						if (bundle.containsKey("username"))
							editor.putString("username", bundle.getString("username"));
						if (bundle.containsKey("thumbUrl"))
							editor.putString("thumbUrl", bundle.getString("thumbUrl"));
						if (bundle.containsKey("profile_pic"))
							editor.putString("profile_pic", bundle.getString("profile_pic"));
						if (bundle.containsKey("description"))
							editor.putString("description", bundle.getString("description"));
						if (bundle.containsKey("user_name"))
							editor.putString("username", bundle.getString("username"));
						editor.putString("access_token", bundle.getString("access_token"));
						if (bundle.containsKey("verifiedUser") && bundle.get("verifiedUser") instanceof Boolean)
							editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
						editor.commit();

						CommonLib.ZLog("login", "LOGIN");

						checkPlayServices();

					}
				} else {
					Toast.makeText(SplashScreen.this, getResources().getString(R.string.err_occurred), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	@Override
	public void uploadStarted(int requestType, int objectId, String stringId, Object object) {
		if (requestType == CommonLib.REQUEST_PRE_SIGNUP) {
			if (zProgressDialog != null && zProgressDialog.isShowing()) {
				zProgressDialog.dismiss();
			}
			zProgressDialog = ProgressDialog.show(mContext, null,
					mContext.getResources().getString(R.string.signingup_wait));
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p/>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {

				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(mContext);
					}

					regId = gcm.register(CommonLib.GCM_SENDER_ID);
					msg = "Device registered, registration ID=" + regId;
					storeRegistrationId(mContext, regId);

					if (prefs.getInt("uid", 0) != 0 && !regId.equals(""))
						sendRegistrationIdToBackend();

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				CommonLib.ZLog("GCM msg", msg);
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	private void storeRegistrationId(Context context, String regId) {

		prefs = getSharedPreferences("application_settings", 0);
		int appVersion = getAppVersion(context);
		Editor editor = prefs.edit();
		editor.putString(CommonLib.PROPERTY_REG_ID, regId);
		editor.putInt(CommonLib.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),1);
			return packageInfo.versionCode;

		} catch (Exception e) {
			CommonLib.ZLog("GCM", "EXCEPTION OCCURED" + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	private void checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

		if (resultCode != ConnectionResult.SUCCESS) {

			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				CommonLib.ZLog("google-play-resultcode", resultCode);
				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();
				if (resultCode == 2 && !isFinishing()) {

					if (windowHasFocus)
						showDialog(PLAY_SERVICES_RESOLUTION_REQUEST);
				} else {
					navigateToHome();
				}

			} else {
				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();
				navigateToHome();
			}

		} else {

			gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
			regId = getRegistrationId(mContext);

			if (hardwareRegistered == 0) {
				// Call
				if (prefs.getInt("uid", 0) != 0 && !regId.equals("")) {
					sendRegistrationIdToBackend();
					Editor editor = prefs.edit();
					editor.putInt("HARDWARE_REGISTERED", 1);
					editor.commit();
				}
			}

			if (regId.isEmpty()) {
				CommonLib.ZLog("GCM", "RegID is empty");
				registerInBackground();
			} else {
				CommonLib.ZLog("GCM", "already registered : " + regId);
			}
			if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
				z_ProgressDialog.dismiss();
			navigateToHome();
		}
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p/>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {

		final SharedPreferences prefs = getSharedPreferences("application_settings", 0);
		String registrationId = prefs.getString(CommonLib.PROPERTY_REG_ID, "");

		if (registrationId.isEmpty()) {
			CommonLib.ZLog("GCM", "Registration not found.");
			return "";
		}
		return registrationId;
	}

	@Override
	public void response(Bundle bundle) {

		error_exception = "";
		error_responseCode = "";
		error_stackTrace = "";
		boolean regIdSent = false;

		if (bundle.containsKey("error_responseCode"))
			error_responseCode = bundle.getString("error_responseCode");

		if (bundle.containsKey("error_exception"))
			error_exception = bundle.getString("error_exception");

		if (bundle.containsKey("error_stackTrace"))
			error_stackTrace = bundle.getString("error_stackTrace");

		try {

			int status = bundle.getInt("status");

			if (status == 0) {

				if (!error_exception.equals("") || !error_responseCode.equals("") || !error_stackTrace.equals(""))
					;

				if (bundle.getString("errorMessage") != null) {
					String errorMessage = bundle.getString("errorMessage");
					Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext, R.string.err_occurred, Toast.LENGTH_SHORT).show();
				}
				if (z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();
			} else {
				Editor editor = prefs.edit();
				editor.putInt("uid", bundle.getInt("uid"));
				if (bundle.containsKey("email"))
					editor.putString("email", bundle.getString("email"));
				if (bundle.containsKey("username"))
					editor.putString("username", bundle.getString("username"));
				if (bundle.containsKey("thumbUrl"))
					editor.putString("thumbUrl", bundle.getString("thumbUrl"));
				if (bundle.containsKey("profile_pic"))
					editor.putString("profile_pic", bundle.getString("profile_pic"));
				if (bundle.containsKey("description"))
					editor.putString("description", bundle.getString("description"));
				if (bundle.containsKey("user_name"))
					editor.putString("username", bundle.getString("username"));
				String token = bundle.getString("access_token");
				System.out.println(token);
				editor.putString("access_token", bundle.getString("access_token"));
				editor.putBoolean("verifiedUser", bundle.getBoolean("verifiedUser"));
				editor.commit();

				CommonLib.ZLog("login", "FACEBOOK");

				checkPlayServices();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendRegistrationIdToBackend() {
		UploadManager.updateRegistrationId(regId);
	}

	private void navigateToHome() {
		if (prefs.getInt("uid", 0) != 0) {
			if(insideApp) {
				finish();
			} else {
				Intent intent = new Intent(this, Home.class);
				startActivity(intent);
				finish();
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (isActivityRunning && z_ProgressDialog != null) {
			z_ProgressDialog.dismiss();
		}
		if ( isActivityRunning && z_ProgressDialog != null && !z_ProgressDialog.isShowing())
			z_ProgressDialog = ProgressDialog.show(this, null, "Fetching Details.Please wait..");
		Log.d(TAG, "onConnected:" + bundle);
		mShouldResolve = false;

		getProfileInformation();
	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Could not connect to Google Play Services. The user needs to select
		// an account,
		// grant permissions or resolve an error in order to sign in. Refer to
		// the javadoc for
		// ConnectionResult to see possible error codes.
		Log.d(TAG, "onConnectionFailed:" + connectionResult);

		if (!mIsResolving && mShouldResolve) {
			if (connectionResult.hasResolution()) {

				try {

					connectionResult.startResolutionForResult(this, RC_SIGN_IN);
					mIsResolving = true;
				} catch (IntentSender.SendIntentException e) {

					Log.e(TAG, "Could not resolve ConnectionResult.", e);
					mIsResolving = false;
					mGoogleApiClient.connect();
				}
			} else {
				// Could not resolve the connection result, show the user an
				// error dialog.
				if (z_ProgressDialog != null)
					z_ProgressDialog.dismiss();
				//Toast.makeText(this, connectionResult.getResolution().describeContents(), Toast.LENGTH_SHORT).show();
			}
		} else {
			if (z_ProgressDialog != null)
				z_ProgressDialog.dismiss();
			// ----------------------Show the signed-out UI

		}
	}

	@Override
	public void onStart() {
		super.onStart();
		connectGoogleApiClient();
		// Check if the intent contains an AppInvite and then process the referral information.
		Intent intent = getIntent();
		if (AppInviteReferral.hasReferral(intent)) {

			CommonLib.ZLog("referal", "entered here");
			//Toast.makeText(getBaseContext(),"you are referred by someone",Toast.LENGTH_LONG).show();
			processReferralIntent(intent);
		}
	}

	String invitationId, deepLink;

	private void processReferralIntent(Intent intent) {
		// Extract referral information from the intent
		invitationId = AppInviteReferral.getInvitationId(intent);
		deepLink = AppInviteReferral.getDeepLink(intent);

		// Display referral information
		// [START_EXCLUDE]
		CommonLib.ZLog(TAG, "Found Referral: " + invitationId + ":" + deepLink);
	}


	@Override
	public void onStop() {
		disconnectApiClient();
		super.onStop();
	}

	private class TourPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {

			RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.tour_element, null);

			if (position == 0) {

				ImageView tour_logo = (ImageView) layout.findViewById(R.id.tour_logo);
				TextView tour_text= (TextView) layout.findViewById(R.id.tour_text);
				tour_text.setVisibility(View.GONE);
				tour_logo.getLayoutParams().width = 217* width  / 800; //if w = 217,h = 200, w = 217x/800
				tour_logo.getLayoutParams().height = width / 4;

				//setting image
				Bitmap bitmap = CommonLib.getBitmap(mContext, R.drawable.logo, width, height);
				tour_logo.setImageBitmap(bitmap);
				tour_logo.setVisibility(View.VISIBLE);

				TextView tour_logo_text = (TextView) layout.findViewById(R.id.tour_logo_text);
				tour_logo_text.setVisibility(View.GONE);

			} else if (position == 1) {

				ImageView tour_logo = (ImageView) layout.findViewById(R.id.tour_logo);
				tour_logo.setVisibility(View.GONE);

				TextView tour_logo_text = (TextView) layout.findViewById(R.id.tour_logo_text);
				tour_logo_text.setText(mContext.getResources().getString(R.string.z_tour_1));
				tour_logo_text.setVisibility(View.VISIBLE);

				TextView tour_text= (TextView) layout.findViewById(R.id.tour_text);
				tour_text.setVisibility(View.VISIBLE);
				tour_text.setText(mContext.getResources().getString(R.string.tour_01));

			} else if (position == 2) {

				ImageView tour_logo = (ImageView) layout.findViewById(R.id.tour_logo);
				tour_logo.setVisibility(View.GONE);

				TextView tour_logo_text = (TextView) layout.findViewById(R.id.tour_logo_text);
				tour_logo_text.setText(mContext.getResources().getString(R.string.z_tour_2));
				tour_logo_text.setVisibility(View.VISIBLE);

				TextView tour_text= (TextView) layout.findViewById(R.id.tour_text);
				tour_text.setVisibility(View.VISIBLE);
				tour_text.setText(mContext.getResources().getString(R.string.tour_02));
			} else if (position == 3) {

				ImageView tour_logo = (ImageView)layout.findViewById(R.id.tour_logo);
				tour_logo.setVisibility(View.GONE);

				TextView tour_logo_text = (TextView) layout.findViewById(R.id.tour_logo_text);
				tour_logo_text.setText(mContext.getResources().getString(R.string.z_tour_3));
				tour_logo_text.setVisibility(View.VISIBLE);

				TextView tour_text= (TextView) layout.findViewById(R.id.tour_text);
				tour_text.setVisibility(View.VISIBLE);
				tour_text.setText(mContext.getResources().getString(R.string.tour_03));
			} else if (position == 4) {

				ImageView tour_logo = (ImageView) layout.findViewById(R.id.tour_logo);
				tour_logo.getLayoutParams().width = 217* width  / 800; //if w = 217,h = 200, w = 217x/800
				tour_logo.getLayoutParams().height = width / 4;

				Bitmap bitmap = CommonLib.getBitmap(mContext, R.drawable.logo, width, height);
				tour_logo.setImageBitmap(bitmap);
				tour_logo.setVisibility(View.VISIBLE);

				TextView tour_logo_text = (TextView) layout.findViewById(R.id.tour_logo_text);
				tour_logo_text.setVisibility(View.GONE);

				TextView tour_text= (TextView) layout.findViewById(R.id.tour_text);
				tour_text.setVisibility(View.GONE);
			}
			collection.addView(layout, 0);
			return layout;
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		@Override
		public void finishUpdate(ViewGroup arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(ViewGroup arg0) {
		}

	}

	private void updateDotsContainer() {

		LinearLayout dotsContainer = (LinearLayout) findViewById(R.id.tour_dots);
		dotsContainer.removeAllViews();

		int index = 5;

		for (int count = 0; count < index; count++) {
			ImageView dots = new ImageView(getApplicationContext());

			if (count == 0) {
				dots.setImageResource(R.drawable.tour_image_dots_selected);
				dots.setPadding(width / 40, 0, width / 40, 0);

			} else {
				dots.setImageResource(R.drawable.tour_image_dots_unselected);
				dots.setPadding(0, 0, width / 40, 0);
			}

			final int c = count;
			dots.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						((ViewPager) mViewPager).setCurrentItem(c);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			dotsContainer.addView(dots);
		}
	}

	@Override
	public void onCoordinatesIdentified(Location loc) {
		if(loc != null && prefs != null) {
			float lat = (float) loc.getLatitude();
			float lon = (float)loc.getLongitude();
			Editor editor = prefs.edit();
			editor.putString("lat1", lat+"");
			editor.putString("lon1", lon+"");
			editor.commit();

			UploadManager.updateLocation(lat, lon);

			Geocoder geocoder;
			List<Address> addresses;
			geocoder = new Geocoder(this, Locale.getDefault());

			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
				if(addresses != null && addresses.size() > 0 && addresses.get(0) != null) {
					String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
					String city = addresses.get(0).getLocality();
					String state = addresses.get(0).getAdminArea();
					address = address + ", " + city + ", " + state;
					zapp.setLocationString(city);
					zapp.setAddressString(address);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onLocationIdentified() {
	}

	@Override
	public void onLocationNotIdentified() {
	}

	@Override
	public void onDifferentCityIdentified() {
	}

	@Override
	public void locationNotEnabled() {
	}

	@Override
	public void onLocationTimedOut() {
	}

	@Override
	public void onNetworkError() {
	}

	public void googleLogin() {
		// User clicked the sign-in button, so begin the sign-in process and
		// automatically
		// attempt to resolve any errors that occur.
		mShouldResolve = true;
		mGoogleApiClient.connect();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id != DIALOG_GET_GOOGLE_PLAY_SERVICES) {
			return super.onCreateDialog(id);
		}

		int available = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (available == ConnectionResult.SUCCESS) {
			return null;
		}

		if (GooglePlayServicesUtil.isUserRecoverableError(available)) {
			AlertDialog.Builder builder_google_play_services = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
			builder_google_play_services.setMessage(getResources().getString(R.string.update_google_play_services)).setCancelable(false)
					.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.gms"));
								startActivityForResult(browserIntent, PLAY_SERVICES_RESOLUTION_REQUEST);
							} catch (ActivityNotFoundException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});

			return builder_google_play_services.create();
			// return GooglePlayServicesUtil.getErrorDialog(available, this,
			// REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
		}

		return new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK).setMessage(R.string.err_occurred).setCancelable(true).create();
	}

	private Runnable googleLoginSuccessful = new Runnable() {
		public void run() {

			if (z_ProgressDialog != null)
				z_ProgressDialog.dismiss();

			Editor editor = prefs.edit();
//			editor.putInt("uid", uid);
//			editor.putString("email", email_global);
//			editor.putString("username", uname);
//			editor.putInt("numReviews", numReviews);
//			editor.putInt("numBlogs", numBlogs);
//			editor.putInt("numFollowers", numFollowers);
//			editor.putString("thumbUrl", thumbUrl);
//			editor.putString("foodieLevel", foodieLevel);
//			editor.putString("foodieColor", foodieColor);
//			editor.putString("access_token", access_token);
//			editor.putBoolean("verifiedUser", verified_user);
//			editor.putInt("googleLogin", 1);
			editor.commit();

			//GCM
			String regId = getRegistrationId(getApplicationContext());
			if(regId.length() > 0 && prefs.getInt("uid", 0)>0)
				sendRegistrationIdToBackend();


			if (REQUEST_CODE == START_LOGIN_INTENT || REQUEST_CODE == START_ACTIVITY_LOGIN_INTENT) {
				// Intent responseIntent = new Intent();
				// setResult(CommonLib.RESULT_GOOGLE_LOGIN_OK, responseIntent);
				navigateToHome();
				// finish();

			} else if (REQUEST_CODE == ZAPPLON_LOGIN_CONFIRMATION_REQUIRED) {
				navigateToHome();
				// setResult(RESULT_OK);
				// finish();

			} else {
				navigateToHome();
				// setResult(RESULT_OK);
				// finish();
			}
			// //overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
		}
	};

	private int REQUEST_CODE = 0;
	private final int START_LOGIN_INTENT = 100;
	private final int START_ACTIVITY_LOGIN_INTENT = 200;
	private final int ZAPPLON_LOGIN_CONFIRMATION_REQUIRED = 999;
	String errorMessage;

	/**
	 * Called if google login fails.
	 */
	private Runnable googleLoginFailure = new Runnable() {

		public void run() {
			z_ProgressDialog.dismiss();
			if (errorMessage.equals(""))
				errorMessage = getResources().getString(R.string.could_not_connect);
			Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT).show();
		}
	};

	public void connectGoogleApiClient() {
		mGoogleApiClient.connect();
	}

	public void disconnectApiClient() {
		if (mGoogleApiClient.isConnected()) {
			mGoogleApiClient.disconnect();
		}
	}

	public void registerGoogleApiClient() {
		mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
				.addScope(Plus.SCOPE_PLUS_PROFILE).build();
	}

	protected void removeDefaultGoogleLogin() {
		if (mGoogleApiClient.isConnected()) {
			Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			mGoogleApiClient.connect();

		}
	}
	String personName, personPhotoUrl, email, personId;
	int PROFILE_PIC_SIZE = 400;

	private void getProfileInformation() {
		try {
			if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {

				Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

				personName = currentPerson.getDisplayName();
				personPhotoUrl = currentPerson.getImage().getUrl();
				personId = currentPerson.getId();

				email = Plus.AccountApi.getAccountName(mGoogleApiClient);

				personPhotoUrl = personPhotoUrl.substring(0, personPhotoUrl.length() - 2) + PROFILE_PIC_SIZE;
                /*
                 * String accessToken =
				 * GoogleAuthUtil.getToken(getApplicationContext(), Plus),
				 * "oauth2:" + Scopes.PLUS_LOGIN + " " + Scopes.PROFILE +
				 * " https://www.googleapis.com/auth/plus.profile.emails.read");
				 */
				// task.execute();
				String token = "1234343432";
				onGoogleDataRetreived(personName, email, personPhotoUrl, token);
				//

			} else {
				if (z_ProgressDialog != null)
					z_ProgressDialog.dismiss();
				Toast.makeText(mContext, "An error occured. Try again.", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String invited_by;

	public void onGoogleDataRetreived(String name, String email, String photoUrl, String token) {
		if(prefs == null)
			prefs = getSharedPreferences("application_settings", 0);
		invited_by = prefs.getString("invited_by",null);
		UploadManager.login(name, email, photoUrl, token, CommonLib.getIMEI(SplashScreen.this), invitationId, invited_by);
		prefs.edit().remove("invited_by").commit();
	}


	private void startAppConfigService(){
		Intent intent = new Intent(SplashScreen.this,AppConfigService.class);
		startService(intent);
	}

	int seconds = 12;
	Timer timer;
	private int mCurrentItem = 0;

	private void startTimer() {
		if (mContext == null || destroyed)
			return;

		timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (!destroyed) {

					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (!destroyed) {
								seconds -= 1;
								if (seconds <= 0) {
									seconds = 12;
									timer.cancel();
								} else {
									mCurrentItem++;
									if (mCurrentItem < 5 && !hasSwipedPager)
										mViewPager.setCurrentItem(mCurrentItem);
								}

							}
						}
					});
				} else {
					timer.cancel();
				}
			}
		}, 3000, 3000);
	}

	// login fundae ;)
	private final int SIGN_IN = 201;
	private final int SIGN_UP = 202;

	final int DEFAULT_SHOWN = 87;
	final int LOGIN_SHOWN = 88;
	final int SIGNUP_SHOWN = 89;
	int mState = DEFAULT_SHOWN;
	RelativeLayout loginPage;
	RelativeLayout signUpPage;

	private int nameLength = 0;
	private int emailLength = 0;
	private int pswdLength = 0;
	private int lastVisited = 0;

	private boolean mLoginContainerAnimating = false;

	public void goBack(View v) {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {

		if (mState == SIGNUP_SHOWN) {

			mState = DEFAULT_SHOWN;

			Animation animation = new TranslateAnimation(0, 0, 0, height);
			animation.setDuration(CommonLib.ANIMATION_LOGIN);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					signUpPage.setVisibility(View.GONE);

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);
				}
			});
			signUpPage.startAnimation(animation);

		} else if (mState == LOGIN_SHOWN) {

			mState = DEFAULT_SHOWN;

			Animation animation = new TranslateAnimation(0, 0, 0, height);
			animation.setDuration(CommonLib.ANIMATION_LOGIN);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					loginPage.setVisibility(View.GONE);

					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);
				}
			});
			loginPage.startAnimation(animation);

		} else {

			int position = (int) findViewById(R.id.main_root).getY();

			if (position == 0) {
				// Tour.BACK_PRESSED = 200;
				// setResult(Activity.RESULT_CANCELED);
				super.onBackPressed();
				// //overridePendingTransition(R.anim.fade_in_fast,R.anim.fade_out_fast);

			} else if (!mLoginContainerAnimating) {

				final View root = findViewById(R.id.main_root);
				View focusedView = root.findFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				if (focusedView != null) {
					imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
					focusedView.clearFocus();

				} else {
					imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);
				}

				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

				root.animate().translationY(0).setDuration(CommonLib.ANIMATION_DURATION_SIGN_IN).setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationStart(Animator animation) {
						mLoginContainerAnimating = true;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						mLoginContainerAnimating = false;
					}

					@Override
					public void onAnimationEnd(Animator animation) {
						if (root.getY() == 0) {
							// findViewById(R.id.login_container).setVisibility(View.GONE);
							// findViewById(R.id.forgot_pass_text).setVisibility(View.GONE);
							// findViewById(R.id.blank_view).setVisibility(View.GONE);
						}
						mLoginContainerAnimating = false;
					}
				});
			}

		}
	}

	public void animateScreens(View view) {
		animaterToScreen2(view, false);
	}

	private void animaterToScreen2(View view, Boolean fromSignUp) {

		int id = view.getId();

		if (id == R.id.layout_signup_text) {

			mState = SIGNUP_SHOWN;

			// animation up
			signUpPage.setVisibility(View.VISIBLE);
			Animation animation = new TranslateAnimation(0, 0, height, 0);
			animation.setDuration(CommonLib.ANIMATION_LOGIN);
			signUpPage.startAnimation(animation);

			// page setup
			signupSetup(signUpPage);

			signUpPage.findViewById(R.id.about_us_terms_conditions_container).setPadding(width/20, width/40, width/20, width/40);

			String signupTerms = getResources().getString(R.string.signup_terms);
			String termsOfService = getResources().getString(R.string.about_us_terms_of_use);
			String privacyPolicy = getResources().getString(R.string.about_us_privacypolicy);

			SpannableStringBuilder finalSpanBuilderStr = new SpannableStringBuilder(signupTerms);

			ClickableSpan cs1 = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(SplashScreen.this, ZWebView.class);
					intent.putExtra("title", getResources().getString(R.string.about_us_privacypolicy));
					intent.putExtra("url", "http://www.zapplon.com/privacy/");
					startActivity(intent);
				}
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(true);
					ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
					ds.setColor(getResources().getColor(R.color.white_trans_fifty));
				}
			};

			ClickableSpan cs3 = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(SplashScreen.this, ZWebView.class);
					intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
					intent.putExtra("url", "http://www.zapplon.com/terms/");
					startActivity(intent);
				}
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(true);
					ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
					ds.setColor(getResources().getColor(R.color.white_trans_fifty));
				}
			};

			try{
				if(signupTerms.indexOf(privacyPolicy) == -1){
					signUpPage.findViewById(R.id.about_us_terms_conditions).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Intent intent = new Intent(SplashScreen.this, ZWebView.class);
							intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
							intent.putExtra("url", "http://www.zapplon.com/terms/");
							startActivity(intent);
						}
					});
				}

				finalSpanBuilderStr.setSpan(cs1,
						signupTerms.indexOf(privacyPolicy),
						signupTerms.indexOf(privacyPolicy) + privacyPolicy.length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

				finalSpanBuilderStr.setSpan(cs3,
						signupTerms.indexOf(termsOfService),
						signupTerms.indexOf(termsOfService) + termsOfService.length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

			} catch(Exception e){
				e.printStackTrace();
			}
			((TextView)signUpPage.findViewById(R.id.about_us_terms_conditions))
					.setText(finalSpanBuilderStr, TextView.BufferType.SPANNABLE);
			((TextView)signUpPage.findViewById(R.id.about_us_terms_conditions)).setMovementMethod(LinkMovementMethod.getInstance());

			// header
			//signUpPage.findViewById(R.id.page_header_text).setPadding(width / 20, 0, 0, 0);

			signUpPage.findViewById(R.id.page_header_close).setPadding(width / 20, 0, width / 20, 0);
			((TextView) signUpPage.findViewById(R.id.page_header_text)).setText(getResources().getString(R.string.email_signup));
			((TextView)signUpPage.findViewById(R.id.help_string)).setText(getResources().getString(R.string.signup_using_email));

			// form
			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_details).getLayoutParams()).setMargins(width / 20, width / 40, width / 20, width / 40);

			final EditText usrnm = ((EditText) signUpPage.findViewById(R.id.login_username));
			final EditText email = ((EditText) signUpPage.findViewById(R.id.login_email));
			final EditText pswrd = ((EditText) signUpPage.findViewById(R.id.login_password));

			usrnm.setPadding(width / 20, 0, width / 20, 0);
			email.setPadding(width / 20, 0, width / 20, 0);
			pswrd.setPadding(width / 20, 0, width / 20, 0);

			usrnm.getLayoutParams().height = width / 7;
			email.getLayoutParams().height = width / 7;
			pswrd.getLayoutParams().height = width / 7;

			//Pre-fill of sign-up credentials
			try{
				AccountManager am = AccountManager.get(getApplicationContext());
				if(am!=null){
					Account[] accounts= am.getAccounts();
					Pattern emailPattern = Patterns.EMAIL_ADDRESS;
					for(Account account: accounts) {
						if(account.type.equals("com.google")&&emailPattern.matcher(account.name).matches()) {
							email.setText(account.name);
						}
					}
				}
			} catch(Exception e){
				Crashlytics.logException(e);
			}

			signUpPage.findViewById(R.id.submit_button).getLayoutParams().height = width / 7;
			signUpPage.findViewById(R.id.forgot_pass_text).getLayoutParams().height = width / 10;
			signUpPage.findViewById(R.id.forgot_pass_text).setPadding(0, width / 40, 0, 0);

			// sign up button
			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_submit).getLayoutParams()).setMargins(width / 20, 0, width / 20, 0);
			((TextView) signUpPage.findViewById(R.id.submit_button)).setText(getResources().getString(R.string.email_signup));

			// separator view
//			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_page_separator).getLayoutParams()).setMargins(width / 20, width / 20, width / 20, width / 20);

			int buttonHeight = (11 * 9 * width) / (80 * 10);

			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_email_details).getLayoutParams()).setMargins(width / 20, width / 20, width / 20, 0);
			((RelativeLayout.LayoutParams) signUpPage.findViewById(R.id.email_login_separator1).getLayoutParams()).setMargins(width / 40, 0, 0, 0);
			signUpPage.findViewById(R.id.email_login_separator1).getLayoutParams().width = (width - 2*width/40 - signUpPage.findViewById(R.id.help_string).getLayoutParams().width)/2;
			((RelativeLayout.LayoutParams) signUpPage.findViewById(R.id.email_login_separator2).getLayoutParams()).setMargins(0, 0, width/40, 0);
			signUpPage.findViewById(R.id.email_login_separator2).getLayoutParams().width = (width - 2*width/40 - signUpPage.findViewById(R.id.help_string).getLayoutParams().width)/2;

			signUpPage.findViewById(R.id.help_string).setPadding(width/40, 0, width/40, 0);

			// fb button
			View fb_cont = signUpPage.findViewById(R.id.login_page_layout_connect_using_facebook);
			fb_cont.getLayoutParams().height = buttonHeight;
			((RelativeLayout.LayoutParams) fb_cont.getLayoutParams()).setMargins(width / 20, width/20, width / 20, width / 40);
			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams()).setMargins(0, 0, width / 20, 0);
			signUpPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().width = buttonHeight;
			signUpPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().height = buttonHeight;
			((TextView) signUpPage.findViewById(R.id.login_page_layout_connect_using_facebook_text)).setText(getResources().getString(R.string.signup_facebook));

			// google button
			View google_cont = signUpPage.findViewById(R.id.login_page_layout_connect_using_google);
			google_cont.getLayoutParams().height = buttonHeight;
			((RelativeLayout.LayoutParams) google_cont.getLayoutParams()).setMargins(width / 20, 0, width / 20, width / 40);
			((LinearLayout.LayoutParams) signUpPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams()).setMargins(0, 0, width / 20, 0);
			signUpPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams().width = buttonHeight;
			signUpPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams().height = buttonHeight;
			((TextView) signUpPage.findViewById(R.id.login_page_layout_connect_using_google_text)).setText(getResources().getString(R.string.signup_google));

			// already have an account
			signUpPage.findViewById(R.id.login_page_already_have_an_account).setVisibility(View.VISIBLE);
			signUpPage.findViewById(R.id.login_page_already_have_an_account).setPadding(0, width / 20, 0, 0);
			setAlreadyHaveAnAccountText();

			// empty space at bottom
			signUpPage.findViewById(R.id.login_blank_view).setVisibility(View.VISIBLE);
			signUpPage.findViewById(R.id.login_blank_view).getLayoutParams().height = width / 20;

			usrnm.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					nameLength = s.toString().trim().length();

					int filled = 0;
					filled = nameLength * pswdLength * emailLength;
					if (filled > 0) {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			email.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					emailLength = s.toString().trim().length();

					int filled = emailLength * pswdLength;

					if (lastVisited == SIGN_UP) {
						filled *= nameLength;
					}

					if (filled > 0) {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			pswrd.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					pswdLength = s.toString().trim().length();
					int filled = emailLength * pswdLength;

					if (lastVisited == SIGN_UP) {
						filled *= nameLength;
					}

					if (filled > 0) {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						signUpPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

//			((EditText) findViewById(R.id.forgot_password_email)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//				@Override
//				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//					if (actionId == EditorInfo.IME_ACTION_DONE) {
//						resetPassword(v);
//						return true;
//					}
//					return false;
//				}
//			});

			signUpPage.findViewById(R.id.login_submit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clickSubmitSignup(v);
				}
			});

			pswrd.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {

					if (keyCode == EditorInfo.IME_ACTION_DONE) {

						View focusedView = v;
						InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
						if (focusedView != null) {
							imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
							focusedView.clearFocus();

						} else
							imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);

						clickSubmitSignup(findViewById(R.id.login_details));

						return true;
					}
					return false;
				}
			});

		} else if (id == R.id.layout_login_text) {

			mState = LOGIN_SHOWN;

			// animations
			loginPage.setVisibility(View.VISIBLE);
			if (fromSignUp) {

				// animation in
				Animation animation = new TranslateAnimation(width, 0, 0, 0);
				animation.setDuration(CommonLib.ANIMATION_LOGIN);
				loginPage.startAnimation(animation);

				// animation out
				Animation animation2 = new TranslateAnimation(0, -width, 0, 0);
				animation2.setDuration(CommonLib.ANIMATION_LOGIN);
				signUpPage.startAnimation(animation2);

				signUpPage.setVisibility(View.GONE);

			} else {
				Animation animation = new TranslateAnimation(0, 0, height, 0);
				animation.setDuration(CommonLib.ANIMATION_LOGIN);
				loginPage.startAnimation(animation);
			}

			// header
			//loginPage.findViewById(R.id.page_header_text).setPadding(width / 20, 0, 0, 0);
			loginPage.findViewById(R.id.page_header_close).setPadding(width / 20, 0, width / 20, 0);
			((TextView) loginPage.findViewById(R.id.page_header_text)).setText(getResources().getString(R.string.Login));

			loginPage.findViewById(R.id.about_us_terms_conditions_container).setPadding(width/20, width/40, width/20, width/40);

			String loginTerms = getResources().getString(R.string.login_terms);
			String termsOfService = getResources().getString(R.string.about_us_terms_of_use);
			String privacyPolicy = getResources().getString(R.string.about_us_privacypolicy);

			SpannableStringBuilder finalSpanBuilderStr = new SpannableStringBuilder(loginTerms);

			ClickableSpan cs1 = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(SplashScreen.this, ZWebView.class);
					intent.putExtra("title", getResources().getString(R.string.about_us_privacypolicy));
					intent.putExtra("url", "http://www.zapplon.com/privacy/");
					startActivity(intent);
				}
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(true);
					ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
					ds.setColor(getResources().getColor(R.color.white_trans_fifty));
				}
			};

			ClickableSpan cs3 = new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					Intent intent = new Intent(SplashScreen
							.this, ZWebView.class);
					intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
					intent.putExtra("url", "http://www.zapplon.com/terms/");
					startActivity(intent);
				}
				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(true);
					ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
					ds.setColor(getResources().getColor(R.color.white_trans_fifty));
				}
			};

			try{
				if(loginTerms.indexOf(privacyPolicy) == -1){
					loginPage.findViewById(R.id.about_us_terms_conditions).setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							Intent intent = new Intent(SplashScreen.this, ZWebView.class);
							intent.putExtra("title", getResources().getString(R.string.about_us_terms_of_use));
							intent.putExtra("url", "http://www.zapplon.com/terms/");
							startActivity(intent);
						}
					});
				}
				finalSpanBuilderStr.setSpan(cs1,
						loginTerms.indexOf(privacyPolicy),
						loginTerms.indexOf(privacyPolicy) + privacyPolicy.length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

				finalSpanBuilderStr.setSpan(cs3,
						loginTerms.indexOf(termsOfService),
						loginTerms.indexOf(termsOfService) + termsOfService.length(),
						SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
			}
			catch(Exception e){
				e.printStackTrace();

			}
			((TextView)loginPage.findViewById(R.id.about_us_terms_conditions))
					.setText(finalSpanBuilderStr, TextView.BufferType.SPANNABLE);

			((TextView)loginPage.findViewById(R.id.about_us_terms_conditions)).setMovementMethod(LinkMovementMethod.getInstance());

			// form
			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_details).getLayoutParams()).setMargins(width / 20, width / 40, width / 20, width / 40);

			final EditText usrnm = ((EditText) loginPage.findViewById(R.id.login_username));
			final EditText email = ((EditText) loginPage.findViewById(R.id.login_email));
			final EditText pswrd = ((EditText) loginPage.findViewById(R.id.login_password));

			usrnm.getLayoutParams().height = width / 7;
			email.getLayoutParams().height = width / 7;
			pswrd.getLayoutParams().height = width / 7;

			usrnm.setPadding(width / 20, 0, width / 20, 0);
			email.setPadding(width / 20, 0, width / 20, 0);
			pswrd.setPadding(width / 20, 0, width / 20, 0);

			loginPage.findViewById(R.id.submit_button).getLayoutParams().height = width / 7;
			loginPage.findViewById(R.id.forgot_pass_text).getLayoutParams().height = width / 10;
			loginPage.findViewById(R.id.forgot_pass_text).setPadding(0, width / 40, 0, 0);

			// log in button
			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_submit).getLayoutParams()).setMargins(width / 20, 0, width / 20, 0);
			((TextView) loginPage.findViewById(R.id.submit_button)).setText(getResources().getString(R.string.Login));

			// separator view
//			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_page_separator).getLayoutParams()).setMargins(width / 20, width / 20, width / 20, width / 20);

			int buttonHeight = (11 * 9 * width) / (80 * 10);
			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_email_details).getLayoutParams()).setMargins(width / 20, width / 20, width / 20, 0);
			loginPage.findViewById(R.id.email_login_separator1).getLayoutParams().width = (width - 2*width/40 - loginPage.findViewById(R.id.help_string).getLayoutParams().width)/2;
			((RelativeLayout.LayoutParams) loginPage.findViewById(R.id.email_login_separator2).getLayoutParams()).setMargins(0, 0, width/40, 0);
			loginPage.findViewById(R.id.email_login_separator2).getLayoutParams().width = (width - 2*width/40 - loginPage.findViewById(R.id.help_string).getLayoutParams().width)/2;

			loginPage.findViewById(R.id.help_string).setPadding(width/40, 0, width/40, 0);

			// fb button
			View fb_cont = loginPage.findViewById(R.id.login_page_layout_connect_using_facebook);
			fb_cont.getLayoutParams().height = buttonHeight;
			((RelativeLayout.LayoutParams) fb_cont.getLayoutParams()).setMargins(width / 20, width/20, width / 20, width / 40);
			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams()).setMargins(0, 0, width / 20, 0);
			loginPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().width = buttonHeight;
			loginPage.findViewById(R.id.login_page_facebook_icon_container).getLayoutParams().height = buttonHeight;
			((TextView) loginPage.findViewById(R.id.login_page_layout_connect_using_facebook_text)).setText(getResources().getString(R.string.login_via_facebook));

			// google button
			View google_cont = loginPage.findViewById(R.id.login_page_layout_connect_using_google);
			google_cont.getLayoutParams().height = buttonHeight;
			((RelativeLayout.LayoutParams) google_cont.getLayoutParams()).setMargins(width / 20, 0, width / 20, width/40);
			((LinearLayout.LayoutParams) loginPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams()).setMargins(0, 0, width / 20, 0);
			loginPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams().width = buttonHeight;
			loginPage.findViewById(R.id.login_page_google_icon_container).getLayoutParams().height = buttonHeight;
			((TextView) loginPage.findViewById(R.id.login_page_layout_connect_using_google_text)).setText(getResources().getString(R.string.login_google));

			// empty space at bottom
			loginPage.findViewById(R.id.login_blank_view).setVisibility(View.VISIBLE);
			loginPage.findViewById(R.id.login_blank_view).getLayoutParams().height = width / 20;

			loginPage.findViewById(R.id.login_page_already_have_an_account).setVisibility(View.GONE);

			usrnm.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					nameLength = s.toString().trim().length();

					int filled = 0;
					filled = nameLength * pswdLength * emailLength;
					if (filled > 0) {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			email.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					emailLength = s.toString().trim().length();

					int filled = emailLength * pswdLength;

					if (lastVisited == SIGN_UP) {
						filled *= nameLength;
					}

					if (filled > 0) {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

			pswrd.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					pswdLength = s.toString().trim().length();
					int filled = emailLength * pswdLength;

					if (lastVisited == SIGN_UP) {
						filled *= nameLength;
					}

					if (filled > 0) {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
					} else {
						loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			});

//			((EditText) findViewById(R.id.forgot_password_email)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
//
//				@Override
//				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//					if (actionId == EditorInfo.IME_ACTION_DONE) {
//						resetPassword(v);
//						return true;
//					}
//					return false;
//				}
//			});

			loginSetup(loginPage);

			loginPage.findViewById(R.id.login_submit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					clickSubmitLogin(v);
				}
			});

			pswrd.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int keyCode, KeyEvent event) {

					if (keyCode == EditorInfo.IME_ACTION_DONE) {

						View focusedView = v;
						InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
						if (focusedView != null) {
							imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
							focusedView.clearFocus();

						} else
							imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);

						clickSubmitLogin(findViewById(R.id.login_details));

						return true;
					}
					return false;
				}
			});
		}
	}

	public void setDailougeMessage(AlertDialog alertDialog, String msg){
		alertDialog.setMessage(msg);
	}

	private void signupSetup(View root) {

		// lastVisited = SIGN_UP;

		// final View pageRoot = findViewById(R.id.main_root);
		// pageRoot.findViewById(R.id.blank_space).setVisibility(View.VISIBLE);

		// root.findViewById(R.id.login_container).setVisibility(View.VISIBLE);
		root.findViewById(R.id.view1).setVisibility(View.VISIBLE);
		root.findViewById(R.id.forgot_pass_text).setVisibility(View.GONE);
		root.findViewById(R.id.login_email).setVisibility(View.VISIBLE);
		root.findViewById(R.id.login_password).setVisibility(View.VISIBLE);
		// root.findViewById(R.id.blank_view).setVisibility(View.VISIBLE);
		TextView name = (TextView) root.findViewById(R.id.login_username);
		name.setVisibility(View.VISIBLE);

		((TextView) root.findViewById(R.id.login_username)).setHint(getResources().getString(R.string.edit_name_hint));
		((TextView) root.findViewById(R.id.login_email)).setHint(getResources().getString(R.string.email));
		// root.findViewById(R.id.blank_view).getLayoutParams().height = height
		// - 4 * width / 7
		// - statusBarHeight
		// - (int) getResources().getDimension(R.dimen.height3);

		nameLength = name.getText().toString().length();
		pswdLength = ((EditText) root.findViewById(R.id.login_password)).getText().toString().length();
		emailLength = ((EditText) root.findViewById(R.id.login_email)).getText().toString().length();

		int filled = nameLength * pswdLength * emailLength;

		if (filled > 0) {
			root.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
		} else {
			root.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
		}

		/*
		 * final EditText input = (EditText)
		 * root.findViewById(R.id.login_username); input.setFocusable(true);
		 * input.setFocusableInTouchMode(true); input.requestFocus();
		 */
	}

	public void login(View view) {

		loginPage.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);

		EditText user = (EditText) view.findViewById(R.id.login_email);
		EditText pwd = (EditText) view.findViewById(R.id.login_password);

		final String username = user.getText().toString().trim();
		final String password = pwd.getText().toString();
		CryptoHelper cryptoHelper = new CryptoHelper();
		String encrypted_password = "";
		try {
			encrypted_password = cryptoHelper.encrypt(password,null,null);
		} catch (Exception e) {
			e.printStackTrace();

		}

		Boolean ok = checkBlankFields(view, false);
		invited_by = prefs.getString("invited_by",null);

		if (!ok) {
			// Async task to check username and password
			z_ProgressDialog = ProgressDialog.show(SplashScreen.this, null, getResources().getString(R.string.verifying_creds), true, false);
			z_ProgressDialog.setCancelable(false);
			UploadManager.login("", username, encrypted_password,invited_by);
		}
	}

	public boolean checkBlankFields(View v, boolean flag) {
		Boolean ok = false;
		EditText user = (EditText) v.findViewById(R.id.login_email);
		EditText pwd = (EditText) v.findViewById(R.id.login_password);

		if (pwd.getText().toString().equals("")) {
			ok = true;
		}

		if (user.getText().toString().equals("")) {
			ok = true;
		}

		if (flag) {
			EditText name = (EditText) v.findViewById(R.id.login_username);
			String usrnm = name.getText().toString();
			if (usrnm.equals(""))
				ok = true;
		}

		if (ok && z_ProgressDialog != null) {
			z_ProgressDialog.dismiss();
		}
		return ok;
	}

	private void setAlreadyHaveAnAccountText() {

		String firstString = getString(R.string.already_have_account);
		String secondString = " " + getString(R.string.Login);

		SpannableStringBuilder finalStr = new SpannableStringBuilder();

		// first
		SpannableString firstStr = new SpannableString(firstString);
		ClickableSpan firstSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				mState = LOGIN_SHOWN;
				animaterToScreen2(findViewById(R.id.layout_login_text), true);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(false);
				ds.setTextSize(getResources().getDimension(R.dimen.size16));
				ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Light));
				ds.setColor(getResources().getColor(R.color.white_trans_seventy));
			}
		};
		firstStr.setSpan(firstSpan, 0, firstString.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

		SpannableString secondStr = new SpannableString(secondString);
		ClickableSpan secondSpan = new ClickableSpan() {
			@Override
			public void onClick(View widget) {
				mState = LOGIN_SHOWN;
				animaterToScreen2(findViewById(R.id.layout_login_text), true);
			}

			@Override
			public void updateDrawState(TextPaint ds) {
				super.updateDrawState(ds);
				ds.setUnderlineText(false);
				ds.setTextSize(getResources().getDimension(R.dimen.size16));
				ds.setTypeface(CommonLib.getTypeface(getApplicationContext(), CommonLib.Bold));
				ds.setColor(getResources().getColor(R.color.white_trans_seventy));
			}
		};
		secondStr.setSpan(secondSpan, 0, secondString.length(), SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);

		finalStr.append(firstStr);
		finalStr.append(secondStr);
		((TextView) signUpPage.findViewById(R.id.login_page_already_have_an_account)).setText(finalStr, TextView.BufferType.SPANNABLE);
		makeLinksFocusable(((TextView) signUpPage.findViewById(R.id.login_page_already_have_an_account)));
	}

	private void makeLinksFocusable(TextView tv) {
		MovementMethod m = tv.getMovementMethod();
		if ((m == null) || !(m instanceof LinkMovementMethod)) {
			if (tv.getLinksClickable()) {
				tv.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	protected void clickSubmitLogin(View v) {

		lastVisited = SIGN_IN;

		CommonLib.ZLog("clickSubmitLogin", "clickSubmitLogin");

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);

		login(loginPage.findViewById(R.id.login_details));
	}

	protected void clickSubmitSignup(View v) {

		lastVisited = SIGN_UP;

		CommonLib.ZLog("clickSubmitSignup", "clickSubmitSignup");

		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.main_root).getWindowToken(), 0);

		String userName = ((TextView)signUpPage.findViewById(R.id.login_username)).getText().toString().trim();
		String email = ((TextView)signUpPage.findViewById(R.id.login_email)).getText().toString().trim();
		String password = ((TextView)signUpPage.findViewById(R.id.login_password)).getText().toString();

		CryptoHelper cryptoHelper = new CryptoHelper();
		String encrypted_password = "";
		try {
			encrypted_password = cryptoHelper.encrypt(password,null,null);
		} catch (Exception e) {
			e.printStackTrace();

		}
		invited_by = prefs.getString("invited_by",null);

		Boolean ok = checkBlankFields(signUpPage, true);
		if(!ok) {
			z_ProgressDialog = ProgressDialog.show(mContext, null,
					mContext.getResources().getString(R.string.signingup_wait));
			z_ProgressDialog.setCancelable(false);

			UploadManager.signup(userName, email, encrypted_password, invited_by);
		}
	}


	private void loginSetup(View root) {
		// lastVisited = SIGN_IN;

		// final View pageRoot = findViewById(R.id.main_root);
		// pageRoot.findViewById(R.id.blank_space).setVisibility(View.VISIBLE);

		// root.findViewById(R.id.login_container).setVisibility(View.VISIBLE);
		root.findViewById(R.id.login_username).setVisibility(View.GONE);
		root.findViewById(R.id.login_email).setVisibility(View.VISIBLE);
		root.findViewById(R.id.login_password).setVisibility(View.VISIBLE);
		root.findViewById(R.id.view1).setVisibility(View.GONE);
		root.findViewById(R.id.forgot_pass_text).setVisibility(View.VISIBLE);

		// root.findViewById(R.id.blank_view).setVisibility(View.VISIBLE);

		((TextView) root.findViewById(R.id.login_email)).setHint(getResources().getString(R.string.email_username));
		// root.findViewById(R.id.blank_view).getLayoutParams().height = height-
		// statusBarHeight - 3 * width / 7 - width / 10;

		pswdLength = ((EditText) root.findViewById(R.id.login_password)).getText().toString().length();
		emailLength = ((EditText) root.findViewById(R.id.login_email)).getText().toString().length();

		int filled = pswdLength * emailLength;

		if (filled > 0) {
			root.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_green_button);
		} else {
			root.findViewById(R.id.login_submit).setBackgroundResource(R.drawable.bottom_button_border);
		}
	}

	public void forgotPassword(View v) {
		final View customView = inflater.inflate(R.layout.forgot_password_dialog, null);

		fPasswordDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
				.setCancelable(true)
				.setView(customView)
				.create();

		customView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String inputMail = ((TextView)customView.findViewById(R.id.email_input)).getText().toString().trim();
				//check email syntax
				boolean result = true;
				try {
					String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
					Pattern p = Pattern.compile(ePattern);
					java.util.regex.Matcher m = p.matcher(inputMail);
					result = m.matches();
				} catch (Exception ex) {
					result = false;
				}
				if(!result) {
					Toast.makeText(mContext, "Invalid email", Toast.LENGTH_SHORT).show();
					return;
				}
				fPasswordDialog.dismiss();
				if(z_ProgressDialog != null && z_ProgressDialog.isShowing())
					z_ProgressDialog.dismiss();
				z_ProgressDialog = ProgressDialog.show(mContext, null, "Validating email");
				UploadManager.updatePassword(inputMail, "", "", "");

			}
		});
		fPasswordDialog.setCanceledOnTouchOutside(true);
		fPasswordDialog.show();
	}

	public void facebookAction(View view) {
		ZTracker.logGAEvent(SplashScreen.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_FACEBOOK_LOGIN_PRESSED, "");
		z_ProgressDialog = ProgressDialog.show(SplashScreen.this, null,
				getResources().getString(R.string.signingup_wait), true, false);
		z_ProgressDialog.setCancelable(false);
		String regId = prefs.getString("registration_id", "");
		hasSwipedPager = true;
		FacebookConnect facebookConnect = new FacebookConnect(SplashScreen.this, 1, APPLICATION_ID, true,
				regId, invitationId);
		facebookConnect.execute();
	}

	public void googleAction(View view) {
		ZTracker.logGAEvent(SplashScreen.this, ZTracker.CATEGORY_WIDGET_ACTION, ZTracker.ACTION_GOOGLE_LOGIN_PRESSED, "");
		z_ProgressDialog = ProgressDialog.show(SplashScreen.this, null,
				getResources().getString(R.string.signingup_wait), true, false);
		z_ProgressDialog.setCancelable(false);
		hasSwipedPager = true;
		googleLogin();
	}
}
