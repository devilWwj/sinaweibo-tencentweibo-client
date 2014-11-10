package com.wwj.weiboClient.workqueue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * ��������ӡ�
 * 
 * @author Lining ˵������������洢�⣬����Ҫͨ���̲߳���ɨ�豣��������List��һ�������������̴���
 *         �����������WorkQueueMonitor�������
 */
public class WorkQueueMonitor implements Runnable, Const {
	private WorkQueueStorage storage; // ����洢
	private Map<Integer, DoneAndProcess> doneAndProcessMap = new HashMap<Integer, DoneAndProcess>();
	private DoingAndProcess doingAndProcess; // ��������ӿڶ���
	private Context context;
	private boolean stopFlag = false; // ֹͣ��ʶ
	private Thread thread; // �߳�����
	private int monitorType = MONITOR_TYPE_IMAGE; // �������ͣ�Ĭ��ΪͼƬ

	// ������ɺ󣬴���ʣ��Ĺ�������Ҫ��ִ��doneAndProcess����
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ParentTask parentTask = (ParentTask) msg.obj;
			// �����������doneAndProcess��������ִ�д˷���
			if (parentTask != null && parentTask.doneAndProcess != null) {
				parentTask.doneAndProcess.doneProcess(parentTask);
			} else { // �����ɹ��췽�������doneAndProcess
				Collection<DoneAndProcess> doneAndProcesses = doneAndProcessMap
						.values();
				for (DoneAndProcess doneAndProcess : doneAndProcesses) {
					doneAndProcess.doneProcess(parentTask);
				}
			}
		};
	};

	/**
	 * ���캯��
	 * 
	 * @param activity
	 *            ����
	 * @param storage
	 *            ����洢����
	 * @param doingAndProcess
	 *            ���ڴ��������ӿڶ���
	 * @param monitorType
	 *            ��������
	 */
	public WorkQueueMonitor(Context context, WorkQueueStorage storage,
			DoingAndProcess doingAndProcess, int monitorType) {
		super();
		this.context = context;
		this.storage = storage;
		this.doingAndProcess = doingAndProcess;
		this.monitorType = monitorType;
	}

	// �����߳�
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start(); // �����߳�
		}
	}

	// ֹͣ�߳�
	public void stop() {
		stopFlag = true;
	}

	// ɨ���ļ������������
	private void imageScan() {
		LogUtils.i("+++ imageScan");
		// ��ȡ�ļ���������
		List<String> webFileDoingList = storage.getDoingWebFileUrls();
		while (webFileDoingList != null) {
			try {
				// �����ļ���������
				doingAndProcess.doingProcess(webFileDoingList);
				handler.sendEmptyMessage(0);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// ɾ���Ѵ����������
				storage.removeDoingWebFileUrl(webFileDoingList);
			}
			webFileDoingList = storage.getDoingWebFileUrls();
		}
	}

	// ɨ��ͨ������
	private void taskScan() {
		List taskList = storage.getDoingTasks();
		while (taskList != null) {
			try {
				doingAndProcess.doingProcess(taskList);
				Message msg = new Message();

				if (taskList.size() > 0) {
					msg.obj = taskList.get(0);
				}
				handler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				storage.removeTask(taskList);
			}
			taskList = storage.getDoingTasks();
		}
	}

	@Override
	public void run() {
		// ���߳�δֹͣ
		while (!stopFlag) {
			// ��������ΪͼƬ
			if (monitorType == MONITOR_TYPE_IMAGE) {
				imageScan();
			} else if (monitorType == MONITOR_TYPE_TASK) {
				taskScan();
			}
			try {
				// ÿ200����ɨ��һ���������
				Thread.sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	
	public void addDoneAndProcess(int type, DoneAndProcess doneAndProcess) {
		if (doneAndProcess != null) {
			doneAndProcessMap.put(type, doneAndProcess);
		}
	}

}
