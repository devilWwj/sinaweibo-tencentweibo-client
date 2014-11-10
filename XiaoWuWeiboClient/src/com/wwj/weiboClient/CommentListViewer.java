package com.wwj.weiboClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.SinaCommentListAdapter;
import com.wwj.weiboClient.adapters.TencentCommentListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.JSONAndObject;
import com.wwj.weiboClient.manager.SinaWeiboManager;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.model.Comment;
import com.wwj.weiboClient.util.LogUtils;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.view.PullToRefreshListView;
import com.wwj.weiboClient.view.PullToRefreshListView.OnRefreshListener;

/**
 * 【查看评论列表】
 * 
 * @author wwj
 * 
 */
public class CommentListViewer extends Activity implements Const,
		OnClickListener, OnRefreshListener {

	private List<Comment> comments;
	private SinaCommentListAdapter commentListAdapter;
	private PullToRefreshListView commentListView;
	private Button commentButton;
	private TextView titleTextView;
	private Button backButton;
	private TextView nocontentTv;
	private long statusId;
	private String text;
	private String title;
	private String rootid;

	private int weiboType;
	private String accessToken;
	// 腾讯微博评论列表数据
	private List<JSONObject> commentList = new ArrayList<JSONObject>();
	private JSONArray jsonArray;
	private TencentCommentListAdapter tencentCommentListAdapter;

	private View loadingView;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (comments.size() == 0) {
				Toast.makeText(CommentListViewer.this, "暂时没有评论",
						Toast.LENGTH_LONG).show();
			} else {
				nocontentTv.setVisibility(View.GONE);
				commentListAdapter = new SinaCommentListAdapter(
						CommentListViewer.this, comments);
				commentListView.setAdapter(commentListAdapter);
			}
			SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm");
			String date = format.format(new Date());
			commentListView.onRefreshComplete(date);
			loadingView.setVisibility(View.GONE);
			super.handleMessage(msg);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtils.d("### CommentListViewer onCreated");
		setContentView(R.layout.comment_list_viewer);
		commentListView = (PullToRefreshListView) findViewById(R.id.lv_comment);
		commentButton = (Button) findViewById(R.id.btn_comment);
		backButton = (Button) findViewById(R.id.btn_back);
		titleTextView = (TextView) findViewById(R.id.tv_title);
		nocontentTv = (TextView) findViewById(R.id.nocontent_tv);
		statusId = getIntent().getLongExtra("status_id", 0);
		rootid = getIntent().getStringExtra("rootid");
		text = getIntent().getStringExtra("text");
		title = getIntent().getStringExtra("title");

		if (title != null) {
			titleTextView.setText(title);
		}
		if (statusId != 0) {
			if (commentListAdapter == null) {
				SinaWeiboManager.getComments(this, statusId,
						commentRequestListener);
			} else {
				commentListView.setAdapter(commentListAdapter);
			}
		} else {
			accessToken = StorageManager.getValue(this,
					StringUtil.TENCENT_ACCESS_TOKEN, "");
			TencentWeiboManager.getReAddList(this, accessToken, rootid,
					mCallback);
		}

		backButton.setOnClickListener(this);
		commentButton.setOnClickListener(this);
		commentListView.setOnRefreshListener(this);
		loadingView = (View) findViewById(R.id.lodingprogress);
		loadingView.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case TYPE_COMMENT:
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_comment:
			comment();
			break;
		}
	}

	private void comment() {
		Intent intent = new Intent(this, PostWeibo.class);
		switch (weiboType) {
		case SINA:
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_COMMENT);
			intent.putExtra("title", "评论微博");
			intent.putExtra("text", "");
			intent.putExtra("status_id", statusId);
			break;
		case TENCENT:
			intent.putExtra(StringUtil.WEIBO_TYPE, weiboType);
			intent.putExtra("type", TYPE_COMMENT);
			intent.putExtra("title", "评论微博");
			intent.putExtra("text", "");
			intent.putExtra("id", rootid);
		}
		startActivity(intent);
	}

	private RequestListener commentRequestListener = new RequestListener() {

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
			LogUtils.v(response);
			if (commentListAdapter == null) {
				if (statusId != 0) {
					comments = (List<Comment>) JSONAndObject.convert(
							Comment.class, response, "comments");
				} else {
					comments = (List<Comment>) JSONAndObject.convert(
							Comment.class, response, null);
				}
			}
			handler.sendEmptyMessage(0);
		}
	};

	@Override
	public void onRefresh() {
		switch (weiboType) {
		case SINA:
			SinaWeiboManager
					.getComments(this, statusId, commentRequestListener);
			break;
		case TENCENT:
			TencentWeiboManager.getReAddList(this, accessToken, rootid,
					mCallback);
			break;
		}
	}

	private HttpCallback mCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null) {
				return;
			}
			String jsonRestult = result.getObj().toString();
			LogUtils.v(jsonRestult);

			try {
				JSONObject jsonObject = new JSONObject(jsonRestult);
				JSONObject dataObject = jsonObject.getJSONObject("data");
				jsonArray = dataObject.getJSONArray("info");
				int length = jsonArray.length();
				for (int i = 0; i < length; i++) {
					JSONObject infoObject = jsonArray.optJSONObject(i);
					commentList.add(infoObject);
				}
				if (commentList.size() != 0) {
					nocontentTv.setVisibility(View.GONE);
				}
				tencentCommentListAdapter = new TencentCommentListAdapter(
						CommentListViewer.this, commentList);
				commentListView.setAdapter(tencentCommentListAdapter);

			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				SimpleDateFormat format = new SimpleDateFormat(
						"yyyy年MM月dd日  HH:mm");
				String date = format.format(new Date());
				commentListView.onRefreshComplete(date);
				loadingView.setVisibility(View.GONE);
			}
		}
	};

}
