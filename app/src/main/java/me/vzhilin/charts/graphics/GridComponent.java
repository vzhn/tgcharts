package me.vzhilin.charts.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.transitions.SinTransition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GridComponent {
    private final Model model;

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

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static float triangleCoords[] = new float[3 * 24];

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    private SinTransition transition;
    private float value;

    private State state = State.HIDDEN;

    public GridComponent(Model model) {
        this.model = model;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
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
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();
        // add the coordinates to the FloatBuffer

        float i1 = (float) model.getMaxValue() / 6f;
        for (int i = 0; i < 10; i++) {
            triangleCoords[6 * i + 0] = -1f;

            triangleCoords[6 * i + 1] = i1 * i;

            triangleCoords[6 * i + 3] = +1f;
            triangleCoords[6 * i + 4] = i1 * i;
        }

        //-------------------- TOP
        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);

        float scrollFactor = (float) ViewConstants.SCROLL_HEIGHT / height;

        float absoluteMax = (float) model.getSmoothMaxFactor();
//        double maxFactor = yColumn.getMaxValue() / absoluteMax;

        float yScaleFactor = 2f / absoluteMax;
        yScaleFactor *= (1f - 1 * (float) ViewConstants.SCROLL_HEIGHT / height);
//        yScaleFactor *= maxFactor;

        Matrix.scaleM(identity, 0, 1f, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    public void show(float start, float end) {
        this.transition = new SinTransition(start, end, 20);

        state = State.FADE_OUT;
    }

    public void hide(float start, float end) {
        this.transition = new SinTransition(start, end, 20);

        state = State.FADE_IN;
    }

    public void tick() {
//        if (transition.tick()) {
//            value += transition.getDelta();
//        } else {
//            if (state == State.FADE_OUT) {
//                state = State.HIDDEN;
//            } else
//            if (state == State.FADE_IN) {
//                state = State.VISIBLE;
//            }
//        }
    }

    enum State {
        VISIBLE,
        FADE_IN,
        FADE_OUT,
        HIDDEN
    }
}
