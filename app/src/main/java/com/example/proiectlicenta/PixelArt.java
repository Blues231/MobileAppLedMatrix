package com.example.proiectlicenta;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import static com.example.proiectlicenta.MainActivity.sendImageBuffer;
import static com.example.proiectlicenta.display.colorList;
import static com.example.proiectlicenta.display.current_brush;
import static com.example.proiectlicenta.display.rectList;

import java.util.Arrays;

import yuku.ambilwarna.AmbilWarnaDialog;

public class PixelArt extends Activity {
    Button colorChanger;

    public static Path path = new Path();
    public static Paint paint_brush = new Paint();
    int defaultColor = 0xFFFFFF;
    int result = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pixel_art);

        colorChanger = (Button) findViewById(R.id.colorChanger);

    }
    public void color(View view)
    {
        openColorPicker();
    }

    //this function resets the sendImageBuffer to the inital values
    //and clears the arrays that represent the pixelart
    public void clear(View view)
    {
        Arrays.fill(sendImageBuffer , (byte) 0);
        for(int j =0 ; j<16 ; j++)
        {
            sendImageBuffer[(j*64)] = 'M';
            sendImageBuffer[(j*64)+1] = 'O';
            sendImageBuffer[(j*64)+2] = 'D';
            sendImageBuffer[(j*64)+3] = 'E';
            sendImageBuffer[(j*64)+4] = '4';
            sendImageBuffer[(j*64)+5] = (byte) ('0' + j);
            for(int i = 54 ; i< 64;i++)
            {
                sendImageBuffer[(j*64) + i] = ';';
            }
        }
        sendImageBuffer[1021] = 'F';
        sendImageBuffer[1022] = 'I';
        sendImageBuffer[1023] = 'N';

        rectList.clear();
        colorList.clear();
    }

    //this function handles the color picking
    private void openColorPicker()
    {
        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this , defaultColor , new AmbilWarnaDialog.OnAmbilWarnaListener()
        {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                //set the picked color as the current color
                defaultColor = color;
                paint_brush.setColor(defaultColor);
                currentColor(paint_brush.getColor());
                //change the color of the button to the picked color
                colorChanger.setBackgroundColor(color);


                int rgbColor = color & 0x00FFFFFF;

                if((((rgbColor & 0xFF0000) >> 16 ) > 0x80) || (((rgbColor & 0x00FF00 ) >> 8) > 0x80) || ((rgbColor & 0x0000FF ) > 0x80))
                {
                    //if the picked color is a bright one , set the text to black
                    colorChanger.setTextColor(Color.BLACK);
                }
                else
                {
                    //else set the text color to white
                    colorChanger.setTextColor(Color.WHITE);
                }
            }
        });

        ambilWarnaDialog.show();
    }

    public void currentColor(int c)
    {
        current_brush = c;
        path = new Path();
    }

    //send the result '2' to the main activity so it knows to send the image via bluetooth communication
    public void printImage(View view)
    {
        result = 2;
        Intent intent=new Intent();
        setResult(result , intent);
        finish();
    }

}