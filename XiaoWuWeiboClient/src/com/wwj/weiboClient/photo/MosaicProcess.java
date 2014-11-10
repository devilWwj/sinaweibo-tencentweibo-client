package com.wwj.weiboClient.photo;

import java.util.ArrayList;
import java.util.List;

import com.wwj.weiboClient.PhotoViewer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;


/**
 * 这个类负责处理马赛克特效
 * @author Administrator
 *
 */
public class MosaicProcess extends PhotoProcessImpl {
	// 保存马赛克区域更小的区域块
	protected List<Rect> mRegions = new ArrayList<Rect>();
	// 特效区域被分成的更小区域的宽度
	protected int mRegionWidth;
	// 特效区域被分成的更小区域的高度
	protected int mRegionHeight;

	public MosaicProcess(Bitmap mBitmap, Rect mRect) {
		super(mBitmap, mRect);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			PhotoViewer.mPbPhotoProcess.setProgress(msg.arg1);
			PhotoViewer.mPbPhotoProcess.setMax(msg.arg2);

			super.handleMessage(msg);
		};
	};

	// 将特效区域分成更小的区域
	private void splitRegions() {
		Rect rect = null;

		int left = 0, top = 0;
		mRegions.clear();
		do {
			do {
				// 特效区域被分成的更小区域
				rect = new Rect();
				// 确定小区域左上角的横坐标
				rect.left = left;
				// 确定小区域右下角的横坐标
				rect.right = rect.left + mRegionWidth;

				if (rect.right >= mWidth)
					rect.right = mWidth - 1;
				// 确定小区域左上角的纵坐标
				rect.top = top;
				// 确定小区域右下角的纵坐标
				rect.bottom = rect.top + mRegionHeight;
				if (rect.bottom >= mHeight)
					rect.bottom = mHeight - 1;
				left = rect.right;

				if (mRect != null) {
					rect.left = rect.left + mRect.left;
					rect.top = rect.top + mRect.top;
					rect.right = rect.right + mRect.left;
					rect.bottom = rect.bottom + mRect.top;
				}

				mRegions.add(rect);

			} while (left < mWidth - 1);
			left = 0;
			top = top + mRegionHeight;
		} while (top < mHeight);

	}

	@Override
	public void work() {
		mWidth = mRect.right - mRect.left + 1;
		mHeight = mRect.bottom - mRect.top + 1;
		
		mRegionWidth = ProcessBitmapRegions.MOSAIC_SINGLE_REGION_SIZE;
		mRegionHeight  = ProcessBitmapRegions.MOSAIC_SINGLE_REGION_SIZE;
		// 将特效区域分成更小的区域
		splitRegions();
		// 处理所有小区域的颜色
		for (int k = 0; k < mRegions.size(); k++) {
			// 获取当前的小区域
			Rect rect = mRegions.get(k);
			// 获取当前小区域要设置的颜色值
			int color = mBitmap.getPixel((rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2);
			// 扫描小区域中所有的像素点
			for (int i = rect.left; i <= rect.right; i++) {
				for (int j = rect.top; j <= rect.bottom; j++) {
					if (i < mBitmap.getWidth() && j < mBitmap.getHeight())
						// 设置小区域的颜色
						mBitmap.setPixel(i, j, color);
				}
			}
			Message message = new Message();
			message.arg2 = mRegions.size();
			message.arg1 = k + 1;
			// 更新处理进度
			mHandler.sendMessage(message);
		}
	}

}
