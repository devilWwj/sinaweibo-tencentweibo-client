package com.wwj.weiboClient.library;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.wwj.weiboClient.interfaces.WeiboObject;

/**
 * JSON�ַ����Ͷ����ת���ࣨ��Ҫ)
 *@author Lining
 *
 */
public class JSONAndObject {
	/**
	 * ��һ������ת��ΪJSON��ʽ���ַ�����ֻת��public���͵ı���
	 * 
	 * @param obj
	 * @return
	 */
	public static String convertSingleObjectToJson(Object obj) {
		String json = null;
		if (obj == null) {
			return json;
		}
		Field[] fields = obj.getClass().getFields();
		json = "{";
		// ��ʼת��ÿһ��public���͵ı���
		for (int i = 0; i < fields.length; i++) {
			try {
				Field field = fields[i];
				if (field.getType() == String.class) {
					// ����ֵΪnull, �ÿ��ַ���ȡ��
					String temp = ((field.get(obj) == null) ? "" : String
							.valueOf(field.get(obj)));
					// �����ַ����е�˫����
					// JSON�ַ����в���ֱ��ʹ��˫����
					temp = temp.replaceAll("\"", "\\\\\"");
					json += "\"" + field.getName() + "\":\"" + temp + "\"";
				}
				// long����
				else if (field.getType() == long.class) {
					json += "\"" + field.getName() + "\":" + field.getLong(obj);
				}
				// int����
				else if (field.getType() == int.class) {
					json += "\"" + field.getName() + "\":" + field.getInt(obj);
				}
				// boolean����
				else if (field.getType() == boolean.class) {
					json += "\"" + field.getName() + "\":"
							+ field.getBoolean(obj);
				}
				// Object���ͣ�WeiboObject����)
				else {
					Object fieldObject = field.get(obj);
					if (fieldObject instanceof WeiboObject) {
						// ��������к��ж������͵ı���
						// �ݹ�����JSON�ַ���
						json += "\"" + field.getName() + "\":"
								+ convertSingleObjectToJson(fieldObject);
					} else {
						continue;
					}
				}
				if (i < fields.length - 1) {
					json += ",";
				}
			} catch (Exception e) {

			}
		}
		json += "}";
		return json;
	}

	/**
	 * ��objת��ΪJSON�ַ��������ַ���������һ������ ����obj������һ��List������JSON�ַ����������һ��propertyName
	 * �ƶ������ԣ�����ֵ��JSON���飬��������objָ����List��Ӧ ������hometimeline.json���ص�JSON�ַ����������
	 * 
	 * @param obj
	 * @param propertyName
	 * @return
	 */
	public static String convertObjectToJson(Object obj, String propertyName) {
		String json = null;
		if (obj == null) {
			return json;
		}
		if (obj instanceof List) {
			List list = (List) obj;
			if (propertyName != null) {
				// ����һ�����ԵĶ�����������Ƕ�������
				json = "{\"" + propertyName + "\":[";
			} else {
				// ��������
				json = "[";
			}
			for (int i = 0; i < list.size(); i++) {
				Object item = list.get(i);
				json += convertSingleObjectToJson(item);
				if (i < list.size() - 1)
					json += ",";
			}
			if (propertyName != null) {
				json += "]}";
			} else {
				json = "]";
			}
		}
		return json;
	}

	/**
	 * ��json�ַ���ת��ΪList
	 * 
	 * @param c
	 * @param json
	 * @param propertyName
	 *            ������������ƶ����ԵĶ��󣬶����������ֵ������һ������
	 * @return
	 */
	public static List convert(Class c, String json, String propertyName) {
		List objs = null;

		if (c == null || json == null)
			return objs;
		try {

			// ֻʹ��public�����ֶ�
			Field[] fields = c.getFields();
			if (fields != null) {

				String jsonStr = json;
				if (propertyName != null) {
					JSONObject jsonObject = new JSONObject(json);
					jsonStr = jsonObject.get(propertyName).toString();
				}

				JSONArray jsonArray = new JSONArray(jsonStr);
				objs = new ArrayList();

				for (int i = 0; i < jsonArray.length(); i++) {
					Object obj = c.newInstance();
					objs.add(obj);
					convertSingleObject(obj, jsonArray.getString(i));
				}

			}
		} catch (Exception e) {
			Log.d("convert", e.getMessage());
		}
		return objs;

	}

	/**
	 * ʹ�ø÷�����Ҫ�ȴ���һ��object,�����һ������ ��JSON��ʽ������ת��Ϊһ������ json������ֵ������һ��JSON��ʽ�Ķ��󣬲���������
	 * 
	 * @param obj
	 * @param json
	 * @return
	 */
	public static Object convertSingleObject(Object obj, String json) {
		if (obj == null || json == null)
			return obj;
		try {
			// ֻʹ��public�����ֶ�
			Field[] fields = obj.getClass().getFields();
			if (fields != null) {
				JSONObject jsonObject = new JSONObject(json);
				for (Field field : fields) {
					try {
						Object objValue = jsonObject.get(field.getName());
						// �ַ�������
						if (field.getType() == String.class) {
							field.set(obj, String.valueOf(objValue));
						}
						// long����
						else if (field.getType() == long.class) {
							field.set(obj,
									Long.valueOf(String.valueOf(objValue)));
						} // int����
						else if (field.getType() == int.class) {
							field.set(obj,
									Integer.valueOf(String.valueOf(objValue)));
						}
						// boolean����
						else if (field.getType() == boolean.class) {
							field.set(obj, Boolean.getBoolean(String
									.valueOf(objValue)));
						}
						// Object����(WeiboObject���ͣ�
						else {
							Object fieldObject = field.getType().newInstance();
							if (fieldObject instanceof WeiboObject) {
								convertSingleObject(fieldObject,
										String.valueOf(objValue));
								field.set(obj, fieldObject);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;

	}
}
