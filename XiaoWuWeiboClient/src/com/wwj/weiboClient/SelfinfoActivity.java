package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * �鿴�������Ͻ���
 * 
 * @author Administrator
 * 
 */
public class SelfinfoActivity extends BaseActivity implements Const,
		OnClickListener {

	private TextView tvUsername; // �û���
	private ImageView ivPhoto; // ͷ��
	private TextView tvScreenName; // �ǳ�
	private TextView tvLocation; // λ��
	private TextView tvDescription; // ����
	private TextView tvFollows; // ��ע
	private TextView tvFriends; // ��˿
	private TextView tvStatus; // ����΢��
	private TextView tvFavorites; // �ղ�

	private LinearLayout llFollows;
	private LinearLayout llStatus;
	private LinearLayout llFriends;
	private LinearLayout llFavorites;

	/** ����΢������ **/
	private TextView tvName;
	private TextView tvStatusText;
	private TextView tvCreateAt;
	private TextView tvSource;

	private Context mContext;
	private AsyncImageLoader asyncImageLoader;
	private int weiboType;

	private long uid; // �����û���uid
	private String name;// ��Ѷ�û����û���
	private String fopenid; // ��Ѷ�û���id

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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frg_selfinfo);
		mContext = this;
		asyncImageLoader = new AsyncImageLoader();
		weiboType = getIntent().getIntExtra(StringUtil.WEIBO_TYPE, 0);
		uid = getIntent().getLongExtra(StringUtil.SINA_UID, Long.MIN_VALUE);
		name = getIntent().getStringExtra(StringUtil.TENCENT_NAME);
		fopenid = getIntent().getStringExtra("fopenid");

		findView();
		getUserInfo();
	}

	private void findView() {
		tvUsername = (TextView) findViewById(R.id.username);
		ivPhoto = (ImageView) findViewById(R.id.photo);
		tvScreenName = (TextView) findViewById(R.id.name);
		tvLocation = (TextView) findViewById(R.id.tv_address);

		tvDescription = (TextView) findViewById(R.id.description);
		tvStatus = (TextView) findViewById(R.id.tv_selfinfoStatusCount);
		tvFollows = (TextView) findViewById(R.id.tv_selfinfoFollowsCount);
		tvFriends = (TextView) findViewById(R.id.tv_selfinfoFriendsCount);
		tvFavorites = (TextView) findViewById(R.id.tv_selfinfoFavouritesCount);

		llFollows = (LinearLayout) findViewById(R.id.ll_follows);
		llStatus = (LinearLayout) findViewById(R.id.ll_twitter);
		llFriends = (LinearLayout) findViewById(R.id.ll_friends);
		llFavorites = (LinearLayout) findViewById(R.id.ll_favorites);

		tvName = (TextView) findViewById(R.id.tv_name);
		tvCreateAt = (TextView) findViewById(R.id.tv_created_at);
		tvStatusText = (TextView) findViewById(R.id.tv_text);
		tvSource = (TextView) findViewById(R.id.tv_source);

	}

	// ��ȡ�û���Ϣ
	private void getUserInfo() {
		if (weiboType == SINA) {
			SinaWeiboManager.getUserInfo(mContext, uid, showUserListener);
		} else if (weiboType == TENCENT) {
			String accessToken = GlobalObject.getTencentAccessToken(mContext);
			if (!TextUtils.isEmpty(accessToken)) {
				TencentWeiboManager.getOtherUserInfo(mContext,
						StringUtil.REQUEST_FORMAT, name, fopenid,
						tencentUserInfoCallback);
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

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.ll_follows: // ��ע
			intent = new Intent(mContext, FollowsListViewer.class);
			startActivity(intent);
			break;
		case R.id.ll_twitter: // ΢��
			switch (weiboType) {
			case SINA:
				intent = new Intent(mContext, SinaWeiboViewer.class);
				intent.putExtra("title", "�ҵ�΢��");
				intent.putExtra("type", USER_TIMELINE);
				break;
			case TENCENT:
				intent = new Intent(mContext, TencentWeiboViewer.class);
				intent.putExtra("title", "�ҵ�΢��");
				intent.putExtra("type", USER_TIMELINE);
				break;
			}
			startActivity(intent);
			break;
		case R.id.ll_friends: // ��˿
			intent = new Intent(mContext, FriendsListViewer.class);
			startActivity(intent);
			break;
		case R.id.ll_favorites: // �ղ�
			switch (weiboType) {
			case SINA:
				intent = new Intent(mContext, SinaWeiboViewer.class);
				intent.putExtra("title", "�ҵ��ղ�");
				intent.putExtra("type", MESSAGE_FAVORITE);
				break;
			case TENCENT:
				intent = new Intent(mContext, TencentWeiboViewer.class);
				intent.putExtra("title", "�ҵ��ղ�");
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
		 * ����ȡ���������ص��ַ����󣬸ú���������
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

	/** ��ȡ��Ѷ΢���û����ϻص� **/
	private HttpCallback tencentUserInfoCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult modelResult = (ModelResult) obj;
			LogUtils.d(modelResult.getObj().toString());

			try {
				String jsonResult = modelResult.getObj().toString();
				// �õ�data����
				JSONObject dataObj = new JSONObject(jsonResult)
						.getJSONObject("data");
				String userId = dataObj.getString("name");
				String username = dataObj.getString("nick");
				String headUrl = dataObj.getString("head") + "/100";
				String location = dataObj.getString("location");
				String description = dataObj.getString("introduction");
				String followsCount = dataObj.getString("idolnum");
				String tweetnum = dataObj.getString("tweetnum");
				String fansCount = dataObj.getString("fansnum");
				String favsCount = dataObj.getString("favnum");
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}