package com.wwj.weiboClient.photo;


public class PhotoThread implements Runnable {

	private PhotoProcess mPhotoProcess;
	public PhotoThread(PhotoProcess photoProcess) {
		mPhotoProcess = photoProcess;
	}
	
	@Override
	public void run() {
		// 处理图像特效
		mPhotoProcess.work();
		// 调用onFinish方法结束图像处理，可以在该方法中做一些收尾工作
		ProcessBitmapRegions.mAllThreadEnd.onFinish();
	}

}
