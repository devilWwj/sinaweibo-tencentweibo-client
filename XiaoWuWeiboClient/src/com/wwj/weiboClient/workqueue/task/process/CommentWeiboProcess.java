package com.wwj.weiboClient.workqueue.task.process;

import android.content.Context;

import com.sina.weibo.sdk.openapi.legacy.CommentsAPI;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.workqueue.task.CommentWeiboTask;

/**
 * ����΢���������� 
 * 
 * ��������΢������API
 * 
 * @author wwj
 * 
 */
public class CommentWeiboProcess {
	private Context context;

	public CommentWeiboProcess(Context context) {
		super();
		this.context = context;
	}

	// �ύ����
	public void process(CommentWeiboTask task) throws Exception {
		CommentsAPI commentsAPI = new CommentsAPI(
				GlobalObject.getSinaAccessToken(context));
		commentsAPI.create(task.text, task.id, task.commentOri, null);
	}

}
