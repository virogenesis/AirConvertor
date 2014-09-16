package com.digitaldesign.dragantest.customViews;

import com.digitaldesign.dragantest.interfaces.OnReceiveValue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class InstrumentView extends View{

	private Paint linija;
	//private int vrednost = 0;
	private int vrednost = 0;
	
	//private OnReceiveValue onReceiveValueCallback = null;

	
	public InstrumentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public InstrumentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public InstrumentView(Context context) {
		super(context);
		init();
	}

	private void init(){
		linija = new Paint();
		linija.setColor(Color.WHITE);
		linija.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float croppedLength = (float) (getMeasuredHeight() * 8 / 19);	// > getMeasuredWidth()/2 ? getMeasuredWidth()/2 : getMeasuredHeight(); 
		
		linija.setStrokeWidth (croppedLength /50);		// Width of Line
		float croppedHeight = getMeasuredHeight();
		float croppedWidth = getMeasuredWidth();

		float LeftX = (croppedWidth - croppedHeight) / 4;
		float RightX = croppedWidth - LeftX;
		float DownY = croppedHeight;

		float startX = croppedWidth / 2 + 6;
		float startY = croppedHeight / 2 - 6;
		
		float stopY = startY - (float) Math.sin((double) vrednost/256*3.14*1.3-0.5) * croppedLength;
		float stopX = startX - (float) Math.cos((double) vrednost/256*3.14*1.3-0.5) * croppedLength;

//		Math.sqrt(67);
		
		Paint p = new Paint();
		// smooths
		p.setAntiAlias(true);
		p.setColor(Color.BLACK);
		p.setStyle(Paint.Style.STROKE); 
		// opacity
		//p.setAlpha(0x80); //

//------------------------------------------------- Zelene kockice
		p.setStrokeWidth(croppedHeight /50);
		p.setColor(Color.parseColor("#005000"));
		if (vrednost < 120){p.setColor(Color.parseColor("#00FFFF"));}
		canvas.drawLine(LeftX, DownY * 89/100, LeftX, DownY * 80/100, p);
		if (vrednost < 121){p.setColor(Color.parseColor("#10FFFF"));}
		canvas.drawLine(LeftX, DownY * 79/100, LeftX, DownY * 70/100, p);
		if (vrednost < 122){p.setColor(Color.parseColor("#40FFF8"));}
		canvas.drawLine(LeftX, DownY * 69/100, LeftX, DownY * 60/100, p);
		if (vrednost < 123){p.setColor(Color.parseColor("#70FFF0"));}
		canvas.drawLine(LeftX, DownY * 59/100, LeftX, DownY * 50/100, p);
		if (vrednost < 124){p.setColor(Color.parseColor("#80FF90"));}
		canvas.drawLine(LeftX, DownY * 49/100, LeftX, DownY * 40/100, p);
		if (vrednost < 125){p.setColor(Color.parseColor("#90FF80"));}
		canvas.drawLine(LeftX, DownY * 39/100, LeftX, DownY * 30/100, p);
		if (vrednost < 126){p.setColor(Color.parseColor("#F0FF70"));}
		canvas.drawLine(LeftX, DownY * 29/100, LeftX, DownY * 20/100, p);
		if (vrednost < 127){p.setColor(Color.parseColor("#F8FF40"));}
		canvas.drawLine(LeftX, DownY * 19/100, LeftX, DownY * 10/100, p);
		if (vrednost < 128){p.setColor(Color.parseColor("#FFFF10"));}
		canvas.drawLine(LeftX, DownY * 9/100, LeftX, DownY * 0/100, p);
//------------------------------------------------- Crvene kockice
		p.setColor(Color.parseColor("#500000"));
		if (vrednost > 136){p.setColor(Color.parseColor("#FF00FF"));}
		canvas.drawLine(RightX, DownY * 89/100, RightX, DownY * 80/100, p);
		if (vrednost > 135){p.setColor(Color.parseColor("#FF00CF"));}
		canvas.drawLine(RightX, DownY * 79/100, RightX, DownY * 70/100, p);
		if (vrednost > 134){p.setColor(Color.parseColor("#FF00A0"));}
		canvas.drawLine(RightX, DownY * 69/100, RightX, DownY * 60/100, p);
		if (vrednost > 133){p.setColor(Color.parseColor("#FF1080"));}
		canvas.drawLine(RightX, DownY * 59/100, RightX, DownY * 50/100, p);
		if (vrednost > 132){p.setColor(Color.parseColor("#FF2040"));}
		canvas.drawLine(RightX, DownY * 49/100, RightX, DownY * 40/100, p);
		if (vrednost > 131){p.setColor(Color.parseColor("#FF4020"));}
		canvas.drawLine(RightX, DownY * 39/100, RightX, DownY * 30/100, p);
		if (vrednost > 130){p.setColor(Color.parseColor("#FF8010"));}
		canvas.drawLine(RightX, DownY * 29/100, RightX, DownY * 20/100, p);
		if (vrednost > 129){p.setColor(Color.parseColor("#FFA000"));}
		canvas.drawLine(RightX, DownY * 19/100, RightX, DownY * 10/100, p);
		if (vrednost > 128){p.setColor(Color.parseColor("#FFFF00"));}
		canvas.drawLine(RightX, DownY * 9/100, RightX, DownY * 0/100, p);
//------------------------------------------------- Bele linije
		p.setColor(Color.WHITE);
		p.setStrokeWidth(croppedHeight /10);
//------------------------------------------------- Bela skazaljka
		canvas.drawLine(startX, startY, stopX, stopY, linija);
//------------------------------------------------- Poklopac kazaljke
		p.setStrokeWidth(croppedHeight /16);
		p.setColor(Color.BLACK);
		canvas.drawCircle(startX, startY, 1, p);
	}
	/**
	 * Pomeranje skale
	 */
	
	public void setValue(int value){
	//public void setValue(byte value){
		vrednost = value;
		
		/*  Necemo vise slati vrednost nazad.
		if(onReceiveValueCallback != null){
			onReceiveValueCallback.onValue(value);
		}
		*/
		invalidate();
	}
	/*
	public void registerValueCallback(OnReceiveValue obj){
		onReceiveValueCallback = obj;
	}
	public void clearValueCallback(){
		onReceiveValueCallback = null;
	}
	*/
	//public int getValue(){
	public int getValue(){
		return vrednost;
	}

}


