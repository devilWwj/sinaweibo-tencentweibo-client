package com.wwj.weiboClient.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.sax.StartElementListener;

import com.tencent.weibo.sdk.android.api.BaseAPI;
import com.tencent.weibo.sdk.android.api.FriendAPI;
import com.tencent.weibo.sdk.android.api.LbsAPI;
import com.tencent.weibo.sdk.android.api.PublishWeiBoAPI;
import com.tencent.weibo.sdk.android.api.TimeLineAPI;
import com.tencent.weibo.sdk.android.api.UserAPI;
import com.tencent.weibo.sdk.android.api.WeiboAPI;
import com.tencent.weibo.sdk.android.api.util.Util;
import com.tencent.weibo.sdk.android.model.AccountModel;
import com.tencent.weibo.sdk.android.model.BaseVO;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.tencent.weibo.sdk.android.network.ReqParam;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.util.StringUtil;

/**
 * ��Ѷ΢��API������
 * 
 * @author by wwj
 * 
 */
public class TencentWeiboManager extends BaseAPI {

	public TencentWeiboManager(AccountModel account) {
		super(account);
	}

	private static AccountModel account;
	private static TimeLineAPI timeLineAPI; // ʱ����API
	private static FriendAPI friendAPI; // �������API
	private static UserAPI userAPI;// �ʻ����API
	private static WeiboAPI weiboAPI;// ΢�����API
	private static LbsAPI lbsAPI;// LBS���API
	private static PublishWeiBoAPI publishWeiBoAPI;

	/** ΢���ӿ� **/
	private static final String SERVER_URL_SHOW = API_SERVER + "/t/show"; // ��ȡ΢����ϸ��Ϣ
	private static final String SERVER_URL_READD = API_SERVER + "/t/re_add";// ת��΢��
	private static final String SERVER_URL_COMMENT = API_SERVER + "/t/comment"; // ����΢��
	private static final String SERVER_URL_FAV = API_SERVER + "/fav/addt"; // �ղ�΢��
	private static final String SERVER_URL_UNFAV = API_SERVER + "/fav/delt"; // ȡ���ղ�
	private static final String SERVER_URL_FAV_LIST = API_SERVER
			+ "/fav/list_t"; // �ղ��б�
	private static final String SERVER_URL_MENTIONS_TILELINE = API_SERVER
			+ "/statuses/mentions_timeline";// ��ȡ@�Լ������µ�΢����Ϣ

	private static double longitude = 0d;
	private static double latitude = 0d;

	public static int type_original_work = 0x1; // ԭ������
	public static int type_retweet = 0x2; // ת��
	public static int type_reply = 0x8; // �ظ�
	public static int type_empty_reply = 0x10; // �ջ�
	public static int type_mention = 0x20; // �ἰ
	public static int type_comment = 0x40; // ����

	/**
	 * ��ø�������
	 * 
	 * @param context
	 * @param requestFormat
	 * @param httpCallback
	 */
	public static void getUserInfo(Context context, String requestFormat,
			HttpCallback httpCallback) {
		account = new AccountModel(GlobalObject.getTencentAccessToken(context));
		userAPI = new UserAPI(account);
		userAPI.getUserInfo(context, requestFormat, httpCallback, null,
				BaseVO.TYPE_JSON);
	}

	/**
	 * ��ȡ�����˵�����
	 * 
	 * @param context
	 * @param requestFormat
	 * @param name
	 * @param fopenid
	 * @param httpCallback
	 */
	public static void getOtherUserInfo(Context context, String requestFormat,
			String name, String fopenid, HttpCallback httpCallback) {
		account = new AccountModel(GlobalObject.getTencentAccessToken(context));
		userAPI = new UserAPI(account);
		userAPI.getUserOtherInfo(context, requestFormat, name, fopenid,
				httpCallback, null, BaseVO.TYPE_JSON);
	}

	/**
	 * ���ʱ����΢��
	 * 
	 * @param context
	 * @param requestFormat
	 * @param httpCallback
	 */
	public static void getHomeTimeline(Context context, String accessToken,
			int pageFlag, int reqNum, String requestFormat,
			HttpCallback httpCallback) {
		// account = new
		// AccountModel(GlobalObject.getTencentAccessToken(context));
		account = new AccountModel(accessToken);
		timeLineAPI = new TimeLineAPI(account);
		timeLineAPI.getHomeTimeLine(context, pageFlag, 0, reqNum, 0, 0,
				requestFormat, httpCallback, null, BaseVO.TYPE_JSON);

	}

	/**
	 * ��ȡ��ǰ��¼�û�΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param pageFlag
	 * @param reqNum
	 * @param requestFormat
	 * @param httpCallback
	 */
	public static void getUserTimeline(Context context, String accessToken,
			int pageFlag, int reqNum, String name, String requestFormat,
			HttpCallback httpCallback) {
		account = new AccountModel(accessToken);
		timeLineAPI = new TimeLineAPI(account);
		timeLineAPI.getUserTimeLine(context, pageFlag, 0, reqNum, 0,
				name, null, 0, 0, requestFormat, httpCallback, null,
				BaseVO.TYPE_JSON);
	}

	/**
	 * ��ʾ����΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param requestFormat
	 * @param id
	 * @param httpCallback
	 */
	public static void showWeiboDetail(Context context, String accessToken,
			String requestFormat, String id, HttpCallback httpCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.getWeiboDetail(context, requestFormat, id, httpCallback, null,
				BaseVO.TYPE_JSON);
	}

	/**
	 * ��ȡ΢����ϸ����
	 * 
	 * @param context
	 * @param format
	 * @param id
	 * @param mCallback
	 * @param mTargetClass
	 * @param resultType
	 */
	public void getWeiboDetail(Context context, String format, String id,
			HttpCallback mCallback, Class<? extends BaseVO> mTargetClass,
			int resultType) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("id", id);
		startRequest(context, SERVER_URL_SHOW, mParam, mCallback, mTargetClass,
				BaseAPI.HTTPMETHOD_GET, resultType);
	}

	/**
	 * ����һ��΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param content
	 * @param mCallBack
	 */
	public static void addWeibo(Context context, String accessToken,
			String content, HttpCallback mCallBack) {
		account = new AccountModel(accessToken);
		weiboAPI = new WeiboAPI(account);
		weiboAPI.addWeibo(context, content, StringUtil.REQUEST_FORMAT,
				longitude, latitude, 0, 0, mCallBack, null, BaseVO.TYPE_JSON);
	}

	/**
	 * ������ͼƬ΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param content
	 * @param bm
	 * @param mCallBack
	 */
	public static void addPicWeibo(Context context, String accessToken,
			String content, Bitmap bm, HttpCallback mCallBack) {
		account = new AccountModel(accessToken);
		weiboAPI = new WeiboAPI(account);
		weiboAPI.addPic(context, content, StringUtil.REQUEST_FORMAT, longitude,
				latitude, bm, 0, 0, mCallBack, null, BaseVO.TYPE_JSON);
	}

	/**
	 * ת��΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param content
	 * @param reid
	 * @param mCallback
	 */
	public static void reAddWeibo(Context context, String accessToken,
			String content, String reid, HttpCallback mCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.reAdd(context, StringUtil.REQUEST_FORMAT, content, reid,
				mCallback, null, BaseVO.TYPE_JSON);
	}

	public void reAdd(Context context, String format, String content,
			String reid, HttpCallback mCallback,
			Class<? extends BaseVO> mTargetClass, int resultType) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("content", content);
		mParam.addParam("clientip", "127.0.0.1");
		mParam.addParam("reid", reid);
		startRequest(context, SERVER_URL_READD, mParam, mCallback,
				mTargetClass, BaseAPI.HTTPMETHOD_POST, resultType);
	}

	/**
	 * ����΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param content
	 * @param reid
	 * @param mCallback
	 */
	public static void commentWeibo(Context context, String accessToken,
			String content, String reid, HttpCallback mCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.comment(context, StringUtil.REQUEST_FORMAT, content, reid,
				mCallback, null, BaseVO.TYPE_JSON);
	}

	public void comment(Context context, String format, String content,
			String reid, HttpCallback mCallback,
			Class<? extends BaseVO> mTargetClass, int resultType) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("content", content);
		mParam.addParam("clientip", "127.0.0.1");
		mParam.addParam("reid", reid);
		startRequest(context, SERVER_URL_COMMENT, mParam, mCallback,
				mTargetClass, BaseAPI.HTTPMETHOD_POST, resultType);
	}

	/**
	 * �ղ�΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param requestFormat
	 * @param id
	 * @param httpCallback
	 */
	public static void favoriteWeibo(Context context, String accessToken,
			String requestFormat, String id, HttpCallback httpCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.favorite(context, requestFormat, id, httpCallback, null,
				BaseVO.TYPE_JSON);
	}

	/**
	 * �ղ�΢��api
	 * 
	 * @param context
	 * @param format
	 * @param id
	 * @param mCallback
	 * @param mTargetClass
	 * @param resultType
	 */
	public void favorite(Context context, String format, String id,
			HttpCallback mCallback, Class<? extends BaseVO> mTargetClass,
			int resultType) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("id", id);
		startRequest(context, SERVER_URL_FAV, mParam, mCallback, mTargetClass,
				BaseAPI.HTTPMETHOD_POST, resultType);
	}

	/**
	 * �ղ�΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param requestFormat
	 * @param id
	 * @param httpCallback
	 */
	public static void unFavoriteWeibo(Context context, String accessToken,
			String requestFormat, String id, HttpCallback httpCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.unFavorite(context, requestFormat, id, httpCallback, null,
				BaseVO.TYPE_JSON);
	}

	/**
	 * �ղ�΢��api
	 * 
	 * @param context
	 * @param format
	 * @param id
	 * @param mCallback
	 * @param mTargetClass
	 * @param resultType
	 */
	public void unFavorite(Context context, String format, String id,
			HttpCallback mCallback, Class<? extends BaseVO> mTargetClass,
			int resultType) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("id", id);
		startRequest(context, SERVER_URL_UNFAV, mParam, mCallback,
				mTargetClass, BaseAPI.HTTPMETHOD_POST, resultType);
	}

	public static void getReAddList(Context context, String accessToken,
			String rootid, HttpCallback mCallback) {
		account = new AccountModel(accessToken);
		weiboAPI = new WeiboAPI(account);
		weiboAPI.reList(context, StringUtil.REQUEST_FORMAT, 2, rootid, 0, "0",
				30, "0", mCallback, null, BaseVO.TYPE_JSON);
	}

	/**
	 * ����ղ�΢���б�
	 * 
	 * @param context
	 * @param requestFormat
	 * @param httpCallback
	 */
	public static void getFavList(Context context, String accessToken,
			int pageFlag, int reqNum, String requestFormat,
			HttpCallback httpCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);

		manager.favList(context, StringUtil.REQUEST_FORMAT, pageFlag, reqNum,
				"0", 0, httpCallback);

	}

	public void favList(Context context, String format, int pageFlag,
			int reqNum, String pageTime, int lastid, HttpCallback mCallback) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("pageflag", pageFlag);
		mParam.addParam("pagetime", pageTime);
		mParam.addParam("reqnum", reqNum);
		mParam.addParam("lastid", lastid);
		startRequest(context, SERVER_URL_FAV_LIST, mParam, mCallback, null,
				BaseAPI.HTTPMETHOD_GET, BaseVO.TYPE_JSON);
	}

	/***
	 * 
	 * @param context
	 * @param accessToken
	 * @param pageFlag
	 * @param reqNum
	 * @param mCallback
	 */
	public static void getMetionsTimeline(Context context, String accessToken,
			int pageFlag, int reqNum, int type, HttpCallback mCallback) {
		account = new AccountModel(accessToken);
		TencentWeiboManager manager = new TencentWeiboManager(account);
		manager.mentionList(context, StringUtil.REQUEST_FORMAT, pageFlag,
				reqNum, "0", 0, type, 0, mCallback);
	}

	public void mentionList(Context context, String format, int pageFlag,
			int reqNum, String pageTime, int lastid, int type, int contentType,
			HttpCallback mCallBack) {
		ReqParam mParam = new ReqParam();
		mParam.addParam("scope", "all");
		mParam.addParam("clientip", Util.getLocalIPAddress(context));
		mParam.addParam("oauth_version", "2.a");
		mParam.addParam("oauth_consumer_key",
				Util.getSharePersistent(context, "CLIENT_ID"));
		mParam.addParam("openid", Util.getSharePersistent(context, "OPEN_ID"));
		mParam.addParam("format", format);
		mParam.addParam("pageflag", pageFlag);
		mParam.addParam("pagetime", pageTime);
		mParam.addParam("reqnum", reqNum);
		mParam.addParam("lastid", lastid);
		mParam.addParam("type", type);
		mParam.addParam("contenttype", contentType);
		startRequest(context, SERVER_URL_MENTIONS_TILELINE, mParam, mCallBack,
				null, HTTPMETHOD_GET, BaseVO.TYPE_JSON);
	}

	/**
	 * ��ȡ�����ҵ��û�
	 * 
	 * @param context
	 * @param accessToken
	 * @param callback
	 */
	public static void getFriendsfansList(Context context, String accessToken,
			HttpCallback mCallBack) {
		account = new AccountModel(accessToken);
		friendAPI = new FriendAPI(account);
		friendAPI.friendFansList(context, StringUtil.REQUEST_FORMAT, 30, 0, 1,
				0, 0, mCallBack, null, BaseVO.TYPE_JSON);
	}

	/**
	 * ��ȡ���������û�
	 * 
	 * @param context
	 * @param accessToken
	 * @param callback
	 */
	public static void getFriendsList(Context context, String accessToken,
			HttpCallback callback) {
		account = new AccountModel(accessToken);
		friendAPI = new FriendAPI(account);
		friendAPI.friendIDolList(context, StringUtil.REQUEST_FORMAT, 30, 0, 1,
				0, callback, null, BaseVO.TYPE_JSON);
	}
}
