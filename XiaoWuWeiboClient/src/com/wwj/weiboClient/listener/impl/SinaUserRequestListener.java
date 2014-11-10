package com.wwj.weiboClient.listener.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Looper;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.wwj.weiboClient.AccountManage;
import com.wwj.weiboClient.FragmentBottomTab;
import com.wwj.weiboClient.LoginActivity;
import com.wwj.weiboClient.database.DataBaseContext;
import com.wwj.weiboClient.database.DataHelper;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.User;
import com.wwj.weiboClient.util.DialogHelp;
import com.wwj.weiboClient.util.ImageUtil;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;

public class SinaUserRequestListener implements RequestListener {
	private UserInfo userInfo;
	private Context context;
	private DataHelper dataHelper;

	public SinaUserRequestListener(Context context, UserInfo userInfo) {
		super();
		this.context = context;
		this.userInfo = userInfo;
		// 获取数据库连接类，单例
		dataHelper = DataBaseContext.getInstance(context);
	}

	@Override
	public void onComplete(String response) {
		LogUtils.v(response);
		try {
			User user = new User();
			JSONAndObject.convertSingleObject(user, response);
			String id = String.valueOf(user.id);
			String name = user.screen_name;
			String profile_image_url = user.profile_image_url;
			Bitmap bitmap = null;
			if (profile_image_url != null) {
				bitmap = ImageUtil.getRoundBitmapFromUrl(profile_image_url, 15);
			}
			// 将这个user对象保存到数据库
			userInfo.setUserName(name);
			userInfo.setType(Const.SINA);
			userInfo.setUrl(profile_image_url);

			if (dataHelper.haveUserInfo(userInfo.getUserId())) { // 数据库已经存在了此用户
				dataHelper.updateUserInfo(name, bitmap, id);
			} else { // 保存用户
				if (profile_image_url != null) {
					dataHelper.saveUserInfo(userInfo, null);
				} else {
					dataHelper.saveUserInfo(userInfo, null);
				}
				// Toast.makeText(context, "添加账号成功", Toast.LENGTH_SHORT).show();
				Looper.prepare();
				if (AccountManage.handler != null) {
					AccountManage.handler.sendEmptyMessage(0);
				}
				Looper.loop();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			boolean hasLogin = StorageManager.getValue(context,
					StringUtil.HAS_LOGIN, false);
			if (!hasLogin) {
				DialogHelp.getInstance().dismissDialog();
				StorageManager.setValue(context, StringUtil.LOGIN_TYPE, 0);
				StorageManager.setValue(context, StringUtil.HAS_LOGIN, true);
				context.startActivity(new Intent(context,
						FragmentBottomTab.class));
			}
			if (LoginActivity.isRunning()) {
				LoginActivity.finishIfRunning();
			}
		}

	}

	@Override
	public void onComplete4binary(ByteArrayOutputStream responseOS) {

	}

	@Override
	public void onIOException(IOException e) {
		e.printStackTrace();
	}

	@Override
	public void onError(WeiboException e) {
		e.printStackTrace();
	}

}
