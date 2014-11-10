package com.wwj.weiboClient.workqueue.task.process;

import android.content.Context;

import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.workqueue.task.PostWeiboTask;

/**
 * ����΢��
 * 
 * ��������΢������΢��API
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
		// �����ͼƬ������upload����
		if (task.file != null) {
			statusesAPI.upload(task.text, task.file, "", "", null);
			return;
		}
		// ������
		statusesAPI.update(task.text, "", "", null);
	}
}
