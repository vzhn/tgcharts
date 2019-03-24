package me.vzhilin.charts.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import android.opengl.Matrix;
import me.vzhilin.charts.ChartRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.data.Column;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Iterator;

import static me.vzhilin.charts.graphics.ScrollComponent.COORDS_PER_VERTEX;

final class ScrollChartColumn {
    private final int mProgram;
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
            "  gl_FragColor = vColor;" +
            "}";


    private final Column yColumn;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    public ScrollChartColumn(Column xColumn, Column yColumn, int c) {
        this.yColumn = yColumn;

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

        initVertexBuffer(xColumn, yColumn);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    private void initVertexBuffer(Column xColumn, Column yColumn) {
        Iterator<Double> itX = xColumn.iterator();
        Iterator<Double> itY = yColumn.iterator();

        double maxY = Collections.max(yColumn.values());
        double minY = 0; //Collections.min(yColumn.values());
        double deltaY = maxY - minY;

        double maxX = Collections.max(xColumn.values());
        double minX = Collections.min(xColumn.values());
        double deltaX = maxX - minX;

        double xFactor = 1.0 / deltaX;
        double yFactor = 1.0 / deltaY;

        int vertexCount = xColumn.size();
        int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
        FloatBuffer vertexBuffer = buildBytebuffer(xColumn).asFloatBuffer();

        while (itX.hasNext()) {
            float x = (float) ((itX.next().floatValue() - minX) * xFactor) * 2f - 1f;
            float y = (float) ((itY.next().floatValue() - minY) * yFactor);

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(0);
        }

        vertexBuffer.position(0);

        yColumn.setVertexBuffer(vertexBuffer, vertexStride, vertexCount);
    }

    private ByteBuffer buildBytebuffer(Column xColumn) {
        ByteBuffer bb = ByteBuffer.allocateDirect(xColumn.size() * 3 * 4);
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        GLES31.glUseProgram(mProgram);
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                yColumn.getVertexStride(), yColumn.getVertexBuffer());

        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        color[3] = yColumn.getOpacity();

        GLES31.glUniform4fv(mColorHandle, 1, color, 0);
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);
        float scaleFactor = 0.8f * (float) 2 *  ViewConstants.SCROLL_HEIGHT / height * yColumn.getAnimatedScrollYScaleFactor();

        Matrix.scaleM(identity, 0, 1f, scaleFactor, 1f);

        Matrix.translateM(identity, 0, 0, (- 1f + (0.2f * ViewConstants.SCROLL_HEIGHT / height)) / scaleFactor  , 0);

        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES31.glLineWidth(4f);
        GLES31.glDrawArrays(GLES31.GL_LINE_STRIP, 0, yColumn.getVertexCount());
        GLES31.glLineWidth(1f);
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
