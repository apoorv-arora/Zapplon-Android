package com.application.zapplon.utils.location;

import android.location.Location;

public interface ZLocationCallback {
	public void onCoordinatesIdentified(Location loc);
	public void onLocationIdentified();
	public void onLocationNotIdentified();
	public void onDifferentCityIdentified();
	public void locationNotEnabled();
	public void onLocationTimedOut();
	public void onNetworkError();
}
