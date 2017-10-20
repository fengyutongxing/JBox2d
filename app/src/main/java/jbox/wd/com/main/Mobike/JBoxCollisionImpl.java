package jbox.wd.com.main.Mobike;

import android.view.View;
import android.view.ViewGroup;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import java.util.Random;

import jbox.wd.com.main.R;

public class JBoxCollisionImpl {

    //模拟世界
    private World mWorld;

    //模拟世界的宽高
    private int mWorldWidth, mWorldHeight;
    private ViewGroup viewGroup;
    //约束用随机数  重力加速度
    private Random random = new Random();

    //运动的频率 一秒60帧
    private float dt = 1f / 30f;
    //每一帧的代的次数
    private int velocityIterations = 5;
    //每一帧计算的位置点
    private int positionIterations = 20;

    //模拟世界和真实世界的映射比例
    private int mProportion = 50;
    //世界里面的密度
    private float mDensity = 0.5f;

    //摩擦系数
    private float mFrictionRatio = 1.8f;
    //补偿系数 设置运动频率的快慢
    private float mRestitutionRatio = 1.1f;

    public  JBoxCollisionImpl(ViewGroup viewGroup,float desity){
        this.viewGroup = viewGroup;
        this.mDensity = desity;

    }



//    public JBoxCollisionImpl(ViewGroup viewGroup) {
//        this.viewGroup = viewGroup;
//        mDensity = viewGroup.getContext().getResources().getDisplayMetrics().density;
//    }

    //创建模拟世界的刚体
    private void createWorld() {
        if (mWorld == null) {
            mWorld = new World(new Vec2(0, 10.0f));
            updateTopAndBottomBounds();
            updateLeftAndRightBounds();
        }
    }

    //每一个view创建一个模拟世界刚体
    private void createWorldChild(boolean change) {
        if (viewGroup != null) {
            int count = viewGroup.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = viewGroup.getChildAt(i);
                if (!isBodyView(view)) {
                    createBody(view);
                }
            }
        }

    }

    private void createBody(View view) {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        Shape shape = null;
        //判断是否是一个圆形
        Boolean isCircle = (Boolean) view.getTag(R.id.wd_view_circle_tag);
        //绘制世界的坐标  x y 重心
        bodyDef.position.set(mappingView2Body(view.getX() + view.getWidth() / 2), mappingView2Body(view.getY() + view.getHeight() / 2));
        if (isCircle != null && isCircle) {
            shape = createCircleBody(view);
        } else {
            shape = createPolygonBody(view);
        }

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = mDensity;
        def.friction = mFrictionRatio;
        def.restitution = mRestitutionRatio;

        Body body = mWorld.createBody(bodyDef);
        body.createFixture(def);
        view.setTag(R.id.wd_view_body_tag, body);
        //设置重力方向的约束 加速 随机值
        body.setLinearVelocity(new Vec2(random.nextFloat(), random.nextFloat()));
    }

    //宽高
    public void onSizeChanged(int w, int h) {
        this.mWorldWidth = w;
        this.mWorldHeight = h;
    }

    public void onLayout(boolean changed) {
        createWorld();
        createWorldChild(changed);
    }

    public void onDraw() {
        //启动事件
        if (mWorld != null) {
            mWorld.step(dt, velocityIterations, positionIterations);
        }
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (isBodyView(view)) {
                view.setX(getViewX(view));
                view.setY(getViewY(view));
                view.setRotation(getViewRotaion(view));//角度
            }
        }
        //刷新
        viewGroup.invalidate();
    }

    //创建上下的静态刚体
    private void updateTopAndBottomBounds() {
        //描述的刚体
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;
        //形状
        PolygonShape shape = new PolygonShape();
        //指定大小
        float hx = mappingView2Body(mWorldWidth);
        //高度为1
        float hy = mappingView2Body(mProportion);
        //设置矩形
        shape.setAsBox(hx, hy);
        //摩擦系数
        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = mDensity;
        def.friction = mFrictionRatio;
        def.restitution = mRestitutionRatio;

        //摩擦初始位置
        bodyDef.position.set(0, -hy);
        //真实的刚体底部的body
        Body bottomBody = mWorld.createBody(bodyDef);
        //设置摩擦系数
        bottomBody.createFixture(def);

        //顶部静态 刚体
        bodyDef.position.set(0, mappingView2Body(mWorldHeight) + hy);
        Body topBody = mWorld.createBody(bodyDef);
        topBody.createFixture(def);
    }

    //左右方向的弹力墙
    private void updateLeftAndRightBounds() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.STATIC;

        PolygonShape shape = new PolygonShape();
        float hx = mappingView2Body(mProportion);
        float hy = mappingView2Body(mWorldHeight);
        shape.setAsBox(hx, hy);

        FixtureDef def = new FixtureDef();
        def.shape = shape;
        def.density = mDensity;
        def.friction = mFrictionRatio;
        def.restitution = mRestitutionRatio;

        bodyDef.position.set(-hx, 0);
        Body leftBody = mWorld.createBody(bodyDef);
        leftBody.createFixture(def);

        bodyDef.position.set(mappingView2Body(mWorldWidth) + hx, 0);
        Body rightBody = mWorld.createBody(bodyDef);
        rightBody.createFixture(def);
    }


    //半径的圆点
    private Shape createCircleBody(View view) {
        //圆形
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(mappingView2Body(view.getWidth() / 2));
        return circleShape;
    }

    private Shape createPolygonBody(View view) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(mappingView2Body(view.getWidth() / 2), mappingView2Body(view.getHeight() / 2));
        return shape;
    }

    //view坐标映射为物理坐标
    private float mappingView2Body(float view) {
        return view / mProportion;
    }

    //物理坐标映射为view坐标
    private float mappingBody2View(float body) {
        return body * mProportion;
    }


    //触发事件 提现到刚体里面
    public void applyLinearImpulse(float x, float y, View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        Vec2 vec2 = new Vec2(x, y);
        body.applyLinearImpulse(vec2, body.getPosition(), true);
    }

    //判断是否是一个body
    public boolean isBodyView(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        return body != null;
    }

    //映射回来的坐标
    private float getViewX(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (body != null) {
            return mappingBody2View(body.getPosition().x) - (view.getWidth() / 2);
        }
        return 0;
    }

    private float getViewY(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (body != null) {
            return mappingBody2View(body.getPosition().y) - (view.getHeight() / 2);
        }
        return 0;
    }

    public float getViewRotaion(View view) {
        Body body = (Body) view.getTag(R.id.wd_view_body_tag);
        if (body != null) {
            float angle = body.getAngle();
            return (angle / 3.14f * 180f) % 360;
        }
        return 0;
    }


}
