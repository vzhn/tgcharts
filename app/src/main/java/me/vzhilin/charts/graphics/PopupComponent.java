package me.vzhilin.charts.graphics;

import android.graphics.Color;
import android.graphics.Rect;
import android.opengl.GLES31;
import android.view.View;
import me.vzhilin.charts.*;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PopupComponent {
    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        // Note that the uMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  gl_FragColor = vec4(0.9, 0.9, 0.9, 1.0);" +
        "}";

    private final int mProgram;

    private final Model model;
    private final SpriteRenderer tw;
    private final GlFloatBuffer popupBackground;
    private final GlFloatBuffer verticalLine;
    private final GlFloatBuffer marker;
    private final int popupBackgroundVertexCount = 6;

    public PopupComponent(Model model, SpriteRenderer tw) {
        this.model = model;
        this.tw = tw;

        popupBackground = new GlFloatBuffer(popupBackgroundVertexCount);
        verticalLine = new GlFloatBuffer(2);
        marker = new GlFloatBuffer(6 * model.getChart().getYColumns().size());

        int vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES31.glCreateProgram();

        // add the vertex shader to program
        GLES31.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES31.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES31.glLinkProgram(mProgram);

//        model.getChart().getXColumn().values().
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        PopupState popupState = model.getPopupState();
        if (popupState.isVisible()) {
            PopupState state = popupState;
            Rect popupDimensions = state.getDimensions();

            drawBorder(popupDimensions, mMVPMatrix);
            drawText(popupDimensions, state.getSamples(), mMVPMatrix);
            drawGeometry(popupDimensions, mMVPMatrix);
            drawMarkers(popupDimensions, mMVPMatrix);
        }
    }

    private void drawText(Rect r, List<Sample> samples, float[] mMVPMatrix) {
        String date = ViewConstants.FORMATTER_WITH_DATE.format(model.getPopupState().getDate());

        Typewriter tpw = tw.getTypewriter();
        float boldHeight = tpw.getContext(Typewriter.FontType.BOLD_FONT).fontHeight;
        float bigHeight = tpw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;
        float normalHeight = tpw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;

        int popupY = (int) (r.top + boldHeight) + PopupState.MARGIN;

        tw.drawString(date, r.left + PopupState.MARGIN, popupY, Color.BLACK, 1.0f, Typewriter.FontType.BOLD_FONT);

        int sampleX = r.left + PopupState.MARGIN;
        int sampleY = popupY;
        for (Sample s: samples) {
            int color = model.getChart().getColor(s.getLabel());

            tw.drawString(s.getStringValue(), sampleX, (int) (sampleY + bigHeight), color, 1.0f, Typewriter.FontType.BIG_FONT);
            tw.drawString(s.getLabel(), sampleX, (int) (sampleY + bigHeight + normalHeight), Color.BLACK,1.0f, Typewriter.FontType.NORMAL_FONT);

            sampleX += s.getWidth(tpw);
            sampleX += 40;
        }
    }

    private void drawBorder(Rect r, float[] mMVPMatrix) {
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);

        popupBackground.clear();
        popupBackground.putVertex(r.left, r.top);
        popupBackground.putVertex(r.left, r.bottom);
        popupBackground.putVertex(r.right, r.top);
        popupBackground.putVertex( r.right, r.bottom);
        popupBackground.putVertex(r.left, r.bottom);
        popupBackground.putVertex(r.right, r.top);
        popupBackground.position(0);

        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        popupBackground.bindPointer(mPositionHandle);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, popupBackground.getVertexCount());

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }

    private void drawGeometry(Rect r, float[] mMVPMatrix) {
        verticalLine.clear();
        int lineX = (int) model.getX(model.getPopupState().getDate());
        verticalLine.putVertex(lineX, r.bottom);
        verticalLine.putVertex(lineX, model.getHeight() - ViewConstants.CHART_OFFSET);
        verticalLine.position(0);

        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        verticalLine.bindPointer(mPositionHandle);
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, verticalLine.getVertexCount());
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }

    private void drawMarkers(Rect r, float[] mMVPMatrix) {
        double date = model.getPopupState().getDate();
        int index = model.getPopupState().getIndex();

        marker.clear();
        for (Column column: model.getChart().getYColumns()) {
            float my = (float) model.getY(column.getValue(index));
            float mx = (float) model.getX(date);

            tw.drawSprite(column.getMarkerSpriteId(), mx - ViewConstants.MARKER_EXTERNAL_RADIUS,
                my - ViewConstants.MARKER_EXTERNAL_RADIUS, column.getColor(), column.getOpacity());

            if (column.getOpacity() > 0) {
                tw.drawSprite(tw.getTypewriter().getMarkerFillerId(), mx - ViewConstants.MARKER_INNER_RADIUS,
                        my - ViewConstants.MARKER_INNER_RADIUS, Color.WHITE,1.0f);
            }
        }

        marker.position(0);
    }
}
