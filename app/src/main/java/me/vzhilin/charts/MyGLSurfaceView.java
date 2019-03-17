package me.vzhilin.charts;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.view.MotionEvent;
import me.vzhilin.charts.data.Data;
import me.vzhilin.charts.data.DataParser;

import java.io.IOException;
import java.io.InputStreamReader;

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
        Data data = readResource(context.getResources());

        System.err.println(data);
    }

    private Data readResource(Resources rs) {
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(rs.openRawResource(R.raw.chart_data)));
            return new DataParser(reader).parse();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
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