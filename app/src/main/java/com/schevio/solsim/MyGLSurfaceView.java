package com.schevio.solsim;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by chaij on 20/11/14.
 */
public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private final float TOUCH_SCALE_FACTOR = 180f / 360;
    private float mPreviousX;
    private float mPreviousY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mRenderer.setAngle_X(mRenderer.getAngle_X() + (dy * TOUCH_SCALE_FACTOR));
                mRenderer.setAngle_Y(mRenderer.getAngle_Y() + (dx * TOUCH_SCALE_FACTOR));

                requestRender();

                System.out.println("dx = " + dx);
                System.out.println("dy = " + dy);

                System.out.println("angle = " + mRenderer.getAngle_Y());
                System.out.println("angle = " + mRenderer.getAngle_X());
        }

        mPreviousX = x;
        mPreviousY = y;

        return true;
    }
}