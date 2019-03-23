package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PopupComponent {
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
            "  gl_FragColor = vec4(1.0, 0.5, 0.5, 1.0);" +
            "}";

    private final int mProgram;

    static float triangleCoords[] = new float[6 * 4];
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final Model model;
    private final SpriteRenderer tw;

    public PopupComponent(Model model, SpriteRenderer tw) {
        this.model = model;
        this.tw = tw;

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

//        model.getChart().getXColumn().values().
    }

    public void draw(int width, int height, float[] mMVPMatrix) {

        List<Sample> samples = new ArrayList<>();
        samples.add(new Sample("Joined", String.format("%.0f", 122f, Locale.US)));
        samples.add(new Sample("Left", String.format("%.0f", 75f, Locale.US)));
        samples.add(new Sample("Extra", String.format("%.0f", 123456f, Locale.US)));
        samples.add(new Sample("A", String.format("%.0f", 55f, Locale.US)));
        samples.add(new Sample("B", String.format("%.0f", 6f, Locale.US)));

        float boldHeight = tw.getTypewriter().getContext(Typewriter.FontType.BOLD_FONT).fontHeight;

        int w = 0;
        int h = 0;
        h += boldHeight;
        for (Sample s: samples) {
            w += s.getWidth(tw.getTypewriter());
        }

        h += samples.get(0).getHeight(tw.getTypewriter());

        w += (samples.size() - 1) * 20;

        int popupX = 200;
        int popupY = 200;
        int popupWidth = w;
        int popupHeight = h;

        drawBorder(popupX, popupY, popupWidth, popupHeight, mMVPMatrix);
        drawText(samples, popupX, popupY, popupWidth, popupHeight, mMVPMatrix);
        drawGeometry();
//        for
    }

    private void drawGeometry() {
        model.getX(model.getPopupDate());
    }

    private void drawText(List<Sample> samples, int popupX, int popupY, int popupWidth, int popupHeight, float[] mMVPMatrix) {
        String date = ViewConstants.FORMATTER_WITH_DATE.format(model.getPopupDate());

        Typewriter tpw = tw.getTypewriter();
        float boldHeight = tpw.getContext(Typewriter.FontType.BOLD_FONT).fontHeight;
        float bigHeight = tpw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;
        float normalHeight = tpw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;

        popupY += boldHeight;

        tw.drawString(date, popupX, popupY, 1.0f, Typewriter.FontType.BOLD_FONT);

        int sampleX = popupX + 0;
        int sampleY = popupY;
        for (Sample s: samples) {
            tw.drawString(s.value, sampleX, (int) (sampleY + bigHeight), 1.0f, Typewriter.FontType.BIG_FONT);
            tw.drawString(s.label, sampleX, (int) (sampleY + bigHeight + normalHeight), 1.0f, Typewriter.FontType.NORMAL_FONT);

            sampleX += s.getWidth(tpw);
            sampleX += 20;
        }
    }

    private void drawBorder(int popupX, int popupY, int popupWidth, int popupHeight, float[] mMVPMatrix) {
        // Add program to OpenGL ES environment
        GLES31.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES31.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        vertexBuffer.clear();

        triangleCoords[0] = popupX;
        triangleCoords[1] = popupY;

        triangleCoords[3] = popupX;
        triangleCoords[4] = popupY + popupHeight;

        triangleCoords[6] = popupX + popupWidth;
        triangleCoords[7] = popupY;

        triangleCoords[9]  = popupX + popupWidth;
        triangleCoords[10] = popupY + popupHeight;

        triangleCoords[12] = popupX;
        triangleCoords[13] = popupY + popupHeight;

        triangleCoords[15] = popupX + popupWidth;
        triangleCoords[16] = popupY;

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

    private final static class Sample {
        private final String label;
        private final String value;
//        private final Typewriter.FontType type;

        public Sample(String label, String value) {
            this.label = label;
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public String getValue() {
            return value;
        }

        public double getWidth(Typewriter tw) {
            double labelWidth = tw.getContext(Typewriter.FontType.NORMAL_FONT).stringWidth(label);
            double valueWidth = tw.getContext(Typewriter.FontType.BIG_FONT).stringWidth(value);

            return Math.max(labelWidth, valueWidth);
        }

        public double getHeight(Typewriter tw) {
            double labelHeight = tw.getContext(Typewriter.FontType.NORMAL_FONT).fontHeight;
            double valueHeight = tw.getContext(Typewriter.FontType.BIG_FONT).fontHeight;

            return labelHeight + valueHeight;
        }
    }
}
