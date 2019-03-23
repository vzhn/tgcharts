package me.vzhilin.charts.graphics;

import me.vzhilin.charts.graphics.typewriter.Typewriter;

public final class StringSprite {
    public final int x;
    public final int y;
    public final String s;
    public final float opacity;
    public final Typewriter.FontType size;

    StringSprite(int x, int y, String s, float opacity, Typewriter.FontType size) {
        this.x = x;
        this.y = y;
        this.s = s;
        this.opacity = opacity;
        this.size = size;
    }
}
