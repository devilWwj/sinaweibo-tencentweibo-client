package com.wwj.weiboClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sina.weibo.sdk.auth.WeiboAuth;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.SinaConstants;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.StringUtil;

/**
 * ��¼���� ��һ�ε�¼��ʱ�򣬻�û�н�����Ȩ����Ȩ���󱣴��ʺ�
 * 
 * @author wwj
 * 
 */
public class LoginActivity extends Activity implements Const, OnClickListener {
	// ����΢��Web��Ȩ�࣬�ṩ��¼�ȹ���
	private WeiboAuth mWeiboAuth;
	private static LoginActivity instance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_login);
		instance = this;
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(this, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);

		Button btnSinaLogin = (Button) findViewById(R.id.btn_sina_login);
		Button btnTencentLogin = (Button) findViewById(R.id.btn_tencent_login);
		btnSinaLogin.setOnClickListener(this);
		btnTencentLogin.setOnClickListener(this);
	}

	// �ж�Activity�Ƿ���������
	public static boolean isRunning() {
		return (instance != null && !instance.isFinishing());
	}

	public static void finishIfRunning() {
		if (instance != null && !instance.isFinishing()) {
			instance.finish();
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, FragmentBottomTab.class);
		// �Ƿ��Ѿ���¼��
		boolean hasLogin = StorageManager.getValue(this, StringUtil.HAS_LOGIN,
				false);
		switch (v.getId()) {
		case R.id.btn_sina_login: // �����˺ŵ�¼
			// �����¼����
			StorageManager.setValue(this, StringUtil.LOGIN_TYPE, SINA);
			// �ж��ʺ��Ƿ����
			if (hasLogin
					&& AccessTokenKeeper.readAccessToken(instance)
							.isSessionValid()) {

				startActivity(intent);
				this.finish();
			} else { // ������ھ�������Ȩ
				// ��Ȩ
				// mWeiboAuth.anthorize(new SinaAuthListener(this));
				Intent i2 = new Intent(this, WebViewActivity.class);
				startActivity(i2);

			}
			break;
		case R.id.btn_tencent_login: // ��Ѷ�˺ŵ�¼
			// �����¼����
			StorageManager.setValue(this, StringUtil.LOGIN_TYPE, TENCENT);
			if (hasLogin && Util.getSharePersistent(this, "EXPIRES_IN") != "") {
				startActivity(intent);
				this.finish();
			} else {
				Intent i2 = new Intent(this, Authorize.class);
				startActivity(i2);
			}
			// long appid =
			// Long.valueOf(Util.getConfig().getProperty("APP_KEY"));
			// String app_secret = Util.getConfig().getProperty("APP_KEY_SEC");
			// LoginUtil.tengXunSSOAuth(this, appid, app_secret);

			break;
		}
	}
}
