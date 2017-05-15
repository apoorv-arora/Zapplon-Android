package com.application.zapplon.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Message;

import com.application.zapplon.ZApplication;
import com.application.zapplon.data.Address;
import com.application.zapplon.data.AppConfig;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.data.CabTimeEstimates;
import com.application.zapplon.data.ConnectedAccount;
import com.application.zapplon.data.IntercityCab;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class RequestWrapper {
	private static SharedPreferences prefs;
	private static ZApplication zapp;

	// cache time
	public static final int FAV = -1;
	public static final int TEMP = 86400;
	public static final int CONSTANT = 1209600;
	public static final int ONE_HOUR = 3600;
	public static final int THREE_HOURS = 3600 * 3;

	// contant identifiers
	public static final String USER_MESSAGES = "user_messages";
	public static final String USER_INFO = "user_info";
	public static final String UBER_LIST = "uber_list";
	public static final String UBER_TIME_ESTIMATES = "UBER_TIME_ESTIMATES";
	public static final String APP_CONFIG = "APP_CONFIG";
	public static final String CONNECTED_ACCOUNTS = "connected_accounts";
	public static final String MY_BOOKINGS = "my_bookings";
	public static final String MY_INTERCITY_BOOKINGS = "my_intercity_bookings";
	public static final String GET_VOUCHER = "get_voucher";
	public static final String GET_POINTS = "get_POINTS";
	public static final String GET_ADDRESS_LIST = "get_address_list";
	public static final String GET_ACTIVE_TRIP = "get_active_trip";
	public static final String GET_OFFLINE_ADDRESS = "get_offline_address";
	public static final String Intercity_List = "intercity_list";

	public static void Initialize(Context context) {
		prefs = context.getSharedPreferences("application_settings", 0);
	}

	public static InputStream fetchhttp(String urlstring) {

		String value = null;
		try {

			CommonLib.ZLog("RW url", urlstring + ".");
			HttpPost httpPost = new HttpPost(urlstring + CommonLib.getVersionString(null));
			httpPost.addHeader(new BasicHeader("access_token", prefs.getString("access_token", "")));
			httpPost.addHeader(new BasicHeader("client_id", CommonLib.CLIENT_ID));
			httpPost.addHeader(new BasicHeader("app_type", CommonLib.APP_TYPE));
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("access_token", prefs.getString("access_token", "")));
			nameValuePairs.add(new BasicNameValuePair("client_id", CommonLib.CLIENT_ID));
			nameValuePairs.add(new BasicNameValuePair("app_type", CommonLib.APP_TYPE));

			nameValuePairs.add(new BasicNameValuePair("latitude", prefs.getString("latitude", "0")));
			nameValuePairs.add(new BasicNameValuePair("longitude", prefs.getString("longitude", "0")));

			if (nameValuePairs != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			}

			CommonLib.ZLog("AccessToken: ", prefs.getString("access_token", ""));

			long timeBeforeApiCall = System.currentTimeMillis();
			HttpResponse response = HttpManager.execute(httpPost);
			CommonLib.ZLog("fetchhttp(); Response Time: ", System.currentTimeMillis() - timeBeforeApiCall);

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream in = CommonLib.getStream(response);
				return in;

			} else {
				CommonLib.ZLog("fetchhttp(); Response Code: ", responseCode + "-------" + urlstring);
			}
		} catch (Exception e) {
			CommonLib.ZLog("Error fetching http url", e.toString());
			e.printStackTrace();
		}
		return  null;
	}

	public static Object RequestHttp(String url, String Object_Type, int status) {
		Object o = null;
		try {
			InputStream http_result;

			http_result = fetchhttp(url);
			o = parse(http_result, Object_Type);
		}
		catch (Exception e) {
			CommonLib.ZLog("Error fetching http url", e.toString());
			e.printStackTrace();
		}
		return o;
	}

	public static Object parse(InputStream result, String Type) {

		Object o = null;

		if (Type == UBER_LIST) {
			ArrayList<CabDetails> categories = null;
			try {
				categories = ParserJson.parseCabDetailsResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return categories;
		}else if (Type == Intercity_List) {
			ArrayList<IntercityCab> intercityCabs = null;
			try {
				intercityCabs = ParserJson.parse_IntercityCabResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return intercityCabs;
		} else if (Type == UBER_TIME_ESTIMATES) {
			ArrayList<CabTimeEstimates> timeEstimates = null;
			try {
				timeEstimates = ParserJson.parse_UberTimeEstimatesResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return timeEstimates;
		} else if (Type == APP_CONFIG) {
			ArrayList<AppConfig> appConfigs = null;
			try {
				appConfigs = ParserJson.parse_AppConfig(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return appConfigs;
		} else if (Type == CONNECTED_ACCOUNTS) {
			ArrayList<ConnectedAccount> connectedAccounts = null;
			try {
				connectedAccounts = ParserJson.parse_ConnectedAccountsResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return connectedAccounts;
		} else if (Type == MY_BOOKINGS) {
			Object[] connectedAccounts = null;
			try {
				connectedAccounts = ParserJson.parse_CabBookingResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return connectedAccounts;
		} else if (Type == MY_INTERCITY_BOOKINGS) {
			Object[] connectedAccounts = null;
			try {
				connectedAccounts = ParserJson.parse_IntercityCabBookingResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return connectedAccounts;
		}
		else if (Type == GET_POINTS) {
			int points = -1;
			try {
				points = ParserJson.parse_UserPointsResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return points;
		} else if (Type == GET_VOUCHER) {
			Object[] connectedAccounts = null;
			try {
				connectedAccounts = ParserJson.parse_VouchersResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return connectedAccounts;
		} else if (Type == GET_ADDRESS_LIST) {
			ArrayList<Address> connectedAccounts = null;
			try {
				connectedAccounts = ParserJson.parse_AddressResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return connectedAccounts;
		} else if (Type == GET_ACTIVE_TRIP) {
			Object[] activeTrips = null;
			try {
				activeTrips = ParserJson.parse_BookingResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return activeTrips;
		} else if (Type == GET_OFFLINE_ADDRESS) {
			Object[] addressList = null;
			try {
				addressList = ParserJson.parseOfflineAddressResponse(result);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return addressList;
		}

		return o;
	}

	public static byte[] Serialize_Object(Object O) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bos);
		out.writeObject(O);
		out.close();

		// Get the bytes of the serialized object
		byte[] buf = bos.toByteArray();
		return buf;
	}

	public static Object Deserialize_Object(byte[] input, String Type) throws IOException, ClassNotFoundException {
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(input));

		if (Type == USER_MESSAGES) {
			Message result = (Message) in.readObject();
			in.close();
			return result;
		} else if (Type.equals("")) {
			Object o = in.readObject();
			in.close();
			return o;
		} else {
			in.close();
			return null;
		}

	}

}
