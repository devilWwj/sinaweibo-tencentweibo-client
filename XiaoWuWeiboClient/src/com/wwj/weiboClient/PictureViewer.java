package com.wwj.weiboClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;
import com.wwj.weiboClient.util.ImageUtil;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * �鿴΢��ͼƬ
 * 
 * @author Administrator
 * 
 */
public class PictureViewer extends Activity implements Const, DoneAndProcess,
		OnClickListener {
	private ImageView pictureViewer;
	private Button back; // ����
	private Button effect; // ��Ч
	private Button remove; // ɾ��
	private Button save; // ����
	private String filename; // �ļ���
	private String fileUrl; // �ļ�url
	private String imageUrl; // ͼƬ�����ַ
	private int type; // ͼƬ����
	private AsyncImageLoader asyncImageLoader; // �첽����ͼƬ��
	private Bitmap saveBitmap; // �����λͼ

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			imageUrl = SinaWeiboManager
					.getImageurl(fileUrl, PictureViewer.this);
			if (imageUrl != null) {
				pictureViewer.setImageURI(Uri.fromFile(new File(imageUrl)));
			}

			super.handleMessage(msg);
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.picture_viewer);
		findViews();
		filename = getIntent().getStringExtra("filename");
		fileUrl = getIntent().getStringExtra("file_url");
		type = getIntent().getExtras()
				.getInt("type", PICTURE_VIEWER_POST_WEIBO);
		asyncImageLoader = new AsyncImageLoader();
		switch (type) {
		case PICTURE_VIEWER_POST_WEIBO: // �ڷ���΢�����������ͼ��
			if (filename != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(filename);
				pictureViewer.setImageBitmap(bitmap);
			}
			save.setVisibility(View.GONE);
			break;

		case PICTURE_VIEWER_WEIBO_BROWSER: // �����΢�������зŴ���ʾͼ��
			effect.setVisibility(View.GONE);
			remove.setVisibility(View.GONE);
			int weiboType = StorageManager.getValue(this,
					StringUtil.LOGIN_TYPE, 0);
			Drawable drawable = asyncImageLoader.loadDrawable(fileUrl,
					pictureViewer, new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								ImageView imageView, String imageUrl) {
							imageView.setImageDrawable(imageDrawable);
							saveBitmap = ImageUtil
									.drawableToBitmap(imageDrawable);
						}
					});
			if (drawable == null) {
				pictureViewer
						.setImageResource(R.drawable.wb_detail_pic_default);
			} else {
				pictureViewer.setImageDrawable(drawable);
			}

			// if (weiboType == SINA) {
			// imageUrl = SinaWeiboManager.getImageurl(fileUrl, this);
			// if (imageUrl != null) {
			// pictureViewer.setImageURI(Uri.fromFile(new File(imageUrl)));
			// }
			// } else {
			// imageUrl = fileUrl;
			// Drawable drawable = asyncImageLoader.loadDrawable(imageUrl,
			// pictureViewer, new ImageCallback() {
			// @Override
			// public void imageLoaded(Drawable imageDrawable,
			// ImageView imageView, String imageUrl) {
			// imageView.setImageDrawable(imageDrawable);
			// }
			// });
			// if (drawable == null) {
			// pictureViewer
			// .setImageResource(R.drawable.wb_detail_pic_default);
			// } else {
			// pictureViewer.setImageDrawable(drawable);
			// }
			// BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
			// bitmapDrawable.getBitmap();
			// }

			break;
		}
	}

	private void findViews() {
		pictureViewer = (ImageView) findViewById(R.id.iv_picture_viewer);
		back = (Button) findViewById(R.id.btn_back);
		effect = (Button) findViewById(R.id.btn_effect);
		remove = (Button) findViewById(R.id.btn_remove);
		save = (Button) findViewById(R.id.btn_save);
		back.setOnClickListener(this);
		effect.setOnClickListener(this);
		remove.setOnClickListener(this);
		save.setOnClickListener(this);
		pictureViewer.setOnTouchListener(new TouchListener());
	}

	@Override
	public void doneProcess(ParentTask task) {
		handler.sendEmptyMessage(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case CODE_RESULT_SAVE: // ����
			if (data != null) {
				filename = data.getStringExtra("filename");
				Bitmap bitmap = BitmapFactory.decodeFile(filename);
				pictureViewer.setImageBitmap(bitmap);
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		Intent intent = null;
		switch (view.getId()) {
		case R.id.btn_back: // ����
			intent = new Intent();
			intent.putExtra("filename", filename);
			setResult(CODE_RESULT_RETURN, intent);
			finish();
			break;
		case R.id.btn_remove:// ɾ��
			setResult(CODE_RESULT_REMOVE);
			finish();
			break;
		case R.id.btn_effect:// ��Ч
			intent = new Intent(this, PhotoViewer.class);
			intent.putExtra("filename", filename);
			startActivityForResult(intent, 0);
			break;
		case R.id.btn_save: // ����ͼ��
			// if (imageUrl != null && fileUrl != null) {
			// String fn = PATH_STORAGE + "/"
			// + String.valueOf(fileUrl.hashCode()) + ".jpg";
			// try {
			// FileInputStream fis = new FileInputStream(imageUrl);
			// FileOutputStream fos = new FileOutputStream(fn);
			// Tools.dataTransfer(fis, fos);
			// fis.close();
			// fos.close();
			// Toast.makeText(this, "����ɹ���ͼ��·����" + fn, Toast.LENGTH_LONG)
			// .show();
			// } catch (Exception e) {
			// Toast.makeText(this, "����ͼ��ʧ��", Toast.LENGTH_LONG).show();
			// }
			// }
			String fn = StorageManager.saveBitmap(saveBitmap);
			if (fn != null) {
				Toast.makeText(this, "����ɹ���ͼ��·����" + fn, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "����ͼ��ʧ��", Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	private class TouchListener implements OnTouchListener {

		private PointF startPoint = new PointF();
		private Matrix matrix = new Matrix();
		private Matrix currentMaritx = new Matrix();

		private int mode = 0; // ���ڱ��ģʽ
		private static final int DRAG = 1; // �϶�
		private static final int ZOOM = 2; // �Ŵ�
		private float startDis = 0;
		private PointF midPoint; // ���ĵ�

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG; // ��ק
				currentMaritx.set(pictureViewer.getImageMatrix()); // ��¼ImageView��ǰ�ƶ�λ��
				startPoint.set(event.getX(), event.getY()); // ��ʼ��
				break;
			case MotionEvent.ACTION_MOVE:// �ƶ��¼�
				if (mode == DRAG) { // ͼƬ�϶��¼�
					float dx = event.getX() - startPoint.x; // x���ƶ�����
					float dy = event.getY() - startPoint.y;
					matrix.set(currentMaritx); // �ڵ�ǰ��λ�û������ƶ�
					matrix.postTranslate(dx, dy);
				} else if (mode == ZOOM) { // ͼƬ�Ŵ��¼�
					float endDis = distance(event); // ��������
					if (endDis > 10f) {
						float scale = endDis / startDis; // �Ŵ���
						matrix.set(currentMaritx);
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
					}

				}
				break;
			case MotionEvent.ACTION_UP:
				mode = 0;
				break;
			// ����ָ�뿪��Ļ������Ļ���д��㣨��ָ��
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
			// ����Ļ���Ѿ��д��㣨��ָ��������һ����ָѹ����Ļ
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = ZOOM;
				startDis = distance(event);
				if (startDis > 10f) { // ������ָ��������
					midPoint = mid(event);
					currentMaritx.set(pictureViewer.getImageMatrix()); // ��¼��ǰ�����ű���
				}
				break;
			}
			// ��ʾ���ź��ͼƬ
			pictureViewer.setImageMatrix(matrix);
			return true;
		}

	}

	/**
	 * ����֮��ľ���
	 * 
	 * @param event
	 * @return
	 */
	private static float distance(MotionEvent event) {
		// �����ߵľ���
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * ��������֮�����ĵ�ľ���
	 * 
	 * @param event
	 * @return
	 */
	private static PointF mid(MotionEvent event) {
		float midX = event.getX(1) + event.getX(0);
		float midY = event.getY(1) - event.getY(0);
		return new PointF(midX / 2, midY / 2);
	}
}
