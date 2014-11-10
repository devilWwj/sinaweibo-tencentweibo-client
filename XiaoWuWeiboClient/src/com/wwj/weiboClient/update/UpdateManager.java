package com.wwj.weiboClient.update;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.manager.AppVersionManager;
import com.wwj.weiboClient.model.AppVersion;
import com.wwj.weiboClient.model.AppVersionLatest;
import com.wwj.weiboClient.net.HttpClientManager;
import com.wwj.weiboClient.parser.AppVersionLatestParser;
import com.wwj.weiboClient.update.UpdateVersionImpl.UpdateVersion;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class UpdateManager {
	private Context mContext;
	private String appname = null;
	private ProgressDialog mPDialog;
	// 下载地址
	private String mURL;
	// 处理不同下载情况
	private Handler mHandler;
	// 情况分类
	public static final int HAS_NEW_VERSION = 1; // 有新版本
	public static final int NO_NEW_VERSION = 2; // 没有新版本
	public static final int CONNECTION_FAIL = 3; // 网络连接失败

	public static final String CONNECTION_FAILDES = "检查更新失败：无法连接到网络";
	public static final String NEW_VERSIONDES = "检查到最新版本为：V";
	public static final String NO_NEWVERSIONDES = "版本已经是最新，不需要升级！";

	// 下载安装路径
	public static final String PATH = Environment.getExternalStorageDirectory()
			.getPath() + "/wwj";
	public static final String APPNAME = "/mobilemeeting.apk";

	public UpdateManager(Context context, String url, Handler handler) {
		this.mContext = context;
		this.mURL = url;
		this.mHandler = handler;
	}

	public void checkUpdate(Context context) {
		Thread checkThread = new Thread(new UpdateCheckThread());
		checkThread.start();
	}

	public static boolean hasNewVersion(AppVersion appVersion,
			AppVersionLatest appVersionLatest) {
		return appVersion.getFileVersion().compareToIgnoreCase(
				appVersionLatest.getFileVersion()) < 0;
	}

	public void updateDialog(final AppVersionLatest avl) {
		final Dialog dlg = new Dialog(mContext, R.style.myDialogTheme);
		dlg.show();

		Window window = dlg.getWindow();
		WindowManager.LayoutParams params = window.getAttributes();
		WindowManager manager = (WindowManager) ((Activity) mContext)
				.getWindowManager();
		Display metrics = manager.getDefaultDisplay();
		params.width = (int) (metrics.getWidth() * 0.9);
		params.height = LayoutParams.WRAP_CONTENT;
		window.setAttributes(params);
		window.setContentView(R.layout.update);
		TextView updateText = (TextView) window.findViewById(R.id.tip);
		updateText.setText(mContext.getString(R.string.more_update_msg)
				+ avl.getFileVersion()
				+ mContext.getString(R.string.more_update_msg_2));
		TextView resionText = (TextView) window.findViewById(R.id.change_reson);
		resionText.setText(avl.getUpdateInfo());
		Button updateOkBtn = (Button) window.findViewById(R.id.update_ok);
		// 点击更新
		updateOkBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				downloadVersionProcess(avl);
				dlg.cancel();
			}
		});
		Button updateCancelBtn = (Button) window
				.findViewById(R.id.update_cancel);
		updateCancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dlg.cancel();
			}
		});

	}

	private void downloadVersionProcess(AppVersionLatest appVersionLatest) {
		Tools.createFile(UpdateManager.PATH);
		updateDialog();
		appname = UpdateManager.APPNAME;
		UpdateVersionImpl ui = new UpdateVersionImpl(new NewVersion(),
				(Activity) mContext, appVersionLatest.getDownloadUrl(),
				appVersionLatest.getFileSize(), appname);
		ui.execute();
	}

	private void updateDialog() {
		mPDialog = new ProgressDialog(mContext);
		mPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // 进度条样式
		mPDialog.setTitle(R.string.tip);
		mPDialog.setMessage(mContext.getString(R.string.downlaoding));
		mPDialog.setIndeterminate(false);
		mPDialog.setCancelable(false);
		mPDialog.setProgress(0);
		mPDialog.incrementProgressBy(1);
		mPDialog.show();

	}

	class UpdateCheckThread implements Runnable {

		@Override
		public void run() {
			HttpClientManager httpClientManager = new HttpClientManager();
			AppVersion appVersion = AppVersionManager.getInstance(mContext)
					.getAppVersion();
			StringBuffer sb = new StringBuffer();
			sb.append("appName=" + URLEncoder.encode(appVersion.getAppName()));
			sb.append("&fileVersion="
					+ URLEncoder.encode(appVersion.getFileVersion()));
			Log.d("update", "sb: " + sb.toString());
			byte[] respBytes = httpClientManager.httpPost2(mURL, sb.toString()
					.getBytes());

			if (respBytes == null) {
				Message msg = Message.obtain();
				msg.what = UpdateManager.CONNECTION_FAIL;
				msg.obj = UpdateManager.CONNECTION_FAILDES;
				mHandler.sendMessage(msg);
				Log.d("update", "fail to connecting");
				return;
			}

			try {
				InputStream is = new ByteArrayInputStream(respBytes);
				AppVersionLatestParser parser = new AppVersionLatestParser(is);
				final AppVersionLatest appVersionLatest = parser
						.getAppVersionLatest();
				Log.e("lastest version", appVersionLatest.toString());

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class NewVersion implements UpdateVersion {

		@Override
		public void onUpdateBegin() {
			mPDialog.setMax(100);
		}

		@Override
		public void onUpdateing(long result) {
			mPDialog.setProgress((int) result);
		}

		@Override
		public void onUpdateSuccess() {
			mPDialog.dismiss();
			Tools.installApk(mContext, UpdateManager.PATH + appname);
		}

		@Override
		public void onUpdateError(int code) {
			if (code == 3) {
				mPDialog.dismiss();
				ToastUtil.showShortToast(mContext, R.string.url_error);
			} else if (code == 4) {
				ToastUtil.showShortToast(mContext, R.string.update_timeout);
			}
		}
		
	}
	
}
