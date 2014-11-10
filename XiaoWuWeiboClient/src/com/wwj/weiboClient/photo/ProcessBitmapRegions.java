package com.wwj.weiboClient.photo;

import android.graphics.Color;

public class ProcessBitmapRegions {
	public static final String PROCESS_TYPE_GRAY = "gray";
	public static final String PROCESS_TYPE_MOSAIC = "mosaic";
	public static final String PROCESS_TYPE_CROP = "crop";
	public static final String PROCESS_TYPE_ROTATE = "rotate";
	// 处理的特效类型
	public static String processType;
	// 获得图像子区域的边框线宽度（未获得焦点）
	public static final int RECT_LINE_WIDTH_NORMAL = 1;
	// 获得图像子区域的边框线宽度（未获得焦点）
	public static final int RECT_LINE_WIDTH_FOCUSED = 2;
	// 获得图像子区域的边框线颜色（未获得焦点）
	public static final int RECT_LINE_COLOR_NORMAL = Color.WHITE;
	// 获取图像子区域的边框线颜色（已获得焦点）
	public static final int RECT_LINE_COLOR_FOCUSED = Color.BLUE;
	// 可获得的最小图像的子区域宽度和高度
	public static final int MIN_MOVE_REGION_SIZE = 70;
	// 图像子区域四边的拖动块区域
	public static final int RESIZE_REGION_SIZE = 30;
	// 处理马赛克特效时每一个小块的大小
	public static final int MOSAIC_SINGLE_REGION_SIZE = 15;
	public static boolean isWorking = false;
	private PhotoProcess mPhotoProcess;
	public static AllThreadEnd mAllThreadEnd;
	public ProcessBitmapRegions(PhotoProcess photoProcess) {
		mPhotoProcess = photoProcess;
	}
	// 启动处理图像特效的线程
	public void work() {
		Thread thread = new Thread(new PhotoThread(mPhotoProcess));
		thread.start();
	}
}
