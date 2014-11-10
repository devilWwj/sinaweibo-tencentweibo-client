package com.wwj.weiboClient.workqueue.task;

/**
 * 【评论微博任务】
 * 
 * @author wwj
 * 
 */
public class CommentWeiboTask extends ParentTask {
	public long id;
	public String text;
	public String weiboText;
	public boolean commentOri;

	public boolean postWeibo; // 同时提交一条微博

}
