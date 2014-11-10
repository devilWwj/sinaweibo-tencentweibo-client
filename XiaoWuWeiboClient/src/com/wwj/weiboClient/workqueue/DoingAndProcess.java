package com.wwj.weiboClient.workqueue;

import java.util.List;

/**
 * 执行任务队列的接口
 * 
 * @author Administrator
 * 
 */
public interface DoingAndProcess {
	void doingProcess(List list) throws Exception;
}
