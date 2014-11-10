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
 * �������ʵ�ֻ����˵�Ч����ViewPagerЧ�����еײ�Tab�л�����Ч��
 * 
 * @author wwj
 * 
 */
public class FragmentBottomTab extends SlidingFragmentActivity implements Const {

	// ����FragmentTabHost����
	private FragmentTabHost mTabHost;
	private RadioGroup mTabRg;
	// ����Fragment
	private final Class[] fragments = { HomeFragment.class,
			MessageFragment.class, SelfInfoFragment.class, MoreFragment.class };
	private CanvasTransformer mTransformer;
	public static SlidingMenu sm;

	private LayoutInflater inflater;
	private View parent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ����������
		setContentView(R.layout.main_tabs);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		parent = inflater.inflate(R.layout.frg_home, null);

		// if (savedInstanceState != null) {
		// mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		// }

		// ��ʼ�������˵�����
		initAnimation();

		// ��ʼ��������ͼ
		initSlidingMenu(savedInstanceState);

		// ��ʼ������
		initView();

		// ��ʼ�����
		initAd();
	}

	private void initSlidingMenu(Bundle savedInstanceState) {

		// ������໬���˵�
		setBehindContentView(R.layout.menu_frame_left);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new SlidingMenuFragment()).commit();

		// ʵ���������˵�����
		sm = getSlidingMenu();
		// ���ÿ������һ����˵�
		sm.setMode(SlidingMenu.LEFT);
		// ���û�����Ӱ�Ŀ��
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// ���û����˵���Ӱ��ͼ����Դ
		sm.setShadowDrawable(R.drawable.shadow);
		// ���û����˵���ͼ�Ŀ��
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// ���ý��뽥��Ч����ֵ
		sm.setFadeDegree(0.35f);
		// ���ô�����Ļ��ģʽ
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		// �����·���ͼ���ڹ���ʱ�����ű���
		sm.setBehindScrollScale(0.0f);
		sm.setBehindCanvasTransformer(mTransformer);

		// �����Ҳ໬����ͼ����
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
		// �õ�fragment�ĸ���
		int count = fragments.length;
		for (int i = 0; i < count; i++) {
			// Ϊÿһ��Tab��ť����ͼ�ꡢ���ֺ�����
			TabSpec tabSpec = mTabHost.newTabSpec(i + "").setIndicator(i + "");
			// ��Tab��ť��ӽ�Tabѡ���
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
		// ��ʼ���ӿڣ�Ӧ������ʱ����
		// ������appId,appSecret,����ģʽ
		AdManager.getInstance(this).init("526a749d8614b9ea",
				"62a6ee07c5a27ad4", false);
		// ���ʹ�û��ֹ�棬����ص��û��ֹ��ĳ�ʼ���ӿڣ�
		OffersManager.getInstance(this).onAppLaunch();
		// �岥�ӿڵ���
		// �����߿��Ե������ߺ�̨����չʾƵ�ʣ���Ҫ�������ߺ�̨����ҳ�棨��ϸ��Ϣ->ҵ����Ϣ->�޻��ֹ��ҵ��->�߼����ã�
		// ��4.03�汾�����ƿ����Ƿ�������㹦�ܣ���Ҫ�������ߺ�̨����ҳ�棨��ϸ��Ϣ->ҵ����Ϣ->�޻��ֹ��ҵ��->�߼����ã�

		// ���ز岥��Դ
		SpotManager.getInstance(this).loadSpotAds();
		// ����չʾʱ�䣬���س�ʱʱ��չʾ��棬Ĭ��Ϊ0���������ó�ʱʱ��
		SpotManager.getInstance(this).setSpotTimeout(5000);// 5��

	}

	private static Interpolator interpolator = new Interpolator() {

		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}
	};

	/**
	 * ��ʼ������
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
			// ��ʾ�岥���
			SpotManager.getInstance(this).showSpotAds(this,
					new SpotDialogListener() {

						@Override
						public void onShowSuccess() {
							Log.i("SpotAd", "չʾ�ɹ�");
						}

						@Override
						public void onShowFailed() {
							Log.i("SpotAd", "չʾʧ��");
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
		case R.id.menu_about: // ����
			new AlertDialog.Builder(this)
					.setMessage("����Android�ͻ���\n\n���ߣ�IT_xiaoС��")
					.setPositiveButton("�ر�", null).show();
			break;
		case R.id.menu_quit: // �˳�
			finish();
			System.exit(0);
			break;
		}
		return true;
	}

	private Fragment mContent;

	/**
	 * �л���ͼ
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
		if (KeyEvent.KEYCODE_BACK == keyCode) { // �����ؼ����˳�Ӧ��
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
		// �ر��������
		Tools.getGlobalObject(this).closeWorkQueue();
		super.finish();
	}
}
