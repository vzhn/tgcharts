package me.vzhilin.charts;

import java.util.ArrayList;
import java.util.List;

public class Scroll {
    private final List<ScrollListener> listeners = new ArrayList<>();

    private double left;
    private double right;

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;

        notifyObservers();
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) {
        this.right = right;

        notifyObservers();
    }

    public void setLeftRight(double left, double right) {
        this.left = left;
        this.right = right;

        notifyObservers();
    }

    public void addListener(ScrollListener scrollListener) {
        listeners.add(scrollListener);
        scrollListener.scrollUpdated(left, right);
    }

    private void notifyObservers() {
        for (ScrollListener listener: listeners) {
            listener.scrollUpdated(left, right);
        }
    }
}
