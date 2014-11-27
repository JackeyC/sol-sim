package com.schevio.solsim;

import android.annotation.TargetApi;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by chaij on 20/11/14.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    float angle;

    private static final String TAG = "MyGLRenderer";
//    private PolyStar3D mPolyStar3D;
    private Earth mEarth;
    private SpaceShip mSpaceShip;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTiltMatrix = new float[16];
    private final float[] mEarthRotationMatrix = new float[16];

    private float mAngle_X;
    private float mAngle_Y;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        mEarth = new Earth();
        mSpaceShip = new SpaceShip();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Redraw Background colour
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float cam_distance = 4;

        if (mAngle_X <= -MyGLSurfaceView.TwoPi | mAngle_X >= MyGLSurfaceView.TwoPi) {
            mAngle_X = 0;
        }
        if (mAngle_Y <= -MyGLSurfaceView.TwoPi | mAngle_Y >= MyGLSurfaceView.TwoPi) {
            mAngle_Y = 0;
        }

        float cam_x = cam_distance * FloatMath.sin(mAngle_X);
        float cam_y = cam_distance * FloatMath.cos(mAngle_X) * FloatMath.cos(mAngle_Y);
        float cam_z = cam_distance * FloatMath.sin(mAngle_Y);

        //Debug
//        System.out.println("cam x = " + cam_x);
//        System.out.println("cam y = " + cam_y);
//        System.out.println("cam z = " + cam_z);

        // Set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, cam_x, cam_y, cam_z, 0f, 0f, 0f, 0f, 0f, 1f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw static
//        mSpaceShip.draw(mMVPMatrix);

        // Create a rotation for the shape
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);


        if (angle >= 360) {
            angle = 0;
        }

        //Debug
//        System.out.println("self rotate angle = "+ angle);

        //Matrix.setRotateM(mTiltMatrix, 0, -23.45f, 0f, 1f, 0f);
        Matrix.setRotateM(mTiltMatrix, 0, 0f, 0f, 1f, 0f);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1f);

        angle += 0.1;

        Matrix.multiplyMM(mEarthRotationMatrix, 0, mTiltMatrix, 0, mRotationMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mEarthRotationMatrix, 0);

        // Draw rotate
        mEarth.draw(scratch);
//        mSpaceShip.draw(scratch);
//        mPolyStar3D.draw(scratch);
//        Matrix.setRotateM(mRotationMatrix, 0, (mAngle + 180) % 360, 0f, 1f, 0f);
//        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

//        mPolyStar3D.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }


    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle com.example.android.shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle_X() {
        return mAngle_X;
    }
    public float getAngle_Y() {
        return mAngle_Y;
    }

    /**
     * Sets the rotation angle of the triangle com.example.android.shape (mTriangle).
     */
    public void setAngle_X(float angle_X) {
        mAngle_X = angle_X;
    }
    public void setAngle_Y(float angle_Y) {
        mAngle_Y = angle_Y;
    }
}
