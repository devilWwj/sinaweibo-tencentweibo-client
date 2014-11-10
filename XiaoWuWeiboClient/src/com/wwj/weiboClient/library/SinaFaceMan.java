package com.wwj.weiboClient.library;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.wwj.weiboClient.R;

/**
 * 【腾讯微博表情类】
 * 
 * @author wwj
 * 
 */
public class SinaFaceMan {
	// 表情
	private static String[] sinaFaces = { "[爱你]:aini", "[奥特曼]:aoteman",
			"[拜拜]:baibai", "[悲伤]:beishang", "[鄙视]:bishi", "[闭嘴]:bizui",
			"[馋嘴]:chanzui", "[吃惊]:chijing", "[打哈欠]:dahaqi", "[顶]:ding",
			"[肥皂]:feizao", "[愤怒]:fennu", "[感冒]:ganmao", "[鼓掌]:guzhang",
			"[哈哈]:haha", "[害羞]:haixiu", "[汗]:han", "[呵呵]:hehe", "[黑线]:heixian",
			"[哼]:heng", "[花心]:huaxin,[可爱]:keai", "[可怜]:kelian", "[哭]:ku",
			"[困]:kun", "[懒得理你]:landelini", "[累]:lei", "[旅行]:lvxing",
			"[男孩儿]:nanhaier", "[怒]:nu", "[怒骂]:numa", "[女孩儿]:nuhaier",
			"[钱]:qian", "[亲亲]:qinqin", "[生病]:shengbing", "[失望]:shiwang",
			"[衰]:shuai", "[书呆子]:shudaizi", "[睡觉]:shuijiao", "[思考]:sikao",
			"[太开心]:taikaixin", "[偷笑]:touxiao", "[吐]:tu", "[兔子]:tuzi",
			"[挖鼻屎]:wabishi", "[委屈]:weiqu", "[熊猫]:xiongmao", "[嘻嘻]:xixi",
			"[嘘]:xu", "[阴险]:yinxian", "[疑问]:yiwen", "[右哼哼]:youhengheng",
			"[晕]:yun", "[抓狂]:zhuakuang", "[猪头]:zhutou", "[做鬼脸]:zuoguilian",
			"[左哼哼]:zuohengheng" };

	private static Map<String, Integer> faceMap = new HashMap<String, Integer>();
	static {
		for (int i = 0; i < getCount(); i++) {
			faceMap.put(getFaceText(i), getFaceResourceId(i));
		}
	}

	public static int getCount() {
		return sinaFaces.length;
	}

	/**
	 * 得到表情文字
	 * 
	 * @param index
	 * @return
	 */
	public static String getFaceText(int index) {
		if (index >= getCount())
			return "";
		String text = sinaFaces[index];
		// 取得文本
		text = text.substring(0, text.indexOf(":"));
		return text;

	}

	/**
	 * 取得标签资源Id
	 * 
	 * @param index
	 * @return
	 */
	public static int getFaceResourceId(int index) {
		if (index >= getCount())
			return 0;
		int resourceId = 0;
		// 比如d_tu
		String faceName = "d_"
				+ sinaFaces[index].substring(sinaFaces[index].indexOf(":") + 1);
		try {
			Field field = R.drawable.class.getField(faceName);
			resourceId = Integer.parseInt(String.valueOf(field.get(null)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;

	}

	/**
	 * 根据文本取得表情资源id
	 * 
	 * @param faceText
	 * @return
	 */
	public static int getResourceId(String faceText) {
		Object obj = faceMap.get(faceText);
		if (obj == null)
			return 0;
		return Integer.parseInt(String.valueOf(obj));

	}
}
