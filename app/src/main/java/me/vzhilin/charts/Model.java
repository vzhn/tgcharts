package me.vzhilin.charts;

import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.graphics.GridComponent;
import me.vzhilin.charts.transitions.LinearTransition;
import me.vzhilin.charts.transitions.SinTransition;
import me.vzhilin.charts.transitions.Transition;

import java.util.*;

public class Model {
    private final Chart chart;

    private double scrollLeft;
    private double scrollRight;

    private int width;
    private int height;

    private List<Transition> animationList = new ArrayList();
    private List<GridComponent> gridComponents = new ArrayList<>();
    private double maxFactor;
    private double smoothMaxFactor = -1;

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

        refreshMaxFactor();
    }

    public void setScrollRight(double scrollRight) {
        this.scrollRight = scrollRight;

        refreshMaxFactor();
    }

    public void setScroll(double scrollLeft, double scrollRight) {
        if (scrollLeft >= 0 && scrollRight <= 1 && scrollLeft < scrollRight) {
            this.scrollLeft = scrollLeft;
            this.scrollRight = scrollRight;

            refreshMaxFactor();
        }
    }

    private void refreshMaxFactor() {
        double max = 0;
        for (Column column: chart.getYColumns()) {
            if (column.isVisible()) {
                max = Math.max(max, column.getMaxValue(scrollLeft, scrollRight));
            }
        }

        max = Math.ceil(max / 50) * 50;

        if (max != maxFactor) {
            if (smoothMaxFactor == -1) {
                smoothMaxFactor = maxFactor;
            }

            animationList.add(new SinTransition((float) maxFactor, (float) max, 20) {
                @Override
                public boolean tick() {
                    boolean b = super.tick();
                    smoothMaxFactor += getDelta();
                    return b;
                }
            });


            GridComponent fadeInComponent = gridComponents.get(0);
            fadeInComponent.setMaxFactor(max);

            GridComponent fadeOutComponent = gridComponents.get(1);
            fadeOutComponent.setMaxFactor(maxFactor);

            fadeInComponent.show((float) maxFactor, (float) max);
            fadeOutComponent.hide((float) maxFactor, (float) max);



//            gridComponents.get(1).hide((float) maxFactor, (float) max);
//            gc.startTransition((float) maxFactor, (float) max, 20);

            maxFactor = max;
        }
    }

    public Chart getChart() {
        return chart;
    }

    public void setVisible(String label, final boolean visible) {
        final Column yColumn = chart.getYColumn(label);
        if (yColumn.isVisible() != visible) {
            yColumn.setVisible(visible);

            if (visible) {
                animationList.add(new LinearTransition(0f, 1.0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            } else {
                animationList.add(new LinearTransition(1.0f, 0f, 20) {
                    @Override
                    public boolean tick() {
                        boolean tick = super.tick();
                        yColumn.incOpacity(getDelta());
                        return tick;
                    }
                });
            }

            refreshMaxFactor();
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

                animationList.add(new LinearTransition(prevScaleFactor, newScaleFactor, 20) {
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
        Iterator<Transition> it = animationList.iterator();
        while (it.hasNext()) {
            Transition v = it.next();
            if (!v.tick()) {
                it.remove();
            }
        }
    }

    public double getSmoothMaxFactor() {
        return smoothMaxFactor;
    }

    public double getMaxValue() {
        return maxFactor;
    }

    public Collection<GridComponent> getGridComponents() {
        return gridComponents;
    }
}
