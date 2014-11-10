package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.AsyncWeiboRunner;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.Utility;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.IWeiboClientListener;
import com.wwj.weiboClient.listener.impl.SinaUserRequestListener;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.SinaConstants;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.DialogHelp;
import com.wwj.weiboClient.util.MyDebug;
import com.wwj.weiboClient.util.StringUtil;

/**
 * �Զ���������Ȩ����
 * 
 * @author wwj
 * 
 */
public class WebViewActivity extends Activity implements IWeiboClientListener {
	private final String TAG = "WebViewActivity";

	private WebView mWebView;

	private View progressBar;

	private WeiboWebViewClient mWeiboWebViewClient;

	private static final String WEIBO_APP_SECRET = "b8545e943ea7d86aabced0e5d89541b0";

	/** ͨ�� code ��ȡ Token �� URL */
	private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";

	/** ��ȡ���� Code */
	private String mCode;
	/** ��ȡ���� Token */
	private Oauth2AccessToken mAccessToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_layout);

		initView();
		initData();

		MyDebug.print("onCreate", "MainThread().getId() = "
				+ Thread.currentThread().getId());
	}

	// ��ʼ����ͼ
	public void initView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.requestFocus();

		// �õ�Web����
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true); // ����֧��JavaScript����
		webSettings.setBuiltInZoomControls(true); // �������õķŴ����
		webSettings.setSupportZoom(true); // ����֧������
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // ���û���ģʽ

		// Բ�ν�����
		progressBar = findViewById(R.id.show_request_progress_bar);
	}

	// ��ʼ������
	public void initData() {
		mWeiboWebViewClient = new WeiboWebViewClient();
		mWebView.setWebViewClient(mWeiboWebViewClient);

		// ���������Ķ��󴴽�һ��Cookieͬ������������
		CookieSyncManager.createInstance(this);

		// ����Url��ַ
		mWebView.loadUrl(SinaWeiboManager.getAuthUrl(this));

	}

	// ��ʾԲ�ν�����
	private void showProgress() {
		// ��UI�߳�����ʾ
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// ��ʾ
				progressBar.setVisibility(View.VISIBLE);
			}
		});

	}

	// ���ؽ�����
	private void hideProgress() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// ���ÿ���Ϊ����
				progressBar.setVisibility(View.INVISIBLE);
			}
		});

	}

	@Override
	public void onCancel() {
		// ȡ����֤
		Toast.makeText(this, "Auth cancel", Toast.LENGTH_SHORT).show();
	}

	// �����֤�ص�����
	@Override
	public void onComplete(Bundle values) {
		// ��ʼͬ��
		CookieSyncManager.getInstance().sync();
		// ��Bundle�н���Token
		mAccessToken = Oauth2AccessToken.parseAccessToken(values);
		if (mAccessToken.isSessionValid()) {
			// ����Token��SharedPreferences
			AccessTokenKeeper.writeAccessToken(this, mAccessToken);
			Toast.makeText(this, R.string.weibosdk_toast_auth_success,
					Toast.LENGTH_LONG).show();
			// �����Ñ���UID
			long uid = Long.valueOf(mAccessToken.getUid());
			StorageManager.setValue(this, StringUtil.SINA_UID, uid);
			// ����һ��user���󱣴浽���ݿ�
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(String.valueOf(uid));
			userInfo.setToken(mAccessToken.getToken());
			userInfo.setTokenSecret("");
			userInfo.setExpires_in(String.valueOf(mAccessToken.getExpiresTime()));
			SinaWeiboManager.getUserInfo(this, uid,
					new SinaUserRequestListener(this, userInfo));

			boolean hasLogin = StorageManager.getValue(this,
					StringUtil.HAS_LOGIN, false);
			if (!hasLogin) {
//				DialogHelp.getInstance().showHttpDialog(this, "", "���ڵ�¼...");
			}
		} else {
			// ����ע���Ӧ�ó���ǩ������ȷʱ���ͻ��յ� Code����ȷ��ǩ����ȷ
			String code = values.getString("code");
			String message = this
					.getString(R.string.weibosdk_toast_auth_failed);
			if (!TextUtils.isEmpty(code)) {
				message = message + "\nObtained the code: " + code;
			}
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}

		// String access_token = values.getString("access_token");
		// String expires_in = values.getString("expires_in");
		// String remind_in = values.getString("remind_in");
		// String uid = values.getString("uid");
		//
		// MyDebug.print("onComplete", "access_token = " + access_token
		// + "\nexpires_in = " + expires_in);

		setResult(RESULT_OK);
		finish();
	}

	// �����쳣ʱ�ص��˷���
	@Override
	public void onWeiboException(WeiboException e) {
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * �Զ���WeiViewClient
	 * 
	 */
	private class WeiboWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			MyDebug.print("WeiboWebViewClient",
					"shouldOverrideUrlLoading url = " + url);
			showProgress();
			view.loadUrl(url);
			return super.shouldOverrideUrlLoading(view, url);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {

			MyDebug.printErr("WeiboWebViewClient",
					"onReceivedError failingUrl = " + failingUrl);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		/**
		 * ҳ�濪ʼ
		 */
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {

			MyDebug.print("WeiboWebViewClient", "onPageStarted url = " + url
					+ "\nthreadid = " + Thread.currentThread().getId());

			showProgress();
			if (url.startsWith(SinaConstants.REDIRECT_URL)) {
				handleRedirectUrl(view, url, WebViewActivity.this);
				view.stopLoading();
				return;
			}

			super.onPageStarted(view, url, favicon);

		}

		// ҳ�����ʱ
		@Override
		public void onPageFinished(WebView view, String url) {
			MyDebug.print("WeiboWebViewClient", "onPageFinished url = " + url);
			hideProgress();
			super.onPageFinished(view, url);
		}

		private boolean handleRedirectUrl(WebView view, String url,
				IWeiboClientListener listener) {
			Bundle values = Utility.parseUrl(url);
			String error = values.getString("error");
			String error_code = values.getString("error_code");

			MyDebug.print("handleRedirectUrl", "error = " + error
					+ "\n error_code = " + error_code);
			if (error == null && error_code == null) {
				listener.onComplete(values);
			} else if (error.equals("access_denied")) {
				listener.onCancel();
			} else {
				WeiboException weiboException = new WeiboException(error);
				listener.onWeiboException(weiboException);
			}

			return false;
		}
	}

	/**
	 * �첽��ȡ Token��
	 * 
	 * @param authCode
	 *            ��Ȩ Code���� Code ��һ���Եģ�ֻ�ܱ���ȡһ�� Token
	 * @param appSecret
	 *            Ӧ�ó���� APP_SECRET����������Ʊ��ܺ��Լ��� APP_SECRET��
	 *            ��Ҫֱ�ӱ�¶�ڳ����У��˴�����Ϊһ��DEMO����ʾ��
	 */
	public void fetchTokenAsync(String authCode, String appSecret) {
		/*
		 * LinkedHashMap<String, String> requestParams = new
		 * LinkedHashMap<String, String>();
		 * requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_ID,
		 * Constants.APP_KEY);
		 * requestParams.put(WBConstants.AUTH_PARAMS_CLIENT_SECRET,
		 * appSecretConstantS.APP_SECRET);
		 * requestParams.put(WBConstants.AUTH_PARAMS_GRANT_TYPE,
		 * "authorization_code");
		 * requestParams.put(WBConstants.AUTH_PARAMS_CODE, authCode);
		 * requestParams.put(WBConstants.AUTH_PARAMS_REDIRECT_URL,
		 * Constants.REDIRECT_URL);
		 */
		WeiboParameters requestParams = new WeiboParameters();
		requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_ID,
				SinaConstants.APP_KEY);
		requestParams.add(WBConstants.AUTH_PARAMS_CLIENT_SECRET, appSecret);
		requestParams.add(WBConstants.AUTH_PARAMS_GRANT_TYPE,
				"authorization_code");
		requestParams.add(WBConstants.AUTH_PARAMS_CODE, authCode);
		requestParams.add(WBConstants.AUTH_PARAMS_REDIRECT_URL,
				SinaConstants.REDIRECT_URL);

		/**
		 * ��ע�⣺ {@link RequestListener} ��Ӧ�Ļص��������ں�̨�߳��еģ� ��ˣ���Ҫʹ�� Handler ����ϸ���
		 * UI��
		 */
		AsyncWeiboRunner.request(OAUTH2_ACCESS_TOKEN_URL, requestParams,
				"POST", new RequestListener() {
					@Override
					public void onComplete(String response) {
						LogUtil.d(TAG, "Response: " + response);

						// ��ȡ Token �ɹ�
						Oauth2AccessToken token = Oauth2AccessToken
								.parseAccessToken(response);
						if (token != null && token.isSessionValid()) {
							LogUtil.d(TAG, "Success! " + token.toString());

							mAccessToken = token;
						} else {
							LogUtil.d(TAG, "Failed to receive access token");
						}
					}

					@Override
					public void onComplete4binary(
							ByteArrayOutputStream responseOS) {
						LogUtil.e(TAG, "onComplete4binary...");
					}

					@Override
					public void onIOException(IOException e) {
						LogUtil.e(TAG, "onIOException�� " + e.getMessage());
					}

					@Override
					public void onError(WeiboException e) {
						LogUtil.e(TAG, "WeiboException�� " + e.getMessage());
					}
				});
	}

}
