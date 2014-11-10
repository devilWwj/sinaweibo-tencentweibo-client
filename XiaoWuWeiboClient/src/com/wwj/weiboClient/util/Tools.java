package com.wwj.weiboClient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.wwj.weiboClient.GlobalObject;
import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.SinaFaceMan;

/**
 * 全局工具类
 * 
 * @author wwj
 * 
 */
public class Tools implements Const {

	// 得到全局变量
	public static GlobalObject getGlobalObject(Activity activity) {
		GlobalObject globalObject = (GlobalObject) activity
				.getApplicationContext();
		return globalObject;
	}

	// 將微博的日期字符串转换为Date对象
	public static Date strToDate(String str) {
		// sample：Tue May 31 17:46:55 +0800 2011
		// E：周 MMM：字符串形式的月，如果只有两个M，表示数值形式的月 Z表示时区（＋0800）
		SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy",
				Locale.US);
		Date result = null;
		try {
			result = sdf.parse(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获得微博发布的时间
	 * 
	 * @param oldTime
	 * @param currentDate
	 * @return
	 */
	public static String getTimeStr(Date oldTime, Date currentDate) {
		long time1 = currentDate.getTime();
		long time2 = oldTime.getTime();
		long time = (time1 - time2) / 1000;

		if (time >= 0 && time < 60) {
			return "刚才";
		} else if (time >= 60 && time < 3600) {
			return time / 60 + "分钟前";
		} else if (time >= 3600 && time < 3600 * 24) {
			return time / 3600 + "小时前";
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			return sdf.format(oldTime);
		}
	}

	/**
	 * 数据流转换
	 * 
	 * @param is
	 * @param os
	 */
	public static void dataTransfer(InputStream is, OutputStream os) {
		byte[] buffer = new byte[8192];
		int count = 0;
		try {
			while ((count = is.read(buffer)) > -1) {
				os.write(buffer, 0, count);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 新浪微博认证类型
	 * 
	 * @param imageView
	 * @param verifiedType
	 */
	public static void userVerified(ImageView imageView, int verifiedType) {
		if (verifiedType >= 0) {
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageLevel(verifiedType);
			switch (verifiedType) {
			case 2:
				break;
			case 0: // 认证个人用户
				imageView.setImageResource(R.drawable.avatar_vip);
				break;
			case 5: // 认证企业用户
				imageView.setImageResource(R.drawable.avatar_enterprise_vip);
				break;
			case 220: // 微博达人
				imageView.setImageResource(R.drawable.avatar_grassroot);
				break;
			default:
				imageView.setImageLevel(1);
				break;
			}
		}
	}

	/**
	 * 改变文本表情
	 * 
	 * @param context
	 * @param spanned
	 * @return
	 */
	public static SpannableString changeTextToFace(Context context,
			Spanned spanned) {
		String text = spanned.toString();
		SpannableString spannableString = new SpannableString(spanned);

		Pattern pattern = Pattern.compile("\\[[^\\]]+\\]");

		Matcher matcher = pattern.matcher(text);

		boolean b = true;

		while (b = matcher.find()) {
			String faceText = text.substring(matcher.start(), matcher.end());
			int resourceId = SinaFaceMan.getResourceId(faceText);
			if (resourceId > 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(
						context.getResources(), resourceId);
				ImageSpan imageSpan = new ImageSpan(bitmap);

				spannableString.setSpan(imageSpan, matcher.start(),
						matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannableString;
	}

	/**
	 * 将@的部分变成蓝色
	 * 
	 * @param s
	 * @return
	 */
	public static String atBlue(String s) {

		StringBuilder sb = new StringBuilder();
		int commonTextColor = Color.BLACK;
		int signColor = Color.BLUE;

		int state = 1;
		String str = "";
		for (int i = 0; i < s.length(); i++) {
			switch (state) {
			case 1: // 普通字符状态
				// 遇到@
				if (s.charAt(i) == '@') {
					state = 2;
					str += s.charAt(i);
				}
				// 遇到#
				else if (s.charAt(i) == '#') {
					str += s.charAt(i);
					state = 3;
				}
				// 添加普通字符
				else {
					if (commonTextColor == Color.BLACK)
						sb.append(s.charAt(i));
					else
						sb.append("<font color='" + commonTextColor + "'>"
								+ s.charAt(i) + "</font>");
				}
				break;
			case 2: // 处理遇到@的情况
				// 处理@后面的普通字符
				if (Character.isJavaIdentifierPart(s.charAt(i))) {
					str += s.charAt(i);
				}

				else {
					// 如果只有一个@，作为普通字符处理
					if ("@".equals(str)) {
						sb.append(str);
					}
					// 将@及后面的普通字符变成蓝色
					else {
						sb.append(setTextColor(str, String.valueOf(signColor)));
					}
					// @后面有#的情况，首先应将#添加到str里，这个值可能会变成蓝色，也可以作为普通字符，要看后面还有没有#了
					if (s.charAt(i) == '#') {

						str = String.valueOf(s.charAt(i));
						state = 3;
					}
					// @后面还有个@的情况，和#类似
					else if (s.charAt(i) == '@') {
						str = String.valueOf(s.charAt(i));
						state = 2;
					}
					// @后面有除了@、#的其他特殊字符。需要将这个字符作为普通字符处理
					else {
						if (commonTextColor == Color.BLACK)
							sb.append(s.charAt(i));
						else
							sb.append("<font color='" + commonTextColor + "'>"
									+ s.charAt(i) + "</font>");
						state = 1;
						str = "";
					}
				}
				break;
			case 3: // 处理遇到#的情况
				// 前面已经遇到一个#了，这里处理结束的#
				if (s.charAt(i) == '#') {
					str += s.charAt(i);
					sb.append(setTextColor(str, String.valueOf(signColor)));
					str = "";
					state = 1;

				}
				// 如果#后面有@，那么看一下后面是否还有#，如果没有#，前面的#作废，按遇到@处理
				else if (s.charAt(i) == '@') {
					if (s.substring(i).indexOf("#") < 0) {
						sb.append(str);
						str = String.valueOf(s.charAt(i));
						state = 2;

					} else {
						str += s.charAt(i);
					}
				}
				// 处理#...#之间的普通字符
				else {
					str += s.charAt(i);
				}
				break;
			}

		}
		if (state == 1 || state == 3) {
			sb.append(str);
		} else if (state == 2) {
			if ("@".equals(str)) {
				sb.append(str);
			} else {
				sb.append(setTextColor(str, String.valueOf(signColor)));
			}

		}

		return sb.toString();

	}

	/**
	 * 设置文本颜色
	 * 
	 * @param s
	 * @param color
	 * @return
	 */
	public static String setTextColor(String s, String color) {
		String result = "<font color='" + color + "'>" + s + "</font>";

		return result;
	}

	public static Bitmap getFitBitmap(String path, long maxSize) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		Bitmap bitmap = null;
		try {
			if (file.length() <= maxSize) {
				try {
					Options options = new Options();
					options.inTempStorage = new byte[16 * 1024];
					try {
						FileInputStream fis = new FileInputStream(path);

						bitmap = BitmapFactory.decodeStream(fis, new Rect(-1,
								-1, -1, -1), options);
						fis.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				int inSampleSize = (int) (file.length() / maxSize + 1);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inTempStorage = new byte[16 * 1024];
				options.inSampleSize = inSampleSize;
				try {
					FileInputStream fis = new FileInputStream(path);
					bitmap = BitmapFactory.decodeStream(fis, new Rect(-1, -1,
							-1, -1), options);
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public static String getEffectTempImageFilename() {
		String filename = PATH_IMAGE + "/effect_temp.jpg";
		File file = new File(filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return filename;
	}

	/**
	 * 是否有SD卡
	 * 
	 * @return
	 */
	public static boolean inquiresSDCard() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 创建目录
	 * 
	 * @param path
	 */
	public static void createFile(String path) {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}
	}

	/**
	 * 递归调用删除所有文件
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete();
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					deleteFile(files[i]); // 把每个文件用这个方法进行迭代
				}
			}
			// 删除根目录
			file.delete();
		} else {
			Log.e("wwj", "所删除的文件不存在");
		}
	}

	/**
	 * 安装apk文件
	 * 
	 * @param context
	 * @param filename
	 */
	public static void installApk(Context context, String filename) {
		File file = new File(filename);
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(Intent.ACTION_VIEW);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(Uri.fromFile(file), type);
		context.startActivity(intent);
	}

	public static void storeBitmapInSDCard(Bitmap bitmap) {
		File file = new File(PATH_IMAGE);
		if (!file.exists()) {
			file.mkdirs();
		}
		File imageFile = new File(PATH_IMAGE + "/"
				+ String.valueOf(new Random().nextLong()) + ".jpg");
		while (imageFile.exists()) {
			imageFile = new File(PATH_IMAGE + "/"
					+ String.valueOf(new Random().nextLong()) + ".jpg");
		}
		try {
			FileOutputStream os = new FileOutputStream(imageFile);
			bitmap.compress(CompressFormat.JPEG, 100, os);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
