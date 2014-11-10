package com.wwj.weiboClient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.wwj.weiboClient.adapters.SinaWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.listener.impl.StatusRequestListenerImpl;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.model.User;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;
import com.wwj.weiboClient.workqueue.WorkQueueMonitor;

/**
 * ��ʾ΢�����棨�ҵ�΢�����ҵ��ղء��������ۡ������ղصȣ�
 * 
 * @author wwj
 * 
 */
public class SinaWeiboViewer extends Activity implements Const,
		OnClickListener, OnItemClickListener, OnRefreshListener {
	private TextView weiboTitle;
	private Button back;
	public static PullToRefreshListView weiboListView;
	private int weiboType; // ΢������
	private String title; // ����
	private int type; // ��������

	public static SinaWeiboListAdapter sinaWeiboListAdapter;
	public static WorkQueueMonitor imageWorkQueueMonitor;
	public WorkQueueMonitor taskWorkQueueMonitor;
	public User user;

	private static int page = 0;
	public static View loadingView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("### weiboViewer onCreated");
		setContentView(R.layout.weibo_viewer);

		// ��ȡintent������������
		title = getIntent().getStringExtra("title");
		type = getIntent().getIntExtra("type", 0);

		findViews();
		List<Status> statuses = new ArrayList<Status>();
		switch (type) {
		case MESSAGE_FAVORITE: // �ղ�
			statuses = StorageManager.loadList(this, MESSAGE_FAVORITE);
			if (statuses == null) {
				SinaWeiboManager.getFavorites(this, 1,
						new StatusRequestListenerImpl(this, MESSAGE_FAVORITE));
			} else {
				sinaWeiboListAdapter = new SinaWeiboListAdapter(this, statuses,
						MESSAGE_FAVORITE);
				weiboListView.setAdapter(sinaWeiboListAdapter);
				loadingView.setVisibility(View.GONE);
			}
			imageWorkQueueMonitor = Tools.getGlobalObject(this)
					.getImageWorkQueueMonitor(this);
			taskWorkQueueMonitor = Tools.getGlobalObject(this)
					.getTaskWorkQueueMonitor(this);
			imageWorkQueueMonitor.addDoneAndProcess(MESSAGE_FAVORITE,
					sinaWeiboListAdapter);
			break;
		case USER_TIMELINE: // �û�΢��
			statuses = StorageManager.loadList(this, USER_TIMELINE);
			if (statuses == null) {
				SinaWeiboManager.getUserTimeline(this, 0, 0,
						new StatusRequestListenerImpl(this, USER_TIMELINE));
			} else {
				sinaWeiboListAdapter = new SinaWeiboListAdapter(this, statuses,
						USER_TIMELINE);
				weiboListView.setAdapter(sinaWeiboListAdapter);
				loadingView.setVisibility(View.GONE);
			}

			imageWorkQueueMonitor = Tools.getGlobalObject(this)
					.getImageWorkQueueMonitor(this);
			taskWorkQueueMonitor = Tools.getGlobalObject(this)
					.getTaskWorkQueueMonitor(this);
			imageWorkQueueMonitor.addDoneAndProcess(USER_TIMELINE,
					sinaWeiboListAdapter);
			break;
		default:
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
		loadingView = (View) findViewById(R.id.lodingprogress);
		loadingView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back: // ����
			finish();
			break;
		default:
			break;

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = null;
		switch (parent.getId()) {
		case R.id.weibo_viewer_list:
			// ��������
			if (position == sinaWeiboListAdapter.getCount()) {
				long maxId = sinaWeiboListAdapter.getMinId() - 1;
				switch (type) {
				case MESSAGE_AT:
					SinaWeiboManager.getMentions(this, 0, maxId,
							new StatusRequestListenerImpl(this, MESSAGE_AT));
					break;
				case MESSAGE_FAVORITE:
					page++;
					SinaWeiboManager.getFavorites(this, page,
							new StatusRequestListenerImpl(this,
									MESSAGE_FAVORITE));
					break;
				case USER_TIMELINE:
					SinaWeiboManager.getUserTimeline(this, 0, maxId,
							new StatusRequestListenerImpl(this, USER_TIMELINE));
					break;
				}
				sinaWeiboListAdapter.showMoreAnim();// ��ʾ���ද��
			} else { // ����б���
				Status status = sinaWeiboListAdapter.getStatus(position - 1);
				if (status != null) {
					intent = new Intent(this, SingleWeiboViewer.class);
					SingleWeiboViewer.status = status;
					startActivity(intent);
				}
			}
			break;
		}
	}

	@Override
	public void onRefresh() {
		getweibos();
	}

	/** �������������첽��ȡ���� **/
	private void getweibos() {
		switch (weiboType) {
		case SINA:
			switch (type) {
			case MESSAGE_FAVORITE: // �ղ�
				SinaWeiboManager.getFavorites(this, 1,
						new StatusRequestListenerImpl(this, MESSAGE_FAVORITE));
				break;
			case USER_TIMELINE:
				SinaWeiboManager.getUserTimeline(this, 0, 0,
						new StatusRequestListenerImpl(this, USER_TIMELINE));
				break;
			default:
				break;
			}
			break;
		case TENCENT:
			break;
		}

	}

}
