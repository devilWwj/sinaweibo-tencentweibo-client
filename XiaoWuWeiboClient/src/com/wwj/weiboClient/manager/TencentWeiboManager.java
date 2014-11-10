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
 * 腾讯微博API管理类
 * 
 * @author by wwj
 * 
 */
public class TencentWeiboManager extends BaseAPI {

	public TencentWeiboManager(AccountModel account) {
		super(account);
	}

	private static AccountModel account;
	private static TimeLineAPI timeLineAPI; // 时间线API
	private static FriendAPI friendAPI; // 好友相关API
	private static UserAPI userAPI;// 帐户相关API
	private static WeiboAPI weiboAPI;// 微博相关API
	private static LbsAPI lbsAPI;// LBS相关API
	private static PublishWeiBoAPI publishWeiBoAPI;

	/** 微博接口 **/
	private static final String SERVER_URL_SHOW = API_SERVER + "/t/show"; // 获取微博详细信息
	private static final String SERVER_URL_READD = API_SERVER + "/t/re_add";// 转播微博
	private static final String SERVER_URL_COMMENT = API_SERVER + "/t/comment"; // 评论微博
	private static final String SERVER_URL_FAV = API_SERVER + "/fav/addt"; // 收藏微博
	private static final String SERVER_URL_UNFAV = API_SERVER + "/fav/delt"; // 取消收藏
	private static final String SERVER_URL_FAV_LIST = API_SERVER
			+ "/fav/list_t"; // 收藏列表
	private static final String SERVER_URL_MENTIONS_TILELINE = API_SERVER
			+ "/statuses/mentions_timeline";// 获取@自己的最新的微博消息

	private static double longitude = 0d;
	private static double latitude = 0d;

	public static int type_original_work = 0x1; // 原创发表
	public static int type_retweet = 0x2; // 转载
	public static int type_reply = 0x8; // 回复
	public static int type_empty_reply = 0x10; // 空回
	public static int type_mention = 0x20; // 提及
	public static int type_comment = 0x40; // 评论

	/**
	 * 获得个人资料
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
	 * 获取其他人的资料
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
	 * 获得时间线微博
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
	 * 获取当前登录用户微博
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
	 * 显示单条微博
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
	 * 获取微博详细内容
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
	 * 发布一条微博
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
	 * 发布带图片微博
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
	 * 转播微博
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
	 * 评论微博
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
	 * 收藏微博
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
	 * 收藏微博api
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
	 * 收藏微博
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
	 * 收藏微博api
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
	 * 获得收藏微博列表
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
	 * 获取收听我的用户
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
	 * 获取我收听的用户
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
