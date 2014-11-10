package com.wwj.weiboClient.workqueue.task;

import com.wwj.weiboClient.model.Status;

/**
 * ¡¾ÊÕ²ØÎ¢²©ÈÎÎñ¡¿
 * 
 * @author wwj
 * 
 */
public class FavoriteWeiboTask extends ParentTask {
	public long id;
	public boolean fav;
	public Status status;
}
