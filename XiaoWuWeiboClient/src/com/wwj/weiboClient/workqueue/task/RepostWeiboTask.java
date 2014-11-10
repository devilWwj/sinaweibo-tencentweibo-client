package com.wwj.weiboClient.workqueue.task;

/**
 * 转发微博任务
 * @author wwj
 *
 */
public class RepostWeiboTask extends ParentTask{
	public long id;
	public String text;
	public int isComment;
}
