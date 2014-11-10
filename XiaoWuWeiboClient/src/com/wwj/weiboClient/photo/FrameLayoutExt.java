package com.wwj.weiboClient.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wwj.weiboClient.R;

/**
 * �Զ��岼��
 * 
 * �̳�FrameLayout
 * 
 * @author Lining
 * 
 */
public class FrameLayoutExt extends FrameLayout {

	public Bitmap mBitmap;
	// װ��ͼ���ImageView�ؼ�
	public ImageView mIvDrawing;
	public float mScale;
	// ͼ������໭���ϱ�Ե�ľ���
	public int mRegionTop;
	// ͼ������໭���±�Ե�ľ���
	public int mRegionBottom;
	// ͼ������໭�����Ե�ľ���
	public int mRegionLeft;
	// ͼ������໭���ұ�Ե�ľ���
	public int mRegionRight;
	// ��Ч����߿��ߵĿ��
	public int mRectLineWidth = 1;
	// ��Ч����߿��ߵ�Ĭ����ɫ
	public int mRectLineColor = Color.WHITE;
	// ��Ч�����Ľǵ��϶����С
	private int mResizeLength = 40;
	// ���Ͻ��϶���ͼ��
	private Bitmap mLeftTopRegion;
	// ���½��϶���ͼ��
	private Bitmap mLeftBottomRegion;
	// ���Ͻ��϶���ͼ��
	private Bitmap mRightTopRegion;
	// ���½��϶���ͼ��
	private Bitmap mRightBottomRegion;
	// �ĸ���λ�϶���ͼ�����ԴID
	public int mLeftTopRegionResourceId = R.drawable.left_top_normal; // ����
	public int mLeftBottomRegionResourceId = R.drawable.left_bottom_normal; // ����
	public int mRightTopRegionResourceId = R.drawable.right_top_normal; // ����
	public int mRightBottomRegionResourceId = R.drawable.right_bottom_normal; // ����
	private int mWidth;
	private int mHeight;

	private Context mContext;

	public FrameLayoutExt(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * ��ʼ��������ֵ
	 */
	public void init() {
		// ��û����Ŀ��
		int screenWidth = mIvDrawing.getWidth();
		// ��û����ĸ߶�
		int screenHeight = mIvDrawing.getHeight();
		// ���΢��ͼ��Ŀ��
		int sourceWidth = mBitmap.getWidth();
		// ���΢��ͼ��ĸ߶�
		int sourceHeight = mBitmap.getHeight();

		// ΢��ͼ��ĸ߶�/�߶ȱȴ��ڻ����Ŀ��/�߶ȱ�
		if (sourceWidth * screenHeight > sourceHeight * screenWidth) {
			// ����ͼ��ĸ߶�
			int insideHeight = sourceHeight * screenWidth / sourceWidth;
			// ����ͼ���ϱ�Ե���뻭���ϱ�Ե�ľ���
			mRegionTop = (screenHeight - insideHeight) / 2;
			// ����ͼ�����Ե���뻭�����Ե�ľ���
			mRegionLeft = 0;
			// ����ͼ���ұ�Ե���뻭���ұ�Ե�ľ���
			mRegionRight = mIvDrawing.getMeasuredWidth();
			// ����ͼ���±�Ե���뻭���±�Ե�ľ���
			mRegionBottom = mRegionTop + insideHeight + 1;
			// �����ԭͼ������ʾ����Ļ�ϵ�ͼ������ű�
			mScale = (float) sourceWidth / (float) screenWidth;
		} else {
			int insideWidth = sourceWidth * screenHeight / sourceHeight;
			mRegionLeft = (screenWidth - insideWidth) / 2;
			mRegionTop = 0;

			mRegionRight = mRegionLeft + insideWidth + 1;
			mRegionBottom = mIvDrawing.getMeasuredHeight();
			mScale = (float) sourceHeight / (float) screenHeight;
		}
		// �õ��ĸ��ǵ�ͼƬ
		mLeftTopRegion = BitmapFactory.decodeResource(mContext.getResources(),
				mLeftTopRegionResourceId);
		mLeftBottomRegion = BitmapFactory.decodeResource(
				mContext.getResources(), mLeftBottomRegionResourceId);
		mRightTopRegion = BitmapFactory.decodeResource(mContext.getResources(),
				mRightTopRegionResourceId);
		mRightBottomRegion = BitmapFactory.decodeResource(
				mContext.getResources(), mRightBottomRegionResourceId);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		init();
		// ��ԭͼ�����Ч�����Ƶ���Ļ��ʾ����Ч������
		canvas.drawBitmap(mBitmap, new Rect(
				(int) ((getLeft() - mRegionLeft) * mScale),
				(int) ((getTop() - mRegionTop) * mScale) + 2,
				(int) ((getRight() - mRegionLeft) * mScale),
				(int) ((getBottom() - mRegionTop) * mScale)), new Rect(0, 0,
				mWidth, mHeight), null);
		Paint paint = new Paint();
		paint.setColor(mRectLineColor);
		paint.setStrokeWidth(mRectLineWidth);
		paint.setStyle(Style.STROKE);
		// ������Ч����ı߿���
		canvas.drawRect(new Rect(1, 1, mWidth - 2, mHeight - 2), paint);
		// ����ͼ����Ч�������Ͻǵ��϶���ͼ��
		canvas.drawBitmap(
				mLeftTopRegion,
				null,
				new Rect(0, 0, mLeftTopRegion.getWidth(), mLeftTopRegion
						.getHeight()), null);
		// ����ͼ����Ч�������½ǵ��϶���ͼ��
		canvas.drawBitmap(mLeftBottomRegion, null, new Rect(0, mHeight
				- mLeftBottomRegion.getHeight(), mLeftBottomRegion.getWidth(),
				mHeight), null);
		// ����ͼ����Ч�������Ͻǵ��϶���ͼ��
		canvas.drawBitmap(mRightTopRegion, null,
				new Rect(mWidth - mLeftBottomRegion.getWidth(), 0, mWidth,
						mRightTopRegion.getHeight()), null);
		// ����ͼ����Ч�������½ǵ��϶���ͼ��
		canvas.drawBitmap(mRightBottomRegion, null,
				new Rect(mWidth - mRightBottomRegion.getWidth(), mHeight
						- mRightBottomRegion.getHeight(), mWidth, mHeight),
				null);

	}

	// �������Ĵ�С�仯ʱ���������û����Ŀ�Ⱥ͸߶�
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
