package me.vzhilin.charts;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import me.vzhilin.charts.data.Column;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Iterator;

import static me.vzhilin.charts.ScrollComponent.COORDS_PER_VERTEX;

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


    private final int vertexStride;
    private final int vertexCount;
    private final Column yColumn;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    private FloatBuffer vertexBuffer;

    public ScrollChartColumn(Column xColumn, Column yColumn, int c) {
        this.yColumn = yColumn;
        ByteBuffer bb = ByteBuffer.allocateDirect(xColumn.size() * 3 * 4);
        bb.order(ByteOrder.nativeOrder());

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

        vertexCount = xColumn.size();
        vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        vertexBuffer = bb.asFloatBuffer();

        Iterator<Double> itX = xColumn.iterator();
        Iterator<Double> itY = yColumn.iterator();

        double maxY = Collections.max(yColumn.values());
        double minY = Collections.min(yColumn.values());
        double deltaY = maxY - minY;

        double maxX = Collections.max(xColumn.values());
        double minX = Collections.min(xColumn.values());
        double deltaX = maxX - minX;

        double xFactor = 1.0 / deltaX;
        double yFactor = 1.0 / deltaY;

        while (itX.hasNext()) {
            float x = (float) ((itX.next().floatValue() - minX) * xFactor) * 2f - 1f;
            float y = (float) ((itY.next().floatValue() - minY) * yFactor) * 2f - 1f;

            vertexBuffer.put(x);
            vertexBuffer.put(y);
            vertexBuffer.put(0);
        }

        vertexBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
//        if (!yColumn.isVisible()) {
//            return;
//        }
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        color[3] = yColumn.getOpacity();

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);
        float scaleFactor = (float) ViewConstants.SCROLL_HEIGHT / height * yColumn.getScrollYScaleFactor();

        Matrix.scaleM(identity, 0, 1f, scaleFactor, 1f);

        Matrix.translateM(identity, 0, 0, - 1f / scaleFactor + 1f, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES20.glLineWidth(4f);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);
        GLES20.glLineWidth(1f);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
