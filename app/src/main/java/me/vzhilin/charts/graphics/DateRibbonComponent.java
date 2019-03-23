package me.vzhilin.charts.graphics;

import me.vzhilin.charts.Model;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateRibbonComponent {
    private final Model model;

    private RibbonState state = RibbonState.INIT;
    private double alpha = 0;
    private int k = 1;

    private final SpriteRenderer spriteRenderer;

    public DateRibbonComponent(SpriteRenderer spriteRenderer, Model model) {
        this.spriteRenderer = spriteRenderer;
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

        int yPos = (int) (15 + height - ViewConstants.SCROLL_HEIGHT -
            spriteRenderer.getTypewriter().getContext(Typewriter.FontType.NORMAL_FONT).fontHeight);

        for (double date: xColumn.sample(effectiveK, model.getScrollLeft(), model.getScrollRight())) {
            double xPos = (date - min) * xFactor;
            String dateText = ViewConstants.FORMATTER.format(new Date((long) date));
            spriteRenderer.drawString(dateText, (int) xPos, yPos, 1.0f);
        }

        if (state == RibbonState.ZOOM_IN || state == RibbonState.ZOOM_OUT) {
            for (double date: xColumn.sampleHalf(effectiveK - 1, model.getScrollLeft(), model.getScrollRight())) {
                double xPos = (date - min) * xFactor;
                String dateText = ViewConstants.FORMATTER.format(new Date((long) date));
                spriteRenderer.drawString(dateText, (int) xPos, yPos,  (float) alpha);
            }
        }
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
