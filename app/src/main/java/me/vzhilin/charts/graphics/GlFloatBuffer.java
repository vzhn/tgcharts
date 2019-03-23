package me.vzhilin.charts.graphics;

import android.opengl.GLES31;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GlFloatBuffer {
    static final int COORDS_PER_VERTEX = 3;

    private final FloatBuffer floatBuffer;
    private int vertexStride = COORDS_PER_VERTEX * 4;
    private int vertexCount;

    public GlFloatBuffer(int vertexCount) {
        this.vertexCount = vertexCount;
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

    public void clear() {
        floatBuffer.clear();
    }

    public void position(int pos) {
        floatBuffer.position(pos);
    }

    public void bindPointer(int handle) {
        // Prepare the triangle coordinate data
        GLES31.glVertexAttribPointer(handle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false,
                vertexStride, floatBuffer);
    }

    public int getVertexCount() {
        return vertexCount;
    }


}
