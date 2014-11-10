package com.wwj.weiboClient.photo;

import android.graphics.Bitmap;
import android.graphics.Color;


public class GrayProcess extends PhotoProcessImpl {

	public GrayProcess(Bitmap mBitmap) {
		super(mBitmap);
	}

	// ��ͼ����лҶȴ���
	@Override
	public void work() {
		// ʹ������forѭ��ɨ��ͼ����ÿһ�����ص�
		for (int i = 0; i < mBitmap.getWidth(); i++) {
			for (int j = 0; j < mBitmap.getHeight(); j++) {
				// ��õ�ǰ���ص�ĺ�ɫ
				int red = Color.red(mBitmap.getPixel(i, j));
				// ��õ�ǰ���ص����ɫ
				int green = Color.green(mBitmap.getPixel(i, j));
				// ��õ�ǰ���ص����ɫ
				int blue = Color.blue(mBitmap.getPixel(i, j));

				// ���ûҶ��㷨����Ҷ�ֵ
				int gray = (int) ((red & 0xff) * 0.3);
				gray += (int) ((green & 0xff) * 0.59);
				gray += (int) ((blue & 0xff) * 0.11);

				// ʹ�ûҶ�ֵ�滻��ԭɫ�����У�255<<24����ʾ��ɫ��͸����
				mBitmap.setPixel(i, j, (255 << 24) | (gray << 16) | (gray << 8)
						| gray);
			}
			mHandler.sendEmptyMessage(i);
		}
	}

}
