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
//    private PolyStar3D mPolyStar3D;
    private Earth mEarth;
    private Axis mAxis;
    private SpaceShip mSpaceShip;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTiltMatrix = new float[16];
    private final float[] mEarthRotationMatrix = new float[16];

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];

    private float mAngle_X;
    private float mAngle_Y;

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

        mEarth = new Earth();
        mAxis = new Axis();
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
//        if (mAngle_Y < -MyGLSurfaceView.TwoPi) {
//            mAngle_Y = -MyGLSurfaceView.TwoPi;
//        }
//        else if (mAngle_Y > MyGLSurfaceView.TwoPi) {
//            mAngle_Y = MyGLSurfaceView.TwoPi;
//        }

//        int max = 360;
//        int i=0;
//        i = (i+1)%max;
//        i = (i-1+max)%max;

        float cam_x = cam_distance * FloatMath.sin(mAngle_X) * FloatMath.cos(mAngle_Y);
        float cam_y = cam_distance * FloatMath.cos(mAngle_X) * FloatMath.cos(mAngle_Y);
        float cam_z = cam_distance * FloatMath.sin(mAngle_Y);

        //Debug
//        System.out.println("cam x = " + cam_x);
//        System.out.println("cam y = " + cam_y);
//        System.out.println("cam z = " + cam_z);

        // Set the camera position
        Matrix.setLookAtM(mViewMatrix, 0, cam_x, cam_y, cam_z, 0f, 0f, 0f, 0f, 0f, 1f);

        // Define a simple shader program for our point.
        final String pointVertexShader =
                "uniform mat4 u_MVPMatrix;      \n"
                        +	"attribute vec4 aPosition;     \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_Position = uMVPMatrix * aPosition;   \n"
                        + "   gl_PointSize = 5.0;         \n"
                        + "}                              \n";

        final String pointFragmentShader =
                "precision mediump float;       \n"
                        + "void main()                    \n"
                        + "{                              \n"
                        + "   gl_FragColor = vec4(1.0,    \n"
                        + "   1.0, 1.0, 1.0);             \n"
                        + "}                              \n";

        final int pointVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, pointVertexShader);
        final int pointFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, pointFragmentShader);
        mPointProgramHandle = createAndLinkProgram(pointVertexShaderHandle, pointFragmentShaderHandle,
                new String[] {"a_Position"});

//        final String vertexShader = getVertexShader();

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Draw static
//        mSpaceShip.draw(mMVPMatrix);

        // Create a rotation for the shape
        int period = 24000;
        long time = SystemClock.uptimeMillis() % period;
        float angle = 360f / period * ((int) time);

        //Debug
//        System.out.println("self rotate angle = "+ angle);

        Matrix.setRotateM(mTiltMatrix, 0, -23.45f, 0f, 1f, 0f);
//        Matrix.setRotateM(mTiltMatrix, 0, 0f, 0f, 1f, 0f);
        Matrix.setRotateM(mRotationMatrix, 0, angle, 0f, 0f, 1f);

//        angle += 0.1;

        Matrix.multiplyMM(mEarthRotationMatrix, 0, mTiltMatrix, 0, mRotationMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mEarthRotationMatrix, 0);

        // Draw rotate
        mAxis.draw(scratch);
        mEarth.draw(scratch);
//        mSpaceShip.draw(scratch);
//        mPolyStar3D.draw(scratch);
//        Matrix.setRotateM(mRotationMatrix, 0, (mAngle + 180) % 360, 0f, 1f, 0f);
//        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

//        mPolyStar3D.draw(scratch);
        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();
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

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

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
