package com.wwj.weiboClient.workqueue.task.process;

import android.content.Context;

import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.workqueue.task.PostWeiboTask;

/**
 * 发布微博
 * 
 * 调用新浪微博发布微博API
 * 
 * @author Administrator
 * 
 */
public class PostWeiboProcess {
	private Context context;

	public PostWeiboProcess(Context context) {
		super();
		this.context = context;
	}

	public void process(PostWeiboTask task) throws Exception {
		StatusesAPI statusesAPI = new StatusesAPI(
				GlobalObject.getSinaAccessToken(context));
		// 如果有图片，调用upload方法
		if (task.file != null) {
			statusesAPI.upload(task.text, task.file, "", "", null);
			return;
		}
		// 纯文字
		statusesAPI.update(task.text, "", "", null);
	}
}
