package com.wwj.weiboClient.workqueue.task.process;

import android.content.Context;

import com.sina.weibo.sdk.openapi.legacy.FavoritesAPI;
import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.workqueue.task.FavoriteWeiboTask;

/**
 * 收藏微博
 * 
 * 调用新浪微博收藏API
 * 
 * @author wwj
 * 
 */
public class FavoriteWeiboProcess {
	private Context context;

	public FavoriteWeiboProcess(Context context) {
		super();
		this.context = context;
	}

	public void process(FavoriteWeiboTask task) throws Exception {
		FavoritesAPI favoritesAPI = new FavoritesAPI(
				GlobalObject.getSinaAccessToken(context));
		if (!task.fav) {
			favoritesAPI.destroy(task.id, null);
			return;
		}
		favoritesAPI.create(task.id, null);
	}

}
