package com.application.zapplon.utils.facebook;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.application.zapplon.R;
import com.application.zapplon.data.FriendsCollection;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.ParserJson;
import com.application.zapplon.utils.PostWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class FacebookConnect {

	// permissions sought from facebook
	private final String PERMISSION_EMAIL = "email";
	private final String PERMISSION_USER_FRIENDS = "user_friends";
	private final String PERMISSION_PUBLIC_PROFILE = "public_profile";
	private final String PERMISSION_ABOUT_ME = "user_about_me";
	private final String PERMISSION_POST = "publish_actions";
	
	// fields sought from the facebook 'user' object
	private final String FIELD_NAME = "name";
	private final String FIELD_ID = "id";
	private final String FIELD_EMAIL = "email";
	private final String FIELD_BIRTHDAY = "birthday";
	private final String FIELD_GENDER = "gender";
	//private final String FIELD_LOCATION = "location";
	private final String FIELDS = "fields";
	private final String FIELD_PICTURE = "picture.type(large)";
	private final String FIELD_ABOUT_ME = "bio";
	
	private Exception failException = null;

	/**
	 * action
	 *  1 => login
	 *  2 => connect
	 *  3 => post
	 *  4 => connect and post
	 *  5 => friends list
	 */
	private int action = 1;
	private String APPLICATION_ID = "";
	private String accessToken = "";
	private int returnAction = 1;
	private boolean regIdSent = false;

	FacebookConnectCallback callback;
	private boolean switchToThreeWhenSessionOpened = false;

	// gcm
	String regId, invitationId;

	public FacebookConnect(FacebookConnectCallback callback, int action, String APPLICATION_ID, boolean gcm, String regId, String invitationId) {
		this.action = action;
		this.callback = callback;
		this.APPLICATION_ID = APPLICATION_ID;
		returnAction = action;
		this.regId = regId;
		this.invitationId = invitationId;
	}

	public FacebookConnect(FacebookConnectCallback callback, int action, String APPLICATION_ID, String accessToken) {
		this.action = action;
		this.callback = callback;
		this.APPLICATION_ID = APPLICATION_ID;
		this.accessToken = accessToken;
		returnAction = action;
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {

			CommonLib.ZLog("FC session", session.toString());
			CommonLib.ZLog("FC state", state.name());

			if (exception != null) {

				failException = exception;
				CommonLib.ZLog("FC exception", exception.toString() + ".");
				exception.printStackTrace();

				Bundle bundle = new Bundle();
				bundle.putInt("action", returnAction);
				bundle.putInt("status", 0);

				if (failException != null) {
					try {
						bundle.putString("error_exception", failException.getClass().toString());
						bundle.putString("error_stackTrace", Log.getStackTraceString(failException) + "");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				callback.response(bundle);
			}

			if (session.isOpened()) {
				CommonLib.ZLog("FC", "session isOpened");
				fbRequestMe(session);
				session.removeCallback(this);

			} else {
				if (state == SessionState.CLOSED_LOGIN_FAILED || state == SessionState.CLOSED) {
					CommonLib.ZLog("FC", "session !isOpened");
					Bundle bundle = new Bundle();
					bundle.putInt("action", returnAction);
					bundle.putInt("status", 0);
					callback.response(bundle);
					session.removeCallback(this);
				}
			}

		}
	}

	private Session.StatusCallback mStatusCallback = new SessionStatusCallback();

	public void execute() {

		CommonLib.ZLog("FC", "Session.getActiveSession()");

		// get the current active session
		Session session = Session.getActiveSession();

		// create session if null
		if (session == null) {
			CommonLib.ZLog("FC", "new Session");
			session = new Session((Activity) callback);
			Session.setActiveSession(session);
		}
		CommonLib.ZLog("FC session state", session.getState() + ".");

		if (!session.isOpened()) {
			String permsString = (action != 3) ? PERMISSION_EMAIL : PERMISSION_POST;

			if (action != 3) {

				if (!session.isOpened() && !session.isClosed()) {
					CommonLib.ZLog("FC", "open for read, site alpha");
					session.openForRead(new Session.OpenRequest((Activity) callback)
								.setPermissions(Arrays.asList(permsString, PERMISSION_USER_FRIENDS, PERMISSION_PUBLIC_PROFILE, PERMISSION_ABOUT_ME))
								.setCallback(mStatusCallback));

			    } else {
                   CommonLib.ZLog("FC", "open for read, site bravo");
                    Session.openActiveSession((Activity) callback, true, mStatusCallback);
                }

			} else {
				
				if (!session.isOpened() && !session.isClosed()) {
					CommonLib.ZLog("FC", "open for publish, site alpha");
					session.openForPublish(new Session.OpenRequest((Activity) callback)
								.setPermissions(Arrays.asList(permsString))
								.setCallback(mStatusCallback));

			    } else {
                   CommonLib.ZLog("FC", "open for publish, site bravo");
                   Session.openActiveSession((Activity) callback, true, mStatusCallback);
                }
			}

		} else {

			CommonLib.ZLog("FC", "session opened");
			if (action == 3) {

				try {
					if (!switchToThreeWhenSessionOpened)
						session.requestNewPublishPermissions(new NewPermissionsRequest((Activity) callback, Arrays.asList(new String[] { PERMISSION_POST})));

					session.addCallback(new Session.StatusCallback() {

						@Override
						public void call(Session session, SessionState state, Exception exception) {

							if (exception != null) {
								CommonLib.ZLog("FC exception", exception.toString() + ".");
								exception.printStackTrace();
								Bundle bundle = new Bundle();
								bundle.putInt("action", returnAction);
								bundle.putInt("status", 0);
								callback.response(bundle);

							}

							if (session.isOpened()) {
								CommonLib.ZLog("FC", "session isOpened");
								fbRequestMe(session);
								session.removeCallback(this);

							} else {
								if (state == SessionState.CLOSED_LOGIN_FAILED || state == SessionState.CLOSED) {
									CommonLib.ZLog("FC", "session !isOpened");
									Bundle bundle = new Bundle();
									bundle.putInt("action", returnAction);
									bundle.putInt("status", 0);
									callback.response(bundle);
									session.removeCallback(this);
								}

							}
						}
					});

				} catch (Exception e) {
					CommonLib.ZLog("FC", "Exception Raised");
					failException = e;
					e.printStackTrace();
				}
			} else {
				fbRequestMe(session);
			}
		}
	}

	public void fbRequestMe(final Session session) {

		CommonLib.ZLog("FC", "fbRequestME");

		String REQUEST_FIELDS = TextUtils.join(",", new String[] {
				FIELD_ID, FIELD_EMAIL, FIELD_NAME, FIELD_BIRTHDAY, FIELD_GENDER, FIELD_PICTURE, FIELD_ABOUT_ME});//, FIELD_LOCATION });

		Bundle parameters = new Bundle();
		parameters.putString(FIELDS, REQUEST_FIELDS);
		
		final Request request = new Request();
		request.setSession(session);
		request.setParameters(parameters);
		request.setGraphPath("me");
		request.setCallback(new Request.Callback() {

			@Override
			public void onCompleted(Response response) {

				CommonLib.ZLog("FC response.getRawResponse();", response.getRawResponse());
				final GraphUser user = response.getGraphObjectAs(GraphUser.class);
		
				if (user != null) {
					CommonLib.ZLog("FC", "user !null");
					String email = "";
					try {
						JSONObject json = user.getInnerJSONObject();
						Map<String, Object> map = user.asMap();
						json = new JSONObject(map);

						final JSONObject finalJson = json;

						if(json.has("email"))
							email = json.getString("email");
						final JSONArray permissionsJson = new JSONArray(session.getPermissions());
						if (action == 1) {
							//check for the email here...
//							if email is null show dialog

							if(email == null || email.equalsIgnoreCase("")) {
								LayoutInflater inflater = LayoutInflater.from((Context) callback);
								final View customView = inflater.inflate(R.layout.email_input_dialog, null);
								final AlertDialog dialog = new AlertDialog.Builder((Context) callback, AlertDialog.THEME_HOLO_LIGHT)
										.setCancelable(false)
										.setView(customView)
										.create();
								dialog.setCanceledOnTouchOutside(false);
								dialog.show();
								customView.findViewById(R.id.email_input).requestFocus();
								((InputMethodManager) ((Activity)callback).getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(((EditText)customView.findViewById(R.id.email_input)), InputMethodManager.SHOW_FORCED);
								try {
									android.os.Handler handler = new android.os.Handler();
									handler.postDelayed(new Runnable() {
										@Override
										public void run() {
											if( (Build.VERSION.SDK_INT >=17 && !((Activity) callback).isDestroyed()) && customView != null && customView.findViewById(R.id.email_input) != null)
												((InputMethodManager) ((Activity)callback).getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(customView.findViewById(R.id.email_input), InputMethodManager.SHOW_FORCED);
										}
									}, 400);

								} catch (Exception e) {
									e.printStackTrace();
								}
								customView.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View v) {
										String inputMail = ((TextView)customView.findViewById(R.id.email_input)).getText().toString().trim();
										if(inputMail == null || inputMail.equalsIgnoreCase("")) {
											Toast.makeText((Context) callback, "Invalid email", Toast.LENGTH_SHORT).show();
											return;
										}
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
											Toast.makeText((Context) callback, "Invalid email", Toast.LENGTH_SHORT).show();
											return;
										}
										CommonLib.hideKeyBoard((Activity)callback, customView.findViewById(R.id.email_input));
										new FBLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] { user.getId().toString(), finalJson.toString(), inputMail.trim().toString(), session.getAccessToken(), permissionsJson.toString() });
										dialog.dismiss();
									}
								});
							} else
								new FBLogin().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] { user.getId().toString(), json.toString(), email, session.getAccessToken(), permissionsJson.toString() });
						} else if (action == 2 || action == 3) {
							new FBConnect().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] { user.getId().toString(), json.toString(), user.getName(), session.getAccessToken(), permissionsJson.toString() });
						} else if (action == 5) {
							new FBConnectForInvite().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[] { user.getId().toString(), json.toString(), user.getName(), session.getAccessToken(), permissionsJson.toString() });
						} else {
							CommonLib.ZLog("FC", "onCompleted");
							action = 3;

							// Session request in execute code
							switchToThreeWhenSessionOpened = true;
							session.requestNewPublishPermissions(new NewPermissionsRequest((Activity) callback, Arrays.asList(new String[] { PERMISSION_POST })));
							execute();
						}

					} catch (Exception e) {
						failException = e;
						e.printStackTrace();
					}

				} else {
					Bundle bundle = new Bundle();
					bundle.putInt("action", returnAction);
					bundle.putInt("status", 0);

					callback.response(bundle);
					// callback = null;
					CommonLib.ZLog("FC", "user null");
				}

				request.setCallback(null);
			}
		});
		CommonLib.ZLog("FC", "batchAsyncFired");
		Request.executeBatchAsync(request);
	}

	private class FBConnect extends AsyncTask<String, Void, Element> {
		private String name;
		private long id;

		@Override
		protected Element doInBackground(String... params) {

			CommonLib.ZLog("FC", "FBConnect");
			id = Long.parseLong(params[0]);
			name = params[2];
			try {
				String url = (CommonLib.SERVER + "facebookconnect.xml?uuid=" + APPLICATION_ID + CommonLib.getVersionString(null));

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("fbid", params[0]));
				try {
					JSONObject fbDataJson = new JSONObject(params[1]);
					String profile_pic = null;
					if (fbDataJson.has("picture")) {
						JSONObject profilePicJson;
						profilePicJson = fbDataJson.getJSONObject("picture");
						if (profilePicJson.has("data")) {
							profilePicJson = profilePicJson.getJSONObject("data");
							if (profilePicJson.has("url"))
								profile_pic = String.valueOf(profilePicJson.get("url"));
						}
					}
					if(profile_pic != null)
						nameValuePairs.add(new BasicNameValuePair("profile_pic", profile_pic));
					JSONObject fbData = new JSONObject();
					if(fbDataJson.has("id"))
						fbData.put("id", String.valueOf(fbDataJson.get("id")));
					if(fbDataJson.has("email"))
						fbData.put("email", String.valueOf(fbDataJson.get("email")));
					if(fbDataJson.has("name"))
						fbData.put("name", String.valueOf(fbDataJson.get("name")));
					nameValuePairs.add(new BasicNameValuePair("fbdata", String.valueOf(fbData)));
				} catch(JSONException e) {
					e.printStackTrace();
				}
				nameValuePairs.add(new BasicNameValuePair("fb_token", params[3]));
				nameValuePairs.add(new BasicNameValuePair("fb_permission", params[4]));

				if (action == 3)
					nameValuePairs.add(new BasicNameValuePair("post_to_facebook_flag", "1"));

				HttpResponse response = PostWrapper.getPostResponse(url, nameValuePairs, null);

				InputStream is = CommonLib.getStream(response);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(is);
				Element res = dom.getDocumentElement();
				is.close();
				return res;
			} catch (Exception e) {
				failException = e;
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Element res) {
			String message = "";
			String mFacebookName = "";
			String mFacebookConnectFlag = "0";
			String mPostToFacebookFlag = "0";
			boolean postToFB = false;

			boolean authenticated = false;

			if (res != null) {
				try {
					boolean status = (res.getElementsByTagName("status").item(0).getFirstChild().getNodeValue().equals("success")) ? true : false;
					if (!status) {
						authenticated = false;
						message = res.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();
					} else {
						authenticated = true;
						message = res.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();
						mFacebookName = name;
						mFacebookConnectFlag = "1";

						mPostToFacebookFlag = res.getElementsByTagName("post_to_facebook_flag").item(0).getFirstChild().getNodeValue();

					}
				} catch (Exception e) {
					failException = e;
					e.printStackTrace();
				} finally {
					Bundle bundle = new Bundle();
					bundle.putInt("action", returnAction);
					bundle.putInt("status", authenticated ? 1 : 0);
					bundle.putString("facebookName", mFacebookName);
					bundle.putString("facebookConnectFlag", mFacebookConnectFlag);
					bundle.putString("postToFacebookFlag", mPostToFacebookFlag);
					bundle.putLong("facebookId", id);
					bundle.putString("message", message);
					callback.response(bundle);
					// callback = null;
					CommonLib.ZLog("here", "here1");
				}
			} else {
				Bundle bundle = new Bundle();
				bundle.putInt("action", returnAction);
				bundle.putInt("status", authenticated ? 1 : 0);
				bundle.putString("facebookName", mFacebookName);
				bundle.putString("facebookConnectFlag", mFacebookConnectFlag);
				bundle.putString("postToFacebookFlag", mPostToFacebookFlag);
				bundle.putLong("facebookId", id);
				bundle.putString("message", ((Context) callback).getResources().getString(R.string.err_occurred));
				callback.response(bundle);
			}
		}
	}

	private class FBLogin extends AsyncTask<String, Void, JSONObject> {
		// private String email;

		int responseCode = -1;

		@Override
		protected JSONObject doInBackground(String... params) {

			// email = params[2];
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("fbid", params[0]));
				String bio = "";

				try {
					JSONObject fbDataJson = new JSONObject(params[1]);
					String profile_pic = null;
					if (fbDataJson.has("picture")) {
						JSONObject profilePicJson;
						profilePicJson = fbDataJson.getJSONObject("picture");
						if (profilePicJson.has("data")) {
							profilePicJson = profilePicJson.getJSONObject("data");
							if (profilePicJson.has("url"))
								profile_pic = String.valueOf(profilePicJson.get("url"));
						}
					}
					if(fbDataJson.has("bio")) {
						bio = String.valueOf(fbDataJson.get("bio"));
						nameValuePairs.add(new BasicNameValuePair("bio", bio));
					}
					if(profile_pic != null)
						nameValuePairs.add(new BasicNameValuePair("profile_pic", profile_pic));
					JSONObject fbData = new JSONObject();
					if(fbDataJson.has("id"))
						fbData.put("id", String.valueOf(fbDataJson.get("id")));
					if(fbDataJson.has("email"))
						fbData.put("email", String.valueOf(fbDataJson.get("email")));
					if(fbDataJson.has("name"))
						fbData.put("name", String.valueOf(fbDataJson.get("name")));
					nameValuePairs.add(new BasicNameValuePair("fbdata", String.valueOf(fbData)));
				} catch(JSONException e) {
					e.printStackTrace();
				}

				nameValuePairs.add(new BasicNameValuePair("fb_token", params[3]));
				nameValuePairs.add(new BasicNameValuePair("fb_permission", params[4]));
				
				try {
					JSONObject object = new JSONObject(params[1]);
					if( object.has("email") ) {
						String emailStr = String.valueOf(object.get("email"));
						if(emailStr != null)
							nameValuePairs.add(new BasicNameValuePair("email", emailStr));
						else {
							try{
								AccountManager am = AccountManager.get(((Context) callback).getApplicationContext());
								if(am!=null){
									Account[] accounts= am.getAccounts();
									Pattern emailPattern = Patterns.EMAIL_ADDRESS;
									for(Account account: accounts) {
										if(account.type.equals("com.google")&&emailPattern.matcher(account.name).matches()) {
											emailStr = account.name;
										}
									}
								}
							} catch(Exception e){
								Crashlytics.logException(e);
							}
							nameValuePairs.add(new BasicNameValuePair("email", emailStr));
						}
					} else {
						//Pre-fill of sign-up credentials
						String emailStr = null;
						try{
							AccountManager am = AccountManager.get(((Context) callback).getApplicationContext());
							if(am!=null){
								Account[] accounts= am.getAccounts();
								Pattern emailPattern = Patterns.EMAIL_ADDRESS;
								for(Account account: accounts) {
									if(account.type.equals("com.google")&&emailPattern.matcher(account.name).matches()) {
										emailStr = account.name;
									}
								}
							}
						} catch(Exception e){
							Crashlytics.logException(e);
						}
						if(emailStr != null)
							nameValuePairs.add(new BasicNameValuePair("email", emailStr));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
				nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));
				nameValuePairs.add(new BasicNameValuePair("referred_by", invitationId));
				nameValuePairs.add(new BasicNameValuePair("deviceId", CommonLib.getIMEI((Context)callback)));

				if (callback instanceof Activity) {
					CommonLib.ZLog("FC", "FBLogin Inside the callback check ");
					SharedPreferences prefs = ((Activity) callback).getSharedPreferences("application_settings", 0);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("description", bio);
					editor.commit();

					String invited_by = prefs.getString("invited_by",null);
					nameValuePairs.add(new BasicNameValuePair("invited_by", invited_by));
					prefs.edit().remove("invited_by").commit();

					regId = prefs.getString("registration_id", "");
					if (!regId.equals("")) {
						CommonLib.ZLog("FC", "FBLogin Sending UUID ");
						regIdSent = true;
						nameValuePairs.add(new BasicNameValuePair("channel_url", regId));
						nameValuePairs.add(new BasicNameValuePair("uuid", regId));
					}
				}


				String url = CommonLib.SERVER + "auth/login?isFacebookLogin=true" + "&uuid=" + APPLICATION_ID + "&device_id=" + CommonLib.getIMEI((Context)callback);
				
				HttpResponse response = PostWrapper.getPostResponse(url, nameValuePairs, null);

				if (response != null) {
					responseCode = response.getStatusLine().getStatusCode();
				}
				int responseCode = response.getStatusLine().getStatusCode();

				JSONObject resp = null;
				if (responseCode == HttpURLConnection.HTTP_OK) {
					InputStream is = CommonLib.getStream(response);
					resp = ParserJson.parseFBLoginResponse(is);
				}
				return resp;
			} catch (Exception e) {
				failException = e;
			}

			return null;
		}

		@Override
		protected void onPostExecute(JSONObject res) {
			if (res != null) {
				String errorMessage = "";
				int uid = 0;
				String uname = "";
				String accessToken = "";
				String thumbUrl = "";
				String profile_pic = "";
				String email = "";
				boolean verified_user = false;
				int status = 0;

				try {
					if( res != null && res.has("access_token") && res.has("user_id") && res.get("user_id") instanceof Integer) {
						status = 1;
						accessToken = res.getString("access_token"); 
						uid = res.getInt("user_id");
						if(res.has("HSLogin") && res.get("HSLogin") instanceof Boolean) {
							SharedPreferences prefs = ((Context)callback).getSharedPreferences("application_settings", 0);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putBoolean("HSLogin",
									res.getBoolean("HSLogin"));
							if (res.has("instutionLogin")) {
								editor.putBoolean("instutionLogin",
										res.getBoolean("instutionLogin"));
							}
							if (res.has("INSTITUTION_NAME")) {
								editor.putString("INSTITUTION_NAME",
										res.getString("INSTITUTION_NAME"));
							}
							if (res.has("STUDENT_ID")) {
								editor.putString("STUDENT_ID",
										res.getString("STUDENT_ID"));
							}
							editor.commit();
						}
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
						status = 0;
						errorMessage = "Something went wrong";
					}
				} catch (Exception e) {
                    e.printStackTrace();
					failException = e;
				} finally {

					Bundle bundle = new Bundle();
					bundle.putInt("uid", uid);
					bundle.putString("profile_pic", profile_pic);
				    bundle.putString("email", email);
					bundle.putString("username", uname);
					bundle.putString("thumbUrl", thumbUrl);
					bundle.putString("access_token", accessToken);
					bundle.putInt("action", returnAction);
					bundle.putInt("status", status);
					bundle.putBoolean("verifiedUser", verified_user);

					if (responseCode != -1 && responseCode != 200 && responseCode != 304)
						bundle.putString("error_responseCode", responseCode + "");

					if (failException != null) {
						try {
							bundle.putString("error_exception", failException.getClass().toString());

							CommonLib.ZLog("Error Exception", Log.getStackTraceString(failException));
							bundle.putString("error_stackTrace", Log.getStackTraceString(failException) + "");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					bundle.putBoolean("reg_id_sent", regIdSent);

					callback.response(bundle);
				}
			} else {
				Bundle bundle = new Bundle();
				bundle.putInt("action", returnAction);
				bundle.putInt("status", 0);
				bundle.putString("errorMessage", ((Context) callback).getResources().getString(R.string.err_occurred));

				if (responseCode != -1 && responseCode != 200) {
					bundle.putString("error_responseCode", responseCode + "");
				}

				if (failException != null) {
					try {
						CommonLib.ZLog("Error Exception", Log.getStackTraceString(failException));
						bundle.putString("error_exception", failException.getClass().toString());
						bundle.putString("error_stackTrace", Log.getStackTraceString(failException));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				callback.response(bundle);
			}
		}
	}

	private class FBConnectForInvite extends AsyncTask<String, Void, Element> {
		private String name;
		private long id;

		@Override
		protected Element doInBackground(String... params) {

			id = Long.parseLong(params[0]);
			name = params[2];
			try {
				CommonLib.ZLog("FC", "FBConnectForInvite");
				String url = (CommonLib.SERVER + "facebookconnect.xml?uuid=" + APPLICATION_ID + CommonLib.getVersionString(null));

				// BasicHeader d2 = new BasicHeader("Authorization",
				// "Basic c3RhZ2luZzpwaGF1Z2hvWGlpNWF5dQ==");
				// post.addHeader(d2);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("fbid", params[0]));
				try {
					JSONObject fbDataJson = new JSONObject(params[1]);
					String profile_pic = null;
					if (fbDataJson.has("picture")) {
						JSONObject profilePicJson;
						profilePicJson = fbDataJson.getJSONObject("picture");
						if (profilePicJson.has("data")) {
							profilePicJson = profilePicJson.getJSONObject("data");
							if (profilePicJson.has("url"))
								profile_pic = String.valueOf(profilePicJson.get("url"));
						}
					}
					if(profile_pic != null)
						nameValuePairs.add(new BasicNameValuePair("profile_pic", profile_pic));
					JSONObject fbData = new JSONObject();
					if(fbDataJson.has("id"))
						fbData.put("id", String.valueOf(fbDataJson.get("id")));
					if(fbDataJson.has("email"))
						fbData.put("email", String.valueOf(fbDataJson.get("email")));
					if(fbDataJson.has("name"))
						fbData.put("name", String.valueOf(fbDataJson.get("name")));
					nameValuePairs.add(new BasicNameValuePair("fbdata", String.valueOf(fbData)));
				} catch(JSONException e) {
					e.printStackTrace();
				}
				
				nameValuePairs.add(new BasicNameValuePair("fb_token", params[3]));
				nameValuePairs.add(new BasicNameValuePair("fb_permission", params[4]));
				nameValuePairs.add(new BasicNameValuePair("response_type", "get_friends"));

				HttpResponse response = PostWrapper.getPostResponse(url, nameValuePairs, null);
				InputStream is = CommonLib.getStream(response);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(is);
				// XMLParser.printXMLString(dom);
				Element res = dom.getDocumentElement();

				is.close();

				return res;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Element res) {
			String message = "";
			String mPostToFacebookFlag = "0";
			FriendsCollection fbFriends = new FriendsCollection();
			boolean authenticated = false;

			if (res != null) {
				try {
					boolean status = (res.getElementsByTagName("status").item(0).getFirstChild().getNodeValue().equals("success")) ? true : false;

					if (!status) {
						authenticated = false;
						message = res.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();

					} else {
						authenticated = true;
						message = res.getElementsByTagName("message").item(0).getFirstChild().getNodeValue();
						mPostToFacebookFlag = res.getElementsByTagName("post_to_facebook_flag").item(0).getFirstChild().getNodeValue();
//						fbFriends = XMLParser.parseFbFriends((Element) res.getElementsByTagName("friends").item(0));
						if (res.getElementsByTagName("friends_count_string").item(0) != null) {
							fbFriends.setFriendsCountString(res.getElementsByTagName("friends_count_string").item(0).getFirstChild().getNodeValue());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Bundle bundle = new Bundle();
					bundle.putString("message", message);
					bundle.putInt("action", returnAction);
					bundle.putInt("status", authenticated ? 1 : 0);
					bundle.putString("postToFacebookFlag", mPostToFacebookFlag);
					bundle.putSerializable("friends", fbFriends);
					bundle.putString("platform", "facebook");
					callback.response(bundle);

					// bundle.putString("facebookName", mFacebookName);
					// bundle.putString("facebookConnectFlag",
					// mFacebookConnectFlag);
					// bundle.putLong("facebookId", id);
				}
			} else {
				Bundle bundle = new Bundle();
				bundle.putString("message", "");
				bundle.putInt("action", returnAction);
				bundle.putInt("status", authenticated ? 1 : 0);
				bundle.putString("postToFacebookFlag", mPostToFacebookFlag);
				bundle.putSerializable("friends", fbFriends);
				bundle.putString("platform", "facebook");
				callback.response(bundle);
			}
		}
	}

}