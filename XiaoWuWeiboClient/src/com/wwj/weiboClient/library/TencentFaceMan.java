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
public class TencentFaceMan {
	// 表情
	private static String[] tencentFaces = { "[微笑]:001", "[撇嘴]:002", "[色]:003",
			"[发呆]:004", "[得意]:005", "[流泪]:006", "[害羞]:007", "[闭嘴]:008",
			"[睡]:009", "[大哭]:010", "[尴尬]:011", "[发怒]:012", "[调皮]:013",
			"[龇牙]:014", "[惊讶]:015", "[难过]:016", "[酷]:017", "[冷汗]:018",
			"[抓狂]:019", "[吐]:020", "[偷笑]:021", "[可爱]:022", "[白眼]:023",
			"[拽]:024", "[饥饿]:025", "[困]:026", "[惊恐]:027", "[流汗]:028",
			"[憨笑]:029", "[大兵]:030", "[奋斗]:031", "[咒骂]:032", "[疑问]:033",
			"[嘘...]:034", "[晕]:035", "[折磨]:036", "[衰]:037", "[骷髅]:038",
			"[敲打]:039", "[再见]:040", "[擦汗]:041", "[抠鼻]:042", "[鼓掌]:043",
			"[糗大了]:044", "[坏笑]:045", "[左哼哼]:046", "[右哼哼]:047", "[哈欠]:048",
			"[鄙视]:049", "[委屈]:050", "[快哭了]:051", "[阴险]:052", "[亲亲]:053",
			"[吓]:054", "[可怜]:055", "[菜刀]:056", "[西瓜]:057", "[啤酒]:058",
			"[篮球]:059", "[乒乓]:060", "[咖啡]:061", "[饭]:062", "[猪头]:063",
			"[玫瑰]:064", "[凋谢]:065", "[示爱]:066", "[可爱]:067", "[爱心]:068",
			"[心碎]:069", "[蛋糕]:070", "[闪电]:071", "[炸弹]:072", "[刀]:073",
			"[足球]:074", "[瓢虫]:075", "[便便]:076", "[月亮]:077", "[太阳]:078",
			"[礼物]:079", "[拥抱]:080", "[强]:081", "[弱]:082", "[握手]:083",
			"[胜利]:084", "[抱拳]:085", "[勾引]:086", "[拳头]:087", "[差劲]:088",
			"[爱你]:089", "[NO]:090", "[ok]:091", "[爱情]:092", "[飞吻]:093",
			"[跳跳]:094", "[发抖]:095", "[怄火]:096", "[转圈]:097", "[磕头]:098",
			"[跳绳]:099", "[挥手]:100", "[激动]:101", "[街舞]:102", "[献吻]:103",
			"[左太极]:104", "[右太极]:105" };

	private static Map<String, Integer> faceMap = new HashMap<String, Integer>();
	static {
		for (int i = 0; i < getCount(); i++) {
			faceMap.put(getFaceText(i), getFaceResourceId(i));
		}
	}

	public static int getCount() {
		return tencentFaces.length;
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
		String text = tencentFaces[index];
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
		// 比如h001
		String faceName = "h"
				+ tencentFaces[index].substring(tencentFaces[index]
						.indexOf(":") + 1);
		try {
			Field field = R.drawable.class.getField(faceName);
			resourceId = Integer.parseInt(String.valueOf(field.get(null)));
		} catch (Exception e) {
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
