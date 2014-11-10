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
 * ȫ�ֹ�����
 * 
 * @author wwj
 * 
 */
public class Tools implements Const {

	// �õ�ȫ�ֱ���
	public static GlobalObject getGlobalObject(Activity activity) {
		GlobalObject globalObject = (GlobalObject) activity
				.getApplicationContext();
		return globalObject;
	}

	// ��΢���������ַ���ת��ΪDate����
	public static Date strToDate(String str) {
		// sample��Tue May 31 17:46:55 +0800 2011
		// E���� MMM���ַ�����ʽ���£����ֻ������M����ʾ��ֵ��ʽ���� Z��ʾʱ������0800��
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
	 * ���΢��������ʱ��
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
			return "�ղ�";
		} else if (time >= 60 && time < 3600) {
			return time / 60 + "����ǰ";
		} else if (time >= 3600 && time < 3600 * 24) {
			return time / 3600 + "Сʱǰ";
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			return sdf.format(oldTime);
		}
	}

	/**
	 * ������ת��
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
	 * ����΢����֤����
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
			case 0: // ��֤�����û�
				imageView.setImageResource(R.drawable.avatar_vip);
				break;
			case 5: // ��֤��ҵ�û�
				imageView.setImageResource(R.drawable.avatar_enterprise_vip);
				break;
			case 220: // ΢������
				imageView.setImageResource(R.drawable.avatar_grassroot);
				break;
			default:
				imageView.setImageLevel(1);
				break;
			}
		}
	}

	/**
	 * �ı��ı�����
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
	 * ��@�Ĳ��ֱ����ɫ
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
			case 1: // ��ͨ�ַ�״̬
				// ����@
				if (s.charAt(i) == '@') {
					state = 2;
					str += s.charAt(i);
				}
				// ����#
				else if (s.charAt(i) == '#') {
					str += s.charAt(i);
					state = 3;
				}
				// �����ͨ�ַ�
				else {
					if (commonTextColor == Color.BLACK)
						sb.append(s.charAt(i));
					else
						sb.append("<font color='" + commonTextColor + "'>"
								+ s.charAt(i) + "</font>");
				}
				break;
			case 2: // ��������@�����
				// ����@�������ͨ�ַ�
				if (Character.isJavaIdentifierPart(s.charAt(i))) {
					str += s.charAt(i);
				}

				else {
					// ���ֻ��һ��@����Ϊ��ͨ�ַ�����
					if ("@".equals(str)) {
						sb.append(str);
					}
					// ��@���������ͨ�ַ������ɫ
					else {
						sb.append(setTextColor(str, String.valueOf(signColor)));
					}
					// @������#�����������Ӧ��#��ӵ�str����ֵ���ܻ�����ɫ��Ҳ������Ϊ��ͨ�ַ���Ҫ�����滹��û��#��
					if (s.charAt(i) == '#') {

						str = String.valueOf(s.charAt(i));
						state = 3;
					}
					// @���滹�и�@���������#����
					else if (s.charAt(i) == '@') {
						str = String.valueOf(s.charAt(i));
						state = 2;
					}
					// @�����г���@��#�����������ַ�����Ҫ������ַ���Ϊ��ͨ�ַ�����
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
			case 3: // ��������#�����
				// ǰ���Ѿ�����һ��#�ˣ����ﴦ�������#
				if (s.charAt(i) == '#') {
					str += s.charAt(i);
					sb.append(setTextColor(str, String.valueOf(signColor)));
					str = "";
					state = 1;

				}
				// ���#������@����ô��һ�º����Ƿ���#�����û��#��ǰ���#���ϣ�������@����
				else if (s.charAt(i) == '@') {
					if (s.substring(i).indexOf("#") < 0) {
						sb.append(str);
						str = String.valueOf(s.charAt(i));
						state = 2;

					} else {
						str += s.charAt(i);
					}
				}
				// ����#...#֮�����ͨ�ַ�
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
	 * �����ı���ɫ
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
	 * �Ƿ���SD��
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
	 * ����Ŀ¼
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
	 * �ݹ����ɾ�������ļ�
	 * 
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file.exists()) { // �ж��ļ��Ƿ����
			if (file.isFile()) { // �ж��Ƿ����ļ�
				file.delete();
			} else if (file.isDirectory()) { // �����������һ��Ŀ¼
				File files[] = file.listFiles(); // ����Ŀ¼�����е��ļ�
				for (int i = 0; i < files.length; i++) { // ����Ŀ¼�����е��ļ�
					deleteFile(files[i]); // ��ÿ���ļ�������������е���
				}
			}
			// ɾ����Ŀ¼
			file.delete();
		} else {
			Log.e("wwj", "��ɾ�����ļ�������");
		}
	}

	/**
	 * ��װapk�ļ�
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
