package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.ChartRenderer;
import me.vzhilin.charts.ViewConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ScrollRibbonComponent {
    private static final int FRAME_WIDTH_2 = 20;
    private static final int FRAME_WIDTH_1 = 5;
    private final Model model;

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final int mProgram;

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;

    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = new float[3 * 12];

    public ScrollRibbonComponent(Model model) {
        this.model = model;

        ByteBuffer bb = ByteBuffer.allocateDirect(
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

    public void draw(int width, int height, float[] mMVPMatrix) {
        GLES31.glUseProgram(mProgram);
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();

        float leftX = (float) (model.getScrollLeft() * width);
        float rightX = (float) (model.getScrollRight() * width);
        int topY = height - ViewConstants.SCROLL_HEIGHT;
        int bottomY = height;

        triangleCoords[0] = 0;
        triangleCoords[1] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[3] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[4] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[6] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[7] = bottomY;

        triangleCoords[9] = 0;
        triangleCoords[10] = topY - ViewConstants.FRAME_WIDTH_1;

        triangleCoords[12] = leftX - ViewConstants.FRAME_WIDTH_2;
        triangleCoords[13] = bottomY;

        triangleCoords[15] = 0;
        triangleCoords[16] = bottomY;

        triangleCoords[18] = rightX;
        triangleCoords[19] = topY - FRAME_WIDTH_1;

        triangleCoords[21] = width;
        triangleCoords[22] = topY - FRAME_WIDTH_1;

        triangleCoords[24] = width;
        triangleCoords[25] = bottomY;

        triangleCoords[27] = rightX;
        triangleCoords[28] = topY - FRAME_WIDTH_1;

        triangleCoords[30] = width;
        triangleCoords[31] = bottomY;

        triangleCoords[33] = rightX;
        triangleCoords[34] = bottomY;


        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
