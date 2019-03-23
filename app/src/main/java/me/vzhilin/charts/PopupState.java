package me.vzhilin.charts;

import android.graphics.Rect;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PopupState {
    private final Model model;
    private final Typewriter tw;

    private int index;
    private double date;

    private boolean visible = true;
    private Rect popupDimensions;
    private ArrayList<Sample> samples;

    public PopupState(Model model) {
        this.model = model;
        this.tw = model.getTypewriter();
    }

    public double getDate() {
        return date;
    }

    public void setDate(double date) {
        this.date = date;
    }

    public boolean isVisible() {
        return popupDimensions != null && !samples.isEmpty();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void refresh() {
        samples = new ArrayList<>();
        for (Column yColumn: model.getChart().getYColumns()) {
            if (!yColumn.isVisible()) {
                continue;
            }
            String label = yColumn.getLabel();
            double value = yColumn.getValue(index);
            samples.add(new Sample(label, value));
        }

        if (!samples.isEmpty()) {
            popupDimensions = computeDimensions(samples);
        }
    }

    public Rect computeDimensions(List<Sample> samples) {
        String dateString = ViewConstants.FORMATTER_WITH_DATE.format(date);
        int  dateWidth = (int) tw.getContext(Typewriter.FontType.BOLD_FONT).stringWidth(dateString);

        float boldHeight = tw.getContext(Typewriter.FontType.BOLD_FONT).fontHeight;

        int w = 0;
        int h = 0;
        {
            h += boldHeight;
            for (Sample s: samples) {
                w += s.getWidth(tw);
            }

            h += samples.get(0).getHeight(tw);
            w += (samples.size() - 1) * 20;
        }

        w = Math.max(w, dateWidth);

        int popupX = (int) (model.getX(date) - w * 0.25f);
        if (popupX < 0) {
            popupX = 0;
        }

        if (popupX + w > model.getWidth()) {
            popupX = model.getWidth() - w;
        }
        int popupY = 0;

        return new Rect(popupX, popupY, popupX + w, popupY + h);
    }

    public void setPosition(int index, double date) {
        this.index = index;
        this.date  = date;
    }

    public int getIndex() {
        return index;
    }

    public Rect getDimensions() {
        return popupDimensions;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public double maxMarkedValue() {
        double max = 0;
        for (Sample s: samples) {
            max = Math.max(s.getValue(), max);
        }
        return max;
    }
}
