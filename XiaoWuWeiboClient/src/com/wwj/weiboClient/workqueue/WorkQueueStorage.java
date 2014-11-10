package com.wwj.weiboClient.workqueue;

import java.util.ArrayList;
import java.util.List;

/**
 * ������洢��
 * 
 * @author wwj
 * 
 */
public class WorkQueueStorage {
	// �洢�ļ����������List
	private List<String> webFileDoingList = new ArrayList<String>();
	// �洢ͨ�������List�� �������񶼱�����ParentTask������
	private List taskList = new ArrayList();

	// ��ȡ�ļ���������Ҫ���ص�URL)
	public List<String> getDoingWebFileUrls() {
		// ����ͬ��
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

	// ɾ���ļ����ض����е�����
	public void removeDoingWebFileUrl(List<String> list) {
		synchronized (webFileDoingList) {
			if (list != null)
				webFileDoingList.removeAll(list);

		}
	}

	// ���ļ����ض����������һ��Url��
	public void addDoingWebFileUrl(String url) {
		synchronized (webFileDoingList) {
			webFileDoingList.add(url);
		}
	}

	// Ŀǰ����ֻ�õ���һ���������һ�µõ����������Ҫ�޸�WorkQueueMonitor��Ĵ���
	// ��ȡһ��ͨ������
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

	// ɾ��ͨ������
	public void removeTask(List tasks) {
		synchronized (taskList) {
			if (taskList.size() > 0) {
				taskList.removeAll(tasks);
			}
		}
	}

	// ���ͨ������
	public void addTask(Object task) {
		synchronized (taskList) {
			taskList.add(task);
		}
	}
}
