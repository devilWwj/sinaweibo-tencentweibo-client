package com.wwj.weiboClient.workqueue;

import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * ����������ɺ����Ĵ���Ľӿ�
 * 
 * @author Administrator
 * 
 */
public interface DoneAndProcess {
	void doneProcess(ParentTask task);
}
