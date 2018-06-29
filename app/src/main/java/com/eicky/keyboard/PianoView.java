package com.eicky.keyboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eicky
 * @Todo:
 * @date: 2018-06-29 17:00:30
 * @version: V1.0
 */
public class PianoView extends View {
    // 白色按键个数
    public static final int WHITE_KEYS_COUNT = 7;
    //黑色按键个数
    public static final int BLACK_KEYS_COUNT = 5;

    //黑色按钮的宽比
    public static final float BLACK_TO_WHITE_WIDTH_RATIO = 0.625f;
    //黑色按钮的高比
    public static final float BLACK_TO_WHITE_HEIGHT_RATIO = 0.58f;

    //默认的颜色和按下的颜色(白色按下、黑色按下)
    private Paint mWhiteKeyPaint, mWhiteKeyHitPaint, mBlackKeyPaint, mBlackKeyHitPaint, mWhiteStrokePaint;

    private List<Rect> whiteKeyRect = new ArrayList<>(WHITE_KEYS_COUNT);
    private List<Rect> blackKeyRect = new ArrayList<>(BLACK_KEYS_COUNT);
    private List<Rect> downRect = new ArrayList<>();
    private Map<Integer, Point> downPoint = new HashMap<>();

    private List<Integer> notes = new ArrayList<>();

    public PianoView(Context context) {
        this(context, null);
    }

    public PianoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PianoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setupPaints();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    private void setupPaints() {
        mWhiteKeyPaint = new Paint();
        mWhiteKeyPaint.setStyle(Paint.Style.FILL);
        mWhiteKeyPaint.setColor(Color.WHITE);
        mWhiteKeyPaint.setAntiAlias(true);

        mWhiteStrokePaint = new Paint();
        mWhiteStrokePaint.setStyle(Paint.Style.STROKE);
        mWhiteStrokePaint.setColor(Color.BLACK);
        mWhiteStrokePaint.setStrokeWidth(1);
        mWhiteStrokePaint.setAntiAlias(true);

        mWhiteKeyHitPaint = new Paint(mWhiteKeyPaint);
        mWhiteKeyHitPaint.setColor(Color.LTGRAY);
        mWhiteKeyHitPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mBlackKeyPaint = new Paint();
        mBlackKeyPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBlackKeyPaint.setColor(Color.BLACK);
        mBlackKeyPaint.setAntiAlias(true);

        mBlackKeyHitPaint = new Paint(mBlackKeyPaint);
        mBlackKeyHitPaint.setColor(Color.DKGRAY);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //获取屏幕大小
        int width = getWidth();
        int height = getHeight();
        //计算每个按键的大小
        int whiteKeyWidth = width / WHITE_KEYS_COUNT;
        int blackKeyWidth = (int) (whiteKeyWidth * BLACK_TO_WHITE_WIDTH_RATIO);
        int blackKeyHeight = (int) (height * BLACK_TO_WHITE_HEIGHT_RATIO);
        for (int i = 0; i < WHITE_KEYS_COUNT; i++) {
            Rect rect = new Rect();
            rect.set(i * whiteKeyWidth, 0, (i + 1) * whiteKeyWidth, height);
            whiteKeyRect.add(rect);
        }

        for (int j = 0; j < BLACK_KEYS_COUNT; j++) {
            Rect rect = new Rect();
            //第几个黑键
            int remain = j % 5;
            //第几组黑键
            int group = j / 5;

            int keyLeft = 0, keyTop = 0, keyRight = 0, keyBottom = 0;

            if (remain == 0) {
                keyLeft = group * 7 * whiteKeyWidth + 1 * whiteKeyWidth - (blackKeyWidth / 2);
            } else if (remain == 1) {
                keyLeft = group * 7 * whiteKeyWidth + 2 * whiteKeyWidth - (blackKeyWidth / 2);
            } else if (remain == 2) {
                keyLeft = group * 7 * whiteKeyWidth + 4 * whiteKeyWidth - (blackKeyWidth / 2);
            } else if (remain == 3) {
                keyLeft = group * 7 * whiteKeyWidth + 5 * whiteKeyWidth - (blackKeyWidth / 2);
            } else if (remain == 4) {
                keyLeft = group * 7 * whiteKeyWidth + 6 * whiteKeyWidth - (blackKeyWidth / 2);
            }

            keyRight = keyLeft + blackKeyWidth;
            keyBottom = blackKeyHeight;
            rect.set(keyLeft, keyTop, keyRight, keyBottom);
            blackKeyRect.add(rect);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制白色按键
        for (int i = 0; i < whiteKeyRect.size(); i++) {
            Rect whiteRect = whiteKeyRect.get(i);
            Paint paint = mWhiteKeyPaint;
            if (downRect.contains(whiteRect)) {
                paint = mWhiteKeyHitPaint;
            }
            canvas.drawRect(whiteRect, mWhiteStrokePaint);
            canvas.drawRect(whiteRect, paint);
        }

        //绘制黑色按键，这里黑色按键会在白色按键上面
        for (int j = 0; j < blackKeyRect.size(); j++) {
            Rect blackRect = blackKeyRect.get(j);
            Paint paint = mBlackKeyPaint;
            if (downRect.contains(blackRect)) {
                paint = mBlackKeyHitPaint;
            }
            canvas.drawRect(blackRect, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        ////有多个手指头点击屏幕，在此判断。
        ////如果当前手指数量大于设置的最大数量
        //int cappedPointerCount = pointerCount > MAX_FINGERS ? MAX_FINGERS : pointerCount;


        //获取在屏幕上手指的个数
        int pointerCount = event.getPointerCount();
        //获取该事件是哪个指针(手指)产生的
        int actionIndex = event.getActionIndex();
        //与getAction()类似，多点触控需要使用这个方法获取事件类型
        int action = event.getActionMasked();
        //获取一个指针(手指)的唯一标识，在手指按下和抬起之间ID始终
        int id = event.getPointerId(actionIndex);

        //检查是否收到了手指的按下或者抬起的动作
        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)) {
            Point point = new Point((int) event.getX(actionIndex), (int) event.getY(actionIndex));
            downPoint.put(id, point);
        } else if ((action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP)) {
            downPoint.remove(id);
        }

        downRect.clear();
        for (Map.Entry<Integer, Point> entry : downPoint.entrySet()) {
            int pointerId = entry.getKey();
            int pointerIndex = event.findPointerIndex(pointerId);
            Point point = new Point((int) event.getX(pointerIndex), (int) event.getY(pointerIndex));
            downPoint.put(pointerId, point);
            Rect rect = getToneForPoint(point);
            if (rect != null)
                downRect.add(rect);
        }

        invalidate();
        return true;
    }

    //黑键优先
    private Rect getToneForPoint(Point point) {
        for (int i = 0; i < blackKeyRect.size(); i++) {
            Rect rect = blackKeyRect.get(i);
            if (rect.contains(point.x, point.y)) {
                return rect;
            }
        }

        for (int i = 0; i < whiteKeyRect.size(); i++) {
            Rect rect = whiteKeyRect.get(i);
            if (rect.contains(point.x, point.y)) {
                return rect;
            }
        }

        return null;
    }
}
