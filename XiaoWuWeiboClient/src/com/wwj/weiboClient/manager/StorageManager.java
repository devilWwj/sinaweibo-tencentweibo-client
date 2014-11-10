package com.wwj.weiboClient.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.model.Comment;
import com.wwj.weiboClient.model.Status;

/**
 * 存储管理类
 * 
 * @author wwj
 * 
 */
public class StorageManager implements Const {

	/**
	 * 保存字符串数据
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setValue(Context context, String key, String value) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		spf.edit().putString(key, value).commit();
	}

	public static void setValue(Context context, String key, boolean value) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		spf.edit().putBoolean(key, value).commit();
	}

	public static void setValue(Context context, String key, long value) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		spf.edit().putLong(key, value).commit();
	}

	public static void setValue(Context context, String key, int value) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		spf.edit().putInt(key, value).commit();
	}

	/**
	 * 得到用SharedPreferences存储的字符串
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getValue(Context context, String key,
			String defaultValue) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		return spf.getString(key, defaultValue);

	}

	public static boolean getValue(Context context, String key,
			boolean defaultValue) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		return spf.getBoolean(key, defaultValue);

	}

	public static long getValue(Context context, String key, long defaultValue) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		return spf.getLong(key, defaultValue);

	}

	public static int getValue(Context context, String key, int defaultValue) {
		SharedPreferences spf = context.getSharedPreferences(Const.CONFIG_NAME,
				Context.MODE_PRIVATE);
		return spf.getInt(key, defaultValue);

	}

	public static String saveBitmap(Bitmap bitmap) {
		return saveBitmap(bitmap, null);
	}

	/**
	 * 保存图片
	 * 
	 * @param bitmap
	 * @param filename
	 * @return
	 */
	public static String saveBitmap(Bitmap bitmap, String filename) {
		String fn = filename;
		if (fn == null) {
			File file = new File(PATH_IMAGE);
			if (!file.exists())
				file.mkdirs();
			fn = PATH_IMAGE + "/" + String.valueOf(new Random().nextLong())
					+ ".jpg";
			while (new File(fn).exists()) {
				fn = PATH_IMAGE + "/" + String.valueOf(new Random().nextLong())
						+ ".jpg";
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(fn);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fn;
	}

	public static void saveList(List list, int type) {
		try {
			saveList(list, PATH_STORAGE, type);
		} catch (Exception e) {
		}
	}

	public static void saveList(List list, String path, int type)
			throws Exception {
		if (list == null || list.size() == 0)
			return;
		String json = "";
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;

		switch (type) {
		case HOME:
			json = JSONAndObject.convertObjectToJson(list, "statuses");
			fos = new FileOutputStream(path + "/home_timeline");
			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write(json);
			osw.close();
			break;
		case MESSAGE_AT:
			Log.d(" FACE_MESSAGE_AT", "ddddd");
			json = JSONAndObject.convertObjectToJson(list, "statuses");
			fos = new FileOutputStream(path + "/mention_timeline");
			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write(json);
			osw.close();
			break;
		case MESSAGE_COMMENT:
			json = JSONAndObject.convertObjectToJson(list, "comments");
			fos = new FileOutputStream(path + "/comment_timeline");
			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write(json);
			osw.close();
			break;
		case MESSAGE_FAVORITE:
			json = JSONAndObject.convertObjectToJson(list, "statuses");
			fos = new FileOutputStream(path + "/favorite");
			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write(json);
			osw.close();
			break;
		case USER_TIMELINE:
			json = JSONAndObject.convertObjectToJson(list, "statuses");
			fos = new FileOutputStream(path + "/user_timeline");
			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write(json);
			osw.close();
			break;
		default:
			break;
		}

	}

	public static List loadList(Context cotext, String path, int type)
			throws Exception {
		List list = null;
		String json = "";
		File file = new File(path);
		if (!file.exists())
			return list;

		switch (type) {
		case HOME:
			json = readFile(path + "/home_timeline");

			if (json == null) {
				// list = SinaWeiboManager.getHomeTimeline(activity);
			} else {
				list = JSONAndObject.convert(Status.class, json, "statuses");
				System.out.println("list--->" + list);
			}
			break;
		case MESSAGE_AT:
			json = readFile(path + "/mention_timeline");

			if (json == null) {
				// list = SinaWeiboManager.getHomeTimeline(activity);
			} else {
				list = JSONAndObject.convert(Status.class, json, "statuses");
			}
			break;
		case MESSAGE_COMMENT:
			json = readFile(path + "/comment_timeline");

			if (json == null) {
				// list = SinaWeiboManager.getCommentTimeline(activity);
			} else {
				list = JSONAndObject.convert(Comment.class, json, "comments");
			}
			break;
		case MESSAGE_FAVORITE:
			json = readFile(path + "/favorite");

			if (json == null) {
				// list = SinaWeiboManager.getFavorites(activity);
			} else {
				list = JSONAndObject.convert(Status.class, json, "statuses");

			}
			break;
		case USER_TIMELINE:
			json = readFile(path + "/user_timeline");

			if (json == null) {
				// json = SinaWeiboManager.getUserTimelineAsync(activity, 0, 0,
				// DEFAULT_STATUS_COUNT);
			}
			list = JSONAndObject.convert(Status.class, json, "statuses");
			break;
		default:
			break;
		}
		return list;

	}

	public static List loadList(Context cotext, int type) {
		try {
			return loadList(cotext, PATH_STORAGE, type);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 读文件
	 * @param filename 文件名
	 * @return
	 */
	public static String readFile(String filename) {
		StringBuilder result = new StringBuilder();
		if (!new File(filename).exists())
			return null;
		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "utf-8");
			BufferedReader br = new BufferedReader(isr);
			String temp = "";
			while ((temp = br.readLine()) != null) {
				result.append(temp);
			}
			isr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result.toString();
	}

}
