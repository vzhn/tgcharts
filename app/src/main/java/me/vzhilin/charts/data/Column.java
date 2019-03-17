package me.vzhilin.charts.data;

import java.util.List;

public class Column {
    private final String label;
    private final List<Double> data;

    public Column(String label, List<Double> data) {
        this.label = label;
        this.data = data;
    }

    public String getLabel() {
        return label;
    }
}
