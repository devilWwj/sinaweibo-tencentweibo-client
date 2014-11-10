package com.wwj.weiboClient.adapters;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.TimeUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;

/**
 * 评论列表适配器
 * 
 * @author wwj
 * 
 */
public class TencentCommentListAdapter extends BaseAdapter implements Const {
	private Context context;
	private LayoutInflater layoutInflater;
	private List<JSONObject> comments;
	private AsyncImageLoader asyncImageLoader;

	public TencentCommentListAdapter(Context context, List<JSONObject> comments) {
		this.context = context;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.comments = comments;
		asyncImageLoader = new AsyncImageLoader();
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
		ImageView headIcon = (ImageView) convertView.findViewById(R.id.iv_profile_image);
		try {
			JSONObject comment = (JSONObject) comments.get(position);
			// 异步加载图片
			Drawable cachedImage = asyncImageLoader.loadDrawable(
					comment.getString("head") + "/100",
					headIcon, new ImageCallback() {

						@Override
						public void imageLoaded(Drawable imageDrawable,
								ImageView imageView, String imageUrl) {
							imageView.setImageDrawable(imageDrawable);
						}

					});
			if (cachedImage == null) {
				headIcon
						.setImageResource(R.drawable.avatar_default);
			} else {
				headIcon.setImageDrawable(cachedImage);
			}
			name.setText(comment.getString("nick"));
			createdAt.setText(TimeUtil.getStandardTime(comment
					.getLong("timestamp")));
			commentText
					.setText(Tools.changeTextToFace(context, Html
							.fromHtml(Tools.atBlue(comment
									.getString("origtext")))));
			source.setText("来自  " + comment.getString("from"));
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return convertView;
	}
}
