package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.SpannableString;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.Status;
import com.wwj.weiboClient.util.AsyncImageLoader;
import com.wwj.weiboClient.util.AsyncImageLoader.ImageCallback;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.DoneAndProcess;
import com.wwj.weiboClient.workqueue.task.FavoriteWeiboTask;
import com.wwj.weiboClient.workqueue.task.ParentTask;
import com.wwj.weiboClient.workqueue.task.PullFileTask;

/**
 * ���������΢����ͼ��
 * 
 * @author wwj
 * 
 */
public class SingleWeiboViewer extends Activity implements Const,
		DoneAndProcess, OnClickListener {

	// ��������Ϊstatic��ʹ��intent����status�Ͳ���ԭ����status��
	public static Status status;
	private ImageView profileImage; // ͷ��
	private ImageView verified; // ��֤ͼƬ
	private TextView userName; // �û��ǳ�
	private TextView statusText; // ΢������
	private View insideContent; // ת������
	private TextView retweetdetailText; // ת���ı�
	private ImageView retweetdetailImage; // ת�����ݵ�ͼƬ
	private ImageView statusImage; // ΢��ͼƬ
	private TextView source; // ΢����Դ
	private Button forwardButton; // ת����ť
	private Button retweetdetailForwardButton;
	private Button commentButton; // ���۰�ť
	private Button retweetdetailCommentButton;
	private Button refreshButton; // ˢ�°�ť
	private ProgressBar refreshProgressBar; // ˢ�½�����
	private View favorite; // �ղ�
	private View unfavorite; // ȡ���ղ�

	private int weiboType; // ΢������
	private String weiboid; // ��Ѷ΢��id
	private AsyncImageLoader asyncImageLoader;

	private JSONObject dataObj; // ��Ѷ����΢������
	private String imageUrl; // ͼƬUrl
	private boolean isFav = false; // �Ƿ��ղ�

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// �����ļ���������
			if (msg.obj instanceof PullFileTask) {
				String imageUrl = null;
				imageUrl = SinaWeiboManager.getImageurl(
						status.user.profile_image_url, SingleWeiboViewer.this,
						SingleWeiboViewer.this);
				if (imageUrl != null) {
					profileImage.setImageURI(Uri.fromFile(new File(imageUrl)));
				}
				if (status.retweeted_status == null) {
					imageUrl = SinaWeiboManager.getImageurl(status.bmiddle_pic,
							SingleWeiboViewer.this, SingleWeiboViewer.this);
					if (imageUrl != null) {
						statusImage.setImageURI(Uri
								.fromFile(new File(imageUrl)));
					}
				} else {
					imageUrl = SinaWeiboManager.getImageurl(
							status.retweeted_status.bmiddle_pic,
							SingleWeiboViewer.this, SingleWeiboViewer.this);
					if (imageUrl != null) {
						retweetdetailImage.setImageURI(Uri.fromFile(new File(
								imageUrl)));
					}
				}
			} else if (msg.obj instanceof FavoriteWeiboTask) {
				// �����ղ�΢������
				if (favorite.getVisibility() == View.VISIBLE) {
					favorite.setVisibility(View.GONE);
					unfavorite.setVisibility(View.VISIBLE);
					status.favorited = true;
					Toast.makeText(SingleWeiboViewer.this, "�ղسɹ�",
							Toast.LENGTH_LONG).show();
				} else {
					status.favorited = false;
					favorite.setVisibility(View.VISIBLE);
					unfavorite.setVisibility(View.GONE);
					Toast.makeText(SingleWeiboViewer.this, "ȡ���ղسɹ�",
							Toast.LENGTH_LONG).show();
				}
			} else if (msg.obj instanceof Status) { // ˢ��
				refreshButton.setVisibility(View.VISIBLE);
				refreshProgressBar.setVisibility(View.GONE);
				loadSinaWeiboContent();
			}
			super.handleMessage(msg);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_weibo_viewer);
		findView();

		asyncImageLoader = new AsyncImageLoader();
		Intent intent = getIntent();
		weiboType = intent.getIntExtra(StringUtil.WEIBO_TYPE, 0);
		weiboid = intent.getStringExtra("weiboid");
		if (weiboType == SINA) {
			loadSinaWeiboContent();
		} else {
			loadTencentWeiboContent();
		}

	}

	private void findView() {
		profileImage = (ImageView) findViewById(R.id.iv_profile_image);
		verified = (ImageView) findViewById(R.id.imageview_verified);
		userName = (TextView) findViewById(R.id.tv_name);
		statusText = (TextView) findViewById(R.id.tv_text);
		statusImage = (ImageView) findViewById(R.id.iv_status_image);
		insideContent = findViewById(R.id.ll_inside_content);
		retweetdetailText = (TextView) findViewById(R.id.tv_retweetdetail_text);
		retweetdetailImage = (ImageView) findViewById(R.id.iv_retweetdetail_image);
		source = (TextView) findViewById(R.id.tv_source);
		forwardButton = (Button) findViewById(R.id.btn_forward);
		commentButton = (Button) findViewById(R.id.btn_comment);
		retweetdetailForwardButton = (Button) findViewById(R.id.btn_retweetdetail_forward);
		retweetdetailCommentButton = (Button) findViewById(R.id.btn_retweetdetail_comment);
		favorite = findViewById(R.id.ll_fav);
		unfavorite = findViewById(R.id.ll_unfav);
		refreshButton = (Button) findViewById(R.id.btn_refresh);
		refreshProgressBar = (ProgressBar) findViewById(R.id.pb_refresh);
		findViewById(R.id.ll_forward).setOnClickListener(this);
		findViewById(R.id.ll_comment).setOnClickListener(this);
		findViewById(R.id.btn_back).setOnClickListener(this);
		refreshButton.setOnClickListener(this);

		retweetdetailImage.setOnClickListener(this);
		statusImage.setOnClickListener(this);
		forwardButton.setOnClickListener(this);
		commentButton.setOnClickListener(this);
		retweetdetailForwardButton.setOnClickListener(this);
		retweetdetailCommentButton.setOnClickListener(this);
		favorite.setOnClickListener(this);
		unfavorite.setOnClickListener(this);

	}

	// ������Ѷ΢������
	private void loadTencentWeiboContent() {
		// ��Ѷ΢��
		String accessToken = StorageManager.getValue(this,
				StringUtil.TENCENT_ACCESS_TOKEN, "");
		TencentWeiboManager.showWeiboDetail(this, accessToken,
				StringUtil.REQUEST_FORMAT, weiboid, weiboDetailCallBack);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	// ��������΢������
	private void loadSinaWeiboContent() {
		// �첽����ͷ��
		String imageUrl = SinaWeiboManager.getImageurl(
				status.user.profile_image_url, this, this);
		if (imageUrl != null) {
			profileImage.setImageURI(Uri.fromFile(new File(imageUrl)));
		}
		userName.setText(status.user.name);
		Tools.userVerified(verified, status.user.verified_type);
		statusText.setText(Tools.changeTextToFace(this,
				Html.fromHtml(Tools.atBlue(status.text))));
		source.setText("����   " + status.getTextSource());
		if (status.retweeted_status == null
				|| status.retweeted_status.user == null) {
			insideContent.setVisibility(View.GONE);
		} else {
			insideContent.setVisibility(View.VISIBLE);
			if (status.retweeted_status.user != null) {
				retweetdetailText.setText(Html.fromHtml(Tools.atBlue("@"
						+ status.retweeted_status.user.name + ":"
						+ status.retweeted_status.text)));
				retweetdetailForwardButton.setText(String
						.valueOf(status.retweeted_status.reposts_count));
				retweetdetailCommentButton.setText(String
						.valueOf(status.retweeted_status.comments_count));
			}
		}
		if (SinaWeiboManager.hasPicture(status)) {
			if (status.retweeted_status == null) {
				statusImage.setVisibility(View.VISIBLE);
				retweetdetailImage.setVisibility(View.GONE);
				// �첽װ��ͼƬ
				imageUrl = SinaWeiboManager.getImageurl(status.bmiddle_pic,
						this, this);
				if (imageUrl != null) {
					statusImage.setImageURI(Uri.fromFile(new File(imageUrl)));
				}
			} else {
				statusImage.setVisibility(View.GONE);
				retweetdetailImage.setVisibility(View.VISIBLE);
				// �첽װ��ͼ��
				imageUrl = SinaWeiboManager.getImageurl(
						status.retweeted_status.bmiddle_pic, this, this);
				if (imageUrl != null) {
					retweetdetailImage.setImageURI(Uri.fromFile(new File(
							imageUrl)));
				}
			}
		}
		forwardButton.setText(String.valueOf(status.reposts_count));
		commentButton.setText(String.valueOf(status.comments_count));
		if (status.favorited) {
			favorite.setVisibility(View.GONE);
			unfavorite.setVisibility(View.VISIBLE);
		} else {
			favorite.setVisibility(View.VISIBLE);
			unfavorite.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_status_image:
		case R.id.iv_retweetdetail_image:
			viewPicture();
			break;
		case R.id.btn_forward:
		case R.id.ll_forward:
			forward(); // ת��΢��
			break;
		case R.id.btn_retweetdetail_forward:
			retweetdetailForword();
			break;
		case R.id.ll_comment:
			comment();
			break;
		case R.id.ll_fav:
		case R.id.ll_unfav:
			favoriteWeibo();
			break;
		case R.id.btn_comment: // ��ȡ����
			getComments();
			break;
		case R.id.btn_retweetdetail_comment: // ��ȡת��΢��������
			getRetweetComments();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_refresh: // ˢ��
			refreshButton.setVisibility(View.GONE);
			refreshProgressBar.setVisibility(View.VISIBLE);
			switch (weiboType) {
			case SINA:
				SinaWeiboManager.showStatus(this, status.id,
						sinaStatusRequestListener);
				break;
			case TENCENT:
				loadTencentWeiboContent();
				break;
			}
			break;
		default:
			break;
		}
	}

	private void favoriteWeibo() {
		switch (weiboType) {
		case SINA:
			favorite(!status.favorited);
			break;
		case TENCENT:
			isFav = !isFav;
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String accessToken = StorageManager.getValue(
						SingleWeiboViewer.this,
						StringUtil.TENCENT_ACCESS_TOKEN, "");
				TencentWeiboManager.favoriteWeibo(SingleWeiboViewer.this,
						accessToken, StringUtil.REQUEST_FORMAT,
						dataObj.getString("id"), favoriteCallback);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

	}

	/**
	 * �鿴ͼƬ
	 */
	private void viewPicture() {
		Intent intent = null;
		String url = null;
		switch (weiboType) {
		case SINA:
			url = status.original_pic;
			if (status.retweeted_status != null) {
				url = status.retweeted_status.original_pic;
			}
			break;
		case TENCENT:
			url = imageUrl;
			break;
		}
		intent = new Intent(this, PictureViewer.class);
		intent.putExtra("file_url", url);
		intent.putExtra("type", PICTURE_VIEWER_WEIBO_BROWSER);
		startActivity(intent);
	}

	private void getRetweetComments() {
		Intent intent = new Intent(this, CommentListViewer.class);
		switch (weiboType) {
		case SINA:
			intent.putExtra("status_id", status.retweeted_status.id);
			// intent.putExtra("text", "//@" + status.retweeted_status.user.name
			// + ":" + status.retweeted_status.text);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				JSONObject sourceObj = dataObj.getJSONObject("source");
				if (sourceObj != null) {
					intent.putExtra("rootid", sourceObj.getString("id"));
					intent.putExtra("text", "//@" + sourceObj.getString("nick")
							+ ":" + sourceObj.getString("origtext"));
				} else {
					intent.putExtra("rootid", dataObj.getString("id"));
					intent.putExtra("text", "//@" + dataObj.getString("nick")
							+ ":" + dataObj.getString("origtext"));
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			break;
		}

		startActivity(intent);
	}

	private void getComments() {
		Intent intent = new Intent(this, CommentListViewer.class);
		switch (weiboType) {
		case SINA:
			intent.putExtra("status_id", status.id);
			intent.putExtra("text", "//@" + status.user.name + ":"
					+ status.text);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				intent.putExtra("rootid", weiboid);
				intent.putExtra("text", "//@" + dataObj.getString("nick") + ":"
						+ dataObj.getString("text"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

		startActivity(intent);
	}

	// �ղ�΢��
	private void favorite(boolean fav) {
		FavoriteWeiboTask favoriteWeiboTask = new FavoriteWeiboTask();
		favoriteWeiboTask.id = status.id;
		favoriteWeiboTask.fav = fav;
		favoriteWeiboTask.doneAndProcess = this;
		Tools.getGlobalObject(this).getWorkQueueStorage()
				.addTask(favoriteWeiboTask);
	}

	// ת��΢��
	private void forward() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			String text = "//@" + status.user.name + ":" + status.text;
			intent.putExtra("type", TYPE_FORWARD);
			intent.putExtra("title", "ת��΢��");
			intent.putExtra("text", text);
			intent.putExtra("status_id", status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String text2 = "//@" + dataObj.getString("nick") + ":"
						+ dataObj.getString("text");
				intent.putExtra("type", TYPE_FORWARD);
				intent.putExtra("title", "ת��΢��");
				intent.putExtra("text", text2);
				intent.putExtra("id", dataObj.getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

		startActivity(intent);
	}

	// ת��ԭ΢��
	private void retweetdetailForword() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			String text = "//@" + status.retweeted_status.user.name + ":"
					+ status.retweeted_status.text;
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_FORWARD);
			intent.putExtra("title", "ת��΢��");
			intent.putExtra("text", text);
			intent.putExtra("status_id", status.retweeted_status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				JSONObject sourceObj = dataObj.getJSONObject("source");
				String text2 = "//@" + dataObj.getString("nick") + ":"
						+ dataObj.getString("text");
				if (sourceObj != null) {
					text2 = "//@" + sourceObj.getString("nick") + ":"
							+ sourceObj.getString("origtext");
				}

				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_FORWARD);
				intent.putExtra("title", "ת��΢��");
				intent.putExtra("text", text2);
				intent.putExtra("id", sourceObj.getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

		startActivity(intent);
	}

	// ����
	private void comment() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_COMMENT);
			intent.putExtra("title", "����΢��");
			intent.putExtra("text", "");
			intent.putExtra("status_id", status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "��ȴ��������",
							Toast.LENGTH_SHORT).show();
					return;
				}
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_COMMENT);
				intent.putExtra("title", "����΢��");
				intent.putExtra("text", "");
				intent.putExtra("id", dataObj.getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		startActivity(intent);
	}

	@Override
	public void doneProcess(ParentTask task) {
		Message msg = new Message();
		msg.obj = task;
		handler.sendMessage(msg);
	}

	public HttpCallback getWeiboDetailCallBack() {
		return weiboDetailCallBack;
	}

	public void setWeiboDetailCallBack(HttpCallback weiboDetailCallBack) {
		this.weiboDetailCallBack = weiboDetailCallBack;
	}

	private HttpCallback weiboDetailCallBack = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null || result.getObj() == null) {
				return;
			}
			String jsonResult = result.getObj().toString();

			Drawable cachedImage;
			try {
				dataObj = new JSONObject(jsonResult).getJSONObject("data");
				cachedImage = asyncImageLoader.loadDrawable(
						dataObj.getString("head") + "/100", profileImage,
						new ImageCallback() {

							@Override
							public void imageLoaded(Drawable imageDrawable,
									ImageView imageView, String imageUrl) {
								imageView.setImageDrawable(imageDrawable);
							}
						});

				if (cachedImage == null) {
					profileImage.setImageResource(R.drawable.avatar_default);
				} else { // ��ʾͷ��
					profileImage.setImageDrawable(cachedImage);
				}
				// ��ʾ�ǳ�
				userName.setText(dataObj.getString("nick"));
				// ΢�����ݿ�ʼ
				String origtext = dataObj.getString("origtext");

				SpannableString spannableString = new SpannableString(origtext);
				statusText.setText(spannableString);
				source.setText("����" + dataObj.getString("from"));
				if (dataObj.getInt("type") != 2) { // ����ת�ض�����
					insideContent.setVisibility(View.GONE);
				} else {
					insideContent.setVisibility(View.VISIBLE);
					JSONObject sourceObj = dataObj.getJSONObject("source");
					retweetdetailText.setText(sourceObj.getString("origtext"));
					retweetdetailForwardButton.setText(sourceObj
							.getString("count"));
					retweetdetailCommentButton.setText(sourceObj
							.getString("mcount"));
					JSONArray retweetStatuImage = sourceObj
							.optJSONArray("image");
					if (retweetStatuImage != null
							&& retweetStatuImage.length() > 0) {
						retweetdetailImage.setVisibility(View.VISIBLE);
						statusImage.setVisibility(View.GONE);
						imageUrl = retweetStatuImage.optString(0) + "/460";// Ϊʲô��/460����Ѷ�涨�ģ�֧��160��2000��460����һЩ���ǲ�ס��
						Drawable drawable = asyncImageLoader.loadDrawable(
								imageUrl, retweetdetailImage,
								new ImageCallback() {
									@Override
									public void imageLoaded(
											Drawable imageDrawable,
											ImageView imageView, String imageUrl) {
										imageView
												.setImageDrawable(imageDrawable);
									}
								});
						if (drawable == null) {
							retweetdetailImage
									.setImageResource(R.drawable.wb_album_pic_default);
						} else {
							retweetdetailImage.setImageDrawable(drawable);
						}
					}
				}
				JSONArray imageArray = dataObj.optJSONArray("image"); // �����΢����ͼƬ���ݣ�����ʾ����
				if (imageArray != null && imageArray.length() > 0) {
					statusImage.setVisibility(View.VISIBLE);
					System.out.println(imageArray.optString(0));
					imageUrl = imageArray.optString(0) + "/460";// Ϊʲô��/460����Ѷ�涨�ģ�֧��160��2000��460����һЩ���ǲ�ס��
					Drawable drawable = asyncImageLoader.loadDrawable(imageUrl,
							statusImage, new ImageCallback() {
								@Override
								public void imageLoaded(Drawable imageDrawable,
										ImageView imageView, String imageUrl) {
									imageView.setImageDrawable(imageDrawable);
								}
							});
					if (drawable == null) {
						statusImage
								.setImageResource(R.drawable.wb_album_pic_default);
					} else {
						statusImage.setImageDrawable(drawable);
					}
				}
				forwardButton.setText(dataObj.getString("count")); // ��ת������
				commentButton.setText(dataObj.getString("mcount")); // �����۴���
				// if (status.favorited) {
				// favorite.setVisibility(View.GONE);
				// unfavorite.setVisibility(View.VISIBLE);
				// } else {
				// favorite.setVisibility(View.VISIBLE);
				// unfavorite.setVisibility(View.GONE);
				// }
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				refreshButton.setVisibility(View.VISIBLE);
				refreshProgressBar.setVisibility(View.GONE);
			}
		}
	};

	private RequestListener sinaStatusRequestListener = new RequestListener() {

		@Override
		public void onIOException(IOException e) {
			e.printStackTrace();
		}

		@Override
		public void onError(WeiboException e) {
			e.printStackTrace();
		}

		@Override
		public void onComplete4binary(ByteArrayOutputStream responseOS) {

		}

		@Override
		public void onComplete(String response) {
			// ���½�΢������װ�ص�status��
			JSONAndObject.convertSingleObject(status, response);
			Message msg = new Message();
			msg.obj = status;
			// ʹ��Handlerˢ�¿ؼ��е�����
			handler.sendMessage(msg);
		}
	};

	private HttpCallback favoriteCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null) {
				return;
			}
			String jsonRestult = result.getObj().toString();
			try {
				JSONObject jsonObject = new JSONObject(jsonRestult);
				if ("ok".equals(jsonObject.getString("msg"))) {
					if (isFav) {
						ToastUtil.showLongToast(SingleWeiboViewer.this, "�ղسɹ�");
						favorite.setVisibility(View.GONE);
						unfavorite.setVisibility(View.VISIBLE);
					} else {
						favorite.setVisibility(View.VISIBLE);
						unfavorite.setVisibility(View.GONE);
						ToastUtil.showLongToast(SingleWeiboViewer.this,
								"ȡ���ղسɹ�");
					}
				} else {
					if (isFav) {
						ToastUtil.showLongToast(SingleWeiboViewer.this, "�ղ�ʧ��");
					} else {
						ToastUtil.showLongToast(SingleWeiboViewer.this,
								"ȡ���ղ�ʧ��");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

}
