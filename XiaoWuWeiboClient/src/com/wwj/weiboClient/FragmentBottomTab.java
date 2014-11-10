package com.wwj.weiboClient;

import net.youmi.android.AdManager;
import net.youmi.android.offers.OffersManager;
import net.youmi.android.spot.SpotDialogListener;
import net.youmi.android.spot.SpotManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost.TabSpec;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.umeng.fb.FeedbackAgent;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.util.Tools;

/**
 * 这个界面实现滑动菜单效果、ViewPager效果还有底部Tab切换界面效果
 * 
 * @author wwj
 * 
 */
public class FragmentBottomTab extends SlidingFragmentActivity implements Const {

	// 定义FragmentTabHost对象
	private FragmentTabHost mTabHost;
	private RadioGroup mTabRg;
	// 所有Fragment
	private final Class[] fragments = { HomeFragment.class,
			MessageFragment.class, SelfInfoFragment.class, MoreFragment.class };
	private CanvasTransformer mTransformer;
	public static SlidingMenu sm;

	private LayoutInflater inflater;
	private View parent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置主界面
		setContentView(R.layout.main_tabs);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		parent = inflater.inflate(R.layout.frg_home, null);

		// if (savedInstanceState != null) {
		// mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		// }

		// 初始化滑动菜单动画
		initAnimation();

		// 初始化滑动视图
		initSlidingMenu(savedInstanceState);

		// 初始化界面
		initView();

		// 初始化广告
		initAd();
	}

	private void initSlidingMenu(Bundle savedInstanceState) {

		// 设置左侧滑动菜单
		setBehindContentView(R.layout.menu_frame_left);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SlidingMenuFragment()).commit();

		// 实例化滑动菜单对象
		sm = getSlidingMenu();
		// 设置可以左右滑动菜单
		sm.setMode(SlidingMenu.LEFT);
		// 设置滑动阴影的宽度
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// 设置滑动菜单阴影的图像资源
		sm.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果的值
		sm.setFadeDegree(0.35f);
		// 设置触摸屏幕的模式
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		// 设置下方视图的在滚动时的缩放比例
		sm.setBehindScrollScale(0.0f);
		sm.setBehindCanvasTransformer(mTransformer);

		// 设置右侧滑动视图界面
		sm.setSecondaryMenu(R.layout.menu_frame_right);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame_two, new SlidingMenuFragment())
				.commit();

	}

	private void initView() {
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		// mTabHost.setup(this, getSupportFragmentManager());
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		// mViewPage = (ViewPager) findViewById(R.id.weibo_pager);
		mTabRg = (RadioGroup) findViewById(R.id.tab_rg_menu);
		// mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPage, mTabRg);
		// 得到fragment的个数
		int count = fragments.length;
		for (int i = 0; i < count; i++) {
			// 为每一个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(i + "").setIndicator(i + "");
			// 将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, fragments[i], null);

		}
		mTabRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tab_rb_1:
					mTabHost.setCurrentTab(0);
					break;
				case R.id.tab_rb_2:
					mTabHost.setCurrentTab(1);
					break;
				case R.id.tab_rb_3:
					mTabHost.setCurrentTab(2);
					break;
				case R.id.tab_rb_4:
					mTabHost.setCurrentTab(3);
					break;
				default:
					break;
				}

			}
		});

		mTabHost.setCurrentTab(0);
	}

	private void initAd() {
		// 初始化接口，应用启动时调用
		// 参数：appId,appSecret,调试模式
		AdManager.getInstance(this).init("526a749d8614b9ea",
				"62a6ee07c5a27ad4", false);
		// 如果使用积分广告，请务必调用积分广告的初始化接口：
		OffersManager.getInstance(this).onAppLaunch();
		// 插播接口调用
		// 开发者可以到开发者后台设置展示频率，需要到开发者后台设置页面（详细信息->业务信息->无积分广告业务->高级设置）
		// 自4.03版本增加云控制是否开启防误点功能，需要到开发者后台设置页面（详细信息->业务信息->无积分广告业务->高级设置）

		// 加载插播资源
		SpotManager.getInstance(this).loadSpotAds();
		// 设置展示时间，加载超时时不展示广告，默认为0，代表不设置超时时间
		SpotManager.getInstance(this).setSpotTimeout(5000);// 5秒

	}

	private static Interpolator interpolator = new Interpolator() {

		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}
	};

	/**
	 * 初始化动画
	 */
	private void initAnimation() {
		mTransformer = new CanvasTransformer() {

			@Override
			public void transformCanvas(Canvas canvas, float percentOpen) {
				canvas.translate(
						0,
						canvas.getHeight()
								* (1 - interpolator
										.getInterpolation(percentOpen)));
			}
		};
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("tab", mTabHost.getCurrentTabTag());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		case R.id.menu_settings:
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			startActivity(intent);
			break;
		case R.id.menu_official:
			// 显示插播广告
			SpotManager.getInstance(this).showSpotAds(this,
					new SpotDialogListener() {

						@Override
						public void onShowSuccess() {
							Log.i("SpotAd", "展示成功");
						}

						@Override
						public void onShowFailed() {
							Log.i("SpotAd", "展示失败");
						}
					});
			break;
		case R.id.menu_feedback:
			FeedbackAgent agent = new FeedbackAgent(this);
			agent.startFeedbackActivity();
			break;
		case R.id.menu_account:
			intent = new Intent(FragmentBottomTab.this, AccountManage.class);
			startActivity(intent);
			break;
		case R.id.menu_about: // 关于
			new AlertDialog.Builder(this)
					.setMessage("浪腾Android客户端\n\n作者：IT_xiao小巫")
					.setPositiveButton("关闭", null).show();
			break;
		case R.id.menu_quit: // 退出
			finish();
			System.exit(0);
			break;
		}
		return true;
	}

	private Fragment mContent;

	/**
	 * 切换视图
	 * 
	 * @param fragment
	 */
	public void switchContent(Fragment fragment) {
		mContent = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		getSlidingMenu().showContent();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) { // 按返回键不退出应用
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent);
			return true;
		}
		return false;
	}

	@Override
	public void finish() {
		// 关闭任务队列
		Tools.getGlobalObject(this).closeWorkQueue();
		super.finish();
	}
}
