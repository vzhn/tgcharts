package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import android.opengl.Matrix;
import me.vzhilin.charts.ChartRenderer;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.transitions.SinTransition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

public class GridComponent {
    private final Model model;

    private final String vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "}";

    private final SpriteRenderer spriteRenderer;
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

    public GridComponent(SpriteRenderer spriteRenderer, Model model) {
        this.model = model;
        this.spriteRenderer = spriteRenderer;

        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mvpMatrix) {
        if (state == State.HIDDEN) {
            return;
        }

        GLES31.glUseProgram(mProgram);
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        vertexBuffer.clear();

        float i1 = (float) maxValue / 6f;
        for (int i = 0; i < 10; i++) {
            triangleCoords[6 * i + 0] = -1f;
            triangleCoords[6 * i + 1] = i1 * i;
            triangleCoords[6 * i + 3] = +1f;
            triangleCoords[6 * i + 4] = i1 * i;
        }

        //-------------------- TOP
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        GLES31.glUniform1f(mColorHandle, opacity);
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

        float yScaleFactor = 2f / absoluteMax;
        yScaleFactor *= (1f - (float) ViewConstants.CHART_OFFSET / height);

        Matrix.scaleM(identity, 0, 1f, yScaleFactor, 1f);
        Matrix.translateM(identity, 0, 0, -1/yScaleFactor, 0);
        Matrix.translateM(identity, 0, 0, 2 * scrollFactor / yScaleFactor, 0);

        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);
        GLES31.glLineWidth(4f);
        GLES31.glDrawArrays(GLES31.GL_LINES, 0, vertexCount);
        GLES31.glDisableVertexAttribArray(mPositionHandle);

        drawText(height, mvpMatrix);
    }

    private void drawText(int height, float[] mvpMatrix) {
        float step = (height - ViewConstants.CHART_OFFSET) / 6;

        float yPos = 0;
        for (int i = 0; i < 6; i++) {
            int y = (int) (-10 + height - ViewConstants.CHART_OFFSET - yPos * 1 / (model.getSmoothMaxFactor() / maxValue));
            spriteRenderer.drawString(String.format("%.0f", i * maxValue / 6f, Locale.US), 5, y, ViewConstants.VIEW_GRAY, opacity);

            yPos += step;
        }

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
