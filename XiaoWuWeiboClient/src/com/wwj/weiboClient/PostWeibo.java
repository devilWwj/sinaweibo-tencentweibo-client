package com.wwj.weiboClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.weibo.sdk.android.model.ModelResult;
import com.tencent.weibo.sdk.android.network.HttpCallback;
import com.wwj.weiboClient.adapters.SinaFaceListAdapter;
import com.wwj.weiboClient.adapters.TencentFaceListAdapter;
import com.wwj.weiboClient.interfaces.Const;
import com.wwj.weiboClient.library.SinaFaceMan;
import com.wwj.weiboClient.library.TencentFaceMan;
import com.wwj.weiboClient.manager.StorageManager;
import com.wwj.weiboClient.manager.TencentWeiboManager;
import com.wwj.weiboClient.util.StringUtil;
import com.wwj.weiboClient.util.ToastUtil;
import com.wwj.weiboClient.util.Tools;
import com.wwj.weiboClient.workqueue.task.CommentWeiboTask;
import com.wwj.weiboClient.workqueue.task.PostWeiboTask;
import com.wwj.weiboClient.workqueue.task.RepostWeiboTask;

/**
 * 【发布微博】
 * 
 * @author wwj
 * 
 */
public class PostWeibo extends Activity implements Const, OnClickListener,
		OnTouchListener, OnItemClickListener {

	private Button btn_back; // 返回
	private Button btn_send; // 发送
	private EditText weiboContent; // 微博内容
	private ImageView minPicViewer; // 微博图片
	private TextView postWeiboTitle; // 微博标题栏
	private View inputBoard; // 输入板（表情）
	private ImageView insertAtButton; // 插入@
	private ImageView insertLocationButton; // 插入位置
	private ImageView insertFaceButton; // 插入表情
	private ImageView insertPicButton; // 插入图片
	private ImageView insertTopicButton; // 插入主题
	private View isCommentView; // 评论
	private CheckBox isCommentCheckBox; // 同时作为评论
	private CheckBox postWeiboCheckBox; // 同为发送一条微博
	private GridView faceList; // 表情列表
	private Bitmap bitmap; // 图片
	private static String filename; // 文件名（这里是图片)
	private int type; // 发布微博类型
	private long statusId; // 状态Id
	private String title; // 标题
	private String text; // 发送的文本

	private int weiboType; // 微博类型
	private String accessToken; // 授权token
	private String id;// 转播微博Id

	private TextView textLength; // 字数
	private static final int MAX_COUNT = 140;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_weibo);

		findViews();
		setListener();

		bitmap = (Bitmap) getLastNonConfigurationInstance();
		if (bitmap != null) {
			minPicViewer.setVisibility(View.VISIBLE);
			minPicViewer.setImageBitmap(bitmap);
		}
		accessToken = StorageManager.getValue(this,
				StringUtil.TENCENT_ACCESS_TOKEN, "");

		// 获得Intent 传过来的参数
		weiboType = getIntent().getIntExtra(StringUtil.WEIBO_TYPE, 0);
		statusId = getIntent().getLongExtra("status_id", 0);
		id = getIntent().getStringExtra("id");
		type = getIntent().getIntExtra("type", TYPE_POST_WEIBO);
		title = getIntent().getStringExtra("title");
		text = getIntent().getStringExtra("text");

		// 表情网格视图填充数据
		setFaceListAdapter();

		viewSetting();
	}

	private void setFaceListAdapter() {
		switch (weiboType) {
		case SINA:
			faceList.setAdapter(new SinaFaceListAdapter(this));
			break;
		case TENCENT:
			faceList.setAdapter(new TencentFaceListAdapter(this));
			break;
		}
	}

	/** 加载视图 **/
	private void findViews() {
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_send = (Button) findViewById(R.id.btn_send);
		weiboContent = (EditText) findViewById(R.id.et_weibo_content);
		minPicViewer = (ImageView) findViewById(R.id.iv_insert_pic_min_viewer);
		postWeiboTitle = (TextView) findViewById(R.id.tv_post_weibo_title);
		inputBoard = findViewById(R.id.fl_input_board);
		faceList = (GridView) findViewById(R.id.gv_face_list);
		insertAtButton = (ImageView) findViewById(R.id.btn_insert_at);
		insertLocationButton = (ImageView) findViewById(R.id.btn_insert_location);
		insertFaceButton = (ImageView) findViewById(R.id.btn_insert_face);
		insertPicButton = (ImageView) findViewById(R.id.btn_insert_pic);
		insertTopicButton = (ImageView) findViewById(R.id.btn_insert_topic);
		isCommentView = findViewById(R.id.fl_is_comment);
		isCommentCheckBox = (CheckBox) findViewById(R.id.cb_is_comment);
		postWeiboCheckBox = (CheckBox) findViewById(R.id.cb_post_weibo);

		textLength = (TextView) findViewById(R.id.remain_count);

	}

	private int editStart;
	private int editEnd;

	private void setListener() {
		btn_back.setOnClickListener(this);
		btn_send.setOnClickListener(this);
		insertAtButton.setOnClickListener(this);
		insertLocationButton.setOnClickListener(this);
		insertFaceButton.setOnClickListener(this);
		insertPicButton.setOnClickListener(this);
		insertTopicButton.setOnClickListener(this);
		faceList.setOnItemClickListener(this);
		minPicViewer.setOnClickListener(this);

		weiboContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				editStart = weiboContent.getSelectionStart();
				editEnd = weiboContent.getSelectionEnd();
//				String content = s.toString();
//				textLength.setText(getWordCount(content));
				
				weiboContent.removeTextChangedListener(this);  
				  
	            // 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度  
	            // 因为是中英文混合，单个字符而言，calculateLength函数都会返回1  
	            while (calculateLength(s.toString()) > MAX_COUNT) { // 当输入字符个数超过限制的大小时，进行截断操作  
	                s.delete(editStart - 1, editEnd);  
	                editStart--;  
	                editEnd--;  
	            }  
	            // mEditText.setText(s);将这行代码注释掉就不会出现后面所说的输入法在数字界面自动跳转回主界面的问题了，多谢@ainiyidiandian的提醒  
	            weiboContent.setSelection(editStart);  
	  
	            // 恢复监听器  
	            weiboContent.addTextChangedListener(this);  
	  
	            setLeftCount();  
			}
		});
	}

	// 界面显示设置
	private void viewSetting() {
		switch (type) {
		case TYPE_POST_WEIBO: // 发布微博
			break;
		case TYPE_FORWARD: // 转发微博
			insertPicButton.setVisibility(View.GONE);
			isCommentView.setVisibility(View.VISIBLE);
			postWeiboCheckBox.setVisibility(View.GONE);
			isCommentCheckBox.setVisibility(View.VISIBLE);
			if (title != null) {
				postWeiboTitle.setText(title);
			}
			if (text != null) {
				weiboContent.setText(Tools.changeTextToFace(this,
						Html.fromHtml(Tools.atBlue(text))));
				weiboContent.getText().insert(0, " ");
				weiboContent.setSelection(0, 1);
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.showSoftInput(weiboContent, InputMethodManager.SHOW_FORCED);
			}
			break;
		case TYPE_COMMENT: // 评论微博
			insertPicButton.setVisibility(View.GONE);
			isCommentView.setVisibility(View.VISIBLE);
			postWeiboCheckBox.setVisibility(View.VISIBLE);
			isCommentCheckBox.setVisibility(View.GONE);
			if (title != null) {
				postWeiboTitle.setText(title);
			}
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.gv_face_list:
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					TencentFaceMan.getFaceResourceId(position));
			ImageSpan imageSpan = null;
			switch (weiboType) {
			case SINA:
				imageSpan = new ImageSpan(this,
						SinaFaceMan.getFaceResourceId(position));
				break;
			case TENCENT:
				imageSpan = new ImageSpan(this,
						TencentFaceMan.getFaceResourceId(position));
				break;
			}

			String faceText = TencentFaceMan.getFaceText(position);
			SpannableString spannableString = new SpannableString(faceText);
			spannableString.setSpan(imageSpan, 0, faceText.length(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			weiboContent.getText().insert(weiboContent.getSelectionStart(),
					spannableString);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (v.getId()) {
		case R.id.et_weibo_content:
			inputBoard.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return bitmap;
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		// 获得系统输入法
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		switch (v.getId()) {
		case R.id.btn_back: // 返回
			finish();
			break;
		case R.id.btn_send: // 发布
			send();
			break;
		case R.id.btn_insert_pic: // 插入图片
			showDialog();
			break;
		case R.id.btn_insert_location: // 插入位置
			Toast.makeText(PostWeibo.this, "这个功能小巫还没弄哦..", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.btn_insert_topic: // 插入主题
			String topicText = "请输入主题";
			weiboContent.getText().insert(weiboContent.getSelectionStart(),
					"#" + topicText + "#");
			weiboContent.setSelection(weiboContent.getSelectionStart()
					- topicText.length() - 1,
					weiboContent.getSelectionStart() - 1);
			imm.showSoftInput(weiboContent, InputMethodManager.SHOW_FORCED);
			break;
		case R.id.btn_insert_at: // 插入@
			String atText = "请输入用户名";
			weiboContent.getText().insert(weiboContent.getSelectionStart(),
					"@" + atText + " ");
			weiboContent.setSelection(weiboContent.getSelectionStart() - 1
					- atText.length(), weiboContent.getSelectionStart() - 1);
			imm.showSoftInput(weiboContent, InputMethodManager.SHOW_FORCED);
			break;
		case R.id.btn_insert_face: // 插入表情
			if (inputBoard.getVisibility() == View.GONE) {
				insertFaceButton
						.setImageResource(R.drawable.btn_insert_keyboard);
				inputBoard.setVisibility(View.VISIBLE);
				faceList.setVisibility(View.VISIBLE);
			} else {
				insertFaceButton.setImageResource(R.drawable.btn_insert_face);
				inputBoard.setVisibility(View.GONE);
				faceList.setVisibility(View.GONE);
			}

			break;
		case R.id.iv_insert_pic_min_viewer: // 图片浏览
			intent = new Intent(PostWeibo.this, PictureViewer.class);
			intent.putExtra("filename", filename);
			startActivityForResult(intent, CODE_REQUEST_PICTURE_VIEWER);
			break;

		default:
			break;

		}
	}

	private void showDialog() {
		final Dialog dialog = new Dialog(PostWeibo.this, R.style.MyDialog);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.insert_pic, null);
		dialog.setContentView(view);
		Button takePic = (Button) view.findViewById(R.id.take_pic);
		Button selectPic = (Button) view.findViewById(R.id.select_pic);
		takePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CODE_REQUEST_CAPTURE_IMAGE);
				dialog.dismiss();
			}
		});
		selectPic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, CODE_PHOTO_LIBRARY);
				dialog.dismiss();
			}
		});
		dialog.show();
	}

	// 发送
	private void send() {
		String text = weiboContent.getText().toString();
		int length = text.length();
		if (length > 140) {
			ToastUtil.showLongToast(this, "超出字数请删除些字符");
			return;
		}
		switch (weiboType) {
		case SINA:
			sinaSend(text);
			break;
		case TENCENT:
			tencentSend(text);
			break;
		}

	}

	// 发布新浪微博
	private void sinaSend(String text) {
		switch (type) {
		case TYPE_POST_WEIBO: // 发送微博
			// 创建一个发布微博的任务
			PostWeiboTask postWeiboTask = new PostWeiboTask();
			postWeiboTask.text = text;
			postWeiboTask.file = filename;
			if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			Tools.getGlobalObject(this).getWorkQueueStorage()
					.addTask(postWeiboTask);
			Toast.makeText(this, "已经提交发布微博任务到工作队列", Toast.LENGTH_LONG).show();
			break;
		case TYPE_FORWARD: // 转发微博
			if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			RepostWeiboTask repostWeiboTask = new RepostWeiboTask();
			repostWeiboTask.id = statusId;
			repostWeiboTask.text = text;
			if (isCommentCheckBox.isChecked()) { // 是否评论
				repostWeiboTask.isComment = 1;
			} else {
				repostWeiboTask.isComment = 0;
			}
			Tools.getGlobalObject(this).getWorkQueueStorage()
					.addTask(repostWeiboTask);
			Toast.makeText(this, "已经提交转发微博任务到工作队列", Toast.LENGTH_LONG).show();
			break;
		case TYPE_COMMENT:
			if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			CommentWeiboTask commentWeiboTask = new CommentWeiboTask();
			commentWeiboTask.text = text;
			commentWeiboTask.weiboText = text + this.text;
			commentWeiboTask.id = statusId;

			if (postWeiboCheckBox.isChecked()) {
				commentWeiboTask.postWeibo = true;
			} else {
				commentWeiboTask.postWeibo = false;
			}
			Tools.getGlobalObject(this).getWorkQueueStorage()
					.addTask(commentWeiboTask);
			// 返回结果
			setResult(TYPE_COMMENT);
			Toast.makeText(this, "已经提交评论微博任务到工作队列", Toast.LENGTH_LONG);
			break;
		}
		finish();
	}

	// 发布腾讯微博
	private void tencentSend(String text) {
		switch (type) {
		case TYPE_POST_WEIBO:
			if (filename != null && "".equals(text)) {
				text = "分享图片";
			} else if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			if (bitmap != null) { // 发布带图片微博
				TencentWeiboManager.addPicWeibo(this, accessToken, text,
						bitmap, weiboCallback);
				return;
			}
			TencentWeiboManager
					.addWeibo(this, accessToken, text, weiboCallback);
			break;
		case TYPE_FORWARD:
			if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			TencentWeiboManager.reAddWeibo(this, accessToken, text, id,
					weiboCallback);
			if (isCommentCheckBox.isChecked()) { // 转播同时评论
				TencentWeiboManager.commentWeibo(this, accessToken, text, id,
						weiboCallback);
			}
			break;
		case TYPE_COMMENT:
			if ("".equals(text)) {
				Toast.makeText(this, "请输入微博内容", Toast.LENGTH_LONG).show();
				return;
			}
			TencentWeiboManager.commentWeibo(this, accessToken, text, id,
					weiboCallback);
			if (postWeiboCheckBox.isChecked()) { // 评论同时转播微博
				TencentWeiboManager.reAddWeibo(this, accessToken, text, id,
						weiboCallback);
			}
			break;
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		filename = null;
		super.onDestroy();
	}

	private File mFile;
	private Uri mImageUri;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CODE_REQUEST_CAPTURE_IMAGE: // 照相机
			switch (resultCode) {
			case Activity.RESULT_OK: // 确定键
				minPicViewer.setVisibility(View.VISIBLE); // 设置图片可见
				bitmap = (Bitmap) data.getExtras().get("data"); // 获取图片数据
				minPicViewer.setImageBitmap(bitmap); // 显示图片
				filename = StorageManager.saveBitmap(bitmap);
				break;
			default:
				break;
			}
			break;
		case CODE_REQUEST_PICTURE_VIEWER: // 来自查看图片的返回结果
			switch (resultCode) {
			case CODE_RESULT_REMOVE: // 删除
				filename = null;
				bitmap = null;
				minPicViewer.setImageBitmap(null);
				minPicViewer.setVisibility(View.GONE);
				break;
			case CODE_RESULT_RETURN: // 返回键
				if (data != null) {
					filename = data.getStringExtra("filename"); // 得到文件名
					bitmap = BitmapFactory.decodeFile(filename);
					minPicViewer.setImageBitmap(bitmap);
				}
				break;

			default:
				break;
			}
		case CODE_PHOTO_LIBRARY: // 相册选择
			if (resultCode == RESULT_OK) {
				mImageUri = data.getData();
				getPic(mImageUri);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	// 得到图片
	private void getPic(Uri uri) {
		mImageUri = uri;
		if (uri.getScheme().equals("content")) {
			mFile = new File(getRealPathFromURI(mImageUri));
		} else {
			mFile = new File(mImageUri.getPath());
		}
		bitmap = createThumbnailBitmap(mImageUri, 400);
		minPicViewer.setVisibility(View.VISIBLE); // 设置图片可见
		minPicViewer.setImageBitmap(bitmap);
		filename = StorageManager.saveBitmap(bitmap);
	}

	/**
	 * 创建缩略图
	 * 
	 * @param uri
	 * @param size
	 * @return
	 */
	private Bitmap createThumbnailBitmap(Uri uri, int size) {
		InputStream input = null;
		try {
			input = getContentResolver().openInputStream(uri);
			;
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, options);
			input.close();

			int scale = 1;
			while ((options.outWidth / scale > size)
					|| (options.outHeight / scale > size)) {
				scale *= 2;
			}

			options.inJustDecodeBounds = false;
			options.inSampleSize = scale;

			input = getContentResolver().openInputStream(uri);
			return BitmapFactory.decodeStream(input, null, options);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private String getWordCount(String content) {
		if (content == null || content.length() == 0) {
			return "0/140";
		}
		int mod = content.length() / 140;
		return content.length() + "/" + ((mod + 1) * 140);
	}

	private HttpCallback weiboCallback = new HttpCallback() {

		@Override
		public void onResult(Object obj) {
			ModelResult result = (ModelResult) obj;
			if (result == null) {
				return;
			}
			String jsonResult = result.getObj().toString();
			try {
				JSONObject dataObj = new JSONObject(jsonResult);
				if ("ok".equals(dataObj.getString("msg"))) {
					switch (type) {
					case TYPE_POST_WEIBO:
						ToastUtil.showLongToast(PostWeibo.this, "发送成功");
						break;
					case TYPE_FORWARD:
						ToastUtil.showLongToast(PostWeibo.this, "转播成功");
						break;
					case TYPE_COMMENT:
						ToastUtil.showLongToast(PostWeibo.this, "评论成功");
						break;
					}
					finish();
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 计算分享内容的字数，一个汉字=两个英文字母，一个中文标点=两个英文标点 注意：该函数的不适用于对单个字符进行计算，因为单个字符四舍五入后都是1
	 * 
	 * @param c
	 * @return
	 */
	private long calculateLength(CharSequence c) {
		double len = 0;
		for (int i = 0; i < c.length(); i++) {
			int tmp = (int) c.charAt(i);
			if (tmp > 0 && tmp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
	}

	/**
	 * 刷新剩余输入字数,最大值新浪微博是140个字，人人网是200个字
	 */
	private void setLeftCount() {
		textLength.setText(String.valueOf((MAX_COUNT - getInputCount())));
	}

	/**
	 * 获取用户输入的分享内容字数
	 * 
	 * @return
	 */
	private long getInputCount() {
		return calculateLength(weiboContent.getText().toString());
	}

}
