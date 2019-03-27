package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import android.opengl.Matrix;
import me.vzhilin.charts.ChartRenderer;

public class VBOComponent {
    private final int mProgram;
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
                    "  gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
                    "}";

    private final GlFloatBuffer vertexBuffer = new GlFloatBuffer(3);

    public VBOComponent() {
        int vertexShader = ChartRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = ChartRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);

        vertexBuffer.putVertex(-1, -1);
        vertexBuffer.putVertex(0, 1);
        vertexBuffer.putVertex(+1, -1);
    }

    public void draw(int width, int height, float[] mViewMatrix) {
        GLES31.glUseProgram(mProgram);

        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        vertexBuffer.position(0);
        vertexBuffer.bindPointer(mPositionHandle);
        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexBuffer.getVertexCount());
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
