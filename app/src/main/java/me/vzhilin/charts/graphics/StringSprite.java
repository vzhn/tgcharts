package me.vzhilin.charts.graphics;

public final class StringSprite {
    public final int x;
    public final int y;
    public final String s;
    public final float opacity;
    public final float size;

    StringSprite(int x, int y, String s, float opacity, float size) {
        this.x = x;
        this.y = y;
        this.s = s;
        this.opacity = opacity;
        this.size = size;
    }
}
