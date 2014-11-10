package com.wwj.weiboClient.photo;

import android.graphics.Bitmap;
import android.graphics.Color;


public class GrayProcess extends PhotoProcessImpl {

	public GrayProcess(Bitmap mBitmap) {
		super(mBitmap);
	}

	// 对图像进行灰度处理
	@Override
	public void work() {
		// 使用两个for循环扫描图像中每一个像素点
		for (int i = 0; i < mBitmap.getWidth(); i++) {
			for (int j = 0; j < mBitmap.getHeight(); j++) {
				// 获得当前像素点的红色
				int red = Color.red(mBitmap.getPixel(i, j));
				// 获得当前像素点的绿色
				int green = Color.green(mBitmap.getPixel(i, j));
				// 获得当前像素点的蓝色
				int blue = Color.blue(mBitmap.getPixel(i, j));

				// 利用灰度算法计算灰度值
				int gray = (int) ((red & 0xff) * 0.3);
				gray += (int) ((green & 0xff) * 0.59);
				gray += (int) ((blue & 0xff) * 0.11);

				// 使用灰度值替换三原色，其中（255<<24）表示颜色的透明度
				mBitmap.setPixel(i, j, (255 << 24) | (gray << 16) | (gray << 8)
						| gray);
			}
			mHandler.sendEmptyMessage(i);
		}
	}

}
