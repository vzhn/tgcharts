package me.vzhilin.charts.graphics;

import me.vzhilin.charts.Model;
import me.vzhilin.charts.data.Chart;
import me.vzhilin.charts.data.Column;

import java.util.ArrayList;
import java.util.List;

public class ScrollChartComponent {
    private final List<ChartComponent> chartComponents = new ArrayList<>();
    private List<ScrollChartColumn> subcomponents = new ArrayList<>();

    public ScrollChartComponent(Model model, SpriteRenderer spriteRenderer) {
        Chart chart = model.getChart();

        Column xColumn = chart.getXColumn();
        for (Column yColumn: chart.getYColumns()) {
            int color = chart.getColor(yColumn.getLabel());
            subcomponents.add(new ScrollChartColumn(xColumn, yColumn, color));
            chartComponents.add(new ChartComponent(model, xColumn, yColumn, color));
        }
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        for (ScrollChartColumn comp: subcomponents) {
            comp.draw(width, height, mMVPMatrix);
        }

        for (ChartComponent cmp: chartComponents) {
            cmp.draw(width, height, mMVPMatrix);
        }
    }
}
