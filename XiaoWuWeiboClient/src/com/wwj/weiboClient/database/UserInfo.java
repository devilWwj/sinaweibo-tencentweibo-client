package com.wwj.weiboClient.database;

import java.io.Serializable;

import android.graphics.drawable.Drawable;

public class UserInfo implements Serializable {

	public static final String ID = "_id";
	public static final String USERID = "userId";
	public static final String TOKEN = "token";
	public static final String TOKENSECRET = "tokenSecret";
	public static final String EXPIRES_IN = "expires_in";
	public static final String USERNAME = "userName";
	public static final String USERICON = "userIcon";
	public static final String URL = "imageUrl";
	public static final String TYPE = "type";

	private String id;
	private String userId;// ”√ªßid
	private String token;
	private String tokenSecret;
	private String expires_in;
	private String userName;
	private Drawable userIcon;
	private String url;
	private int type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Drawable getUserIcon() {
		return userIcon;
	}

	public void setUserIcon(Drawable userIcon) {
		this.userIcon = userIcon;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
