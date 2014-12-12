/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.schevio.solsim.Objects;

import android.opengl.GLES20;

import com.schevio.solsim.MyGLRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * A two-dimensional polygon for use as a drawn object in OpenGL ES 2.0.
 */
public class Saturn_Ring {

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer vertexBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 2;

    int Star_vertex = 30;
    float radius = 1.0f;
    float inner_radius = 0.7f;

    float color[] = { 0.9f, 0.8f, 0.2f, 0.0f };

    private final int vertexCount = Star_vertex * 2 + 2;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float vertexCoords[] = new float[vertexCount*2 + 4];
    int idx = 0;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Saturn_Ring() {
//        vertexCoords[idx++] = center_x;
//        vertexCoords[idx++] = center_y;

        int outerVertexCount = vertexCount - 2;
        boolean outer_vertex = true;

        for (int i = 0; i < outerVertexCount; i++) {
            float percent = (i / (float) (outerVertexCount - 1));
            float rad = (float) (percent * 2 * Math.PI);
            float outer_x;
            float outer_y;

            if (outer_vertex) {
                outer_x = (float) (radius * Math.sin(rad));
                outer_y = (float) (radius * Math.cos(rad));
                outer_vertex = false;
            }
            else {
                outer_x = (float) (inner_radius * Math.sin(rad));
                outer_y = (float) (inner_radius * Math.cos(rad));
                outer_vertex = true;
            }

            vertexCoords[idx++] = outer_x;
            vertexCoords[idx++] = outer_y;
        }
        vertexCoords[idx++] = vertexCoords[0];
        vertexCoords[idx++] = vertexCoords[1];
//        vertexCoords[idx++] = vertexCoords[2];
//        vertexCoords[idx++] = vertexCoords[3];

        // initialize vertex byte buffer for com.example.android.shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                vertexCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(vertexCoords);
        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

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

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to com.example.android.shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // Draw the Polygon
        GLES20.glDrawArrays(
                GLES20.GL_TRIANGLE_STRIP, 0, vertexCount
//                GLES20.GL_LINE_LOOP, 2, vertexCount
//                GLES20.GL_LINE_STRIP, 0, vertexCount
        );

        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
