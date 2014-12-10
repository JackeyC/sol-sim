package com.schevio.solsim;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SkyBox {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                    + "uniform mat4 uMVMatrix;          \n"		// A constant representing the combined model/view matrix.

                    + "attribute vec4 vPosition;        \n"		// Per-vertex position information we will pass in.
                    + "attribute vec4 aColor;           \n"		// Per-vertex color information we will pass in.
//                    + "attribute vec2 aTexture;         \n"     // Per-vertex texture coordinate information we will pass in

                    + "varying vec4 vColor;             \n"		// This will be passed into the fragment shader.
//                    + "varying vec2 vTexture;           \n"     // This will be passed into the fragment shader.

                    + "void main() {        \n"

                    // Multiply the color by the illumination level. It will be interpolated across the triangle.
                    + "   vColor = aColor;              \n"     // Pass through the color.
//                    + "   vTexture = aTexture           \n"     // Pass through the texture coordinate.

                    // gl_Position is a special variable used to store the final position.
                    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                    + "   gl_Position = uMVPMatrix * vPosition;                            \n"
                    + "}                                                                   \n";

    private final String fragmentShaderCode =
            "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "varying vec4 vColor;             \n"		// This is the color from the vertex shader interpolated across the triangle per fragment.

                    + "uniform sampler2D uTextureImage; \n"     // The input texture.
//                    + "varying vec2 vTexture;           \n"     // Interpolated texture coordinate per fragment.

                    + "void main()                      \n"		// The entry point for our fragment shader.
                    + "{                                \n"
                    + "   gl_FragColor = vColor; "//* texture2D(uTextureImage, vTexture);        \n"		// Pass the color and texture directly through the pipeline.
                    + "}                                \n";

    private final FloatBuffer vertexBuffer;
    private final FloatBuffer colorBuffer;
    private final FloatBuffer textureBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureHandle;
    private int mTextureImageHandle;
    private int mTextureDataHandle;
    private int mMVPMatrixHandle;
    static Context mActivityContext;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;

    static float Vertices[] = {
            // Front face
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,

            // Right face
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,

            // Back face
            -1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, 1.0f, 1.0f,

            // Left face
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,

            // Top face
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,

            // Bottom face
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
    };




    static float Colors[] = {
            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            // Back face (blue)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (magenta)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f
    };




    static float TextureCoordinates[] = {
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Right face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Back face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Left face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Top face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Bottom face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };

    private final int vertexCount = Vertices.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public SkyBox() {
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

        // initialize texture byte buffer for object
        bb = ByteBuffer.allocateDirect(TextureCoordinates.length * 4);
        bb.order(ByteOrder.nativeOrder());
        textureBuffer = bb.asFloatBuffer();
        textureBuffer.put(TextureCoordinates);
        textureBuffer.position(0);

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


        // get handle to fragment shader's aNormal member
//        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTexture");
//        // Pass in the normal information
//        textureBuffer.position(0);
//        GLES20.glVertexAttribPointer(
//                mTextureHandle,
//                2,
//                GLES20.GL_FLOAT,
//                false,
//                0,
//                textureBuffer
//        );
//        // Enable a handle to the normals
//        GLES20.glEnableVertexAttribArray(mTextureHandle);
//
//        mTextureImageHandle = GLES20.glGetUniformLocation(mProgram, "uTextureImage");
//
//        //Load the texture
//        mTextureDataHandle = TextureHelper.loadTexture(mActivityContext, R.drawable.ic_launcher);
//
//        // Set the active texture unit to texture unit 0.
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
//
//        // Bind the texture to this unit.
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
//
//        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
//        GLES20.glUniform1i(mTextureImageHandle, 0);

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
