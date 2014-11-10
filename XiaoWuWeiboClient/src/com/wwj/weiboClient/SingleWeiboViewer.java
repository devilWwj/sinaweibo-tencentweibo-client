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
 * 【浏览单条微博视图】
 * 
 * @author wwj
 * 
 */
public class SingleWeiboViewer extends Activity implements Const,
		DoneAndProcess, OnClickListener {

	// 必须声明为static，使用intent传递status就不是原来的status了
	public static Status status;
	private ImageView profileImage; // 头像
	private ImageView verified; // 认证图片
	private TextView userName; // 用户昵称
	private TextView statusText; // 微博内容
	private View insideContent; // 转发内容
	private TextView retweetdetailText; // 转发文本
	private ImageView retweetdetailImage; // 转发内容的图片
	private ImageView statusImage; // 微博图片
	private TextView source; // 微博来源
	private Button forwardButton; // 转发按钮
	private Button retweetdetailForwardButton;
	private Button commentButton; // 评论按钮
	private Button retweetdetailCommentButton;
	private Button refreshButton; // 刷新按钮
	private ProgressBar refreshProgressBar; // 刷新进度条
	private View favorite; // 收藏
	private View unfavorite; // 取消收藏

	private int weiboType; // 微博类型
	private String weiboid; // 腾讯微博id
	private AsyncImageLoader asyncImageLoader;

	private JSONObject dataObj; // 腾讯单条微博对象
	private String imageUrl; // 图片Url
	private boolean isFav = false; // 是否收藏

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			// 处理文件下载任务
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
				// 处理收藏微博任务
				if (favorite.getVisibility() == View.VISIBLE) {
					favorite.setVisibility(View.GONE);
					unfavorite.setVisibility(View.VISIBLE);
					status.favorited = true;
					Toast.makeText(SingleWeiboViewer.this, "收藏成功",
							Toast.LENGTH_LONG).show();
				} else {
					status.favorited = false;
					favorite.setVisibility(View.VISIBLE);
					unfavorite.setVisibility(View.GONE);
					Toast.makeText(SingleWeiboViewer.this, "取消收藏成功",
							Toast.LENGTH_LONG).show();
				}
			} else if (msg.obj instanceof Status) { // 刷新
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

	// 加载腾讯微博内容
	private void loadTencentWeiboContent() {
		// 腾讯微博
		String accessToken = StorageManager.getValue(this,
				StringUtil.TENCENT_ACCESS_TOKEN, "");
		TencentWeiboManager.showWeiboDetail(this, accessToken,
				StringUtil.REQUEST_FORMAT, weiboid, weiboDetailCallBack);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 加载新浪微博内容
	private void loadSinaWeiboContent() {
		// 异步加载头像
		String imageUrl = SinaWeiboManager.getImageurl(
				status.user.profile_image_url, this, this);
		if (imageUrl != null) {
			profileImage.setImageURI(Uri.fromFile(new File(imageUrl)));
		}
		userName.setText(status.user.name);
		Tools.userVerified(verified, status.user.verified_type);
		statusText.setText(Tools.changeTextToFace(this,
				Html.fromHtml(Tools.atBlue(status.text))));
		source.setText("来自   " + status.getTextSource());
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
				// 异步装载图片
				imageUrl = SinaWeiboManager.getImageurl(status.bmiddle_pic,
						this, this);
				if (imageUrl != null) {
					statusImage.setImageURI(Uri.fromFile(new File(imageUrl)));
				}
			} else {
				statusImage.setVisibility(View.GONE);
				retweetdetailImage.setVisibility(View.VISIBLE);
				// 异步装载图像
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
			forward(); // 转发微博
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
		case R.id.btn_comment: // 获取评论
			getComments();
			break;
		case R.id.btn_retweetdetail_comment: // 获取转发微博的评论
			getRetweetComments();
			break;
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_refresh: // 刷新
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
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
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
	 * 查看图片
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
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
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
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
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

	// 收藏微博
	private void favorite(boolean fav) {
		FavoriteWeiboTask favoriteWeiboTask = new FavoriteWeiboTask();
		favoriteWeiboTask.id = status.id;
		favoriteWeiboTask.fav = fav;
		favoriteWeiboTask.doneAndProcess = this;
		Tools.getGlobalObject(this).getWorkQueueStorage()
				.addTask(favoriteWeiboTask);
	}

	// 转发微博
	private void forward() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			String text = "//@" + status.user.name + ":" + status.text;
			intent.putExtra("type", TYPE_FORWARD);
			intent.putExtra("title", "转发微博");
			intent.putExtra("text", text);
			intent.putExtra("status_id", status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
							Toast.LENGTH_SHORT).show();
					return;
				}
				String text2 = "//@" + dataObj.getString("nick") + ":"
						+ dataObj.getString("text");
				intent.putExtra("type", TYPE_FORWARD);
				intent.putExtra("title", "转播微博");
				intent.putExtra("text", text2);
				intent.putExtra("id", dataObj.getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

		startActivity(intent);
	}

	// 转发原微博
	private void retweetdetailForword() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			String text = "//@" + status.retweeted_status.user.name + ":"
					+ status.retweeted_status.text;
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_FORWARD);
			intent.putExtra("title", "转发微博");
			intent.putExtra("text", text);
			intent.putExtra("status_id", status.retweeted_status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
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
				intent.putExtra("title", "转播微博");
				intent.putExtra("text", text2);
				intent.putExtra("id", sourceObj.getString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}

		startActivity(intent);
	}

	// 评论
	private void comment() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_COMMENT);
			intent.putExtra("title", "评论微博");
			intent.putExtra("text", "");
			intent.putExtra("status_id", status.id);
			break;
		case TENCENT:
			try {
				if (dataObj == null) {
					Toast.makeText(SingleWeiboViewer.this, "请等待加载完成",
							Toast.LENGTH_SHORT).show();
					return;
				}
				intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
				intent.putExtra("type", TYPE_COMMENT);
				intent.putExtra("title", "评论微博");
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
				} else { // 显示头像
					profileImage.setImageDrawable(cachedImage);
				}
				// 显示昵称
				userName.setText(dataObj.getString("nick"));
				// 微博内容开始
				String origtext = dataObj.getString("origtext");

				SpannableString spannableString = new SpannableString(origtext);
				statusText.setText(spannableString);
				source.setText("来自" + dataObj.getString("from"));
				if (dataObj.getInt("type") != 2) { // 不是转载而来的
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
						imageUrl = retweetStatuImage.optString(0) + "/460";// 为什么加/460，腾讯规定的，支持160，2000，460还有一些，记不住了
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
				JSONArray imageArray = dataObj.optJSONArray("image"); // 如果此微博有图片内容，就显示出来
				if (imageArray != null && imageArray.length() > 0) {
					statusImage.setVisibility(View.VISIBLE);
					System.out.println(imageArray.optString(0));
					imageUrl = imageArray.optString(0) + "/460";// 为什么加/460，腾讯规定的，支持160，2000，460还有一些，记不住了
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
				forwardButton.setText(dataObj.getString("count")); // 被转发次数
				commentButton.setText(dataObj.getString("mcount")); // 被评论次数
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
			// 重新将微博数据装载到status中
			JSONAndObject.convertSingleObject(status, response);
			Message msg = new Message();
			msg.obj = status;
			// 使用Handler刷新控件中的数据
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
						ToastUtil.showLongToast(SingleWeiboViewer.this, "收藏成功");
						favorite.setVisibility(View.GONE);
						unfavorite.setVisibility(View.VISIBLE);
					} else {
						favorite.setVisibility(View.VISIBLE);
						unfavorite.setVisibility(View.GONE);
						ToastUtil.showLongToast(SingleWeiboViewer.this,
								"取消收藏成功");
					}
				} else {
					if (isFav) {
						ToastUtil.showLongToast(SingleWeiboViewer.this, "收藏失败");
					} else {
						ToastUtil.showLongToast(SingleWeiboViewer.this,
								"取消收藏失败");
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

}
