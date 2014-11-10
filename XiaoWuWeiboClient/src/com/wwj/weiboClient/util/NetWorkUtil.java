package com.wwj.weiboClient.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * 检查网络工具类
 * 
 * @author wwj
 * 
 */
public class NetWorkUtil {
	/**
	 * 检测网络是否可用。如果不可用，则提示Toast，并返回false
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetWorkAvailable(Context context) {
		if (!isNetWorkAvailable(context)) {
			Toast.makeText(context, "当前网络不可用,请检查网络配置!", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		return true;
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isNetWorkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}

		NetworkInfo[] infos = cm.getAllNetworkInfo();
		if (infos != null) {
			for (int i = 0; i < infos.length; i++) {
				if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}
		return false;
	}

	public static void setNetworkMethod(final Context context) {
		// 提示对话框
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("网络设置提示")
				.setMessage("网络连接超时,请检查网络？")
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = null;
						// 判断手机系统的版本，即API大于10就是3.0或以上版本
						if (android.os.Build.VERSION.SDK_INT > 10) {
							intent = new Intent(
									android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						} else {
							intent = new Intent();
							ComponentName componentName = new ComponentName(
									"com.android.settings",
									"com.android.settings.WirelessSettings");
							intent.setComponent(componentName);
							intent.setAction("android.intent.action.VIEW");
						}
						context.startActivity(intent);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

}
