package com.example.heartpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import java.util.ArrayList;

public class MyService extends WallpaperService {
    Bitmap bitmap;
    Bitmap res;
    private String path;

    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.zhy.livewallpaper";

    public MyService() {
    }


    public static void ImagePath(Context context, String data){
        Intent intent = new Intent(MyService.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra("ImagePath",data);
        context.sendBroadcast(intent);
    }

    public static void setToWallPaper(Context context){
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(context,MyService.class));
        context.startActivity(intent);
    }


    @Override
    public Engine onCreateEngine() {

        return new MyEnfine();
    }


    class MyEnfine extends Engine {

        private static final int DIS_SOLP = 80;
        private ArrayList<Wave> wList;
        private boolean mVisible;
        protected boolean isRuning = false;
        private BroadcastReceiver mImageReceiver;


        private class Wave {
            int cx;
            int cy;
            Paint p;
        }

        // 定义画笔

        // 定义一个Handler
        Handler mHandler = new Handler();
        // 定义一个周期性执行的任务
        private final Runnable drawTarget = new Runnable() {
            public void run() {
                // 动态地绘制图形
                drawFrame();
            }
        };


        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);
            registerReceiver(mImageReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                        path = intent.getStringExtra("ImagePath");
                }
            },intentFilter);
            if (path == null){
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_back);
            }else {
                bitmap = BitmapFactory.decodeFile(path);
            }

            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            int width2 = wm.getDefaultDisplay().getWidth();
            int height2 = wm.getDefaultDisplay().getHeight();
            int width1 = bitmap.getWidth();
            int height1 = bitmap.getHeight();
            float scaleWight = ((float)width2)/width1;
            float scaleHeight = ((float)height2)/height1;
            Matrix matrix = new Matrix();
            matrix.preScale(scaleWight,scaleHeight);
            res = Bitmap.createBitmap(bitmap,0,0,width1,height1,matrix,true);

            wList = new ArrayList<>();
            // 初始化画笔
            // 设置处理触摸事件
            setTouchEventsEnabled(true);


        }


        @Override
        public void onDestroy() {
            unregisterReceiver(mImageReceiver);
            super.onDestroy();
            // 删除回调
            mHandler.removeCallbacks(drawTarget);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            mVisible = visible;
            // 当界面可见时候，执行drawFrame()方法。
            if (visible && isRuning) {
                // 动态地绘制图形
                drawFrame();
            } else {
                // 如果界面不可见，删除回调
                mHandler.removeCallbacks(drawTarget);
            }
        }


        public void onTouchEvent(MotionEvent event) {
            // 如果检测到滑动操作
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    addPoint(x, y);
                    break;
                default:
            }
            super.onTouchEvent(event);
        }


        private void addPoint(int x, int y) {
            if (wList.size() == 0) {
                addPoint2(x, y);
                isRuning = true;
                drawFrame();
            } else {
                Wave w = wList.get(wList.size() - 1);
                if (Math.abs(w.cx - x) > DIS_SOLP || Math.abs(w.cy - y) > DIS_SOLP) {
                    addPoint2(x, y);
                }
            }
        }

        private void addPoint2(int x, int y) {
            Wave w = new Wave();
            w.cx = x;
            w.cy = y;
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL);
            w.p = p;
            wList.add(w);
        }

        // 定义绘制图形的工具方法
        private void drawFrame() {
            // 获取该壁纸的SurfaceHolder
            final SurfaceHolder holder = getSurfaceHolder();
            Canvas c = null;
            try {
                // 对画布加锁
                c = holder.lockCanvas();
                if (c != null) {
                    isRuning = true;
                    c.save();
                    // 绘制背景色
                    Paint paint1 = new Paint();
                    paint1.setAlpha(255);
                    paint1.setAntiAlias(true);
                    paint1.setStyle(Paint.Style.FILL);
                    c.drawBitmap(res,0,0,paint1);
                    // 在触碰点绘制圆圈
                    drawTouchPoint(c);
                    for (int i = 0; i < wList.size(); i++) {
                        Wave w = wList.get(i);
                        int alpha = w.p.getAlpha();
                        if (alpha == 0) {
                            wList.remove(i);
                            //删除i以后，总数会减少一，否则会漏掉一个对象
                            i--;
                            continue;
                        }
                        alpha -= 10;
                        if (alpha < 10) {
                            alpha = 0;
                        }
                        w.p.setAlpha(alpha);
                    }
                    if (wList.size() == 0) {
                        isRuning = false;
                    }


                }
            } finally {
                if (c != null)
                    holder.unlockCanvasAndPost(c);
            }
            mHandler.removeCallbacks(drawTarget);
            // 调度下一次重绘
            if (mVisible && isRuning) {


                // 指定0.1秒后重新执行mDrawCube一次
                mHandler.postDelayed(drawTarget,50);
            }
        }

        // 在屏幕触碰点绘制圆圈
        private void drawTouchPoint(Canvas c) {
            for (int i = 0; i < wList.size(); i++) {
                Wave wave = wList.get(i);
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.mipmap.heart_icon);
                c.drawBitmap(bitmap1,wave.cx,wave.cy,wave.p);

            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);


            Canvas ca = holder.lockCanvas();
            Paint paint1 = new Paint();
            paint1.setAlpha(255);
            paint1.setAntiAlias(true);
            paint1.setStyle(Paint.Style.FILL);
            ca.drawBitmap(res,0,0,paint1);
            holder.unlockCanvasAndPost(ca);
        }

    }

}