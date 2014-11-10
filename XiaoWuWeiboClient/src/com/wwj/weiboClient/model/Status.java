package com.wwj.weiboClient.model;

import java.io.Serializable;
import java.util.Date;

import com.wwj.weiboClient.interfaces.WeiboObject;
import com.wwj.weiboClient.util.Tools;

import android.text.Html;

/**
 * 【新浪微博实体类】
 * 
 * @author wwj
 * 
 */
@SuppressWarnings("serial")
public class Status implements WeiboObject, Serializable {
	/** 字符串型的微博ID */
	public String idstr;
	/** 创建时间 */
	public String created_at;
	/** 微博ID */
	public long id;
	/** 微博信息内容 */
	public String text;
	/** 微博来源(html形式） */
	public String source;

	/** 是否已收藏 */
	public boolean favorited;
	/** 是否被截断 */
	public boolean truncated;
	/** 回复ID */
	public long in_reply_to_status_id;
	/** 回复人UID */
	public long in_reply_to_user_id;
	/** 回复人昵称 */
	public String in_reply_to_screen_name;
	/** 微博MID */
	public long mid;
	/** 中等尺寸图片地址 */
	public String bmiddle_pic;
	/** 原始图片地址 */
	public String original_pic;
	/** 缩略图片地址 */
	public String thumbnail_pic;
	/** 转发数 */
	public int reposts_count;
	/** 评论数 */
	public int comments_count;
	/** 点赞数 */
	public int attitudes_count;
	/** 转发的微博内容 */
	public Status retweeted_status;
	/** 微博作者的用户信息字段 */
	public User user; // 不要初始化，否则可能会引起递归创建对象，导致stack溢出

	/** 获取Date形式的创建时间 */
	public Date getCreatedAt() {
		return Tools.strToDate(created_at);
	}

	/** 文本形式的source， */
	private String text_source;

	/** 获取文本形式的source */
	public String getTextSource() {
		if (text_source == null) {
			try {
				// 有时返回的来源是null，可能是一个bug，所以必须加上try...catch...
				text_source = Html.fromHtml(source).toString();
			} catch (Exception e) {
				text_source = source;
			}

		}

		return text_source;

	}
}
