package com.schevio.solsim;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
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
    private SkyBox mSkyBox;
    private Axis mAxis;
    private Sun mSun;
    private Earth mEarth;
    private Moon mMoon;
    private SpaceShip mSpaceShip;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mEarthTiltMatrix = new float[16];
    private final float[] mEarthRotationMatrix = new float[16];
    private final float[] mMoonMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];

    private float mAngle_X;
    private float mAngle_Y;
    private float mCam_distance = 7f;

    float cam_x;
    float cam_y;
    float cam_z;
    float focus_x;
    float focus_y;
    float focus_z;

    int mSpeed = 1000;
    int Earth_day_period;

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    /** This is a handle to our light point program. */
    private int mPointProgramHandle;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

        mSkyBox = new SkyBox();
        mAxis = new Axis();
        mSun = new Sun();
        mEarth = new Earth();
        mMoon = new Moon();
        mSpaceShip = new SpaceShip();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] SkyBox = new float[16];
        float[] sun = new float[16];
        float[] earth = new float[16];
        float[] moon = new float[16];

        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Enable depth testing
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
//        GLES20.glDepthMask(true );

        if (mAngle_X <= -MyGLSurfaceView.TwoPi | mAngle_X >= MyGLSurfaceView.TwoPi) {
            mAngle_X = 0;
        }
        if (mAngle_Y < -MyGLSurfaceView.HalfPi | mAngle_Y > MyGLSurfaceView.HalfPi) {
            if (mAngle_Y < -MyGLSurfaceView.HalfPi) {
                mAngle_Y = -MyGLSurfaceView.HalfPi;
            } else {
                mAngle_Y = MyGLSurfaceView.HalfPi;
            }
        }

//        int max = 360;
//        int i=0;
//        i = (i+1)%max;
//        i = (i-1+max)%max;

        cam_x = -mCam_distance * FloatMath.sin(mAngle_X) * FloatMath.cos(mAngle_Y);
        cam_y = -mCam_distance * FloatMath.cos(mAngle_X) * FloatMath.cos(mAngle_Y);
        cam_z = mCam_distance * FloatMath.sin(mAngle_Y);

        // Set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, cam_x, cam_y, cam_z, focus_x, focus_y, focus_z, 0f, 0f, 1f);

        // Define a simple shader program for our point.
//        final String pointVertexShader =
//                "uniform mat4 u_MVPMatrix;      \n"
//                        +	"attribute vec4 aPosition;     \n"
//                        + "void main()                    \n"
//                        + "{                              \n"
//                        + "   gl_Position = uMVPMatrix * aPosition;   \n"
//                        + "   gl_PointSize = 5.0;         \n"
//                        + "}                              \n";
//
//        final String pointFragmentShader =
//                "precision mediump float;       \n"
//                        + "void main()                    \n"
//                        + "{                              \n"
//                        + "   gl_FragColor = vec4(1.0,    \n"
//                        + "   1.0, 1.0, 1.0);             \n"
//                        + "}                              \n";

//        final int pointVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
//        final int pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
//        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
//                new String[] {"a_Position"});

//        final String vertexShader = getVertexShader();

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw static
        Matrix.scaleM(SkyBox, 0, mMVPMatrix, 0, 200, 200, 200);
//        mSkyBox.draw(SkyBox);
//        mSpaceShip.draw(mMVPMatrix);

        if (mSpeed > 0) {
            Earth_day_period = 3600000 / mSpeed;
        }
        else {
            mSpeed = 1;
        }

        long time = SystemClock.uptimeMillis() % Earth_day_period;
        float Earth_day = 360f / Earth_day_period * ((int) time);
//        float radian = MyGLSurfaceView.TwoPi / Earth_day_period * ((int) time);

        int Earth_year_period = Earth_day_period * 365;
        long time2 = SystemClock.uptimeMillis() % Earth_year_period;
        float Earth_year = 360f / Earth_year_period * ((int) time2);

        int Moon_period = Earth_day_period * 27;
        long time1 = SystemClock.uptimeMillis() % Moon_period;
        float Moon_orbit = 360f / Moon_period * ((int) time1);

        Matrix.setRotateM(mEarthTiltMatrix, 0, 23.45f, 0f, 1f, 0f);
//        Matrix.setRotateM(mEarthTiltMatrix, 0, 0f, 0f, 1f, 0f);
        Matrix.setRotateM(mRotationMatrix, 0, Earth_day, 0f, 0f, 1f);

        Matrix.multiplyMM(mEarthRotationMatrix, 0, mEarthTiltMatrix, 0, mRotationMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(earth, 0, mMVPMatrix, 0, mEarthRotationMatrix, 0);
        Matrix.scaleM(earth, 0, 3, 3, 3);
        mEarth.draw(earth);
        mAxis.draw(earth);

        Matrix.setIdentityM(mMoonMatrix, 0);
        Matrix.rotateM(mMoonMatrix, 0, Moon_orbit, 0, 0, 1);
        Matrix.translateM(mMoonMatrix, 0, 5, 0, 0);
        Matrix.multiplyMM(moon, 0, mMVPMatrix, 0, mMoonMatrix, 0);
        mMoon.draw(moon);
        mAxis.draw(moon);

        Matrix.setIdentityM(mMoonMatrix, 0);
        Matrix.rotateM(mMoonMatrix, 0, Earth_year, 0, 0, 1);
        Matrix.translateM(mMoonMatrix, 0, -20, 0, 0);
        Matrix.rotateM(mMoonMatrix, 0, Moon_orbit, 0, 0, 1);
        Matrix.multiplyMM(sun, 0, mMVPMatrix, 0, mMoonMatrix, 0);
        Matrix.scaleM(sun, 0, 5, 5, 5);
        mSun.draw(sun);
        mAxis.draw(sun);

//        mSpaceShip.draw(scratch);
//        mPolyStar3D.draw(scratch);
//        Matrix.setRotateM(mRotationMatrix, 0, (mAngle + 180) % 360, 0f, 1f, 0f);
//        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

//        mPolyStar3D.draw(scratch);
        // Draw a point to indicate the light.
//        GLES20.glUseProgram(mPointProgramHandle);
//        drawLight();

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
//        GLES20.glClearDepthf(1.0f);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
//        GLES20.glDepthMask(true);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far)
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 1000);
//        Matrix.perspectiveM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 1000);
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
    public int getSpeed() {
        return mSpeed;
    }
    public float getCam_distance() {
        return mCam_distance;
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
    public void setSpeed(int speed) {
        mSpeed = speed;
    }
    public void setCam_distance(float cam_distance) {
        mCam_distance = cam_distance;
    }
    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "uMVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "aPosition");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }
}
