package com.wwj.weiboClient.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.os.AsyncTask;

public class UpdateVersionImpl extends AsyncTask<String, Void, String> {

	private String mVersionPath = null;
	private UpdateVersion _event;
	public static final int JSON_ERROR = 1;
	public static final int OBJECT_NULL = 2;
	public static final int URL_NULL = 3;
	public static final int TIME_OUT = 4;
	private long mFileSize = 0;
	private int downLoadFileSize = 0;
	private int _code = -1;
	private Activity mActivity;
	private String mAppname = null;

	public UpdateVersionImpl(UpdateVersion event, Activity activity,
			String versionpath, long fileSize, String appname) {
		this.mVersionPath = versionpath;
		this._event = event;
		this.mFileSize = fileSize;
		this.mAppname = appname;
		this.mActivity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected String doInBackground(String... params) {
		if (params == null && params.length < 1) {
			_code = OBJECT_NULL;
			return null;
		}
		URL url = null;
		try {
			url = new URL(mVersionPath);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setConnectTimeout(1000 * 20);
			InputStream in = con.getInputStream();

			File fileOut = new File(UpdateManager.PATH + mAppname);
			FileOutputStream out = new FileOutputStream(fileOut);
			byte[] bytes = new byte[1024];
			onBegin();
			int c;
			while ((c = in.read(bytes)) != -1) {
				out.write(bytes, 0, c);
				downLoadFileSize += c;
				mActivity.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						long a = downLoadFileSize * 100 / mFileSize;
						onIng(a);
					}
				});
			}
			in.close();
			out.close();

			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					onSUC();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			mActivity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					onError(TIME_OUT);
				}
			});
		}
		return null;
	}

	private void onError(int code) {
		if (_event != null) {
			_event.onUpdateError(code);
		}
	}

	private void onSUC() {
		if (_event != null) {
			_event.onUpdateSuccess();
		}
	}

	private void onBegin() {
		if (_event != null) {
			_event.onUpdateBegin();
		}
	}

	private void onIng(long result) {
		if (_event != null) {
			_event.onUpdateing(result);
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
	}

	public interface UpdateVersion {
		void onUpdateBegin();

		void onUpdateing(long result);

		void onUpdateSuccess();

		void onUpdateError(int code);
	}
}
