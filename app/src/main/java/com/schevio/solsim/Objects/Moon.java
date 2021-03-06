package com.schevio.solsim.Objects;

import android.opengl.GLES20;

import com.schevio.solsim.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class Moon {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "attribute vec4 vPosition;     \n"		// Per-vertex position information we will pass in.
                    + "attribute vec4 aColor;        \n"		// Per-vertex color information we will pass in.
                    + "attribute vec3 aNormal;       \n"		// Per-vertex normal information we will pass in.

                    + "varying vec4 vColor;          \n"		// This will be passed into the fragment shader.

                    + "void main() {        \n"
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.

                    // Transform the vertex into eye space.
//                    + "   vec3 modelViewVertex = vec3(uMVMatrix * aPosition);              \n"

                    // Transform the normal's orientation into eye space.
//                    + "   vec3 modelViewNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));     \n"

                    // Will be used for attenuation.
//                    + "   float distance = length(uLightPosition - modelViewVertex);             \n"

                    // Get a lighting direction vector from the light to the vertex.
//                    + "   vec3 lightVector = normalize(uLightPosition - modelViewVertex);        \n"

                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
//                    + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"

                    // Attenuate the light based on distance.
//                    + "   diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));  \n"

                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    + "   vColor = aColor;                                       \n"
//                    + "   vColor = aColor + 0.25*vPosition;                           \n"

                    // gl_Position is a special variable used to store the final position.
                    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                    + "   gl_Position = uMVPMatrix * vPosition;                            \n"
                    + "}                                                                   \n";

    private final String fragmentShaderCode =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "varying vec4 vColor;          \n"		// This is the color from the vertex shader interpolated across the
                    // triangle per fragment.
                    + "void main()                    \n"		// The entry point for our fragment shader.
                    + "{                              \n"
                    + "   gl_FragColor = vColor;     \n"		// Pass the color directly through the pipeline.
                    + "}                              \n";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer normalBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mNormalHandle;
    private int mLightPositionHandle;
    private int mMVPMatrixHandle;

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    int shape_faces = Icosphere.shape_faces;

    static float Vertices[] = Icosphere.Vertices;

    float Colors[] = new float[shape_faces * 4 * 3];
    int idx = 0;

//    static float Colors[] = {};

    static float Normals[] = Icosphere.Normals;

    private final int vertexCount = Vertices.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Moon() {
        Random random = new Random();

        for (int i = 0; i < shape_faces; i++) {
            int n = random.nextInt(40);
            boolean white = true;

            if (n < 15 | white & n < 20) {
                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 0.7f;
                Colors[idx++] = 1.0f;
            }

            else if (n < 35) {
                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 0.5f;
                Colors[idx++] = 1.0f;

                white = false;
            }
            else {
                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 0.3f;
                Colors[idx++] = 1.0f;

                white = false;
            }
        }

        // initialize vertex byte buffer for object
        ByteBuffer bb = ByteBuffer.allocateDirect(Vertices.length * 4);   // (# of coordinate values * 4 bytes per float)
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(Vertices);
        vertexBuffer.position(0);

        // initialize color byte buffer for object
        bb = ByteBuffer.allocateDirect(Colors.length * 4);
        bb.order(ByteOrder.nativeOrder());
        colorBuffer = bb.asFloatBuffer();
        colorBuffer.put(Colors);
        colorBuffer.position(0);

        // initialize normal byte buffer for object
        bb = ByteBuffer.allocateDirect(Normals.length * 4);
        bb.order(ByteOrder.nativeOrder());
        normalBuffer = bb.asFloatBuffer();
        normalBuffer.put(Normals);
        normalBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode
        );
        int fragmentShader = MyGLRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode
        );

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this com.example.android.shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this com.example.android.shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Pass in the position information
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                vertexStride,
                vertexBuffer
        );
        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // get handle to vertex shader's vPosition member
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor");
        // Pass in the position information
        colorBuffer.position(0);
        GLES20.glVertexAttribPointer(mColorHandle,
                4,
                GLES20.GL_FLOAT,
                false,
                0,
                colorBuffer
        );
        // Enable a handle to the vertices
        GLES20.glEnableVertexAttribArray(mColorHandle);


        // get handle to fragment shader's aColor member
//        mColorHandle = GLES20.glGetUniformLocation(mProgram, "aColor");
        // Set color for drawing the shape
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);


        // get handle to fragment shader's aNormal member
//        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        // Pass in the normal information
//        normalBuffer.position(0);
//        GLES20.glVertexAttribPointer(
//                mNormalHandle,
//                3,
//                GLES20.GL_FLOAT,
//                false,
//                0,
//                normalBuffer
//        );
        // Enable a handle to the normals
//        GLES20.glEnableVertexAttribArray(mNormalHandle);

        // get handle to com.example.android.shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the shape with array
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLES, 0, vertexCount
//                GLES20.GL_LINE_STRIP, 0, vertexCount
        );

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
