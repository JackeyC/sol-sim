package com.schevio.solsim;

/**
 * Created by chaij on 01/12/14.
 */
public class MyShader {
    public String getVertexShader() {
        final String vertexShader =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
                        + "uniform mat4 uMVMatrix;       \n"		// A constant representing the combined model/view matrix.
                        + "uniform vec3 uLightPosition;       \n"	    // The position of the light in eye space.

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

                        // gl_Position is a special variable used to store the final position.
                        // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
                        + "   gl_Position = uMVPMatrix * vPosition;                            \n"
                        + "}                                                                   \n";

        return vertexShader;
    }

    public String getFragmentShader() {
        final String fragmentShader =
                "precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec4 vColor;          \n"		// This is the color from the vertex shader interpolated across the
                        // triangle per fragment.
                        + "void main()                    \n"		// The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = vColor;     \n"		// Pass the color directly through the pipeline.
                        + "}                              \n";

        return fragmentShader;
    }
}
