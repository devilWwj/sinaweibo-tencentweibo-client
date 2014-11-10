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
 * 【全局对象】 用于管理任务队列线程的启动和关闭
 * 
 * @author wwj
 * 
 */
public class GlobalObject extends Application implements Const {
	// 调试开关
	public static final boolean DEBUG = true;

	public static int type;

	// 任务存储
	private WorkQueueStorage workQueueStorage;
	// 图像下载监视器
	private WorkQueueMonitor imageWorkQueueMonitor;
	// 通用任务监视器
	private WorkQueueMonitor taskWorkQueueMonitor;

	public static int getType() {
		return type;
	}

	public static void setType(int type) {
		GlobalObject.type = type;
	}

	/**
	 * 判断是否已用新浪帐号登录
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
	 * 判断是否已用腾讯帐号登录
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
	 * 得到新浪微博的访问令牌
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
	 * 得到腾讯微博的访问令牌
	 * 
	 * @param context
	 * @return
	 */
	public static String getTencentAccessToken(Context context) {
		String accessToken = Util.getSharePersistent(context, "ACCESS_TOKEN");
		if (accessToken == null || "".equals(accessToken)) {
			Toast.makeText(context, "请先授权", Toast.LENGTH_SHORT).show();
		}
		return accessToken;
	}

	/**
	 * 获取图像工作队列监视器
	 * 
	 * @param activity
	 * @return
	 */
	public WorkQueueMonitor getImageWorkQueueMonitor(Context context) {
		if (imageWorkQueueMonitor == null) {
			// Pullfile用于下载文件，实现了DoingAndProcess接口
			imageWorkQueueMonitor = new WorkQueueMonitor(context,
					getWorkQueueStorage(), new PullFile(), MONITOR_TYPE_IMAGE);
			// 开启线程
			imageWorkQueueMonitor.start();
		}
		return imageWorkQueueMonitor;
	}

	/**
	 * 获取通用任务监视器
	 * 
	 * @param activity
	 * @return
	 */
	public WorkQueueMonitor getTaskWorkQueueMonitor(Context context) {
		if (taskWorkQueueMonitor == null) {
			// TaskMan用于处理通用任务
			taskWorkQueueMonitor = new WorkQueueMonitor(context,
					getWorkQueueStorage(), new TaskMan(context),
					MONITOR_TYPE_TASK);
			// 开启线程
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
	 * 关闭任务队列
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
