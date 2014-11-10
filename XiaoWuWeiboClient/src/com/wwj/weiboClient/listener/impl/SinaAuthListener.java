package com.wwj.weiboClient.listener.impl;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wwj.weiboClient.R;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.DialogHelp;
import com.wwj.weiboClient.util.StringUtil;

/**
 * 新浪认证
 * 
 * @author wwj
 * 
 */
public class SinaAuthListener implements WeiboAuthListener, Const {
	private static final String TAG = "sina oauth";
	private Context mContext;
	private Oauth2AccessToken mAccessToken;

	public SinaAuthListener(Context mContext) {
		super();
		this.mContext = mContext;
	}

	@Override
	public void onCancel() {
		Toast.makeText(mContext, R.string.weibosdk_toast_auth_canceled,
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onComplete(Bundle values) { // 回调方法
		LogUtil.d(TAG, "Oauth Response: " + values);
		// 从Bundle中解析Token
		mAccessToken = Oauth2AccessToken.parseAccessToken(values);
		if (mAccessToken.isSessionValid()) {
			// 保存Token到SharedPreferences
			AccessTokenKeeper.writeAccessToken(mContext, mAccessToken);
			Toast.makeText(mContext, R.string.weibosdk_toast_auth_success,
					Toast.LENGTH_LONG).show();
			// 保存用戶的UID
			long uid = Long.valueOf(mAccessToken.getUid());
			StorageManager.setValue(mContext, StringUtil.SINA_UID, uid);
			// 生成一个user对象保存到数据库
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(String.valueOf(uid));
			userInfo.setToken(mAccessToken.getToken());
			userInfo.setTokenSecret("");
			userInfo.setExpires_in(String.valueOf(mAccessToken.getExpiresTime()));
			SinaWeiboManager.getUserInfo(mContext, uid,
					new SinaUserRequestListener(mContext, userInfo));

			boolean hasLogin = StorageManager.getValue(mContext,
					StringUtil.HAS_LOGIN, false);
			if (!hasLogin) {
				DialogHelp.getInstance()
						.showHttpDialog(mContext, "", "正在登录...");
			}
		} else {
			// 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
			String code = values.getString("code");
			String message = mContext
					.getString(R.string.weibosdk_toast_auth_failed);
			if (!TextUtils.isEmpty(code)) {
				message = message + "\nObtained the code: " + code;
			}
			Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onWeiboException(WeiboException e) {
		e.printStackTrace();
	}
}
