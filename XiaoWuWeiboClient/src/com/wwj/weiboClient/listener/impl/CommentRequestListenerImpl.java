package com.wwj.weiboClient.listener.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.wwj.weiboClient.MessageFragment;
import com.wwj.weiboClient.R;
import com.wwj.weiboClient.adapters.SinaCommentListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.model.Comment;
import com.wwj.weiboClient.util.LogUtils;

/**
 * 新浪评论请求
 * 
 * @author wwj
 * 
 */
public class CommentRequestListenerImpl implements RequestListener, Const {
	private SinaCommentListAdapter adapter;
	private MessageFragment message;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (message.replyMeListView.getAdapter() != null) {
				adapter.putComments((List<Comment>) msg.obj);
			}
			message.replyMeListView.setAdapter(adapter);

			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
			String date = format.format(new Date());
			message.replyMeListView.onRefreshComplete(date);
		};
	};

	public CommentRequestListenerImpl(MessageFragment message) {
		super();
		this.message = message;
	}

	@Override
	public void onComplete(String response) {
		List<Comment> comments = JSONAndObject.convert(Comment.class, response,
				"comments");
		LogUtils.d("###comments--->" + comments);
		if (message.commentAdapter != null) {
			adapter = message.commentAdapter;
		} else {
			adapter = new SinaCommentListAdapter(message.getActivity(), comments);
			message.commentAdapter = adapter;
		}
		Message msg = new Message();
		msg.obj = comments;
		handler.sendMessage(msg);
	}

	@Override
	public void onComplete4binary(ByteArrayOutputStream responseOS) {

	}

	@Override
	public void onIOException(IOException e) {
		e.printStackTrace();
	}

	@Override
	public void onError(WeiboException e) {
		e.printStackTrace();
	}

}
