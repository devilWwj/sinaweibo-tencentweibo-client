package com.wwj.weiboClient.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * 【微博列表适配器 】主要用来显示首页、提示我的、收藏微博列表
 * 
 * @author wwj
 * 
 */
public class SinaWeiboListAdapter extends BaseAdapter implements Const,
		DoneAndProcess {
	protected Activity context;
	protected int type;
	protected LayoutInflater layoutInflater;
	protected List<Status> statuses;

	public SinaWeiboListAdapter(Activity context) {
		super();
		this.context = context;
	}

	/**
	 * 需要传入一个已经封装微博数据的List对象 type表示
	 * 
	 * @param activity
	 * @param statuses
	 * @param type
	 *            表示处理的微博数据类型，在Const中定义相关常量
	 */
	public SinaWeiboListAdapter(Activity context, List<Status> statuses,
			int type) {
		this.context = context;
		this.type = type;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.statuses = new ArrayList<Status>();
		if (statuses != null)
			this.statuses.addAll(statuses);
		try {
			// 保存微博数据
			StorageManager.saveList(statuses, PATH_STORAGE, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
		// 获取微博数，加1为了最后一项显示"更多"
		return statuses.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	// 获取制定位置的Status对象
	public Status getStatus(int position) {
		if (statuses.size() > 0) {
			return statuses.get(position);
		} else {
			return null;
		}
	}

	// 获取已经显示的微博的最小ID
	public long getMinId() {
		if (statuses.size() > 0)
			// 除去更多项
			return statuses.get(statuses.size() - 1).id;
		else
			return Long.MAX_VALUE;
	}

	// 获取已经显示的微博最大ID
	public long getMaxId() {
		if (statuses.size() > 0)
			return statuses.get(0).id;
		else
			return 0;
	}

	// 添加新的微博
	// 在刷新、显示更多微博时会根据不同的情况决定如何添加微博
	public void putStatuses(List<Status> statuses) {
		if (statuses == null || this.statuses == null)
			return;
		if (statuses.size() == 0)
			return;
		if (this.statuses.size() == 0) {
			this.statuses.addAll(statuses);
		} else if (statuses
				.get(0)
				.getCreatedAt()
				.before(this.statuses.get(this.statuses.size() - 1)
						.getCreatedAt())) {
			this.statuses.addAll(statuses);
			// 添加的statuses比原来的新，并且数量小于等于默认返回数量，直接将statuses添加到前面
		} else if (statuses.get(statuses.size() - 1).getCreatedAt()
				.after(this.statuses.get(0).getCreatedAt())
				&& statuses.size() <= DEFAULT_STATUS_COUNT) {
			this.statuses.addAll(0, statuses);
		} else {
			this.statuses.clear();
			this.statuses.addAll(statuses);
		}
		try {
			// 保存微博数据
			StorageManager.saveList(this.statuses, PATH_STORAGE, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 并且刷新ListView
		this.notifyDataSetChanged();
	}

	// 控制更多动画
	private boolean showMoreAnimFlag = false;

	// 显示更多动画
	public void showMoreAnim() {
		showMoreAnimFlag = true;
		notifyDataSetChanged();
	}

	// 隐藏更多动画
	public void hideMoreAnim() {
		showMoreAnimFlag = false;
		notifyDataSetChanged();
	}

	// 通过url装载要显示的图像，如果图像文件不存在，回通过hideView标志决定是否隐藏ImageView组件
	// hideView: true 隐藏ImageView hideView：false 不做任何动作
	private void loadImage(ImageView imageView, String url, boolean hideView) {
		if (url != null) {
			String imageUrl = SinaWeiboManager.getImageurl(url, context);
			if (imageUrl != null) {
				imageView.setImageURI(Uri.fromFile(new File(imageUrl)));
				imageView.setVisibility(View.VISIBLE);
				return;
			}
		}
		if (hideView)
			imageView.setVisibility(View.GONE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.sina_weibo_list_item,
					null);
		}

		View weiboListItem = convertView.findViewById(R.id.ll_weibo_list_item); // 微博列表项
		View moreWeiboListItem = convertView
				.findViewById(R.id.rl_more_weibo_list_item); // "更多"列表项
		// 当列表项不是最后一项时继续执行
		if (position < statuses.size()) {
			weiboListItem.setVisibility(View.VISIBLE);
			moreWeiboListItem.setVisibility(View.GONE);// 不到最后一项不显示
			Status status = statuses.get(position);

			TextView statusText = (TextView) convertView
					.findViewById(R.id.tv_text);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);
			TextView createAt = (TextView) convertView
					.findViewById(R.id.tv_created_at);
			ImageView profileImage = (ImageView) convertView
					.findViewById(R.id.iv_profile_image);
			profileImage.setImageResource(R.drawable.avatar_default); // 设置默认头像
			ImageView picture = (ImageView) convertView
					.findViewById(R.id.iv_picture);
			ImageView statusImage = (ImageView) convertView
					.findViewById(R.id.iv_status_image);
			ImageView verified = (ImageView) convertView
					.findViewById(R.id.iv_verified);

			verified.setVisibility(View.GONE); // 设置认证不可见

			if (status.user != null) {
				// 设置用户认证图标，即各种颜色的“V”或其他符号
				Tools.userVerified(verified, status.user.verified_type);
			}

			statusImage.setImageBitmap(null);
			LinearLayout insideContent = (LinearLayout) convertView
					.findViewById(R.id.ll_inside_content);
			ImageView retweetdetailImage = (ImageView) convertView
					.findViewById(R.id.iv_retweetdetail_image);
			retweetdetailImage.setImageBitmap(null);
			TextView retweetdetailText = (TextView) convertView
					.findViewById(R.id.tv_retweetdetail_text);
			TextView source = (TextView) convertView
					.findViewById(R.id.tv_source);
			Button btnRetweet = (Button) convertView
					.findViewById(R.id.btn_retweet);
			Button btnComment = (Button) convertView
					.findViewById(R.id.btn_comment);
			Button btnGood = (Button) convertView.findViewById(R.id.btn_good);

			// 装载图像
			if (status.user != null) {
				loadImage(profileImage, status.user.profile_image_url, false);
			}
			loadImage(statusImage, status.thumbnail_pic, true);

			statusText.setText(Tools.changeTextToFace(context,
					Html.fromHtml(Tools.atBlue(status.text))));

			if (status.user != null)
				name.setText(status.user.name); // 显示用户昵称
			// 当前微博有图像
			if (SinaWeiboManager.hasPicture(status))
				picture.setVisibility(View.VISIBLE);
			else
				picture.setVisibility(View.INVISIBLE);

			createAt.setText(Tools.getTimeStr(status.getCreatedAt(), new Date()));
			source.setText("来自: " + status.getTextSource()); // 设置微博来源

			if (status.retweeted_status != null // 如果转发的数据不为空
					&& status.retweeted_status.user != null) {
				insideContent.setVisibility(View.VISIBLE);

				retweetdetailText.setText(Html.fromHtml(Tools.atBlue("@"
						+ status.retweeted_status.user.name + ":"
						+ status.retweeted_status.text)));
				loadImage(retweetdetailImage,
						status.retweeted_status.thumbnail_pic, false);
			} else {
				insideContent.setVisibility(View.GONE);
			}
			btnRetweet.setText("转发:" + status.reposts_count);
			btnComment.setText("评论:" + status.comments_count);
			btnGood.setText("点赞:" + status.attitudes_count);

		} else {
			moreWeiboListItem.setVisibility(View.VISIBLE);// 显示"更多"
			weiboListItem.setVisibility(View.GONE);
			View moreAnim = convertView.findViewById(R.id.pb_more);
			if (showMoreAnimFlag) {
				moreAnim.setVisibility(View.VISIBLE);
			} else {
				moreAnim.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	@Override
	public void doneProcess(ParentTask task) {
		notifyDataSetChanged(); // 通知更新列表数据
	}
}
