package com.wwj.weiboClient.manager;

import java.io.InputStream;

import android.content.Context;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.model.AppVersion;
import com.wwj.weiboClient.parser.AppVersionParser;

public class AppVersionManager {
	private static AppVersionManager instance;

	private AppVersion appVersion;

	public synchronized static AppVersionManager getInstance(Context context) {
		if (instance == null) {
			instance = new AppVersionManager(context);
		}
		return instance;
	}

	public AppVersionManager(Context context) {
		try {
			InputStream is = context.getResources().openRawResource(
					R.raw.version);
			AppVersionParser parser = new AppVersionParser(is);
			appVersion = parser.getAppVersion();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public AppVersion getAppVersion() {
		return appVersion;
	}
}
