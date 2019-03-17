package me.vzhilin.charts.data;

import java.util.LinkedHashMap;
import java.util.Map;

public class Chart {
    private Map<String, Column> columns = new LinkedHashMap<>();
    private Map<String, String> types;
    private Map<String, String> names;
    private Map<String, String> colors;

    public void addColumn(Column readColumn) {
        columns.put(readColumn.getLabel(), readColumn);
    }

    public void setTypes(Map<String, String> types) {
        this.types = types;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }
}
