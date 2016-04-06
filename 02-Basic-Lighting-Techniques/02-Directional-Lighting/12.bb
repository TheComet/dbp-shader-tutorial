[b][center]TheComet's Shader Tutorial[/center]
[center]Basic Lighting Techniques - Directional Lighting[/center][/b]

[b]Synopsis[/b]

Here you will learn about about directional lighting, and how it's different to global lighting. Directional lighting serves as a basis for all proceeding basic lighting tutorials.



[b]The Difference[/b]

Unlike [b]global lighting[/b], [b]directional lighting[/b] has a [b]position[/b].

In the previous tutorial, we could only specify the light direction, and that direction was the same on every surface no matter where you positioned your object.

This is no longer the case with directional lighting, because now that the light has a position, the light rays aren't parallel to each other any more and have to be calculated differently for every pixel.

The result is a much smoother lighting gradient on flat surfaces.

The following is an image demonstrating this:

[img][/img]



[b]Theory[/b]

We will be using the same theory as in the previous tutorial, only with a slight adjustment.

The pixel shader needs a light [b]direction[/b], but we only have a light [b]position[/b]. Therefore, we have to transform each vertex into world space and subtract the light position from it to get the light direction in world space.

So something like:

[code]float3 vertexPosition_worldSpace = mul( input.position, matWorld ).xyz;
float3 lightDirection_worldSpace = vertexPosition_worldSpace - lightPosition_WorldSpace;
//NOTE: lightDirection_worldSpace should be normalised because it is a directional vector,
//      but there's no need to do that here because the pixel shader is going to normalise it anyway.[/code]



[b]Shader constants[/b]

We still require the two matrices from the previous tutorial:
[code]// transformation matrices
float4x4 matWorldViewProjection : WORLDVIEWPROJECTION;
float4x4 matWorld : WORLD;[/code]

Shader constants will be the light position in world space and light colour:
[code]float3 lightPosition_worldSpace = {100.0f, 50.0f, 50.0f};
float3 lightColour = {1.0f, 1.0f, 1.0f};[/code]



[b]Vertex Shader Structs[/b]

We will need the vertex position, vertex normals for light directional calculations, and UV coordinates so we can sample from the texture.
[code]struct VS_INPUT
{
    float4 position : POSITION0;
	float4 normal   : NORMAL0;
	float2 texCoord : TEXCOORD0;
};[/code]

We will output the usual stuff (transformed position and UV coordinates), and additionally we will have to calculate the transformed vertex normal and light direction for the pixel shader.

[code]struct VS_OUTPUT
{
    float4 position                  : POSITION0;
	float2 texCoord                  : TEXCOORD0;
	float3 surfaceNormal_worldSpace  : TEXCOORD1;
	float3 lightDirection_worldSpace : TEXCOORD2;
};[/code]



[b]Pixel Shader Structs[/b]

The pixel shader needs to read in the transformed vectors and texture coordinates:

[code]struct PS_INPUT
{
    float2 texCoord                  : TEXCOORD0;
	float3 surfaceNormal_worldSpace  : TEXCOORD1;
	float3 lightDirection_worldSpace : TEXCOORD2;
};[/code]

And the output still remains the same:

[code]struct PS_OUTPUT
{
    colour : COLOR;
};[/code]



[b]Vertex Shader[/b]

The comments explain it all.
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
    // declare output data
	VS_OUTPUT output;
	
	// Pixel shader requires the surface normal to calculate lighting. Transform it into world space.
	// Note the use of dot notation to extract only the first 3 components (xyz). The w component still
	// remains 1.0 after transformation, so xyz can be considered cartesian coordinates.
	output.surfaceNormal_worldSpace = mul( input.normal, matWorld ).xyz;
	
	// Pixel shader requires the light direction to calculate lighting. Transform each vertex into
	// world space and subtract the light position from it to get the light directional vector.
	// NOTE: Directional vectors should be normalised, but no there's no need to do that here because
	//       the pixel shader will do it anyway.
	output.lightDirection_worldSpace = mul( input.position, matWorld ).xyz - lightPosition_worldSpace;
	
	// output UV coordinates
	output.texCoord = input.texCoord;
	
	// transform vertex into projection space
	output.position = mul( input.position, matWorldViewProjection );
	
	// return outupt data
	return output;
}[/code]



[b]Pixel Shader[/b]

Not much changes here, other than replacing the global light direction from the previous tutorial with the now passed in light direction calculated in the vertex shader:
[code]PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output data
	PS_OUTPUT output;
	
	// The rasteriser interpolates linearly. This means vectors that should be normalised aren't normalised any more,
	// so we have to explicitly normalise them again.
	input.surfaceNormal_worldSpace = normalise( input.surfaceNormal_worldSpace );
	input.lightDirection_worldSpace = normalise( input.lightDirection_worldSpace );

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



[b]The Results[/b]

Here is the result compared with the ambient lighting from the previous tutorial:

[img][/img]

We observe a smoother lighting gradient on flat surfaces, since each pixel on the surface is being hit by a light ray at a slightly different angle.



[b]Summary[/b]



[b]Links[/b]

Proceed to the next tutorial here.

TheComet
