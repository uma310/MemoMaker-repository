package com.example.memomaker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

public class CustomImageView extends androidx.appcompat.widget.AppCompatImageView {

    public CustomImageView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CustomImageView(Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CustomImageView(Context context, final AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init();
    }
    private Bitmap mBitmap;
    private Context context;
    private float touchPointX;
    private float touchPointY;
    private float mLastScaleFactor = 1.0f;
    private Matrix bitmapMatrix = new Matrix();
    private Paint mPaint = new Paint();
    private ScaleGestureDetector mScaleGestureDetector;
    private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            // ピンチイン/アウト開始
            // タッチの座標を記録
            touchPointX = detector.getFocusX();
            touchPointY = detector.getFocusY();
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            // ピンチイン/アウト終了
            super.onScaleEnd(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // ピンチイン/アウト中(毎フレーム呼ばれる)
            // 縮尺を計算
            mLastScaleFactor = detector.getScaleFactor();
            // マトリックスに加算(縦と横を同じように拡大縮小する)
            bitmapMatrix.postScale(mLastScaleFactor, mLastScaleFactor, touchPointX, touchPointY);
            // Viewを再読み込み(onDrawの発火)
            invalidate();
            super.onScale(detector);
            return true;
        }
    };

    private GestureDetector mGestureDetector;
    private GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // スクロールしたとき
            if (e1.getPointerId(0) == e2.getPointerId(0)) {
                // 開始地点と終了地点の指が同じ(一回も途切れなかった)なら
                // 画像を移動
                bitmapMatrix.postTranslate(-distanceX, -distanceY);
                // Viewを再読み込み(onDrawの発火)
                invalidate();
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onDoubleTap (MotionEvent e) {
            // ダブルタッチしたとき
            // 縮尺、移動をすべてリセット
            bitmapMatrix.reset();
            // Viewを再読み込み(onDrawの発火)
            invalidate();
            return super.onDoubleTap(e);
        }
    };

    private void init () {
        // Gestureたちの設定
        mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetector(context, mSimpleOnGestureListener);
    }
    @Override
    public boolean onTouchEvent (MotionEvent event) {
        // すべてのGestureはここを通る
        return mGestureDetector.onTouchEvent(event) || mScaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //すべてのmatrixを適応
        canvas.save();
        canvas.drawBitmap(mBitmap, bitmapMatrix, mPaint);
        canvas.restore();
    }

    public void setBitmap(Bitmap bitmap) {
        // 外部から画像を取り入れる。
        this.mBitmap = bitmap;
    }

}
