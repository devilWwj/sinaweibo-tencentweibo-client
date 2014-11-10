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
 * ��ʾ΢�����棨�ҵ�΢�����ҵ��ղء��������ۡ������ղصȣ�
 * 
 * @author wwj
 * 
 */
public class TencentWeiboViewer extends Activity implements Const,
		OnClickListener, OnItemClickListener, OnRefreshListener {
	private TextView weiboTitle;
	private Button back;
	public static PullToRefreshListView weiboListView;
	private String title; // ����
	private int type; // ��������
	private String name; // �û���

	private String accessToken;
	private JSONArray jsonArray; // ��Ѷ΢��json����
	private LinearLayout list_footer; // ������
	private TextView tv_msg;
	private LinearLayout loading;
	private static int PAGE_SIZE = 20; // ÿҳ��ʾ��΢������
	private int TOTAL_PAGE = 0; // ��ǰ�Ѿ���¼��΢��ҳ��
	// ��Ѷ΢�������б�
	public static List<JSONObject> tencentWeiboList = new ArrayList<JSONObject>();
	public static TencentWeiboListAdapter listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("### weiboViewer onCreated");
		setContentView(R.layout.weibo_viewer);

		// ��ȡintent������������
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
		weiboTitle.setText(title); // ��ʾ�������
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
		case R.id.btn_back: // ����
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
			// ȡ�õ���΢������
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
				weiboListView.setSelection((TOTAL_PAGE - 1) * PAGE_SIZE + 1);// �������»�ȡһҳ���ݳɹ�����ʾ���ݵ���ʼ����
				// loading.setVisibility(View.GONE); // �����·��Ľ�����
				// tv_msg.setVisibility(View.VISIBLE); // ��ʾ�������ࡱ
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy��MM��dd�� HH:mm");
				String date = format.format(new Date());
				weiboListView.onRefreshComplete(date);
				listAdapter.hideMoreAnim();

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	};

}
