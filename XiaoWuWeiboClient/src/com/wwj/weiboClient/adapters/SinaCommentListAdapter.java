package com.wwj.weiboClient.adapters;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.Comment;
import com.wwj.weiboClient.util.Tools;

/**
 * 评论列表适配器
 * 
 * @author wwj
 * 
 */
public class SinaCommentListAdapter extends BaseAdapter implements Const {
	private Activity context;
	private LayoutInflater layoutInflater;
	private List<Comment> comments;

	public SinaCommentListAdapter(Activity context, List<Comment> comments) {
		this.context = context;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.comments = comments;
		StorageManager.saveList(comments, MESSAGE_COMMENT);
	}

	@Override
	public int getCount() {
		return comments.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void putComments(List<Comment> comments) {
		if (comments == null || this.comments == null) {
			return;
		}
		if (comments.size() == 0) {
			return;
		}
		if (this.comments.size() == 0) {
			this.comments.addAll(comments);
		} else if (comments
				.get(0)
				.getCreatedAt()
				.before(this.comments.get(this.comments.size() - 1)
						.getCreatedAt())) {
			this.comments.addAll(comments);
		} else if (comments.get(comments.size() - 1).getCreatedAt()
				.after(this.comments.get(0).getCreatedAt())
				&& comments.size() <= DEFAULT_COMMENTS_COUNT) {
			this.comments.addAll(0, comments);
		} else {
			this.comments.clear();
			this.comments.addAll(comments);
		}
		try {
			// 保存评论信息
			StorageManager.saveList(this.comments, PATH_STORAGE,
					Const.MESSAGE_COMMENT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(
					R.layout.comment_list_viewer_item, null);
		}

		TextView name = (TextView) convertView.findViewById(R.id.tv_name);
		TextView createdAt = (TextView) convertView
				.findViewById(R.id.tv_created_at);
		TextView commentText = (TextView) convertView
				.findViewById(R.id.tv_comment_text);
		TextView source = (TextView) convertView.findViewById(R.id.tv_source);
		ImageView profileImage = (ImageView) convertView
				.findViewById(R.id.iv_profile_image);
		profileImage.setImageResource(R.drawable.avatar_default); // 设置默认头像
		Comment comment = comments.get(position);
		name.setText(comment.user.name);
		createdAt.setText(comment.getFormatCreatedAt());
		if (comment.user.profile_image_url != null) {
			loadImage(profileImage, comment.user.profile_image_url, false);
		}
		commentText.setText(Tools.changeTextToFace(context,
				Html.fromHtml(Tools.atBlue(comment.text))));
		source.setText("来自  " + comment.getTextSource());

		return convertView;
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

}
