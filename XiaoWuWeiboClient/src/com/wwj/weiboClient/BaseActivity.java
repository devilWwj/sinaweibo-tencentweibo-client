package com.wwj.weiboClient;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {
	protected Context context;

	protected boolean isShowing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isShowing = true;
	}

	@Override
	protected void onPause() {
		isShowing = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
