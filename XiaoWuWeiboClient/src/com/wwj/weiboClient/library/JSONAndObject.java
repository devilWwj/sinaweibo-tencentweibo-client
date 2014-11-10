package com.wwj.weiboClient.library;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.wwj.weiboClient.interfaces.WeiboObject;

/**
 * JSON字符串和对象的转换类（重要)
 *@author Lining
 *
 */
public class JSONAndObject {
	/**
	 * 将一个对象转换为JSON格式的字符串，只转换public类型的变量
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
		// 开始转换每一个public类型的变量
		for (int i = 0; i < fields.length; i++) {
			try {
				Field field = fields[i];
				if (field.getType() == String.class) {
					// 属性值为null, 用空字符串取代
					String temp = ((field.get(obj) == null) ? "" : String
							.valueOf(field.get(obj)));
					// 处理字符串中的双引号
					// JSON字符串中不能直接使用双引号
					temp = temp.replaceAll("\"", "\\\\\"");
					json += "\"" + field.getName() + "\":\"" + temp + "\"";
				}
				// long类型
				else if (field.getType() == long.class) {
					json += "\"" + field.getName() + "\":" + field.getLong(obj);
				}
				// int类型
				else if (field.getType() == int.class) {
					json += "\"" + field.getName() + "\":" + field.getInt(obj);
				}
				// boolean类型
				else if (field.getType() == boolean.class) {
					json += "\"" + field.getName() + "\":"
							+ field.getBoolean(obj);
				}
				// Object类型（WeiboObject类型)
				else {
					Object fieldObject = field.get(obj);
					if (fieldObject instanceof WeiboObject) {
						// 如果对象中含有对象类型的变量
						// 递归生成JSON字符串
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
	 * 将obj转换为JSON字符串，该字符串必须是一个对象 其中obj必须是一个List，而且JSON字符串必须包含一个propertyName
	 * 制定的属性，属性值是JSON数组，该数组与obj指定的List对应 类似于hometimeline.json返回的JSON字符串的逆过程
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
				// 包含一个属性的对象，这个属性是对象数组
				json = "{\"" + propertyName + "\":[";
			} else {
				// 对象数组
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
	 * 将json字符串转换为List
	 * 
	 * @param c
	 * @param json
	 * @param propertyName
	 *            这个参数用来制定属性的对象，而且这个属性值必须是一个数组
	 * @return
	 */
	public static List convert(Class c, String json, String propertyName) {
		List objs = null;

		if (c == null || json == null)
			return objs;
		try {

			// 只使用public类型字段
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
	 * 使用该方法需要先创建一个object,传入第一个参数 将JSON格式的数据转换为一个对象 json参数的值必须是一个JSON格式的对象，不能是数组
	 * 
	 * @param obj
	 * @param json
	 * @return
	 */
	public static Object convertSingleObject(Object obj, String json) {
		if (obj == null || json == null)
			return obj;
		try {
			// 只使用public类型字段
			Field[] fields = obj.getClass().getFields();
			if (fields != null) {
				JSONObject jsonObject = new JSONObject(json);
				for (Field field : fields) {
					try {
						Object objValue = jsonObject.get(field.getName());
						// 字符串类型
						if (field.getType() == String.class) {
							field.set(obj, String.valueOf(objValue));
						}
						// long类型
						else if (field.getType() == long.class) {
							field.set(obj,
									Long.valueOf(String.valueOf(objValue)));
						} // int类型
						else if (field.getType() == int.class) {
							field.set(obj,
									Integer.valueOf(String.valueOf(objValue)));
						}
						// boolean类型
						else if (field.getType() == boolean.class) {
							field.set(obj, Boolean.getBoolean(String
									.valueOf(objValue)));
						}
						// Object类型(WeiboObject类型）
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
