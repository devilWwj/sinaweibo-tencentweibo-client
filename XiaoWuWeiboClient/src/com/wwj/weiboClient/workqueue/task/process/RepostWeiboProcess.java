package com.wwj.weiboClient.workqueue.task.process;

import android.content.Context;

import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI.COMMENTS_TYPE;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.workqueue.task.RepostWeiboTask;

/**
 * ת��΢��������
 * 
 * ��������΢��ת��API
 * 
 * @author wwj
 * 
 */
public class RepostWeiboProcess {
	private Context context;

	public RepostWeiboProcess(Context context) {
		super();
		this.context = context;
	}

	// �ύ΢��
	public void process(RepostWeiboTask task) throws Exception {
		StatusesAPI statusesAPI = new StatusesAPI(
				GlobalObject.getSinaAccessToken(context));
		COMMENTS_TYPE is_comment = COMMENTS_TYPE.NONE;
		// 0����1�����۸���ǰ΢����2�����۸�ԭ΢����3�������ۣ�Ĭ��Ϊ0
		if (task.isComment == 0) {
			is_comment = COMMENTS_TYPE.NONE;
		} else if (task.isComment == 1) {
			is_comment = COMMENTS_TYPE.CUR_STATUSES;
		} else if (task.isComment == 2) {
			is_comment = COMMENTS_TYPE.ORIGAL_STATUSES;
		} else if (task.isComment == 3) {
			is_comment = COMMENTS_TYPE.BOTH;
		}
		statusesAPI.repost(task.id, task.text, is_comment, null);
	}

}
