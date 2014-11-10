package com.wwj.weiboClient.listener.impl;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.AccountManage;
import com.wwj.weiboClient.FragmentBottomTab;
import com.wwj.weiboClient.LoginActivity;
import com.wwj.weiboClient.database.DataBaseContext;
import com.wwj.weiboClient.database.DataHelper;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.util.StringUtil;

/**
 * 【获取腾讯用户信息回调】
 * 
 * @author wwj
 * 
 */
public class TencentGetUserInfoCallBack implements HttpCallback {
	private final static String TAG = "TencentGetUserInfoCallBack";
	private UserInfo userInfo;

	private DataHelper dataHelper;
	private Context context;
	private Bitmap bitmap;
	private String headUrl;
	private byte[] imageByte;

	private Handler handler = new Handler();

	public TencentGetUserInfoCallBack(Context context, UserInfo userInfo) {
		super();
		this.context = context;
		this.userInfo = userInfo;
		dataHelper = DataBaseContext.getInstance(context);
	}

	@Override
	public void onResult(Object obj) {
		ModelResult result = (ModelResult) obj;
		LogUtil.v(TAG, result.getObj().toString());

		try {
			String jsonResult = result.getObj().toString();
			// 得到data对象
			final JSONObject dataObj = new JSONObject(jsonResult)
					.getJSONObject("data");
			final String userId = dataObj.getString("name");
			String username = dataObj.getString("nick");
			userInfo.setToken(Util.getSharePersistent(context, "ACCESS_TOKEN"));
			userInfo.setTokenSecret("");
			userInfo.setExpires_in(Util.getSharePersistent(context,
					"EXPIRES_IN"));
			userInfo.setType(Const.TENCENT);
			userInfo.setUserId(userId);
			userInfo.setUserName(username);

			if (dataObj.getString("head") != null
					&& !"".equals(dataObj.getString("head"))) {
				headUrl = dataObj.getString("head") + "/100";
				userInfo.setUrl(headUrl);
			}
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						// bitmap = ImageUtil.getBitmapFromUrl(headUrl);
						// imageByte = ImageUtil.getBytesFromUrl(headUrl);
						bitmap = null;
						imageByte = null;
						if (dataHelper.haveUserInfo(userId)) {
							dataHelper.updateUserInfo(
									dataObj.getString("nick"), bitmap, userId);
						} else {
							if (headUrl != null) {
								dataHelper.saveUserInfo(userInfo, imageByte);
								// dataHelper.saveUserInfo(userInfo, null);
							} else {
								dataHelper.saveUserInfo(userInfo, null);
							}
						}
						Looper.prepare();
						if (AccountManage.handler != null) {
							// 通知数据发生改变，刷新列表
							AccountManage.handler.sendEmptyMessage(1);
						}
						Looper.loop();

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			boolean hasLogin = StorageManager.getValue(context,
					StringUtil.HAS_LOGIN, false);
			if (!hasLogin) {
				// DialogHelp.getInstance().dismissDialog();
				StorageManager.setValue(context, StringUtil.LOGIN_TYPE, 1);
				StorageManager.setValue(context, StringUtil.HAS_LOGIN, true);
				context.startActivity(new Intent(context,
						FragmentBottomTab.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

			}
			if (LoginActivity.isRunning()) {
				LoginActivity.finishIfRunning();
			}
		}

	}
}
