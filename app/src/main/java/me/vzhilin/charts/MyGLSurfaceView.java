package me.vzhilin.charts;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.data.Data;
import me.vzhilin.charts.data.DataParser;

import java.io.IOException;
import java.io.InputStreamReader;

class MyGLSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private final TouchListener touchListener;

    private final Model model;

    public MyGLSurfaceView(Context context, AttributeSet attrs){
        super(context, attrs);

        Data data = readResource(context.getResources());
        model = new Model(data.getChart(0));
        model.setScrollLeft(0.5);
        model.setScrollRight(0.6);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(model);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);
        touchListener = new TouchListener(model);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        LinearLayout mailLayout = ((Activity) getContext()).findViewById(R.id.mainLayout);

        Chart chart = model.getChart();
        for (final Column yColumn: chart.getYColumns()) {
            CheckBox checkBox = new CheckBox(getContext());
            final String label = yColumn.getLabel();
            checkBox.setText(label);
            checkBox.setTextColor(chart.getColor(label));
            mailLayout.addView(checkBox);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    model.setVisible(label, isChecked);

                }
            });
        }
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