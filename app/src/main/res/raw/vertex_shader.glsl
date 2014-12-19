// This matrix member variable provides a hook to manipulate
// the coordinates of the objects that use this vertex shader

uniform mat4 uMVPMatrix;        // A constant representing the combined model/view/projection matrix.
uniform mat4 uMVMatrix;         // A constant representing the combined model/view matrix.
//uniform vec3 uLightPosition;  // The position of the light in eye space.

attribute vec4 aPosition;       // Per-vertex position information we will pass in.
attribute vec4 aColor;          // Per-vertex color information we will pass in.
attribute vec3 aNormal;         // Per-vertex normal information we will pass in.

varying vec4 vColor;            // This will be passed into the fragment shader.
//varying vec4 vPosition;       // Per-vertex position information we will pass in.

void main() {
    // The matrix must be included as a modifier of gl_Position.
    // Note that the uMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.

    // Transform the vertex into eye space.
    vec3 modelViewVertex = vec3(uMVMatrix * aPosition);

    // Transform the normal's orientation into eye space.
//    vec3 modelViewNormal = vec3(uMVMatrix * vec4(aNormal, 0.0));

    // Will be used for attenuation.
//    float distance = length(uLightPosition - modelViewVertex);

    // Get a lighting direction vector from the light to the vertex.
//    vec3 lightVector = normalize(uLightPosition - modelViewVertex);

    // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
    // pointing in the same direction then it will get max illumination.
//    float diffuse = max(dot(modelViewNormal, lightVector), 0.1);

    // Attenuate the light based on distance.
//    diffuse = diffuse * (1.0 / (1.0 + (0.025 * distance * distance)));

    // Multiply the color by the illumination level. It will be interpolated across the triangle.
    vColor = aColor;
//    vColor = 0.75*aColor + 0.5*aPosition;
//    if(abs(aPosition.z)>0.4) vColor=1;
//    if(abs(aPosition.z)>0.1 && abs(aPosition.z)<0.4) vColor*=1.5;
    if (abs(aPosition.z) > 0.48) {
        vColor.rgb=vec3(1,1,1);
    }
//    vPosition = aPosition;
    // gl_Position is a special variable used to store the final position.
    // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
    gl_Position = uMVPMatrix * aPosition;

    // lighting per vertex
    // http://www.lighthouse3d.com/tutorials/glsl-core-tutorial/directional-lights/
//    vec3 l_dir = uLightPosition-(uMVMatrix * vec4(aPosition.xyz,1)).xyz;
    // transform normal to camera space and normalize it
//    vec3 n = normalize(uMVMatrix * vec4(aNormal,1)).xyz;
    // compute the intensity as the dot product
    // the max prevents negative intensity values
//    float intensity = max(dot(n, l_dir), 0.25);
//    vec4 diffuse = vColor;
    // Compute the color per vertex
//    vColor = intensity * diffuse;
//    vColor = 0.75*aColor + 0.5*(uMVMatrix*aPosition-aPosition);
}