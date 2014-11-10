package com.wwj.weiboClient.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wwj.weiboClient.R;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.task.ParentTask;

/**
 * ��΢���б������� ����Ҫ������ʾ��ҳ����ʾ�ҵġ��ղ�΢���б�
 * 
 * @author wwj
 * 
 */
public class SinaWeiboListAdapter extends BaseAdapter implements Const,
		DoneAndProcess {
	protected Activity context;
	protected int type;
	protected LayoutInflater layoutInflater;
	protected List<Status> statuses;

	public SinaWeiboListAdapter(Activity context) {
		super();
		this.context = context;
	}

	/**
	 * ��Ҫ����һ���Ѿ���װ΢�����ݵ�List���� type��ʾ
	 * 
	 * @param activity
	 * @param statuses
	 * @param type
	 *            ��ʾ�����΢���������ͣ���Const�ж�����س���
	 */
	public SinaWeiboListAdapter(Activity context, List<Status> statuses,
			int type) {
		this.context = context;
		this.type = type;
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.statuses = new ArrayList<Status>();
		if (statuses != null)
			this.statuses.addAll(statuses);
		try {
			// ����΢������
			StorageManager.saveList(statuses, PATH_STORAGE, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
		// ��ȡ΢��������1Ϊ�����һ����ʾ"����"
		return statuses.size() + 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	// ��ȡ�ƶ�λ�õ�Status����
	public Status getStatus(int position) {
		if (statuses.size() > 0) {
			return statuses.get(position);
		} else {
			return null;
		}
	}

	// ��ȡ�Ѿ���ʾ��΢������СID
	public long getMinId() {
		if (statuses.size() > 0)
			// ��ȥ������
			return statuses.get(statuses.size() - 1).id;
		else
			return Long.MAX_VALUE;
	}

	// ��ȡ�Ѿ���ʾ��΢�����ID
	public long getMaxId() {
		if (statuses.size() > 0)
			return statuses.get(0).id;
		else
			return 0;
	}

	// ����µ�΢��
	// ��ˢ�¡���ʾ����΢��ʱ����ݲ�ͬ���������������΢��
	public void putStatuses(List<Status> statuses) {
		if (statuses == null || this.statuses == null)
			return;
		if (statuses.size() == 0)
			return;
		if (this.statuses.size() == 0) {
			this.statuses.addAll(statuses);
		} else if (statuses
				.get(0)
				.getCreatedAt()
				.before(this.statuses.get(this.statuses.size() - 1)
						.getCreatedAt())) {
			this.statuses.addAll(statuses);
			// ��ӵ�statuses��ԭ�����£���������С�ڵ���Ĭ�Ϸ���������ֱ�ӽ�statuses��ӵ�ǰ��
		} else if (statuses.get(statuses.size() - 1).getCreatedAt()
				.after(this.statuses.get(0).getCreatedAt())
				&& statuses.size() <= DEFAULT_STATUS_COUNT) {
			this.statuses.addAll(0, statuses);
		} else {
			this.statuses.clear();
			this.statuses.addAll(statuses);
		}
		try {
			// ����΢������
			StorageManager.saveList(this.statuses, PATH_STORAGE, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// ����ˢ��ListView
		this.notifyDataSetChanged();
	}

	// ���Ƹ��ද��
	private boolean showMoreAnimFlag = false;

	// ��ʾ���ද��
	public void showMoreAnim() {
		showMoreAnimFlag = true;
		notifyDataSetChanged();
	}

	// ���ظ��ද��
	public void hideMoreAnim() {
		showMoreAnimFlag = false;
		notifyDataSetChanged();
	}

	// ͨ��urlװ��Ҫ��ʾ��ͼ�����ͼ���ļ������ڣ���ͨ��hideView��־�����Ƿ�����ImageView���
	// hideView: true ����ImageView hideView��false �����κζ���
	private void loadImage(ImageView imageView, String url, boolean hideView) {
		if (url != null) {
			String imageUrl = SinaWeiboManager.getImageurl(url, context);
			if (imageUrl != null) {
				imageView.setImageURI(Uri.fromFile(new File(imageUrl)));
				imageView.setVisibility(View.VISIBLE);
				return;
			}
		}
		if (hideView)
			imageView.setVisibility(View.GONE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.sina_weibo_list_item,
					null);
		}

		View weiboListItem = convertView.findViewById(R.id.ll_weibo_list_item); // ΢���б���
		View moreWeiboListItem = convertView
				.findViewById(R.id.rl_more_weibo_list_item); // "����"�б���
		// ���б�������һ��ʱ����ִ��
		if (position < statuses.size()) {
			weiboListItem.setVisibility(View.VISIBLE);
			moreWeiboListItem.setVisibility(View.GONE);// �������һ���ʾ
			Status status = statuses.get(position);

			TextView statusText = (TextView) convertView
					.findViewById(R.id.tv_text);
			TextView name = (TextView) convertView.findViewById(R.id.tv_name);
			TextView createAt = (TextView) convertView
					.findViewById(R.id.tv_created_at);
			ImageView profileImage = (ImageView) convertView
					.findViewById(R.id.iv_profile_image);
			profileImage.setImageResource(R.drawable.avatar_default); // ����Ĭ��ͷ��
			ImageView picture = (ImageView) convertView
					.findViewById(R.id.iv_picture);
			ImageView statusImage = (ImageView) convertView
					.findViewById(R.id.iv_status_image);
			ImageView verified = (ImageView) convertView
					.findViewById(R.id.iv_verified);

			verified.setVisibility(View.GONE); // ������֤���ɼ�

			if (status.user != null) {
				// �����û���֤ͼ�꣬��������ɫ�ġ�V������������
				Tools.userVerified(verified, status.user.verified_type);
			}

			statusImage.setImageBitmap(null);
			LinearLayout insideContent = (LinearLayout) convertView
					.findViewById(R.id.ll_inside_content);
			ImageView retweetdetailImage = (ImageView) convertView
					.findViewById(R.id.iv_retweetdetail_image);
			retweetdetailImage.setImageBitmap(null);
			TextView retweetdetailText = (TextView) convertView
					.findViewById(R.id.tv_retweetdetail_text);
			TextView source = (TextView) convertView
					.findViewById(R.id.tv_source);
			Button btnRetweet = (Button) convertView
					.findViewById(R.id.btn_retweet);
			Button btnComment = (Button) convertView
					.findViewById(R.id.btn_comment);
			Button btnGood = (Button) convertView.findViewById(R.id.btn_good);

			// װ��ͼ��
			if (status.user != null) {
				loadImage(profileImage, status.user.profile_image_url, false);
			}
			loadImage(statusImage, status.thumbnail_pic, true);

			statusText.setText(Tools.changeTextToFace(context,
					Html.fromHtml(Tools.atBlue(status.text))));

			if (status.user != null)
				name.setText(status.user.name); // ��ʾ�û��ǳ�
			// ��ǰ΢����ͼ��
			if (SinaWeiboManager.hasPicture(status))
				picture.setVisibility(View.VISIBLE);
			else
				picture.setVisibility(View.INVISIBLE);

			createAt.setText(Tools.getTimeStr(status.getCreatedAt(), new Date()));
			source.setText("����: " + status.getTextSource()); // ����΢����Դ

			if (status.retweeted_status != null // ���ת�������ݲ�Ϊ��
					&& status.retweeted_status.user != null) {
				insideContent.setVisibility(View.VISIBLE);

				retweetdetailText.setText(Html.fromHtml(Tools.atBlue("@"
						+ status.retweeted_status.user.name + ":"
						+ status.retweeted_status.text)));
				loadImage(retweetdetailImage,
						status.retweeted_status.thumbnail_pic, false);
			} else {
				insideContent.setVisibility(View.GONE);
			}
			btnRetweet.setText("ת��:" + status.reposts_count);
			btnComment.setText("����:" + status.comments_count);
			btnGood.setText("����:" + status.attitudes_count);

		} else {
			moreWeiboListItem.setVisibility(View.VISIBLE);// ��ʾ"����"
			weiboListItem.setVisibility(View.GONE);
			View moreAnim = convertView.findViewById(R.id.pb_more);
			if (showMoreAnimFlag) {
				moreAnim.setVisibility(View.VISIBLE);
			} else {
				moreAnim.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	@Override
	public void doneProcess(ParentTask task) {
		notifyDataSetChanged(); // ֪ͨ�����б�����
	}
}
