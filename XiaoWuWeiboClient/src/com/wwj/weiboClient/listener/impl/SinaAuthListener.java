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
 * ������֤
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
	public void onComplete(Bundle values) { // �ص�����
		LogUtil.d(TAG, "Oauth Response: " + values);
		// ��Bundle�н���Token
		mAccessToken = Oauth2AccessToken.parseAccessToken(values);
		if (mAccessToken.isSessionValid()) {
			// ����Token��SharedPreferences
			AccessTokenKeeper.writeAccessToken(mContext, mAccessToken);
			Toast.makeText(mContext, R.string.weibosdk_toast_auth_success,
					Toast.LENGTH_LONG).show();
			// �����Ñ���UID
			long uid = Long.valueOf(mAccessToken.getUid());
			StorageManager.setValue(mContext, StringUtil.SINA_UID, uid);
			// ����һ��user���󱣴浽���ݿ�
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
						.showHttpDialog(mContext, "", "���ڵ�¼...");
			}
		} else {
			// ����ע���Ӧ�ó���ǩ������ȷʱ���ͻ��յ� Code����ȷ��ǩ����ȷ
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
