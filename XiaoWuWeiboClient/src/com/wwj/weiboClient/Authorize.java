package com.wwj.weiboClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.weibo.sdk.android.api.util.BackGroudSeletor;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.listener.impl.TencentGetUserInfoCallBack;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.util.StringUtil;

/**
 * ��Ѷ�û���Ȩ���
 * 
 * */
public class Authorize extends Activity {

	WebView webView;
	String _url;
	String _fileName;
	public static int WEBVIEWSTATE_1 = 0;
	int webview_state = 0;
	String path;
	Dialog _dialog;
	public static final int ALERT_DOWNLOAD = 0;
	public static final int ALERT_FAV = 1;
	public static final int PROGRESS_H = 3;
	public static final int ALERT_NETWORK = 4;
	private ProgressDialog dialog;
	private LinearLayout layout = null;
	private String redirectUri = null;
	private String clientId = null;
	private boolean isShow = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Util.isNetworkAvailable(this)) {
			Authorize.this.showDialog(ALERT_NETWORK);
		} else {
			DisplayMetrics displaysMetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
			String pix = displaysMetrics.widthPixels + "x"
					+ displaysMetrics.heightPixels;
			BackGroudSeletor.setPix(pix);

			try {
				// Bundle bundle = getIntent().getExtras();
				clientId = Util.getConfig().getProperty("APP_KEY");// bundle.getString("APP_KEY");
				redirectUri = Util.getConfig().getProperty("REDIRECT_URI");// bundle.getString("REDIRECT_URI");
				if (clientId == null || "".equals(clientId)
						|| redirectUri == null || "".equals(redirectUri)) {
					Toast.makeText(Authorize.this, "���������ļ�����д��Ӧ����Ϣ",
							Toast.LENGTH_SHORT).show();
				}
				Log.d("redirectUri", redirectUri);
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				int state = (int) Math.random() * 1000 + 111;
				path = "https://open.t.qq.com/cgi-bin/oauth2/authorize?client_id="
						+ clientId
						+ "&response_type=token&redirect_uri="
						+ redirectUri + "&state=" + state;
				this.initLayout();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ��ʼ������ʹ�ÿؼ�����������Ӧ����
	 * */
	public void initLayout() {
		RelativeLayout.LayoutParams fillParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);
		RelativeLayout.LayoutParams fillWrapParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams wrapParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		// ����Ի���
		dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setMessage("���Ժ�...");
		dialog.setIndeterminate(false);
		dialog.setCancelable(false);
		dialog.show();

		layout = new LinearLayout(this);
		layout.setLayoutParams(fillParams);
		layout.setOrientation(LinearLayout.VERTICAL);

		RelativeLayout cannelLayout = new RelativeLayout(this);
		cannelLayout.setLayoutParams(fillWrapParams);
		cannelLayout.setBackgroundDrawable(BackGroudSeletor.getdrawble(
				"up_bg2x", getApplication()));
		cannelLayout.setGravity(LinearLayout.HORIZONTAL);

		Button returnBtn = new Button(this);
		String[] pngArray = { "quxiao_btn2x", "quxiao_btn_hover" };
		returnBtn.setBackgroundDrawable(BackGroudSeletor.createBgByImageIds(
				pngArray, getApplication()));
		returnBtn.setText("ȡ��");
		wrapParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
				RelativeLayout.TRUE);
		wrapParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		wrapParams.leftMargin = 10;
		wrapParams.topMargin = 10;
		wrapParams.bottomMargin = 10;

		returnBtn.setLayoutParams(wrapParams);
		returnBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Authorize.this.finish();
			}
		});
		cannelLayout.addView(returnBtn);

		TextView title = new TextView(this);
		title.setText("��Ȩ");
		title.setTextColor(Color.WHITE);
		title.setTextSize(24f);
		RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		titleParams.addRule(RelativeLayout.CENTER_IN_PARENT,
				RelativeLayout.TRUE);
		title.setLayoutParams(titleParams);
		cannelLayout.addView(title);

		layout.addView(cannelLayout);

		webView = new WebView(this);
		LinearLayout.LayoutParams wvParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		webView.setLayoutParams(wvParams);
		WebSettings webSettings = webView.getSettings();
		webView.setVerticalScrollBarEnabled(false);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setLoadWithOverviewMode(false);
		webView.loadUrl(path);
		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				Log.d("newProgress", newProgress + "..");
				// if (dialog!=null&& !dialog.isShowing()) {
				// dialog.show();
				// }

			}

		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				// super.onPageFinished(view, url);
				Log.d("backurl", url);
				if (url.indexOf("access_token") != -1 && !isShow) {
					jumpResultParser(url);
				}
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
				}

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.indexOf("access_token") != -1 && !isShow) {
					jumpResultParser(url);
				}
				return false;
			}
		});
		layout.addView(webView);
		this.setContentView(layout);
	}

	/**
	 * 
	 * ��ȡ��Ȩ��ķ��ص�ַ����������н���
	 */
	public void jumpResultParser(String result) {

		String resultParam = result.split("#")[1];
		String params[] = resultParam.split("&");
		String accessToken = params[0].split("=")[1];
		String expiresIn = params[1].split("=")[1];
		String openid = params[2].split("=")[1];
		String openkey = params[3].split("=")[1];
		String refreshToken = params[4].split("=")[1];
		String state = params[5].split("=")[1];
		String name = params[6].split("=")[1];
		String nick = params[7].split("=")[1];
		Context context = this.getApplicationContext();
		if (accessToken != null && !"".equals(accessToken)) {
			Util.saveSharePersistent(context, "ACCESS_TOKEN", accessToken);
			Util.saveSharePersistent(context, "EXPIRES_IN", expiresIn);// accesstoken����ʱ�䣬�Է��ص�ʱ���׼����λΪ�룬ע�����ʱ�����û�������Ȩ
			Util.saveSharePersistent(context, "OPEN_ID", openid);
			Util.saveSharePersistent(context, "OPEN_KEY", openkey);
			Util.saveSharePersistent(context, "REFRESH_TOKEN", refreshToken);
			Util.saveSharePersistent(context, "NAME", name);
			Util.saveSharePersistent(context, "NICK", nick);
			Util.saveSharePersistent(context, "CLIENT_ID", clientId);
			Util.saveSharePersistent(context, "AUTHORIZETIME",
					String.valueOf(System.currentTimeMillis() / 1000l));

			// ����һ��UserInfo����
			UserInfo userInfo = new UserInfo();
			userInfo.setToken(accessToken);
			userInfo.setTokenSecret("");
			if (expiresIn == "") {
				expiresIn = "0";
			}
			userInfo.setExpires_in(expiresIn);
			userInfo.setType(Const.TENCENT);
			// ��ȡ�û�����
			TencentWeiboManager.getUserInfo(context, StringUtil.REQUEST_FORMAT,
					new TencentGetUserInfoCallBack(context, userInfo));

			// boolean hasLogin = StorageManager.getValue(context,
			// StringUtil.HAS_LOGIN, false);
			// if (!hasLogin) {
			// DialogHelp.getInstance().showHttpDialog(context, "", "���ڵ�¼...");
			// }
			Toast.makeText(Authorize.this, "��Ȩ�ɹ�", Toast.LENGTH_SHORT).show();
			this.finish();
			isShow = true;
		}
	}

	Handler handle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 100:
				// Log.i("showDialog", "showDialog");
				Authorize.this.showDialog(ALERT_NETWORK);
				break;
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case PROGRESS_H:
			_dialog = new ProgressDialog(this);
			((ProgressDialog) _dialog).setMessage("������...");
			break;
		case ALERT_NETWORK:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setTitle("���������쳣���Ƿ��������ӣ�");
			builder2.setPositiveButton("��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (Util.isNetworkAvailable(Authorize.this)) {
								webView.loadUrl(path);
							} else {
								Message msg = Message.obtain();
								msg.what = 100;
								handle.sendMessage(msg);
							}
						}

					});
			builder2.setNegativeButton("��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Authorize.this.finish();
						}
					});
			_dialog = builder2.create();
			break;
		}
		return _dialog;
	}

}
