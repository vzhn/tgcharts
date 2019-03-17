package me.vzhilin.charts;

import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;

import java.util.ArrayList;
import java.util.List;

public class ScrollChartComponent {
    private List<ScrollChartColumn> subcomponents = new ArrayList<>();

    public ScrollChartComponent(Model model) {
        Chart chart = model.getChart();

        Column xColumn = chart.getXColumn();
        for (Column yColumn: chart.getYColumns()) {
            int color = chart.getColor(yColumn.getLabel());
            subcomponents.add(new ScrollChartColumn(xColumn, yColumn, color));
        }
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        for (ScrollChartColumn comp: subcomponents) {
            comp.draw(width, height, mMVPMatrix);
        }
    }
}
