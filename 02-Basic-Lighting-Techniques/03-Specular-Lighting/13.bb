[b][center]TheComet's Shader Tutorial[/center]
[center]Basic Lighting Techniques - Specular Lighting[/center][/b]

[b]Synopsis[/b]

Here you will learn what specular lighting is, and how it can be calculated.



[b]Theory[/b]

Specular lighting gives us the feeling of "shininess" of an object.

In the real world, every object reflects light. A mirror reflects nearly everything, whereas a darker object absorbs most of the light.

There is a point on every object where your eye will be looking directly into the reflection of the light source. This effect is best observed when taking a picture with a camera and turning flash on. Here is a picture of my desk:

[img][/img]

Notice the circled white areas? That is the flash being reflected from various surfaces directly into the camera lens. This doesn't happen on the entire surface, only in very local areas where the camera lens just happens to be perfectly aligned with the reflected light source.

[img][/img]



[b]Math and pseudo-code[/b]

[img]http://upload.wikimedia.org/wikipedia/commons/thumb/3/35/Phong_Vectors.svg/250px-Phong_Vectors.svg.png[/img]

The computation of the specular reflection requires the surface normal vector N, the direction to the light source L, the reflected direction to he light source R, and the direction to the camera V.

The reflected light vector R can be calculated with the reflection formula:
[i]R = 2N(N.L) - L[/i]

HLSL provides us with a handy function for reflective vectors, unsurprisingly known as [b]reflect()[/b]:
[code]reflectedLightDirection = reflect( -lightDirection, surfaceNormal );[/code]

The closer the camera view direction is to the vector R (reflectedLightDirection), the more intense specular reflection should be. In shaders, this is usually achieved by raising the resulting angle between the camera vector and the reflected light vector to the power of a factor [i]shininess[/i]. The angle between two vectors, as we know, can be calculated with the dot product.
[code]// Calculate cosTheta of the reflected light vector and camera view direction.
// The closer this value is to 1.0, the more intense specular reflection should be.
specularIntensity = dot( viewDirection, reflectedLightDirection );

// Angles greater than 90° make no sense - clamp to 0.0, 1.0
specularIntensity = saturate( specularIntensity );

// To make specular reflection more local, raise it to the power of some pre-defined factor.
// The higher the value of shininess, the more focused the "white spots" will become.
// Recommended values for shininess: 4, 8, 16, 32.
specularIntensity = pow( specularIntensity, shininess );[/code]

Make sure you fully understand the math!



[b]Implementation[/b]

We're going to do one thing a little differently this time. We will be working in [b]view space[/b] instead of [b]world space[/b], because that way we can very easily get the camera position (it's located at 0, 0, 0 in view space).



[b]Shader Constants[/b]

We will need the [b]world view[/b] matrix, and the [b]world view projection[/b] matrix.
[code]// projection matrices
float4x4 matWorldView : WORLDVIEW;
float4x4 matWorldViewProjection : WORLDVIEWPROJECTION;[/code]

For our lighting, we require a [b]light position[/b], [b]light colour[/b], and as discussed above, a [b]shininess[/b] factor for specular lighting.
[code]// shader constants
float3 lightPosition_worldSpace = {100.0f, 50.0f, 50.0f};
float3 lightColour = {1.0f, 1.0f, 1.0f};
float shininess = 16.0f;  // good values to try: 4, 8, 16, 32, 64[/code]



[b]Vertex Shader[/b]

For our input struct, the usual stuff:
[code]struct VS_INPUT
{
    float4 position : POSITION0;
    float4 normal   : NORMAL0;
    float2 texCoord : TEXCOORD0;
};[/code]

The output struct has one additional output parameter, and everything is in view space:
[code]struct VS_OUTPUT
{
    float4 position                 : POSITION;
    float2 texCoord                 : TEXCOORD0;
    float3 surfaceNormal_viewSpace  : TEXCOORD1;
    float3 lightDirection_viewSpace : TEXCOORD2;
    float3 vertexPosition_viewSpace : TEXCOORD3;
};[/code]

The vertex shader needs to transform things into view space instead of world space:
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output data
	VS_OUTPUT output;
	
	// Pixel shader requires camera position. Since we're working in view space already, camera
	// is located at 0, 0, 0 - no need to do anything.
	
	// Pixel shader requires the surface normal to calculate lighting. Transform it into view space.
	// Note the use of dot notation to extract only the first 3 components (xyz). The w component still
	// remains 1.0 after transformation, so xyz can be considered cartesian coordinates.
	output.surfaceNormal_viewSpace = mul( input.normal, matWorldView ).xyz;
	
	// Pixel shader requires the light direction to calculate lighting. Transform each vertex into
	// view space, transform the light into view space, and subtract the light position from it to
	// get the light directional vector.
	// NOTE: Directional vectors should be normalised, but no there's no need to do that here because
	//       the pixel shader will do it anyway.
	output.lightDirection_viewSpace = mul( input.position, matWorldView ).xyz
	                                - mul( float4(lightPosition_worldSpace, 1.0f), matWorldView ).xyz;
	
	// output UV coordinates
	output.texCoord = input.texCoord;
	
	// transform vertex into projection space
	output.position = mul( input.position, matWorldViewProjection );
	
	// return output data
	return output;
}



[b]Pixel Shader[/b]

The input struct remains almost the same, except for working with view space instead of world space:
[code]struct PS_INPUT
{
    float2 texCoord                 : TEXCOORD0;
	float3 surfaceNormal_viewSpace  : TEXCOORD1;
	flaot3 lightDirection_viewSpace : TEXCOORD2;
};[/code]

The output struct remains the same, as always:
[code]struct PS_OUTPUT
{
    float4 colour : COLOR;
};[/code]

Here the updated pixel shader for specular lighting.
[code]PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output data
	PS_OUTPUT output;
	
	// The rasteriser interpolates linearly. This means vectors that should be normalised aren't normalised any more,
	// so we have to explicitly normalise them again.
	input.surfaceNormal_viewSpace = normalise( input.surfaceNormal_worldSpace );
	input.lightDirection_viewSpace = normalise( input.lightDirection_worldSpace );

	// calculate cosine of the angle of light hitting the surface
	// 1.0 will mean the light is hitting it head on
	// 0.0 will mean the light is hitting it from 90° or more (in other words, no light at all)
	// clamp it so value doesn't go below 0.0, as angles greater than 90° mean the light is hitting the surface from behind.
	float cosTheta = dot( input.surfaceNormal_worldSpace, input.lightDirection_worldSpace );
	cosTheta = clamp( cosTheta, 0.0f, 1.0f );

	// sample ambient colour from texture
	float3 ambient = tex2D( sampDiffuse, input.texCoord ).xyz;

	// calculate diffuse colour based on global light colour, ambient colour, and light angle
	// NOTE: multiplying two vectors in shaders will generate a vector with an equal amount of components,
	//       where the resulting vector is every component of one vector multiplied with the opposing
	//       component of the other.
	float3 diffuse = ambient * lightColour * cosTheta;

	// combine final colour
	output.colour = float4( ambient*0.1 + diffuse*0.9, 1.0f );

	// output data
	return output;
}[/code]

[b]Summary[/b]



[b]Links[/b]

Proceed to the next tutorial here.

TheComet
