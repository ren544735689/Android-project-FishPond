package com.example.android_forest_app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.example.android_forest_app.R;

public class processBar extends View {
    private Paint paint;
    private int roundColor;
    private int roundProgressColor;
    private int circleBackgroundColor;
    private float roundWidth;
    private int oldx,oldy;
    private int cx,cy;
    private int max;        //max progress
    private int progress;   //current progress
    private int textColor;  //center progress text color
    private float textSize; //center progress text size
    private float pointRadius;
    private float pointWidth;
    private Drawable mThumb, mThumbPress;

    private static boolean flagLock = false;
    private progressCallback mProgressCallback;

    public processBar(Context context) {
        this(context, null);
    }
    public processBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public processBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
        //获取自定义属性和默认值
        roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
        roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
        circleBackgroundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_circleBackGround, Color.YELLOW);
        roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 3);
        textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.WHITE);
        textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
        max = mTypedArray.getInteger(R.styleable.RoundProgressBar_imageMax, 7200);
        //init, 7200 = 7200* (120min/120min)
        progress = 7200;
        pointRadius = mTypedArray.getDimension(R.styleable.RoundProgressBar_pointRadius, 3);
        pointWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_pointWidth, 2);
        mTypedArray.recycle();
        // 加载拖动图标
        mThumb = getResources().getDrawable(R.drawable.press_up);// 圆点图片
        int thumbHalfheight = mThumb.getIntrinsicHeight() / 2;
        int thumbHalfWidth = mThumb.getIntrinsicWidth() / 2;
        mThumb.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);
        mThumbPress = getResources().getDrawable(R.drawable.press_down);// 圆点图片
        thumbHalfheight = mThumbPress.getIntrinsicHeight() / 2;
        thumbHalfWidth = mThumbPress.getIntrinsicWidth() / 2;
        mThumbPress.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);
        paddingOuterThumb = thumbHalfheight;
    }
    @Override
    public void onDraw(Canvas canvas) {
        //draw the circle background
//        paint.setColor(circleBackgroundColor);
//        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);  //消除锯齿
//        canvas.drawCircle(centerX, centerY, radius-roundWidth/2, paint);

        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);  //用于定义的圆弧的形状和大小的界限
        PointF progressPoint = ChartUtil.calcArcEndPointXY(centerX, centerY, radius, 360 * progress / max, 270);
        if(!flagLock) {
            // 画最外层的大圆环
            paint.setColor(roundColor); //设置圆环的颜色
            paint.setStyle(Paint.Style.STROKE); //设置空心
            paint.setStrokeWidth(roundWidth); //设置圆环的宽度
            canvas.drawCircle(centerX, centerY, radius, paint); //画出圆环

            //画圆弧 ，画圆环的进度
            paint.setStrokeWidth(roundWidth + 2); //设置圆环的宽度
            paint.setColor(roundProgressColor);  //设置进度的颜色
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(oval, 270, 360 * progress / max, false, paint);  //根据进度画圆弧
            // 画圆上的两个点
            paint.setStrokeWidth(pointWidth);
            // PointF startPoint = ChartUtil.calcArcEndPointXY(centerX, centerY, radius, 0, 270);
            // canvas.drawCircle(startPoint.x, startPoint.y, pointRadius, paint);
            //paint.setStyle(Paint.Style.FILL_AND_STROKE);
           //canvas.drawCircle(progressPoint.x, progressPoint.y, pointRadius, paint);
        }

        //画文字
        paint.setStrokeWidth(0);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        // paint.setTypeface(Typeface.DEFAULT); //设置字体
        String textTime = getTimeText(progress);
        float textWidth = paint.measureText(textTime);   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
//        canvas.drawText(textTime, centerX - textWidth / 2, 160 + radius + centerY + textSize/2, paint);

        // 画Thumb
        canvas.save();
        canvas.translate(progressPoint.x, progressPoint.y);
        if(!flagLock) {
            if (downOnArc) {
                mThumbPress.draw(canvas);
            } else {
                mThumb.draw(canvas);
            }
        }
        canvas.restore();
    }
    private boolean downOnArc = false;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();
        if(!flagLock) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (isTouchArc(x, y)) {
                        downOnArc = true;
                        updateArc(x, y);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (downOnArc) {
                        updateArc(x, y);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    downOnArc = false;
                    invalidate();
                    if (changeListener != null) {
                        changeListener.onProgressChangeEnd(max, progress);
                    }
                    break;
            }
        }
        return super.onTouchEvent(event);
    }
    private int centerX, centerY;
    private int radius;
    private int paddingOuterThumb;
    @Override
    protected void onSizeChanged(int width, int height, int oldw, int oldh) {
        centerX = width / 2;
        centerY = height / 2;
        int minCenter = Math.min(centerX, centerY);
        radius = (int) (minCenter - roundWidth/2 - paddingOuterThumb)*3/4; //圆环的半径
        minValidateTouchArcRadius = (int) (radius - paddingOuterThumb*1.5f);
        maxValidateTouchArcRadius = (int) (radius + paddingOuterThumb*1.5f);
        super.onSizeChanged(width, height, oldw, oldh);
    }
    // 根据点的位置，更新进度
    private void updateArc(int x, int y) {
        cx = x - getWidth() / 2;
        cy = y - getHeight() / 2;
        // 计算角度，得出（-1->1）之间的数据，等同于（-180°->180°）
        double oldAngle = Math.atan2(oldy, oldx)/Math.PI;
        double angle = Math.atan2(cy, cx)/Math.PI;
        Log.d("oldAngle:",oldAngle+"");
        Log.d("Angle:",angle+"");
        // 将角度转换成（0->2）之间的值，然后加上90°的偏移量
        if(oldAngle<=-0.5 && angle>-0.5 && angle < 0.5){
            angle = 2;
        }
        else{
            angle = ((2 + angle)%2 + (90/180f))%2;
            oldx = cx;
            oldy = cy;
        }

        int timeBlock = (int)(angle * 120 / 2);
        if(timeBlock <=10)
            timeBlock = 5;
        if(timeBlock <=117)
            timeBlock = timeBlock/5*5+5>120 ? 120 : timeBlock/5*5+5;
        else
            timeBlock = 120;
        Log.d("time:",timeBlock+"");
        // 用（0->2）之间的角度值乘以总进度，等于当前进度
        progress = (int) (timeBlock * max/120);
        if (changeListener != null) {
            changeListener.onProgressChange(max, progress);
        }
        invalidate();
        mProgressCallback.updateListener(progress);
    }

    //progress callback
    public interface progressCallback{
        public void updateListener(int _progress);
    }
    public void setProgressCallback(progressCallback progressCallback){
        this.mProgressCallback = progressCallback;
    }

    private int minValidateTouchArcRadius; // 最小有效点击半径
    private int maxValidateTouchArcRadius; // 最大有效点击半径
    // 判断是否按在圆边上
    private boolean isTouchArc(int x, int y) {
        double d = getTouchRadius(x, y);
        if (d >= minValidateTouchArcRadius && d <= maxValidateTouchArcRadius) {
            return true;
        }
        return false;
    }
    // 计算某点到圆点的距离
    private double getTouchRadius(int x, int y) {
        int cx = x - getWidth() / 2;
        int cy = y - getHeight() / 2;
        return Math.hypot(cx, cy);
    }
    public String getTimeText(int progress) {
        int minute = progress / 60;
        int second = progress % 60;
        String result = (minute < 10 ? "0" : "") + minute + ":" + (second < 10 ? "0" : "") + second;
        return result;
    }
    public synchronized int getMax() {
        return max;
    }
    /**
     * 设置进度的最大值
     * @param max
     */
    public synchronized void setMax(int max) {
        if(max < 0){
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }
    /**
     * 获取进度.需要同步
     * @return
     */
    public synchronized int getProgress() {
        return progress;
    }
    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if(progress < 0){
            throw new IllegalArgumentException("progress not less than 0");
        }
        if(progress > max){
            progress = max;
        }
        if(progress <= max){
            this.progress = progress;
            postInvalidate();
        }
    }
    public int getCricleColor() {
        return roundColor;
    }
    public void setCricleColor(int cricleColor) {
        this.roundColor = cricleColor;
    }
    public int getCricleProgressColor() {
        return roundProgressColor;
    }
    public void setCricleProgressColor(int cricleProgressColor) {
        this.roundProgressColor = cricleProgressColor;
    }
    public float getRoundWidth() {
        return roundWidth;
    }
    public void setRoundWidth(float roundWidth) {
        this.roundWidth = roundWidth;
    }
    public static class ChartUtil {
        /**
         * 依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
         * @param cirX
         * @param cirY
         * @param radius
         * @param cirAngle
         * @return
         */
        public static PointF calcArcEndPointXY(float cirX, float cirY, float radius, float cirAngle){
            float posX = 0.0f;
            float posY = 0.0f;
            //将角度转换为弧度
            float arcAngle = (float) (Math.PI * cirAngle / 180.0);
            if (cirAngle < 90)
            {
                posX = cirX + (float)(Math.cos(arcAngle)) * radius;
                posY = cirY + (float)(Math.sin(arcAngle)) * radius;
            }
            else if (cirAngle == 90)
            {
                posX = cirX;
                posY = cirY + radius;
            }
            else if (cirAngle > 90 && cirAngle < 180)
            {
                arcAngle = (float) (Math.PI * (180 - cirAngle) / 180.0);
                posX = cirX - (float)(Math.cos(arcAngle)) * radius;
                posY = cirY + (float)(Math.sin(arcAngle)) * radius;
            }
            else if (cirAngle == 180)
            {
                posX = cirX - radius;
                posY = cirY;
            }
            else if (cirAngle > 180 && cirAngle < 270)
            {
                arcAngle = (float) (Math.PI * (cirAngle - 180) / 180.0);
                posX = cirX - (float)(Math.cos(arcAngle)) * radius;
                posY = cirY - (float)(Math.sin(arcAngle)) * radius;
            }
            else if (cirAngle == 270)
            {
                posX = cirX;
                posY = cirY - radius;
            }
            else if(cirAngle > 270 && cirAngle < 360)
            {
                arcAngle = (float) (Math.PI * (360 - cirAngle) / 180.0);
                posX = cirX + (float)(Math.cos(arcAngle)) * radius;
                posY = cirY - (float)(Math.sin(arcAngle)) * radius;
            }
            else if(cirAngle >= 360){
                posX = cirX + radius;
                posY = cirY;
            }
            return new PointF(posX, posY);
        }
        public static PointF calcArcEndPointXY(float cirX, float cirY, float radius, float cirAngle, float orginAngle){
            if(orginAngle + cirAngle < 630)
                cirAngle = (orginAngle + cirAngle) % 360;
            else
                cirAngle = orginAngle;
            return calcArcEndPointXY(cirX, cirY, radius, cirAngle);
        }
    }
    private OnProgressChangeListener changeListener;
    public void setChangeListener(OnProgressChangeListener changeListener) {
        this.changeListener = changeListener;
    }
    public interface OnProgressChangeListener {
        void onProgressChange(int duration, int progress);
        void onProgressChangeEnd(int duration, int progress);
    }

    public boolean getflagLock(){
        return flagLock;
    }
    public void setflagLock(boolean flagLock){
        this.flagLock = flagLock;
    }
}
