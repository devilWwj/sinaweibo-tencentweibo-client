package com.wwj.weiboClient.interfaces;

/**
 * �����ӿ�
 * 
 * @description ���峣�õ�һЩ����
 * @author wwj
 * 
 */
public interface Const {
	// ΢����Ŀ¼
	String ROOT_PATH = android.os.Environment.getExternalStorageDirectory()
			.toString() + "/~~weibo~~";
	// ΢��ͼƬ����Ŀ¼
	String PATH_FILE_CACHE = ROOT_PATH + "/cache";
	// ΢���洢Ŀ¼
	String PATH_STORAGE = ROOT_PATH + "/storage";
	// ΢��ͼƬ�洢Ŀ¼
	String PATH_IMAGE = ROOT_PATH + "/images";
	// ������
	String CONFIG_NAME = "config";

	// ͼ�����ֵ
	int BITMAP_MAX_SIZE = 307200; // 300K

	int HOME = 101;
	int MESSAGE_AT = 120;
	int MESSAGE_COMMENT = 121;
	int MESSAGE_FAVORITE = 122;
	int MESSAGE_GOOD = 123;
	int MESSAGE_BOX = 124;
	int USER_TIMELINE = 125;
	int SELFINFO = 130;
	int SQUARE = 140;
	int MORE = 150;
	int WEIBO_LIST = 151;
	int HOT_STATUSES = 152;
	int HOT_FAVORITIES = 153;
	int SEARCH_APPS = 154;
	int SEARCH_USERS = 155;

	// �����ύ΢�������ͼ�����
	int PICTURE_VIEWER_POST_WEIBO = 501;
	// ����΢����������ͼ�����
	int PICTURE_VIEWER_WEIBO_BROWSER = 502;
	// �ύ΢��
	int TYPE_POST_WEIBO = 601;
	// ת��΢��
	int TYPE_FORWARD = 602;
	// ����΢��
	int TYPE_COMMENT = 603;
	// �������
	int TYPE_FEEDBACK = 604;

	int TYPE_HOME_TIMELINE = 1001;

	int DEFAULT_STATUS_COUNT = 30;
	int DEFAULT_COMMENTS_COUNT = 31;
	int DEFAULT_STATUS_PAGE = 1;

	int MONITOR_TYPE_IMAGE = 1501;
	int MONITOR_TYPE_TASK = 1502;

	int CODE_REQUEST_CAPTURE_IMAGE = 2001;
	int CODE_REQUEST_PICTURE_VIEWER = 2002;
	int CODE_RESULT_RETURN = 2101;
	int CODE_RESULT_REMOVE = 2102;
	int CODE_RESULT_SAVE = 2103;
	int CODE_RESULT_CANCEL = 2104;
	int CODE_PHOTO_LIBRARY = 2105;

	int HANDLER_TYPE_LOAD_PROFILE_IMAGE = 3001;
	
	
	// ΢�����ͣ����ˡ���Ѷ��
	int SINA = 0;
	int TENCENT = 1;
	
	int ACCOUNT_RESULT_CODE = 1;
}
