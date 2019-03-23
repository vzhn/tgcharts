package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import android.view.View;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
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
        "  gl_FragColor = vec4(1.0, 0.5, 0.5, 1.0);" +
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
        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample("Joined", String.format("%.0f", 122f, Locale.US)));
        samples.add(new Sample("Left", String.format("%.0f", 75f, Locale.US)));
        samples.add(new Sample("Extra", String.format("%.0f", 123456f, Locale.US)));
        samples.add(new Sample("A", String.format("%.0f", 55f, Locale.US)));
        samples.add(new Sample("B", String.format("%.0f", 6f, Locale.US)));

        float boldHeight = tw.getTypewriter().getContext(Typewriter.FontType.BOLD_FONT).fontHeight;

        int w = 0;
        int h = 0;
        h += boldHeight;
        for (Sample s: samples) {
            w += s.getWidth(tw.getTypewriter());
        }

        h += samples.get(0).getHeight(tw.getTypewriter());

        w += (samples.size() - 1) * 20;

        int popupX = (int) model.getX(model.getPopupDate());
        int popupY = 200;
        int popupWidth = w;
        int popupHeight = h;

        drawBorder(popupX, popupY, popupWidth, popupHeight, mMVPMatrix);
        drawText(samples, popupX, popupY, popupWidth, popupHeight, mMVPMatrix);
        drawGeometry(mMVPMatrix);
        drawMarkers(mMVPMatrix);
//        for
    }

    private void drawGeometry() {
        model.getX(model.getPopupDate());
    }

    private void drawText(List<Sample> samples, int popupX, int popupY, int popupWidth, int popupHeight, float[] mMVPMatrix) {
        String date = ViewConstants.FORMATTER_WITH_DATE.format(model.getPopupDate());

        Typewriter tpw = tw.getTypewriter();
        float boldHeight = tpw.getContext(Typewriter.FontType.BOLD_FONT).fontHeight;
        float bigHeight = tpw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;
        float normalHeight = tpw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;

        popupY += boldHeight;

        tw.drawString(date, popupX, popupY, 1.0f, Typewriter.FontType.BOLD_FONT);

        int sampleX = popupX + 0;
        int sampleY = popupY;
        for (Sample s: samples) {
            tw.drawString(s.value, sampleX, (int) (sampleY + bigHeight), 1.0f, Typewriter.FontType.BIG_FONT);
            tw.drawString(s.label, sampleX, (int) (sampleY + bigHeight + normalHeight), 1.0f, Typewriter.FontType.NORMAL_FONT);

            sampleX += s.getWidth(tpw);
            sampleX += 20;
        }
    }

    private void drawBorder(int popupX, int popupY, int popupWidth, int popupHeight, float[] mMVPMatrix) {
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);

        popupBackground.clear();
        popupBackground.putVertex(popupX, popupY);
        popupBackground.putVertex(popupX, popupY + popupHeight);
        popupBackground.putVertex(popupX + popupWidth, popupY);
        popupBackground.putVertex( popupX + popupWidth, popupY + popupHeight);
        popupBackground.putVertex(popupX, popupY + popupHeight);
        popupBackground.putVertex(popupX + popupWidth, popupY);
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

    private void drawGeometry(float[] mMVPMatrix) {
        verticalLine.clear();
        int lineX = (int) model.getX(model.getPopupDate());
        verticalLine.putVertex(lineX, 0);
        verticalLine.putVertex(lineX, model.getHeight() - ViewConstants.CHART_OFFSET);
        verticalLine.position(0);

        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        verticalLine.bindPointer(mPositionHandle);
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, popupBackground.getVertexCount());
    }

    private void drawMarkers(float[] mMVPMatrix) {
        double date = model.getPopupDate();
        int index = model.getPopupDateIndex();

        marker.clear();
        for (Column column: model.getChart().getYColumns()) {
            float my = (float) model.getY(column.getValue(index));
            float mx = (float) model.getX(date);

            tw.drawSprite(column.getMarkerSpriteId(), mx - ViewConstants.MARKER_EXTERNAL_RADIUS,
                my - ViewConstants.MARKER_EXTERNAL_RADIUS, column.getOpacity());

            if (column.getOpacity() > 0) {
                tw.drawSprite(tw.getTypewriter().getMarkerFillerId(), mx - ViewConstants.MARKER_INNER_RADIUS,
                        my - ViewConstants.MARKER_INNER_RADIUS, 1.0f);
            }
        }

        marker.position(0);
    }

    private final static class Sample {
        private final String label;
        private final String value;

        public Sample(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public double getWidth(Typewriter tw) {
            double labelWidth = tw.getContext(Typewriter.FontType.NORMAL_FONT).stringWidth(label);
            double valueWidth = tw.getContext(Typewriter.FontType.BIG_FONT).stringWidth(value);

            return Math.max(labelWidth, valueWidth);
        }

        public double getHeight(Typewriter tw) {
            double labelHeight = tw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;
            double valueHeight = tw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;

            return labelHeight + valueHeight;
        }
    }
}
