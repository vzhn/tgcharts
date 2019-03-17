package me.vzhilin.charts;

public class Animation {
    private final float delta;
    private int tick;
    private final int ticks;

    public Animation(float from, float to, int ticks) {
        this.ticks = ticks;
        delta = (to - from) / ticks;

        tick = 0;
    }

    public boolean tick() {
        ++tick;
        return tick < ticks;
    }

    public float getDelta() {
        return delta;
    }
}
