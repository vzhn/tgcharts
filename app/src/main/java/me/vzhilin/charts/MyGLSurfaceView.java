package me.vzhilin.charts;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer renderer;
    private final TouchListener touchListener;
    private final Scroll scroll;

    public MyGLSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        scroll = new Scroll();
        scroll.setLeft(0.5);
        scroll.setRight(0.6);
        renderer = new MyGLRenderer(scroll);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        touchListener = new TouchListener(this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchListener.onTouchEvent(event);
        return true;
    }

    public Scroll getScroll() {
        return scroll;
    }
}