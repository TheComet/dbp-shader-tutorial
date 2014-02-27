[b][center]TheComet's Shader Tutorial[/center]
[center]03 - Vertex Shader Coordinate System[/center][/b]

[b]Synopsis[/b]

In tutorial 02 you wrote your very first shader from scratch, and have a basic understanding of how it works. You will learn the following in this chapter.

[b]*[/b] Why do positions use 4-dimensional vectors and not 3-dimensional vectors?



[b]Coordinate system in the vertex shader[/b]

You will have noticed that the POSITION0 semantic was assigned to a variable of type [b]float4[/b]. Why? We're working with 3D coordinates, so why are positions 4-dimensional?

Shaders actually don't use Cartesian coordinates (x, y, z), because they are flawed for a number of reasons. For example, what happens when you project an object using a perspective projection matrix, but the object is exactly 90 degrees to the left of the camera? The resulting vertex positions would be placed into infinity. As we know, though, computers can't handle infinite numbers, and you'd get unpredictable behaviour (such as the object drawing over places it shouldn't).

To solve this problem, mathematicians came up with an ingenious, alternate coordinate system for handling infinite numbers with finite components. This coordinate system is what's known as the [b]homogeneous coordinate system[/b], which has one extra component [b]w[/b]:
[code] /x\
| y |
| z |
 \w/[/code]

Now don't get scared, it's really quite trivial to understand. In order to convert a homogeneous coordinate back to a Cartesian coordinate, all you need to do is divide its x, y, and z components by its w component:
[code]float3 cartesian;

// converts a homogeneous coordinate to a cartesian coordinate
cartesian.x = homogeneous.x / homogeneous.w;
cartesian.y = homogeneous.y / homogeneous.w;
cartesian.z = homogeneous.z / homogeneous.w;[/code]

In fact, to make things even easier, the POSITION0 attribute of the vertex will [b]always[/b] set the [b]w[/b] component to [b]1.0[/b]. And we all know that dividing anything by 1.0 won't change the value, so you can effectively ignore the [b]w[/b] component, and pretend that [b]homogeneous.xyz[/b] is cartesian. Pretty neat, huh?

But what's the point then? Isn't that the same as Cartesian?

Not exactly, because this makes it possible to define points in infinity. For example:

[code]float4 homogeneous = { 10.0f, 6.0f, 3.0f, 0.0f };{/code]

Oh oh, we've set [b]w[/b] to 0.0. If you look back a bit on how to convert to Cartesian, you'll notice that we're dividing x, y, and z by 0. Believe it or not, this is actually a valid coordinate. It defines a point located infinitely away, but we're not using infinite numbers. This is how the GPU handles correct projections without causing undefined behaviour. How cool is that?

One experiment you can do to prove this is to add the following to your vertex shader:
[code]VS_OUTPUT vs_main( VS_INPUT input )
{
	// declare output struct, so we can write output data
	VS_OUTPUT output;

	// set the w component to half the size
	input.position.w = 0.5f;

	// take each position attribute of the incoming vertex and transform it into projection space
	output.position = mul( input.position, matWorldViewProjection );

	// return output data
	return output;
}[/code]

So now, instead of dividing each component by 1.0, it will divide by 0.5. This will cause your object to scale to twice of its original size:

[img]illustration[/img]



[b]Summary[/b]

[b]*[/b] Vertex shaders use the homogeneous coordinate system, which allows the GPU to define and handle points located in infinity.



[b]Links[/b]

Proceed to the next tutorial here.

TheComet
