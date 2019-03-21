package me.vzhilin.charts;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import me.vzhilin.charts.graphics.*;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final Model model;

//    private GridComponent mGridComponent;
    private ScrollChartComponent scrollChartComponent;
    private ScrollRibbonComponent scrollRibbonComponent;
    private ScrollComponent mScrollComponent;
    private TextComponent textComponent;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];


    public MyGLRenderer(Model model) {
        this.model = model;
    }

    public void onDrawFrame(GL10 unused) {

        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, +6, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Create a rotation and translation for the cube
        Matrix.setIdentityM(mRotationMatrix, 0);

        Matrix.translateM(mRotationMatrix, 0, 0, 0, 0);

        //Assign mRotationMatrix a rotation with the seekbar
//        Matrix.rotateM(mRotationMatrix, 0, zoom * 3.6f, 1.0f, 1.0f, 1.0f);

        // combine the model with the view matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mRotationMatrix, 0);

        // combine the model-view with the projection matrix
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        for (GridComponent gc: model.getGridComponents()) {
            gc.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
            gc.tick();
        }
        mScrollComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
        scrollChartComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
        scrollRibbonComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
        textComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);

        model.tick();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // initialize a triangle
        mScrollComponent = new ScrollComponent(model);
        scrollChartComponent = new ScrollChartComponent(model);
        scrollRibbonComponent = new ScrollRibbonComponent(model);
        textComponent = new TextComponent(model);

        model.getGridComponents().add(new GridComponent(model));
        model.getGridComponents().add(new GridComponent(model));
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, +width, height, 0, 3, 7);

        model.setWidth(width);
        model.setHeight(height);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}