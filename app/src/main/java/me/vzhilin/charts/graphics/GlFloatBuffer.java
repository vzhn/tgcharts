package me.vzhilin.charts.graphics;

import android.opengl.GLES31;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class GlFloatBuffer {
    private final FloatBuffer floatBuffer;
    private final int vertexCount;
    private final int vertexStride;
    private final int coordsPerVertex;

    public GlFloatBuffer(int vertexCount) {
        this(3, vertexCount);
    }

    public GlFloatBuffer(int coordsPerVertex, int vertexCount) {
        this.coordsPerVertex = coordsPerVertex;
        this.vertexCount = vertexCount;
        this.vertexStride = coordsPerVertex * 4;
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexCount * vertexStride);
        bb.order(ByteOrder.nativeOrder());
        floatBuffer = bb.asFloatBuffer();
    }

    public void putVertex(float x, float y) {
        putVertex(x, y, 0);
    }

    public void putVertex(float x, float y, float z) {
        floatBuffer.put(x);
        floatBuffer.put(y);
        floatBuffer.put(z);
    }

    public void putVertex(float r, float g, float b, float a) {
        floatBuffer.put(r);
        floatBuffer.put(g);
        floatBuffer.put(b);
        floatBuffer.put(a);
    }

    public void clear() {
        floatBuffer.clear();
    }

    public void position(int pos) {
        floatBuffer.position(pos);
    }

    public void bindPointer(int handle) {
        GLES31.glVertexAttribPointer(handle, coordsPerVertex,
                GLES31.GL_FLOAT, false,
                vertexStride, floatBuffer);
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
