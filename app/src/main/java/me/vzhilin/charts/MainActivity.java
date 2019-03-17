package me.vzhilin.charts;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView gLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
//        gLView = new MyGLSurfaceView(this);
//        setContentView(gLView);

    }
}
