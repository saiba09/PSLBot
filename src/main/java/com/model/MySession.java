package com.model;

public class MySession {
	String userName;
	String accessToken ;
	
	public MySession(String userName, String accessToken) {
		this.userName = userName;
		this.accessToken = accessToken;
	}

	public String getUserName() {
		return userName;
	}

	public String getAccessToken() {
		return accessToken;
	}

	
}
