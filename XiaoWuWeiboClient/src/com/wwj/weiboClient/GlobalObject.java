package com.wwj.weiboClient;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.workqueue.WorkQueueMonitor;
import com.wwj.weiboClient.workqueue.WorkQueueStorage;
import com.wwj.weiboClient.workqueue.task.PullFile;
import com.wwj.weiboClient.workqueue.task.TaskMan;

/**
 * ��ȫ�ֶ��� ���ڹ�����������̵߳������͹ر�
 * 
 * @author wwj
 * 
 */
public class GlobalObject extends Application implements Const {
	// ���Կ���
	public static final boolean DEBUG = true;

	public static int type;

	// ����洢
	private WorkQueueStorage workQueueStorage;
	// ͼ�����ؼ�����
	private WorkQueueMonitor imageWorkQueueMonitor;
	// ͨ�����������
	private WorkQueueMonitor taskWorkQueueMonitor;

	public static int getType() {
		return type;
	}

	public static void setType(int type) {
		GlobalObject.type = type;
	}

	/**
	 * �ж��Ƿ����������ʺŵ�¼
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasSinaLogin(Context context) {
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			return false;
		}
		return true;
	}

	/**
	 * �ж��Ƿ�������Ѷ�ʺŵ�¼
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasTencentLogin(Context context) {
		String accessToken = Util.getSharePersistent(context, "ACCESS_TOKEN");
		if (accessToken.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * �õ�����΢���ķ�������
	 * 
	 * @param context
	 * @return
	 */
	public static Oauth2AccessToken getSinaAccessToken(Context context) {
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		return accessToken;
	}

	/**
	 * �õ���Ѷ΢���ķ�������
	 * 
	 * @param context
	 * @return
	 */
	public static String getTencentAccessToken(Context context) {
		String accessToken = Util.getSharePersistent(context, "ACCESS_TOKEN");
		if (accessToken == null || "".equals(accessToken)) {
			Toast.makeText(context, "������Ȩ", Toast.LENGTH_SHORT).show();
		}
		return accessToken;
	}

	/**
	 * ��ȡͼ�������м�����
	 * 
	 * @param activity
	 * @return
	 */
	public WorkQueueMonitor getImageWorkQueueMonitor(Context context) {
		if (imageWorkQueueMonitor == null) {
			// Pullfile���������ļ���ʵ����DoingAndProcess�ӿ�
			imageWorkQueueMonitor = new WorkQueueMonitor(context,
					getWorkQueueStorage(), new PullFile(), MONITOR_TYPE_IMAGE);
			// �����߳�
			imageWorkQueueMonitor.start();
		}
		return imageWorkQueueMonitor;
	}

	/**
	 * ��ȡͨ�����������
	 * 
	 * @param activity
	 * @return
	 */
	public WorkQueueMonitor getTaskWorkQueueMonitor(Context context) {
		if (taskWorkQueueMonitor == null) {
			// TaskMan���ڴ���ͨ������
			taskWorkQueueMonitor = new WorkQueueMonitor(context,
					getWorkQueueStorage(), new TaskMan(context),
					MONITOR_TYPE_TASK);
			// �����߳�
			taskWorkQueueMonitor.start();
		}
		return taskWorkQueueMonitor;
	}

	public WorkQueueMonitor getImageWorkQueueMonitor() {
		return imageWorkQueueMonitor;
	}

	public WorkQueueMonitor getTaskWorkQueueMonitor() {
		return taskWorkQueueMonitor;
	}

	public WorkQueueStorage getWorkQueueStorage() {
		if (workQueueStorage == null) {
			workQueueStorage = new WorkQueueStorage();
		}
		return workQueueStorage;
	}

	/**
	 * �ر��������
	 */
	public void closeWorkQueue() {
		if (imageWorkQueueMonitor != null) {
			imageWorkQueueMonitor.stop();
			imageWorkQueueMonitor = null;
		}
		if (taskWorkQueueMonitor != null) {
			taskWorkQueueMonitor.stop();
			taskWorkQueueMonitor = null;
		}
	}

}
