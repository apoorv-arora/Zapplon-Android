package com.application.zapplon.utils;

import com.application.zapplon.data.Address;
import com.application.zapplon.data.AppConfig;
import com.application.zapplon.data.CabBooking;
import com.application.zapplon.data.CabDetails;
import com.application.zapplon.data.CabTimeEstimates;
import com.application.zapplon.data.ConnectedAccount;
import com.application.zapplon.data.IntercityCab;
import com.application.zapplon.data.Surcharge;
import com.application.zapplon.data.TaxiBookings;
import com.application.zapplon.data.Tracking;
import com.application.zapplon.data.User;
import com.application.zapplon.data.Voucher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class ParserJson {

	@SuppressWarnings("resource")
	public static JSONObject convertInputStreamToJSON(InputStream is) throws JSONException {
		if(is == null)
			return null;
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		String responseJSON = s.hasNext() ? s.next() : "";

		CommonLib.ZLog("response", responseJSON);
		JSONObject map = new JSONObject(responseJSON);
		CommonLib.ZLog("RESPONSE", map.toString(2));
		return map;
	}

	public static Object[] parseSignupResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}


    public static Object[] parseRatingResponse(InputStream is) throws JSONException {

        Object[] output = new Object[]{"failed", "", null};

        JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

        if (responseObject != null && responseObject.has("status")) {
            output[0] = responseObject.getString("status");
            if (output[0].equals("success")) {
                if (responseObject.has("response"))
                    output[1] = responseObject.getString("response");
            } else {
                if (responseObject.has("errorMessage")) {
                    output[1] = responseObject.getString("errorMessage");
                }
            }
        }
        return output;
    }

	public static Object[] parseActiveTripResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.get("errorMessage");
				}
			}
		}
		return output;
	}


	public static Object[] fPasswordResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseRedeemCouponResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseUpdateBookingResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseBillingUpdateResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseGenericResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response"))
					output[1] = responseObject.getString("response");
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseAccessTokenResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");
					ConnectedAccount account = parse_ConnectedAccountObject(responseJson);
					output[1] = account;
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseLoginResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}


	public static Object[] parseOfflineAddressResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static JSONObject parseFBLoginResponse(InputStream is) throws JSONException {

		JSONObject output = new JSONObject();

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			String out = responseObject.getString("status");
			if (out.equals("success")) {
				if (responseObject.has("response")) {
					output = new JSONObject(String.valueOf(responseObject.get("response")));
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output.put("error", responseObject.getString("errorMessage"));
				}
			}
		}
		return output;
	}

	public static Object[] parseGLoginResponse(InputStream is) throws JSONException {

		Object[] output = new Object[2];

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = new JSONObject(String.valueOf(responseObject.get("response")));
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}


	public static Object[] parsevoucherResponse(InputStream is) throws JSONException {

		Object[] output = new Object[2];

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
					output[1] = responseObject.getJSONArray("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}

		return output;
	}

	public static Object[] parseLogoutResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseWishPostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static Object[] parseWishDeletePostResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static User parse_User(JSONObject userObject) {
		if (userObject == null)
			return null;

		User returnUser = new User();
		try {
			if (userObject.has("user_id") && userObject.get("user_id") instanceof Integer) {
				returnUser.setUserId(userObject.getInt("user_id"));
			}

			if (userObject.has("email")) {
				returnUser.setEmail(String.valueOf(userObject.get("email")));
			}

			if (userObject.has("profile_pic")) {
				returnUser.setImageUrl(String.valueOf(userObject.get("profile_pic")));
			}

			if (userObject.has("user_name")) {
				returnUser.setUserName(String.valueOf(userObject.get("user_name")));
			}

			if (userObject.has("bio")) {
				returnUser.setBio(String.valueOf(userObject.get("bio")));
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return returnUser;
	}

	public static ArrayList<CabDetails> parseCabDetailsResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<CabDetails> uberCabs = new ArrayList<CabDetails>();
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray uberJsonArray = responseObject.getJSONArray("response");
			for(int i=0; i<uberJsonArray.length(); i++) {
				if(uberJsonArray.get(i) instanceof JSONObject) {
					JSONObject uberJson = uberJsonArray.getJSONObject(i);
					CabDetails uberCab = parse_CabDetailsObject(uberJson);
					uberCabs.add(uberCab);
				}
			}
		}
		return uberCabs;
	}


	public static Object[] parseIntercityBookingResponse(InputStream is) throws JSONException {

		Object[] response = {"failure",null};
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
			JSONObject object = responseObject.getJSONObject("response");
			response[0] = "success";
			response[1] = parse_TaxiBookings(object);
		} else {
            response[1] = "booking failed";
        }
		return response;
	}


	public static ArrayList<Voucher> parseGetVoucherResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<Voucher> voucherList = new ArrayList<Voucher>();
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray voucherJsonArray = responseObject.getJSONArray("response");
			for(int i=0; i<voucherJsonArray.length(); i++) {
				if(voucherJsonArray.get(i) instanceof JSONObject) {
					JSONObject voucherJson = voucherJsonArray.getJSONObject(i);
					Voucher voucher = parse_voucherObject(voucherJson);
					voucherList.add(voucher);
				}
			}
		}
		return voucherList;
	}

	public static ArrayList<ConnectedAccount> parse_ConnectedAccountsResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<ConnectedAccount> connectedAccounts = new ArrayList<ConnectedAccount>();
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray uberJsonArray = responseObject.getJSONArray("response");
			for(int i=0; i<uberJsonArray.length(); i++) {
				if(uberJsonArray.get(i) instanceof JSONObject) {
					JSONObject connectedAccountJson = uberJsonArray.getJSONObject(i);
					ConnectedAccount connectedAccount = parse_ConnectedAccountObject(connectedAccountJson);
					connectedAccounts.add(connectedAccount);
				}
			}
		}
		return connectedAccounts;
	}

	public static Object[] parse_CabBookingResponse(InputStream is) throws JSONException {

		Object[] output = new Object[2];
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<CabBooking> connectedAccounts = new ArrayList<CabBooking>();
		int size = 0;
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
			JSONObject responseJson = responseObject.getJSONObject("response");

			if(responseJson.has("total") && responseJson.get("total") instanceof Integer)
				size = responseJson.getInt("total");

			if(responseJson.has("bookings") && responseJson.get("bookings") instanceof JSONArray) {
				JSONArray uberJsonArray = responseJson.getJSONArray("bookings");
				for (int i = 0; i < uberJsonArray.length(); i++) {
					if (uberJsonArray.get(i) instanceof JSONObject) {
						JSONObject connectedAccountJson = uberJsonArray.getJSONObject(i);
						CabBooking connectedAccount = parse_CabBookingObject(connectedAccountJson);
						connectedAccounts.add(connectedAccount);
					}
				}
			}
		}
		output[0] = size;
		output[1] = connectedAccounts;
		return output;
	}

	public static Object[] parse_IntercityCabBookingResponse(InputStream is) throws JSONException {

		Object[] output = new Object[2];
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<TaxiBookings> connectedAccounts = new ArrayList<TaxiBookings>();
		int size = 0;
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
			JSONObject responseJson = responseObject.getJSONObject("response");

			if(responseJson.has("total") && responseJson.get("total") instanceof Integer)
				size = responseJson.getInt("total");

			if(responseJson.has("bookings") && responseJson.get("bookings") instanceof JSONArray) {
				JSONArray uberJsonArray = responseJson.getJSONArray("bookings");
				for (int i = 0; i < uberJsonArray.length(); i++) {
					if (uberJsonArray.get(i) instanceof JSONObject) {
						JSONObject connectedAccountJson = uberJsonArray.getJSONObject(i);
						TaxiBookings connectedAccount = parse_TaxiBookings(connectedAccountJson);
						connectedAccounts.add(connectedAccount);
					}
				}
			}
		}
		output[0] = size;
		output[1] = connectedAccounts;
		return output;
	}

	public static Object[] cabBookingTracking(InputStream is) throws JSONException{
		Object[] output = new Object[2];
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		CabBooking cabBooking = new CabBooking();

        if (responseObject!=null && responseObject.has("response") && responseObject.get("response") instanceof JSONObject){
            JSONObject responseJson = responseObject.getJSONObject("response");

            cabBooking = parse_CabBookingObject(responseJson);
            output[0] = "SUCCESS";
            output[1] = cabBooking;

        }
        return output;
	}

	public static Object[] parse_VouchersResponse(InputStream is) throws JSONException {

		Object[] output = new Object[2];
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<Voucher> connectedAccounts = new ArrayList<Voucher>();
		int size = 0;
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
			JSONObject responseJson = responseObject.getJSONObject("response");

			if(responseJson.has("total") && responseJson.get("total") instanceof Integer)
				size = responseJson.getInt("total");

			if(responseJson.has("vouchers") && responseJson.get("vouchers") instanceof JSONArray) {
				JSONArray uberJsonArray = responseJson.getJSONArray("vouchers");
				for (int i = 0; i < uberJsonArray.length(); i++) {
					if (uberJsonArray.get(i) instanceof JSONObject) {
						JSONObject connectedAccountJson = uberJsonArray.getJSONObject(i);
						Voucher connectedAccount = parse_VoucherObject(connectedAccountJson);
						connectedAccounts.add(connectedAccount);
					}
				}
			}
		}
		output[0] = size;
		output[1] = connectedAccounts;
		return output;
	}

	public static ArrayList<Address> parse_AddressResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<Address> connectedAccounts = new ArrayList<Address>();
		int size = 0;
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray responseJsonArr = responseObject.getJSONArray("response");
			for (int i = 0; i < responseJsonArr.length(); i++) {
				if (responseJsonArr.get(i) instanceof JSONObject) {
					JSONObject connectedAccountJson = responseJsonArr.getJSONObject(i);
					Address connectedAccount = parse_AddressObject(connectedAccountJson);
					connectedAccounts.add(connectedAccount);
				}
			}
		}
		return connectedAccounts;
	}

	public static int parse_UserPointsResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof Integer) {
			int points = responseObject.getInt("response");
			return points;
		}
		return -1;
	}

	public static Object[] parse_BookingResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONObject) {
					JSONObject responseJson = responseObject.getJSONObject("response");
					Object[] booking = new Object[2];
					if (responseJson.has("intracityBooking")) {
						booking[0] = parse_CabBookingObject(responseJson);
					}
					if (responseJson.has("intercityBooking")) {
						booking[1] = parse_TaxiBookings(responseJson);
					}
					output[1] = booking;
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static ArrayList<CabTimeEstimates> parse_UberTimeEstimatesResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<CabTimeEstimates> uberCabs = new ArrayList<CabTimeEstimates>();
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray uberJsonArray = responseObject.getJSONArray("response");
			for(int i=0; i<uberJsonArray.length(); i++) {
				if(uberJsonArray.get(i) instanceof JSONObject) {
					JSONObject uberJson = uberJsonArray.getJSONObject(i);
					CabTimeEstimates uberCab = parse_UberTimeEstimatesObject(uberJson);
					uberCabs.add(uberCab);
				}
			}
		}
		return uberCabs;
	}

	public static ArrayList<IntercityCab> parse_IntercityCabResponse(InputStream is) throws JSONException {

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		ArrayList<IntercityCab> intercityCabs = new ArrayList<IntercityCab>();
		if (responseObject != null && responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
			JSONArray intercityJsonArray = responseObject.getJSONArray("response");
			for(int i=0; i<intercityJsonArray.length(); i++) {
				if(intercityJsonArray.get(i) instanceof JSONObject) {
					JSONObject intercityJson = intercityJsonArray.getJSONObject(i);
                    IntercityCab cab = parseIntercityResponse(intercityJson);
                    intercityCabs.add(cab);
				}
			}
		}
		return intercityCabs;
	}

	public static IntercityCab parseIntercityResponse(JSONObject intercityJSON)throws JSONException{
		IntercityCab cab = new IntercityCab();
		if(intercityJSON.has("type")){
			cab.setType(Integer.parseInt(String.valueOf(intercityJSON.get("type"))));
		}
		if(intercityJSON.has("subType")){
			cab.setSubType(Integer.parseInt(String.valueOf(intercityJSON.get("subType"))));
		}
		if(intercityJSON.has("logoUrl")){
			cab.setLogoUrl(String.valueOf(intercityJSON.get("logoUrl")));
		}
		if(intercityJSON.has("displayName")){
			cab.setDisplayName(String.valueOf(intercityJSON.get("displayName")));
		}
		if(intercityJSON.has("costPerDistance")){
			cab.setCostPerDistance(Double.valueOf(String.valueOf(intercityJSON.get("costPerDistance"))));
		}
		if(intercityJSON.has("basePrice")){
			cab.setBasePrice(Double.valueOf(String.valueOf(intercityJSON.get("basePrice"))));
		}
		if(intercityJSON.has("fare")){
			cab.setFare(Double.valueOf(String.valueOf(intercityJSON.get("fare"))));
		}
		if(intercityJSON.has("advance")){
			cab.setAdvance(Double.valueOf(String.valueOf(intercityJSON.get("advance"))));
		}
		if(intercityJSON.has("capacity")){
			cab.setCapacity(Integer.parseInt(String.valueOf(intercityJSON.get("capacity"))));
		}
		if(intercityJSON.has("structure")){
			cab.setStructure(Integer.parseInt(String.valueOf(intercityJSON.get("structure"))));
		}
		if(intercityJSON.has("modes")){
			cab.setModes(Integer.parseInt(String.valueOf(intercityJSON.get("modes"))));
		}
		if(intercityJSON.has("availability")){
			cab.setAvailability(Integer.parseInt(String.valueOf(intercityJSON.get("availability"))));
		}
		if(intercityJSON.has("cabImageUrl")){
			cab.setCabImageUrl(String.valueOf(intercityJSON.get("cabImageUrl")));
		}
		if(intercityJSON.has("cabType")){
			cab.setCabType(Integer.parseInt(String.valueOf(intercityJSON.get("cabType"))));
		}
        if(intercityJSON.has("terms")){
            cab.setTerms(String.valueOf(intercityJSON.get("terms")));
        }
		if(intercityJSON.has("bookingId")){
			cab.setBookingId(String.valueOf(intercityJSON.get("bookingId")));
		}
		return cab;
	}

	public static TaxiBookings parse_TaxiBookings(JSONObject object)throws JSONException {
		if(object == null)
			return null;

		TaxiBookings booking = new TaxiBookings();

		if (object.has("fromCity")) {
			booking.setFromCity(String.valueOf("fromCity"));
		}
		if (object.has("toCity")) {
			booking.setToCity(String.valueOf("toCity"));
		}
		if (object.has("status")) {
			booking.setStatus(Integer.parseInt(String.valueOf(object.get("status"))));
		}
		if (object.has("startDate")) {
			booking.setStartDate(Long.parseLong(String.valueOf(object.get("startDate"))));
		}
		if (object.has("returnDate")) {
			booking.setReturnDate(Long.parseLong(String.valueOf(object.get("returnDate"))));
		}
		if (object.has("bookingCode")) {
			booking.setBookingCode(String.valueOf(object.get("bookingCode")));
		}
		if (object.has("amount")) {
			booking.setAmount(Double.valueOf(String.valueOf(object.get("amount"))));
		}
		if (object.has("advance")) {
			booking.setAdvance(Double.valueOf(String.valueOf(object.get("advance"))));
		}
		if (object.has("type")) {
			booking.setType(Integer.parseInt(String.valueOf(object.get("type"))));
		}
		if (object.has("subType")) {
			booking.setSubType(Integer.parseInt(String.valueOf(object.get("subType"))));
		}
		if (object.has("paymentParam")) {
			booking.setPaymentParam(String.valueOf(object.get("paymentParam")));
		}
		if (object.has("paymentUrl")) {
			booking.setPaymentUrl(String.valueOf(object.get("paymentUrl")));
		}

		if (object.has("referenceId")) {
			booking.setReferenceId(String.valueOf(object.get("referenceId")));
		}

		if (object.has("hash")) {
			booking.setHash(String.valueOf(object.get("hash")));
		}

		return booking;

	}

	public static ArrayList<AppConfig> parse_AppConfig(InputStream is) throws JSONException {

		ArrayList<AppConfig> object = new ArrayList<AppConfig>();
		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);
		if (responseObject != null && responseObject.has("status")) {
			if (responseObject.getString("status").equals("success")) {
				if (responseObject.has("response") && responseObject.get("response") instanceof JSONArray) {
					JSONArray categoryArr = responseObject.getJSONArray("response");

					for (int i = 0; i < categoryArr.length(); i++) {
						JSONObject categoryJson = categoryArr.getJSONObject(i);
						AppConfig config = new AppConfig();
						if (categoryJson.has("key")) {
							config.setKey(String.valueOf(categoryJson.get("key")));
						}

						if (categoryJson.has("value")) {
							config.setValue(String.valueOf(categoryJson.get("value")));
						}
						object.add(config);
					}
				}
			}
		}
		return object;
	}

	public static Object[] parseFeedbackResponse(InputStream is) throws JSONException {

		Object[] output = new Object[]{"failed", "", null};

		JSONObject responseObject = ParserJson.convertInputStreamToJSON(is);

		if (responseObject != null && responseObject.has("status")) {
			output[0] = responseObject.getString("status");
			if (output[0].equals("success")) {
				if (responseObject.has("response")) {
					output[1] = responseObject.get("response");
				}
			} else {
				if (responseObject.has("errorMessage")) {
					output[1] = responseObject.getString("errorMessage");
				}
			}
		}
		return output;
	}

	public static CabDetails parse_CabDetailsObject(JSONObject uberJson) {
		if(uberJson == null)
			return null;

		CabDetails cabDetails = new CabDetails();
		try {


			if (uberJson.has("zapp_count") && uberJson.get("zapp_count") instanceof Integer) {
				cabDetails.setZapp_count(uberJson.getInt("zapp_count"));
			}

			if (uberJson.has("capacity") && uberJson.get("capacity") instanceof Integer) {
				cabDetails.setCapacity(uberJson.getInt("capacity"));
			}


			if(uberJson.has("waitingCostPerMinute")) {
				cabDetails.setWaitingCostPerMinute(uberJson.getDouble("waitingCostPerMinute"));
			}

			if(uberJson.has("ridingCostPerMinute")) {
				cabDetails.setRidingCostPerMinute(uberJson.getDouble("ridingCostPerMinute"));
			}



			if (uberJson.has("productId")) {
				cabDetails.setProductId(String.valueOf(uberJson.get("productId")));
			}

			if (uberJson.has("costPerDistance")) {
				cabDetails.setCostPerDistance(uberJson.getDouble("costPerDistance"));
			}

/*
			if (uberJson.has("surcharge") && uberJson.get("surcharge") instanceof JSONArray) {
				JSONArray surchargeArray = uberJson.getJSONArray("surcharge");
				if (surchargeArray != null && surchargeArray.length() > 0
						&& surchargeArray.get(0) instanceof JSONObject) {
					JSONObject surchargeJson = surchargeArray.getJSONObject(0);
					if (surchargeJson != null) {
						// if (surchargeJson.has("value") &&
						// surchargeJson.get("value") instanceof
						// Double)
						// CabDetailsObject.setSurge(surchargeJson.getDouble("value"));
						// else if (surchargeJson.has("value")
						// && surchargeJson.get("value") instanceof
						// Integer)
						// CabDetailsObject.setSurge(surchargeJson.getInt("value"));
						Surcharge sObject = new Surcharge();
						if(surchargeJson.has("name")){
							sObject.setName(surchargeJson.getString("name"));
						}
						if(surchargeJson.has("type")){
							sObject.setType(surchargeJson.getString("type"));
						}
						if(surchargeJson.has("description")){
							sObject.setDescription(surchargeJson.getString("description"));
						}
						if(surchargeJson.has("value")){
							sObject.setValue(surchargeJson.getDouble("value"));
						}

						cabDetails.setSurcharge(sObject);

					}
				}

			}

*/
            if(uberJson.has("surcharge") && uberJson.get("surcharge") instanceof JSONObject){
                JSONObject object = (JSONObject) uberJson.get("surcharge");
                Surcharge surcharge = new Surcharge();
                if(object.has("name")){
                    surcharge.setName(String.valueOf(object.get("name")));
                }
                if(object.has("value")){
                    surcharge.setValue(Double.parseDouble(String.valueOf(object.get("value"))));
                }

                cabDetails.setSurcharge(surcharge);

            }

			if (uberJson.has("logoUrl")) {
				cabDetails.setLogoUrl(String.valueOf(uberJson.get("logoUrl")));
			}

			if (uberJson.has("type") && uberJson.get("type") instanceof Integer) {
				cabDetails.setType(uberJson.getInt("type"));
			}


			if (uberJson.has("subType") && uberJson.get("subType") instanceof Integer) {
				cabDetails.setSubType(uberJson.getInt("subType"));
			}

			if (uberJson.has("priceEstimate")) {
				cabDetails.setPriceEstimate(String.valueOf(uberJson.get("priceEstimate")));
			}

			if (uberJson.has("estimatedTimeOfArrival") && uberJson.get("estimatedTimeOfArrival") instanceof Long) {
				cabDetails.setEstimatedTimeOfArrival(uberJson.getLong("estimatedTimeOfArrival")/60);
			} else if (uberJson.has("estimatedTimeOfArrival") && uberJson.get("estimatedTimeOfArrival") instanceof Integer) {
				cabDetails.setEstimatedTimeOfArrival(uberJson.getInt("estimatedTimeOfArrival")/60);
			}

			if (uberJson.has("timeEstimate")) {
				cabDetails.setTimeEstimate(uberJson.getString("timeEstimate"));
			}



			if (uberJson.has("displayName")) {
				cabDetails.setDisplayName(String.valueOf(uberJson.get("displayName")));
			}

			if (uberJson.has("logoUrl")) {
				cabDetails.setLogoUrl(String.valueOf(uberJson.get("logoUrl")));
			}

			if (uberJson.has("cancellationFee") && uberJson.get("cancellationFee") instanceof Double) {
				cabDetails.setCancellationFee(uberJson.getDouble("cancellationFee"));
			} else if (uberJson.has("cancellationFee") && uberJson.get("cancellationFee") instanceof Integer) {
				cabDetails.setCancellationFee(uberJson.getInt("cancellationFee"));
			}

			if (uberJson.has("base") && uberJson.get("base") instanceof Double) {
				cabDetails.setBase(uberJson.getDouble("base"));
			} else if (uberJson.has("base") && uberJson.get("base") instanceof Integer) {
				cabDetails.setBase(uberJson.getInt("base"));
			}

			if (uberJson.has("isRecommended") && uberJson.get("isRecommended") instanceof Integer) {
				cabDetails.setIsRecommended(uberJson.getInt("isRecommended"));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return cabDetails;
	}

	public static Voucher parse_voucherObject(JSONObject voucherJson) {
		if(voucherJson == null)
			return null;

		Voucher voucher = new Voucher();
		try {
			if (voucherJson.has("image_url") && voucherJson.get("image_url") instanceof String) {
				voucher.setImage_url(voucherJson.getString("image_url"));
			}
			if (voucherJson.has("company_name") && voucherJson.get("company_name") instanceof String) {
				voucher.setComapany_name(voucherJson.getString("company_name"));
			}

			if (voucherJson.has("terms") && voucherJson.get("terms") instanceof String) {
				voucher.setTerms(voucherJson.getString("terms"));
			}



		} catch(JSONException e) {
			e.printStackTrace();
		}
		return voucher;
	}


	public static ConnectedAccount parse_ConnectedAccountObject(JSONObject uberJson) {
		if(uberJson == null)
			return null;

		ConnectedAccount connectedAccount = new ConnectedAccount();
		try {

			if(uberJson.has("type") && uberJson.get("type") instanceof  Integer) {
				connectedAccount.setCabCompany(uberJson.getInt("type"));
			}

			if(uberJson.has("cabSessionId") && uberJson.get("cabSessionId") instanceof  Integer) {
				connectedAccount.setCabSessionId(uberJson.getInt("cabSessionId"));
			}
			if(uberJson.has("accessToken")) {
				connectedAccount.setAccessToken(String.valueOf(uberJson.get("accessToken")));
			}

			if(uberJson.has("cabCompanyName")) {
				connectedAccount.setCabCompanyName(String.valueOf(uberJson.get("cabCompanyName")));
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
		return connectedAccount;
	}

	public static CabTimeEstimates parse_UberTimeEstimatesObject(JSONObject uberJson) {
		if(uberJson == null)
			return null;

		CabTimeEstimates uberDetailsObject = new CabTimeEstimates();
		try {

			if (uberJson.has("product_id")) {
				uberDetailsObject.setProductId(String.valueOf(uberJson.get("product_id")));
			}

			if (uberJson.has("display_name")) {
				uberDetailsObject.setDisplayName(String.valueOf(uberJson.get("display_name")));
			}

			if (uberJson.has("estimate") && uberJson.get("estimate") instanceof Long) {
				uberDetailsObject.setEstimate(uberJson.getLong("estimate"));
			} else if (uberJson.has("estimate") && uberJson.get("estimate") instanceof Integer) {
				uberDetailsObject.setEstimate(uberJson.getInt("estimate"));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return uberDetailsObject;
	}

	public static CabBooking parse_CabBookingObject(JSONObject cabJson) {
		if(cabJson == null)
			return null;

		CabBooking cabBookingDetails = new CabBooking();
		try {

			if (cabJson.has("bookingId") && cabJson.get("bookingId") instanceof Integer) {
				cabBookingDetails.setBookingId(cabJson.getInt("bookingId"));
			}

			if (cabJson.has("crn")) {
				cabBookingDetails.setCrn(String.valueOf(cabJson.get("crn")));
			}

			if (cabJson.has("callId")) {
				cabBookingDetails.setCallId(String.valueOf(cabJson.get("callId")));
			}

			if (cabJson.has("driver_name")) {
				cabBookingDetails.setDriverName(String.valueOf(cabJson.get("driver_name")));
			}

			if (cabJson.has("driver_number")) {
				cabBookingDetails.setDriverNumber(String.valueOf(cabJson.get("driver_number")));
			}

			if (cabJson.has("type") && cabJson.get("type") instanceof Integer) {
				cabBookingDetails.setType(cabJson.getInt("type"));
			}

			if (cabJson.has("cab_model")) {
				cabBookingDetails.setCabModel(String.valueOf(cabJson.get("cab_model")));
			}

			if (cabJson.has("cab_number")) {
				cabBookingDetails.setCabNumber(String.valueOf(cabJson.get("cab_number")));
			}

			if (cabJson.has("cab_type") && cabJson.get("cab_type") instanceof Integer) {
				cabBookingDetails.setCabType(cabJson.getInt("cab_type")+"");
			}

			if (cabJson.has("eta") && cabJson.get("eta") instanceof Long) {
				cabBookingDetails.setEta(cabJson.getLong("eta"));
			} else if (cabJson.has("eta") && cabJson.get("eta") instanceof Integer) {
				cabBookingDetails.setEta(cabJson.getInt("eta"));
			}

			if (cabJson.has("driver_lat") && cabJson.get("driver_lat") instanceof Double) {
				cabBookingDetails.setDriverLatitude(cabJson.getDouble("driver_lat"));
			}

			if (cabJson.has("driver_lng") && cabJson.get("driver_lng") instanceof Double) {
				cabBookingDetails.setDriverLongitude(cabJson.getDouble("driver_lng"));
			}

			if (cabJson.has("created") && cabJson.get("created") instanceof Long) {
				cabBookingDetails.setCreated(cabJson.getLong("created"));
			} else if (cabJson.has("created") && cabJson.get("created") instanceof Integer) {
				cabBookingDetails.setCreated(cabJson.getInt("created"));
			}

			if (cabJson.has("status") && cabJson.get("status") instanceof Integer) {
				cabBookingDetails.setStatus(cabJson.getInt("status"));
			}

			if (cabJson.has("pickupLatitude") && cabJson.get("pickupLatitude") instanceof Double) {
				cabBookingDetails.setPickupLatitude(cabJson.getDouble("pickupLatitude"));
			}

			if (cabJson.has("pickupLongitude") && cabJson.get("pickupLongitude") instanceof Double) {
				cabBookingDetails.setPickupLongitude(cabJson.getDouble("pickupLongitude"));
			}

			if (cabJson.has("dropLatitude") && cabJson.get("dropLatitude") instanceof Double) {
				cabBookingDetails.setDropLatitude(cabJson.getDouble("dropLatitude"));
			}

			if (cabJson.has("dropLongitude") && cabJson.get("dropLongitude") instanceof Double) {
				cabBookingDetails.setDropLongitude(cabJson.getDouble("dropLongitude"));
			}

			if (cabJson.has("amount") && cabJson.get("amount") instanceof Double) {
				cabBookingDetails.setAmount(cabJson.getDouble("amount"));
			} else if (cabJson.has("amount") && cabJson.get("amount") instanceof Integer) {
				cabBookingDetails.setAmount(cabJson.getInt("amount"));
			}

			if (cabJson.has("paymentMode") && cabJson.get("paymentMode") instanceof Integer) {
				cabBookingDetails.setPaymentMode(cabJson.getInt("paymentMode"));
			}

			if (cabJson.has("accessToken")) {
				cabBookingDetails.setAccessToken(String.valueOf(cabJson.get("accessToken")));
			}

			if (cabJson.has("refreshToken")) {
				cabBookingDetails.setRefreshToken(String.valueOf(cabJson.get("refreshToken")));
			}

			if (cabJson.has("deletionTime") && cabJson.get("deletionTime") instanceof Long) {
				cabBookingDetails.setDeletionTime(cabJson.getLong("deletionTime"));
			} else if (cabJson.has("deletionTime") && cabJson.get("deletionTime") instanceof Integer) {
				cabBookingDetails.setDeletionTime(cabJson.getInt("deletionTime"));
			}

			if (cabJson.has("pickup_address")) {
				cabBookingDetails.setPickupAddress(String.valueOf(cabJson.get("pickup_address")));
			}

			if (cabJson.has("drop_address")) {
				cabBookingDetails.setDropAddress(String.valueOf(cabJson.get("drop_address")));
			}

			if (cabJson.has("keyUrl")) {
				cabBookingDetails.setShareUrl(String.valueOf(cabJson.get("keyUrl")));
			}
			if (cabJson.has("hash")){
				cabBookingDetails.setHash((String.valueOf(cabJson.get("hash"))));
			}

			if (cabJson.has("cashback") && cabJson.get("cashback") instanceof Integer) {
				cabBookingDetails.setCashback(cabJson.getInt("cashback"));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return cabBookingDetails;
	}

	public static Tracking parse_CabTrackingObject(JSONObject cabJson) {
		if(cabJson == null)
			return null;

		Tracking tracking = new Tracking();
		try {

			if(cabJson.has("advance") && cabJson.get("advance") instanceof Double) {
				tracking.setAdvance(cabJson.getDouble("advance"));
			} else if(cabJson.has("advance") && cabJson.get("advance") instanceof Integer) {
				tracking.setAdvance(cabJson.getInt("advance"));
			}

			if(cabJson.has("amount") && cabJson.get("amount") instanceof Double) {
				tracking.setAmount(cabJson.getDouble("amount"));
			} else if(cabJson.has("amount") && cabJson.get("amount") instanceof Integer) {
				tracking.setAmount(cabJson.getInt("amount"));
			}

			if(cabJson.has("booking_status") && cabJson.get("booking_status") instanceof Integer) {
				tracking.setBookingStatus(cabJson.getInt("booking_status"));
			}

			if(cabJson.has("discount") && cabJson.get("discount") instanceof Double) {
				tracking.setDiscount(cabJson.getDouble("discount"));
			} else if(cabJson.has("discount") && cabJson.get("discount") instanceof Integer) {
				tracking.setDiscount(cabJson.getInt("discount"));
			}

			if(cabJson.has("distance_value") && cabJson.get("distance_value") instanceof Double) {
				tracking.setDistance_value(cabJson.getDouble("distance_value"));
			} else if(cabJson.has("distance_value") && cabJson.get("distance_value") instanceof Integer) {
				tracking.setDistance_value(cabJson.getInt("distance_value"));
			}

			if(cabJson.has("driver_latitude") && cabJson.get("driver_latitude") instanceof Double) {
				tracking.setDriverLatitude(cabJson.getDouble("driver_latitude"));
			}

			if(cabJson.has("driver_longitude") && cabJson.get("driver_longitude") instanceof Double) {
				tracking.setDriverLongitude(cabJson.getDouble("driver_longitude"));
			}

			if(cabJson.has("money_balance") && cabJson.get("money_balance") instanceof Double) {
				tracking.setMoneyBalance(cabJson.getDouble("money_balance"));
			} else if(cabJson.has("money_balance") && cabJson.get("money_balance") instanceof Integer) {
				tracking.setMoneyBalance(cabJson.getInt("money_balance"));
			}

			if(cabJson.has("payable_amount") && cabJson.get("payable_amount") instanceof Double) {
				tracking.setPayableAmount(cabJson.getDouble("payable_amount"));
			} else if(cabJson.has("payable_amount") && cabJson.get("payable_amount") instanceof Integer) {
				tracking.setPayableAmount(cabJson.getInt("payable_amount"));
			}

			if(cabJson.has("timestamp") && cabJson.get("timestamp") instanceof Long) {
				tracking.setTimestamp(cabJson.getLong("timestamp"));
			} else if(cabJson.has("timestamp") && cabJson.get("timestamp") instanceof Integer) {
				tracking.setTimestamp(cabJson.getInt("timestamp"));
			}

			if(cabJson.has("token_deletion_time") && cabJson.get("token_deletion_time") instanceof Long) {
				tracking.setTokenDeletionTime(cabJson.getLong("token_deletion_time"));
			} else if(cabJson.has("token_deletion_time") && cabJson.get("token_deletion_time") instanceof Integer) {
				tracking.setTokenDeletionTime(cabJson.getInt("token_deletion_time"));
			}

			if(cabJson.has("tracking_id") && cabJson.get("tracking_id") instanceof Integer) {
				tracking.setTrackingId(cabJson.getInt("tracking_id"));
			}

			if(cabJson.has("wait_time_value") && cabJson.get("wait_time_value") instanceof Double) {
				tracking.setWait_time_value(cabJson.getDouble("wait_time_value"));
			} else if(cabJson.has("wait_time_value") && cabJson.get("wait_time_value") instanceof Integer) {
				tracking.setWait_time_value(cabJson.getInt("wait_time_value"));
			}

			if(cabJson.has("crn")) {
				tracking.setCrn(String.valueOf(cabJson.get("crn")));
			}

			if(cabJson.has("distance_unit")) {
				tracking.setDistance_unit(String.valueOf(cabJson.get("distance_unit")));
			}

			if(cabJson.has("status")) {
				tracking.setStatus(String.valueOf(cabJson.get("status")));
			}

			if(cabJson.has("token")) {
				tracking.setToken(String.valueOf(cabJson.get("token")));
			}

			if(cabJson.has("wait_time_unit")) {
				tracking.setWait_time_unit(String.valueOf(cabJson.get("wait_time_unit")));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return tracking;
	}

	public static Voucher parse_VoucherObject(JSONObject cabJson) {
		if(cabJson == null)
			return null;

		Voucher voucher = new Voucher();
		try {

			if (cabJson.has("voucher_id") && cabJson.get("voucher_id") instanceof Integer) {
				voucher.setVoucherId(cabJson.getInt("voucher_id"));
			}

			if (cabJson.has("is_valid") && cabJson.get("is_valid") instanceof Boolean) {
				voucher.setIsValid(cabJson.getBoolean("is_valid"));
			}

			if (cabJson.has("image_url")) {
				voucher.setImage_url(String.valueOf(cabJson.get("image_url")));
			}

			if (cabJson.has("company_name")) {
				voucher.setComapany_name(String.valueOf(cabJson.get("company_name")));
			}

			if (cabJson.has("terms")) {
				voucher.setTerms(String.valueOf(cabJson.get("terms")));
			}

			if (cabJson.has("value") && cabJson.get("value") instanceof Double) {
				voucher.setValue(cabJson.getDouble("value"));
			} else if (cabJson.has("value") && cabJson.get("value") instanceof Integer) {
				voucher.setValue(cabJson.getInt("value"));
			}

			if (cabJson.has("zapps_required") && cabJson.get("zapps_required") instanceof Integer) {
				voucher.setZappsRequired(cabJson.getInt("zapps_required"));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return voucher;
	}

	public static Address parse_AddressObject(JSONObject cabJson) {
		if(cabJson == null)
			return null;

		Address voucher = new Address();
		try {

			if (cabJson.has("latitude") && cabJson.get("latitude") instanceof Double) {
				voucher.setAddressLatitude(cabJson.getDouble("latitude"));
			}

			if (cabJson.has("longitude") && cabJson.get("longitude") instanceof Double) {
				voucher.setAddressLongitude(cabJson.getDouble("longitude"));
			}

			if (cabJson.has("address")) {
				voucher.setAddress(String.valueOf(cabJson.get("address")));
			}

			if (cabJson.has("type") && cabJson.get("type") instanceof Integer) {
				voucher.setAddressType(cabJson.getInt("type"));
			}

		} catch(JSONException e) {
			e.printStackTrace();
		}
		return voucher;
	}

}
