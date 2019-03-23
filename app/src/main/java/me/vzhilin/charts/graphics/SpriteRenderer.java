package me.vzhilin.charts.graphics;

import android.opengl.GLES31;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.graphics.typewriter.FontContext;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpriteRenderer {
    private final String vertexShaderCode =
        // This matrix member variable provides a hook to manipulate
        // the coordinates of the objects that use this vertex shader
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec4 inputTextureCoordinate;" +
        "attribute float opacity;" +
        "varying vec2 textureCoordinate;" +
        "varying float vOpacity;" +
        "void main() {" +
        // the matrix must be included as a modifier of gl_Position
        // Note that the uMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  vOpacity = opacity;" +
        "  textureCoordinate = inputTextureCoordinate.xy;" +
        "}";

    private final String fragmentShaderCode =
        "precision mediump float;"+
        "varying vec2 textureCoordinate;" +
        "varying float vOpacity;" +
        "uniform sampler2D videoFrame;" +
        "uniform vec4 vColor;" +
        "void main() {" +
        "  vec4 color = texture2D(videoFrame, textureCoordinate);" +
//        "  color.r += 0.5;" +
//        "  color.a += 0.5;" +
        "  color.a *= vOpacity;" + //FIXME
        "  gl_FragColor = color;" +
        "}";

    private final Typewriter tw;

    private final int mPositionHandle;
    private final int mOpacity;
    private final int mInputTextureCoordinate;


    private final int mProgram;

//    private final int vertexCount = ;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer floatBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    private final List<StringSprite> stringSprites = new ArrayList<StringSprite>();
    private final List<TextureSprite> sprites = new ArrayList<>();

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

    static float colorVertices[];

    public SpriteRenderer(Typewriter tw, Model model) {
        this.tw = tw;

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

        int vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES31.glCreateProgram();

        mPositionHandle = 1;
        mOpacity = 2;
        mInputTextureCoordinate = 3;

        GLES31.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
        GLES31.glBindAttribLocation(mProgram, mOpacity, "opacity");
        GLES31.glBindAttribLocation(mProgram, mInputTextureCoordinate, "inputTextureCoordinate");

        // add the vertex shader to program
        GLES31.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES31.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(int width, int height, float[] mMVPMatrix) {
        GLES31.glUseProgram(mProgram);

        prepareBuffers();
        fillBuffers();
        drawBuffer(mMVPMatrix);

        sprites.clear();
        stringSprites.clear();
    }


    public void drawSprite(Typewriter.TextureCharacter character, int x, int y) {
        sprites.add(new TextureSprite(character, x, y));
    }

    public void drawString(String string, int x, int y, float opacity) {
        stringSprites.add(new StringSprite(x, y, string, opacity, Typewriter.FontType.NORMAL_FONT));
    }

    public void drawString(String string, int x, int y, float opacity, Typewriter.FontType type) {
        stringSprites.add(new StringSprite(x, y, string, opacity, type));
    }

    private void prepareBuffers() {
        int totalSprites = 0;
        for (StringSprite sc: stringSprites) {
            totalSprites += sc.s.length();
        }

        totalSprites += sprites.size();

        squareVertices = new float[18 * totalSprites];
        textureVertices = new float[18 * totalSprites];
        colorVertices = new float[6 * totalSprites];
    }

    private void fillBuffers() {
        int i = 0;

        for (StringSprite sc: stringSprites) {
            float offset = sc.x;

            FontContext context = tw.getContext(sc.size);
            for (int j = 0; j < sc.s.length(); j++) {
                Typewriter.TextureCharacter ch = context.get(sc.s.charAt(j));
                float width = ch.width;

                float x1 = offset, y1 = sc.y - ch.height, x2 = offset + width, y2 = sc.y;

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

                colorVertices[i * 6 + 0] = sc.opacity;
                colorVertices[i * 6 + 1] = sc.opacity;
                colorVertices[i * 6 + 2] = sc.opacity;
                colorVertices[i * 6 + 3] = sc.opacity;
                colorVertices[i * 6 + 4] = sc.opacity;
                colorVertices[i * 6 + 5] = sc.opacity;
                offset += width;
                ++i;
            }
        }

        for (TextureSprite tx: sprites) {
            Typewriter.TextureCharacter ch = tx.character;
            float x1 = tx.x, y1 = tx.y, x2 = tx.x + ch.width, y2 = tx.y + ch.width;

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

            colorVertices[i * 6 + 0] = 1f;
            colorVertices[i * 6 + 1] = 1f;
            colorVertices[i * 6 + 2] = 1f;
            colorVertices[i * 6 + 3] = 1f;
            colorVertices[i * 6 + 4] = 1f;
            colorVertices[i * 6 + 5] = 1f;
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

        ByteBuffer floatBB = ByteBuffer.allocateDirect(colorVertices.length * 4);
        floatBB.order(ByteOrder.nativeOrder());
        floatBuffer = floatBB.asFloatBuffer();
        floatBuffer.put(colorVertices);
        floatBuffer.position(0);
    }

    private void drawBuffer(float[] mMVPMatrix) {
        // get handle to vertex shader's vPosition member
        // Enable a handle to the triangle vertices
        GLES31.glEnableVertexAttribArray(mPositionHandle);
        GLES31.glEnableVertexAttribArray(mOpacity);
        GLES31.glEnableVertexAttribArray(mInputTextureCoordinate);

        vertexBuffer.clear();
        vertexBuffer.put(squareVertices);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);
        // Prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        textureBuffer.clear();
        textureBuffer.put(textureVertices);

        textureBuffer.position(0);

        GLES31.glVertexAttribPointer(mInputTextureCoordinate, 3,
                GLES31.GL_FLOAT, false,
                vertexStride, textureBuffer);

        floatBuffer.clear();
        floatBuffer.put(colorVertices);
        floatBuffer.position(0);
        GLES31.glVertexAttribPointer(mOpacity, 1, GLES31.GL_FLOAT, false, 4, floatBuffer);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = Arrays.copyOf(mMVPMatrix, 16);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        int mTexture = GLES31.glGetUniformLocation(mProgram, "videoFrame");
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, tw.getTextureId());
        GLES31.glUniform1i(mTexture, 0);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, squareVertices.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle);
        GLES31.glDisableVertexAttribArray(mInputTextureCoordinate);
        GLES31.glDisableVertexAttribArray(mOpacity);
    }

    public Typewriter getTypewriter() {
        return tw;
    }

    private class TextureSprite {
        private final Typewriter.TextureCharacter character;
        public final int x;
        public final int y;

        public TextureSprite(Typewriter.TextureCharacter character, int x, int y) {
            this.character = character;
            this.x = y;
            this.y = y;
        }
    }
}

