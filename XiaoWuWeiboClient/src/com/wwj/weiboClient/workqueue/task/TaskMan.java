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
 * 【处理任务的中枢】
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
		// 创建各种任务处理类对象
		postWeiboProcess = new PostWeiboProcess(context);
		repostWeiboProcess = new RepostWeiboProcess(context);
		favoriteWeiboProcess = new FavoriteWeiboProcess(context);
		commentWeiboProcess = new CommentWeiboProcess(context);
	}

	@Override
	public void doingProcess(List list) throws Exception {
		// 开始处理各种任务
		for (Object task : list) {
			// 处理发布微博任务
			if (task instanceof PostWeiboTask) {
				try {
					postWeiboProcess.process((PostWeiboTask) task);
					Message msg = new Message();
					msg.obj = "成功发布微博";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// 处理下载文件任务
			} else if (task instanceof PullFileTask) {
				PullFileTask pullFileTask = (PullFileTask) task;
				PullFile pullFile = new PullFile();
				pullFile.doingProcess(pullFileTask.fileUrl);
				// 处理转发微博任务
			} else if (task instanceof RepostWeiboTask) {
				try {
					repostWeiboProcess.process((RepostWeiboTask) task);
					Message msg = new Message();
					msg.obj = "成功转发微博";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// 处理评论微博任务
			} else if (task instanceof CommentWeiboTask) {
				try {
					commentWeiboProcess.process((CommentWeiboTask) task);
					Message msg = new Message();
					msg.obj = "成功评论微博";
					msgHandler.sendMessage(msg);
				} catch (Exception e) {
					exceptionProcess(task);
					throw e;
				}
				// 处理收藏微博任务
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

	// 执行任务类失败时调用该方法
	private void exceptionProcess(Object task) {
		Message msg = new Message();
		// 任务处理
		if (task instanceof PostWeiboTask) {
			msg.obj = "微博发布失败";
		} else if (task instanceof RepostWeiboTask) {
			msg.obj = "转发微博失败";
		} else if (task instanceof CommentWeiboTask) {
			msg.obj = "评论微博失败";
		} else if (task instanceof FavoriteWeiboTask) {
			msg.obj = "收藏微博失败";
		}
		msgHandler.sendMessage(msg);
	}

}
