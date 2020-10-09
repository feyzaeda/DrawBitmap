package com.example.drawbitmap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnSaveImg,btnLoadImg,btnUndo;
    private ImageView imageResult;

    private Uri imgUri;
    private Bitmap bitmap;
    private Bitmap defaultBitmap = null;
    private Canvas canvas;
    private Paint paint;

    private int px,py;
    private final int SELECT_IMAGE = 1 ;
    private List<Bitmap> actionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoadImg = findViewById(R.id.btnloadimage);
        btnSaveImg = findViewById(R.id.btnsaveimage);
        btnUndo = findViewById(R.id.btnundo);
        imageResult = findViewById(R.id.imgResult);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(10);

        btnLoadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loadImg = new Intent(Intent.ACTION_GET_CONTENT);
                loadImg.setType("image/*");
                startActivityForResult(loadImg, SELECT_IMAGE);
            }
        });
        
        btnSaveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bitmap != null){
                    saveBitmap(bitmap);

                }
            }
        });

        imageResult.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (action){
                    case MotionEvent.ACTION_DOWN:
                        px = x;
                        py = y;
                        drawOnProjectBitmap((ImageView) v,bitmap,px,py,x,y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawOnProjectBitmap((ImageView) v,bitmap,px,py,x,y);
                        px = x;
                        py = y;
                        break;
                    case MotionEvent.ACTION_UP:
                        drawOnProjectBitmap((ImageView) v, bitmap, px, py, x, y);
                        addLastAction(bitmap);
                        break;



                }
                return true;
            }
        });

        btnUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoLastAction();
            }
        });


    }

    public void undoLastAction() {
        if(actionList.size() > 0){
            actionList.remove(actionList.size() - 1);
            if (actionList.size() > 0){
                bitmap = actionList.get(actionList.size() - 1);
            }
            else {
                bitmap = Bitmap.createBitmap(defaultBitmap);
            }

            btnSaveImg.invalidate();


        }
    }

    private void addLastAction(Bitmap bitmap){
        actionList.add(bitmap);
    }

    private void drawOnProjectBitmap(ImageView imgView, Bitmap bitmap,float x0, float y0, float x, float y) {
        if(x<0 || y<0 || x > imgView.getWidth() || y > imgView.getHeight()){
            //outside ImageView
            return;
        }else{

            float ratioWidth = (float)bitmap.getWidth()/(float)imgView.getWidth();
            float ratioHeight = (float)bitmap.getHeight()/(float)imgView.getHeight();

            canvas.drawLine(
                    x0 * ratioWidth,
                    y0 * ratioHeight,
                    x * ratioWidth,
                    y * ratioHeight,
                    paint);
            imageResult.invalidate();
        }
    }

    private void saveBitmap(Bitmap bitmap) {
        File file = Environment.getExternalStorageDirectory();
        File newFile = new File(file,"test.jpg");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(newFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            Toast.makeText(MainActivity.this,"Save Bitmap: " + fileOutputStream.toString(),Toast.LENGTH_LONG).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Something wrong: " + e.getMessage(),Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Something wrong",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap tempBitmap;

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_IMAGE:
                    imgUri = data.getData();

                    try {

                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imgUri));

                        Bitmap.Config config;
                        if (tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }


                        bitmap = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);

                        canvas = new Canvas(bitmap);
                        canvas.drawBitmap(tempBitmap, 0, 0, null);

                        imageResult.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
            }

        }


    }


}
