package com.wwj.weiboClient.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class HttpRequest {
	private String requestMethod;
	private HttpURLConnection httpURLConnection;
	private URL url;
	private int delayTime;

	public HttpRequest(String requestMethod, String urlStr, int delayTime) {
		this.requestMethod = requestMethod;
		this.delayTime = delayTime;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		initConnection();
	}

	private void initConnection() {
		try {
			httpURLConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setExternalRequestProperty(String keyword, String value) {
		try {
			httpURLConnection.setRequestProperty(keyword, value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			httpURLConnection.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getResponseCode() {
		try {
			return httpURLConnection.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public HttpURLConnection getConnection() {
		return httpURLConnection;
	}

	public void setRequestMethod() {
		try {
			httpURLConnection.setRequestMethod(requestMethod);
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
	}

	public void setConnectTimeout() {
		httpURLConnection.setConnectTimeout(delayTime);
	}

	public void setBaseRequestProperty() {
		try {
			httpURLConnection.setRequestProperty(HttpUtils.PROPERTY_KEY_01,
					HttpUtils.PROPERTY_VALUE_01_A);
			httpURLConnection.setRequestProperty(HttpUtils.PROPERTY_KEY_02,
					HttpUtils.PROPERTY_VALUE_02_A);
			httpURLConnection.setRequestProperty(HttpUtils.PROPERTY_KEY_03,
					HttpUtils.PROPERTY_VALUE_03_A);
			httpURLConnection.setRequestProperty(HttpUtils.PROPERTY_KEY_04,
					HttpUtils.PROPERTY_VALUE_04_A);
			httpURLConnection.setRequestProperty(HttpUtils.PROPERTY_KEY_05,
					HttpUtils.PROPERTY_VALUE_05_A);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getResponseString() {
		String str = "";
		try {
			InputStream is = null;
			byte[] buffer = new byte[HttpUtils.SIZE_READ_BUFFER];
			int offset = 0;
			is = httpURLConnection.getInputStream();
			if (is != null) {
				while ((offset = is.read(buffer, 0, HttpUtils.SIZE_READ_BUFFER)) != -1) {
					str += new String(buffer, 0, offset);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	public byte[] getResponseData() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			InputStream is = null;
			byte[] buffer = new byte[HttpUtils.SIZE_READ_BUFFER];
			int offset = 0;
			is = httpURLConnection.getInputStream();
			if (is != null) {
				while ((offset = is.read(buffer, 0, HttpUtils.SIZE_READ_BUFFER)) != -1) {
					baos.write(buffer, 0, offset);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public InputStream getResponseStream() {
		InputStream is = null;
		try {
			is = httpURLConnection.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}
	
	public void initRequest() {
		setRequestMethod();
		setConnectTimeout();
		setBaseRequestProperty();
	}
}
