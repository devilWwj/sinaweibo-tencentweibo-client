package com.wwj.weiboClient.workqueue;

import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * 用于任务完成后续的处理的接口
 * 
 * @author Administrator
 * 
 */
public interface DoneAndProcess {
	void doneProcess(ParentTask task);
}
