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
 * ������繤����
 * 
 * @author wwj
 * 
 */
public class NetWorkUtil {
	/**
	 * ��������Ƿ���á���������ã�����ʾToast��������false
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetWorkAvailable(Context context) {
		if (!isNetWorkAvailable(context)) {
			Toast.makeText(context, "��ǰ���粻����,������������!", Toast.LENGTH_LONG)
					.show();
			return false;
		}
		return true;
	}

	/**
	 * �ж������Ƿ����
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
		// ��ʾ�Ի���
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("����������ʾ")
				.setMessage("�������ӳ�ʱ,�������磿")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = null;
						// �ж��ֻ�ϵͳ�İ汾����API����10����3.0�����ϰ汾
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
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

}
