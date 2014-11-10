package com.wwj.weiboClient.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

import com.wwj.weiboClient.R;

public class TextUtil {
	public static Map<String, Integer> faceNameToDrawableId = new HashMap<String, Integer>();
	public static Map<String, String> drawableIdToFaceName = new HashMap<String, String>();
	static {
		faceNameToDrawableId.put("µ÷Æ¤", R.drawable.h000);
		faceNameToDrawableId.put("ßÚÑÀ", R.drawable.h001);
		faceNameToDrawableId.put("¾ªÑÈ", R.drawable.h002);
		faceNameToDrawableId.put("ÄÑ¹ý", R.drawable.h003);
		faceNameToDrawableId.put("¿á", R.drawable.h004);
		faceNameToDrawableId.put("Àäº¹", R.drawable.h005);
		faceNameToDrawableId.put("×¥¿ñ", R.drawable.h006);
		faceNameToDrawableId.put("ÍÂ", R.drawable.h007);
		faceNameToDrawableId.put("ÍµÐ¦", R.drawable.h008);
		faceNameToDrawableId.put("¿É°®", R.drawable.h009);
		faceNameToDrawableId.put("°×ÑÛ", R.drawable.h010);
		faceNameToDrawableId.put("°ÁÂý", R.drawable.h011);
		faceNameToDrawableId.put("Î¢Ð¦", R.drawable.h012);
		faceNameToDrawableId.put("Æ²×ì", R.drawable.h013);
		faceNameToDrawableId.put("É«", R.drawable.h014);
		faceNameToDrawableId.put("·¢´ô", R.drawable.h015);
		faceNameToDrawableId.put("µÃÒâ", R.drawable.h016);
		faceNameToDrawableId.put("Á÷Àá", R.drawable.h017);
		faceNameToDrawableId.put("º¦Ðß", R.drawable.h018);
		faceNameToDrawableId.put("Ðê", R.drawable.h019);
		faceNameToDrawableId.put("À§", R.drawable.h020);

		faceNameToDrawableId.put("ÞÏÞÎ", R.drawable.h021);
		faceNameToDrawableId.put("·¢Å­", R.drawable.h022);
		faceNameToDrawableId.put("´ó¿Þ", R.drawable.h023);
		faceNameToDrawableId.put("Á÷º¹", R.drawable.h024);
		faceNameToDrawableId.put("ÔÙ¼û", R.drawable.h025);
		faceNameToDrawableId.put("ÇÃ´ò", R.drawable.h026);
		faceNameToDrawableId.put("²Áº¹", R.drawable.h027);
		faceNameToDrawableId.put("Î¯Çü", R.drawable.h028);
		faceNameToDrawableId.put("ÒÉÎÊ", R.drawable.h029);
		faceNameToDrawableId.put("Ë¯", R.drawable.h030);
		faceNameToDrawableId.put("Ç×Ç×", R.drawable.h031);

		faceNameToDrawableId.put("º©Ð¦", R.drawable.h032);
		faceNameToDrawableId.put("µ÷Æ¤", R.drawable.h033);
		faceNameToDrawableId.put("ÒõÏÕ", R.drawable.h034);
		faceNameToDrawableId.put("·Ü¶·", R.drawable.h035);
		faceNameToDrawableId.put("ÓÒºßºß", R.drawable.h036);
		faceNameToDrawableId.put("Óµ±§", R.drawable.h037);
		faceNameToDrawableId.put("»µÐ¦", R.drawable.h038);
		faceNameToDrawableId.put("±ÉÊÓ", R.drawable.h039);
		faceNameToDrawableId.put("ÔÎ", R.drawable.h040);
		faceNameToDrawableId.put("´ó±ø", R.drawable.h041);
		faceNameToDrawableId.put("¿ÉÁ¯", R.drawable.h042);

		faceNameToDrawableId.put("¼¢¶ö", R.drawable.h043);
		faceNameToDrawableId.put("ÖäÂî", R.drawable.h044);
		faceNameToDrawableId.put("¿Ù±Ç", R.drawable.h045);
		faceNameToDrawableId.put("¹ÄÕÆ", R.drawable.h046);
		faceNameToDrawableId.put("ôÜ´óÁË", R.drawable.h047);
		faceNameToDrawableId.put("×óºßºß", R.drawable.h048);
		faceNameToDrawableId.put("¹þÇ·", R.drawable.h049);
		faceNameToDrawableId.put("¿ì¿ÞÁË", R.drawable.h050);
		faceNameToDrawableId.put("ÏÅ", R.drawable.h051);
		faceNameToDrawableId.put("±Õ×ì", R.drawable.h052);
		faceNameToDrawableId.put("¾ª¿Ö", R.drawable.h053);

		faceNameToDrawableId.put("ÕÛÄ¥", R.drawable.h054);
		faceNameToDrawableId.put("Ê¾°®", R.drawable.h055);
		faceNameToDrawableId.put("°®ÐÄ", R.drawable.h056);
		faceNameToDrawableId.put("ÐÄËé", R.drawable.h057);
		faceNameToDrawableId.put("µ°¸â", R.drawable.h058);
		faceNameToDrawableId.put("ÉÁµç", R.drawable.h059);
		faceNameToDrawableId.put("Õ¨µ¯", R.drawable.h060);
		faceNameToDrawableId.put("µ¶", R.drawable.h061);
		faceNameToDrawableId.put("×ãÇò", R.drawable.h062);
		faceNameToDrawableId.put("Æ°³æ", R.drawable.h063);
		faceNameToDrawableId.put("±ã±ã", R.drawable.h064);

		faceNameToDrawableId.put("¿§·È", R.drawable.h065);
		faceNameToDrawableId.put("·¹", R.drawable.h066);
		faceNameToDrawableId.put("Öí", R.drawable.h067);
		faceNameToDrawableId.put("Ãµ¹å", R.drawable.h068);
		faceNameToDrawableId.put("µòÐ»", R.drawable.h069);
		faceNameToDrawableId.put("ÔÂÁÁ", R.drawable.h070);
		faceNameToDrawableId.put("Ì«Ñô", R.drawable.h071);
		faceNameToDrawableId.put("ÀñÎï", R.drawable.h072);
		faceNameToDrawableId.put("Ç¿", R.drawable.h073);
		faceNameToDrawableId.put("Èõ", R.drawable.h074);
		faceNameToDrawableId.put("ÎÕÊÖ", R.drawable.h075);

		faceNameToDrawableId.put("Ê¤Àû", R.drawable.h076);
		faceNameToDrawableId.put("±§È­", R.drawable.h077);
		faceNameToDrawableId.put("¹´Òý", R.drawable.h078);
		faceNameToDrawableId.put("È­Í·", R.drawable.h079);
		faceNameToDrawableId.put("²î¾¢", R.drawable.h080);
		faceNameToDrawableId.put("°®Äã", R.drawable.h081);
		faceNameToDrawableId.put("NO", R.drawable.h082);
		faceNameToDrawableId.put("OK", R.drawable.h083);
		faceNameToDrawableId.put("°®Çé", R.drawable.h084);
		faceNameToDrawableId.put("·ÉÎÇ", R.drawable.h085);
		faceNameToDrawableId.put("ÌøÌø", R.drawable.h086);

		faceNameToDrawableId.put("·¢¶¶", R.drawable.h087);
		faceNameToDrawableId.put("âæ»ð", R.drawable.h088);
		faceNameToDrawableId.put("×ªÈ¦", R.drawable.h089);
		faceNameToDrawableId.put("¿ÄÍ·", R.drawable.h090);
		faceNameToDrawableId.put("»ØÍ·", R.drawable.h091);
		faceNameToDrawableId.put("ÌøÉþ", R.drawable.h092);
		faceNameToDrawableId.put("»ÓÊÖ", R.drawable.h093);
		faceNameToDrawableId.put("¼¤¶¯", R.drawable.h094);
		faceNameToDrawableId.put("½ÖÎè", R.drawable.h095);
		faceNameToDrawableId.put("Ï×ÎÇ", R.drawable.h096);
		faceNameToDrawableId.put("×óÌ«¼«", R.drawable.h097);

		faceNameToDrawableId.put("ÓÒÌ«¼«", R.drawable.h098);
		faceNameToDrawableId.put("²Ëµ¶", R.drawable.h099);
		faceNameToDrawableId.put("Î÷¹Ï", R.drawable.h100);
		faceNameToDrawableId.put("Æ¡¾Æ", R.drawable.h101);
		faceNameToDrawableId.put("÷¼÷Ã", R.drawable.h102);
		faceNameToDrawableId.put("ÀºÇò", R.drawable.h103);
		faceNameToDrawableId.put("Æ¹ÅÒ", R.drawable.h104);
		faceNameToDrawableId.put("Æ¹ÅÒ", R.drawable.h105);
	}

	static {
		drawableIdToFaceName.put("h000", "µ÷Æ¤");
		drawableIdToFaceName.put("h001", "ßÚÑÀ");
		drawableIdToFaceName.put("h002", "¾ªÑÈ");
		drawableIdToFaceName.put("h003", "ÄÑ¹ý");
		drawableIdToFaceName.put("h004", "¿á");
		drawableIdToFaceName.put("h005", "Àäº¹");
		drawableIdToFaceName.put("h006", "×¥¿ñ");
		drawableIdToFaceName.put("h007", "ÍÂ");
		drawableIdToFaceName.put("h008", "ÍµÐ¦");
		drawableIdToFaceName.put("h009", "¿É°®");
		drawableIdToFaceName.put("h010", "°×ÑÛ");
		drawableIdToFaceName.put("h011", "°ÁÂý");
		drawableIdToFaceName.put("h012", "Î¢Ð¦");
		drawableIdToFaceName.put("h013", "Æ²×ì");
		drawableIdToFaceName.put("h014", "É«");
		drawableIdToFaceName.put("h015", "·¢´ô");
		drawableIdToFaceName.put("h016", "µÃÒâ");
		drawableIdToFaceName.put("h017", "Á÷Àá");
		drawableIdToFaceName.put("h018", "º¦Ðß");
		drawableIdToFaceName.put("h019", "Ðê");
		drawableIdToFaceName.put("h020", "À§");

		drawableIdToFaceName.put("h021", "ÞÏÞÎ");
		drawableIdToFaceName.put("h022", "·¢Å­");
		drawableIdToFaceName.put("h023", "´ó¿Þ");
		drawableIdToFaceName.put("h024", "Á÷º¹");
		drawableIdToFaceName.put("h025", "ÔÙ¼û");
		drawableIdToFaceName.put("h026", "ÇÃ´ò");
		drawableIdToFaceName.put("h027", "²Áº¹");
		drawableIdToFaceName.put("h028", "Î¯Çü");
		drawableIdToFaceName.put("h029", "ÒÉÎÊ");
		drawableIdToFaceName.put("h030", "Ë¯");
		drawableIdToFaceName.put("h031", "Ç×Ç×");

		drawableIdToFaceName.put("h032", "º©Ð¦");
		drawableIdToFaceName.put("h033", "µ÷Æ¤");
		drawableIdToFaceName.put("h034", "ÒõÏÕ");
		drawableIdToFaceName.put("h035", "·Ü¶·");
		drawableIdToFaceName.put("h036", "ÓÒºßºß");
		drawableIdToFaceName.put("h037", "Óµ±§");
		drawableIdToFaceName.put("h038", "»µÐ¦");
		drawableIdToFaceName.put("h039", "±ÉÊÓ");
		drawableIdToFaceName.put("h040", "ÔÎ");
		drawableIdToFaceName.put("h041", "´ó±ø");
		drawableIdToFaceName.put("h042", "¿ÉÁ¯");

		drawableIdToFaceName.put("h043", "¼¢¶ö");
		drawableIdToFaceName.put("h044", "ÖäÂî");
		drawableIdToFaceName.put("h045", "¿Ù±Ç");
		drawableIdToFaceName.put("h046", "¹ÄÕÆ");
		drawableIdToFaceName.put("h047", "ôÜ´óÁË");
		drawableIdToFaceName.put("h048", "×óºßºß");
		drawableIdToFaceName.put("h049", "¹þÇ·");
		drawableIdToFaceName.put("h050", "¿ì¿ÞÁË");
		drawableIdToFaceName.put("h051", "ÏÅ");
		drawableIdToFaceName.put("h052", "±Õ×ì");
		drawableIdToFaceName.put("h053", "¾ª¿Ö");

		drawableIdToFaceName.put("h054", "ÕÛÄ¥");
		drawableIdToFaceName.put("h055", "Ê¾°®");
		drawableIdToFaceName.put("h056", "°®ÐÄ");
		drawableIdToFaceName.put("h057", "ÐÄËé");
		drawableIdToFaceName.put("h058", "µ°¸â");
		drawableIdToFaceName.put("h059", "ÉÁµç");
		drawableIdToFaceName.put("h060", "Õ¨µ¯");
		drawableIdToFaceName.put("h061", "µ¶");
		drawableIdToFaceName.put("h062", "×ãÇò");
		drawableIdToFaceName.put("h063", "Æ°³æ");
		drawableIdToFaceName.put("h064", "±ã±ã");

		drawableIdToFaceName.put("h065", "¿§·È");
		drawableIdToFaceName.put("h066", "·¹");
		drawableIdToFaceName.put("h067", "Öí");
		drawableIdToFaceName.put("h068", "Ãµ¹å");
		drawableIdToFaceName.put("h069", "µòÐ»");
		drawableIdToFaceName.put("h070", "ÔÂÁÁ");
		drawableIdToFaceName.put("h071", "Ì«Ñô");
		drawableIdToFaceName.put("h072", "ÀñÎï");
		drawableIdToFaceName.put("h073", "Ç¿");
		drawableIdToFaceName.put("h074", "Èõ");
		drawableIdToFaceName.put("h075", "ÎÕÊÖ");

		drawableIdToFaceName.put("h076", "Ê¤Àû");
		drawableIdToFaceName.put("h077", "±§È­");
		drawableIdToFaceName.put("h078", "¹´Òý");
		drawableIdToFaceName.put("h079", "È­Í·");
		drawableIdToFaceName.put("h080", "²î¾¢");
		drawableIdToFaceName.put("h081", "°®Äã");
		drawableIdToFaceName.put("h082", "NO");
		drawableIdToFaceName.put("h083", "OK");
		drawableIdToFaceName.put("h084", "°®Çé");
		drawableIdToFaceName.put("h085", "·ÉÎÇ");
		drawableIdToFaceName.put("h086", "ÌøÌø");

		drawableIdToFaceName.put("h087", "·¢¶¶");
		drawableIdToFaceName.put("h088", "âæ»ð");
		drawableIdToFaceName.put("h089", "×ªÈ¦");
		drawableIdToFaceName.put("h090", "¿ÄÍ·");
		drawableIdToFaceName.put("h091", "»ØÍ·");
		drawableIdToFaceName.put("h092", "ÌøÉþ");
		drawableIdToFaceName.put("h093", "»ÓÊÖ");
		drawableIdToFaceName.put("h094", "¼¤¶¯");
		drawableIdToFaceName.put("h095", "½ÖÎè");
		drawableIdToFaceName.put("h096", "Ï×ÎÇ");
		drawableIdToFaceName.put("h097", "×óÌ«¼«");

		drawableIdToFaceName.put("h098", "ÓÒÌ«¼«");
		drawableIdToFaceName.put("h099", "²Ëµ¶");
		drawableIdToFaceName.put("h100", "Î÷¹Ï");
		drawableIdToFaceName.put("h101", "Æ¡¾Æ");
		drawableIdToFaceName.put("h102", "÷¼÷Ã");
		drawableIdToFaceName.put("h103", "ÀºÇò");
		drawableIdToFaceName.put("h104", "Æ¹ÅÒ");
		drawableIdToFaceName.put("h105", "Æ¹ÅÒ");
	}

	public static SpannableString decorateFaceInStr(SpannableString spannable,
			List<Map<String, Object>> list, Resources resources) {
		int size = list.size();
		Drawable drawable = null;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < size; i++) {
				Map<String, Object> map = list.get(i);
				drawable = resources.getDrawable(faceNameToDrawableId.get(map
						.get("faceName")));
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight());
				ImageSpan span = new ImageSpan(drawable,
						ImageSpan.ALIGN_BASELINE);
				spannable.setSpan(span, (Integer) map.get("startIndex"),
						(Integer) map.get("endIndex"),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	public static SpannableString decorateVipInStr(SpannableString spannable,
			List<Map<String, Object>> list, Resources resources) {
		int size = list.size();
		Drawable drawable = null;
		if (list != null && list.size() > 0) {
			for (int i = 0; i < size; i++) {
				Map<String, Object> map = list.get(i);
				drawable = resources.getDrawable(R.drawable.icon_vip);
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight());
				ImageSpan span = new ImageSpan(drawable,
						ImageSpan.ALIGN_BASELINE);
				spannable.setSpan(span, (Integer) map.get("startIndex"),
						(Integer) map.get("endIndex"),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}

	public static SpannableString decorateTopicInStr(SpannableString spannable,
			List<Map<String, Object>> list, Resources resources) {
		int size = list.size();
		Drawable drawable = null;
		CharacterStyle foregroundColorSpan = new ForegroundColorSpan(
				Color.argb(255, 33, 92, 110));
		if (list != null && list.size() > 0) {
			for (int i = 0; i < size; i++) {
				Map<String, Object> map = list.get(i);
				spannable.setSpan(foregroundColorSpan,
						(Integer) map.get("startIndex"),
						(Integer) map.get("endIndex"),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return spannable;
	}
}
