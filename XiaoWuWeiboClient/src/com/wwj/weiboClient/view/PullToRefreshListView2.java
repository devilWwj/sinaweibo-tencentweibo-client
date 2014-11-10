package com.wwj.weiboClient.view;


import com.wwj.weiboClient.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

/**
 * 下拉刷新列表
 * 
 * @author wwj 实现滚动监听器
 */
public class PullToRefreshListView2 extends ListView implements OnScrollListener {

	// 四种状态（点击刷新，下拉刷新，释放刷新，正在刷新）
	private static final int TAP_TO_REFRESH = 1;
	private static final int PULL_TO_REFRESH = 2;
	private static final int RELEASE_TO_REFRESH = 3;
	private static final int REFRESHING = 4;

	private static final String TAG = "PullToRefreshListView";

	private OnRefreshListener mOnRefreshListener;

	/**
	 * Listener that will receive notifications every time the list scrolls.
	 */
	private OnScrollListener mOnScrollListener;
	private LayoutInflater mInflater;

	// 刷新视图
	private RelativeLayout mRefreshView;
	// 显示刷新的文字
	private TextView mRefreshViewText;
	// 箭头图片
	private ImageView mRefreshViewImage;
	// 进度条
	private ProgressBar mRefreshViewProgress;
	// 显示最近更新时间
	private TextView mRefreshViewLastUpdated;

	// 当前滚动状态
	private int mCurrentScrollState;
	// 当前刷新状态
	private int mRefreshState;

	// 旋转动画
	private RotateAnimation mFlipAnimation;
	private RotateAnimation mReverseFlipAnimation;

	// 刷新视图的高度
	private int mRefreshViewHeight;
	private int mRefreshOriginalTopPadding;
	// 最后点击的位置
	private int mLastMotionY;

	private boolean mBounceHack;

	public PullToRefreshListView2(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshListView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullToRefreshListView2(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// Load all of the animations we need in code rather than through XML
		// fromDegrees：旋转的开始角度。
		// toDegrees：旋转的结束角度。
		// pivotXType：X轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// pivotXValue：X坐标的伸缩值。
		// pivotYType：Y轴的伸缩模式，可以取值为ABSOLUTE、RELATIVE_TO_SELF、RELATIVE_TO_PARENT。
		// pivotYValue：Y坐标的伸缩值。
		mFlipAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mFlipAnimation.setInterpolator(new LinearInterpolator());
		mFlipAnimation.setDuration(250);
		mFlipAnimation.setFillAfter(true);
		mReverseFlipAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
		mReverseFlipAnimation.setDuration(250);
		mReverseFlipAnimation.setFillAfter(true);

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 实例化各个view
		mRefreshView = (RelativeLayout) mInflater.inflate(
				R.layout.pull_to_refresh_header, this, false);
		mRefreshViewText = (TextView) mRefreshView
				.findViewById(R.id.pull_to_refresh_text);
		mRefreshViewImage = (ImageView) mRefreshView
				.findViewById(R.id.pull_to_refresh_image);
		mRefreshViewProgress = (ProgressBar) mRefreshView
				.findViewById(R.id.pull_to_refresh_progress);
		mRefreshViewLastUpdated = (TextView) mRefreshView
				.findViewById(R.id.pull_to_refresh_updated_at);

		// 设置图像最小高度
		mRefreshViewImage.setMinimumHeight(50);
		mRefreshView.setOnClickListener(new OnClickRefreshListener());
		mRefreshOriginalTopPadding = mRefreshView.getPaddingTop();

		// 初始化刷新状态
		mRefreshState = TAP_TO_REFRESH;

		// 添加视图到列表顶部
		addHeaderView(mRefreshView);

		super.setOnScrollListener(this);

		// 测量视图
		measureView(mRefreshView);
		// 刷新视图的原始高度
		mRefreshViewHeight = mRefreshView.getMeasuredHeight();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setSelection(1);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		setSelection(1);
	}

	/**
	 * Set the listener that will receive notifications every time the list
	 * scrolls. 设置这个监听器每次列表滚动都将收到提示
	 * 
	 * @param l
	 *            The scroll listener.
	 */
	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener l) {
		mOnScrollListener = l;
	}

	/**
	 * Register a callback to be invoked when this list should be refreshed.
	 * 注册一个回调接口，当列表应当刷新的时候去调用这个回调接口
	 * 
	 * @param onRefreshListener
	 *            The callback to run.
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		mOnRefreshListener = onRefreshListener;
	}

	/**
	 * Set a text to represent when the list was last updated. 设置一个文本表示列表最近更新的时间
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void setLastUpdated(CharSequence lastUpdated) {
		if (lastUpdated != null) {
			mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
			mRefreshViewLastUpdated.setText("更新于:" + lastUpdated);
		} else {
			mRefreshViewLastUpdated.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int y = (int) event.getY();
		mBounceHack = false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP: // 抬起
			if (!isVerticalScrollBarEnabled()) {
				// 设置竖向滚动条可用
				setVerticalScrollBarEnabled(true);
			}
			// 如果列表第一项可见的位置为0并且刷新状态不是正在刷新的状态
			if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
				// 如果刷新视图的底部大于或者等于刷新视图原始高度 || 刷新视图的顶部位置大于等于零 && 刷新状态为释放刷新
				if ((mRefreshView.getBottom() >= mRefreshViewHeight || mRefreshView
						.getTop() >= 0) && mRefreshState == RELEASE_TO_REFRESH) {
					// 开始刷新
					mRefreshState = REFRESHING;
					prepareForRefresh();
					onRefresh();
				} else if (mRefreshView.getBottom() < mRefreshViewHeight
						|| mRefreshView.getTop() <= 0) {
					// Abort refresh and scroll down below the refresh view
					resetHeader();
					setSelection(1);
				}
			}
			break;
		case MotionEvent.ACTION_DOWN: // 按下
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE: // 移动
			applyHeaderPadding(event);
			break;
		}
		return super.onTouchEvent(event);
	}

	// 设置头部内边距
	private void applyHeaderPadding(MotionEvent ev) {
		// getHistorySize has been available since API 1
		// 得到点的个数
		int pointerCount = ev.getHistorySize();

		for (int p = 0; p < pointerCount; p++) {
			// 如果刷新状态为释放刷新
			if (mRefreshState == RELEASE_TO_REFRESH) {
				if (isVerticalFadingEdgeEnabled()) {
					setVerticalScrollBarEnabled(false);
				}

				// 获得历史事件的位置
				int historicalY = (int) ev.getHistoricalY(p);

				// Calculate the padding to apply, we divide by 1.7 to simulate
				// a more resistant effect during pull.
				// 计算内边距去应用，我们除以1.7去模拟拉伸的效果当被拉的时候
				int topPadding = (int) (((historicalY - mLastMotionY) - mRefreshViewHeight) / 1.7);

				mRefreshView.setPadding(mRefreshView.getPaddingLeft(),
						topPadding, mRefreshView.getPaddingRight(),
						mRefreshView.getPaddingBottom());
			}
		}
	}

	/**
	 * Sets the header padding back to original size. 设置头部内边距为原始值
	 */
	private void resetHeaderPadding() {
		mRefreshView.setPadding(mRefreshView.getPaddingLeft(),
				mRefreshOriginalTopPadding, mRefreshView.getPaddingRight(),
				mRefreshView.getPaddingBottom());
	}

	/**
	 * Resets the header to the original state. 从新设置头部回到最初状态
	 */
	private void resetHeader() {
		if (mRefreshState != TAP_TO_REFRESH) {
			mRefreshState = TAP_TO_REFRESH;

			resetHeaderPadding();

			// Set refresh view text to the pull label
			mRefreshViewText.setText(R.string.pull_to_refresh_tap_label);
			// Replace refresh drawable with arrow drawable
			mRefreshViewImage
					.setImageResource(R.drawable.ic_pulltorefresh_arrow);
			// Clear the full rotation animation
			mRefreshViewImage.clearAnimation();
			// Hide progress bar and arrow.
			mRefreshViewImage.setVisibility(View.GONE);
			mRefreshViewProgress.setVisibility(View.GONE);
		}
	}

	/**
	 * 测量视图大小
	 * 
	 * @param child
	 */
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// When the refresh view is completely visible, change the text to say
		// "Release to refresh..." and flip the arrow drawable.
		if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
				&& mRefreshState != REFRESHING) {
			if (firstVisibleItem == 0) {
				mRefreshViewImage.setVisibility(View.VISIBLE);
				if ((mRefreshView.getBottom() >= mRefreshViewHeight + 20 || mRefreshView
						.getTop() >= 0) && mRefreshState != RELEASE_TO_REFRESH) {
					mRefreshViewText
							.setText(R.string.pull_to_refresh_release_label);
					mRefreshViewImage.clearAnimation();
					mRefreshViewImage.startAnimation(mFlipAnimation);
					mRefreshState = RELEASE_TO_REFRESH;
				} else if (mRefreshView.getBottom() < mRefreshViewHeight + 20
						&& mRefreshState != PULL_TO_REFRESH) {
					mRefreshViewText
							.setText(R.string.pull_to_refresh_pull_label);
					if (mRefreshState != TAP_TO_REFRESH) {
						mRefreshViewImage.clearAnimation();
						mRefreshViewImage.startAnimation(mReverseFlipAnimation);
					}
					mRefreshState = PULL_TO_REFRESH;
				}
			} else {
				mRefreshViewImage.setVisibility(View.GONE);
				resetHeader();
			}
		} else if (mCurrentScrollState == SCROLL_STATE_FLING
				&& firstVisibleItem == 0 && mRefreshState != REFRESHING) {
			setSelection(1);
			mBounceHack = true;
		} else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
			setSelection(1);
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mCurrentScrollState = scrollState;

		if (mCurrentScrollState == SCROLL_STATE_IDLE) {
			mBounceHack = false;
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	/**
	 * 准备刷新
	 */
	public void prepareForRefresh() {
		resetHeaderPadding();
		// 让箭头不可见
		mRefreshViewImage.setVisibility(View.GONE);
		// We need this hack, otherwise it will keep the previous drawable.
		mRefreshViewImage.setImageDrawable(null);
		// 设置进度条可见
		mRefreshViewProgress.setVisibility(View.VISIBLE);

		// Set refresh view text to the refreshing label
		// 设置刷新视图的文本信息为"加载中..."
		mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);

		// 更改状态为正在刷新
		mRefreshState = REFRESHING;
	}

	public void onRefresh() {
		Log.d(TAG, "onRefresh");

		if (mOnRefreshListener != null) {
			mOnRefreshListener.onRefresh();
		}
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 * 
	 * @param lastUpdated
	 *            Last updated at.
	 */
	public void onRefreshComplete(CharSequence lastUpdated) {
		setLastUpdated(lastUpdated);
		onRefreshComplete();
	}

	/**
	 * Resets the list to a normal state after a refresh.
	 */
	public void onRefreshComplete() {
		Log.d(TAG, "onRefreshComplete");

		resetHeader();

		// If refresh view is visible when loading completes, scroll down to
		// the next item.
		if (getFirstVisiblePosition() == 0) {
			invalidateViews();
			setSelection(1);
		}
	}

	/**
	 * Invoked when the refresh view is clicked on. This is mainly used when
	 * there's only a few items in the list and it's not possible to drag the
	 * list.
	 */
	private class OnClickRefreshListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (mRefreshState != REFRESHING) {
				prepareForRefresh();
				onRefresh();
			}
		}

	}

	/**
	 * Interface definition for a callback to be invoked when list should be
	 * refreshed.
	 */
	public interface OnRefreshListener {
		/**
		 * Called when the list should be refreshed.
		 * <p>
		 * A call to {@link PullToRefreshListView2 #onRefreshComplete()} is
		 * expected to indicate that the refresh has completed.
		 */
		public void onRefresh();
	}
}
