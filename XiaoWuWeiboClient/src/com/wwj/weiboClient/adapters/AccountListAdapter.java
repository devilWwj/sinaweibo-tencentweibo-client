package com.wwj.weiboClient.adapters;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.database.UserInfo;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;

/**
 * °æ’ ∫≈¡–±Ì  ≈‰∆˜°ø
 * 
 * @author wwj
 * 
 */
public class AccountListAdapter extends BaseAdapter {

	public List<UserInfo> userInfos;
	private LayoutInflater inflater;
	private AsyncImageLoader asyncImageLoader;

	public AccountListAdapter(Context context, List<UserInfo> userInfos) {
		super();
		this.userInfos = userInfos;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		asyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public int getCount() {
		return userInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return userInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.account_list_item, null);
			holder.userIcon = (ImageView) convertView
					.findViewById(R.id.user_icon);
			holder.userName = (TextView) convertView
					.findViewById(R.id.username);
			holder.userId = (TextView) convertView.findViewById(R.id.user_id);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		UserInfo userInfo = userInfos.get(position);

		Drawable drawable = asyncImageLoader.loadDrawable(userInfo.getUrl(),
				holder.userIcon, new ImageCallback() {

					@Override
					public void imageLoaded(Drawable imageDrawable,
							ImageView imageView, String imageUrl) {
						imageView.setImageDrawable(imageDrawable);
					}
				});

		if (drawable == null) {
			holder.userIcon.setImageDrawable(null);
			holder.userIcon.setImageResource(R.drawable.avatar_default);
		} else {
			holder.userIcon.setImageDrawable(drawable);
		}
		// if (userInfo.getUserIcon() != null) {
		// // holder.userIcon.setImageDrawable(userInfo.getUserIcon());
		//
		// } else {
		// holder.userIcon.setImageResource(R.drawable.avatar_default);
		// }

		holder.userName.setText(userInfo.getUserName());
		holder.userId.setText(userInfo.getUserId());

		return convertView;
	}

	private class ViewHolder {
		private ImageView userIcon;
		private TextView userName;
		private TextView userId;
	}
}
