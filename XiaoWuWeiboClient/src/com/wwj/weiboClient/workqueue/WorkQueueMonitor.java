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
 * 【任务监视】
 * 
 * @author Lining 说明：除了任务存储外，还需要通过线程不断扫描保存的任务的List，一旦发现任务，立刻处理。
 *         这个工作交给WorkQueueMonitor类来完成
 */
public class WorkQueueMonitor implements Runnable, Const {
	private WorkQueueStorage storage; // 任务存储
	private Map<Integer, DoneAndProcess> doneAndProcessMap = new HashMap<Integer, DoneAndProcess>();
	private DoingAndProcess doingAndProcess; // 处理任务接口对象
	private Context context;
	private boolean stopFlag = false; // 停止标识
	private Thread thread; // 线程引用
	private int monitorType = MONITOR_TYPE_IMAGE; // 监视类型，默认为图片

	// 任务完成后，处理剩余的工作，主要是执行doneAndProcess方法
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			ParentTask parentTask = (ParentTask) msg.obj;
			// 如果任务本身有doneAndProcess方法，则执行此方法
			if (parentTask != null && parentTask.doneAndProcess != null) {
				parentTask.doneAndProcess.doneProcess(parentTask);
			} else { // 否则由构造方法传入的doneAndProcess
				Collection<DoneAndProcess> doneAndProcesses = doneAndProcessMap
						.values();
				for (DoneAndProcess doneAndProcess : doneAndProcesses) {
					doneAndProcess.doneProcess(parentTask);
				}
			}
		};
	};

	/**
	 * 构造函数
	 * 
	 * @param activity
	 *            窗口
	 * @param storage
	 *            任务存储对象
	 * @param doingAndProcess
	 *            正在处理的任务接口对象
	 * @param monitorType
	 *            监视类型
	 */
	public WorkQueueMonitor(Context context, WorkQueueStorage storage,
			DoingAndProcess doingAndProcess, int monitorType) {
		super();
		this.context = context;
		this.storage = storage;
		this.doingAndProcess = doingAndProcess;
		this.monitorType = monitorType;
	}

	// 开启线程
	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start(); // 启动线程
		}
	}

	// 停止线程
	public void stop() {
		stopFlag = true;
	}

	// 扫描文件下载任务队列
	private void imageScan() {
		LogUtils.i("+++ imageScan");
		// 获取文件下载任务
		List<String> webFileDoingList = storage.getDoingWebFileUrls();
		while (webFileDoingList != null) {
			try {
				// 处理文件下载任务
				doingAndProcess.doingProcess(webFileDoingList);
				handler.sendEmptyMessage(0);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 删除已处理完的任务
				storage.removeDoingWebFileUrl(webFileDoingList);
			}
			webFileDoingList = storage.getDoingWebFileUrls();
		}
	}

	// 扫描通用任务
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
		// 当线程未停止
		while (!stopFlag) {
			// 监视类型为图片
			if (monitorType == MONITOR_TYPE_IMAGE) {
				imageScan();
			} else if (monitorType == MONITOR_TYPE_TASK) {
				taskScan();
			}
			try {
				// 每200毫秒扫描一次任务队列
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
