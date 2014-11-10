package com.wwj.weiboClient.workqueue.task;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.wwj.weiboClient.workqueue.DoingAndProcess;
import com.wwj.weiboClient.workqueue.task.process.CommentWeiboProcess;
import com.wwj.weiboClient.workqueue.task.process.FavoriteWeiboProcess;
import com.wwj.weiboClient.workqueue.task.process.PostWeiboProcess;
import com.wwj.weiboClient.workqueue.task.process.RepostWeiboProcess;

/**
 * ��������������ࡿ
 * 
 * @author wwj
 * 
 */
public class TaskMan implements DoingAndProcess {

	private Context context;
	private PostWeiboProcess postWeiboProcess;
	private RepostWeiboProcess repostWeiboProcess;
	private CommentWeiboProcess commentWeiboProcess;
	private FavoriteWeiboProcess favoriteWeiboProcess;

	private Handler msgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String str = String.valueOf(msg.obj);
			Toast.makeText(context, str, Toast.LENGTH_LONG).show();
			super.handleMessage(msg);
		};
	};

	public TaskMan(Context context) {
		super();
		this.context = context;
		// �������������������
		postWeiboProcess = new PostWeiboProcess(context);
		repostWeiboProcess = new RepostWeiboProcess(context);
		favoriteWeiboProcess = new FavoriteWeiboProcess(context);
		commentWeiboProcess = new CommentWeiboProcess(context);
	}

	@Override
	public void doingProcess(List list) throws Exception {
		// ��ʼ�����������
		for (Object task : list) {
			// ������΢������
			if (task instanceof PostWeiboTask) {
				try {
					postWeiboProcess.process((PostWeiboTask) task);
					Message msg = new Message();
					msg.obj = "�ɹ�����΢��";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// ���������ļ�����
			} else if (task instanceof PullFileTask) {
				PullFileTask pullFileTask = (PullFileTask) task;
				PullFile pullFile = new PullFile();
				pullFile.doingProcess(pullFileTask.fileUrl);
				// ����ת��΢������
			} else if (task instanceof RepostWeiboTask) {
				try {
					repostWeiboProcess.process((RepostWeiboTask) task);
					Message msg = new Message();
					msg.obj = "�ɹ�ת��΢��";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// ��������΢������
			} else if (task instanceof CommentWeiboTask) {
				try {
					commentWeiboProcess.process((CommentWeiboTask) task);
					Message msg = new Message();
					msg.obj = "�ɹ�����΢��";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// �����ղ�΢������
			} else if (task instanceof FavoriteWeiboTask) {
				try {
					favoriteWeiboProcess.process((FavoriteWeiboTask) task);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
			}
		}
	}

	// ִ��������ʧ��ʱ���ø÷���
	private void exceptionProcess(Object task) {
		Message msg = new Message();
		// ������
		if (task instanceof PostWeiboTask) {
			msg.obj = "΢������ʧ��";
		} else if (task instanceof RepostWeiboTask) {
			msg.obj = "ת��΢��ʧ��";
		} else if (task instanceof CommentWeiboTask) {
			msg.obj = "����΢��ʧ��";
		} else if (task instanceof FavoriteWeiboTask) {
			msg.obj = "�ղ�΢��ʧ��";
		}
		msgHandler.sendMessage(msg);
	}

}
