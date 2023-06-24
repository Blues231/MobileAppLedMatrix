package com.example.proiectlicenta;

import static com.example.proiectlicenta.PixelArt.paint_brush;
import static com.example.proiectlicenta.PixelArt.path;
import static com.example.proiectlicenta.MainActivity.sendImageBuffer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class display extends View {
    int line , col;
    public static ArrayList<Rect> rectList = new ArrayList<Rect>();
    public static ArrayList<Integer> colorList = new ArrayList<>();
    public static int current_brush = Color.BLACK;
    public display(Context context) {
        super(context);
    }

    public display(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public display(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //this function handles the touching of the painting area
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //get the current position we are touching
        float x = event.getX();
        float y = event.getY();

        //make sure the position is withing the limits of the canvas
        if((x >= 0) && (x <=1024) && (y >= 0) && (y <=1024))
        {
            //if we just press once
            if (event.getAction() == MotionEvent.ACTION_DOWN)
            {

                //identify in which of the 256 squares we are touching
                col = (int) (x / 65);
                line = (int) (y / 65);

                //create the square
                Rect rect = new Rect();
                rect.left = col * 65;
                rect.top = line * 65;
                rect.right = rect.left + 65;
                rect.bottom = rect.top + 65;

                //break the color in the red , green and blue parts
                int rgb = current_brush & 0x00FFFFFF;

                byte r = (byte) ((rgb & 0xFF0000) >> 16);
                byte g = (byte) ((rgb & 0x00FF00) >> 8);
                byte b = (byte) (rgb & 0x0000FF);

                //save the parts in the sandImageBuffer
                sendImageBuffer[(line * 64) + (col * 3) + 6] = r;
                sendImageBuffer[(line * 64) + (col * 3) + 7] = g;
                sendImageBuffer[(line * 64) + (col * 3) + 8] = b;

                //save the square and it's color
                rectList.add(rect);
                colorList.add(current_brush);

                postInvalidate();
                return true;
            }
            else if (event.getAction() == MotionEvent.ACTION_MOVE)
            {
                //if it's a moving motion do the same for all squares touched

                //identify in which of the 256 squares we are touching
                col = (int) (x / 65);
                line = (int) (y / 65);

                //create the square
                Rect rect = new Rect();
                rect.left = col * 65;
                rect.top = line * 65;
                rect.right = rect.left + 65;
                rect.bottom = rect.top + 65;

                //break the color in the red , green and blue parts
                int rgb = current_brush & 0x00FFFFFF;

                byte r = (byte) ((rgb & 0xFF0000) >> 16);
                byte g = (byte) ((rgb & 0x00FF00) >> 8);
                byte b = (byte) (rgb & 0x0000FF);

                //save the parts in the sandImageBuffer
                sendImageBuffer[(line * 64) + (col * 3) + 6] = r;
                sendImageBuffer[(line * 64) + (col * 3) + 7] = g;
                sendImageBuffer[(line * 64) + (col * 3) + 8] = b;

                //save the square and it's color
                rectList.add(rect);
                colorList.add(current_brush);

                postInvalidate();
                return true;
            }
            else
            {
                //do nothing
            }
        }
        return false;
    }

    //this function handles the appearing of the drawing on the canvas
    @Override
    protected void onDraw(Canvas canvas) {

        for(int i = 0; i<rectList.size();i++)
        {
            paint_brush.setColor(colorList.get(i));
            canvas.drawRect(rectList.get(i),paint_brush);
            invalidate();
        }
    }
}
