package com.wwj.weiboClient.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 吐司工具类
 * 
 * @author wwj
 * 
 */
public class ToastUtil {
	public static void showShortToast(Context context, int str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static void showShortToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static void showLongToast(Context context, int str) {
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}

	public static void showLongToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_LONG).show();
	}
}
