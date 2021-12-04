 package com.example.memomaker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ExpandImageViewActivity extends AppCompatActivity{
    private  CustomImageView cImageView;
    private int preDx, preDy;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // タイトルバーを非表示にする（setContentViewの前に呼ぶ）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.expand_image_view);
        //setContentView(new CustomImageView(this));
        cImageView = findViewById(R.id.expandImageView);
        Intent intent = getIntent();
        //Bitmap bitmap = intent.getByteArrayExtra(MainActivity.URI_DATA);
        Uri uri = Uri.parse(intent.getStringExtra(MainActivity.URI_DATA));
        if(uri == null){
            Toast.makeText(ExpandImageViewActivity.this, "画像が読み込めませんでした。", Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            cImageView.setBitmap(bitmap);
        }catch (IOException e) {
            Toast.makeText(ExpandImageViewActivity.this, "画像が読み込めませんでした。", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }catch (SecurityException e){
            Toast.makeText(ExpandImageViewActivity.this, "画像が読み込めませんでした。", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        //cImageView.setImageURI(uri);

        //cImageView.setOnTouchListener(this);
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // 拡大した画像をタップするともとの画面に戻る
//                Intent intent = new Intent();
//                setResult(RESULT_OK, intent);
//                finish();
//            }
//        });



        }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        // x,y 位置取得
//        int newDx = (int)event.getRawX();
//        int newDy = (int)event.getRawY();
//
//        switch (event.getAction()) {
//            // タッチダウンでdragされた
//            case MotionEvent.ACTION_MOVE:
//                // ACTION_MOVEでの位置
//                // performCheckを入れろと警告が出るので
//                v.performClick();
//                int dx = cImageView.getLeft() + (newDx - preDx);
//                int dy = cImageView.getTop() + (newDy - preDy);
//                int imgW = dx + cImageView.getWidth();
//                int imgH = dy + cImageView.getHeight();
//
//                // 画像の位置を設定する
//                cImageView.layout(dx, dy, imgW, imgH);
//
//                //String str = "dx="+dx+"\ndy="+dy;
//                //textView.setText(str);
//                Log.d("onTouch","ACTION_MOVE: dx="+dx+", dy="+dy);
//                break;
//            case MotionEvent.ACTION_DOWN:
//                // nothing to do
//                break;
//            case MotionEvent.ACTION_UP:
//                // nothing to do
//                break;
//            default:
//                break;
//        }
//
//        // タッチした位置を古い位置とする
//        preDx = newDx;
//        preDy = newDy;
//
//        return true;
//    }
}