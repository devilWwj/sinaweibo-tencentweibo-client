package com.wwj.weiboClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * ����������
 * 
 * @author wwj
 * 
 */
public class Splash extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				// �ӳ�3������¼����
				Intent intent = new Intent(Splash.this, LoginActivity.class);
				startActivity(intent);
				Splash.this.finish();
			}
		}, 3000);
	}
}
