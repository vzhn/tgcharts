package me.vzhilin.charts;

import android.view.MotionEvent;

public class TouchListener {
    private final MyGLSurfaceView view;
    private DragState state = DragState.NONE;
    private MotionEvent ev;

    public TouchListener(MyGLSurfaceView myGLSurfaceView) {
        this.view = myGLSurfaceView;
    }

    public void onTouchEvent(MotionEvent e) {
        Scroll scroll = view.getScroll();
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            int width = view.getWidth();

            double xleft = scroll.getLeft() * width;
            double xright = scroll.getRight() * width;

            int ypos = view.getHeight() - ViewConstants.SCROLL_HEIGHT;
            if (!(e.getY() < ypos) && !(e.getY() > view.getHeight())) {
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
            int width = view.getWidth();
            float dx = e.getX() - ev.getX();

            switch (state) {
                case LEFT:
                    scroll.setLeft(scroll.getLeft() + (double) dx / width);
                    break;
                case RIGHT:
                    scroll.setRight(scroll.getRight() + (double) dx / width);
                    break;
                case BOTH:
                    scroll.setLeftRight(scroll.getLeft() + (double) dx / width,
                            scroll.getRight() + (double) dx / width);
                    break;
                case NONE:
                    break;
            }
        }

        ev = e;
    }
}
