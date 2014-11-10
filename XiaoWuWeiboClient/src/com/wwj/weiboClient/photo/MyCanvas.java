package com.wwj.weiboClient.photo;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义画布
 * 
 * @author Administrator
 * 
 */
public class MyCanvas extends View {

	public MyCanvas(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);

		canvas.drawLine(1, new Random().nextInt(200), 300, 300, paint);
		super.onDraw(canvas);
	}
}
