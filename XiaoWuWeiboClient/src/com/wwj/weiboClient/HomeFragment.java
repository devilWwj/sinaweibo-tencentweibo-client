package com.wwj.weiboClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.youmi.android.banner.AdSize;
import net.youmi.android.banner.AdView;
import net.youmi.android.banner.AdViewListener;

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
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.SinaWeiboListAdapter;
import com.wwj.weiboClient.adapters.TencentWeiboListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.WeiboData;
import com.wwj.weiboClient.listener.impl.StatusRequestListenerImpl;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.WorkQueueMonitor;
import com.wwj.weiboClient.workqueue.task.FavoriteWeiboTask;
import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * 【首页微博】
 * 
 * 注：新浪微博处理方式与腾讯微博处理方式是不一样的 比如新浪用到任务队列来执行任务，而腾讯微博并没有用得到任务队列
 * 至于为什么要用不同的处理机制，主要是新浪和腾讯返回的数据结构不一样，无法统一处理
 * 
 * @author wwj
 * 
 */
public class HomeFragment extends Fragment implements Const, OnRefreshListener,
		OnClickListener, OnItemClickListener, OnItemLongClickListener,
		OnMenuItemClickListener, DoneAndProcess {
	private Button openSlidingMenu;
	private Button postWeibo;
	public static PullToRefreshListView sinaWeiboListView;
	public static PullToRefreshListView tencentWeiboListView;
	public static View loadingView; // 加载进度条

	public static Activity mContext;
	public static SinaWeiboListAdapter sinaWeiboListAdapter;

	// 腾讯微博数据列表
	public static List<JSONObject> tencentWeiboList = new ArrayList<JSONObject>();
	public static TencentWeiboListAdapter listAdapter;

	private int weiboType;
	private WeiboData weiboData;
	private List<Status> statuses;

	private int position;
	public static WorkQueueMonitor imageWorkQueueMonitor;
	public static WorkQueueMonitor taskWorkQueueMonitor;

	private View rootView; // 缓存Fragment View
	private boolean isFirstTime = true;;

	private String accessToken;
	private JSONArray jsonArray; // 腾讯微博json数组
	private LinearLayout list_footer; // 更多项
	private TextView tv_msg;
	private LinearLayout loading;
	private static int PAGE_SIZE = 20; // 每页显示的微博条数
	private int TOTAL_PAGE = 0; // 当前已经记录的微博页数

	private boolean isFav = false; // 是否收藏

	private final int ACTION_REFRESH = 0;
	private final int ACTION_MORE = 1;
	private int actionState = ACTION_REFRESH;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// 新浪的收藏任务
			if (msg.obj instanceof FavoriteWeiboTask) {
				FavoriteWeiboTask favoriteWeiboTask = (FavoriteWeiboTask) msg.obj;
				if (favoriteWeiboTask.fav) {
					ToastUtil.showShortToast(mContext, "收藏微博成功");
				} else {
					ToastUtil.showShortToast(mContext, "取消收藏成功");
				}
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.frg_home, null);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}

		mContext = getActivity();
		// 获取微博类型
		weiboType = StorageManager.getValue(mContext, StringUtil.LOGIN_TYPE, 0);
		registerBrocastReceiver();
		findViews(rootView);
		accessToken = StorageManager.getValue(mContext,
				StringUtil.TENCENT_ACCESS_TOKEN, "");
		// 如果是第一次加载这个页面
		if (isFirstTime) {
			showData();
		}

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void showData() {
		if (weiboType == SINA) { // 新浪微博
			loadingView.setVisibility(View.VISIBLE);
			sinaWeiboListView.setVisibility(View.VISIBLE);
			tencentWeiboListView.setVisibility(View.GONE);
			// 从sd卡当中读取数据
			new Thread(new Runnable() {

				@Override
				public void run() {
					statuses = StorageManager.loadList(getActivity(), HOME);
				}
			}).start();

			if (statuses == null) {
				weiboData = new WeiboData(getActivity(), sinaWeiboListAdapter,
						HOME, sinaWeiboListView);
				weiboData.loadSinaWeiboListData(mContext, HOME);
			} else { // 如果有存在了直接显示数据
				sinaWeiboListAdapter = new SinaWeiboListAdapter(getActivity(),
						statuses, HOME);
				sinaWeiboListView.setAdapter(sinaWeiboListAdapter);
				loadingView.setVisibility(View.GONE);
			}

			// 启动图像、普通任务队列监视器
			imageWorkQueueMonitor = Tools.getGlobalObject(getActivity())
					.getImageWorkQueueMonitor(mContext);
			taskWorkQueueMonitor = Tools.getGlobalObject(getActivity())
					.getTaskWorkQueueMonitor(mContext);
			imageWorkQueueMonitor.addDoneAndProcess(HOME, sinaWeiboListAdapter);

		} else {
			loadingView.setVisibility(View.VISIBLE);
			sinaWeiboListView.setVisibility(View.GONE);
			tencentWeiboListView.setVisibility(View.VISIBLE);
			try {
				// 腾讯微博
				TencentWeiboManager.getHomeTimeline(getActivity()
						.getApplicationContext(), accessToken, 0,
						(TOTAL_PAGE + 1) * PAGE_SIZE,
						StringUtil.REQUEST_FORMAT, getTimelineCallback);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		isFirstTime = false;

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	private void findViews(View parentView) {
		openSlidingMenu = (Button) parentView.findViewById(R.id.btn_open_menu);
		postWeibo = (Button) parentView.findViewById(R.id.btn_post_weibo);
		openSlidingMenu.setOnClickListener(clickListener);
		postWeibo.setOnClickListener(clickListener);

		sinaWeiboListView = (PullToRefreshListView) parentView
				.findViewById(R.id.sina_weibolist);
		tencentWeiboListView = (PullToRefreshListView) parentView
				.findViewById(R.id.tencent_weibolist);
		loadingView = (View) parentView.findViewById(R.id.lodingprogress);

		// 注册上下文菜单
		registerForContextMenu(sinaWeiboListView);
		registerForContextMenu(tencentWeiboListView);
		sinaWeiboListView.setOnRefreshListener(this);
		sinaWeiboListView.setOnItemClickListener(this);
		sinaWeiboListView.setOnItemLongClickListener(this);
		tencentWeiboListView.setOnRefreshListener(this);
		tencentWeiboListView.setOnItemClickListener(this);
		tencentWeiboListView.setOnItemLongClickListener(this);
		list_footer = (LinearLayout) LayoutInflater.from(mContext).inflate(
				R.layout.tencent_list_footer, null);
		tv_msg = (TextView) list_footer.findViewById(R.id.tv_msg);
		loading = (LinearLayout) list_footer.findViewById(R.id.loading);
		tv_msg.setOnClickListener(this);

		// 广告条接口调用
		// 江广告条adView添加到需要展示的layout控件中
		LinearLayout adLayout = (LinearLayout) parentView
				.findViewById(R.id.adLayout);
		AdView adView = new AdView(mContext, AdSize.FIT_SCREEN);
		adLayout.addView(adView);
		// 监听广告条接口
		adView.setAdListener(new AdViewListener() {

			@Override
			public void onSwitchedAd(AdView arg0) {
				Log.i("YoumiSample", "广告条切换");

			}

			@Override
			public void onReceivedAd(AdView arg0) {
				Log.i("YoumiSample", "请求广告成功");

			}

			@Override
			public void onFailedToReceivedAd(AdView arg0) {
				Log.i("YoumiSample", "请求广告失败");

			}
		});
		// // SmartBanner初始化接口，在应用开启的时候调用一次即可
		// SmartBannerManager.init(mContext);
		// // 调用飘窗
		// SmartBannerManager.show(mContext);

	}

	private OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.btn_open_menu: // 打开滑动菜单
				FragmentBottomTab.sm.toggle(true);
				break;
			case R.id.btn_post_weibo: // 发布微博
				intent = new Intent(getActivity(), PostWeibo.class);
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				startActivity(intent);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 腾讯获取首页微博回调接口
	 */
	public HttpCallback getTimelineCallback = new HttpCallback() {

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

				if (jsonArray != null && jsonArray.length() > 0) {
					TOTAL_PAGE++;
					tencentWeiboList.clear();
					int length = jsonArray.length();
					for (int i = 0; i < length; i++) {
						JSONObject infoObject = jsonArray.optJSONObject(i);
						tencentWeiboList.add(infoObject);
					}
				}
				if (TOTAL_PAGE == 1) {
					listAdapter = new TencentWeiboListAdapter(mContext,
							tencentWeiboList);
					tencentWeiboListView.setAdapter(listAdapter);
				}
				if (TOTAL_PAGE > 1) {
					listAdapter.notifyDataSetChanged();
				}
				listAdapter = new TencentWeiboListAdapter(mContext,
						tencentWeiboList);
				tencentWeiboListView.setAdapter(listAdapter);
				tencentWeiboListView.setSelection((TOTAL_PAGE - 1) * PAGE_SIZE
						+ 1);// 设置最新获取一页数据成功后显示数据的起始数据

			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				// sinaWeiboListView.setVisibility(View.GONE);
				// tencentWeiboListView.setVisibility(View.VISIBLE);
				if (actionState == ACTION_REFRESH) {
					loadingView.setVisibility(View.GONE);
					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy年MM月dd日 HH:mm");
					String date = format.format(new Date());
					tencentWeiboListView.onRefreshComplete(date);
				} else {
					listAdapter.hideMoreAnim();
				}

			}

		}
	};

	/**
	 * 刷新列表
	 */
	@Override
	public void onRefresh() {
		if (weiboType == SINA) {
			long sinceId = sinaWeiboListAdapter.getMaxId() + 1;
			SinaWeiboManager.getHomeTimeline(mContext, sinceId, 0,
					DEFAULT_STATUS_COUNT, new StatusRequestListenerImpl(
							mContext, HOME));
		} else {
			actionState = ACTION_REFRESH;
			TencentWeiboManager.getHomeTimeline(mContext, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					getTimelineCallback);
		}

	}

	/**
	 * 上下文菜单
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater()
				.inflate(R.menu.weibo_context_menu, menu);
		MenuItem forwardMenu = menu.findItem(R.id.menu_forward);
		MenuItem commentMenu = menu.findItem(R.id.menu_comment);
		MenuItem favoriteMenu = menu.findItem(R.id.menu_favorite);
		MenuItem unfavoriteMenu = menu.findItem(R.id.menu_unfavorite);
		MenuItem oriForwardMenu = menu.findItem(R.id.menu_ori_forward);
		MenuItem selfinfoMenu = menu.findItem(R.id.menu_selfinfo);
		forwardMenu.setOnMenuItemClickListener(this);
		commentMenu.setOnMenuItemClickListener(this);
		favoriteMenu.setOnMenuItemClickListener(this);
		oriForwardMenu.setOnMenuItemClickListener(this);
		selfinfoMenu.setOnMenuItemClickListener(this);
		unfavoriteMenu.setOnMenuItemClickListener(this);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (weiboType) {
		case SINA:
			sinaMenuContext(item);
			break;
		case TENCENT:
			tencentMenuContext(item);
			break;
		}
		return true;
	}

	private void tencentMenuContext(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_forward:// 转发
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				String text = "//@" + weiboInfo.getString("nick") + ":"
						+ weiboInfo.getString("text");
				intent = new Intent(mContext, PostWeibo.class);
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_FORWARD);
				intent.putExtra("title", "转播微博");
				intent.putExtra("text", text);
				intent.putExtra("id", weiboInfo.getString("id"));
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_ori_forward: // 原文转发
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				JSONObject sourceObj = weiboInfo.getJSONObject("source");
				String text = "//@" + weiboInfo.getString("nick") + ":"
						+ weiboInfo.getString("text");
				if (sourceObj != null) {
					text = "//@" + sourceObj.getString("nick") + ":"
							+ sourceObj.getString("text");
				}

				intent = new Intent(mContext, PostWeibo.class);
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_FORWARD);
				intent.putExtra("title", "转播微博");
				intent.putExtra("text", text);
				intent.putExtra("id", sourceObj.getString("id"));
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_comment: // 评论
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				intent = new Intent(mContext, PostWeibo.class);
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_COMMENT);
				intent.putExtra("title", "评论微博");
				intent.putExtra("id", weiboInfo.getString("id"));
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_favorite: // 收藏
			isFav = true;
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				TencentWeiboManager.favoriteWeibo(mContext, accessToken,
						StringUtil.REQUEST_FORMAT, weiboInfo.getString("id"),
						favoriteCallback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_unfavorite: // 取消收藏
			isFav = false;
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				TencentWeiboManager.unFavoriteWeibo(mContext, accessToken,
						StringUtil.REQUEST_FORMAT, weiboInfo.getString("id"),
						favoriteCallback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_selfinfo: // 个人资料
			try {
				JSONObject weiboInfo = (JSONObject) jsonArray.opt(position);
				intent = new Intent(mContext, SelfinfoActivity.class);
				intent.putExtra(StringUtil.WEIBO_TYPE, TENCENT);
				intent.putExtra(StringUtil.OPEN_ID,
						weiboInfo.getString("openid"));
				intent.putExtra(StringUtil.TENCENT_NAME,
						weiboInfo.getString("name"));
				startActivity(intent);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			break;

		}
	}

	private void sinaMenuContext(MenuItem item) {
		Status status = null;
		Intent intent = null;
		switch (item.getItemId()) {
		case R.id.menu_forward:// 转发
			status = sinaWeiboListAdapter.getStatus(position);
			forward(status);
			break;
		case R.id.menu_ori_forward: // 原文转发
			status = sinaWeiboListAdapter.getStatus(position).retweeted_status;
			if (status == null) {
				status = sinaWeiboListAdapter.getStatus(position);
			}
			forward(status);
			break;
		case R.id.menu_comment: // 评论
			status = sinaWeiboListAdapter.getStatus(position);
			intent = new Intent(mContext, PostWeibo.class);
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_COMMENT);
			intent.putExtra("title", "评论微博");
			intent.putExtra("status_id", status.id);
			startActivity(intent);
			break;
		case R.id.menu_favorite: // 收藏
			favorite(true);
			break;
		case R.id.menu_unfavorite: // 取消收藏
			favorite(false);
			break;
		case R.id.menu_selfinfo: // 个人资料
			status = sinaWeiboListAdapter.getStatus(position);
			intent = new Intent(mContext, SelfinfoActivity.class);
			intent.putExtra(StringUtil.WEIBO_TYPE, SINA);
			intent.putExtra(StringUtil.SINA_UID, status.user.id);
			startActivity(intent);
			break;

		}
	}

	private void forward(Status status) {
		String text = "//@" + status.user.name + ":" + status.text;
		Intent intent = new Intent(mContext, PostWeibo.class);
		intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
		intent.putExtra("type", TYPE_FORWARD);
		intent.putExtra("title", "转发微博");
		intent.putExtra("text", text);
		intent.putExtra("status_id", status.id);
		startActivity(intent);
	}

	private void favorite(boolean fav) {
		Status status = sinaWeiboListAdapter.getStatus(position);
		FavoriteWeiboTask favoriteWeiboTask = new FavoriteWeiboTask();
		favoriteWeiboTask.id = status.id;
		favoriteWeiboTask.fav = fav;
		favoriteWeiboTask.status = status;
		favoriteWeiboTask.doneAndProcess = this;
		Tools.getGlobalObject(getActivity()).getWorkQueueStorage()
				.addTask(favoriteWeiboTask);
	}

	/**
	 * 列表长按回调方法
	 * 点击列表最后一项，也就是“加载更多”不弹出上下文菜单
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		switch (weiboType) {
		case SINA:
			if (position == sinaWeiboListAdapter.getCount()) {
				return true;
			}
			break;
		case TENCENT:
			if (position == listAdapter.getCount()) {
				return true;
			}
			break;
		}

		this.position = position - 1;
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (weiboType) {
		case SINA:
			sinaListItemClick(parent, position);
			break;
		case TENCENT:
			tencentListItemClick(position - 1);
			break;
		}

	}

	/**
	 * 腾讯列表项点击
	 * 
	 * @param position
	 */
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
			listAdapter.showMoreAnim();
			actionState = ACTION_MORE;
			TencentWeiboManager.getHomeTimeline(mContext, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					getTimelineCallback);

		}

	}

	/**
	 * 新浪微博列表项点击触发
	 * 
	 * @param parent
	 * @param position
	 */
	private void sinaListItemClick(AdapterView<?> parent, int position) {
		Intent intent = null;
		switch (parent.getId()) {
		case R.id.sina_weibolist:
			// 按更多项
			if (position == sinaWeiboListAdapter.getCount()) {
				long maxId = sinaWeiboListAdapter.getMinId() - 1;
				SinaWeiboManager.getHomeTimeline(mContext, 0, maxId,
						DEFAULT_STATUS_COUNT, new StatusRequestListenerImpl(
								mContext, HOME));
				sinaWeiboListAdapter.showMoreAnim(); // 显示更多动画
			} else { // 点击列表项
				Status status = sinaWeiboListAdapter.getStatus(position - 1);
				if (status != null) {
					intent = new Intent(mContext, SingleWeiboViewer.class);
					intent.putExtra(StringUtil.WEIBO_TYPE, SINA);
					SingleWeiboViewer.status = status;
					startActivity(intent);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_msg:
			TencentWeiboManager.getHomeTimeline(mContext, accessToken, 0,
					(TOTAL_PAGE + 1) * PAGE_SIZE, StringUtil.REQUEST_FORMAT,
					getTimelineCallback);
			tv_msg.setVisibility(View.GONE);
			loading.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void doneProcess(ParentTask task) {
		Message msg = new Message();
		msg.obj = task;
		handler.sendMessage(msg);
	}

	public void registerBrocastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(StringUtil.ACCOUNT_CHANGE);
		// 注册广播
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	/**
	 * 广播接收器是接收切换帐号时所发出的广播
	 */
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			weiboType = intent.getIntExtra(StringUtil.WEIBO_TYPE, 0);
			String action = intent.getAction();
			final String accessToken = intent
					.getStringExtra(StringUtil.ACCESS_TOKEN);
			if (action.equals(StringUtil.ACCOUNT_CHANGE)) {
				loadingView.setVisibility(View.VISIBLE);
				if (weiboType == SINA) { // 新浪
					sinaWeiboListView.setVisibility(View.VISIBLE);
					tencentWeiboListView.setVisibility(View.GONE);
					SinaWeiboManager.getHomeTimeline(mContext, 0, 0,
							DEFAULT_STATUS_COUNT,
							new StatusRequestListenerImpl(mContext, HOME));
					if (taskWorkQueueMonitor == null) {
						taskWorkQueueMonitor = Tools.getGlobalObject(
								getActivity())
								.getTaskWorkQueueMonitor(mContext);
					}
				} else { // 腾讯
					sinaWeiboListView.setVisibility(View.GONE);
					tencentWeiboListView.setVisibility(View.VISIBLE);
					// 如果是腾讯微博，加上更多项
					TencentWeiboManager.getHomeTimeline(mContext, accessToken,
							0, (TOTAL_PAGE + 1) * PAGE_SIZE,
							StringUtil.REQUEST_FORMAT, getTimelineCallback);
				}

			}
		}
	};

	// 腾讯微博收藏回到接口
	private HttpCallback favoriteCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null) {
				return;
			}
			String jsonRestult = result.getObj().toString();
			try {
				JSONObject jsonObject = new JSONObject(jsonRestult);
				if ("ok".equals(jsonObject.getString("msg"))) {
					if (isFav) {
						ToastUtil.showLongToast(mContext, "收藏成功");
					} else {
						ToastUtil.showLongToast(mContext, "取消收藏成功");
					}
				} else {
					if (isFav) {
						ToastUtil.showLongToast(mContext, "收藏失败");
					} else {
						ToastUtil.showLongToast(mContext, "取消收藏失败");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

}
