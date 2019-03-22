package me.vzhilin.charts.graphics;

import me.vzhilin.charts.Model;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

public class PopupComponent {
    private final Model model;
    private final Typewriter tw;

    public PopupComponent(Model model, Typewriter tw) {
        this.model = model;
        this.tw = tw;
    }

    public void draw(int width, int height, float[] mMVPMatrix) {

    }
}
