package com.wwj.weiboClient.workqueue.task;

import com.wwj.weiboClient.model.Status;

/**
 * ���ղ�΢������
 * 
 * @author wwj
 * 
 */
public class FavoriteWeiboTask extends ParentTask {
	public long id;
	public boolean fav;
	public Status status;
}
