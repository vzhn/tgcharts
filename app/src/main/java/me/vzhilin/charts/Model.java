package me.vzhilin.charts;

import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Model {
    private final Chart chart;

    private double scrollLeft;
    private double scrollRight;

    private int width;
    private int height;

    private List<Animation> animationList = new ArrayList();

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

    public Chart getChart() {
        return chart;
    }

    public void setVisible(String label, final boolean visible) {
        final Column yColumn = chart.getYColumn(label);
        if (yColumn.isVisible() != visible) {
            yColumn.setVisible(visible);

            if (visible) {
                animationList.add(new Animation(0f, 1.0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            } else {
                animationList.add(new Animation(1.0f, 0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            }
        }

        refreshScrollScaleFactors();
    }

    private void refreshScrollScaleFactors() {
        double max = 0;
        for (Column yColumn: chart.getYColumns()) {
            if (yColumn.isVisible()) {
                max = Math.max(max, yColumn.getMaxValue());
            }
        }

        for (final Column yColumn: chart.getYColumns()) {
            if (yColumn.isVisible()) {
                float prevScaleFactor = yColumn.getScrollYScaleFactor();
                float newScaleFactor = (float) (yColumn.getMaxValue() / max);

                yColumn.setScrollYScaleFactor(newScaleFactor);

                animationList.add(new Animation(prevScaleFactor, newScaleFactor, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incAnimatedScrollScaleFactor(getDelta());
                        return tick;
                    }
                });
            }
        }
    }

    public void tick() {
        Iterator<Animation> it = animationList.iterator();
        while (it.hasNext()) {
            Animation v = it.next();
            if (!v.tick()) {
                it.remove();
            }
        }
    }

    public double getMaxValue(double scrollLeft, double scrollRight) {
        double max = 0;
        for (Column column: chart.getYColumns()) {
            max = Math.max(max, column.getMaxValue(scrollLeft, scrollRight));
        }

        return max;
    }
}
