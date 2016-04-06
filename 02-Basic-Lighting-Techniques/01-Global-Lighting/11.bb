[b][center]TheComet's Shader Tutorial[/center]
[center]Basic Lighting Techniques - Global Lighting[/center][/b]

[b]Synopsis[/b]

Here you will learn about global lighting and how to combine it with ambient lighting.



[b]The Problem[/b]

If you're coming from tutorial 06, you'll notice that your objects are looking awfully "flat". This is because we're really only mapping the texture directly onto the object without doing any light calculations whatsoever. The result is that the object has the same brightness everywhere.

By default, when working with built in objects, DBP provides you with a single [b]global light[/b]. This light has a [b]direction[/b], but no position. It's range is [b]infinite[/b], and it emits 360° around itself. This is what we're going to replicate in this tutorial.



[b]Importance of the Surface Normal[/b]

What actually determines how bright a surface is? Let's say you're shining a torch onto a piece of paper in a dark room. If the torch light hits the surface head on, it'll be pretty bright, right?

[img]Light head on[/img]

If you start to rotate the torch so the light hits the surface on an angle, the more you rotate away, the less bright the paper becomes.

[img]light from angle[/img]

So we have to somehow know what angle our light source is hitting the surface at. That's what the [b]surface normal[/b] is for. If we calculate the angle between the light direction and the surface normal direction, that angle is going to be directly proportional to how bright the surface will be.

[img]demonstration[/img]

So we can kind of already write some pseudo code for that:
[code]// The dot product of two vectors returns the cosine of the angle between them.
// cosTheta is the cosine of the angle of the light hitting the surface
// If both vectors are unit vectors, cosTheta will be in the range of -1.0 and 1.0
// --> If the vectors are pointing in the same direction, cosTheta = 1
// --> If they are 90° to each other, cosTheta = 0
float cosTheta = dot( lightDirection_worldSpace, normal_worldSpace );[/code]

An angle greater than 90° means the light is hitting the surface from behind. That doesn't really make any sense, and would mean that cosTheta would be negative, so it's best to clamp that:
[code]cosTheta = clamp( cosTheta, 0.0f, 1.0f );[/code]

What about colour? We know the surface has a particular colour, and we know the light source also has a particular colour. If, for instance, a white light hits a red surface, the surface will fully absorb the green and blue components, but reflect the red component. This is actually very easy to calculate with a simple multiplication:
[code]float4 finalColour = surfaceColour * lightColour;[/code]

The reason being:

[code]surfaceColour.r = 1.0f;    lightColour.r = 1.0f;
surfaceColour.g = 0.0f;    lightColour.g = 1.0f;
surfaceColour.b = 0.0f;    lightColour.b = 1.0f;

finalColour.r = 1.0f*1.0f;  // = 1.0
finalColour.g = 0.0f*1.0f;  // = 0.0
finalColour.b = 0.0f*0.0f;  // = 0.0[/code]

That's already all of the math that needs to be done.



[b]Bringing it all Together[/b]

The most important thing to know when working with shaders: It's [b]crucial[/b] to do your calculations in a [b]common 3D space[/b]. You've heard of [i]object space[/i], [i]world space[/i], [i]view space[/i], and [i]projection space[/i]. These are all different 3D spaces, and aren't related to one another in any way. That is, there's no linear relationship between them.

As an example, it's useless to have the light source placed in world space, but the vertices you wish to use the light on placed in projection space. They aren't in any way related to each other like that, and it's hard to do calculations with them in that state.

Since DBP works in world space, it makes the most sense to also do everything in world space. For this we require the [b]world matrix[/b], which can be accessed by using the [b]WORLD[/b] semantic:
[code]// at the top of your shader
float4x4 matWorld : WORLD;[/code]

We'll need a [b]light direction[/b]. The best way to do that would be to use a shader constant, so DBP can change it later on. I've given it a default direction so you don't [i]have[/i] to set it in DBP:
[code]float3 lightDirection_worldSpace = {0.8165f, 0.4082f, 0.4082f};[/code]

Note that the direction should be normalised.

We'll also need a [b]light colour[/b]. Again, I'd add that as a shader constant so DBP can change it, and set its default value to white:
[code]float3 ligthColour = {1.0f, 1.0f, 1.0f};[/code]

That is all!

[b]NOTE[/b]: Get used to writing which 3D space your vector is in. Things can get incredibly confusing later down the line, not only for you but also for anyone who reads your code.

Next, we consider our input and output structs (get used to this as well). Our vertex shader needs a [b]vertex position[/b], [b]vertex normal[/b] (for light direction calculation), and [b]uv coordinates[/b] so we can sample the texture.
[code]struct VS_INPUT
{
	float4 position : POSITION0;
	float4 normal   : NORMAL0;
	float2 texCoord : TEXCOORD0;
};[/code]

The things we want to output are, again, the new [b]position[/b] and the [b]uv coordinates[/b]. This time though there's something new:
[code]struct VS_OUTPUT
{
	float4 position                  : POSITION0;
	float2 texCoord                  : TEXCOORD0;
	float surfaceNormal_worldSpace   : TEXCOORD1;
};[/code]

This is the most confusing thing to understand in my opinion, but once you understand it, it makes complete sense.

Why are we outputting the surface normal using TEXCOORD semantics? Remember back in tutorial 06, where we learned about samplers? When the vertex shader finishes its job, the [b]rasteriser[/b] will connect the vertices together and fill the resulting surfaces with pixels, forming the final polygon shapes. You can think of this as a form of "sampling", much like texture samplers work, only in 3D. It generates a list of pixels to fit exactly onto this surface, which are later passed on to the pixel shader.

The rasteriser does much more than fill the surfaces with pixels. [b]It also interpolates all of the attributes of the output vertices in order to find the new attributes for every pixel of the surface![/b] This means that everything we output to the vertex output struct will be interpolated for the pixel shader in its input struct.

Because we need to calculate the light of the surface on a per-pixel basis, and not on a per-vertex basis, we have to do the transformations in the vertex shader, and pass them on to the pixel shader for the actual lighting calculation. The TEXCOORD semantics act like slots for the values to be interpolated for the pixel shader.

Vector transformations are [b]always[/b] calculated in the [b]vertex shader[/b]. In fact, I will smack you if I ever catch you doing it in the pixel shader. This has three reasons.
1) It's faster, because there are generally far less vertices than there are pixels.
2) By doing it in the pixel shader, you're losing the advantage of the rasteriser interpolating your values.
3) Most of the time, you don't have a choice. The vectors need to know about vertex attributes, and you don't have access to those in the pixel shader.

Anyway, enough of this abstract ramble on rasterisers! Let's take a look at the vertex shader:
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output data
	VS_OUTPUT output;

	// Pixel shader requires the surface normal to calculate lighting. Transform it into world space.
	// Note the use of dot notation to extract only the first 3 components (xyz). The w component still
	// remains 1.0 after transformation, so xyz can be considered cartesian coordinates.
	output.surfaceNormal_worldSpace = mul( input.normal, matWorld ).xyz;

	// output UV coordinates
	output.texCoord = input.texCoord;

	// transform vertex into projection space
	output.position = mul( input.position, matWorldViewProjection );

	// output data
	return output;
}[/code]

As the comments already describe, we're preparing all of the vectors for the pixel shader by making sure everything is in world space and by doing the usual UV coordinate and vertex projections.

The pixel shader is where the lighting is calculated.

First, let's examine the input and output structs of the pixel shader:
[code]struct PS_INPUT
{
	float2 texCoord                  : TEXCOORD0;
	float3 surfaceNormal_worldSpace  : TEXCOORD1;
};[/code]

Here, we simply catch the interpolated values of the vectors calculated in the vertex shader.

The output struct remains the same:
[code]struct PS_OUTPUT
{
	float4 colour : COLOR;
};[/code]

And finally, where the magic happens:
[code]PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output data
	PS_OUTPUT output;
	
	// The rasteriser interpolates linearly. This means vectors that should be normalised aren't normalised any more,
	// so we have to explicitly normalise them again.
	input.surfaceNormal_worldSpace = normalize( input.surfaceNormal_worldSpace );

	// calculate cosine of the angle of light hitting the surface
	// 1.0 will mean the light is hitting it head on
	// 0.0 will mean the light is hitting it from 90° or more (in other words, no light at all)
	// clamp it so value doesn't go below 0.0, as angles greater than 90° mean the light is hitting the surface from behind.
	float cosTheta = dot( input.surfaceNormal_worldSpace, lightDirection_worldSpace );
	cosTheta = clamp( cosTheta, 0.0f, 1.0f );

	// sample ambient colour from texture
	float3 ambient = tex2D( sampDiffuse, input.texCoord ).xyz;

	// calculate diffuse colour based on global light colour, ambient colour, and light angle
	// NOTE: multiplying two vectors in shaders will generate a vector with an equal amount of components,
	//       where the resulting vector is every component of one vector multiplied with the opposing
	//       component of the other.
	float3 diffuse = ambient * lightColour * cosTheta;

	// combine final colour
	output.colour = float4(diffuse, 1.0f);

	// output data
	return output;
}[/code]

There are two special things to note in the code above. Firstly, how vector multiplication is handled. See the following example:
[code]float4 f = a*b; // where a and b are both float4 types

// this is the same as:
f.x = a.x*b.x;
f.y = a.y*b.y;
f.z = a.z*b.z;
f.w = a.w*b.w;[/code]

Secondly, if you ever have a normalised vector in your vertex shader, and you're passing it to the pixel shader, [b]that vector will no longer be normalised in the pixel shader[/b] because the rasteriser interpolated them! Therefore, any vectors that should be normalised need to be re-normalised before using them.

[img][/img]



Run the shader, and you should get something like the following:

[img]this[/img]



[b]Combining with ambient lighting[/b]

Notice how surfaces pointing away from the light source are totally black. In the real world, light bounces around and still ends up illuminating surfaces not directly in line-of-sight with the light source. This is known as "light scattering" and is a very expensive process to calculate, so people came up with a way to cheat the real world.

Add this to your pixel shader:
[code]// combine final colour
output.colour = float4( ambient*0.1 + diffuse*0.9, 1.0f );[/code]

This takes the "flat" shading you had in the previous tutorial combines 10% of it with 90% of the diffuse lighting. This makes surfaces that would be completely black gain some ambient lighting, as seen here:

[img]this[/img]

It's a simple trick, but has a huge effect on the end result.



[b]Summary[/b]

[b]*[/b] The surface normal is crucial in determining how much light it receives.
[b]*[/b] Output data from the vertex shader and input data for the pixel shader is [b]interpolated[/b] during rasterisation.
[b]*[/b] It is possible to emulate a light scattering effect by cheating and adding some of the ambient component to the final colour.



[b]Links[/b]

Proceed to the next tutorial here.

TheComet
