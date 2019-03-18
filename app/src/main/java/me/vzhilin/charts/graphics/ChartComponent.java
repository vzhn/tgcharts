package me.vzhilin.charts.graphics;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.data.Column;

import static me.vzhilin.charts.graphics.ScrollComponent.COORDS_PER_VERTEX;

public class ChartComponent {
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

    private Model model;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    public ChartComponent(Model model, Column xColumn, Column yColumn, int c) {
        this.model = model;
        this.yColumn = yColumn;

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

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
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                yColumn.getVertexStride(), yColumn.getVertexBuffer());

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

        float scrollFactor = (float) ViewConstants.SCROLL_HEIGHT / height;

        double absoluteMax = model.getSmoothMaxFactor();
        double maxFactor = yColumn.getMaxValue() / absoluteMax;

        float yScaleFactor = 2f;
        yScaleFactor *= (1f - 1 * (float) ViewConstants.SCROLL_HEIGHT / height);
        yScaleFactor *= maxFactor;

        float xScaleFactor = 1f / (float) (model.getScrollRight() - model.getScrollLeft());
        Matrix.scaleM(identity, 0, xScaleFactor, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, - (float) model.getScrollLeft() + (float) (1 - model.getScrollRight()), 0, 0);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES20.glLineWidth(6f);
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, yColumn.getVertexCount());
        GLES20.glLineWidth(1f);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
