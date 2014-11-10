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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.SinaCommentListAdapter;
import com.wwj.weiboClient.adapters.SinaWeiboListAdapter;
import com.wwj.weiboClient.adapters.TencentCommentListAdapter;
import com.wwj.weiboClient.adapters.TencentWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.library.WeiboData;
import com.wwj.weiboClient.listener.impl.StatusRequestListenerImpl;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.Comment;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;
import com.wwj.weiboClient.workqueue.WorkQueueMonitor;

/**
 * 【消息界面】
 * 
 * 获取回复我的消息和@我的消息
 * 
 * @author wwj
 * 
 */
public class MessageFragment extends Fragment implements Const,
		OnClickListener, OnRefreshListener, OnItemClickListener {

	private Button btnReplyMe, btnAtMe;
	private FrameLayout replyMeLayout, atMeLayout;
	public static PullToRefreshListView replyMeListView, atMeListView;
	public SinaCommentListAdapter commentAdapter;
	public static SinaWeiboListAdapter sinaMentionAdapter;
	public TencentWeiboListAdapter tencentMentionAdapter;

	private int weiboType;
	private Activity mContext;

	// 新浪微博数据
	private WeiboData weiboData;

	private View rootView;
	private boolean isReplyMeFirstTime = true;
	private boolean isAtMeFirstTime = true;

	public static ProgressBar replyMeProgressBar;
	public static ProgressBar atMeProgressBar;
	private boolean replyMeShowing = true;
	private boolean accountChange = false;

	public static WorkQueueMonitor imageWorkQueueMonitor;

	private List<Comment> comments;
	private List<Status> statuses;

	private String accessToken;
	// 腾讯微博评论列表数据
	private List<JSONObject> commentList = new ArrayList<JSONObject>();
	private JSONArray jsonArray;
	private TencentCommentListAdapter tencentCommentListAdapter;
	public static List<JSONObject> tencentWeiboList = new ArrayList<JSONObject>();
	private static int PAGE_SIZE = 20; // 每页显示的微博条数
	private int TOTAL_PAGE = 0; // 当前已经记录的微博页数

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.frg_message, container, false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		mContext = getActivity();
		// 得到微博类型
		weiboType = StorageManager.getValue(mContext, StringUtil.LOGIN_TYPE, 0);
		accessToken = StorageManager.getValue(mContext,
				StringUtil.TENCENT_ACCESS_TOKEN, "");
		findViews();
		registerBrocastReceiver();

		weiboData = new WeiboData(getActivity(), sinaMentionAdapter,
				MESSAGE_AT, atMeListView);
		// 图片工作队列监视器
		imageWorkQueueMonitor = Tools.getGlobalObject(getActivity())
				.getImageWorkQueueMonitor(mContext);

		return rootView;
	}

	// 查找控件
	private void findViews() {

		btnReplyMe = (Button) rootView.findViewById(R.id.btn_replyme);
		btnAtMe = (Button) rootView.findViewById(R.id.btn_atme);

		btnReplyMe.setOnClickListener(this);
		btnAtMe.setOnClickListener(this);

		replyMeLayout = (FrameLayout) rootView.findViewById(R.id.fl_replyme);
		atMeLayout = (FrameLayout) rootView.findViewById(R.id.fl_atme);
		replyMeListView = (PullToRefreshListView) rootView
				.findViewById(R.id.lv_replyme);
		atMeListView = (PullToRefreshListView) rootView
				.findViewById(R.id.lv_atme);

		replyMeListView.setOnRefreshListener(this);
		atMeListView.setOnRefreshListener(this);

		replyMeProgressBar = (ProgressBar) rootView
				.findViewById(R.id.pb_replyme);
		atMeProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_atme);
		atMeListView.setOnItemClickListener(this);

		// 显示回复我的
		showReplyMeView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_replyme:
			showReplyMeView();
			break;
		case R.id.btn_atme:
			showAtMeView();
			break;
		}
	}

	/** 显示@我的微博 **/
	private void showAtMeView() {
		replyMeShowing = false;
		btnAtMe.setBackgroundResource(R.drawable.topbar_bt);
		btnAtMe.setTextColor(0xffffffff);
		btnReplyMe.setBackgroundDrawable(null);
		btnReplyMe.setTextColor(0xff90afff);

		replyMeLayout.setVisibility(View.GONE);
		atMeLayout.setVisibility(View.VISIBLE);
		if (isAtMeFirstTime || accountChange) {
			atMeProgressBar.setVisibility(View.VISIBLE);
			if (weiboType == SINA) {
//				new Thread(new Runnable() {
//
//					@Override
//					public void run() {
//						statuses = StorageManager.loadList(getActivity(),
//								MESSAGE_AT);
//					}
//				}).start();
				if (statuses == null) {
					weiboData.loadSinaWeiboListData(mContext, MESSAGE_AT);
				} else {
					sinaMentionAdapter = new SinaWeiboListAdapter(
							getActivity(), statuses, MESSAGE_AT);
					atMeListView.setAdapter(sinaMentionAdapter);
					atMeProgressBar.setVisibility(View.GONE);
				}

			} else {
				TencentWeiboManager.getMetionsTimeline(mContext, accessToken,
						0, (TOTAL_PAGE + 1) * PAGE_SIZE,
						TencentWeiboManager.type_reply, mCallback);
			}
			isAtMeFirstTime = false;
		}

	}

	/** 显示回复我的 **/
	private void showReplyMeView() {
		replyMeShowing = true;
		btnReplyMe.setBackgroundResource(R.drawable.topbar_bt);
		btnReplyMe.setTextColor(0xffffffff);
		btnAtMe.setBackgroundDrawable(null);
		btnAtMe.setTextColor(0xff90afff);
		atMeLayout.setVisibility(View.GONE);
		replyMeLayout.setVisibility(View.VISIBLE);

		if (isReplyMeFirstTime || accountChange) {
			replyMeProgressBar.setVisibility(View.VISIBLE);
			if (weiboType == SINA) {
				// new Thread(new Runnable() {
				//
				// @Override
				// public void run() {
				// comments = StorageManager.loadList(mContext,
				// MESSAGE_COMMENT);
				// }
				// }).start();

				if (comments == null) {
					SinaWeiboManager.getCommentTimeline(mContext,
							new CommentRequestListenerImpl());
				} else {
					commentAdapter = new SinaCommentListAdapter(mContext,
							comments);
					replyMeListView.setAdapter(commentAdapter);
					replyMeProgressBar.setVisibility(View.GONE);
				}
			} else {
				TencentWeiboManager.getMetionsTimeline(mContext, accessToken,
						0, 30, TencentWeiboManager.type_comment, mCallback);
			}
			isReplyMeFirstTime = false;
		}

	}

	@Override
	public void onRefresh() {
		switch (weiboType) {
		case SINA:
			if (replyMeShowing) {
				SinaWeiboManager.getCommentTimeline(mContext,
						new CommentRequestListenerImpl());
			} else {
				weiboData.loadSinaWeiboListData(mContext, MESSAGE_AT);
			}
			break;
		case TENCENT:
			if (replyMeShowing) {
				TencentWeiboManager.getMetionsTimeline(mContext, accessToken,
						0, 30, TencentWeiboManager.type_comment, mCallback);
			} else {
				TencentWeiboManager.getMetionsTimeline(mContext, accessToken,
						0, (TOTAL_PAGE + 1) * PAGE_SIZE,
						TencentWeiboManager.type_mention, mCallback);
			}
			break;
		}
	}

	/**
	 * 新浪回复我的请求
	 * 
	 * @author wwj
	 * 
	 */
	private class CommentRequestListenerImpl implements RequestListener {
		private SinaCommentListAdapter adapter;

		private Handler handler = new Handler() {
			@Override
			public void handleMessage(android.os.Message msg) {
				if (replyMeListView.getAdapter() != null) {
					commentAdapter.putComments((List<Comment>) msg.obj);
				}
				replyMeListView.setAdapter(commentAdapter);

				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日  HH:mm");
				String date = format.format(new Date());
				replyMeListView.onRefreshComplete(date);
				replyMeProgressBar.setVisibility(View.GONE);
			};
		};

		@Override
		public void onComplete(String response) {
			List<Comment> comments = JSONAndObject.convert(Comment.class,
					response, "comments");
			LogUtils.d("###comments--->" + comments);
			if (commentAdapter != null) {
				adapter = commentAdapter;
			} else {
				adapter = new SinaCommentListAdapter(mContext, comments);
				commentAdapter = adapter;
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

	// 注册广播接收器
	public void registerBrocastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(StringUtil.ACCOUNT_CHANGE);
		// 注册广播
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			weiboType = intent.getIntExtra(StringUtil.WEIBO_TYPE, 0);
			accountChange = true;
			if (action.equals(StringUtil.ACCOUNT_CHANGE)) {
				if (replyMeShowing) {
					replyMeProgressBar.setVisibility(View.VISIBLE);
				} else {
					atMeProgressBar.setVisibility(View.VISIBLE);
				}
				if (weiboType == SINA) {
					SinaWeiboManager.getCommentTimeline(mContext,
							new CommentRequestListenerImpl());
					weiboData = new WeiboData(getActivity(),
							sinaMentionAdapter, MESSAGE_AT, atMeListView);
					weiboData.loadSinaWeiboListData(mContext, MESSAGE_AT);
				} else {
					if (replyMeShowing) {
						TencentWeiboManager.getMetionsTimeline(mContext,
								accessToken, 0, 30,
								TencentWeiboManager.type_comment, mCallback);
					} else {
						TencentWeiboManager.getMetionsTimeline(mContext,
								accessToken, 0, (TOTAL_PAGE + 1) * PAGE_SIZE,
								TencentWeiboManager.type_reply, mCallback);
					}
				}
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = null;
		switch (parent.getId()) {
		case R.id.lv_atme:
			if (weiboType == SINA) {
				// 按更多项
				if (position == sinaMentionAdapter.getCount()) {
					long maxId = sinaMentionAdapter.getMinId() - 1;
					SinaWeiboManager
							.getMentions(mContext, 0, maxId,
									new StatusRequestListenerImpl(mContext,
											MESSAGE_AT));
					sinaMentionAdapter.showMoreAnim(); // 显示更多动画
				} else { // 点击列表项
					Status status = sinaMentionAdapter.getStatus(position - 1);
					if (status != null) {
						intent = new Intent(mContext, SingleWeiboViewer.class);
						SingleWeiboViewer.status = status;
						startActivity(intent);
					}
				}
			} else {
				tencentListItemClick(position - 1);
			}

			break;
		}
	}

	private void tencentListItemClick(int position) {
		if (position < jsonArray.length()) {
			// 取得单条微博对象
			JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
			Intent intent = new Intent(mContext, SingleWeiboViewer.class);
			try {
				intent.putExtra(StringUtil.WEIBO_TYPE, TENCENT);
				intent.putExtra("weiboid", weiboInfo.getString("id"));
				mContext.startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			tencentMentionAdapter.showMoreAnim();
			TencentWeiboManager.getMetionsTimeline(mContext, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE,
					TencentWeiboManager.type_reply, mCallback);
		}

	}

	private HttpCallback mCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null || result.getObj() == null) {
				return;
			}
			String jsonRestult = result.getObj().toString();
			LogUtils.v(jsonRestult);

			try {
				JSONObject jsonObject = new JSONObject(jsonRestult);
				JSONObject dataObject = jsonObject.getJSONObject("data");
				jsonArray = dataObject.getJSONArray("info");
				int length = jsonArray.length();
				if (replyMeShowing) {
					for (int i = 0; i < length; i++) {
						JSONObject infoObject = jsonArray.optJSONObject(i);
						commentList.add(infoObject);
					}
					tencentCommentListAdapter = new TencentCommentListAdapter(
							mContext, commentList);
					replyMeListView.setAdapter(tencentCommentListAdapter);

				} else {
					if (jsonArray != null && length > 0) {
						TOTAL_PAGE++;
						tencentWeiboList.clear();
						for (int i = 0; i < length; i++) {
							JSONObject infoObject = jsonArray.optJSONObject(i);
							tencentWeiboList.add(infoObject);
						}
					}
					tencentMentionAdapter = new TencentWeiboListAdapter(
							mContext, tencentWeiboList);
					if (TOTAL_PAGE > 1) {
						tencentMentionAdapter.notifyDataSetChanged();
					}
					if (TOTAL_PAGE == 1) {
						atMeListView.setAdapter(tencentMentionAdapter);
						atMeListView.setSelection((TOTAL_PAGE - 1) * PAGE_SIZE
								+ 1);// 设置最新获取一页数据成功后显示数据的起始数据
					}
					tencentMentionAdapter.hideMoreAnim();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日  HH:mm");
				String date = format.format(new Date());
				replyMeListView.onRefreshComplete(date);
				replyMeProgressBar.setVisibility(View.GONE);
				atMeListView.onRefreshComplete(date);
				atMeProgressBar.setVisibility(View.GONE);

			}
		}
	};
}
