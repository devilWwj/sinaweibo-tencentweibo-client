package com.wwj.weiboClient.library;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.wwj.weiboClient.HomeFragment;
import com.wwj.weiboClient.MessageFragment;
import com.wwj.weiboClient.adapters.SinaWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.LogUtils;

/**
 * 【用于加载微博数据，显示到ListView】
 * 
 * @author wwj
 * 
 */
public class WeiboData implements Const {

	public List<Status> statuses;
	private ListView listView;
	private int type;
	private Activity context;
	private SinaWeiboListAdapter adapter;

	public WeiboData(Activity context, SinaWeiboListAdapter adapter, int type,
			ListView listView) {
		this.context = context;
		this.adapter = adapter;
		this.type = type;
		this.listView = listView;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			String date = format.format(new Date());
			statuses = (List<Status>) msg.obj;
			adapter = new SinaWeiboListAdapter(context, statuses, type);
			switch (type) {
			case HOME:
				HomeFragment.sinaWeiboListAdapter = adapter;
				HomeFragment.sinaWeiboListView.onRefreshComplete(date);
				HomeFragment.loadingView.setVisibility(View.GONE);
				break;
			case MESSAGE_AT:
				MessageFragment.sinaMentionAdapter = adapter;
				MessageFragment.atMeListView.onRefreshComplete(date);
				MessageFragment.atMeProgressBar.setVisibility(View.GONE);
				break;
			default:
				break;
			}
			listView.setAdapter(adapter);
		};
	};

	public void loadSinaWeiboListData(Context context, int type) {
		switch (type) {
		case HOME:
			SinaWeiboManager.getHomeTimeline(context, 0, 0,
					DEFAULT_STATUS_COUNT, listener);
			break;
		case MESSAGE_AT:
			SinaWeiboManager.getMentions(context, 0, 0, listener);
			break;
		}
	}

	private RequestListener listener = new RequestListener() {

		@Override
		public void onIOException(IOException e) {
			e.printStackTrace();
		}

		@Override
		public void onError(WeiboException e) {
			e.printStackTrace();
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream responseOS) {

		}

		@Override
		public void onComplete(String response) {
			LogUtils.v(response);
			statuses = JSONAndObject
					.convert(Status.class, response, "statuses");
			Message msg = new Message();
			msg.obj = statuses;
			handler.sendMessage(msg);
		}
	};
}
