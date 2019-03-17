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
//    private final Scroll scroll;

    public MyGLSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);

        Data data = readResource(context.getResources());
        Model model = new Model(data.getChart(0));
        model.setScrollLeft(0.5);
        model.setScrollRight(0.6);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(model);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        touchListener = new TouchListener(model);
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
}