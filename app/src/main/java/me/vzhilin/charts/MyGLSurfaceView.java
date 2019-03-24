package me.vzhilin.charts;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.support.v4.widget.CompoundButtonCompat;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.data.Data;
import me.vzhilin.charts.data.DataParser;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MyGLSurfaceView extends GLSurfaceView {
    private final ChartRenderer renderer;
    private final TouchListener touchListener;

    private final Model model;

    public MyGLSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);

        Resources resources = context.getResources();
        Typewriter tw = new Typewriter(resources);

        Data data = readResource(resources);
        model = new Model(data.getChart(4), tw);
        model.setScroll(0.5, 0.6);

        setEGLContextClientVersion(3);
//        GLES31.glHint(GLES31.GL_NICEST);

        renderer = new ChartRenderer(model, resources);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        touchListener = new TouchListener(model);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        LinearLayout mailLayout = ((Activity) getContext()).findViewById(R.id.mainLayout);
        Chart chart = model.getChart();
        for (final Column yColumn: chart.getYColumns()) {
            CheckBox checkBox = new CheckBox(getContext());

            int states[][] = {{android.R.attr.state_checked}, {}};
            int colors[] = {chart.getColor(yColumn.getLabel()), ViewConstants.VIEW_GRAY};
            CompoundButtonCompat.setButtonTintList(checkBox, new ColorStateList(states, colors));

            final String label = yColumn.getLabel();
            checkBox.setText(label);

            mailLayout.addView(checkBox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    synchronized (Model.class) {
                        model.setVisible(label, isChecked);
                    }
                }
            });
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (model.isDirty()) {
                    requestRender();
                }
            }
        }, 0, 1000 / 60, TimeUnit.MILLISECONDS);
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