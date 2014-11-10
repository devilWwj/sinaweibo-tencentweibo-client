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
 * 查看微博图片
 * 
 * @author Administrator
 * 
 */
public class PictureViewer extends Activity implements Const, DoneAndProcess,
		OnClickListener {
	private ImageView pictureViewer;
	private Button back; // 返回
	private Button effect; // 特效
	private Button remove; // 删除
	private Button save; // 保存
	private String filename; // 文件名
	private String fileUrl; // 文件url
	private String imageUrl; // 图片网络地址
	private int type; // 图片类型
	private AsyncImageLoader asyncImageLoader; // 异步加载图片类
	private Bitmap saveBitmap; // 保存的位图

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
		case PICTURE_VIEWER_POST_WEIBO: // 在发布微博界面中浏览图像
			if (filename != null) {
				Bitmap bitmap = BitmapFactory.decodeFile(filename);
				pictureViewer.setImageBitmap(bitmap);
			}
			save.setVisibility(View.GONE);
			break;

		case PICTURE_VIEWER_WEIBO_BROWSER: // 在浏览微博界面中放大显示图像
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
		case CODE_RESULT_SAVE: // 保存
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
		case R.id.btn_back: // 返回
			intent = new Intent();
			intent.putExtra("filename", filename);
			setResult(CODE_RESULT_RETURN, intent);
			finish();
			break;
		case R.id.btn_remove:// 删除
			setResult(CODE_RESULT_REMOVE);
			finish();
			break;
		case R.id.btn_effect:// 特效
			intent = new Intent(this, PhotoViewer.class);
			intent.putExtra("filename", filename);
			startActivityForResult(intent, 0);
			break;
		case R.id.btn_save: // 保存图像
			// if (imageUrl != null && fileUrl != null) {
			// String fn = PATH_STORAGE + "/"
			// + String.valueOf(fileUrl.hashCode()) + ".jpg";
			// try {
			// FileInputStream fis = new FileInputStream(imageUrl);
			// FileOutputStream fos = new FileOutputStream(fn);
			// Tools.dataTransfer(fis, fos);
			// fis.close();
			// fos.close();
			// Toast.makeText(this, "保存成功，图像路径：" + fn, Toast.LENGTH_LONG)
			// .show();
			// } catch (Exception e) {
			// Toast.makeText(this, "保存图像失败", Toast.LENGTH_LONG).show();
			// }
			// }
			String fn = StorageManager.saveBitmap(saveBitmap);
			if (fn != null) {
				Toast.makeText(this, "保存成功，图像路径：" + fn, Toast.LENGTH_LONG)
						.show();
			} else {
				Toast.makeText(this, "保存图像失败", Toast.LENGTH_LONG).show();
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

		private int mode = 0; // 用于标记模式
		private static final int DRAG = 1; // 拖动
		private static final int ZOOM = 2; // 放大
		private float startDis = 0;
		private PointF midPoint; // 中心点

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mode = DRAG; // 拖拽
				currentMaritx.set(pictureViewer.getImageMatrix()); // 记录ImageView当前移动位置
				startPoint.set(event.getX(), event.getY()); // 开始点
				break;
			case MotionEvent.ACTION_MOVE:// 移动事件
				if (mode == DRAG) { // 图片拖动事件
					float dx = event.getX() - startPoint.x; // x轴移动距离
					float dy = event.getY() - startPoint.y;
					matrix.set(currentMaritx); // 在当前的位置基础上移动
					matrix.postTranslate(dx, dy);
				} else if (mode == ZOOM) { // 图片放大事件
					float endDis = distance(event); // 结束距离
					if (endDis > 10f) {
						float scale = endDis / startDis; // 放大倍数
						matrix.set(currentMaritx);
						matrix.postScale(scale, scale, midPoint.x, midPoint.y);
					}

				}
				break;
			case MotionEvent.ACTION_UP:
				mode = 0;
				break;
			// 有手指离开屏幕，但屏幕还有触点（手指）
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
			// 当屏幕上已经有触点（手指），再有一个手指压下屏幕
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = ZOOM;
				startDis = distance(event);
				if (startDis > 10f) { // 避免手指上有两个
					midPoint = mid(event);
					currentMaritx.set(pictureViewer.getImageMatrix()); // 记录当前的缩放倍数
				}
				break;
			}
			// 显示缩放后的图片
			pictureViewer.setImageMatrix(matrix);
			return true;
		}

	}

	/**
	 * 两点之间的距离
	 * 
	 * @param event
	 * @return
	 */
	private static float distance(MotionEvent event) {
		// 两根线的距离
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 计算两点之间中心点的距离
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
