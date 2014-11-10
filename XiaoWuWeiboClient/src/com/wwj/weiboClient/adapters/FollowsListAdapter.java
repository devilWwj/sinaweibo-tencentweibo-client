package com.wwj.weiboClient.adapters;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.model.User;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;

/**
 * 【粉丝列表适配器】
 * 
 * @author wwj
 * 
 */
public class FollowsListAdapter extends BaseAdapter implements Const {
	private Activity context;
	private List<JSONObject> tencentUsers;
	private List<User> sinaUsers;
	private int weiboType;
	private LayoutInflater layoutInflater;
	private AsyncImageLoader asyncImageLoader;

	public FollowsListAdapter(Activity context, List<JSONObject> tencentUsers,
			List<User> sinaUsers, int weiboType) {
		super();
		this.context = context;
		this.tencentUsers = tencentUsers;
		this.sinaUsers = sinaUsers;
		this.weiboType = weiboType;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		asyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public int getCount() {
		int count = 0;
		switch (weiboType) {
		case SINA:
			count = sinaUsers.size();
			break;
		case TENCENT:
			count = tencentUsers.size();
			break;
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return (weiboType == SINA) ? sinaUsers.get(position) : tencentUsers
				.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = new ViewHolder();
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.friends_item, null);
			viewHolder.profileImage = (ImageView) convertView
					.findViewById(R.id.iv_profile_image);
			viewHolder.username = (TextView) convertView
					.findViewById(R.id.tv_name);
			viewHolder.description = (TextView) convertView
					.findViewById(R.id.tv_description_text);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		switch (weiboType) {
		case SINA:
			User user = sinaUsers.get(position);
			viewHolder.username.setText(user.name);
			viewHolder.description.setText(user.description);
			if (user.profile_image_url != null) {
				loadImage(viewHolder.profileImage, user.profile_image_url,
						false);
			}
			break;
		case TENCENT:
			JSONObject userObj = tencentUsers.get(position);
			try {
				viewHolder.username.setText(userObj.getString("nick"));
				viewHolder.description.setText(userObj.getString("location"));
				Drawable drawable = asyncImageLoader.loadDrawable(
						userObj.getString("head") + "/100",
						viewHolder.profileImage, new ImageCallback() {

							@Override
							public void imageLoaded(Drawable imageDrawable,
									ImageView imageView, String imageUrl) {
								imageView.setImageDrawable(imageDrawable);
							}
						});
				if (drawable == null) {
					viewHolder.profileImage
							.setImageResource(R.drawable.avatar_default);
				} else {
					viewHolder.profileImage.setImageDrawable(drawable);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			break;
		}
		return convertView;
	}

	private class ViewHolder {
		private ImageView profileImage;
		private TextView username;
		private TextView description;
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
