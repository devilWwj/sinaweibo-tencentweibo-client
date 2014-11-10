package com.wwj.weiboClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sina.weibo.sdk.auth.WeiboAuth;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.SinaConstants;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.StringUtil;

/**
 * 登录界面 第一次登录的时候，还没有进行授权，授权过后保存帐号
 * 
 * @author wwj
 * 
 */
public class LoginActivity extends Activity implements Const, OnClickListener {
	// 新浪微博Web授权类，提供登录等功能
	private WeiboAuth mWeiboAuth;
	private static LoginActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_login);
		instance = this;
		// 新浪微博授权
		mWeiboAuth = new WeiboAuth(this, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);

		Button btnSinaLogin = (Button) findViewById(R.id.btn_sina_login);
		Button btnTencentLogin = (Button) findViewById(R.id.btn_tencent_login);
		btnSinaLogin.setOnClickListener(this);
		btnTencentLogin.setOnClickListener(this);
	}

	// 判读Activity是否正在运行
	public static boolean isRunning() {
		return (instance != null && !instance.isFinishing());
	}

	public static void finishIfRunning() {
		if (instance != null && !instance.isFinishing()) {
			instance.finish();
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, FragmentBottomTab.class);
		// 是否已经登录过
		boolean hasLogin = StorageManager.getValue(this, StringUtil.HAS_LOGIN,
				false);
		switch (v.getId()) {
		case R.id.btn_sina_login: // 新浪账号登录
			// 保存登录类型
			StorageManager.setValue(this, StringUtil.LOGIN_TYPE, SINA);
			// 判断帐号是否过期
			if (hasLogin
					&& AccessTokenKeeper.readAccessToken(instance)
							.isSessionValid()) {

				startActivity(intent);
				this.finish();
			} else { // 如果过期就重新授权
				// 授权
				// mWeiboAuth.anthorize(new SinaAuthListener(this));
				Intent i2 = new Intent(this, WebViewActivity.class);
				startActivity(i2);

			}
			break;
		case R.id.btn_tencent_login: // 腾讯账号登录
			// 保存登录类型
			StorageManager.setValue(this, StringUtil.LOGIN_TYPE, TENCENT);
			if (hasLogin && Util.getSharePersistent(this, "EXPIRES_IN") != "") {
				startActivity(intent);
				this.finish();
			} else {
				Intent i2 = new Intent(this, Authorize.class);
				startActivity(i2);
			}
			// long appid =
			// Long.valueOf(Util.getConfig().getProperty("APP_KEY"));
			// String app_secret = Util.getConfig().getProperty("APP_KEY_SEC");
			// LoginUtil.tengXunSSOAuth(this, appid, app_secret);

			break;
		}
	}
}
