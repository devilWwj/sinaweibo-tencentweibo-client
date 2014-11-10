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
 * 自定义新浪授权界面
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

	/** 通过 code 获取 Token 的 URL */
	private static final String OAUTH2_ACCESS_TOKEN_URL = "https://open.weibo.cn/oauth2/access_token";

	/** 获取到的 Code */
	private String mCode;
	/** 获取到的 Token */
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

	// 初始化视图
	public void initView() {
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.requestFocus();

		// 得到Web设置
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true); // 设置支持JavaScript代码
		webSettings.setBuiltInZoomControls(true); // 设置内置的放大控制
		webSettings.setSupportZoom(true); // 设置支持缩放
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 设置缓存模式

		// 圆形进度条
		progressBar = findViewById(R.id.show_request_progress_bar);
	}

	// 初始化数据
	public void initData() {
		mWeiboWebViewClient = new WeiboWebViewClient();
		mWebView.setWebViewClient(mWeiboWebViewClient);

		// 利用上下文对象创建一个Cookie同步管理单例对象
		CookieSyncManager.createInstance(this);

		// 加载Url地址
		mWebView.loadUrl(SinaWeiboManager.getAuthUrl(this));

	}

	// 显示圆形进度条
	private void showProgress() {
		// 在UI线程中显示
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// 显示
				progressBar.setVisibility(View.VISIBLE);
			}
		});

	}

	// 隐藏进度条
	private void hideProgress() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// 设置可视为隐藏
				progressBar.setVisibility(View.INVISIBLE);
			}
		});

	}

	@Override
	public void onCancel() {
		// 取消认证
		Toast.makeText(this, "Auth cancel", Toast.LENGTH_SHORT).show();
	}

	// 完成认证回调方法
	@Override
	public void onComplete(Bundle values) {
		// 开始同步
		CookieSyncManager.getInstance().sync();
		// 从Bundle中解析Token
		mAccessToken = Oauth2AccessToken.parseAccessToken(values);
		if (mAccessToken.isSessionValid()) {
			// 保存Token到SharedPreferences
			AccessTokenKeeper.writeAccessToken(this, mAccessToken);
			Toast.makeText(this, R.string.weibosdk_toast_auth_success,
					Toast.LENGTH_LONG).show();
			// 保存用戶的UID
			long uid = Long.valueOf(mAccessToken.getUid());
			StorageManager.setValue(this, StringUtil.SINA_UID, uid);
			// 生成一个user对象保存到数据库
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
//				DialogHelp.getInstance().showHttpDialog(this, "", "正在登录...");
			}
		} else {
			// 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
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

	// 出现异常时回调此方法
	@Override
	public void onWeiboException(WeiboException e) {
		Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
	}

	/**
	 * 
	 * 自定义WeiViewClient
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
		 * 页面开始
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

		// 页面完成时
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
	 * 异步获取 Token。
	 * 
	 * @param authCode
	 *            授权 Code，该 Code 是一次性的，只能被获取一次 Token
	 * @param appSecret
	 *            应用程序的 APP_SECRET，请务必妥善保管好自己的 APP_SECRET，
	 *            不要直接暴露在程序中，此处仅作为一个DEMO来演示。
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
		 * 请注意： {@link RequestListener} 对应的回调是运行在后台线程中的， 因此，需要使用 Handler 来配合更新
		 * UI。
		 */
		AsyncWeiboRunner.request(OAUTH2_ACCESS_TOKEN_URL, requestParams,
				"POST", new RequestListener() {
					@Override
					public void onComplete(String response) {
						LogUtil.d(TAG, "Response: " + response);

						// 获取 Token 成功
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
						LogUtil.e(TAG, "onIOException： " + e.getMessage());
					}

					@Override
					public void onError(WeiboException e) {
						LogUtil.e(TAG, "WeiboException： " + e.getMessage());
					}
				});
	}

}
