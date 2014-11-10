package com.wwj.weiboClient;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.sina.weibo.sdk.auth.WeiboAuth;
import com.wwj.weiboClient.adapters.AccountListAdapter;
import com.wwj.weiboClient.database.DataBaseContext;
import com.wwj.weiboClient.database.DataHelper;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.listener.impl.SinaAuthListener;
import com.wwj.weiboClient.model.SinaConstants;
import com.wwj.weiboClient.util.StringUtil;

/**
 * 2013/12/17 ���˺Ź���
 * 
 * @author WWJ
 * 
 */
public class AccountManage extends Activity implements Const, OnClickListener,
		OnMenuItemClickListener, OnItemLongClickListener {
	private Spinner mWeiboSpinner; // �ʺ�����Spinner
	private Button mAddAccount; // ����ʺ�
	private static int weiboType = 0; // ΢������

	ArrayList<String> list = new ArrayList<String>();
	ArrayAdapter<String> adapter;
	// ����΢��Web��Ȩ�࣬�ṩ��¼�ȹ���
	private WeiboAuth mWeiboAuth;

	private static Context mContext;

	private static ListView accountListView;
	private static DataHelper dataHelper;

	public static AccountListAdapter sinaAccountAdapter;
	public static AccountListAdapter tencentAccountAdapter;

	public static List<UserInfo> userInfos;
	private static List<UserInfo> sinaUsers;
	private static List<UserInfo> tencentUsers;

	private Button backBtn; // ���ذ�ť
	private int positon;

	public static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				updateSinaUserList();
				break;
			case 1:
				updateTencentUserList();
				break;

			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_manage);

		backBtn = (Button) findViewById(R.id.backBtn);
		backBtn.setOnClickListener(this);
		mWeiboSpinner = (Spinner) findViewById(R.id.weibo_spinner);
		String[] ls = getResources().getStringArray(R.array.weibo);
		for (int i = 0; i < ls.length; i++) {
			list.add(ls[i]);
		}

		adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mWeiboSpinner.setAdapter(adapter);
		mWeiboSpinner.setPrompt("��ѡ��΢������");
		mWeiboSpinner.setOnItemSelectedListener(spinnerOnItemSelected);

		mAddAccount = (Button) findViewById(R.id.add_account);
		mAddAccount.setOnClickListener(this);

		mContext = this;
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(mContext, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);

		accountListView = (ListView) findViewById(R.id.account_list);

		// ��ȡ���ݿ��������
		dataHelper = DataBaseContext.getInstance(mContext);
		// ��ȡ�û��б�
		userInfos = dataHelper.getUserList(true);
		setListAdapter();
		registerForContextMenu(accountListView);
		accountListView.setOnItemLongClickListener(this);

	}

	private void setListAdapter() {
		// ��������û�����Ѷ�û����б�
		getSinaUsers();
		getTencentUsers();
		if (userInfos.size() == 0) {
			Toast.makeText(mContext, "����û����ӹ��˺�Ŷ", Toast.LENGTH_LONG).show();
		}
		sinaAccountAdapter = new AccountListAdapter(AccountManage.this,
				sinaUsers);
		tencentAccountAdapter = new AccountListAdapter(AccountManage.this,
				tencentUsers);
		accountListView.setAdapter(sinaAccountAdapter);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.add_account:
			if (weiboType == SINA) { // ����΢��
				mWeiboAuth.anthorize(new SinaAuthListener(mContext));
				// mWeiboAuth.authorize(new SinaAuthListener(mContext),
				// WeiboAuth.OBTAIN_AUTH_TOKEN);
			} else if (weiboType == TENCENT) { // ��Ѷ΢��
			// long appid = Long.valueOf(Util.getConfig().getProperty(
			// "APP_KEY"));
			// String app_secret = Util.getConfig().getProperty("APP_KEY_SEC");
			// LoginUtil.tengXunSSOAuth(mContext, appid, app_secret);

				// intent = new Intent(mContext, Authorize.class);
				// startActivity(intent);
				//
				// // ����һ��UserInfo����
				// UserInfo userInfo = new UserInfo();
				// ��
				// userInfo.setToken(Util.getSharePersistent(mContext,
				// "ACCESS_TOKEN"));
				// userInfo.setTokenSecret("");
				// //
				// userInfo.setExipires_in(Long.valueOf(Util.getSharePersistent(
				// // mContext, "EXPIRES_IN")));
				// // userInfo.setType(Const.TENCENT);
				// // ��ȡ�û�����
				// TencentWeiboManager.getUserInfo(mContext,
				// StringUtil.REQUEST_FORMAT,
				// new TencentGetUserInfoCallBack(mContext, userInfo));
				
				intent = new Intent(this, Authorize.class);
				startActivity(intent);
			}
			break;

		case R.id.backBtn:
			setResult(ACCOUNT_RESULT_CODE);
			finish();
			break;
		}

	}

	// Spinner��Ŀѡ�������
	private OnItemSelectedListener spinnerOnItemSelected = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			weiboType = position;
			if (weiboType == SINA) {
				if (sinaUsers.size() == 0) {
					Toast.makeText(mContext, "��������κ������˺���", Toast.LENGTH_SHORT)
							.show();
				}
				accountListView.setAdapter(sinaAccountAdapter);

			} else if (weiboType == TENCENT) {
				if (tencentUsers.size() == 0) {
					Toast.makeText(mContext, "��������κ���Ѷ�˺���", Toast.LENGTH_SHORT)
							.show();
				}
				accountListView.setAdapter(tencentAccountAdapter);
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

		}

	};

	// ��������û�
	private static List<UserInfo> getSinaUsers() {
		sinaUsers = new ArrayList<UserInfo>();
		for (UserInfo userInfo : userInfos) {
			if (userInfo.getType() == SINA) {
				sinaUsers.add(userInfo);
			}
		}
		return sinaUsers;
	}

	// �����Ѷ�û�
	private static List<UserInfo> getTencentUsers() {
		tencentUsers = new ArrayList<UserInfo>();
		for (UserInfo userInfo : userInfos) {
			if (userInfo.getType() == TENCENT) {
				tencentUsers.add(userInfo);
			}
		}
		return tencentUsers;
	}

	public static void updateAccountList() {
		// ��ȡ�û��б�
		userInfos = dataHelper.getUserList(true);
		getSinaUsers();
		getTencentUsers();
		sinaAccountAdapter.notifyDataSetChanged();
		tencentAccountAdapter.notifyDataSetChanged();
		if (weiboType == SINA) {
			accountListView.setAdapter(sinaAccountAdapter);
		} else {
			accountListView.setAdapter(tencentAccountAdapter);
		}
	}

	public static void updateSinaUserList() {
		// ��ȡ���ݿ��û��б�
		userInfos = dataHelper.getUserList(true);
		List<UserInfo> infos = new ArrayList<UserInfo>();
		for (UserInfo userInfo : userInfos) {
			if (userInfo.getType() == SINA) {
				infos.add(userInfo);
			}
		}
		sinaUsers.clear();
		sinaUsers.addAll(infos);
		// getSinaUsers();
		// �����б�
		sinaAccountAdapter.notifyDataSetChanged();
	}

	public static void updateTencentUserList() {
		// ��ȡ���ݿ��û��б�
		userInfos = dataHelper.getUserList(true);
		List<UserInfo> infos = new ArrayList<UserInfo>();
		for (UserInfo userInfo : userInfos) {
			if (userInfo.getType() == TENCENT) {
				infos.add(userInfo);
			}
		}
		tencentUsers.clear();
		tencentUsers.addAll(infos);
		// getTencentUsers();
		tencentAccountAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		this.getMenuInflater().inflate(R.menu.account_context_menu, menu);
		MenuItem viewMenu = menu.findItem(R.id.view_account);
		MenuItem deleteMenu = menu.findItem(R.id.delete_account);
		viewMenu.setOnMenuItemClickListener(this);
		deleteMenu.setOnMenuItemClickListener(this);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.view_account: // �鿴�ʺ�
			Intent intent = new Intent(mContext, SelfinfoActivity.class);
			if (weiboType == SINA) {
				intent.putExtra(StringUtil.WEIBO_TYPE, SINA);
				intent.putExtra(StringUtil.SINA_UID,
						Long.valueOf(sinaUsers.get(positon).getUserId()));
			} else {
				intent.putExtra(StringUtil.WEIBO_TYPE, TENCENT);
				intent.putExtra(StringUtil.TENCENT_NAME,
						tencentUsers.get(positon).getUserId());
			}
			startActivity(intent);
			break;
		case R.id.delete_account: // ɾ���ʺ�
			if (weiboType == SINA) {
				dataHelper.delUserInfo(sinaUsers.get(positon).getUserId());
				updateSinaUserList();
			} else {
				dataHelper.delUserInfo(tencentUsers.get(positon).getUserId());
				updateTencentUserList();
			}

			break;
		}
		return false;
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		this.positon = position;
		return false;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			setResult(ACCOUNT_RESULT_CODE);
			finish();
		}
		return super.dispatchKeyEvent(event);
	}
}
