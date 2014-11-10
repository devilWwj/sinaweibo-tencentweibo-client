package com.wwj.weiboClient.photo;

import java.util.ArrayList;
import java.util.List;

import com.wwj.weiboClient.PhotoViewer;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;


/**
 * ����ฺ������������Ч
 * @author Administrator
 *
 */
public class MosaicProcess extends PhotoProcessImpl {
	// ���������������С�������
	protected List<Rect> mRegions = new ArrayList<Rect>();
	// ��Ч���򱻷ֳɵĸ�С����Ŀ��
	protected int mRegionWidth;
	// ��Ч���򱻷ֳɵĸ�С����ĸ߶�
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

	// ����Ч����ֳɸ�С������
	private void splitRegions() {
		Rect rect = null;

		int left = 0, top = 0;
		mRegions.clear();
		do {
			do {
				// ��Ч���򱻷ֳɵĸ�С����
				rect = new Rect();
				// ȷ��С�������Ͻǵĺ�����
				rect.left = left;
				// ȷ��С�������½ǵĺ�����
				rect.right = rect.left + mRegionWidth;

				if (rect.right >= mWidth)
					rect.right = mWidth - 1;
				// ȷ��С�������Ͻǵ�������
				rect.top = top;
				// ȷ��С�������½ǵ�������
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
		// ����Ч����ֳɸ�С������
		splitRegions();
		// ��������С�������ɫ
		for (int k = 0; k < mRegions.size(); k++) {
			// ��ȡ��ǰ��С����
			Rect rect = mRegions.get(k);
			// ��ȡ��ǰС����Ҫ���õ���ɫֵ
			int color = mBitmap.getPixel((rect.left + rect.right) / 2, (rect.top + rect.bottom) / 2);
			// ɨ��С���������е����ص�
			for (int i = rect.left; i <= rect.right; i++) {
				for (int j = rect.top; j <= rect.bottom; j++) {
					if (i < mBitmap.getWidth() && j < mBitmap.getHeight())
						// ����С�������ɫ
						mBitmap.setPixel(i, j, color);
				}
			}
			Message message = new Message();
			message.arg2 = mRegions.size();
			message.arg1 = k + 1;
			// ���´������
			mHandler.sendMessage(message);
		}
	}

}
