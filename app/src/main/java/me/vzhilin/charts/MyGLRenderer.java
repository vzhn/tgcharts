package me.vzhilin.charts;

import android.content.res.Resources;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import me.vzhilin.charts.graphics.*;
import me.vzhilin.charts.graphics.typewriter.Typewriter;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private final Model model;
    private final Resources resources;

    //    private GridComponent mGridComponent;
    private ScrollChartComponent scrollChartComponent;
    private ScrollRibbonComponent scrollRibbonComponent;
    private ScrollComponent mScrollComponent;
    private DateRibbonComponent mDateComponent;
    private PopupComponent mPopupComponent;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private SpriteRenderer spriteRenderer;

    public MyGLRenderer(Model model, Resources resources) {
        this.model = model;
        this.resources = resources;
    }

    public void onDrawFrame(GL10 unused) {
        long ts = System.currentTimeMillis();

        // Redraw background color
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

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
        mDateComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
        mPopupComponent.draw(model.getWidth(), model.getHeight(), mMVPMatrix);
        spriteRenderer.draw(model.getWidth(), model.getHeight(), mMVPMatrix);

        model.tick();
        mDateComponent.tick();

        long delta = System.currentTimeMillis() - ts;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        // Set the background frame color
        GLES31.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES31.glEnable(GLES31.GL_BLEND);

        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);


        Typewriter tw = new Typewriter(resources);
        spriteRenderer = new SpriteRenderer(tw, model);
        // initialize a triangle
        mScrollComponent = new ScrollComponent(model);
        mDateComponent = new DateRibbonComponent(spriteRenderer, model);
        scrollChartComponent = new ScrollChartComponent(model, spriteRenderer);
        scrollRibbonComponent = new ScrollRibbonComponent(model);
        mPopupComponent = new PopupComponent(model, spriteRenderer);

        model.getGridComponents().add(new GridComponent(spriteRenderer, model));
        model.getGridComponents().add(new GridComponent(spriteRenderer, model));
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, +width, height, 0, 3, 7);

        model.setWidth(width);
        model.setHeight(height);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES31.glCreateShader(type);

        GLES31.glShaderSource(shader, shaderCode);
        GLES31.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES31.glDeleteShader(shader);
            throw new RuntimeException("Could not compile program: "
                    + GLES31.glGetShaderInfoLog(shader) + " | " + shaderCode);
        }

        return shader;
    }
}