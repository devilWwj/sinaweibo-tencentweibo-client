package com.wwj.weiboClient.model;

import java.io.Serializable;

import com.wwj.weiboClient.interfaces.WeiboObject;



public class User implements WeiboObject, Serializable {
	/** 用户UID */
	public long id;
	/** 用户昵称 */
	public String screen_name;
	/** 友好显示名称 */
	public String name;
	/** 用户所在地区ID */
	public int province;
	/** 用户所在城市ID */
	public int city;
	/** 用户所在地 */
	public String location;
	/** 用户描述 */
	public String description;
	/** 用户博客地址 */
	public String url;
	/** 用户头像地址 */
	public String profile_image_url;
	/** 用户的个性化域名 */
	public String domain;
	/** 性别，m：男、f：女、n：未知 */
	public String gender;
	/** 粉丝数 */
	public String followers_count;
	/** 关注数 */
	public int friends_count;
	/** 微博数 */
	public int statuses_count;
	/** 收藏数 */
	public int favourites_count;
	/** 创建时间 */
	public String created_at;
	/** 当前登录用户是否已关注该用户 */
	public boolean following;
	/** 是否允许所有人给我发私信 */
	public boolean allow_all_act_msg;
	/** 是否允许带有地理信息 */
	public boolean geo_enabled;
	/** 是否是微博认证用户，即带V用户 */
	public boolean verified;
	/**
	 * 认证类型<br>
	 * <br>
	 * <b>－1：</b>普通用户<br>
	 * <br>
	 * 
	 * <b>0：</b>认证个人用户<br>
	 * <br>
	 * 
	 * <b>5：</b>认证企业用户<br>
	 * <br>
	 * 
	 * <b>220：</b>微博达人<br>
	 * <br>
	 * 
	 * 
	 */
	public int verified_type;

	/** 是否允许所有人对我的微博进行评论 */
	public boolean allow_all_comment;
	/** 用户大头像地址 */
	public String avatar_large;
	/** 认证原因 */
	public String verified_reason;
	/** 该用户是否关注当前登录用户 */
	public boolean follow_me;
	/** 用户的在线状态，0：不在线、1：在线 */
	public boolean online_status;
	/** 用户的互粉数 */
	public int bi_followers_count;
	/** 用户的最近一条微博信息字段 */
	public Status status; // 不要初始化，否则可能会引起递归创建对象，导致stack溢出
	
	public String getGender(String gender) {
		String genderStr = "";
		if (gender.equals("m")) {
			genderStr = "男";
		} else if (gender.equals("f")) {
			genderStr = "女";
		} else if (gender.equals("n")) {
			genderStr = "未知";
		}
		return genderStr;
	}

}
