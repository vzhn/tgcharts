package me.vzhilin.charts.data;

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
}
