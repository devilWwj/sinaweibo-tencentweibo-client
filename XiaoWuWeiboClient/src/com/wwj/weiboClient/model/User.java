package com.wwj.weiboClient.model;

import java.io.Serializable;

import com.wwj.weiboClient.interfaces.WeiboObject;



public class User implements WeiboObject, Serializable {
	/** �û�UID */
	public long id;
	/** �û��ǳ� */
	public String screen_name;
	/** �Ѻ���ʾ���� */
	public String name;
	/** �û����ڵ���ID */
	public int province;
	/** �û����ڳ���ID */
	public int city;
	/** �û����ڵ� */
	public String location;
	/** �û����� */
	public String description;
	/** �û����͵�ַ */
	public String url;
	/** �û�ͷ���ַ */
	public String profile_image_url;
	/** �û��ĸ��Ի����� */
	public String domain;
	/** �Ա�m���С�f��Ů��n��δ֪ */
	public String gender;
	/** ��˿�� */
	public String followers_count;
	/** ��ע�� */
	public int friends_count;
	/** ΢���� */
	public int statuses_count;
	/** �ղ��� */
	public int favourites_count;
	/** ����ʱ�� */
	public String created_at;
	/** ��ǰ��¼�û��Ƿ��ѹ�ע���û� */
	public boolean following;
	/** �Ƿ����������˸��ҷ�˽�� */
	public boolean allow_all_act_msg;
	/** �Ƿ�������е�����Ϣ */
	public boolean geo_enabled;
	/** �Ƿ���΢����֤�û�������V�û� */
	public boolean verified;
	/**
	 * ��֤����<br>
	 * <br>
	 * <b>��1��</b>��ͨ�û�<br>
	 * <br>
	 * 
	 * <b>0��</b>��֤�����û�<br>
	 * <br>
	 * 
	 * <b>5��</b>��֤��ҵ�û�<br>
	 * <br>
	 * 
	 * <b>220��</b>΢������<br>
	 * <br>
	 * 
	 * 
	 */
	public int verified_type;

	/** �Ƿ����������˶��ҵ�΢���������� */
	public boolean allow_all_comment;
	/** �û���ͷ���ַ */
	public String avatar_large;
	/** ��֤ԭ�� */
	public String verified_reason;
	/** ���û��Ƿ��ע��ǰ��¼�û� */
	public boolean follow_me;
	/** �û�������״̬��0�������ߡ�1������ */
	public boolean online_status;
	/** �û��Ļ����� */
	public int bi_followers_count;
	/** �û������һ��΢����Ϣ�ֶ� */
	public Status status; // ��Ҫ��ʼ����������ܻ�����ݹ鴴�����󣬵���stack���
	
	public String getGender(String gender) {
		String genderStr = "";
		if (gender.equals("m")) {
			genderStr = "��";
		} else if (gender.equals("f")) {
			genderStr = "Ů";
		} else if (gender.equals("n")) {
			genderStr = "δ֪";
		}
		return genderStr;
	}

}
