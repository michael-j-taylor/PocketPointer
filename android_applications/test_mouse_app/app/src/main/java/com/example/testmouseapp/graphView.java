package com.example.testmouseapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class graphView extends View {

    private Paint paint;

    public graphView(Context ctx) {
        super(ctx);

        //create paint and set color
        paint = new Paint();
        paint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        canvas.drawCircle(200,200,100, paint);
    }
}
