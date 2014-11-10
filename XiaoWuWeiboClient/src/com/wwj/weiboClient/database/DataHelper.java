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
	// ���ݿ�����
	private static String DB_NAME = "weibo.db";
	// ���ݿ�汾
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

	// ��ȡusers���е�UserID��Access Token��Access Secret�ļ�¼
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

	// �ж�users���е��Ƿ����ĳ��UserID�ļ�¼
	public Boolean haveUserInfo(String UserId) {
		Boolean b = false;
		Cursor cursor = db.query(SqliteHelper.TABLE_NAME, null, UserInfo.USERID
				+ "=?", new String[] { UserId }, null, null, null);
		b = cursor.moveToFirst();
		Log.e("HaveUserInfo", b.toString());
		cursor.close();
		return b;
	}

	// ����users��ļ�¼������UserId�����û��ǳƺ��û�ͼ��
	public int updateUserInfo(String userName, Bitmap userIcon, String UserId) {
		ContentValues values = new ContentValues();
		values.put(UserInfo.USERNAME, userName);
		// BLOB����
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		// ��Bitmapѹ����PNG���룬����Ϊ100%�洢
		userIcon.compress(Bitmap.CompressFormat.PNG, 100, os);
		// ����SQLite��Content��������Ҳ����ʹ��raw
		values.put(UserInfo.USERICON, os.toByteArray());
		int id = db.update(SqliteHelper.TABLE_NAME, values, UserInfo.USERID
				+ "=?", new String[] { UserId });
		Log.e("UpdateUserInfo2", id + "");
		return id;
	}

	// ����users��ļ�¼
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

	// ���users��ļ�¼
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

	// ���users��ļ�¼
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
		// �����ļ�url
		values.put(UserInfo.URL, user.getUrl());
		values.put(UserInfo.TYPE, user.getType());
		Long uid = db.insert(SqliteHelper.TABLE_NAME, UserInfo.ID, values);
		Log.e("SaveUserInfo", uid + "");
		return uid;
	}

	// ɾ��users��ļ�¼
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
