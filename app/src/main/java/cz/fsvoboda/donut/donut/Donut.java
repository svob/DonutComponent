package cz.fsvoboda.donut.donut;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cz.fsvoboda.donut.R;


public class Donut extends View {
    // layout & drawing
    private int mCircleColor;
    private int indicatorColor;
    private Paint mCirclePaint;
    private RectF mCircleBounds;
    private RectF mArcBounds;
    private Paint mArcPaint;
    private Paint mIndicatorPaint;
    private Paint mIndicatorPaintFull;
    private float borderWidth;
    private int mArcColor;
    private LinearLayout centerLayout, leftLayout, rightLayout;
    private View currentLayout, rightInnerLayout, leftInnerLayout;
    private List<DonutPage> pages;
    private boolean pageChanged = true;
    private int currentPage;
    private boolean first = true;

    // animation
    private float distanceX = 0;
    private boolean touch;
    private int mMaxValue, mValue;
    private float mAngleEnd, mAngleCurrent;
    private float mSpeed;
    private GestureDetector gestureDetector;
    private long fps;
    private float startAngle;

    private final String TAG = "donut";

    public Donut(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Donut, 0, 0);

        try {
            mCircleColor = array.getColor(R.styleable.Donut_borderColor, Color.LTGRAY);
            mArcColor = array.getColor(R.styleable.Donut_valueColor, Color.GREEN);
            indicatorColor = array.getColor(R.styleable.Donut_indicatorColor, Color.LTGRAY);
            mSpeed = array.getFloat(R.styleable.Donut_animationSpeed, 1000);
            startAngle = array.getFloat(R.styleable.Donut_startAngle, 270);
            borderWidth = array.getFloat(R.styleable.Donut_borderWidth, 5);
        } finally {
            array.recycle();
        }

        init();
    }

    @Override
    protected void onDraw(Canvas canvas)  {
        super.onDraw(canvas);

        long startFrameTime = System.currentTimeMillis();

        canvas.drawCircle(mCircleBounds.width() / 2, mCircleBounds.height() / 2, mCircleBounds.height() / 2 - borderWidth, mCirclePaint);

        float indicatorX = mCircleBounds.width() / 2 - 12 * (pages.size()-1);
        if (pages.size() > 1) {
            for (int i = 0; i < pages.size(); i++) {
                if (currentPage == i)
                    canvas.drawCircle(indicatorX + i * 24, mCircleBounds.height() * 0.85f, 4, mIndicatorPaintFull);
                else
                    canvas.drawCircle(indicatorX + i * 24, mCircleBounds.height() * 0.85f, 4, mIndicatorPaint);
            }
        }

        Path clipPath = new Path();
        clipPath.addCircle(mCircleBounds.width() / 2, mCircleBounds.height() / 2, mCircleBounds.height() / 2 - 20, Path.Direction.CW);
        canvas.clipPath(clipPath);

        centerLayout.measure(canvas.getWidth(), canvas.getHeight());
        centerLayout.layout(0, 0, canvas.getWidth(), canvas.getHeight());
        leftLayout.measure(canvas.getWidth(), canvas.getHeight());
        leftLayout.layout(0, 0, canvas.getWidth(), canvas.getHeight());
        rightLayout.measure(canvas.getWidth(), canvas.getHeight());
        rightLayout.layout(0, 0, canvas.getWidth(), canvas.getHeight());

        if (first) {
            changePage(0);
            first = false;
        }
        canvas.save();

        if (touch) {
            canvas.translate(mCircleBounds.width() - distanceX, 0);
            if (rightInnerLayout != null) {
                canvas.translate(0, (mCircleBounds.height() / 2) - (rightInnerLayout.getHeight() / 2));
                rightLayout.draw(canvas);
                canvas.translate(0, -((mCircleBounds.height() / 2) - (rightInnerLayout.getHeight() / 2)));
            }

            canvas.translate(-mCircleBounds.width(), 0);
            if (leftInnerLayout != null) {
                canvas.translate(-leftInnerLayout.getWidth(), (mCircleBounds.height() / 2) - (leftInnerLayout.getHeight() / 2));
                leftLayout.draw(canvas);
                canvas.translate(leftInnerLayout.getWidth(), -((mCircleBounds.height() / 2) - (leftInnerLayout.getHeight() / 2)));
            }

            canvas.translate((mCircleBounds.width() / 2) - (currentLayout.getWidth() / 2), (mCircleBounds.height() / 2) - (currentLayout.getHeight() / 2));
            centerLayout.draw(canvas);
        } else {
            canvas.translate((mCircleBounds.width() / 2) - (currentLayout.getWidth() / 2), (mCircleBounds.height() / 2) - (currentLayout.getHeight() / 2));
            centerLayout.draw(canvas);
        }
        canvas.restore();

        if (mAngleCurrent < mAngleEnd) {
            if (fps != 0)
                mAngleCurrent += mSpeed / fps;
            canvas.drawArc(mArcBounds, startAngle, mAngleCurrent, false, mArcPaint);
            invalidate();
        } else {
            canvas.drawArc(mArcBounds, startAngle, mAngleEnd, false, mArcPaint);
        }

        long timeThisFrame = System.currentTimeMillis() - startFrameTime;
        if (timeThisFrame > 0) {
            fps = 1000 / timeThisFrame;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xpad = (float)(getPaddingLeft() + getPaddingRight());
        float ypad = (float)(getPaddingTop() + getPaddingBottom());

        float ww = (float)w - xpad;
        float hh = (float)h - ypad;

        float diameter = Math.min(ww, hh);

        mCircleBounds = new RectF(0.0f, 0.0f, diameter, diameter);
        mCircleBounds.offsetTo(getPaddingLeft(), getPaddingRight());
        mArcBounds = new RectF(0 + 20, 0 + 20, diameter - 20, diameter - 20);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int diameter = Math.min(widthSize, heightSize);
        setMeasuredDimension(diameter, diameter);
    }

    private void init() {
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(borderWidth);
        mCirclePaint.setAlpha(50);

        mArcPaint = new Paint();
        mArcPaint.setColor(mArcColor);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(10);

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setColor(indicatorColor);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeWidth(1);

        mIndicatorPaintFull = new Paint();
        mIndicatorPaintFull.setColor(indicatorColor);

        centerLayout = new LinearLayout(getContext());
        leftLayout = new LinearLayout(getContext());
        rightLayout = new LinearLayout(getContext());

        mAngleCurrent = 0;

        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        pages = new ArrayList<DonutPage>();
    }

    public void addPage(DonutPage page) {
        this.pages.add(page);
    }

    private void changePage(int position) {
        if (pages.size() > position) {
            mMaxValue = pages.get(position).getMaxValue();
            mValue = pages.get(position).getValue();
            mAngleEnd = (float) mValue / mMaxValue * 360;
            mAngleCurrent = 0;
            if (pages.get(position).getColor() != 0) {
                mArcPaint.setColor(pages.get(position).getColor());
            } else {
                mArcPaint.setColor(mArcColor);
            }
            currentLayout = pages.get(position).getLayout();
            currentPage = position;
            pageChanged = true;
            centerLayout.removeAllViews();
            leftLayout.removeAllViews();
            rightLayout.removeAllViews();
            centerLayout.addView(currentLayout);
            rightInnerLayout = null;
            leftInnerLayout = null;
            if (pages.size() > position + 1) {
                rightInnerLayout = pages.get(position + 1).getLayout();
                rightLayout.addView(rightInnerLayout);
            }
            if (position > 0) {
                leftInnerLayout = pages.get(position - 1).getLayout();
                leftLayout.addView(leftInnerLayout);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(distanceX) > mCircleBounds.width() / 3) {
                if (distanceX > 0) {
                    //scrollLeft(); TODO: smooth scroll to center
                    changePage(currentPage + 1);
                } else {
                    //scrollRight(); TODO: smooth scroll to center
                    changePage(currentPage - 1);
                }
            }
            touch = false;
            distanceX = 0;
            invalidate();
            return true;
        }
        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends android.view.GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Donut.this.touch = true;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if ((distanceX > 0 && (Donut.this.pages.size() > Donut.this.currentPage + 1 || Donut.this.distanceX != 0 && Donut.this.distanceX < distanceX )) ||
                (distanceX < 0 && (Donut.this.currentPage > 0 || Donut.this.distanceX != 0 && Donut.this.distanceX > distanceX))) {
                Donut.this.distanceX += distanceX;
                Donut.this.invalidate();
            }
            return true;
        }

        /*
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_TRESHOLD) {
                    if (diffX > 0) {
                        //onSwipeRight();
                        Toast.makeText(getContext(), "right swipe", Toast.LENGTH_SHORT).show ();
                    } else {
                        //onSwipeLeft();
                        Toast.makeText(getContext(), "left swipe", Toast.LENGTH_SHORT).show ();
                    }
                }
                result = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return result;
        }
        */
    }
}
