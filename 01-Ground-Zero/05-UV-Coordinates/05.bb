[b][center]TheComet's Shader Tutorials[/center]
[center]05 - UV Coordinates[/center][/b]

[b]Synopsis[/b]

You will learn:

[b]*[/b] What UV coordinates are.
[b]*[/b] How they are passed to the pixel shader.
[b]*[/b] How colours are encoded and why are they also 4-dimensional.



[b]What are UV coordinates?[/b]

In tutorial 01 we discussed [b]vertex attributes[/b]. Another attribute a vertex can have is what's known as a [b]UV coordinate[/b].

When texturing an object, we have to somehow remember how the texture was "wrapped" onto the object. This is done by saving where a vertex was located on a texture as an attribute of the vertex itself.

In DBP, you usually work with [b]pixel coordinates[/b]. If you load a 256x256 image, you'd have to use the coordinates 128,128 to draw to the very middle of the image.

GPUs don't do this because textures can have varying resolutions. Instead, the GPU defines the [b]top left corner[/b] to be at [b]0.0, 0.0[/b], and the [b]bottom right corner[/b] to be at [b]1.0, 1.0[/b]. If you wanted to draw in the very middle of the image, you'd have to do it at [b]0.5, 0.5[/b], which is exactly half of [b]1.0, 1.0[/b].

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/plane02_zps14c70627.png[/img]

In order to texture a 3D object, it needs to be "unwrapped" so it becomes 2-dimensional. The following is an example with a sphere:

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/UVMapping_zps74d1d059.png[/img]

This makes it easy to slap an image onto it.

So when rendering an object, every vertex knows where it was located on the texture, and stores this its [b]UV coordinate[/b] attribute.

You can access a vertex' UV coordinate with the semantic [b]TEXCOORD0[/b].

Notice the "0" in "TEXCOORD0". You can apply more than one texture to the same object, and every texture can be mapped differently to the object. The second texture could be accessed through TEXOORD1, and so on.



[b]Let's see some shader code![/b]

Texture coordinates are a little special. They are an attribute of [b]vertices[/b], but they aren't used by the vertex shader. The pixel shader is what needs them. However, they still need to be extracted by the vertex shader and passed on to the pixel shader, because only the vertex shader has access to vertices.

In order to do this, we modify the vertex shader input and output structs to include the new semantics:
[code]struct VS_INPUT
{
	float4 position : POSITION0;
	float2 texCoord : TEXCOORD0;
};[/code]

[code]struct VS_OUTPUT
{
	float4 position : POSITION;
	float2 texCoord : TEXCOORD0;
};[/code]

Additionally, the pixel shader input struct also needs to read the information from the vertex shader:
[code]struct PS_INPUT
{
	float2 texCoord : TEXCOORD0;
};[/code]

With these structs, the data flows as follows:
[b]vertex UV attribute[/b] -> [b]vertex shader[/b] -> [b]rasteriser[/b] -> [b]pixel shader[/b]

Now, modify the vertex shader to read the texture coordinates from the vertices and output them for the pixel shader. This is as simple as copying the values from input to output:
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output struct, so we can write output data
	VS_OUTPUT output;

	// take each position attribute of the incoming vertex and transform it into projection space
	output.position = mul( input.position, matWorldViewProjection );

	// output texture coordinates
	output.texCoord = input.texCoord;

	// return output data
	return output;
}[/code]

Awesome!

Now the pixel shader has to make use of the new input values it can receive. Right now, let's just set the colour of the object according to the UV coordinates:
[code]PS_OUTPUT ps_main( PS_INPUT input )
{
	// declare output struct, so we can write output data
	PS_OUTPUT output;

	// set pixel colour according to the UV coordinate
	output.colour = float4( input.texCoord.xy, 1.0f, 1.0f );

	// output final colour
	return output;
}[/code]

Run the code and you should get something like this:

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/rainbows_zps49845a95.png[/img]



[b]Colours on the graphics card[/b]

So why did that happen? Let's examine. What do we know?

1) UV coordinates will be between the value of 0.0 and 1.0.

Good. And now you can probably guess that the colours on the GPU are [i]also[/i] defined between [b]0.0 and 1.0[/b] instead of 0 and 255 *gasp*. What a surprise!

That's right. For the GPU, a value of [b]1, 1, 1[/b] is completely white, where [b]0.5, 0.5, 0.5[/b] is grey, and [b]1, 0, 0[/b] is red, etc.

One small detail is that colours are also 4-dimensional. The last value defines the alpha channel, where 0 is totally transparent and 1 is totally opaque.

And just like with coordinates, each component can also be accessed via dot notation:

[b]coordinate.xyzw
colour.rgba[/b]

Where r is red, g is green, b is blue, and a is alpha.

Since we mapped the UV coordinates directly to the colour, you can see that the GPU [b]interpolates[/b] the UV coordinates between each vertex during rasterisation, to find an intermediate texture coordinate for the current pixel. That's why the colours have such smooth gradients.



[b]Summary[/b]

[b]*[/b] The vertex shader needs to pass texture coordinates to the pixel shader.
[b]*[/b] During rasterisation the UV coordinates are linearly interpolated between vertices for each pixel.
[b]*[/b] UV coordinates are always between 0.0 and 1.0.
[b]*[/b] Colours on the GPU are handled as four floating point values, each between 0.0 and 1.0.



[b]Links[/b]

Proceed to the next tutorial: [href=]06 - Sampling a Texture[/href]
Proceed to the previous tutorial here: [href=]04 - Vertex Normals[/href]

TheComet
