package me.vzhilin.charts.graphics;

import me.vzhilin.charts.Model;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.data.Column;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateRibbonComponent {
    private final Model model;
    private final SimpleDateFormat format = new SimpleDateFormat("MMM  d", Locale.US);

    private RibbonState state = RibbonState.INIT;
    private double alpha = 0;
    private int k = 1;

    private final TextComponent textComponent;

    public DateRibbonComponent(TextComponent textComponent, Model model) {
        this.textComponent = textComponent;
        this.model = model;

        model.setRibbonComponent(this);
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        Column xColumn = model.getChart().getXColumn();

        double xDelta = xColumn.getMaxValue() - xColumn.getMinValue();
        double min = xColumn.getMinValue() + xDelta * model.getScrollLeft();

        xDelta *= (model.getScrollRight() - model.getScrollLeft());

        double xFactor = width / xDelta;

        int effectiveK = 1;
        switch (state) {
            case INIT:
                effectiveK = k;
                break;
            case ZOOM_IN:
                effectiveK = k + 1;
                break;
            case ZOOM_OUT:
                effectiveK = k;
                break;
        }

        List<StringComponent> components = new ArrayList<>();
        for (double date: xColumn.sample(effectiveK, model.getScrollLeft(), model.getScrollRight())) {
            double xPos = (date - min) * xFactor;
            String dateText = format.format(new Date((long) date));
            components.add(new StringComponent((int) xPos, height - ViewConstants.SCROLL_HEIGHT, dateText, 1.0f));
        }

        if (state == RibbonState.ZOOM_IN || state == RibbonState.ZOOM_OUT) {
            for (double date: xColumn.sampleHalf(effectiveK - 1, model.getScrollLeft(), model.getScrollRight())) {
                double xPos = (date - min) * xFactor;
                String dateText = format.format(new Date((long) date));
                components.add(new StringComponent((int) xPos, height - ViewConstants.SCROLL_HEIGHT, dateText, (float) alpha * 0.5f));
            }
        }

        textComponent.drawString(components, mMVPMatrix);
    }
    public void onFactorUpdated(int kOld, int k) {
        switch (state) {
            case INIT:
                if (k > this.k) {
                    state = RibbonState.ZOOM_OUT;
                    alpha = 1f;
                } else
                if (k < this.k) {
                    state = RibbonState.ZOOM_IN;
                    alpha = 0f;
                }
                break;
            case ZOOM_IN:
                break;
            case ZOOM_OUT:
                break;
            default:
                throw new IllegalStateException();
        }

        this.k = k;
    }

    public void tick() {
        switch (state) {
            case INIT:
                break;
            case ZOOM_IN:
                alpha += 1/20f;
                if (alpha >= 1.0f) {
                    alpha = 1.0f;
                    state = RibbonState.INIT;
                }
                break;
            case ZOOM_OUT:
                alpha -= 1/20f;
                if (alpha <= 0.0f) {
                    alpha = 0.0f;
                    state = RibbonState.INIT;
                }
                break;
        }
    }

    private enum RibbonState {
        INIT,
        ZOOM_IN,
        ZOOM_OUT
    }
}
