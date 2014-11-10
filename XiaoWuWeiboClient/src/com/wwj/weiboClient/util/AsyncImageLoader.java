package com.wwj.weiboClient.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

/**
 * 异步加载图片
 * 
 * @author wwj
 * 
 */
public class AsyncImageLoader {
	// SoftReference是软引用，是为了更好的为了系统回收变量
	private HashMap<String, SoftReference<Drawable>> imageCache;

	public AsyncImageLoader() {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}

	public Drawable loadDrawable(final String imageUrl,
			final ImageView imageView, final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			// 从缓存中获取
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageView,
						imageUrl);
			}
		};

		// 建立一个新的线程下载图片
		new Thread() {
			public void run() {
				Drawable drawable = null;
				try {
					drawable = ImageUtil.geRoundDrawableFromUrl(imageUrl, 20);
					// drawable = ImageUtil.getDrawableFromUrl(imageUrl);
				} catch (Exception e) {
					e.printStackTrace();
				}
				imageCache.put(imageUrl, new SoftReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);

			};
		}.start();
		return null;
	}

	// 回调接口
	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, ImageView imageView,
				String imageUrl);
	}
}
