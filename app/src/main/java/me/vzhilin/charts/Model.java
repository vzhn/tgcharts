package me.vzhilin.charts;

import me.vzhilin.charts.data.Chart;

public class Model {
    private final Chart chart;

    private double scrollLeft;
    private double scrollRight;

    private int width;
    private int height;

    public Model(Chart chart) {
        this.chart = chart;
    }

    public double getScrollLeft() {
        return scrollLeft;
    }

    public double getScrollRight() {
        return scrollRight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setScrollLeft(double scrollLeft) {
        this.scrollLeft = scrollLeft;
    }

    public void setScrollRight(double scrollRight) {
        this.scrollRight = scrollRight;
    }

    public void setScroll(double scrollLeft, double scrollRight) {
        this.scrollLeft = scrollLeft;
        this.scrollRight = scrollRight;
    }
}
