package com.wwj.weiboClient.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DataHelper {
	// 数据库名称
	private static String DB_NAME = "weibo.db";
	// 数据库版本
	private static int DB_VERSION = 1;

	private SQLiteDatabase db;
	private SqliteHelper dbHelper;

	public DataHelper(Context context) {
		dbHelper = new SqliteHelper(context, DB_NAME, null, DB_VERSION);
		db = dbHelper.getWritableDatabase();
	}

	public void Close() {
		db.close();
		dbHelper.close();
	}

	// 获取users表中的UserID、Access Token、Access Secret的记录
	public List<UserInfo> getUserList(Boolean isSimple) {
		List<UserInfo> userList = new ArrayList<UserInfo>();
		Cursor cursor = db.query(SqliteHelper.TABLE_NAME, null, null, null,
				null, null, UserInfo.ID + " DESC");
		cursor.moveToFirst();
		while (!cursor.isAfterLast() && (cursor.getString(1) != null)) {
			UserInfo user = new UserInfo();
			user.setId(cursor.getString(0));
			user.setUserId(cursor.getString(1));
			user.setToken(cursor.getString(2));
			user.setTokenSecret(cursor.getString(3));
			user.setExpires_in(String.valueOf(cursor.getLong(4)));
			user.setUserName(cursor.getString(5));
			if (!isSimple) {
				ByteArrayInputStream stream = new ByteArrayInputStream(
						cursor.getBlob(6));
				Drawable icon = Drawable.createFromStream(stream, "image");
				user.setUserIcon(icon);
			}
			user.setUrl(cursor.getString(7));
			user.setType(cursor.getInt(8));
			userList.add(user);
			cursor.moveToNext();
		}
		cursor.close();
		return userList;
	}

	// 判断users表中的是否包含某个UserID的记录
	public Boolean haveUserInfo(String UserId) {
		Boolean b = false;
		Cursor cursor = db.query(SqliteHelper.TABLE_NAME, null, UserInfo.USERID
				+ "=?", new String[] { UserId }, null, null, null);
		b = cursor.moveToFirst();
		Log.e("HaveUserInfo", b.toString());
		cursor.close();
		return b;
	}

	// 更新users表的记录，根据UserId更新用户昵称和用户图标
	public int updateUserInfo(String userName, Bitmap userIcon, String UserId) {
		ContentValues values = new ContentValues();
		values.put(UserInfo.USERNAME, userName);
		// BLOB类型
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		// 将Bitmap压缩成PNG编码，质量为100%存储
		userIcon.compress(Bitmap.CompressFormat.PNG, 100, os);
		// 构造SQLite的Content对象，这里也可以使用raw
		values.put(UserInfo.USERICON, os.toByteArray());
		int id = db.update(SqliteHelper.TABLE_NAME, values, UserInfo.USERID
				+ "=?", new String[] { UserId });
		Log.e("UpdateUserInfo2", id + "");
		return id;
	}

	// 更新users表的记录
	public int updateUserInfo(UserInfo user) {
		ContentValues values = new ContentValues();
		values.put(UserInfo.USERID, user.getUserId());
		values.put(UserInfo.TOKEN, user.getToken());
		values.put(UserInfo.TOKENSECRET, user.getTokenSecret());
		values.put(UserInfo.EXPIRES_IN, user.getExpires_in());
		int id = db.update(SqliteHelper.TABLE_NAME, values, UserInfo.USERID
				+ "=" + user.getUserId(), null);
		Log.e("UpdateUserInfo", id + "");
		return id;
	}

	// 添加users表的记录
	public Long saveUserInfo(UserInfo user) {
		ContentValues values = new ContentValues();
		values.put(UserInfo.USERID, user.getUserId());
		values.put(UserInfo.TOKEN, user.getToken());
		values.put(UserInfo.TOKENSECRET, user.getTokenSecret());
		values.put(UserInfo.EXPIRES_IN, user.getExpires_in());
		Long uid = db.insert(SqliteHelper.TABLE_NAME, UserInfo.ID, values);
		Log.e("SaveUserInfo", uid + "");
		return uid;
	}

	// 添加users表的记录
	public Long saveUserInfo(UserInfo user, byte[] icon) {
		ContentValues values = new ContentValues();
		values.put(UserInfo.USERID, user.getUserId());
		values.put(UserInfo.USERNAME, user.getUserName());
		values.put(UserInfo.TOKEN, user.getToken());
		values.put(UserInfo.TOKENSECRET, user.getTokenSecret());
		values.put(UserInfo.EXPIRES_IN, user.getExpires_in());
		if (icon != null) {
			values.put(UserInfo.USERICON, icon);
		}
		// 保存文件url
		values.put(UserInfo.URL, user.getUrl());
		values.put(UserInfo.TYPE, user.getType());
		Long uid = db.insert(SqliteHelper.TABLE_NAME, UserInfo.ID, values);
		Log.e("SaveUserInfo", uid + "");
		return uid;
	}

	// 删除users表的记录
	public int delUserInfo(String UserId) {
		int id = db.delete(SqliteHelper.TABLE_NAME, UserInfo.USERID + "=?",
				new String[] { UserId });
		Log.e("DelUserInfo", id + "");
		return id;
	}

	public static UserInfo getUserByName(String userName,
			List<UserInfo> userList) {
		UserInfo userInfo = null;
		int size = userList.size();
		for (int i = 0; i < size; i++) {
			if (userName.equals(userList.get(i).getUserName())) {
				userInfo = userList.get(i);
				break;
			}
		}
		return userInfo;
	}
}
