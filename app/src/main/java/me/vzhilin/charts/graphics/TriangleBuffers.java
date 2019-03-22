package me.vzhilin.charts.graphics;

import me.vzhilin.charts.graphics.typewriter.Typewriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TriangleBuffers {
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final Typewriter.TextureCharacter ch;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    private int vertexStride = COORDS_PER_VERTEX * 4;
    private int vertexCount = 6; // 4 bytes per vertex

    public TriangleBuffers(Typewriter.TextureCharacter circleCharacter) {
        this.ch = circleCharacter;
        buildVertexBuffer();
        buildTextureBuffer();
    }

    private void buildTextureBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCount * vertexStride);
        bb.order(ByteOrder.nativeOrder());

        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(ch.x1);
        textureBuffer.put(ch.y1);
        textureBuffer.put(0);

        textureBuffer.put(ch.x2);
        textureBuffer.put(ch.y1);
        textureBuffer.put(0);

        textureBuffer.put(ch.x1);
        textureBuffer.put(ch.y2);
        textureBuffer.put(0);

        textureBuffer.put(ch.x2);
        textureBuffer.put(ch.y1);
        textureBuffer.put(0);

        textureBuffer.put(ch.x2);
        textureBuffer.put(ch.y2);
        textureBuffer.put(0);

        textureBuffer.put(ch.x1);
        textureBuffer.put(ch.y2);
        textureBuffer.put(0);

        textureBuffer.position(0);
    }

    private void buildVertexBuffer() {
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCount * vertexStride);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(-0.5f);
        vertexBuffer.put(-0.5f);
        vertexBuffer.put(0);

        vertexBuffer.put(0.5f);
        vertexBuffer.put(-0.5f);
        vertexBuffer.put(0);

        vertexBuffer.put(-0.5f);
        vertexBuffer.put(0.5f);
        vertexBuffer.put(0);

        vertexBuffer.put(0.5f);
        vertexBuffer.put(-0.5f);
        vertexBuffer.put(0);

        vertexBuffer.put(0.5f);
        vertexBuffer.put(0.5f);
        vertexBuffer.put(0);

        vertexBuffer.put(-0.5f);
        vertexBuffer.put(0.5f);
        vertexBuffer.put(0);
        vertexBuffer.position(0);

    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public FloatBuffer getTextureBuffer() {
        return textureBuffer;
    }

    public int getVertexStride() {
        return vertexStride;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
