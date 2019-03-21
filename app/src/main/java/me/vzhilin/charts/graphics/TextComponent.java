package me.vzhilin.charts.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class TextComponent {
    private static final int FRAME_WIDTH_2 = 20;
    private static final int FRAME_WIDTH_1 = 5;
    private final Model model;

    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec4 inputTextureCoordinate;" +
        "varying vec2 textureCoordinate;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        // Note that the uMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  textureCoordinate = inputTextureCoordinate.xy;" +
        "}";
    private final Typewriter tw;

//    private final int textureId;

//    private final String vertexShaderCode =
//            // This matrix member variable provides a hook to manipulate
//            // the coordinates of the objects that use this vertex shader
//            "uniform mat4 uMVPMatrix;" +
//            "attribute vec4 vPosition;" +
//            "void main() {" +
//            // the matrix must be included as a modifier of gl_Position
//            // Note that the uMVPMatrix factor *must be first* in order
//            // for the matrix multiplication product to be correct.
//            "  gl_Position = uMVPMatrix * vPosition;" +
//            "}";

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;

//    private final String fragmentShaderCode =
//            "precision mediump float;" +
//                    "uniform vec4 vColor;" +
//                    "void main() {" +
//                    "  gl_FragColor = vColor;" +
//                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;"+
            "varying vec2 textureCoordinate;" +
            "uniform sampler2D videoFrame;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = texture2D(videoFrame, textureCoordinate);" +
            "}";

    private final int mProgram;

//    private final int vertexCount = ;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

//    static float triangleCoords[] = new float[3 * 12];

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    static float squareVertices[] = {
            0f, 0f, 0f,
            1.0f, 0f, 0f,
            0f,  1.0f, 0f,

            1.0f, 0f, 0f,
            1.0f,  1.0f, 0f,
            0f,  1.0f, 0f,
    };

    static float textureVertices[] = {
            0.0f, 1.0f, 0f,
            +1.0f, 1.0f, 0f,
            0.0f, 0.0f, 0f,

            +1.0f, 1.0f, 0f,
            0.0f, 0.0f, 0f,
            +1.0f, 0.0f, 0f,
    };

    public TextComponent(Model model) {
        this.model = model;

        tw = new Typewriter();

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer vertexBB = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                squareVertices.length * 4);
        // use the device hardware's native byte order
        vertexBB.order(ByteOrder.nativeOrder());
        // create a floating point buffer from the ByteBuffer
        vertexBuffer = vertexBB.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(squareVertices);
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

    public void draw(int width, int height, float[] mMVPMatrix) {
        // Add program to OpenGL ES environment
        drawString(100, 100, "Hello world", mMVPMatrix);
    }

    public void drawString(int x, int y, String s, float[] mMVPMatrix) {
        GLES20.glUseProgram(mProgram);

        prepareBuffers(s.length());
        fillBuffers(x, y, s);
        drawBuffer(x, y, mMVPMatrix);

    }

    private void prepareBuffers(int length) {
        squareVertices = new float[18 * length];
        textureVertices = new float[18 * length];
    }

    private void fillBuffers(int x, int y, String s) {
        float offset = 0;
        for (int i = 0; i < s.length(); i++) {
            Typewriter.TextureCharacter ch = tw.get(s.charAt(i));
            float width = ch.width;

            float x1 = x + offset, y1 = y, x2 = x + offset + width, y2 = y + tw.getHeight();

            squareVertices[i * 18 + 0] = x1;
            squareVertices[i * 18 + 1] = y1;

            squareVertices[i * 18 + 3] = x2;
            squareVertices[i * 18 + 4] = y1;

            squareVertices[i * 18 + 6] = x1;
            squareVertices[i * 18 + 7] = y2;

            squareVertices[i * 18 + 9] = x2;
            squareVertices[i * 18 + 10] = y1;

            squareVertices[i * 18 + 12] = x2;
            squareVertices[i * 18 + 13] = y2;

            squareVertices[i * 18 + 15] = x1;
            squareVertices[i * 18 + 16] = y2;

            textureVertices[i * 18 + 0] = ch.x1;
            textureVertices[i * 18 + 1] = ch.y1;

            textureVertices[i * 18 + 3] = ch.x2;
            textureVertices[i * 18 + 4] = ch.y1;

            textureVertices[i * 18 + 6] = ch.x1;
            textureVertices[i * 18 + 7] = ch.y2;

            textureVertices[i * 18 + 9] = ch.x2;
            textureVertices[i * 18 + 10] = ch.y1;

            textureVertices[i * 18 + 12] = ch.x2;
            textureVertices[i * 18 + 13] = ch.y2;

            textureVertices[i * 18 + 15] = ch.x1;
            textureVertices[i * 18 + 16] = ch.y2;

            offset += width;
        }

        ByteBuffer vertexBB = ByteBuffer.allocateDirect(squareVertices.length * 4);
        vertexBB.order(ByteOrder.nativeOrder());
        vertexBuffer = vertexBB.asFloatBuffer();
        vertexBuffer.put(squareVertices);
        vertexBuffer.position(0);

        ByteBuffer textureBB = ByteBuffer.allocateDirect(textureVertices.length * 4);
        textureBB.order(ByteOrder.nativeOrder());
        textureBuffer = textureBB.asFloatBuffer();
        textureBuffer.put(textureVertices);
        textureBuffer.position(0);
    }

    private void drawBuffer(int x, int y, float[] mMVPMatrix) {
        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int mInputTextureCoordinate = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();
        vertexBuffer.put(squareVertices);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        textureBuffer.clear();
        textureBuffer.put(textureVertices);
        GLES20.glEnableVertexAttribArray(mInputTextureCoordinate);
        textureBuffer.position(0);

        GLES20.glVertexAttribPointer(mInputTextureCoordinate, 3,
                GLES20.GL_FLOAT, false,
                vertexStride, textureBuffer);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        int mTexture = GLES20.glGetUniformLocation(mProgram, "videoFrame");

        float[] identity = Arrays.copyOf(mMVPMatrix, 16);
        Matrix.translateM(identity, 0, x, y,0);
//        Matrix.scaleM(identity, 0, 100, -100, 1);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tw.getTextureId());
        GLES20.glUniform1i(mTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, squareVertices.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mInputTextureCoordinate);
    }
}

