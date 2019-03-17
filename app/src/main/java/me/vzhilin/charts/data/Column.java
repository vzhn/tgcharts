package me.vzhilin.charts.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class Column {
    private final String label;
    private final List<Double> data;
    private boolean visible;

    public Column(String label, List<Double> data) {
        this.label = label;
        this.data = data;
    }

    public String getLabel() {
        return label;
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
}
