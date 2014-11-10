package com.wwj.weiboClient.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.Window;

/**
 * 自定义对话框帮助类
 * 
 * @author wwj
 * 
 */
public class DialogHelp {
	// 单例模式
	private static DialogHelp mHelp = new DialogHelp();
	// 一般对话框
	private Dialog mDialog;
	// 进度条对话框
	private ProgressDialog mProgress;

	private Dialog mRadioDialog;

	public void showDialog(Context context, View view) {
		if (mDialog == null) {
			mDialog = new Dialog(context);
			mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mDialog.setContentView(view);
		}
		mDialog.show();

	}

	public Dialog getDialog() {
		return mDialog;
	}

	// 获取单例对象
	public static DialogHelp getInstance() {
		return mHelp;
	}

	// 升级对话框，比较复杂，暂时不做
	/*
	 * public void showLevelUpDialog(Context context,String title,String
	 * content){ mProgress = new ProgressDialog(context);
	 * mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置风格为长进度条
	 * mProgress.setTitle(title);// 设置标题
	 * mProgress.setMessage(getString(R.string.downlaoding));
	 * mProgress.setIndeterminate(false);// 设置进度条是否为不明确 false 就是不设置为不明确
	 * mProgress.setCancelable(false);// 用户按back键不能消失对话框
	 * mProgress.setProgress(0); mProgress.incrementProgressBy(1); //
	 * 增加和减少进度，这个属性必须的 mProgress.show(); }
	 */

	// http请求对话框
	public void showHttpDialog(Context context, int titleId, String content) {
		// 初始化
		mProgress = null;
		mProgress = new ProgressDialog(context);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mProgress.setTitle(titleId);// 设置标题
		mProgress.setMessage(content);
		mProgress.setIndeterminate(false);// 设置进度条是否为不明确 false 就是不设置为不明确
		mProgress.setCancelable(true);// 用户按back键不能消失对话框
		mProgress.setCanceledOnTouchOutside(false); // 点击外面不能消失对话框
		mProgress.show();
	}

	public void showHttpDialog(Context context, String title, String content) {
		// 初始化
		mProgress = null;
		mProgress = new ProgressDialog(context);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置风格为圆形进度条
		mProgress.setTitle(title);// 设置标题
		mProgress.setMessage(content);
		mProgress.setIndeterminate(false);// 设置进度条是否为不明确 false 就是不设置为不明确
		mProgress.setCancelable(true);// 用户按back键不能消失对话框
		mProgress.setCanceledOnTouchOutside(false); // 点击外面不能消失对话框
		mProgress.show();
	}

	public boolean isHttpDialogShow() {
		if (mProgress == null) {
			new IllegalArgumentException(
					"mProgress is null in function isHttpDialogShow");
			return false;
		}
		return mProgress.isShowing();
	}

	public void showHttpDialog() {
		if (mProgress == null) {
			new IllegalArgumentException(
					"mProgress is null in function showHttpDialog");
			return;
		}
		if (!mProgress.isShowing())
			mProgress.show();
	}

	public void dismissDialog() {
		if (mProgress == null) {
			new IllegalArgumentException(
					"mProgress is null in function dismissDialog");
			return;
		}
		if (mProgress.isShowing())
			mProgress.dismiss();
	}

	public void dismissDialog(Context context, int text) {
		ToastUtil.showShortToast(context, context.getString(text));
		if (mProgress == null) {
			new IllegalArgumentException(
					"mProgress is null in function dismissDialog");
			return;
		}
		if (mProgress.isShowing())
			mProgress.dismiss();
	}

	// 网络超时对话框
	/*
	 * 打开设置网络界面
	 */
	public static void setNetworkDialog(final Context context) {
		// 提示对话框
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("网络设置提示")
				.setMessage("网络连接超时,请检查网络?")
				.setPositiveButton("设置", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = null;
						// 判断手机系统的版本 即API大于10 就是3.0或以上版本
						if (android.os.Build.VERSION.SDK_INT > 10) {
							intent = new Intent(
									android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						} else {
							intent = new Intent();
							ComponentName component = new ComponentName(
									"com.android.settings",
									"com.android.settings.WirelessSettings");
							intent.setComponent(component);
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
