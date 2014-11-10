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
		faceNameToDrawableId.put("��Ƥ", R.drawable.h000);
		faceNameToDrawableId.put("����", R.drawable.h001);
		faceNameToDrawableId.put("����", R.drawable.h002);
		faceNameToDrawableId.put("�ѹ�", R.drawable.h003);
		faceNameToDrawableId.put("��", R.drawable.h004);
		faceNameToDrawableId.put("�亹", R.drawable.h005);
		faceNameToDrawableId.put("ץ��", R.drawable.h006);
		faceNameToDrawableId.put("��", R.drawable.h007);
		faceNameToDrawableId.put("͵Ц", R.drawable.h008);
		faceNameToDrawableId.put("�ɰ�", R.drawable.h009);
		faceNameToDrawableId.put("����", R.drawable.h010);
		faceNameToDrawableId.put("����", R.drawable.h011);
		faceNameToDrawableId.put("΢Ц", R.drawable.h012);
		faceNameToDrawableId.put("Ʋ��", R.drawable.h013);
		faceNameToDrawableId.put("ɫ", R.drawable.h014);
		faceNameToDrawableId.put("����", R.drawable.h015);
		faceNameToDrawableId.put("����", R.drawable.h016);
		faceNameToDrawableId.put("����", R.drawable.h017);
		faceNameToDrawableId.put("����", R.drawable.h018);
		faceNameToDrawableId.put("��", R.drawable.h019);
		faceNameToDrawableId.put("��", R.drawable.h020);

		faceNameToDrawableId.put("����", R.drawable.h021);
		faceNameToDrawableId.put("��ŭ", R.drawable.h022);
		faceNameToDrawableId.put("���", R.drawable.h023);
		faceNameToDrawableId.put("����", R.drawable.h024);
		faceNameToDrawableId.put("�ټ�", R.drawable.h025);
		faceNameToDrawableId.put("�ô�", R.drawable.h026);
		faceNameToDrawableId.put("����", R.drawable.h027);
		faceNameToDrawableId.put("ί��", R.drawable.h028);
		faceNameToDrawableId.put("����", R.drawable.h029);
		faceNameToDrawableId.put("˯", R.drawable.h030);
		faceNameToDrawableId.put("����", R.drawable.h031);

		faceNameToDrawableId.put("��Ц", R.drawable.h032);
		faceNameToDrawableId.put("��Ƥ", R.drawable.h033);
		faceNameToDrawableId.put("����", R.drawable.h034);
		faceNameToDrawableId.put("�ܶ�", R.drawable.h035);
		faceNameToDrawableId.put("�Һߺ�", R.drawable.h036);
		faceNameToDrawableId.put("ӵ��", R.drawable.h037);
		faceNameToDrawableId.put("��Ц", R.drawable.h038);
		faceNameToDrawableId.put("����", R.drawable.h039);
		faceNameToDrawableId.put("��", R.drawable.h040);
		faceNameToDrawableId.put("���", R.drawable.h041);
		faceNameToDrawableId.put("����", R.drawable.h042);

		faceNameToDrawableId.put("����", R.drawable.h043);
		faceNameToDrawableId.put("����", R.drawable.h044);
		faceNameToDrawableId.put("�ٱ�", R.drawable.h045);
		faceNameToDrawableId.put("����", R.drawable.h046);
		faceNameToDrawableId.put("�ܴ���", R.drawable.h047);
		faceNameToDrawableId.put("��ߺ�", R.drawable.h048);
		faceNameToDrawableId.put("��Ƿ", R.drawable.h049);
		faceNameToDrawableId.put("�����", R.drawable.h050);
		faceNameToDrawableId.put("��", R.drawable.h051);
		faceNameToDrawableId.put("����", R.drawable.h052);
		faceNameToDrawableId.put("����", R.drawable.h053);

		faceNameToDrawableId.put("��ĥ", R.drawable.h054);
		faceNameToDrawableId.put("ʾ��", R.drawable.h055);
		faceNameToDrawableId.put("����", R.drawable.h056);
		faceNameToDrawableId.put("����", R.drawable.h057);
		faceNameToDrawableId.put("����", R.drawable.h058);
		faceNameToDrawableId.put("����", R.drawable.h059);
		faceNameToDrawableId.put("ը��", R.drawable.h060);
		faceNameToDrawableId.put("��", R.drawable.h061);
		faceNameToDrawableId.put("����", R.drawable.h062);
		faceNameToDrawableId.put("ư��", R.drawable.h063);
		faceNameToDrawableId.put("���", R.drawable.h064);

		faceNameToDrawableId.put("����", R.drawable.h065);
		faceNameToDrawableId.put("��", R.drawable.h066);
		faceNameToDrawableId.put("��", R.drawable.h067);
		faceNameToDrawableId.put("õ��", R.drawable.h068);
		faceNameToDrawableId.put("��л", R.drawable.h069);
		faceNameToDrawableId.put("����", R.drawable.h070);
		faceNameToDrawableId.put("̫��", R.drawable.h071);
		faceNameToDrawableId.put("����", R.drawable.h072);
		faceNameToDrawableId.put("ǿ", R.drawable.h073);
		faceNameToDrawableId.put("��", R.drawable.h074);
		faceNameToDrawableId.put("����", R.drawable.h075);

		faceNameToDrawableId.put("ʤ��", R.drawable.h076);
		faceNameToDrawableId.put("��ȭ", R.drawable.h077);
		faceNameToDrawableId.put("����", R.drawable.h078);
		faceNameToDrawableId.put("ȭͷ", R.drawable.h079);
		faceNameToDrawableId.put("�", R.drawable.h080);
		faceNameToDrawableId.put("����", R.drawable.h081);
		faceNameToDrawableId.put("NO", R.drawable.h082);
		faceNameToDrawableId.put("OK", R.drawable.h083);
		faceNameToDrawableId.put("����", R.drawable.h084);
		faceNameToDrawableId.put("����", R.drawable.h085);
		faceNameToDrawableId.put("����", R.drawable.h086);

		faceNameToDrawableId.put("����", R.drawable.h087);
		faceNameToDrawableId.put("���", R.drawable.h088);
		faceNameToDrawableId.put("תȦ", R.drawable.h089);
		faceNameToDrawableId.put("��ͷ", R.drawable.h090);
		faceNameToDrawableId.put("��ͷ", R.drawable.h091);
		faceNameToDrawableId.put("����", R.drawable.h092);
		faceNameToDrawableId.put("����", R.drawable.h093);
		faceNameToDrawableId.put("����", R.drawable.h094);
		faceNameToDrawableId.put("����", R.drawable.h095);
		faceNameToDrawableId.put("����", R.drawable.h096);
		faceNameToDrawableId.put("��̫��", R.drawable.h097);

		faceNameToDrawableId.put("��̫��", R.drawable.h098);
		faceNameToDrawableId.put("�˵�", R.drawable.h099);
		faceNameToDrawableId.put("����", R.drawable.h100);
		faceNameToDrawableId.put("ơ��", R.drawable.h101);
		faceNameToDrawableId.put("����", R.drawable.h102);
		faceNameToDrawableId.put("����", R.drawable.h103);
		faceNameToDrawableId.put("ƹ��", R.drawable.h104);
		faceNameToDrawableId.put("ƹ��", R.drawable.h105);
	}

	static {
		drawableIdToFaceName.put("h000", "��Ƥ");
		drawableIdToFaceName.put("h001", "����");
		drawableIdToFaceName.put("h002", "����");
		drawableIdToFaceName.put("h003", "�ѹ�");
		drawableIdToFaceName.put("h004", "��");
		drawableIdToFaceName.put("h005", "�亹");
		drawableIdToFaceName.put("h006", "ץ��");
		drawableIdToFaceName.put("h007", "��");
		drawableIdToFaceName.put("h008", "͵Ц");
		drawableIdToFaceName.put("h009", "�ɰ�");
		drawableIdToFaceName.put("h010", "����");
		drawableIdToFaceName.put("h011", "����");
		drawableIdToFaceName.put("h012", "΢Ц");
		drawableIdToFaceName.put("h013", "Ʋ��");
		drawableIdToFaceName.put("h014", "ɫ");
		drawableIdToFaceName.put("h015", "����");
		drawableIdToFaceName.put("h016", "����");
		drawableIdToFaceName.put("h017", "����");
		drawableIdToFaceName.put("h018", "����");
		drawableIdToFaceName.put("h019", "��");
		drawableIdToFaceName.put("h020", "��");

		drawableIdToFaceName.put("h021", "����");
		drawableIdToFaceName.put("h022", "��ŭ");
		drawableIdToFaceName.put("h023", "���");
		drawableIdToFaceName.put("h024", "����");
		drawableIdToFaceName.put("h025", "�ټ�");
		drawableIdToFaceName.put("h026", "�ô�");
		drawableIdToFaceName.put("h027", "����");
		drawableIdToFaceName.put("h028", "ί��");
		drawableIdToFaceName.put("h029", "����");
		drawableIdToFaceName.put("h030", "˯");
		drawableIdToFaceName.put("h031", "����");

		drawableIdToFaceName.put("h032", "��Ц");
		drawableIdToFaceName.put("h033", "��Ƥ");
		drawableIdToFaceName.put("h034", "����");
		drawableIdToFaceName.put("h035", "�ܶ�");
		drawableIdToFaceName.put("h036", "�Һߺ�");
		drawableIdToFaceName.put("h037", "ӵ��");
		drawableIdToFaceName.put("h038", "��Ц");
		drawableIdToFaceName.put("h039", "����");
		drawableIdToFaceName.put("h040", "��");
		drawableIdToFaceName.put("h041", "���");
		drawableIdToFaceName.put("h042", "����");

		drawableIdToFaceName.put("h043", "����");
		drawableIdToFaceName.put("h044", "����");
		drawableIdToFaceName.put("h045", "�ٱ�");
		drawableIdToFaceName.put("h046", "����");
		drawableIdToFaceName.put("h047", "�ܴ���");
		drawableIdToFaceName.put("h048", "��ߺ�");
		drawableIdToFaceName.put("h049", "��Ƿ");
		drawableIdToFaceName.put("h050", "�����");
		drawableIdToFaceName.put("h051", "��");
		drawableIdToFaceName.put("h052", "����");
		drawableIdToFaceName.put("h053", "����");

		drawableIdToFaceName.put("h054", "��ĥ");
		drawableIdToFaceName.put("h055", "ʾ��");
		drawableIdToFaceName.put("h056", "����");
		drawableIdToFaceName.put("h057", "����");
		drawableIdToFaceName.put("h058", "����");
		drawableIdToFaceName.put("h059", "����");
		drawableIdToFaceName.put("h060", "ը��");
		drawableIdToFaceName.put("h061", "��");
		drawableIdToFaceName.put("h062", "����");
		drawableIdToFaceName.put("h063", "ư��");
		drawableIdToFaceName.put("h064", "���");

		drawableIdToFaceName.put("h065", "����");
		drawableIdToFaceName.put("h066", "��");
		drawableIdToFaceName.put("h067", "��");
		drawableIdToFaceName.put("h068", "õ��");
		drawableIdToFaceName.put("h069", "��л");
		drawableIdToFaceName.put("h070", "����");
		drawableIdToFaceName.put("h071", "̫��");
		drawableIdToFaceName.put("h072", "����");
		drawableIdToFaceName.put("h073", "ǿ");
		drawableIdToFaceName.put("h074", "��");
		drawableIdToFaceName.put("h075", "����");

		drawableIdToFaceName.put("h076", "ʤ��");
		drawableIdToFaceName.put("h077", "��ȭ");
		drawableIdToFaceName.put("h078", "����");
		drawableIdToFaceName.put("h079", "ȭͷ");
		drawableIdToFaceName.put("h080", "�");
		drawableIdToFaceName.put("h081", "����");
		drawableIdToFaceName.put("h082", "NO");
		drawableIdToFaceName.put("h083", "OK");
		drawableIdToFaceName.put("h084", "����");
		drawableIdToFaceName.put("h085", "����");
		drawableIdToFaceName.put("h086", "����");

		drawableIdToFaceName.put("h087", "����");
		drawableIdToFaceName.put("h088", "���");
		drawableIdToFaceName.put("h089", "תȦ");
		drawableIdToFaceName.put("h090", "��ͷ");
		drawableIdToFaceName.put("h091", "��ͷ");
		drawableIdToFaceName.put("h092", "����");
		drawableIdToFaceName.put("h093", "����");
		drawableIdToFaceName.put("h094", "����");
		drawableIdToFaceName.put("h095", "����");
		drawableIdToFaceName.put("h096", "����");
		drawableIdToFaceName.put("h097", "��̫��");

		drawableIdToFaceName.put("h098", "��̫��");
		drawableIdToFaceName.put("h099", "�˵�");
		drawableIdToFaceName.put("h100", "����");
		drawableIdToFaceName.put("h101", "ơ��");
		drawableIdToFaceName.put("h102", "����");
		drawableIdToFaceName.put("h103", "����");
		drawableIdToFaceName.put("h104", "ƹ��");
		drawableIdToFaceName.put("h105", "ƹ��");
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
