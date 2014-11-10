package com.wwj.weiboClient.net;

/**
 * HTTP工具类 定义相关常量参数
 * 
 * @author wwj
 * 
 */
public class HttpUtils {

	/* HTTP Request Method */
	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";

	/* HTTP Request Property */
	public static final String PROPERTY_KEY_01 = "Accept";
	public static final String PROPERTY_KEY_02 = "Accept-Language";
	public static final String PROPERTY_KEY_03 = "Charset";
	public static final String PROPERTY_KEY_04 = "User-Agent";
	public static final String PROPERTY_KEY_05 = "Connection";

	public static final String PROPERTY_VALUE_01_A = "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*";
	public static final String PROPERTY_VALUE_02_A = "zh-CN";
	public static final String PROPERTY_VALUE_03_A = "UTF-8";
	public static final String PROPERTY_VALUE_03_B = "GBK";
	public static final String PROPERTY_VALUE_03_C = "gb2312";
	public static final String PROPERTY_VALUE_04_A = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/533.4 (KHTML, like Gecko) Chrome/5.0.375.126 Safari/533.4 ChromePlus/1.4.1.0)";
	public static final String PROPERTY_VALUE_05_A = "Keep-Alive";

	/* File Operation */
	public static final int SIZE_READ_BUFFER = 1024;

}
