package me.vzhilin.charts.data;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Chart {
    private Column xColumn;
    private Map<String, Column> columns = new LinkedHashMap<>();
    private Map<String, String> types;
    private Map<String, String> names;
    private Map<String, String> colors;

    public void addColumn(Column readColumn) {
        if (readColumn.getLabel().equals("x")) {
            xColumn = readColumn;
        } else {
            columns.put(readColumn.getLabel(), readColumn);
        }
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

    public Column getXColumn() {
        return xColumn;
    }

    public Collection<Column> getYColumns() {
        return columns.values();
    }

    public String getColor(String label) {
        return colors.get(label);
    }
}
