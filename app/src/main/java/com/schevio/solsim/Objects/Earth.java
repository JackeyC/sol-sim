package com.schevio.solsim.Objects;

import android.content.Context;
import android.opengl.GLES20;

import com.schevio.solsim.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

public class Earth {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 uMVMatrix;       \n"		// A constant representing the combined model/view matrix.
//                    + "uniform vec3 uLightPosition;       \n"	    // The position of the light in eye space.

                    + "attribute vec4 aPosition;     \n"		// Per-vertex position information we will pass in.
                    + "attribute vec4 aColor;        \n"		// Per-vertex color information we will pass in.
                    + "attribute vec3 aNormal;       \n"		// Per-vertex normal information we will pass in.

                    + "varying vec4 vColor;          \n"		// This will be passed into the fragment shader.
//                    + "varying vec4 vPosition;     \n"		// Per-vertex position information we will pass in.
                    + "void main() {        \n"
                    // The matrix must be included as a modifier of gl_Position.
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.

                    // Transform the vertex into eye space.
                    + "   vec3 modelViewVertex = vec3(uMVMatrix * aPosition);              \n"

                    // Transform the normal's orientation into eye space.
                    + "   vec3 modelViewNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));     \n"

                    // Will be used for attenuation.
//                    + "   float distance = length(vec3(0,0,0) - modelViewVertex);             \n"

                    // Get a lighting direction vector from the light to the vertex.
                    + "   vec3 lightVector = normalize(vec3(0,0,0) - modelViewVertex);        \n"

                    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
                    // pointing in the same direction then it will get max illumination.
                    + "   float diffuse = max(dot(modelViewNormal, lightVector), 0.1);       \n"

                    // Attenuate the light based on distance.
//                    + "   diffuse = diffuse * (1.0 / (1.0 + (0.025 * distance * distance)));  \n"

                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    + "   vColor = aColor * diffuse;                                                \n"
//                    + "   vColor = 0.75*aColor + 0.5*aPosition;       \n"
//                    + "   if(abs(aPosition.z)>0.4) vColor=1;                                  \n"
//                    + "   if(abs(aPosition.z)>0.1 && abs(aPosition.z)<0.4) vColor*=1.5;                                  \n"
//                    + "   if(abs(aPosition.z)>0.48) vColor.rgb=vec3(1,1,1);                                  \n"
//                    + "   vPosition = aPosition;                                  \n"
                    // gl_Position is a special variable used to store the final position.
                    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                    + "   gl_Position = uMVPMatrix * aPosition;                            \n"

                    // lighting per vertex
                    // http://www.lighthouse3d.com/tutorials/glsl-core-tutorial/directional-lights/
//                    +"vec3 l_dir = uLightPosition-(uMVMatrix * vec4(aPosition.xyz,1)).xyz; \n"
                    // transform normal to camera space and normalize it
//                    +"vec3 n = normalize(uMVMatrix * vec4(aNormal,1)).xyz; \n"
                    // compute the intensity as the dot product
                    // the max prevents negative intensity values
//                    +"float intensity = max(dot(n, l_dir), 0.25); \n"
//                    +"vec4 diffuse = vColor; \n"
                    // Compute the color per vertex
//                    +"vColor = intensity * diffuse; \n"
//                    + "   vColor = 0.75*aColor + 0.5*(uMVMatrix*aPosition-aPosition);                         \n"
                    + "}                                                                   \n";

    private final String fragmentShaderCode =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "varying vec4 vColor;          \n"		// This is the color from the vertex shader interpolated across the
                    // triangle per fragment.
//                    + "varying vec4 vPosition;     \n"		// Per-vertex position information we will pass in.
                    + "void main()                    \n"		// The entry point for our fragment shader.
                    + "{                              \n"
//                    + "   vec4 color = vColor;                          \n"
//                    + "   if(abs(vPosition.z)>0.4) color.b=0;                                  \n"
//                    + "   if(abs(vPosition.z)>0.1 && abs(vPosition.z)<0.4) color*=1.5;                                  \n"
//                    + "   if(abs(vPosition.z)<0.1) color.ra=vec2(1,1);                                  \n"
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
//    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
//
//    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
//    private final float[] mLightPosInWorldSpace = new float[4];
//
//    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
//    private final float[] mLightPosInEyeSpace = new float[4];

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
    public Earth() {
        Random random = new Random();
        boolean land = false;

        for (int i = 0; i < shape_faces; i++) {
            int n = random.nextInt(40);
            int j = i * 9 + 5;
            if ((Vertices[j] > 0.44f | Vertices[j] < -0.45f) & n < 35) {
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;
            }

            else if (n < 3 | (land & n < 30)) {
                Colors[idx++] = 0.13f;
                Colors[idx++] = 0.55f;
                Colors[idx++] = 0.13f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.13f;
                Colors[idx++] = 0.55f;
                Colors[idx++] = 0.13f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.13f;
                Colors[idx++] = 0.55f;
                Colors[idx++] = 0.13f;
                Colors[idx++] = 1.0f;

                land = true;
            }
            else {
                Colors[idx++] = 0.0f;
                Colors[idx++] = 0.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.0f;
                Colors[idx++] = 0.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;

                Colors[idx++] = 0.0f;
                Colors[idx++] = 0.0f;
                Colors[idx++] = 1.0f;
                Colors[idx++] = 1.0f;

                land = false;
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

        // get handle to vertex shader's aPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
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

        // get handle to vertex shader's aColor member
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


//        float LightPosition[] = {
//            8f, 0f, 0f
//        };

        // get handle to fragment shader's aColor member
//        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        // Set color for drawing the shape
//        GLES20.glUniform4fv(mLightPositionHandle, 1, LightPosition, 0);


        // get handle to fragment shader's aNormal member
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "aNormal");
        // Pass in the normal information
        normalBuffer.position(0);
        GLES20.glVertexAttribPointer(
                mNormalHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                0,
                normalBuffer
        );
        // Enable a handle to the normals
        GLES20.glEnableVertexAttribArray(mNormalHandle);


        //  get handle to fragment shader's aLightPosition member
//        mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition");
        // Pass in the light position in eye space.
//        GLES20.glUniform3f(
//                mLightPositionHandle, 0f,0f,0f
//                mLightPosInEyeSpace[0],
//                mLightPosInEyeSpace[1],
//                mLightPosInEyeSpace[2]
//        );


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
