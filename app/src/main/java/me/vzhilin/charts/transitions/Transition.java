package me.vzhilin.charts.transitions;

public interface Transition {
    boolean tick();

    float getDelta();
}
