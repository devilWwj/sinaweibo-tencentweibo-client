package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.User;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.Tools;

/**
 * 【个人资料界面】
 * 
 * 显示个人详细信息
 * 
 * @author wwj
 * 
 */
public class SelfInfoFragment extends BaseFragment implements Const,
		OnClickListener {
	private TextView tvUsername; // 用户名
	private ImageView ivPhoto; // 头像
	private TextView tvScreenName; // 昵称
	private TextView tvLocation; // 位置
	private TextView tvDescription; // 描述
	private TextView tvFollows; // 关注
	private TextView tvFriends; // 粉丝
	private TextView tvStatus; // 最新微博
	private TextView tvFavorites; // 收藏
	private ImageView ivSex; // 性别标识

	private LinearLayout llFollows;
	private LinearLayout llStatus;
	private LinearLayout llFriends;
	private LinearLayout llFavorites;

	/** 最新微博内容 **/
	private TextView tvName;
	private TextView tvStatusText;
	private TextView tvCreateAt;
	private TextView tvSource;

	private Context mContext;
	private AsyncImageLoader asyncImageLoader;

	private View rootView;
	private int weiboType;
	private String name;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.obj instanceof User) {
				User user = (User) msg.obj;
				tvUsername.setText(user.name);
				tvScreenName.setText(user.screen_name);
				tvLocation.setText(user.location);
				tvDescription.setText(user.description);
				tvFollows.setText(String.valueOf(user.followers_count));
				tvStatus.setText(String.valueOf(user.statuses_count));
				tvFriends.setText(String.valueOf(user.friends_count));
				tvFavorites.setText(String.valueOf(user.favourites_count));
				if (user.gender.equals("m")) {
					ivSex.setImageResource(R.drawable.userinfo_icon_male);
				} else if (user.gender.equals("f")) {
					ivSex.setImageResource(R.drawable.userinfo_icon_female);
				} else {
					ivSex.setVisibility(View.INVISIBLE);
				}

				Drawable cachedImage = asyncImageLoader.loadDrawable(
						user.profile_image_url, ivPhoto, new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable,
									ImageView imageView, String imageUrl) {
								imageView.setImageDrawable(imageDrawable);
							}
						});
				if (cachedImage == null) {
					ivPhoto.setImageResource(R.drawable.avatar_default);
				} else {
					ivPhoto.setImageDrawable(cachedImage);
				}
				tvName.setText(user.name);
				tvCreateAt.setText(Tools.getTimeStr(user.status.getCreatedAt(),
						new Date()));
				tvStatusText.setText(user.status.text);
				tvSource.setText(user.status.getTextSource());
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		asyncImageLoader = new AsyncImageLoader();
		registerBrocastReceiver();
		weiboType = StorageManager.getValue(mContext, StringUtil.LOGIN_TYPE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater
					.inflate(R.layout.frg_selfinfo, container, false);
		}
		// 缓存的rootView需要判断是否已经被加过parent，
		// 如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		findView(rootView);
		setListener();

		getUserInfo();

		return rootView;
	}

	// 获取用户信息
	private void getUserInfo() {
		if (weiboType == SINA) {
			long uid = StorageManager.getValue(mContext, StringUtil.SINA_UID,
					Long.valueOf(0));
			SinaWeiboManager.getUserInfo(mContext, uid, showUserListener);
		} else if (weiboType == TENCENT) {
			String accessToken = GlobalObject.getTencentAccessToken(mContext);
			if (!TextUtils.isEmpty(accessToken)) {
				TencentWeiboManager.getUserInfo(mContext,
						StringUtil.REQUEST_FORMAT, tencentUserInfoCallback);
			}
			recoverView();
		}
	}

	private void recoverView() {
		tvUsername.setText("");
		tvScreenName.setText("");
		tvLocation.setText("");
		tvDescription.setText("");
		tvFollows.setText("");
		tvStatus.setText("");
		tvFriends.setText("");
		tvFavorites.setText("");
		ivPhoto.setImageResource(R.drawable.avatar_default);
		tvName.setText("");
		tvCreateAt.setText("");
		tvStatusText.setText("");
		tvSource.setText("");

	}

	private void findView(View parent) {
		tvUsername = (TextView) parent.findViewById(R.id.username);
		ivPhoto = (ImageView) parent.findViewById(R.id.photo);
		tvScreenName = (TextView) parent.findViewById(R.id.name);
		tvLocation = (TextView) parent.findViewById(R.id.tv_address);

		tvDescription = (TextView) parent.findViewById(R.id.description);
		tvStatus = (TextView) parent.findViewById(R.id.tv_selfinfoStatusCount);
		tvFollows = (TextView) parent
				.findViewById(R.id.tv_selfinfoFollowsCount);
		tvFriends = (TextView) parent
				.findViewById(R.id.tv_selfinfoFriendsCount);
		tvFavorites = (TextView) parent
				.findViewById(R.id.tv_selfinfoFavouritesCount);

		llFollows = (LinearLayout) parent.findViewById(R.id.ll_follows);
		llStatus = (LinearLayout) parent.findViewById(R.id.ll_twitter);
		llFriends = (LinearLayout) parent.findViewById(R.id.ll_friends);
		llFavorites = (LinearLayout) parent.findViewById(R.id.ll_favorites);

		tvName = (TextView) parent.findViewById(R.id.tv_name);
		tvCreateAt = (TextView) parent.findViewById(R.id.tv_created_at);
		tvStatusText = (TextView) parent.findViewById(R.id.tv_text);
		tvSource = (TextView) parent.findViewById(R.id.tv_source);

		ivSex = (ImageView) parent.findViewById(R.id.sex);
	}

	private void setListener() {
		llFollows.setOnClickListener(this);
		llStatus.setOnClickListener(this);
		llFriends.setOnClickListener(this);
		llFavorites.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.ll_follows: // 关注
			intent = new Intent(mContext, FollowsListViewer.class);
			startActivity(intent);
			break;
		case R.id.ll_twitter: // 微博
			switch (weiboType) {
			case SINA:
				intent = new Intent(mContext, SinaWeiboViewer.class);
				intent.putExtra("title", "我的微博");
				intent.putExtra("type", USER_TIMELINE);
				break;
			case TENCENT:
				intent = new Intent(mContext, TencentWeiboViewer.class);
				intent.putExtra("title", "我的微博");
				intent.putExtra("type", USER_TIMELINE);
				intent.putExtra("name", name);
				break;
			}
			startActivity(intent);
			break;
		case R.id.ll_friends: // 粉丝
			intent = new Intent(mContext, FriendsListViewer.class);
			startActivity(intent);
			break;
		case R.id.ll_favorites: // 收藏
			switch (weiboType) {
			case SINA:
				intent = new Intent(mContext, SinaWeiboViewer.class);
				intent.putExtra("title", "我的收藏");
				intent.putExtra("type", MESSAGE_FAVORITE);
				break;
			case TENCENT:
				intent = new Intent(mContext, TencentWeiboViewer.class);
				intent.putExtra("title", "我的收藏");
				intent.putExtra("type", MESSAGE_FAVORITE);
				break;
			}
			startActivity(intent);
			break;
		}

	}

	private RequestListener showUserListener = new RequestListener() {

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

		/**
		 * 当获取服务器返回的字符串后，该函数被调用
		 */
		@Override
		public void onComplete(String response) {
			Log.v("response", response);
			User user = new User();
			JSONAndObject.convertSingleObject(user, response);
			Message msg = new Message();
			msg.obj = user;
			handler.sendMessage(msg);
		}
	};

	/** 获取腾讯微博用户资料回调 **/
	private HttpCallback tencentUserInfoCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult modelResult = (ModelResult) obj;
			// LogUtils.d(modelResult.getObj().toString());

			try {
				String jsonResult = modelResult.getObj().toString();
				// 得到data对象
				JSONObject dataObj = new JSONObject(jsonResult)
						.getJSONObject("data");
				name = dataObj.getString("name");
				String username = dataObj.getString("nick");
				String headUrl = dataObj.getString("head") + "/100";
				String location = dataObj.getString("location");
				String description = dataObj.getString("introduction");
				String followsCount = dataObj.getString("idolnum");
				String tweetnum = dataObj.getString("tweetnum");
				String fansCount = dataObj.getString("fansnum");
				String favsCount = dataObj.getString("favnum");
				int sex = dataObj.getInt("sex");
				if (sex == 1) {
					ivSex.setImageResource(R.drawable.userinfo_icon_male);
				} else if (sex == 2) {
					ivSex.setImageResource(R.drawable.userinfo_icon_female);
				} else {
					ivSex.setVisibility(View.INVISIBLE);
				}
				tvScreenName.setText(username);
				tvName.setText(username);
				Drawable cachedImage = asyncImageLoader.loadDrawable(
						dataObj.getString("head") + "/100", ivPhoto,
						new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable,
									ImageView imageView, String imageUrl) {
								imageView.setImageDrawable(imageDrawable);
							}
						});
				if (cachedImage == null) {
					ivPhoto.setImageResource(R.drawable.avatar_default);
				} else {
					ivPhoto.setImageDrawable(cachedImage);
				}
				tvLocation.setText(location);
				tvDescription.setText(description);
				tvFollows.setText(followsCount);
				tvStatus.setText(tweetnum);
				tvFriends.setText(fansCount);
				tvFavorites.setText(favsCount);

				JSONArray tweetinfo = dataObj.getJSONArray("tweetinfo");
				int size = tweetinfo.length();
				for (int i = 0; i < size; i++) {
					JSONObject jsonObject = (JSONObject) tweetinfo.opt(i);
					String origtext = jsonObject.getString("origtext");
					String from = jsonObject.getString("from");

					tvName.setText(username);
					tvStatusText.setText(origtext);
					tvSource.setText(from);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	};

	public void registerBrocastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(StringUtil.ACCOUNT_CHANGE);
		// 注册广播
		mContext.registerReceiver(broadcastReceiver, intentFilter);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			weiboType = intent.getIntExtra(StringUtil.WEIBO_TYPE, 0);
			if (action.equals(StringUtil.ACCOUNT_CHANGE)) {
				getUserInfo();
			}
		}
	};

	public void onDestroy() {
		mContext.unregisterReceiver(broadcastReceiver);
	};

}
