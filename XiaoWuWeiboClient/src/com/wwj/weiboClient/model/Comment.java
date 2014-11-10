package com.wwj.weiboClient.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wwj.weiboClient.interfaces.WeiboObject;
import com.wwj.weiboClient.util.Tools;

import android.text.Html;

public class Comment implements WeiboObject, Serializable {
	/** 评论创建时间 */
	public String created_at;
	/** 评论的ID */
	public long id;
	/** 评论的内容 */
	public String text;
	/** 评论的来源 */
	public String source;
	/** 评论的MID */
	public long mid;
	/** 评论作者的用户信息字段 */
	public User user;
	/** 评论的微博信息字段 */
	public Status status;

	/** 获取Date形式的创建时间 */
	public Date getCreatedAt() {
		return Tools.strToDate(created_at);
	}

	public String getFormatCreatedAt() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
		return sdf.format(getCreatedAt());
	}

	/** 文本形式的source */
	private String text_source;

	/** 获取文本形式的source */
	public String getTextSource() {
		if (text_source == null) {
			text_source = Html.fromHtml(source).toString();
		}
		return text_source;

	}
}
