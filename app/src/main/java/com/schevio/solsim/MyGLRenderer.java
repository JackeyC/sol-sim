package com.schevio.solsim;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.FloatMath;
import android.util.Log;

import com.schevio.solsim.Objects.Axis;
import com.schevio.solsim.Objects.Earth;
import com.schevio.solsim.Objects.Moon;
import com.schevio.solsim.Objects.RawResourceReader;
import com.schevio.solsim.Objects.Saturn;
import com.schevio.solsim.Objects.Saturn_Ring;
import com.schevio.solsim.Objects.SkyBox;
import com.schevio.solsim.Objects.StarsDome;
import com.schevio.solsim.Objects.Sun;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by chaij on 20/11/14.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    float angle;

    private static final String TAG = "MyGLRenderer";

//    private final Context mActivityContext;
//    private SkyBox mSkyBox;
    private StarsDome mStarsDome;
    private Axis mAxis;
    private Sun mSun;
    private Earth mEarth;
    private Moon mMoon;
    private Saturn mSaturn;
    private Saturn_Ring mSaturn_Ring;
//    private SpaceShip mSpaceShip;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
//    private float[] mLightModelMatrix = new float[16];

    private float mAngle_X;
    private float mAngle_Y;
    private float mCam_distance = 7f;
    private int mPlanet = 1;
    private boolean mAxisOn;

    float cam_x;
    float cam_y;
    float cam_z;
    float focus_x;
    float focus_y;
    float focus_z;

    float Sun_x = 0;
    float Sun_y = 0;

    float Earth_distance = 20;
    float Earth_x;
    float Earth_y;

    float Moon_distance = 5;
    float Moon_x;
    float Moon_y;

    float Saturn_distance = 30;
    float Saturn_x;
    float Saturn_y;

    int mSpeed = 500;
    int Earth_day_period;

    /**
     * Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     * we multiply this by our transformation matrices.
     */
//    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
//
//    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
//    private final float[] mLightPosInWorldSpace = new float[4];
//
//    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
//    private final float[] mLightPosInEyeSpace = new float[4];
//
//    /** This is a handle to our light point program. */
//    private int mPointProgramHandle;
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame colour
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);

//        mSkyBox = new SkyBox();
        mStarsDome = new StarsDome();
        mAxis = new Axis();
        mSun = new Sun();
        mEarth = new Earth();
        mMoon = new Moon();
        mSaturn = new Saturn();
        mSaturn_Ring = new Saturn_Ring();
//        mSpaceShip = new SpaceShip();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] starsDome = new float[16];
        float[] sun = new float[16];
        float[] earth = new float[16];
        float[] moon = new float[16];
        float[] saturn = new float[16];
        float[] saturn_ring = new float[16];

        // Clear color and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
//        GLES20.glClearDepthf(1.0f);
//        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
//        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
//        GLES20.glDepthMask(true);

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

        if (mCam_distance <= 3f) {
            mCam_distance = 3f;
        }
        if (mCam_distance >= 100f) {
            mCam_distance = 100f;
        }

        float xs[]={Sun_x, Earth_x, Moon_x, Saturn_x};
        float ys[]={Sun_y, Earth_y, Moon_y, Saturn_y};
        focus_x = xs[getPlanet()];
        focus_y = ys[getPlanet()];

        cam_x = focus_x - mCam_distance * FloatMath.sin(mAngle_X) * FloatMath.cos(mAngle_Y);
        cam_y = focus_y - mCam_distance * FloatMath.cos(mAngle_X) * FloatMath.cos(mAngle_Y);
        cam_z = focus_z + mCam_distance * FloatMath.sin(mAngle_Y);

        // Set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, cam_x, cam_y, cam_z, focus_x, focus_y, focus_z, 0f, 0f, 1f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw static
//        Matrix.scaleM(skyBox, 0, mMVPMatrix, 0, 200, 200, 200);
//        mSkyBox.draw(SkyBox);
//        mSpaceShip.draw(mMVPMatrix);

        Matrix.setIdentityM(starsDome, 0);
        Matrix.translateM(starsDome, 0, focus_x, focus_y, focus_z);
        Matrix.scaleM(starsDome, 0, 500, 500, 500);
        Matrix.multiplyMM(starsDome, 0, mMVPMatrix, 0, starsDome, 0);
        mStarsDome.draw(starsDome);

        if (mSpeed > 0) {
            Earth_day_period = 3600000 / mSpeed;
        } else {
            mSpeed = 1;
        }

        long time1 = SystemClock.uptimeMillis() % Earth_day_period;
        float Earth_day = 360f / Earth_day_period * ((int) time1);
        float Earth_day_radian = MyGLSurfaceView.TwoPi / Earth_day_period * ((int) time1);

        int Earth_year_period = Earth_day_period * 365;
        long time2 = SystemClock.uptimeMillis() % Earth_year_period;
        float Earth_year = 360f / Earth_year_period * ((int) time2);
        float Earth_year_radian = MyGLSurfaceView.TwoPi / Earth_year_period * ((int) time2);

        int Moon_period = Earth_day_period * 27;
        long time3 = SystemClock.uptimeMillis() % Moon_period;
        float Moon_orbit = 360f / Moon_period * ((int) time3);
        float Moon_orbit_radian = MyGLSurfaceView.TwoPi / Moon_period * ((int) time3);

        int Saturn_period = Earth_year_period * 8; //29
        long time4 = SystemClock.uptimeMillis() % Saturn_period;
        float Saturn_year = 360f / Saturn_period * ((int) time4);
        float Saturn_year_radian = MyGLSurfaceView.TwoPi / Saturn_period * ((int) time4);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.

        Earth_x = Earth_distance * FloatMath.cos(Earth_year_radian);
        Earth_y = Earth_distance * FloatMath.sin(Earth_year_radian);

        Moon_x = Earth_x + Moon_distance * FloatMath.cos(Moon_orbit_radian);
        Moon_y = Earth_y + Moon_distance * FloatMath.sin(Moon_orbit_radian);

        Saturn_x = Saturn_distance * FloatMath.cos(Saturn_year_radian);
        Saturn_y = Saturn_distance * FloatMath.sin(Saturn_year_radian);

        Matrix.setIdentityM(sun, 0);
        Matrix.rotateM(sun, 0, Moon_orbit, 0, 0, 1);
        Matrix.scaleM(sun, 0, 5, 5, 5);
        Matrix.multiplyMM(sun, 0, mMVPMatrix, 0, sun, 0);
        mSun.draw(sun);

       // public static void multiplyMV (float[] resultVec, int resultVecOffset, float[] lhsMat, int lhsMatOffset, float[] rhsVec, int rhsVecOffset)
        float l_pos [] = {0, 0, 0, 1};
        float [] sunModelViewMatrix = new float[16];
        Matrix.multiplyMM(sunModelViewMatrix, 0, mViewMatrix, 0, sun, 0);
        Matrix.multiplyMV(l_pos, 0, sunModelViewMatrix, 0, l_pos, 0);

        Matrix.setIdentityM(earth, 0);
        Matrix.translateM(earth, 0, Earth_x, Earth_y, 0);
        Matrix.rotateM(earth, 0, 23.45f, 0f, 1f, 0f);
        Matrix.rotateM(earth, 0, Earth_day, 0f, 0f, 1f);
        Matrix.scaleM(earth, 0, 3, 3, 3);
        Matrix.multiplyMM(earth, 0, mMVPMatrix, 0, earth, 0);
        mEarth.draw(earth);
//        float[] earthModelViewMatrix = new float[16];
//        Matrix.multiplyMM(earthModelViewMatrix, 0, mViewMatrix, 0, earth, 0);
//        mEarth.draw(earth, earthModelViewMatrix, l_pos);

        Matrix.setIdentityM(moon, 0);
        Matrix.translateM(moon, 0, Moon_x, Moon_y, 0);
        Matrix.rotateM(moon, 0, Moon_orbit, 0, 0, 1);
        Matrix.multiplyMM(moon, 0, mMVPMatrix, 0, moon, 0);
        mMoon.draw(moon);

        Matrix.setIdentityM(saturn, 0);
        Matrix.setIdentityM(saturn_ring, 0);
        Matrix.translateM(saturn, 0, Saturn_x, Saturn_y, 0);
        Matrix.rotateM(saturn, 0, 23.45f, 1f, 0f, 0f);
        Matrix.scaleM(saturn_ring, 0, saturn, 0, 4, 4, 4);
        Matrix.multiplyMM(saturn_ring, 0, mMVPMatrix, 0, saturn_ring, 0);
        mSaturn_Ring.draw(saturn_ring);
        Matrix.rotateM(saturn, 0, Earth_day, 0f, 0f, 1f);
        Matrix.scaleM(saturn, 0, 4, 4, 4);
        Matrix.multiplyMM(saturn, 0, mMVPMatrix, 0, saturn, 0);
        mSaturn.draw(saturn);

        if (mAxisOn) {
            mAxis.draw(sun);
            mAxis.draw(earth);
            mAxis.draw(moon);
            mAxis.draw(saturn);
        }
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
     * <p/>
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type       - Vertex or fragment shader type.
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
     * <p/>
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


//    protected String getVertexShader()
//    {
//        return RawResourceReader.readTextFileFromRawResource(R.raw.vertex_shader);
//    }
//
//    protected String getFragmentShader()
//    {
//        return RawResourceReader.readTextFileFromRawResource(R.raw.fragment_shader);
//    }

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

    public int getPlanet() {
        return mPlanet;
    }

    public boolean getAxis() {
        return mAxisOn;
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

    public int getNumOfPlanet()
    {
        return 4;
    }

    public  void nextPlanet()
    {
        mPlanet = (mPlanet + 1) % getNumOfPlanet();
    }

    public  void previousPlanet()
    {
        mPlanet = (mPlanet - 1 + getNumOfPlanet()) % getNumOfPlanet();
    }

    public void setAxis(boolean axis) {
        mAxisOn = axis;
    }
}