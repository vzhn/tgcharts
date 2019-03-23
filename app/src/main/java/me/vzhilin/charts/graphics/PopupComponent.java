package me.vzhilin.charts.graphics;

import me.vzhilin.charts.Model;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PopupComponent {
    private final Model model;
    private final SpriteRenderer tw;

    public PopupComponent(Model model, SpriteRenderer tw) {
        this.model = model;
        this.tw = tw;

//        model.getChart().getXColumn().values().
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        int popupX = 100;
        int popupY = 100;

        String date = "Sat, Feb 24";

        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample("Joined", 122));
        samples.add(new Sample("Left", 67));

//        tw.drawSprite(tw.getTypewriter().getCornersTexture(), popupX, popupY);
        tw.drawString(date, popupX + 20, popupY, 1.0f, Typewriter.FontType.BOLD_FONT);

        int sampleX = popupX + 20;
        int sampleY = popupY + 70;
        for (Sample s: samples) {
            tw.drawString(String.format("%.0f", s.value, Locale.US), sampleX, sampleY, 1.0f, Typewriter.FontType.BIG_FONT);
            tw.drawString(s.label, sampleX, sampleY + 50, 1.0f, Typewriter.FontType.NORMAL_FONT);

            sampleX += 150;
        }

//        for
    }

    private final static class Sample {
        private final String label;
        private final double value;

        public Sample(String label, double value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public double getValue() {
            return value;
        }
    }
}
