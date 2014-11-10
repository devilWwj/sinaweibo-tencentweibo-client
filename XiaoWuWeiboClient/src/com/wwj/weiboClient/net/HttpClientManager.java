package com.wwj.weiboClient.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * 对客户端Http请求的网络的管理
 * 
 * @author wwj
 * 
 */
public class HttpClientManager {

	private Map<String, String> heads = new HashMap<String, String>();
	private Map<String, String> cookies = new HashMap<String, String>();

	public HttpClientManager() {

	}

	public byte[] httpGet(String url) {
		HttpRequest httpRequest = new HttpRequest(HttpUtils.METHOD_GET, url,
				10000);
		httpRequest.initRequest();
		httpRequest.connect();
		return httpRequest.getResponseData();
	}

	public byte[] httpGet2(String url) {
		HttpURLConnection urlCon = null;
		try {
			urlCon = (HttpURLConnection) (new URL(url)).openConnection();
			urlCon.setConnectTimeout(15 * 1000);
			urlCon.setReadTimeout(15 * 1000);
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("GET");
			urlCon.setUseCaches(false);
			urlCon.setDoInput(true);
			if (cookies != null) {
				String cookieStr = "";
				Set<String> keys = cookies.keySet();
				for (Iterator<String> it = keys.iterator(); it.hasNext();) {
					String key = it.next();
					cookieStr += " " + key + "=" + cookies.get(key) + ";";
				}
				if (cookieStr.length() > 0) {
					urlCon.addRequestProperty("Cookie", cookieStr);
				}
			}
			urlCon.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			if (heads != null) {
				Set<String> keys = heads.keySet();
				for (Iterator<String> it = keys.iterator(); it.hasNext();) {
					String key = it.next();
					urlCon.setRequestProperty(key, heads.get(key));
				}
			}

			InputStream input = urlCon.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int count = -1;

			while ((count = input.read(buffer, 0, 1024)) != -1) {
				baos.write(buffer, 0, count);
			}
			Map<String, List<String>> fileds = urlCon.getHeaderFields();
			Set<String> keys = fileds.keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				List<String> values = fileds.get(key);
				for (int i = 0; i < values.size(); i++) {
					String value = values.get(i);
					if ("Set-Cookie".equals(key)) {
						String nameValueStr = value.split(";")[0];
						String[] nameValue = nameValueStr.split("=");
						cookies.put(nameValue[0], nameValue[1]);
					}
				}
			}
			return baos.toByteArray();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] httpPost2(String url, byte[] postContent) {
		HttpURLConnection urlCon = null;
		try {
			urlCon = (HttpURLConnection) (new URL(url)).openConnection();
			urlCon.setConnectTimeout(15 * 1000);
			urlCon.setReadTimeout(15 * 1000);
			urlCon.setUseCaches(false);
			urlCon.setDoInput(true);
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");

			urlCon.getOutputStream().write(postContent, 0, postContent.length);
			urlCon.getOutputStream().flush();
			urlCon.getOutputStream().close();

			InputStream input = urlCon.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];

			int count = -1;
			while ((count = input.read(buffer, 0, 1024)) != -1) {
				baos.write(buffer, 0, count);
			}
			Map<String, List<String>> fileds = urlCon.getHeaderFields();
			Set<String> keys = fileds.keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				List<String> values = fileds.get(key);
				for (int i = 0; i < values.size(); i++) {
					String value = values.get(i);
					if ("Set-Cookie".equals(key)) {
						String nameValueStr = value.split(";")[0];
						String[] nameValue = nameValueStr.split("=");
						cookies.put(nameValue[0], nameValue[1]);
					}
				}
			}
			return baos.toByteArray();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] httpPost(String url, byte[] postContent) {
		HttpURLConnection urlCon = null;
		try {
			urlCon = (HttpURLConnection) (new URL(url)).openConnection();
			urlCon.setConnectTimeout(15 * 1000);
			urlCon.setReadTimeout(15 * 1000);
			urlCon.setUseCaches(false);
			urlCon.setDoInput(true);
			urlCon.setDoOutput(true);
			urlCon.setRequestMethod("POST");
			HttpsURLConnection.setFollowRedirects(true);

			if (cookies != null) {
				String cookieStr = "";
				Set<String> keys = cookies.keySet();
				for (Iterator<String> it = keys.iterator(); it.hasNext();) {
					String key = it.next();
					cookieStr += " " + key + "=" + cookies.get(key) + ";";
				}
				if (cookieStr.length() > 0) {
					urlCon.addRequestProperty("Cookie", cookieStr);
				}
			}
			urlCon.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			urlCon.setRequestProperty("Content-Length", "" + postContent.length);
			urlCon.setRequestProperty("Connection", "Keep-Alive"); // 维持长连接
			Set<String> keySet = heads.keySet();
			for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
				String key = it.next();
				String value = heads.get(key);
				urlCon.setRequestProperty(key, value);
			}

			Map<String, List<String>> refileds = urlCon.getRequestProperties();
			Set<String> rekeys = refileds.keySet();

			for (Iterator<String> it = rekeys.iterator(); it.hasNext();) {
				String key = it.next();
				List<String> values = refileds.get(key);
				for (int i = 0; i < values.size(); i++) {
					String value = values.get(i);
					if ("Set-Cookie".equals(key)) {
						String nameValueStr = value.split(";")[0];
						String[] nameValue = nameValueStr.split("=");
						cookies.put(nameValue[0], nameValue[1]);
					}
				}
			}

			urlCon.getOutputStream().write(postContent, 0, postContent.length);
			urlCon.getOutputStream().flush();
			urlCon.getOutputStream().close();
			InputStream input = urlCon.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count = -1;
			while ((count = input.read(buffer, 0, 1024)) != -1) {
				baos.write(buffer, 0, count);
			}
			Map<String, List<String>> fileds = urlCon.getHeaderFields();
			Set<String> keys = fileds.keySet();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String key = it.next();
				List<String> values = fileds.get(key);
				for (int i = 0; i < values.size(); i++) {
					String value = values.get(i);
					if ("Set-Cookies".equals(key)) {
						String nameValueStr = value.split(";")[0];
						String[] nameValue = nameValueStr.split("=");
						cookies.put(nameValue[0], nameValue[1]);
					}
				}
			}
			return baos.toByteArray();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addHead(String name, String value) {
		if (heads == null) {
			heads = new HashMap<String, String>();
		}
		heads.put(name, value);
	}

	public Map<String, String> getCookies() {
		return cookies;
	}
}
