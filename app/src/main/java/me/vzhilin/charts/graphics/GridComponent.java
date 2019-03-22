package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import android.opengl.Matrix;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.transitions.SinTransition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

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
    private final TextComponent textComponent;

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform float vColor;" +
                    "void main() {" +
                    "  gl_FragColor.rgb = vec3(241.0 / 255.0, 241.0 / 255.0, 241.0 / 255.0);" +
                    "  gl_FragColor.a = vColor;" +
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

    private SinTransition transition;
    private float opacity;

    private State state = State.HIDDEN;
    private double maxValue;

    public GridComponent(TextComponent textComponent, Model model) {
        this.model = model;
        this.textComponent = textComponent;

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
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        if (state == State.HIDDEN) {
            return;
        }
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();
        // add the coordinates to the FloatBuffer

        float i1 = (float) maxValue / 6f;
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
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES31.glUniform1f(mColorHandle, opacity);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);

        float scrollFactor = (float) ViewConstants.CHART_OFFSET / height;

        float absoluteMax = (float) model.getSmoothMaxFactor();
//        double maxFactor = yColumn.getMaxValue() / absoluteMax;

        float yScaleFactor = 2f / absoluteMax;
        yScaleFactor *= (1f - (float) ViewConstants.CHART_OFFSET / height);
//        yScaleFactor *= maxFactor;

        Matrix.scaleM(identity, 0, 1f, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        // Pass the projection and view transformation to the shader
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES31.glLineWidth(4f);
        // Draw the triangle
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, vertexCount);

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle);

        drawText(height, mvpMatrix);
    }

    private void drawText(int height, float[] mvpMatrix) {
        float step = (height - ViewConstants.CHART_OFFSET) / 6;

        List<StringComponent> strings = new ArrayList<>();
        float yPos = 0;
        for (int i = 0; i < 6; i++) {
            int y = (int) (-10 + height - ViewConstants.CHART_OFFSET - yPos * 1 / (model.getSmoothMaxFactor() / maxValue));
            String text = String.format("%.0f", i * maxValue / 6f);
            strings.add(new StringComponent(5, y, text, opacity));
            yPos += step;
        }
        textComponent.drawString(strings, mvpMatrix);
    }

    public void show() {
        opacity = 0.0f;
        this.transition = new SinTransition(0, 1, 20);

        state = State.FADE_IN;
    }

    public void hide() {
        opacity = 1.0f;
        this.transition = new SinTransition(1, 0, 20);

        state = State.FADE_OUT;
    }

    public void tick() {
        if (state == State.HIDDEN || state == State.VISIBLE) {
            return;
        }

        if (transition.tick()) {
            opacity += transition.getDelta();
        } else {
            if (state == State.FADE_OUT) {
                state = State.HIDDEN;
            } else
            if (state == State.FADE_IN) {
                state = State.VISIBLE;
            }
        }
    }

    public void setMaxFactor(double max) {
        this.maxValue = max;
    }

    enum State {
        VISIBLE,
        FADE_IN,
        FADE_OUT,
        HIDDEN
    }
}
