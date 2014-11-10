package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.FollowsListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.User;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;

/**
 * 【查看粉丝列表】
 * 
 * @author wwj
 * 
 */
public class FollowsListViewer extends Activity implements Const,
		OnClickListener, OnRefreshListener {

	private Button backBtn;
	private PullToRefreshListView followsListview;
	private int weiboType;
	private long uid;
	private List<User> users;
	private View loadmoreView; // 加载更多按钮
	private TextView loadmoreText;
	private ProgressBar loadmoreBar;
	private boolean isLoadmore = false;

	private List<JSONObject> tencentUsers = new ArrayList<JSONObject>();

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (isLoadmore) {
				loadmoreText.setText("加载更多");
				loadmoreBar.setVisibility(View.GONE);
				isLoadmore = false;
			}
			List<User> usersList = (List<User>) msg.obj;
			FollowsListAdapter followsListAdapter = new FollowsListAdapter(
					FollowsListViewer.this, null, usersList, weiboType);
			followsListview.setAdapter(followsListAdapter);
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			String date = format.format(new Date());
			followsListview.onRefreshComplete(date);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.follows_list_viewer);
		backBtn = (Button) findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);
		followsListview = (PullToRefreshListView) findViewById(R.id.lv_follows);
		followsListview.setOnRefreshListener(this);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		loadmoreView = inflater.inflate(R.layout.loadmore_layout, null);
		followsListview.addFooterView(loadmoreView);
		loadmoreView.setOnClickListener(this);
		loadmoreText = (TextView) loadmoreView.findViewById(R.id.loadmore_text);
		loadmoreBar = (ProgressBar) loadmoreView
				.findViewById(R.id.loadmore_progress);

		weiboType = StorageManager.getValue(this, StringUtil.LOGIN_TYPE, 0);
		uid = StorageManager.getValue(this, StringUtil.SINA_UID,
				Long.valueOf(0));
		switch (weiboType) {
		case SINA:
			SinaWeiboManager.showFollows(this, uid, 0,
					new SinaFollowsRequestListener());
			break;
		case TENCENT:
			String accessToken = GlobalObject.getTencentAccessToken(this);
			TencentWeiboManager.getFriendsfansList(this, accessToken, callback);
			break;
		}
	}

	@Override
	public void onRefresh() {
		switch (weiboType) {
		case SINA:
			SinaWeiboManager.showFollows(this, uid, 0,
					new SinaFollowsRequestListener());
			break;
		case TENCENT:
			String accessToken = GlobalObject.getTencentAccessToken(this);
			TencentWeiboManager.getFriendsfansList(this, accessToken, callback);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.loadmore_layout:
			isLoadmore = true;
			loadmoreText.setText("加载中...");
			loadmoreBar.setVisibility(View.VISIBLE);
			switch (weiboType) {
			case SINA:
				SinaWeiboManager.showFollows(this, uid, 0,
						new SinaFollowsRequestListener());
				break;
			case TENCENT:
				String accessToken = GlobalObject.getTencentAccessToken(this);
				TencentWeiboManager.getFriendsfansList(this, accessToken,
						callback);
				break;
			}
			break;
		}
	}

	private class SinaFollowsRequestListener implements RequestListener {

		@Override
		public void onComplete(String response) {
			users = JSONAndObject.convert(User.class, response, "users");
			Message msg = new Message();
			msg.obj = users;
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

	private HttpCallback callback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult modelResult = (ModelResult) obj;
			try {
				String jsonResult = modelResult.getObj().toString();
				LogUtils.d(jsonResult);
				// 得到data对象
				JSONObject jsonObject = new JSONObject(jsonResult);
				JSONObject dataObject = jsonObject.getJSONObject("data");
				JSONArray jsonArray = dataObject.getJSONArray("info");
				int size = jsonArray.length();
				for (int i = 0; i < size; i++) {
					JSONObject infoObj = (JSONObject) jsonArray.opt(i);
					tencentUsers.add(infoObj);
				}

				FollowsListAdapter adapter = new FollowsListAdapter(
						FollowsListViewer.this, tencentUsers, null, weiboType);
				followsListview.setAdapter(adapter);
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日 HH:mm");
				String date = format.format(new Date());
				followsListview.onRefreshComplete(date);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};
}
