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
	// ��Ч�����Զ��岼��
	private FrameLayoutExt mFlMoveRegion;

	// ��������Ļ�·��ļ�����εİ�ť
	private View mMainButton;
	private View mMosaicButton;

	private TextView mTvStart;

	// ����ע��Context Menu,������ʾ
	private View mEffectMenu;

	private SeekBar mSbRotate;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			try {
				// ��ʾ��������Чͼ��
				mIvDrawing.setImageBitmap(mEffectBitmap);
				// ����Ч������Ϊ���һ�δ������Чͼ��
				mFlMoveRegion.mBitmap = mEffectBitmap;
				// ������Ч����
				mFlMoveRegion.setVisibility(View.GONE);
				// ���ؽ�����
				mPbPhotoProcess.setVisibility(View.GONE);
				// �������ʲ㣨������ͼ���ϵİ�͸��TextView�ؼ���
				mTvUnCleanLayer.setVisibility(View.GONE);
				// ������ʾ�����水ť
				mMainButton.setVisibility(View.VISIBLE);
				// ���������˰�ť
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

		// ��ȡҪ�༭��ͼ���ļ���
		String filename = getIntent().getStringExtra("filename");

		if (filename == null) {
			mSourceBitmap = (Bitmap) getIntent().getExtras().get("bitmap");
		} else {
			mSourceBitmap = Tools.getFitBitmap(filename, BITMAP_MAX_SIZE);
		}

		copyBitmap();

		mFlMoveRegion.mBitmap = mEffectBitmap;
		mFlMoveRegion.mIvDrawing = mIvDrawing;

		// ��ʾԴͼ��
		mIvDrawing.setImageBitmap(mSourceBitmap);

		mEffectMenu = findViewById(R.id.tvEffectMenu);

		ProcessBitmapRegions.mAllThreadEnd = this;
		ProcessBitmapRegions.processType = "";
		// ע�������Ĳ˵�
		registerForContextMenu(mEffectMenu);

	}

	/**
	 * �ָ�ԭʼͼ��
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
			Toast.makeText(this, "�������ڽ����У��޷�������������.", Toast.LENGTH_LONG).show();
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
			// ��ʾ�����˰�ť����
			mMosaicButton.setVisibility(View.VISIBLE);
			mFlMoveRegion.setVisibility(View.VISIBLE);
			mTvStart.setText("������");

			mTvUnCleanLayer.setVisibility(View.VISIBLE);

			mTvUnCleanLayer.getLayoutParams().width = regionRight - regionLeft;
			mTvUnCleanLayer.getLayoutParams().height = regionBottom - regionTop;
			mTvUnCleanLayer.requestLayout();
			mSbRotate.setVisibility(View.GONE);
			break;
		case R.id.mnuCrop:
			if (ProcessBitmapRegions.isWorking) {
				Toast.makeText(this, "�������ڽ����У��޷�������������.", Toast.LENGTH_LONG)
						.show();
				break;
			}
			ProcessBitmapRegions.processType = ProcessBitmapRegions.PROCESS_TYPE_MOSAIC;
			mMainButton.setVisibility(View.GONE);
			mMosaicButton.setVisibility(View.VISIBLE);
			mFlMoveRegion.setVisibility(View.VISIBLE);
			mTvStart.setText("��ͼ");
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
		// ��������ָ����ʱ�Ķ���
		case MotionEvent.ACTION_DOWN:
			// ����ɹ�װ����ԭͼ��������FrameLayoutExt�б���
			// ԭͼ��ı���(mBitmap)
			if (mEffectBitmap != null)
				mFlMoveRegion.mBitmap = mEffectBitmap;
			// ��ס��Ч�������Ͻǵ��϶��飬�϶���꣬�����Դ����Ͻǿ�ʼ����ͼ��
			if (event.getX() <= ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() <= ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mLeftTopResize = true;
				// �������Ͻ��϶����ý���״̬��ͼ����ԴID
				mFlMoveRegion.mLeftTopRegionResourceId = R.drawable.left_top_focused;
			}
			// ��ס��Ч�������½ǵ��϶��飬�϶���꣬�����Դ����½ǿ�ʼ����ͼ��
			else if (event.getX() <= ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() >= v.getHeight()
							- ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mLeftBottomResize = true;
				// �������½��϶����ý���״̬��ͼ����ԴID
				mFlMoveRegion.mLeftBottomRegionResourceId = R.drawable.left_bottom_focused;
			}
			// ��ס��Ч�������Ͻǵ��϶��飬�϶���꣬�����Դ����Ͻǿ�ʼ����ͼ��
			else if (event.getX() >= v.getWidth()
					- ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() <= ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mRightTopResize = true;
				// �������Ͻ��϶����ý���״̬��ͼ����ԴID
				mFlMoveRegion.mRightTopRegionResourceId = R.drawable.right_top_focused;
			}
			// ��ס��Ч�������½ǵ��϶����ϣ��϶���꣬�����Դ����½ǿ�ʼ����ͼ��
			else if (event.getX() >= v.getWidth()
					- ProcessBitmapRegions.RESIZE_REGION_SIZE
					&& event.getY() >= v.getHeight()
							- ProcessBitmapRegions.RESIZE_REGION_SIZE) {
				mRightBottomResize = true;
				// �������½��϶����ý���״̬��ͼ����ԴID
				mFlMoveRegion.mRightBottomRegionResourceId = R.drawable.right_bottom_focused;
			}
			// ����Ч��������ĵ�������ס����������ţ���Ч����߿��ߵ���ɫ��ı�
			// ��ʾ��Ч����ѡ�У������϶�������
			else {
				mFlMoveRegion.mRectLineColor = ProcessBitmapRegions.RECT_LINE_COLOR_FOCUSED;
				mFlMoveRegion.mRectLineWidth = ProcessBitmapRegions.RECT_LINE_WIDTH_FOCUSED;
			}

			// ���浱ǰλ��X����
			mOldX = event.getRawX();
			// ���浱ǰλ��Y����
			mOldY = event.getRawY();
			mFlMoveRegion.invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			// �������Ч�������Ͻ��µĺ�����
			int left = v.getLeft() + (int) (event.getRawX() - mOldX);
			// �������Ч�������Ͻ��µ�������
			int top = v.getTop() + (int) (event.getRawY() - mOldY);
			// �������Ч�������½��µĺ�����
			int right = v.getRight() + (int) (event.getRawX() - mOldX);
			// �������Ч�������½��µ�������
			int bottom = v.getBottom() + (int) (event.getRawY() - mOldY);
			// ������Ͻ��º����곬���߽磬�������ø�����
			if (left < mFlMoveRegion.mRegionLeft) {
				left = mFlMoveRegion.mRegionLeft;
				right = left + v.getWidth();
			}
			// ������½��µĺ����곬���߽磬�������ø�����
			if (right > mFlMoveRegion.mRegionRight) {
				right = mFlMoveRegion.mRegionRight;
				left = right - v.getWidth();
			}
			// ������Ͻ��µ������곬���߽磬�������ø�����
			if (top < mFlMoveRegion.mRegionTop) {
				top = mFlMoveRegion.mRegionTop;
				bottom = top + v.getHeight();
			}
			// ������½��µ������곬���߽磬�������ø�����
			if (bottom > mFlMoveRegion.mRegionBottom) {
				bottom = mFlMoveRegion.mRegionBottom;
				top = bottom - v.getHeight();
			}
			// �϶���Ч�������ϽǸı���Ч����Ĵ�С
			if (mLeftTopResize) {
				if (v.getRight() - left < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					left = v.getLeft();
				if (v.getBottom() - top < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					top = v.getTop();
				// ����������Ч����Ĵ�С
				v.layout(left, top, v.getRight(), v.getBottom());
			}
			// �϶���Ч�������½Ǹı���Ч����Ĵ�С
			else if (mLeftBottomResize) {
				if (v.getRight() - left < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					left = v.getLeft();
				if (bottom - v.getTop() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					bottom = v.getBottom();
				v.layout(left, v.getTop(), v.getRight(), bottom);
			}
			// �϶���Ч�������ϽǸı���Ч����Ĵ�С
			else if (mRightTopResize) {
				if (v.getBottom() - top < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					top = v.getTop();
				if (right - v.getLeft() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					right = v.getRight();
				v.layout(v.getLeft(), top, right, v.getBottom());
			}
			// �϶���Ч�������½Ǹı���Ч����Ĵ�С
			else if (mRightBottomResize) {
				if (right - v.getLeft() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					right = v.getRight();
				if (bottom - v.getTop() < ProcessBitmapRegions.MIN_MOVE_REGION_SIZE)
					bottom = v.getBottom();
				v.layout(v.getLeft(), v.getTop(), right, bottom);
			} else {
				// �ж��ƶ����Ƿ�Խ��
				v.layout(left, top, right, bottom);
				v.postInvalidate();
			}
			mOldX = event.getRawX();
			mOldY = event.getRawY();
			break;
		// ��������ָ����Ч����̧��ʱ�Ķ���
		case MotionEvent.ACTION_UP:
			// �ָ���Ч����߿���Ĭ�ϵ���ɫ
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
