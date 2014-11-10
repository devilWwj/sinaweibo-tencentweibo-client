package com.wwj.weiboClient.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.component.Authorize;
import com.tencent.weibo.sdk.android.component.sso.AuthHelper;
import com.tencent.weibo.sdk.android.component.sso.OnAuthListener;
import com.tencent.weibo.sdk.android.component.sso.WeiboToken;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.listener.impl.TencentGetUserInfoCallBack;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;

/**
 * 【帐号登录工具类】
 * 
 * @author wwj
 * 
 */
public class LoginUtil {

	/**
	 * 
	 * 腾讯微博Android SDK V2.0 SSO授权方式
	 * 
	 * @param appid
	 * @param app_secret
	 */
	public static void tengXunSSOAuth(final Context context, long appid,
			String app_secret) {

		// 注册回调
		AuthHelper.register(context, appid, app_secret, new OnAuthListener() {

			@Override
			public void onWeiBoNotInstalled() {
				Toast.makeText(context, "onWeiBoNotInstalled", 1000).show();
				Intent i = new Intent(context, Authorize.class);
				context.startActivity(i);
			}

			@Override
			public void onWeiboVersionMisMatch() {
				Toast.makeText(context, "onWeiboVersionMisMatch", 1000).show();
				Intent i = new Intent(context, Authorize.class);
				context.startActivity(i);
			}

			@Override
			public void onAuthFail(int result, String err) {
				Toast.makeText(context, "result : " + result, 1000).show();
			}

			@Override
			public void onAuthPassed(String name, WeiboToken token) {
				Util.saveSharePersistent(context, "ACCESS_TOKEN",
						token.accessToken);
				Util.saveSharePersistent(context, "EXPIRES_IN",
						String.valueOf(token.expiresIn));
				Util.saveSharePersistent(context, "OPEN_ID", token.openID);
				Util.saveSharePersistent(context, "OPEN_KEY", token.omasKey);
				Util.saveSharePersistent(context, "REFRESH_TOKEN", "");
				Util.saveSharePersistent(context, "NAME", name);
				Util.saveSharePersistent(context, "NICK", name);
				Util.saveSharePersistent(context, "CLIENT_ID", Util.getConfig()
						.getProperty("APP_KEY"));
				Util.saveSharePersistent(context, "AUTHORIZETIME",
						String.valueOf(System.currentTimeMillis() / 1000l));

				String accessToken = GlobalObject
						.getTencentAccessToken(context);
				if (accessToken == null || "".equals(accessToken)) {
					Toast.makeText(context, "请先授权", Toast.LENGTH_SHORT).show();
				}

				// 定义一个UserInfo对象
				UserInfo userInfo = new UserInfo();
				userInfo.setToken(token.accessToken);
				userInfo.setTokenSecret("");
				userInfo.setExpires_in(String.valueOf(token.expiresIn));
				userInfo.setType(Const.TENCENT);
				// 获取用户数据
				TencentWeiboManager.getUserInfo(context,
						StringUtil.REQUEST_FORMAT,
						new TencentGetUserInfoCallBack(context, userInfo));
				boolean hasLogin = StorageManager.getValue(context,
						StringUtil.HAS_LOGIN, false);
				if (!hasLogin) {
					DialogHelp.getInstance().showHttpDialog(context, "",
							"正在登录...");
				}
			}
		});

		AuthHelper.auth(context, "");
	}
}
