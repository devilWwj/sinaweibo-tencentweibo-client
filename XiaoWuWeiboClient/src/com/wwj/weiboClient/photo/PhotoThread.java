package com.wwj.weiboClient.photo;


public class PhotoThread implements Runnable {

	private PhotoProcess mPhotoProcess;
	public PhotoThread(PhotoProcess photoProcess) {
		mPhotoProcess = photoProcess;
	}
	
	@Override
	public void run() {
		// ����ͼ����Ч
		mPhotoProcess.work();
		// ����onFinish��������ͼ���������ڸ÷�������һЩ��β����
		ProcessBitmapRegions.mAllThreadEnd.onFinish();
	}

}
