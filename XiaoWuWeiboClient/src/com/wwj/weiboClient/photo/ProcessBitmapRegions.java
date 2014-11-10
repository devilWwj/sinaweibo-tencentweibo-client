package com.wwj.weiboClient.photo;

import android.graphics.Color;

public class ProcessBitmapRegions {
	public static final String PROCESS_TYPE_GRAY = "gray";
	public static final String PROCESS_TYPE_MOSAIC = "mosaic";
	public static final String PROCESS_TYPE_CROP = "crop";
	public static final String PROCESS_TYPE_ROTATE = "rotate";
	// �������Ч����
	public static String processType;
	// ���ͼ��������ı߿��߿�ȣ�δ��ý��㣩
	public static final int RECT_LINE_WIDTH_NORMAL = 1;
	// ���ͼ��������ı߿��߿�ȣ�δ��ý��㣩
	public static final int RECT_LINE_WIDTH_FOCUSED = 2;
	// ���ͼ��������ı߿�����ɫ��δ��ý��㣩
	public static final int RECT_LINE_COLOR_NORMAL = Color.WHITE;
	// ��ȡͼ��������ı߿�����ɫ���ѻ�ý��㣩
	public static final int RECT_LINE_COLOR_FOCUSED = Color.BLUE;
	// �ɻ�õ���Сͼ����������Ⱥ͸߶�
	public static final int MIN_MOVE_REGION_SIZE = 70;
	// ͼ���������ıߵ��϶�������
	public static final int RESIZE_REGION_SIZE = 30;
	// ������������Чʱÿһ��С��Ĵ�С
	public static final int MOSAIC_SINGLE_REGION_SIZE = 15;
	public static boolean isWorking = false;
	private PhotoProcess mPhotoProcess;
	public static AllThreadEnd mAllThreadEnd;
	public ProcessBitmapRegions(PhotoProcess photoProcess) {
		mPhotoProcess = photoProcess;
	}
	// ��������ͼ����Ч���߳�
	public void work() {
		Thread thread = new Thread(new PhotoThread(mPhotoProcess));
		thread.start();
	}
}
