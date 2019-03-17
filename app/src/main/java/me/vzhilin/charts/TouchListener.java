package me.vzhilin.charts;

import android.view.MotionEvent;

public class TouchListener {
    private final Model model;

    private DragState state = DragState.NONE;
    private float evX;
    private float evY;
//    private MotionEvent ev;

    public TouchListener(Model model) {
        this.model = model;
    }

    public void onTouchEvent(MotionEvent e) {
//        Scroll scroll = view.getScroll();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int width = model.getWidth();

            double xleft = model.getScrollLeft() * width;
            double xright = model.getScrollRight() * width;

            int ypos = model.getHeight() - ViewConstants.SCROLL_HEIGHT;
            if (!(e.getY() < ypos) && !(e.getY() > model.getHeight())) {
                if (Math.abs(e.getX() - xleft) < 10) {
                    state = DragState.LEFT;
                } else
                if (Math.abs(e.getX() - xright) < 10) {
                    state = DragState.RIGHT;
                } else
                if (e.getX() > xleft && e.getX() < xright) {
                    state = DragState.BOTH;
                }
            }
        } else
        if (e.getAction() == MotionEvent.ACTION_UP) {
            state = DragState.NONE;
        } else
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            int width = model.getWidth();
            float dx = e.getX() - evX;

            switch (state) {
                case LEFT:
                    model.setScrollLeft(model.getScrollLeft() + (double) dx / width);
                    break;
                case RIGHT:
                    model.setScrollRight(model.getScrollRight() + (double) dx / width);
                    break;
                case BOTH:
                    model.setScroll(model.getScrollLeft() + (double) dx / width,
                            model.getScrollRight() + (double) dx / width);
                    break;
                case NONE:
                    break;
            }
        }

        evX = e.getX();
        evY = e.getY();
    }
}
