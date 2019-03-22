package me.vzhilin.charts.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import android.opengl.Matrix;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.data.Column;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import static me.vzhilin.charts.graphics.ScrollComponent.COORDS_PER_VERTEX;

public class ChartLineCapsComponent {
    private final int mProgram;
    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 inputTextureCoordinate;" +
//                    "attribute float opacity;" +
                    "varying vec2 textureCoordinate;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition; " +
                    "  textureCoordinate = inputTextureCoordinate.xy;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 textureCoordinate;" +
                    "uniform sampler2D videoFrame;" +
//                    "varying float vOpacity;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  vec4 color = texture2D(videoFrame, textureCoordinate);" +
//                    "  color.a *= vOpacity;" +
                    "  gl_FragColor = color;" +
                    "}";
    private final Model model;
    private final Column yColumn;
    private final Typewriter tw;
    private final int mInputTextureCoordinate;
    private final int mPositionHandle;
    private final TriangleBuffers buffers;
//    private final int mOpacity;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };


    public ChartLineCapsComponent(Typewriter tw, Model model, Column xColumn, Column yColumn, int c) {
        this.model = model;
        this.yColumn = yColumn;
        this.tw = tw;

        this.buffers = new TriangleBuffers(tw.getCircleTexture());

        color[0] = Color.red(c) / 255f;
        color[1] = Color.green(c) / 255f;
        color[2] = Color.blue(c) / 255f;

        int vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES31.glCreateProgram();

        mPositionHandle = 1;
//        mOpacity = 2;
        mInputTextureCoordinate = 3;

        GLES31.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
//        GLES31.glBindAttribLocation(mProgram, mOpacity, "opacity");
        GLES31.glBindAttribLocation(mProgram, mInputTextureCoordinate, "inputTextureCoordinate");

        // add the vertex shader to program
        GLES31.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES31.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        // get handle to vertex shader's vPosition member
        // Enable a handle to the triangle vertices
        // get handle to vertex shader's vPosition member
        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(mPositionHandle);
//        GLES31.glEnableVertexAttribArray(mOpacity);
        GLES31.glEnableVertexAttribArray(mInputTextureCoordinate);

        drawLineCaps(mPositionHandle);

        // get handle to vertex shader's vPosition member
        // Enable a handle to the triangle vertices
        GLES31.glDisableVertexAttribArray(mPositionHandle);
//        GLES31.glDisableVertexAttribArray(mOpacity);
        GLES31.glDisableVertexAttribArray(mInputTextureCoordinate);
    }

    private void drawLineCaps(int mPositionHandle) {
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);


        // get handle to fragment shader's vColor member
        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");
        color[3] = yColumn.getOpacity();

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = new float[] {
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 0.0f, 0.0f, 1.0f,
        };
        Matrix.setIdentityM(identity, 0);

        // Pass the projection and view transformation to the shader
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        // Set color for drawing the triangle
        GLES31.glUniform4fv(mColorHandle, 1, color, 0);

        // Prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                buffers.getVertexStride(), buffers.getVertexBuffer());

        GLES31.glVertexAttribPointer(mInputTextureCoordinate, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                buffers.getVertexStride(), buffers.getTextureBuffer());

        int mTexture = GLES31.glGetUniformLocation(mProgram, "videoFrame");
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, tw.getTextureId());
        GLES31.glUniform1i(mTexture, 0);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, buffers.getVertexCount());
    }
}
