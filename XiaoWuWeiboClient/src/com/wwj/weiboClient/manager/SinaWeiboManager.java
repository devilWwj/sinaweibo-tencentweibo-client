package com.wwj.weiboClient.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.sina.weibo.sdk.api.share.ApiUtils.WeiboInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboParameters;
import com.sina.weibo.sdk.auth.WeiboAuth.AuthInfo;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.openapi.legacy.CommentsAPI;
import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.sina.weibo.sdk.openapi.legacy.FriendshipsAPI;
import com.sina.weibo.sdk.openapi.legacy.StatusesAPI;
import com.sina.weibo.sdk.openapi.legacy.UsersAPI;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI.AUTHOR_FILTER;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI.FEATURE;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI.SRC_FILTER;
import com.sina.weibo.sdk.openapi.legacy.WeiboAPI.TYPE_FILTER;
import com.sina.weibo.sdk.utils.Utility;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.listener.impl.SinaAuthListener;
import com.wwj.weiboClient.model.Favorite;
import com.wwj.weiboClient.model.SinaConstants;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.AccessTokenKeeper;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.WorkQueueStorage;
import com.wwj.weiboClient.workqueue.task.PullFileTask;

/**
 * ������΢�������ࣨ��������Android SDK����
 * 
 * @author wwj
 * 
 */
public class SinaWeiboManager implements Const {
	// ����΢��Web��Ȩ�࣬�ṩ��¼�ȹ���
	private static WeiboAuth mWeiboAuth;
	public static String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

	/**
	 * �����ȨUrl
	 * 
	 * @param context
	 * @return
	 */
	public static String getAuthUrl(Context context) {
		WeiboParameters parameters = new WeiboParameters();
		parameters.add("client_id", SinaConstants.APP_KEY);
		parameters.add("response_type", "token");
		parameters.add("redirect_uri", SinaConstants.REDIRECT_URL);
		parameters.add("display", "mobile");
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (accessToken.isSessionValid()) {
			parameters.add("access_token", accessToken.getToken());
		}
		String url = URL_OAUTH2_ACCESS_AUTHORIZE + "?"
				+ Utility.encodeUrl(parameters);
		return url;

	}

	public static boolean hasPicture(Status status) {
		if (status.thumbnail_pic != null && !"".equals(status.thumbnail_pic))
			return true;
		if (status.retweeted_status != null) {
			if (status.retweeted_status.thumbnail_pic != null
					&& !"".equals(status.retweeted_status.thumbnail_pic)) {
				return true;
			}
		}
		return false;
	}

	// ���ȴ�SD����������ȡͼ��·������������ڣ���url��ӵ��������У����ҷ���null
	// url������web��ַ�������ص��ǻ��������ļ�������url��hashcode������
	public static String getImageurl(String url, Activity context) {
		return getImageurl(url, null, context);
	}

	public static String getImageurl(String url, DoneAndProcess doneAndProcess,
			Activity context) {
		String result = null;
		if (url == null || "".equals(url))
			return result;
		// result����ͼ���ļ���Ӧ�ı����ļ�·��
		result = PATH_FILE_CACHE + "/" + url.hashCode();
		File file = new File(PATH_FILE_CACHE + "/" + url.hashCode());
		// ����ļ��Ѵ��ھ�ֱ�ӷ����ļ���
		if (file.exists()) {
			return result;
		} else {
			WorkQueueStorage workQueueStorage = Tools.getGlobalObject(context)
					.getWorkQueueStorage();
			if (workQueueStorage != null) {
				if (doneAndProcess == null) {
					workQueueStorage.addDoingWebFileUrl(url);
				} else {
					PullFileTask pullFileTask = new PullFileTask();
					pullFileTask.doneAndProcess = doneAndProcess;
					pullFileTask.fileUrl = url;
					workQueueStorage.addTask(pullFileTask);
				}
			}
			result = null;
		}
		return result;
	}

	/**
	 * ��ȡʱ����΢��
	 * 
	 * @param context
	 * @param accessToken
	 * @param since_id
	 * @param max_id
	 * @param count
	 * @param listener
	 */
	public static void getHomeTimeline(Context context, long since_id,
			long max_id, int count, RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		StatusesAPI statusesAPI = new StatusesAPI(accessToken);
		statusesAPI.homeTimeline(since_id, max_id, count, 1, false,
				FEATURE.ALL, false, listener);
	}

	/**
	 * ��ȡ�û�����
	 * 
	 * @param context
	 * @param uid
	 * @param listener
	 */
	public static void getUserInfo(Context context, long uid,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		// �����û�����
		UsersAPI usersAPI = new UsersAPI(accessToken);
		usersAPI.show(uid, listener);
	}

	/**
	 * ��ȡ��ǰ�û���������
	 * 
	 * @param context
	 * @param listener
	 */
	public static void getCommentTimeline(Context context,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		// ���ص�ǰ�û���������
		CommentsAPI commentsAPI = new CommentsAPI(accessToken);
		commentsAPI.timeline(0, 0, 50, 1, false, listener);
	}

	/**
	 * ��ȡ@�ҵ�΢��
	 * 
	 * @param context
	 * @param listener
	 */
	public static void getMentions(Context context, long since_id, long max_id,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		StatusesAPI statusesAPI = new StatusesAPI(accessToken);
		statusesAPI.mentions(since_id, max_id, 50, 1, AUTHOR_FILTER.ALL,
				SRC_FILTER.ALL, TYPE_FILTER.ALL, false, listener);
	}

	/**
	 * ��ȡ��ǰ�û����ղ�΢��
	 * 
	 * @param context
	 * @param listener
	 */
	public static void getFavorites(Context context, int page,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		FavoritesAPI favoritesAPI = new FavoritesAPI(
				AccessTokenKeeper.readAccessToken(context));
		favoritesAPI.favorites(DEFAULT_STATUS_COUNT, page, listener);
	}

	public static List<Status> FavoriteToStatus(List<Favorite> favorites) {
		List<Status> statuses = new ArrayList<Status>();
		for (Favorite favorite : favorites) {
			favorite.status.favorited = true;
			statuses.add(favorite.status);
		}
		return statuses;

	}

	/**
	 * ��ȡ��ǰ�û����·����΢���б�
	 * 
	 * @param context
	 * @param listener
	 */
	public static void getUserTimeline(Context context, long since_id,
			long max_id, RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		StatusesAPI statusesAPI = new StatusesAPI(accessToken);
		statusesAPI.userTimeline(since_id, max_id, DEFAULT_STATUS_COUNT,
				DEFAULT_STATUS_PAGE, false, FEATURE.ALL, false, listener);
	}

	/**
	 * ����΢��ID����ĳ��΢���������б�
	 * 
	 * @param context
	 * @param id
	 * @param listener
	 */
	public static void getComments(Context context, long id,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		CommentsAPI commentsAPI = new CommentsAPI(accessToken);
		commentsAPI.show(id, 0, 0, 50, 1, AUTHOR_FILTER.ALL, listener);
	}

	/**
	 * ��ʾ����΢��
	 * 
	 * @param context
	 * @param id
	 * @param listener
	 */
	public static void showStatus(Context context, long id,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		StatusesAPI statusesAPI = new StatusesAPI(accessToken);
		statusesAPI.show(id, listener);
	}

	public static void addLocation(Context context, RequestListener listener) {
		//
	}

	/**
	 * ��ȡUID�û���ע���û�
	 * 
	 * @param context
	 * @param uid
	 * @param cursor
	 * @param listener
	 */
	public static void showFriends(Context context, long uid, int cursor,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		FriendshipsAPI friendshipsAPI = new FriendshipsAPI(accessToken);
		friendshipsAPI.friends(uid, 50, cursor, true, listener);

	}

	/**
	 * ��ȡUID�û��ķ�˿
	 * 
	 * @param context
	 * @param uid
	 * @param cursor
	 * @param listener
	 */
	public static void showFollows(Context context, long uid, int cursor,
			RequestListener listener) {
		// ����΢����Ȩ
		mWeiboAuth = new WeiboAuth(context, SinaConstants.APP_KEY,
				SinaConstants.REDIRECT_URL, SinaConstants.SCOPE);
		Oauth2AccessToken accessToken = AccessTokenKeeper
				.readAccessToken(context);
		if (!accessToken.isSessionValid()) {
			Toast.makeText(context, "��Ȩ�ѹ��ڣ���������Ȩ", Toast.LENGTH_SHORT).show();
			mWeiboAuth.anthorize(new SinaAuthListener(context));
		}
		FriendshipsAPI friendshipsAPI = new FriendshipsAPI(accessToken);
		friendshipsAPI.followers(uid, 50, cursor, true, listener);
	}

}
