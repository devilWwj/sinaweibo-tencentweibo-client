package com.wwj.weiboClient.listener.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.wwj.weiboClient.HomeFragment;
import com.wwj.weiboClient.MessageFragment;
import com.wwj.weiboClient.SinaWeiboViewer;
import com.wwj.weiboClient.adapters.SinaWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.model.Favorite;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.Tools;

public class StatusRequestListenerImpl implements RequestListener, Const {

	private SinaWeiboListAdapter weiboListAdapter;
	private Activity context;
	private int type;

	private List<Status> statuses;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			String date = format.format(new Date());
			weiboListAdapter.putStatuses((List<Status>) msg.obj);
			weiboListAdapter.hideMoreAnim();
			switch (type) {
			case HOME: // 微博首页
				if (HomeFragment.sinaWeiboListView.getAdapter() == null) {
					HomeFragment.sinaWeiboListView.setAdapter(weiboListAdapter);
					if (HomeFragment.imageWorkQueueMonitor == null) {
						HomeFragment.imageWorkQueueMonitor =Tools.getGlobalObject(context)
								.getImageWorkQueueMonitor(context);
						HomeFragment.imageWorkQueueMonitor.addDoneAndProcess(HOME,
								weiboListAdapter);
					}
				}
				HomeFragment.sinaWeiboListView.onRefreshComplete(date);
				HomeFragment.loadingView.setVisibility(View.GONE);
				break;
			case MESSAGE_FAVORITE: // 收藏
				if (SinaWeiboViewer.weiboListView.getAdapter() == null) {
					SinaWeiboViewer.weiboListView.setAdapter(weiboListAdapter);
					SinaWeiboViewer.imageWorkQueueMonitor.addDoneAndProcess(
							MESSAGE_FAVORITE, weiboListAdapter);
				}
				SinaWeiboViewer.weiboListView.onRefreshComplete(date);
				SinaWeiboViewer.loadingView.setVisibility(View.GONE);
				break;
			case USER_TIMELINE: // 用户微博
				if (SinaWeiboViewer.weiboListView.getAdapter() == null) {
					SinaWeiboViewer.weiboListView.setAdapter(weiboListAdapter);
					SinaWeiboViewer.imageWorkQueueMonitor.addDoneAndProcess(
							USER_TIMELINE, weiboListAdapter);
				}
				SinaWeiboViewer.weiboListView.onRefreshComplete(date);
				SinaWeiboViewer.loadingView.setVisibility(View.GONE);
				break;
			case MESSAGE_AT: // @我的
				if (MessageFragment.atMeListView.getAdapter() == null) {
					MessageFragment.atMeListView.setAdapter(weiboListAdapter);
					MessageFragment.imageWorkQueueMonitor.addDoneAndProcess(
							MESSAGE_AT, weiboListAdapter);
				}
				SinaWeiboViewer.loadingView.setVisibility(View.GONE);
				break;
			}
		};
	};

	public StatusRequestListenerImpl(Activity context, int type) {
		super();
		this.context = context;
		this.type = type;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onComplete(String response) {
		Log.v("response", response);

		statuses = JSONAndObject.convert(Status.class, response, "statuses");
		switch (type) {
		case HOME: // 首页微博
			if (HomeFragment.sinaWeiboListAdapter != null) {
				weiboListAdapter = HomeFragment.sinaWeiboListAdapter;
			} else {
				weiboListAdapter = new SinaWeiboListAdapter(context, null, type);
				HomeFragment.sinaWeiboListAdapter = weiboListAdapter;
			}
			break;
		case USER_TIMELINE: // 用户微博
			if (SinaWeiboViewer.sinaWeiboListAdapter != null) {
				weiboListAdapter = SinaWeiboViewer.sinaWeiboListAdapter;
			} else {
				weiboListAdapter = new SinaWeiboListAdapter(context, null, type);
				SinaWeiboViewer.sinaWeiboListAdapter = weiboListAdapter;
			}
			break;
		case MESSAGE_FAVORITE: // 收藏
			if (SinaWeiboViewer.sinaWeiboListAdapter != null) {
				weiboListAdapter = SinaWeiboViewer.sinaWeiboListAdapter;
			} else {
				weiboListAdapter = new SinaWeiboListAdapter(context, null, type);
				SinaWeiboViewer.sinaWeiboListAdapter = weiboListAdapter;
			}
			List<Favorite> favorites = null;
			favorites = JSONAndObject.convert(Favorite.class, response,
					"favorites");
			statuses = SinaWeiboManager.FavoriteToStatus(favorites);
			break;
		case MESSAGE_AT:
			if (MessageFragment.sinaMentionAdapter != null) {
				weiboListAdapter = MessageFragment.sinaMentionAdapter;
			} else {
				weiboListAdapter = new SinaWeiboListAdapter(context, null, type);
				MessageFragment.sinaMentionAdapter = weiboListAdapter;
			}
			break;
		}
		Message msg = new Message();
		msg.obj = statuses;
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
