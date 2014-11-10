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
 * ������΢����
 * 
 * @author wwj
 * 
 */
public class PostWeibo extends Activity implements Const, OnClickListener,
		OnTouchListener, OnItemClickListener {

	private Button btn_back; // ����
	private Button btn_send; // ����
	private EditText weiboContent; // ΢������
	private ImageView minPicViewer; // ΢��ͼƬ
	private TextView postWeiboTitle; // ΢��������
	private View inputBoard; // ����壨���飩
	private ImageView insertAtButton; // ����@
	private ImageView insertLocationButton; // ����λ��
	private ImageView insertFaceButton; // �������
	private ImageView insertPicButton; // ����ͼƬ
	private ImageView insertTopicButton; // ��������
	private View isCommentView; // ����
	private CheckBox isCommentCheckBox; // ͬʱ��Ϊ����
	private CheckBox postWeiboCheckBox; // ͬΪ����һ��΢��
	private GridView faceList; // �����б�
	private Bitmap bitmap; // ͼƬ
	private static String filename; // �ļ�����������ͼƬ)
	private int type; // ����΢������
	private long statusId; // ״̬Id
	private String title; // ����
	private String text; // ���͵��ı�

	private int weiboType; // ΢������
	private String accessToken; // ��Ȩtoken
	private String id;// ת��΢��Id

	private TextView textLength; // ����
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

		// ���Intent �������Ĳ���
		weiboType = getIntent().getIntExtra(StringUtil.WEIBO_TYPE, 0);
		statusId = getIntent().getLongExtra("status_id", 0);
		id = getIntent().getStringExtra("id");
		type = getIntent().getIntExtra("type", TYPE_POST_WEIBO);
		title = getIntent().getStringExtra("title");
		text = getIntent().getStringExtra("text");

		// ����������ͼ�������
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

	/** ������ͼ **/
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
				  
	            // ע������ֻ��ÿ�ζ�������EditText�������󳤶ȣ����ܶ�ɾ���ĵ����ַ��󳤶�  
	            // ��Ϊ����Ӣ�Ļ�ϣ������ַ����ԣ�calculateLength�������᷵��1  
	            while (calculateLength(s.toString()) > MAX_COUNT) { // �������ַ������������ƵĴ�Сʱ�����нضϲ���  
	                s.delete(editStart - 1, editEnd);  
	                editStart--;  
	                editEnd--;  
	            }  
	            // mEditText.setText(s);�����д���ע�͵��Ͳ�����ֺ�����˵�����뷨�����ֽ����Զ���ת��������������ˣ���л@ainiyidiandian������  
	            weiboContent.setSelection(editStart);  
	  
	            // �ָ�������  
	            weiboContent.addTextChangedListener(this);  
	  
	            setLeftCount();  
			}
		});
	}

	// ������ʾ����
	private void viewSetting() {
		switch (type) {
		case TYPE_POST_WEIBO: // ����΢��
			break;
		case TYPE_FORWARD: // ת��΢��
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
		case TYPE_COMMENT: // ����΢��
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
		// ���ϵͳ���뷨
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		switch (v.getId()) {
		case R.id.btn_back: // ����
			finish();
			break;
		case R.id.btn_send: // ����
			send();
			break;
		case R.id.btn_insert_pic: // ����ͼƬ
			showDialog();
			break;
		case R.id.btn_insert_location: // ����λ��
			Toast.makeText(PostWeibo.this, "�������С�׻�ûŪŶ..", Toast.LENGTH_SHORT)
					.show();
			break;
		case R.id.btn_insert_topic: // ��������
			String topicText = "����������";
			weiboContent.getText().insert(weiboContent.getSelectionStart(),
					"#" + topicText + "#");
			weiboContent.setSelection(weiboContent.getSelectionStart()
					- topicText.length() - 1,
					weiboContent.getSelectionStart() - 1);
			imm.showSoftInput(weiboContent, InputMethodManager.SHOW_FORCED);
			break;
		case R.id.btn_insert_at: // ����@
			String atText = "�������û���";
			weiboContent.getText().insert(weiboContent.getSelectionStart(),
					"@" + atText + " ");
			weiboContent.setSelection(weiboContent.getSelectionStart() - 1
					- atText.length(), weiboContent.getSelectionStart() - 1);
			imm.showSoftInput(weiboContent, InputMethodManager.SHOW_FORCED);
			break;
		case R.id.btn_insert_face: // �������
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
		case R.id.iv_insert_pic_min_viewer: // ͼƬ���
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

	// ����
	private void send() {
		String text = weiboContent.getText().toString();
		int length = text.length();
		if (length > 140) {
			ToastUtil.showLongToast(this, "����������ɾ��Щ�ַ�");
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

	// ��������΢��
	private void sinaSend(String text) {
		switch (type) {
		case TYPE_POST_WEIBO: // ����΢��
			// ����һ������΢��������
			PostWeiboTask postWeiboTask = new PostWeiboTask();
			postWeiboTask.text = text;
			postWeiboTask.file = filename;
			if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
				return;
			}
			Tools.getGlobalObject(this).getWorkQueueStorage()
					.addTask(postWeiboTask);
			Toast.makeText(this, "�Ѿ��ύ����΢�����񵽹�������", Toast.LENGTH_LONG).show();
			break;
		case TYPE_FORWARD: // ת��΢��
			if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
				return;
			}
			RepostWeiboTask repostWeiboTask = new RepostWeiboTask();
			repostWeiboTask.id = statusId;
			repostWeiboTask.text = text;
			if (isCommentCheckBox.isChecked()) { // �Ƿ�����
				repostWeiboTask.isComment = 1;
			} else {
				repostWeiboTask.isComment = 0;
			}
			Tools.getGlobalObject(this).getWorkQueueStorage()
					.addTask(repostWeiboTask);
			Toast.makeText(this, "�Ѿ��ύת��΢�����񵽹�������", Toast.LENGTH_LONG).show();
			break;
		case TYPE_COMMENT:
			if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
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
			// ���ؽ��
			setResult(TYPE_COMMENT);
			Toast.makeText(this, "�Ѿ��ύ����΢�����񵽹�������", Toast.LENGTH_LONG);
			break;
		}
		finish();
	}

	// ������Ѷ΢��
	private void tencentSend(String text) {
		switch (type) {
		case TYPE_POST_WEIBO:
			if (filename != null && "".equals(text)) {
				text = "����ͼƬ";
			} else if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
				return;
			}
			if (bitmap != null) { // ������ͼƬ΢��
				TencentWeiboManager.addPicWeibo(this, accessToken, text,
						bitmap, weiboCallback);
				return;
			}
			TencentWeiboManager
					.addWeibo(this, accessToken, text, weiboCallback);
			break;
		case TYPE_FORWARD:
			if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
				return;
			}
			TencentWeiboManager.reAddWeibo(this, accessToken, text, id,
					weiboCallback);
			if (isCommentCheckBox.isChecked()) { // ת��ͬʱ����
				TencentWeiboManager.commentWeibo(this, accessToken, text, id,
						weiboCallback);
			}
			break;
		case TYPE_COMMENT:
			if ("".equals(text)) {
				Toast.makeText(this, "������΢������", Toast.LENGTH_LONG).show();
				return;
			}
			TencentWeiboManager.commentWeibo(this, accessToken, text, id,
					weiboCallback);
			if (postWeiboCheckBox.isChecked()) { // ����ͬʱת��΢��
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
		case CODE_REQUEST_CAPTURE_IMAGE: // �����
			switch (resultCode) {
			case Activity.RESULT_OK: // ȷ����
				minPicViewer.setVisibility(View.VISIBLE); // ����ͼƬ�ɼ�
				bitmap = (Bitmap) data.getExtras().get("data"); // ��ȡͼƬ����
				minPicViewer.setImageBitmap(bitmap); // ��ʾͼƬ
				filename = StorageManager.saveBitmap(bitmap);
				break;
			default:
				break;
			}
			break;
		case CODE_REQUEST_PICTURE_VIEWER: // ���Բ鿴ͼƬ�ķ��ؽ��
			switch (resultCode) {
			case CODE_RESULT_REMOVE: // ɾ��
				filename = null;
				bitmap = null;
				minPicViewer.setImageBitmap(null);
				minPicViewer.setVisibility(View.GONE);
				break;
			case CODE_RESULT_RETURN: // ���ؼ�
				if (data != null) {
					filename = data.getStringExtra("filename"); // �õ��ļ���
					bitmap = BitmapFactory.decodeFile(filename);
					minPicViewer.setImageBitmap(bitmap);
				}
				break;

			default:
				break;
			}
		case CODE_PHOTO_LIBRARY: // ���ѡ��
			if (resultCode == RESULT_OK) {
				mImageUri = data.getData();
				getPic(mImageUri);
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);

	}

	// �õ�ͼƬ
	private void getPic(Uri uri) {
		mImageUri = uri;
		if (uri.getScheme().equals("content")) {
			mFile = new File(getRealPathFromURI(mImageUri));
		} else {
			mFile = new File(mImageUri.getPath());
		}
		bitmap = createThumbnailBitmap(mImageUri, 400);
		minPicViewer.setVisibility(View.VISIBLE); // ����ͼƬ�ɼ�
		minPicViewer.setImageBitmap(bitmap);
		filename = StorageManager.saveBitmap(bitmap);
	}

	/**
	 * ��������ͼ
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
						ToastUtil.showLongToast(PostWeibo.this, "���ͳɹ�");
						break;
					case TYPE_FORWARD:
						ToastUtil.showLongToast(PostWeibo.this, "ת���ɹ�");
						break;
					case TYPE_COMMENT:
						ToastUtil.showLongToast(PostWeibo.this, "���۳ɹ�");
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
	 * ����������ݵ�������һ������=����Ӣ����ĸ��һ�����ı��=����Ӣ�ı�� ע�⣺�ú����Ĳ������ڶԵ����ַ����м��㣬��Ϊ�����ַ������������1
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
	 * ˢ��ʣ����������,���ֵ����΢����140���֣���������200����
	 */
	private void setLeftCount() {
		textLength.setText(String.valueOf((MAX_COUNT - getInputCount())));
	}

	/**
	 * ��ȡ�û�����ķ�����������
	 * 
	 * @return
	 */
	private long getInputCount() {
		return calculateLength(weiboContent.getText().toString());
	}

}
