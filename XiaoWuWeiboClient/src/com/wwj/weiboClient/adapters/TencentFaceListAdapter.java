package com.wwj.weiboClient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.library.TencentFaceMan;

/**
 * 腾讯表情适配器
 * 
 * @author wwj
 * 
 */
public class TencentFaceListAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater;

	public TencentFaceListAdapter(Context context) {
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return TencentFaceMan.getCount();
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
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = layoutInflater.inflate(R.layout.face, null);
			holder.faceView = (ImageView) convertView
					.findViewById(R.id.faceView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		// ImageView imageView = (ImageView) convertView;

		// 显示表情
		holder.faceView.setImageResource(TencentFaceMan
				.getFaceResourceId(position));
		return convertView;
	}

	private class ViewHolder {
		private ImageView faceView;
	}

}
