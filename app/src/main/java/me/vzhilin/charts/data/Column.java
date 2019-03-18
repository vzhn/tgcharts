package me.vzhilin.charts.data;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Column {
    private final String label;
    private final List<Double> data;
    private final double minValue;
    private final double maxValue;

    private float opacity = 0f;
    private boolean visible;

    private float scrollYScaleFactor = 1.0f;
    private float animatedScrollYScaleFactor = 1.0f;
    private FloatBuffer vertexBuffer;
    private int vertexStride;
    private int vertexCount;

    public Column(String label, List<Double> data) {
        this.label = label;
        this.data = data;

        this.maxValue = Collections.max(data);
        this.minValue = Collections.min(data);
    }

    public String getLabel() {
        return label;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public Iterator<Double> iterator() {
        return data.iterator();
    }

    public int size() {
        return data.size();
    }

    public Collection<Double> values() {
        return data;
    }

    public void setVisible(boolean isChecked) {
        this.visible = isChecked;
    }

    public boolean isVisible() {
        return visible;
    }

    public void incOpacity(float delta) {
        opacity += delta;
    }

    public float getOpacity() {
        return opacity;
    }

    public float getScrollYScaleFactor() {
        return scrollYScaleFactor;
    }

    public void setScrollYScaleFactor(float factor) {
        this.scrollYScaleFactor = factor;
    }

    public float getAnimatedScrollYScaleFactor() {
        return animatedScrollYScaleFactor;
    }

    public void setAnimatedScrollYScaleFactor(float animatedScrollYScaleFactor) {
        this.animatedScrollYScaleFactor = animatedScrollYScaleFactor;
    }

    public void incAnimatedScrollScaleFactor(float delta) {
        this.animatedScrollYScaleFactor += delta;
    }

    public void setVertexBuffer(FloatBuffer vertexBuffer, int vertexStride, int vertexCount) {
        this.vertexBuffer = vertexBuffer;
        this.vertexStride = vertexStride;
        this.vertexCount = vertexCount;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public int getVertexStride() {
        return vertexStride;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public double getMaxValue(double left, double right) {
        int ix1 = (int) Math.floor(left * (data.size() - 1));
        int ix2 = (int) Math.ceil(right * (data.size() - 1));

        double v = 0;
        for (int i = ix1; i < ix2; i++) {
            v = Math.max(v, data.get(i));
        }

        return v;
    }
}
