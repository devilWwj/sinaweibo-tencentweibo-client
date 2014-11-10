package com.wwj.weiboClient.workqueue;

import java.util.ArrayList;
import java.util.List;

/**
 * 【任务存储】
 * 
 * @author wwj
 * 
 */
public class WorkQueueStorage {
	// 存储文件下载任务的List
	private List<String> webFileDoingList = new ArrayList<String>();
	// 存储通用任务的List， 所有任务都必须是ParentTask的子类
	private List taskList = new ArrayList();

	// 获取文件下载任务（要下载的URL)
	public List<String> getDoingWebFileUrls() {
		// 进行同步
		synchronized (webFileDoingList) {
			if (webFileDoingList.size() > 0) {
				List<String> resultList = new ArrayList<String>();
				resultList.addAll(webFileDoingList);
				return resultList;
			} else {
				return null;
			}
		}
	}

	// 删除文件下载队列中的任务
	public void removeDoingWebFileUrl(List<String> list) {
		synchronized (webFileDoingList) {
			if (list != null)
				webFileDoingList.removeAll(list);

		}
	}

	// 向文件下载队列添加任务（一个Url）
	public void addDoingWebFileUrl(String url) {
		synchronized (webFileDoingList) {
			webFileDoingList.add(url);
		}
	}

	// 目前必须只得到到一个任务，如果一下得到多个任务，需要修改WorkQueueMonitor类的代码
	// 获取一个通用任务
	public List getDoingTasks() {
		synchronized (taskList) {
			List result = new ArrayList();
			if (taskList.size() > 0) {
				result.add(taskList.get(0));
				return result;
			} else {
				return null;
			}
		}
	}

	// 删除通用任务
	public void removeTask(List tasks) {
		synchronized (taskList) {
			if (taskList.size() > 0) {
				taskList.removeAll(tasks);
			}
		}
	}

	// 添加通用任务
	public void addTask(Object task) {
		synchronized (taskList) {
			taskList.add(task);
		}
	}
}
