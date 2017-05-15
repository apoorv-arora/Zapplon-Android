package com.application.zapplon.data;

import java.io.Serializable;

public class Address implements Serializable {

	private int addressId;
	private int userId;
	private String address;
	private int addressType;
	private double addressLatitude;
	private double addressLongitude;
	private long created;
	private long modified;

	public Address() {

	}

	public int getAddressId() {
		return addressId;
	}

	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getAddressType() {
		return addressType;
	}

	public void setAddressType(int addressType) {
		this.addressType = addressType;
	}

	public double getAddressLatitude() {
		return addressLatitude;
	}

	public void setAddressLatitude(double addressLatitude) {
		this.addressLatitude = addressLatitude;
	}

	public double getAddressLongitude() {
		return addressLongitude;
	}

	public void setAddressLongitude(double addressLongitude) {
		this.addressLongitude = addressLongitude;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}

}
