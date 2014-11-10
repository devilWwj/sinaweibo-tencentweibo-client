package com.wwj.weiboClient.util;

import android.util.Log;

import com.wwj.weiboClient.GlobalObject;


/**
 * logcat日志工具类 用来输出程序运行日志，方便调试
 * 
 * @author wwj
 * 
 */
public class LogUtils {
	private static final String TAG = "weibo";

	public static void v(String msg) {
		if (GlobalObject.DEBUG) {
			Log.v(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (GlobalObject.DEBUG) {
			Log.w(TAG, msg);
		}
	}

	public static void d(String msg) {
		if (GlobalObject.DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void i(String msg) {
		if (GlobalObject.DEBUG) {
			Log.i(TAG, msg);
		}
	}

	public static void e(String msg) {
		if (GlobalObject.DEBUG) {
			Log.e(TAG, msg);
		}
	}
}
