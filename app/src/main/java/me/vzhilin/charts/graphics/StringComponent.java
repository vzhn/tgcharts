package me.vzhilin.charts.graphics;

public final class StringComponent {
    public final int x;
    public final int y;
    public final String s;
    public final float opacity;

    StringComponent(int x, int y, String s, float opacity) {
        this.x = x;
        this.y = y;
        this.s = s;
        this.opacity = opacity;
    }
}
