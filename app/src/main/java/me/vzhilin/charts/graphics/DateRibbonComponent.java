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
    private static final int WITH_LIMIT = 300;
    private final Model model;

    private final TextComponent textComponent;

    public DateRibbonComponent(TextComponent textComponent, Model model) {
        this.textComponent = textComponent;
        this.model = model;
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        Column xColumn = model.getChart().getXColumn();
        double min = xColumn.getMinValue();
        double max = xColumn.getMaxValue();

        double xDelta = max - min;
        min = min + xDelta * model.getScrollLeft();
        max = min + xDelta * model.getScrollRight();

        xDelta *= (model.getScrollRight() - model.getScrollLeft());

        double xFactor = width / xDelta;

        int k = (int) Math.max(1, Math.log(WITH_LIMIT / (xColumn.getDivision() * xFactor)) / Math.log(2));

        List<StringComponent> components = new ArrayList<>();
        List<Double> dateSlice = xColumn.sample(k, model.getScrollLeft(), model.getScrollRight());
        SimpleDateFormat format = new SimpleDateFormat("MMM  d", Locale.US);

        for (double date: dateSlice) {
            double xPos = (date - min) * xFactor;
            String dateText = format.format(new Date((long) date));

            StringComponent c = new StringComponent((int) xPos, height - ViewConstants.SCROLL_HEIGHT, dateText, 1.0f);
            components.add(c);
        }
        textComponent.drawString(components, mMVPMatrix);
    }
}
