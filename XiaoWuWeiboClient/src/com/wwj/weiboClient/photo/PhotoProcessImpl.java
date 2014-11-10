package com.wwj.weiboClient.photo;

import com.wwj.weiboClient.PhotoViewer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;


public abstract class PhotoProcessImpl implements PhotoProcess {
	protected Bitmap mBitmap;
	protected Rect mRect;
	
	protected int mWidth;
	protected int mHeight;
	
	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			PhotoViewer.mPbPhotoProcess.setProgress(msg.what);
			super.handleMessage(msg);
		};
	};

	public PhotoProcessImpl(Bitmap mBitmap) {
		super();
		this.mBitmap = mBitmap;
	}

	public PhotoProcessImpl(Bitmap mBitmap, Rect mRect) {
		super();
		this.mBitmap = mBitmap;
		this.mRect = mRect;
	}
	
	
	
}
