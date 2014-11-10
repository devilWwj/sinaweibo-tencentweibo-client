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
 * 自定义布局
 * 
 * 继承FrameLayout
 * 
 * @author Lining
 * 
 */
public class FrameLayoutExt extends FrameLayout {

	public Bitmap mBitmap;
	// 装载图像的ImageView控件
	public ImageView mIvDrawing;
	public float mScale;
	// 图像区域距画布上边缘的距离
	public int mRegionTop;
	// 图像区域距画布下边缘的距离
	public int mRegionBottom;
	// 图像区域距画布左边缘的距离
	public int mRegionLeft;
	// 图像区域距画布右边缘的距离
	public int mRegionRight;
	// 特效区域边框线的宽度
	public int mRectLineWidth = 1;
	// 特效区域边框线的默认颜色
	public int mRectLineColor = Color.WHITE;
	// 特效区域四角的拖动块大小
	private int mResizeLength = 40;
	// 左上角拖动块图像
	private Bitmap mLeftTopRegion;
	// 左下角拖动块图像
	private Bitmap mLeftBottomRegion;
	// 右上角拖动块图像
	private Bitmap mRightTopRegion;
	// 有下角拖动块图像
	private Bitmap mRightBottomRegion;
	// 四个方位拖动块图像的资源ID
	public int mLeftTopRegionResourceId = R.drawable.left_top_normal; // 左上
	public int mLeftBottomRegionResourceId = R.drawable.left_bottom_normal; // 左下
	public int mRightTopRegionResourceId = R.drawable.right_top_normal; // 右上
	public int mRightBottomRegionResourceId = R.drawable.right_bottom_normal; // 右下
	private int mWidth;
	private int mHeight;

	private Context mContext;

	public FrameLayoutExt(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * 初始话变量的值
	 */
	public void init() {
		// 获得画布的宽度
		int screenWidth = mIvDrawing.getWidth();
		// 获得画布的高度
		int screenHeight = mIvDrawing.getHeight();
		// 获得微博图像的宽度
		int sourceWidth = mBitmap.getWidth();
		// 获得微博图像的高度
		int sourceHeight = mBitmap.getHeight();

		// 微博图像的高度/高度比大于画布的宽度/高度比
		if (sourceWidth * screenHeight > sourceHeight * screenWidth) {
			// 设置图像的高度
			int insideHeight = sourceHeight * screenWidth / sourceWidth;
			// 设置图像上边缘距离画布上边缘的距离
			mRegionTop = (screenHeight - insideHeight) / 2;
			// 设置图像左边缘距离画布左边缘的距离
			mRegionLeft = 0;
			// 设置图像右边缘距离画布右边缘的距离
			mRegionRight = mIvDrawing.getMeasuredWidth();
			// 设置图像下边缘距离画布下边缘的距离
			mRegionBottom = mRegionTop + insideHeight + 1;
			// 计算出原图像与显示在屏幕上的图像的缩放比
			mScale = (float) sourceWidth / (float) screenWidth;
		} else {
			int insideWidth = sourceWidth * screenHeight / sourceHeight;
			mRegionLeft = (screenWidth - insideWidth) / 2;
			mRegionTop = 0;

			mRegionRight = mRegionLeft + insideWidth + 1;
			mRegionBottom = mIvDrawing.getMeasuredHeight();
			mScale = (float) sourceHeight / (float) screenHeight;
		}
		// 得到四个角的图片
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
		// 将原图像的特效区域复制到屏幕显示的特效区域中
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
		// 绘制特效区域的边框线
		canvas.drawRect(new Rect(1, 1, mWidth - 2, mHeight - 2), paint);
		// 绘制图像特效区域左上角的拖动块图像
		canvas.drawBitmap(
				mLeftTopRegion,
				null,
				new Rect(0, 0, mLeftTopRegion.getWidth(), mLeftTopRegion
						.getHeight()), null);
		// 绘制图像特效区域左下角的拖动块图像
		canvas.drawBitmap(mLeftBottomRegion, null, new Rect(0, mHeight
				- mLeftBottomRegion.getHeight(), mLeftBottomRegion.getWidth(),
				mHeight), null);
		// 绘制图像特效区域右上角的拖动块图像
		canvas.drawBitmap(mRightTopRegion, null,
				new Rect(mWidth - mLeftBottomRegion.getWidth(), 0, mWidth,
						mRightTopRegion.getHeight()), null);
		// 绘制图像特效区域右下角的拖动块图像
		canvas.drawBitmap(mRightBottomRegion, null,
				new Rect(mWidth - mRightBottomRegion.getWidth(), mHeight
						- mRightBottomRegion.getHeight(), mWidth, mHeight),
				null);

	}

	// 当画布的大小变化时，重新设置画布的宽度和高度
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mWidth = w;
		mHeight = h;
		super.onSizeChanged(w, h, oldw, oldh);
	}

}
