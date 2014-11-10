package com.wwj.weiboClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.TencentWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;

/**
 * 显示微博界面（我的微博、我的收藏、热门评论、热门收藏等）
 * 
 * @author wwj
 * 
 */
public class TencentWeiboViewer extends Activity implements Const,
		OnClickListener, OnItemClickListener, OnRefreshListener {
	private TextView weiboTitle;
	private Button back;
	public static PullToRefreshListView weiboListView;
	private String title; // 标题
	private int type; // 数据类型
	private String name; // 用户名

	private String accessToken;
	private JSONArray jsonArray; // 腾讯微博json数组
	private LinearLayout list_footer; // 更多项
	private TextView tv_msg;
	private LinearLayout loading;
	private static int PAGE_SIZE = 20; // 每页显示的微博条数
	private int TOTAL_PAGE = 0; // 当前已经记录的微博页数
	// 腾讯微博数据列表
	public static List<JSONObject> tencentWeiboList = new ArrayList<JSONObject>();
	public static TencentWeiboListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("### weiboViewer onCreated");
		setContentView(R.layout.weibo_viewer);

		// 获取intent传过来的数据
		title = getIntent().getStringExtra("title");
		type = getIntent().getIntExtra("type", 0);
		name = getIntent().getStringExtra("name");
		accessToken = StorageManager.getValue(this,
				StringUtil.TENCENT_ACCESS_TOKEN, "");

		findViews();
		switch (type) {
		case MESSAGE_FAVORITE:
			TencentWeiboManager.getFavList(this, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					httpCallback);
			break;
		case USER_TIMELINE:
			TencentWeiboManager.getUserTimeline(this, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, name,
					StringUtil.REQUEST_FORMAT, httpCallback);
			break;
		}

	}

	private void findViews() {
		weiboTitle = (TextView) findViewById(R.id.tv_weibo_name);
		back = (Button) findViewById(R.id.btn_back);
		weiboListView = (PullToRefreshListView) findViewById(R.id.weibo_viewer_list);
		back.setOnClickListener(this);
		weiboListView.setOnRefreshListener(this);
		weiboListView.setOnItemClickListener(this);
		weiboTitle.setText(title); // 显示界面标题
		list_footer = (LinearLayout) LayoutInflater.from(this).inflate(
				R.layout.tencent_list_footer, null);
		tv_msg = (TextView) list_footer.findViewById(R.id.tv_msg);
		loading = (LinearLayout) list_footer.findViewById(R.id.loading);
		tv_msg.setOnClickListener(this);
		// weiboListView.addFooterView(list_footer);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back: // 返回
			finish();
			break;
		case R.id.tv_msg:
			TencentWeiboManager.getFavList(this, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					httpCallback);
			tv_msg.setVisibility(View.GONE);
			loading.setVisibility(View.VISIBLE);
			break;
		default:
			break;

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position < jsonArray.length()) {
			// 取得单条微博对象
			JSONObject weiboInfo = (JSONObject) jsonArray.opt(position - 1);
			Intent intent = new Intent(this, SingleWeiboViewer.class);
			try {
				intent.putExtra(StringUtil.WEIBO_TYPE, TENCENT);
				intent.putExtra("weiboid", weiboInfo.getString("id"));
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			listAdapter.showMoreAnim();
			TencentWeiboManager.getFavList(this, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					httpCallback);
		}

	}

	@Override
	public void onRefresh() {
		TencentWeiboManager.getFavList(this, accessToken, 0, (TOTAL_PAGE + 1)
				* PAGE_SIZE, StringUtil.REQUEST_FORMAT, httpCallback);
	}

	private HttpCallback httpCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null) {
				return;
			}
			String jsonRestult = result.getObj().toString();
			LogUtils.v(jsonRestult);

			try {
				JSONObject jsonObject = new JSONObject(jsonRestult);
				JSONObject dataObject = jsonObject.getJSONObject("data");
				jsonArray = dataObject.getJSONArray("info");

				if (jsonArray != null && jsonArray.length() > 0) {
					TOTAL_PAGE++;
					tencentWeiboList.clear();
					int length = jsonArray.length();
					for (int i = 0; i < length; i++) {
						JSONObject infoObject = jsonArray.optJSONObject(i);
						tencentWeiboList.add(infoObject);
					}
				}
				listAdapter = new TencentWeiboListAdapter(
						TencentWeiboViewer.this, tencentWeiboList);
				if (TOTAL_PAGE > 1) {
					listAdapter.notifyDataSetChanged();
				}
				if (TOTAL_PAGE == 1) {
					weiboListView.setAdapter(listAdapter);
				}
				weiboListView.setSelection((TOTAL_PAGE - 1) * PAGE_SIZE + 1);// 设置最新获取一页数据成功后显示数据的起始数据
				// loading.setVisibility(View.GONE); // 隐藏下方的进度条
				// tv_msg.setVisibility(View.VISIBLE); // 显示出“更多”
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日 HH:mm");
				String date = format.format(new Date());
				weiboListView.onRefreshComplete(date);
				listAdapter.hideMoreAnim();

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	};

}
