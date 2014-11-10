package com.wwj.weiboClient.adapters;

import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;
import com.wwj.weiboClient.util.RegexUtil;
import com.wwj.weiboClient.util.TextUtil;
import com.wwj.weiboClient.util.TimeUtil;

public class TencentWeiboListAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater inflater;
	private List<JSONObject> list;
	private AsyncImageLoader asyncImageLoader;

	public TencentWeiboListAdapter(Context context, List<JSONObject> list) {
		super();
		this.context = context;
		this.list = list;
		this.inflater = LayoutInflater.from(context);
		asyncImageLoader = new AsyncImageLoader();
	}

	@Override
	public int getCount() {
		// 加1为了最后一项显示”更多“
		return list.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
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

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = new ViewHolder();

		convertView = inflater.inflate(R.layout.tencent_weibo_list_item, null);
		View weiboListItem = convertView.findViewById(R.id.ll_weibo_list_item);
		View moreWeiboListItem = convertView
				.findViewById(R.id.rl_more_weibo_list_item);

		viewHolder.home_headicon = (ImageView) convertView
				.findViewById(R.id.home_headicon);
		viewHolder.home_nick = (TextView) convertView
				.findViewById(R.id.home_nick);
		viewHolder.home_vip = (ImageView) convertView
				.findViewById(R.id.home_vip);
		viewHolder.home_hasimage = (ImageView) convertView
				.findViewById(R.id.home_hasimage);
		viewHolder.home_timestamp = (TextView) convertView
				.findViewById(R.id.home_timestamp);
		viewHolder.home_origtext = (TextView) convertView
				.findViewById(R.id.home_origtext);
		viewHolder.home_source = (TextView) convertView
				.findViewById(R.id.home_source);
		viewHolder.status_pic = (ImageView) convertView
				.findViewById(R.id.iv_status_image);
		if (position < list.size()) {
			weiboListItem.setVisibility(View.VISIBLE);
			moreWeiboListItem.setVisibility(View.GONE);
			JSONObject data = (JSONObject) list.get(position);
			JSONObject source = null;
			if (data != null) {
				try {
					convertView.setTag(data.get("id"));
					viewHolder.home_nick.setText(data.getString("nick")); // 昵称
					if (data.getInt("isvip") != -1) {
						viewHolder.home_vip.setVisibility(View.GONE); // 非vip隐藏vip标志
					}
					viewHolder.home_timestamp.setText(TimeUtil.covertTime(Long
							.parseLong(data.getString("timestamp"))));

					// 异步加载图片
					Drawable cachedImage = asyncImageLoader.loadDrawable(
							data.getString("head") + "/100",
							viewHolder.home_headicon, new ImageCallback() {

								@Override
								public void imageLoaded(Drawable imageDrawable,
										ImageView imageView, String imageUrl) {
									imageView.setImageDrawable(imageDrawable);
								}

							});
					if (cachedImage == null) {
						viewHolder.home_headicon
								.setImageResource(R.drawable.avatar_default);
					} else {
						viewHolder.home_headicon.setImageDrawable(cachedImage);
					}

					if (!"null".equals(data.getString("image"))) {
						viewHolder.home_hasimage
								.setImageResource(R.drawable.hasimage);
						viewHolder.status_pic.setVisibility(View.VISIBLE);
						JSONArray imageArray = data.optJSONArray("image"); // 如果此微博有图片内容，就显示出来
						if (imageArray != null && imageArray.length() > 0) {
							String imageUrl = imageArray.optString(0) + "/460"; 
							Drawable drawable = asyncImageLoader.loadDrawable(imageUrl, viewHolder.status_pic, new ImageCallback() {
								
								@Override
								public void imageLoaded(Drawable imageDrawable, ImageView imageView,
										String imageUrl) {
									imageView.setImageDrawable(imageDrawable);
								}
							});
							if (drawable == null) {
								viewHolder.status_pic.setImageResource(R.drawable.wb_album_pic_default);
								
							} else {
								viewHolder.status_pic.setImageDrawable(drawable);
							}
						}
							
					}

					// 微博内容开始
					String origtext = data.getString("origtext");

					SpannableString spannableString = new SpannableString(
							origtext);

					viewHolder.home_origtext.setText(spannableString);
					// ----微博内容结束

					// 处理引用的转播,评论的微博内容
					try {
						if (!"null".equals(data.getString("source"))) {
							source = data.getJSONObject("source");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					if (source != null) {
						String home_source_text = null;
						boolean isvip = source.getInt("isvip") == 1 ? true
								: false;
						if (isvip) {
							home_source_text = "@" + source.getString("nick")
									+ "======:" + source.getString("origtext");// 用6个连续的=号来替换vip标志图标
						} else {
							home_source_text = "@" + source.getString("nick")
									+ ":" + source.getString("origtext");
						}
						SpannableString spannableSource = new SpannableString(
								home_source_text);
						spannableSource = TextUtil.decorateFaceInStr(
								spannableSource, RegexUtil.getStartAndEndIndex(
										home_source_text,
										Pattern.compile("@.*:")), context
										.getResources());
						spannableSource = TextUtil.decorateTopicInStr(
								spannableSource, RegexUtil.getStartAndEndIndex(
										home_source_text,
										Pattern.compile("#.*#")), context
										.getResources());
						spannableSource = TextUtil
								.decorateTopicInStr(
										spannableSource,
										RegexUtil.getStartAndEndIndex(
												home_source_text,
												Pattern.compile("^http://\\w+(\\.\\w+|)+(/\\w+)*(/\\w+\\.(\\w+|))?")),
										context.getResources());

						if (isvip) {
							spannableSource = TextUtil.decorateVipInStr(
									spannableSource, RegexUtil
											.getStartAndEndIndex(
													home_source_text,
													Pattern.compile("======")),
									context.getResources()); // 替换为vip认证图片

							viewHolder.home_timestamp.setText(spannableSource);
							viewHolder.home_source
									.setBackgroundResource(R.drawable.home_source_bg);
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			moreWeiboListItem.setVisibility(View.VISIBLE); // 显示“更多”
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

	private class ViewHolder {
		private ImageView home_headicon;
		private TextView home_nick;
		private ImageView home_vip;
		private TextView home_timestamp;
		private TextView home_origtext;
		private TextView home_source;
		private ImageView home_hasimage;
		private ImageView status_pic;
	}
}
