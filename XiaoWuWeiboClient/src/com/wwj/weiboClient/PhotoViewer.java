package com.wwj.weiboClient;

import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.photo.AllThreadEnd;
import com.wwj.weiboClient.photo.FrameLayoutExt;
import com.wwj.weiboClient.photo.GrayProcess;
import com.wwj.weiboClient.photo.ImageViewExt;
import com.wwj.weiboClient.photo.MosaicProcess;
import com.wwj.weiboClient.photo.ProcessBitmapRegions;
import com.wwj.weiboClient.util.Tools;

public class PhotoViewer extends Activity implements Const, OnClickListener,
		OnMenuItemClickListener, AllThreadEnd, OnTouchListener,
		OnSeekBarChangeListener {
	private View mEffect;
	public static ProgressBar mPbPhotoProcess;
	private Bitmap mSourceBitmap;
	private Bitmap mEffectBitmap;
	private ImageViewExt mIvDrawing;
	private TextView mTvUnCleanLayer;
	// 特效区域，自定义布局
	private FrameLayoutExt mFlMoveRegion;

	// 下面是屏幕下方的几个层次的按钮
	private View mMainButton;
	private View mMosaicButton;

	private TextView mTvStart;

	// 用于注册Context Menu,并不显示
	private View mEffectMenu;

	private SeekBar mSbRotate;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				// 显示处理后的特效图像
				mIvDrawing.setImageBitmap(mEffectBitmap);
				// 将特效区域设为最后一次处理的特效图像
				mFlMoveRegion.mBitmap = mEffectBitmap;
				// 隐藏特效区域
				mFlMoveRegion.setVisibility(View.GONE);
				// 隐藏进度条
				mPbPhotoProcess.setVisibility(View.GONE);
				// 隐藏朦胧层（覆盖在图像上的半透明TextView控件）
				mTvUnCleanLayer.setVisibility(View.GONE);
				// 重新显示主界面按钮
				mMainButton.setVisibility(View.VISIBLE);
				// 隐藏马赛克按钮
				mMosaicButton.setVisibility(View.GONE);
			} catch (Exception e) {

			}
			super.handleMessage(msg);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_viewer);

		mEffect = findViewById(R.id.llEffect);

		mPbPhotoProcess = (ProgressBar) findViewById(R.id.pbPhotoProcess);
		mTvUnCleanLayer = (TextView) findViewById(R.id.tvUnCleanLayer);

		mMainButton = findViewById(R.id.llMainButton);
		mMosaicButton = findViewById(R.id.llMosaicButton);

		View startButton = findViewById(R.id.llStart);
		View backButton = findViewById(R.id.llBack);

		View saveButton = findViewById(R.id.llSave);
		View cancelButton = findViewById(R.id.llCancel);

		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		mTvStart = (TextView) findViewById(R.id.tvStart);
		mSbRotate = (SeekBar) findViewById(R.id.sbRotate);
		mSbRotate.setOnSeekBarChangeListener(this);
		startButton.setOnClickListener(this);
		backButton.setOnClickListener(this);

		mEffect.setOnClickListener(this);

		mFlMoveRegion = (FrameLayoutExt) findViewById(R.id.flMoveRegion);
		mFlMoveRegion.setOnTouchListener(this);

		mIvDrawing = (ImageViewExt) findViewById(R.id.ivDrawing);

		// 获取要编辑的图像文件名
		String filename = getIntent().getStringExtra("filename");

		if (filename == null) {
			mSourceBitmap = (Bitmap) getIntent().getExtras().get("bitmap");
		} else {
			mSourceBitmap = Tools.getFitBitmap(filename, BITMAP_MAX_SIZE);
		}

		copyBitmap();

		mFlMoveRegion.mBitmap = mEffectBitmap;
		mFlMoveRegion.mIvDrawing = mIvDrawing;

		// 显示源图像
		mIvDrawing.setImageBitmap(mSourceBitmap);

		mEffectMenu = findViewById(R.id.tvEffectMenu);

		ProcessBitmapRegions.mAllThreadEnd = this;
		ProcessBitmapRegions.processType = "";
		// 注册上下文菜单
		registerForContextMenu(mEffectMenu);

	}

	/**
	 * 恢复原始图像
	 */
	private void copyBitmap() {
		mEffectBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(),
				mSourceBitmap.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(mEffectBitmap);
		canvas.drawBitmap(mSourceBitmap, 0, 0, null);

	}

	@Override
	public void onFinish() {
		try {
			mHandler.sendEmptyMessage(0);
		} catch (Exception e) {

		}
		ProcessBitmapRegions.isWorking = false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (ProcessBitmapRegions.isWorking) {
			Toast.makeText(this, "任务正在进行中，无法进行其他操作.", Toast.LENGTH_LONG).show();
			return false;
		}
		int screenWidth = mIvDrawing.getWidth();
		int screenHeight = mIvDrawing.getHeight();

		int sourceWidth = mEffectBitmap.getWidth();
		int sourceHeight = mEffectBitmap.getHeight();
		int regionTop = 0;
		int regionLeft = 0;
		int regionRight = 0;
		int regionBottom = 0;
		if (sourceWidth * screenHeight > sourceHeight * screenWidth) {

			int insideHeight = sourceHeight * screenWidth / sourceWidth;
			regionTop = (screenHeight - insideHeight) / 2;
			regionLeft = 0;
			regionRight = mIvDrawing.getMeasuredWidth();
			regionBottom = regionTop + insideHeight + 1;

		} else {
			int insideWidth = sourceWidth * screenHeight / sourceHeight;
			regionLeft = (screenWidth - insideWidth) / 2;
			regionTop = 0;

			regionRight = regionLeft + insideWidth + 1;
			regionBottom = mIvDrawing.getMeasuredHeight();

		}
		switch (item.getItemId()) {
		case R.id.mnuGray:
			ProcessBitmapRegions.isWorking = true;
			mPbPhotoProcess.setVisibility(View.VISIBLE);
			mPbPhotoProcess.setMax(mEffectBitmap.getWidth() - 1);
			mPbPhotoProcess.setProgress(0);
			if (mEffectBitmap == null) {
				mEffectBitmap = Bitmap.createBitmap(mSourceBitmap.getWidth(),
						mSourceBitmap.getHeight(), Config.ARGB_8888);
				Canvas canvas = new Canvas(mEffectBitmap);
				canvas.drawBitmap(mSourceBitmap, 0, 0, null);
			}
			mFlMoveRegion.mBitmap = mEffectBitmap;
			ProcessBitmapRegions processBitmapRegions = new ProcessBitmapRegions(
					new GrayProcess(mEffectBitmap));
			processBitmapRegions.work();
			mSbRotate.setVisibility(View.GONE);
			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_GRAY;
			break;
		case R.id.mnuMosaic:

			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_MOSAIC;
			mMainButton.setVisibility(View.GONE);
			// 显示马赛克按钮布局
			mMosaicButton.setVisibility(View.VISIBLE);
			mFlMoveRegion.setVisibility(View.VISIBLE);
			mTvStart.setText("马赛克");

			mTvUnCleanLayer.setVisibility(View.VISIBLE);

			mTvUnCleanLayer.getLayoutParams().width = regionRight - regionLeft;
			mTvUnCleanLayer.getLayoutParams().height = regionBottom - regionTop;
			mTvUnCleanLayer.requestLayout();
			mSbRotate.setVisibility(View.GONE);
			break;
		case R.id.mnuCrop:
			if (ProcessBitmapRegions.isWorking) {
				Toast.makeText(this, "任务正在进行中，无法进行其他操作.", Toast.LENGTH_LONG)
						.show();
				break;
			}
			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_MOSAIC;
			mMainButton.setVisibility(View.GONE);
			mMosaicButton.setVisibility(View.VISIBLE);
			mFlMoveRegion.setVisibility(View.VISIBLE);
			mTvStart.setText("截图");
			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_CROP;
			mTvUnCleanLayer.setVisibility(View.VISIBLE);

			mTvUnCleanLayer.getLayoutParams().width = regionRight - regionLeft;
			mTvUnCleanLayer.getLayoutParams().height = regionBottom - regionTop;
			mTvUnCleanLayer.requestLayout();
			mSbRotate.setVisibility(View.GONE);

			break;
		case R.id.mnuRotate:
			if (ProcessBitmapRegions.PROCESS_TYPE_ROTATE
					.equals(ProcessBitmapRegions.processType))
				break;
			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_ROTATE;
			mOldEffectBitmap = mEffectBitmap;
			mSbRotate.setVisibility(View.VISIBLE);
			mSbRotate.setProgress(0);

			mFlMoveRegion.mBitmap = mEffectBitmap;
			mHandler.sendEmptyMessage(0);
			break;
		case R.id.mnuResume:
			mSbRotate.setVisibility(View.GONE);
			copyBitmap();
			mHandler.sendEmptyMessage(0);
			break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.llEffect:
			openContextMenu(mEffectMenu);
			break;
		case R.id.llStart:
			if (mEffectBitmap == null) {
				copyBitmap();
			}
			Rect rect = new Rect();

			rect.left = (int) ((mFlMoveRegion.getLeft() - mFlMoveRegion.mRegionLeft) * mFlMoveRegion.mScale);
			rect.top = (int) ((mFlMoveRegion.getTop() - mFlMoveRegion.mRegionTop) * mFlMoveRegion.mScale);
			rect.right = (int) ((mFlMoveRegion.getRight() - mFlMoveRegion.mRegionLeft) * mFlMoveRegion.mScale);
			rect.bottom = (int) ((mFlMoveRegion.getBottom() - mFlMoveRegion.mRegionTop) * mFlMoveRegion.mScale);
			if (ProcessBitmapRegions.PROCESS_TYPE_MOSAIC
					.equals(ProcessBitmapRegions.processType)) {
				ProcessBitmapRegions.isWorking = true;
				mPbPhotoProcess.setVisibility(View.VISIBLE);
				mPbPhotoProcess.setProgress(0);
				mFlMoveRegion.setVisibility(View.GONE);

				ProcessBitmapRegions processBitmapRegions = new ProcessBitmapRegions(
						new MosaicProcess(mEffectBitmap, rect));

				processBitmapRegions.work();
			} else if (ProcessBitmapRegions.PROCESS_TYPE_CROP
					.equals(ProcessBitmapRegions.processType)) {
				mEffectBitmap = Bitmap.createBitmap(mEffectBitmap, rect.left,
						rect.top, rect.right - rect.left, rect.bottom
								- rect.top);
				mHandler.sendEmptyMessage(0);
			}
			break;
		case R.id.llBack:
			mTvUnCleanLayer.setVisibility(View.GONE);
			mFlMoveRegion.setVisibility(View.GONE);
			mMosaicButton.setVisibility(View.GONE);
			mMainButton.setVisibility(View.VISIBLE);
			break;
		case R.id.llSave:
			try {
				String fn = Tools.getEffectTempImageFilename();

				FileOutputStream fos = new FileOutputStream(fn);

				mEffectBitmap.compress(CompressFormat.JPEG, 100, fos);

				fos.close();

				Intent intent = new Intent();
				intent.putExtra("filename", fn);

				setResult(CODE_RESULT_SAVE, intent);

				finish();
			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.llCancel:
			setResult(CODE_RESULT_CANCEL);
			finish();
			break;
		}
	}

	private Bitmap mOldEffectBitmap;

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Matrix matrix = new Matrix();
		matrix.setRotate(progress);
		mEffectBitmap = Bitmap.createBitmap(mOldEffectBitmap, 0, 0,
				mOldEffectBitmap.getWidth(), mOldEffectBitmap.getHeight(),
				matrix, true);
		mFlMoveRegion.mBitmap = mEffectBitmap;
		mHandler.sendEmptyMessage(0);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		switch (v.getId()) {
		case R.id.tvEffectMenu:
			getMenuInflater().inflate(R.menu.effect_menu, menu);
			menu.findItem(R.id.mnuGray).setOnMenuItemClickListener(this);
			menu.findItem(R.id.mnuMosaic).setOnMenuItemClickListener(this);
			menu.findItem(R.id.mnuCrop).setOnMenuItemClickListener(this);
			menu.findItem(R.id.mnuRotate).setOnMenuItemClickListener(this);
			menu.findItem(R.id.mnuResume).setOnMenuItemClickListener(this);
			break;
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	private float mOldX;
	private float mOldY;
	private boolean mLeftTopResize = false;
	private boolean mLeftBottomResize = false;
	private boolean mRightTopResize = false;
	private boolean mRightBottomResize = false;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		// 当鼠标或手指按下时的动作
		case MotionEvent.ACTION_DOWN:
			// 如果成功装载了原图像，则设置FrameLayoutExt中保存
			// 原图像的变量(mBitmap)
			if (mEffectBitmap != null)
				mFlMoveRegion.mBitmap = mEffectBitmap;
			// 按住特效区域左上角的拖动块，拖动鼠标，即可以从左上角开始缩放图像
			if (event.getX() <= ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() <= ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mLeftTopResize = true;
				// 设置左上角拖动块获得焦点状态的图像资源ID
				mFlMoveRegion.mLeftTopRegionResourceId = R.drawable.left_top_focused;
			}
			// 按住特效区域左下角的拖动块，拖动鼠标，即可以从左下角开始缩放图像
			else if (event.getX() <= ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() >= v.getHeight()
							- ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mLeftBottomResize = true;
				// 设置左下角拖动块获得焦点状态的图像资源ID
				mFlMoveRegion.mLeftBottomRegionResourceId = R.drawable.left_bottom_focused;
			}
			// 按住特效区域右上角的拖动块，拖动鼠标，即可以从右上角开始缩放图像
			else if (event.getX() >= v.getWidth()
					- ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() <= ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mRightTopResize = true;
				// 设置右上角拖动块获得焦点状态的图像资源ID
				mFlMoveRegion.mRightTopRegionResourceId = R.drawable.right_top_focused;
			}
			// 按住特效区域右下角的拖动块上，拖动鼠标，即可以从右下角开始缩放图像
			else if (event.getX() >= v.getWidth()
					- ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() >= v.getHeight()
							- ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mRightBottomResize = true;
				// 设置右下角拖动块获得焦点状态的图像资源ID
				mFlMoveRegion.mRightBottomRegionResourceId = R.drawable.right_bottom_focused;
			}
			// 在特效区域的中心单击并按住鼠标键左键不放，特效区域边框线的颜色会改变
			// 表示特效区域被选中，可以拖动该区域
			else {
				mFlMoveRegion.mRectLineColor = ProcessBitmapRegions.RECT_LINE_COLOR_FOCUSED;
				mFlMoveRegion.mRectLineWidth = ProcessBitmapRegions.RECT_LINE_WIDTH_FOCUSED;
			}

			// 保存当前位置X坐标
			mOldX = event.getRawX();
			// 保存当前位置Y坐标
			mOldY = event.getRawY();
			mFlMoveRegion.invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			// 计算出特效区域左上角新的横坐标
			int left = v.getLeft() + (int) (event.getRawX() - mOldX);
			// 计算出特效区域左上角新的纵坐标
			int top = v.getTop() + (int) (event.getRawY() - mOldY);
			// 计算出特效区域右下角新的横坐标
			int right = v.getRight() + (int) (event.getRawX() - mOldX);
			// 计算出特效区域右下角新的纵坐标
			int bottom = v.getBottom() + (int) (event.getRawY() - mOldY);
			// 如果左上角新横坐标超过边界，重新设置该坐标
			if (left < mFlMoveRegion.mRegionLeft) {
				left = mFlMoveRegion.mRegionLeft;
				right = left + v.getWidth();
			}
			// 如果右下角新的横坐标超过边界，重新设置该坐标
			if (right > mFlMoveRegion.mRegionRight) {
				right = mFlMoveRegion.mRegionRight;
				left = right - v.getWidth();
			}
			// 如果左上角新的纵坐标超过边界，重新设置该坐标
			if (top < mFlMoveRegion.mRegionTop) {
				top = mFlMoveRegion.mRegionTop;
				bottom = top + v.getHeight();
			}
			// 如果右下角新的纵坐标超过边界，重新设置该坐标
			if (bottom > mFlMoveRegion.mRegionBottom) {
				bottom = mFlMoveRegion.mRegionBottom;
				top = bottom - v.getHeight();
			}
			// 拖动特效区域左上角改变特效区域的大小
			if (mLeftTopResize) {
				if (v.getRight() - left < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					left = v.getLeft();
				if (v.getBottom() - top < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					top = v.getTop();
				// 重新设置特效区域的大小
				v.layout(left, top, v.getRight(), v.getBottom());
			}
			// 拖动特效区域左下角改变特效区域的大小
			else if (mLeftBottomResize) {
				if (v.getRight() - left < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					left = v.getLeft();
				if (bottom - v.getTop() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					bottom = v.getBottom();
				v.layout(left, v.getTop(), v.getRight(), bottom);
			}
			// 拖动特效区域右上角改变特效区域的大小
			else if (mRightTopResize) {
				if (v.getBottom() - top < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					top = v.getTop();
				if (right - v.getLeft() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					right = v.getRight();
				v.layout(v.getLeft(), top, right, v.getBottom());
			}
			// 拖动特效区域右下角改变特效区域的大小
			else if (mRightBottomResize) {
				if (right - v.getLeft() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					right = v.getRight();
				if (bottom - v.getTop() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					bottom = v.getBottom();
				v.layout(v.getLeft(), v.getTop(), right, bottom);
			} else {
				// 判断移动框是否越界
				v.layout(left, top, right, bottom);
				v.postInvalidate();
			}
			mOldX = event.getRawX();
			mOldY = event.getRawY();
			break;
		// 当鼠标或手指在特效区域抬起时的动作
		case MotionEvent.ACTION_UP:
			// 恢复特效区域边框线默认的颜色
			mFlMoveRegion.mRectLineColor = ProcessBitmapRegions.RECT_LINE_COLOR_NORMAL;
			mFlMoveRegion.mRectLineWidth = ProcessBitmapRegions.RECT_LINE_WIDTH_NORMAL;
			mFlMoveRegion.mLeftTopRegionResourceId = R.drawable.left_top_normal;
			mFlMoveRegion.mLeftBottomRegionResourceId = R.drawable.left_bottom_normal;
			mFlMoveRegion.mRightTopRegionResourceId = R.drawable.right_top_normal;
			mFlMoveRegion.mRightBottomRegionResourceId = R.drawable.right_bottom_normal;
			mFlMoveRegion.invalidate();
			mLeftTopResize = false;
			mLeftBottomResize = false;
			mRightTopResize = false;
			mRightBottomResize = false;
			break;
		}
		return true;
	}

}
