package com.schevio.solsim;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.FloatMath;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by chaij on 20/11/14.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLRenderer";
//    private PolyStar3D mPolyStar3D;
    private Square mSquare;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] tempMatrix = new float[16];

    private float mAngle;
    private float mAngle_X;
    private float mAngle_Y;
    private float pole_X;
    private float pole_Y;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

//        mPolyStar3D = new PolyStar3D();
        mSquare = new Square();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] scratch = new float[16];

        // Redraw Background colour
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float x_coord = 4 * FloatMath.sin(mAngle_Y) * FloatMath.cos(mAngle_X);
        float y_coord = 4 * FloatMath.sin(mAngle_Y) * FloatMath.sin(mAngle_X);
        float z_coord = 4 * FloatMath.cos(mAngle_Y);

        // Set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, x_coord, y_coord, z_coord, 0f, 0f, 0f, 0.0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw shape
//        mCircle.draw(mMVPMatrix);
//        mPolygon.draw(mMVPMatrix);
        mSquare.draw(mMVPMatrix);

        // Create a rotation for the shape

        // Use the following code to generate constant rotation.
        // Leave this code out when using TouchEvents.
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);

        Matrix.setRotateM(mRotationMatrix, 0, 0, 0f, 1f, 0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        // Draw star
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
