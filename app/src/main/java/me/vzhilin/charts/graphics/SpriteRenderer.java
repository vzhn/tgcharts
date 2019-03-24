package me.vzhilin.charts.graphics;

import android.graphics.Color;
import android.opengl.GLES31;
import me.vzhilin.charts.Model;
import me.vzhilin.charts.MyGLRenderer;
import me.vzhilin.charts.ViewConstants;
import me.vzhilin.charts.data.Column;
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
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 inputTextureCoordinate;" +
            "attribute vec4 color;" +
            "varying vec2 textureCoordinate;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  vColor = color;" +
            "  textureCoordinate = inputTextureCoordinate.xy;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;"+
            "uniform sampler2D videoFrame;" +
            "varying vec4 vColor;" +
            "varying vec2 textureCoordinate;" +
            "void main() {" +
            "  vec4 color = texture2D(videoFrame, textureCoordinate);" +
            "  color.rgb += vColor.rgb;" +
            "  color.a *= vColor.a;" +
            "  gl_FragColor = color;" +
            "}";

    private final Typewriter tw;

    private final int mPositionHandle;
    private final int mColor;
    private final int mInputTextureCoordinate;
    private final int mProgram;


    //    private FloatBuffer vertexBuffer;
    private FloatBuffer floatBuffer;
    private GlFloatBuffer textureBuffer;
    private GlFloatBuffer squareBuffer;
    private GlFloatBuffer colorBuffer;

    private final List<StringSprite> stringSprites = new ArrayList<StringSprite>();
    private final List<TextureSprite> sprites = new ArrayList<>();

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0, 0, 0, 1.0f };

    static float colorVertices[];


    public SpriteRenderer(Model model) {
        this.tw = model.getTypewriter();

        for (Column column: model.getChart().getYColumns()) {
            int columnColor = model.getChart().getColor(column.getLabel());

            column.setMarkerSpriteId(tw.newMarker(columnColor));
        }

        int vertexShader = MyGLRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES31.glCreateProgram();

        mPositionHandle = 1;
        mColor = 2;
        mInputTextureCoordinate = 3;

        GLES31.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
        GLES31.glBindAttribLocation(mProgram, mColor, "color");
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

    public void drawSprite(int id, float x, float y, int color, float opacity) {
        sprites.add(new TextureSprite(tw.getSprite(id), x, y, color, opacity));
    }

    public void drawSprite(int id, float x, float y, int color, float opacity, float sx, float sy) {
        sprites.add(new TextureSprite(tw.getSprite(id), x, y, color, opacity, sx, sy));
    }

    public void drawString(String string, int x, int y, int color, float opacity) {
        stringSprites.add(new StringSprite(x, y, string, color, opacity, Typewriter.FontType.NORMAL_FONT));
    }

    public void drawString(String string, int x, int y, int color, float opacity, Typewriter.FontType type) {
        stringSprites.add(new StringSprite(x, y, string, color, opacity, type));
    }

    private void prepareBuffers() {
        int totalSprites = 0;
        for (StringSprite sc: stringSprites) {
            totalSprites += sc.s.length();
        }

        totalSprites += sprites.size();

        squareBuffer = new GlFloatBuffer(6 * totalSprites);
        textureBuffer = new GlFloatBuffer(6 * totalSprites);
        colorBuffer   = new GlFloatBuffer(4, 6 * totalSprites);

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

                squareBuffer.putVertex(x1, y1);
                squareBuffer.putVertex(x2, y1);
                squareBuffer.putVertex(x1, y2);
                squareBuffer.putVertex(x2, y1);
                squareBuffer.putVertex(x2, y2);
                squareBuffer.putVertex(x1, y2);

                textureBuffer.putVertex(ch.x1, ch.y1);
                textureBuffer.putVertex(ch.x2, ch.y1);
                textureBuffer.putVertex(ch.x1, ch.y2);
                textureBuffer.putVertex(ch.x2, ch.y1);
                textureBuffer.putVertex(ch.x2, ch.y2);
                textureBuffer.putVertex(ch.x1, ch.y2);

                for (int k = 0; k < 6; k++) {
                    float r = Color.red(sc.color) / 255.0f;
                    float g = Color.green(sc.color) / 255.0f;
                    float b = Color.blue(sc.color) / 255.0f;
                    colorBuffer.putVertex(r, g, b, sc.opacity);
                }
                offset += width;
                ++i;
            }
        }

        for (TextureSprite tx: sprites) {
            Typewriter.TextureCharacter ch = tx.character;
            float x1 = tx.x, y1 = tx.y, x2 = tx.x + ch.width * tx.sx, y2 = tx.y + ch.height * tx.sy;

            squareBuffer.putVertex(x1, y1);
            squareBuffer.putVertex(x2, y1);
            squareBuffer.putVertex(x1, y2);
            squareBuffer.putVertex(x2, y1);
            squareBuffer.putVertex(x2, y2);
            squareBuffer.putVertex(x1, y2);

            textureBuffer.putVertex(ch.x1, ch.y1);
            textureBuffer.putVertex(ch.x2, ch.y1);
            textureBuffer.putVertex(ch.x1, ch.y2);
            textureBuffer.putVertex(ch.x2, ch.y1);
            textureBuffer.putVertex(ch.x2, ch.y2);
            textureBuffer.putVertex(ch.x1, ch.y2);

            for (int k = 0; k < 6; k++) {
                float r = Color.red(tx.color) / 255.0f;
                float g = Color.green(tx.color) / 255.0f;
                float b = Color.blue(tx.color) / 255.0f;
                colorBuffer.putVertex(r, g, b, tx.opacity);
            }
            ++i;
        }

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
        GLES31.glEnableVertexAttribArray(mColor);
        GLES31.glEnableVertexAttribArray(mInputTextureCoordinate);

        squareBuffer.position(0);
        textureBuffer.position(0);
        colorBuffer.position(0);

        squareBuffer.bindPointer(mPositionHandle);
        textureBuffer.bindPointer(mInputTextureCoordinate);
        colorBuffer.bindPointer(mColor);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");

        float[] identity = Arrays.copyOf(mMVPMatrix, 16);
        GLES31.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, identity, 0);

        int mTexture = GLES31.glGetUniformLocation(mProgram, "videoFrame");
        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, tw.getTextureId());
        GLES31.glUniform1i(mTexture, 0);

        GLES31.glDrawArrays(GLES31.GL_TRIANGLES, 0, squareBuffer.getVertexCount());

        // Disable vertex array
        GLES31.glDisableVertexAttribArray(mPositionHandle);
        GLES31.glDisableVertexAttribArray(mColor);
        GLES31.glDisableVertexAttribArray(mInputTextureCoordinate);
    }

    public Typewriter getTypewriter() {
        return tw;
    }

    private class TextureSprite {
        private final Typewriter.TextureCharacter character;
        public final float x;
        public final float y;
        public final float sx;
        public final float sy;
        public final float opacity;
        public final int color;


        public TextureSprite(Typewriter.TextureCharacter character,
                             float x,
                             float y,
                             int color,
                             float opacity,
                             float sx,
                             float sy) {
            this.color = color;
            this.character = character;
            this.x = x;
            this.y = y;
            this.opacity = opacity;
            this.sx = sx;
            this.sy = sy;
        }

        public TextureSprite(Typewriter.TextureCharacter character,
                             float x,
                             float y,
                             int color,
                             float opacity) {

            this(character, x, y, color, opacity, 1f, 1f);
        }
    }
}

