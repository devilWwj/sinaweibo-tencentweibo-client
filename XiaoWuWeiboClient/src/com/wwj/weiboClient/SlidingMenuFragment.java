package com.wwj.weiboClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.wwj.weiboClient.database.DataBaseContext;
import com.wwj.weiboClient.database.DataHelper;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;

/**
 * 【滑动菜单的界面】
 * 
 * @author wwj
 * 
 */
public class SlidingMenuFragment extends Fragment implements Const,
		OnClickListener {
	private final static String TAG = SlidingMenuFragment.class.getName();

	// 上下文对象
	private Context mContext;

	// 两个下拉框
	private Spinner mWeiboSpinner;
	private Spinner mAccountSpinner;

	ArrayList<String> list = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	static ArrayAdapter<String> accountAdapter;
	// 微博类型
	private static int weiboType = 0;
	private int index = 0;

	// 账号管理
	private Button btnAccountManage;
	// 登录按钮
	private Button btnLogin;

	// 微博昵称
	private TextView tvScreenName;
	// 微博头像
	static ImageView ivHead;
	// 认证类型
	static ImageView ivVerified;

	// 数据库帮助类
	private static DataHelper dataHelper;
	// 新浪访问令牌
	private Oauth2AccessToken oauth2AccessToken;
	// 腾讯访问令牌
	private String tencentAccessToken;
	// 所有用户
	private static List<UserInfo> userInfos;
	// 新浪用户
	private static List<UserInfo> sinaUsers;
	// 腾讯用户
	private static List<UserInfo> tencentUsers;
	// 新浪用户昵称集合
	static List<String> sinaUserNames;
	// 腾讯用户昵称集合
	static List<String> tencentUserNames;
	private static List<List<String>> accounts;

	private String loginAccessToken;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String urlStr = (String) msg.obj;

			// 加载头像
			if (urlStr == null) {
				ivHead.setImageResource(R.drawable.avatar_default);
				ivVerified.setVisibility(View.GONE);
			} else {
				ivVerified.setVisibility(View.VISIBLE);
				ivHead.setImageURI(Uri.fromFile(new File(urlStr)));
				Tools.userVerified(ivVerified, 220);
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 上下文对象
		mContext = getActivity();
		dataHelper = DataBaseContext.getInstance(mContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View slidingMenuView = inflater.inflate(R.layout.frg_sliding_menu,
				container, false);

		weiboType = StorageManager.getValue(mContext, StringUtil.LOGIN_TYPE, 0);
		findViews(slidingMenuView);

		return slidingMenuView;
	}

	private void findViews(View slidingMenuView) {

		mWeiboSpinner = (Spinner) slidingMenuView
				.findViewById(R.id.weibo_spinner);
		mAccountSpinner = (Spinner) slidingMenuView
				.findViewById(R.id.account_spinner);

		loadData();

		// 从资源文件里获取微博类型数组
		String[] ls = getResources().getStringArray(R.array.weibo);
		for (int i = 0; i < ls.length; i++) {
			list.add(ls[i]);
		}

		adapter = new ArrayAdapter<String>(mContext, R.layout.spinner_layout,
				list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mWeiboSpinner.setAdapter(adapter);
		mWeiboSpinner.setPrompt("请选择微博类型");

		accounts = getAccounts();
		if (weiboType == SINA) {
			mWeiboSpinner.setSelection(SINA);
		} else {
			mWeiboSpinner.setSelection(TENCENT);
		}

		// 微博类型选择
		mWeiboSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// 微博类型(0为新浪,1为腾讯)
				weiboType = position;

				accountAdapter = new ArrayAdapter<String>(mContext,
						R.layout.spinner_layout, accounts.get(position));
				accountAdapter
						.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				mAccountSpinner.setAdapter(accountAdapter);
				mAccountSpinner.setPrompt("微博帐号");
				btnLogin.setEnabled(true);
				if (weiboType == SINA && sinaUserNames.size() == 0) {
					if (sinaUsers.size() == 0) {
						Toast.makeText(mContext, "赶快去添加一个新浪微博帐号吧",
								Toast.LENGTH_SHORT).show();
					}
					ivHead.setImageResource(R.drawable.avatar_default);
					ivVerified.setVisibility(View.GONE);
					btnLogin.setEnabled(false);
				} else if (weiboType == TENCENT && tencentUserNames.size() == 0) {
					if (tencentUsers.size() == 0) {
						Toast.makeText(mContext, "赶快去添加一个腾讯微博帐号吧",
								Toast.LENGTH_SHORT).show();
						btnLogin.setEnabled(false);
					}
					ivHead.setImageResource(R.drawable.avatar_default);
					ivVerified.setVisibility(View.GONE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		mAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				index = position;
				if (weiboType == SINA) { // 新浪
					final String url = sinaUsers.get(index).getUrl();
					getImageUrl(url);

					// 保存当前选择用户的UID
					StorageManager.setValue(mContext, StringUtil.SINA_UID,
							Long.valueOf(sinaUsers.get(index).getUserId()));
					oauth2AccessToken = new Oauth2AccessToken();
					oauth2AccessToken.setUid(sinaUsers.get(index).getUserId());
					oauth2AccessToken.setToken(sinaUsers.get(index).getToken());
					oauth2AccessToken.setExpiresTime(Long.valueOf(sinaUsers
							.get(index).getExpires_in()));
					AccessTokenKeeper.writeAccessToken(mContext,
							oauth2AccessToken);
					loginAccessToken = oauth2AccessToken.getToken();

				} else if (weiboType == TENCENT) {

					final String url = tencentUsers.get(index).getUrl();
					getImageUrl(url);
					tencentAccessToken = tencentUsers.get(index).getToken();
					// 保存腾讯用户的访问令牌
					StorageManager
							.setValue(mContext,
									StringUtil.TENCENT_ACCESS_TOKEN,
									tencentAccessToken);
					loginAccessToken = tencentAccessToken;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		btnLogin = (Button) slidingMenuView.findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(this);

		btnAccountManage = (Button) slidingMenuView
				.findViewById(R.id.account_manage);
		btnAccountManage.setOnClickListener(this);

		tvScreenName = (TextView) slidingMenuView
				.findViewById(R.id.tv_screen_name);
		ivHead = (ImageView) slidingMenuView.findViewById(R.id.iv_head);
		ivHead.setOnClickListener(this);
		ivVerified = (ImageView) slidingMenuView.findViewById(R.id.iv_verified);

	}

	private List<List<String>> getAccounts() {
		// 从数据库当中得到的用户名
		sinaUserNames = new ArrayList<String>();
		for (int i = 0; i < sinaUsers.size(); i++) {
			sinaUserNames.add(sinaUsers.get(i).getUserName());
		}

		tencentUserNames = new ArrayList<String>();
		for (int i = 0; i < tencentUsers.size(); i++) {
			tencentUserNames.add(tencentUsers.get(i).getUserName());
		}
		accounts = new ArrayList<List<String>>();
		accounts.add(sinaUserNames);
		accounts.add(tencentUserNames);
		return accounts;
	}

	public static void refreshAccounts() {
		loadData();
		sinaUserNames.clear();
		for (int i = 0; i < sinaUsers.size(); i++) {
			sinaUserNames.add(sinaUsers.get(i).getUserName());
		}
		tencentUserNames.clear();
		for (int i = 0; i < tencentUsers.size(); i++) {
			tencentUserNames.add(tencentUsers.get(i).getUserName());
		}
		accounts.clear();
		accounts.add(sinaUserNames);
		accounts.add(tencentUserNames);
	}

	// 加载数据
	private static void loadData() {
		userInfos = dataHelper.getUserList(true);

		sinaUsers = new ArrayList<UserInfo>();
		tencentUsers = new ArrayList<UserInfo>();
		for (UserInfo userInfo : userInfos) {
			if (userInfo.getType() == Const.SINA) {
				sinaUsers.add(userInfo);
			} else if (userInfo.getType() == Const.TENCENT) {
				tencentUsers.add(userInfo);
			}
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btnLogin: // 登录

			changeAccount();
			break;
		case R.id.account_manage: // 帐号管理
			intent = new Intent(mContext, AccountManage.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.iv_head:

			break;
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case ACCOUNT_RESULT_CODE: // 账号管理返回来的结果
			refreshAccounts();
			accountAdapter.notifyDataSetChanged();
			switch (weiboType) {
			case SINA:
				if (sinaUsers.size() == 0) {
					ivHead.setImageResource(R.drawable.avatar_default);
					return;
				}
				btnLogin.setEnabled(true);
				getImageUrl(sinaUsers.get(index).getUrl());
				break;
			case TENCENT:
				if (tencentUsers.size() == 0) {
					ivHead.setImageResource(R.drawable.avatar_default);
					return;
				}
				btnLogin.setEnabled(true);
				getImageUrl(tencentUsers.get(index).getUrl());
				break;
			}
			if (weiboType == SINA && sinaUsers.size() == 0) {
				ivHead.setImageResource(R.drawable.avatar_default);
				ivVerified.setVisibility(View.GONE);
				btnLogin.setEnabled(false);
			} else if (weiboType == TENCENT && tencentUsers.size() == 0) {
				ivHead.setImageResource(R.drawable.avatar_default);
				ivVerified.setVisibility(View.GONE);
				btnLogin.setEnabled(false);
			}
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// 切换帐号
	private void changeAccount() {
		// 关闭滑动菜单
		FragmentBottomTab.sm.toggle();
		Intent intent = new Intent(StringUtil.ACCOUNT_CHANGE);
		intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
		intent.putExtra(StringUtil.ACCESS_TOKEN, tencentAccessToken);

		int loginType = StorageManager.getValue(mContext,
				StringUtil.LOGIN_TYPE, 0);
		String accessToken = StorageManager.getValue(mContext,
				StringUtil.ACCESS_TOKEN, "");
		if (weiboType == loginType && accessToken.equals(loginAccessToken)) {
			ToastUtil.showShortToast(mContext, "您已经登录无须再次登录");
			return;
		} else {
			StorageManager.setValue(mContext, StringUtil.LOGIN_TYPE, weiboType);
			StorageManager.setValue(mContext, StringUtil.ACCESS_TOKEN,
					loginAccessToken);
		}
		mContext.sendBroadcast(intent);

	}

	private void getImageUrl(final String url) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String urlStr = SinaWeiboManager.getImageurl(url,
						getActivity());
				Message msg = new Message();
				msg.obj = urlStr;
				handler.sendMessage(msg);
			}
		}).start();
	}

}
