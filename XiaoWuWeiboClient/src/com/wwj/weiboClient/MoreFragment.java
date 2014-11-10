package com.wwj.weiboClient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.Toast;

import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeConfig;
import com.umeng.socialize.controller.RequestType;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.UMWXHandler;
import com.umeng.socialize.media.UMImage;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.AppVersionLatest;
import com.wwj.weiboClient.update.UpdateManager;
import com.wwj.weiboClient.util.DialogHelp;
import com.wwj.weiboClient.util.NetWorkUtil;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;

public class MoreFragment extends Fragment implements Const, OnClickListener {
	private Context mContext;
	private TableRow more_page_favorites; // �ҵ��ղ�
	private TableRow more_page_official; // �ٷ�΢��
	private TableRow more_page_account; // �ʺŹ���
	private TableRow more_page_feedback; // �û�����
	private TableRow more_page_invitefriends; // �������
	private TableRow more_page_settings; // ����
	private TableRow more_page_checkupdate; // ������
	private TableRow more_page_about; // ����
	private View rootView;

	private int weiboType;
	private UMSocialService mController = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mContext = getActivity();
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.frg_more, container, false);
		}
		// �����rootView��Ҫ�ж��Ƿ��Ѿ����ӹ�parent��
		// �����parent��Ҫ��parentɾ����Ҫ��Ȼ�ᷢ�����rootview�Ѿ���parent�Ĵ���
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null) {
			parent.removeView(rootView);
		}
		weiboType = StorageManager.getValue(mContext, StringUtil.LOGIN_TYPE, 0);
		findViews(rootView);
		registerBrocastReceiver();
		initialUM();
		return rootView;
		
	}

	private void findViews(View parentView) {
		more_page_favorites = (TableRow) parentView
				.findViewById(R.id.more_page_favorites);
		more_page_official = (TableRow) parentView
				.findViewById(R.id.more_page_official);
		more_page_account = (TableRow) parentView
				.findViewById(R.id.more_page_account);
		more_page_feedback = (TableRow) parentView
				.findViewById(R.id.more_page_feedback);
		more_page_invitefriends = (TableRow) parentView
				.findViewById(R.id.more_page_invitefriends);
		more_page_settings = (TableRow) parentView
				.findViewById(R.id.more_page_settings);
		more_page_checkupdate = (TableRow) parentView
				.findViewById(R.id.more_page_checkupdate);
		more_page_about = (TableRow) parentView
				.findViewById(R.id.more_page_about);
		more_page_favorites.setOnClickListener(this);
		more_page_official.setOnClickListener(this);
		more_page_account.setOnClickListener(this);
		more_page_feedback.setOnClickListener(this);
		more_page_invitefriends.setOnClickListener(this);
		more_page_settings.setOnClickListener(this);
		more_page_checkupdate.setOnClickListener(this);
		more_page_about.setOnClickListener(this);
	}

	private void initialUM() {
		// ����������Activity��������³�Ա����
		mController = UMServiceFactory.getUMSocialService("com.umeng.share",
				RequestType.SOCIAL);
		// ���÷�������
		mController
				.setShareContent("������ữ�����SDK�����ƶ�Ӧ�ÿ��������罻�����ܣ�http://www.umeng.com/social");
		mController
				.setShareMedia(new UMImage(mContext, R.drawable.ic_launcher)); // ���÷���ͼƬ����

		SocializeConfig config = mController.getConfig();
		// ��ͨ����
		config.setShareSms(true);
		config.setShareMail(true);

		config.setPlatforms(SHARE_MEDIA.QZONE, SHARE_MEDIA.TENCENT);
		// wx967daebe835fbeac������΢�ſ���ƽ̨ע��Ӧ�õ�AppID, ������Ҫ�滻����ע���AppID
		String appID = "wxfc7bdd52daa288f9";
		// ΢��ͼ�ķ����������һ��Url
		String contentUrl = "http://t.cn/zTXUNMu";
		// ���΢��ƽ̨������1Ϊ��ǰActivity�� ����2Ϊ�û�����AppID,����3Ϊ�������������ת����Ŀ��url
		UMWXHandler wxHandler = config.supportWXPlatform(getActivity(), appID,
				contentUrl);
		// ���÷������
		wxHandler.setWXTitle("����΢���ͻ��˲���");
		// ֧��΢������Ȧ
		UMWXHandler circleHandler = config.supportWXCirclePlatform(
				getActivity(), appID, contentUrl);
		circleHandler.setCircleTitle("����΢���ͻ��˻�����..");
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.more_page_favorites: // �ҵ��ղ�
			viewFavoriteList();
			break;
		case R.id.more_page_official: // �ٷ�΢��
			viewOfficialWeibo();
//			Toast.makeText(mContext, "����ʵ��", Toast.LENGTH_SHORT).show();
			break;
		case R.id.more_page_account:
			intent = new Intent(mContext, AccountManage.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.more_page_feedback: // �û�����
			// Toast.makeText(mContext, "����ʵ��", Toast.LENGTH_SHORT).show();
			FeedbackAgent agent = new FeedbackAgent(getActivity());
			agent.startFeedbackActivity();
			break;
		case R.id.more_page_invitefriends: // ��������
			// Toast.makeText(mContext, "����ʵ��", Toast.LENGTH_SHORT).show();
			mController.openShare(getActivity(), false);
			break;
		case R.id.more_page_settings: // ����
			intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
			startActivity(intent);
			break;
		case R.id.more_page_checkupdate: // ������
			Toast.makeText(mContext, "��ǰΪ���°汾", Toast.LENGTH_LONG).show();
			// updateLevel();
			break;
		case R.id.more_page_about: // ����
			new AlertDialog.Builder(mContext)
					.setMessage("����Android�ͻ���\n\n���ߣ�IT_xiaoС��")
					.setPositiveButton("�ر�", null).show();
			break;
		default:
			break;
		}

	}

	/** �鿴�ղ��б� **/
	private void viewFavoriteList() {
		Intent intent = null;
		switch (weiboType) {
		case SINA:
			intent = new Intent(mContext, SinaWeiboViewer.class);
			intent.putExtra("title", "�ղ�");
			intent.putExtra("type", MESSAGE_FAVORITE);
			break;
		case TENCENT:
			intent = new Intent(mContext, TencentWeiboViewer.class);
			intent.putExtra("title", "�ҵ��ղ�");
			intent.putExtra("type", MESSAGE_FAVORITE);
			break;
		}
		startActivity(intent);
	}
	
	/** �鿴�ղ��б� **/
	private void viewOfficialWeibo() {
		Intent intent = null;
		switch (weiboType) {
		case SINA:	
			intent = new Intent(mContext, SinaWeiboViewer.class);
			intent.putExtra("title", "�ٷ�΢��");
			intent.putExtra("type", USER_TIMELINE);
			break;
		case TENCENT:
			intent = new Intent(mContext, TencentWeiboViewer.class);
			intent.putExtra("title", "�ٷ�΢��");
			intent.putExtra("type", USER_TIMELINE);
			intent.putExtra("name", "api_weibo");
			break;
		}
		startActivity(intent);
	}

	private UpdateManager mLeverUp;
	// public static String URL =
	// "http://219.140.63.168:9080/mwayadmin/appUpdate";
	public static final String CHECK_UPDATE_URL = "http://www.159758.com:7893/AppMgr/app/reqXml.do?id=2oIXMM";

	// public static String URL =
	// "http://gdown.baidu.com/data/wisegame/f34afa8d6d747583/weibo_811.apk";
	// http://gdown.baidu.com/data/wisegame/f34afa8d6d747583/weibo_811.apk

	private void updateLevel() {
		// ����
		if (!NetWorkUtil.checkNetWorkAvailable(mContext)) {
			return;
		}
		if (!Tools.inquiresSDCard()) {
			ToastUtil.showShortToast(mContext, "û���ҵ�SD��");
			return;
		}

		// 18928389198
		mLeverUp = new UpdateManager(mContext, CHECK_UPDATE_URL, handler);
		mLeverUp.checkUpdate(mContext);
		DialogHelp help = DialogHelp.getInstance();
		help.showHttpDialog(mContext, R.string.alert, "���ڼ�����...");
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			// ȡ���Ի���
			DialogHelp help = DialogHelp.getInstance();
			if (!help.isHttpDialogShow()) {
				return;
			} else {
				help.dismissDialog();
			}
			Log.d("update", "recieve message: " + msg.what);
			switch (msg.what) {
			case UpdateManager.HAS_NEW_VERSION: // �°汾
				AppVersionLatest app = (AppVersionLatest) msg.obj;
				if (mLeverUp != null) {
					mLeverUp.updateDialog(app);
				} else {
					new NullPointerException("mLevelUp is null");
				}
				break;
			case UpdateManager.NO_NEW_VERSION: // û���°汾
				ToastUtil.showShortToast(mContext, (String) msg.obj);
				break;
			case UpdateManager.CONNECTION_FAIL: // ����ʧ��
				ToastUtil.showShortToast(mContext, (String) msg.obj);
				Log.d("update", "recieve message: " + (String) msg.obj);
				break;

			}
		};
	};

	public void registerBrocastReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(StringUtil.ACCOUNT_CHANGE);
		// ע��㲥
		mContext.registerReceiver(mBroadcastReceiver, intentFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			weiboType = intent.getIntExtra(StringUtil.WEIBO_TYPE, 0);
			String action = intent.getAction();
			String accessToken = intent.getStringExtra(StringUtil.ACCESS_TOKEN);
			if (action.equals(StringUtil.ACCOUNT_CHANGE)) {

			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case ACCOUNT_RESULT_CODE: // �˺Ź��������Ľ��
			SlidingMenuFragment.refreshAccounts();
			SlidingMenuFragment.accountAdapter.notifyDataSetChanged();
			if (weiboType == SINA
					&& SlidingMenuFragment.sinaUserNames.size() == 0) {
				SlidingMenuFragment.ivHead
						.setImageResource(R.drawable.avatar_default);
				SlidingMenuFragment.ivVerified.setVisibility(View.GONE);
			} else if (weiboType == TENCENT
					&& SlidingMenuFragment.tencentUserNames.size() == 0) {
				SlidingMenuFragment.ivHead
						.setImageResource(R.drawable.avatar_default);
				SlidingMenuFragment.ivVerified.setVisibility(View.GONE);
			}
			break;
		}
	}
}
