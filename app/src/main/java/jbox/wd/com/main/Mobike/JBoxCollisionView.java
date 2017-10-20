package jbox.wd.com.main.Mobike;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class JBoxCollisionView extends FrameLayout {
    private JBoxCollisionImpl jboxImpl;

    public JBoxCollisionView(Context context) {
        this(context, null);
    }

    public JBoxCollisionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JBoxCollisionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);//渲染布局
        initView();

    }

    private void initView() {
        jboxImpl = new JBoxCollisionImpl(this,getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        jboxImpl.onSizeChanged(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //创建world
        jboxImpl.onLayout(changed);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        jboxImpl.onDraw();
    }

    //重力感应变换
    public void onSensorChanged(float x, float y) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (jboxImpl.isBodyView(view)) {
                jboxImpl.applyLinearImpulse(x, y, view);
            }
        }
    }
}
