package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ScrollRibbonComponent {
    private static final int FRAME_WIDTH_2 = 20;
    private static final int FRAME_WIDTH_1 = 5;
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

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static float triangleCoords[] = new float[3 * 12];

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    public ScrollRibbonComponent(Model model) {
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

    public void draw(int width, int height, float[] mMVPMatrix) {
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
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

        triangleCoords[18] = rightX + FRAME_WIDTH_2;
        triangleCoords[19] = topY - FRAME_WIDTH_1;

        triangleCoords[21] = width;
        triangleCoords[22] = topY - FRAME_WIDTH_1;

        triangleCoords[24] = width;
        triangleCoords[25] = bottomY;

        triangleCoords[27] = rightX + FRAME_WIDTH_2;
        triangleCoords[28] = topY - FRAME_WIDTH_1;

        triangleCoords[30] = width;
        triangleCoords[31] = bottomY;

        triangleCoords[33] = rightX + FRAME_WIDTH_2;
        triangleCoords[34] = bottomY;


        vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);


        // Prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES31.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES31.glUniform4fv(mColorHandle, 1, ViewConstants.SCROLL_COLOR, 0);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle);
    }
}
