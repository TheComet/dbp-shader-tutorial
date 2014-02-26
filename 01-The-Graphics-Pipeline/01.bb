[b][center]TheComet's Shader Tutorial[/center]
[center]01 - Understanding The Graphics Pipeline[/center][/b]

[b]Synopsis[/b]

You will learn the following in this chapter.

[b]*[/b] The basics of what a graphics card is, what it does, and how it does it.
[b]*[/b] What parts of it we can programmatically manipulate through shader programs.
[b]*[/b] When it makes sense to use the GPU and when it makes sense to use the CPU.

You may feel the need to skip this chapter, but I advise you not to. It contains some fundamental information that is later built upon.



[b]What is a grahics card?[/b]

A Graphics Processing Unit (GPU) is a specialised electronic circuit designed to rapidly manipulate and alter memory to accelerate the creation of images in a frame buffer intended for output to a display.

Unlike a CPU, the arcitecture of a GPU is highly parallelised. A single GPU contains [i]thousands[/i] of cores, with the ability to reach a total processing power of multiple Tflop/s (10¹² floating point operations per second).

You may ask yourself: Why hasn't the CPU been replaced by a GPU yet? The GPU is obviously over a million times faster. The answer is quite simple: Some mathematical problems cannot be solved in parallel efficiently, while some can. The CPU is designed to solve sequential problems, while the GPU is designed to solve parallel problems.

For example, think about the code you wrote in your latest project. Each command you typed in needs to be processed sequentially. It wouldn't make sense for your enemy to search for a path and at the same time try to follow the path, because the path data hasn't finished calculating yet. The path needs to first exist before the enemy can follow it: It's a sequential problem.

It's hard to parallelise a system that needs to know about other parts of itself, like a pathfinder.

But lets say your game has thousands of bullets that need to be simulated at the same time, because you're writing the next MMOFPS. It's possible to update the position of each bullet in parallel, because they don't need to know about each other. Of course, you'd still have to make sure you check for collision [i]after[/i] all of the bullets have been updated, because again, it doesn't make sense to check for collision at the same time you're updating the bullets, because it's possible that some were update while the others aren't yet.

And this is what the GPU is good at. If you have a problem that can be split up into independant sections that don't need to know about each other, then your problem will be far more efficiently solved on the GPU than on the CPU.

That's where drawing objects comes in.



[b]A Journey Of A 3D Object To The Screen[/b]

There are a number of sequential operations required to get an object from memory to the screen. Here, we will examine how this exactly works.

I want you to meet, for a lack of a better name, Bob. He is the cutest cube ever created, and was just loaded into memory using the following code.

[code lang=dbp]load object "bob.x", 1[/code]

[img]bob.png[/img]

Well, at least that's what he [i]should[/i] look like, but the RAM isn't concerned with that. All it cares about are the [b][i]vertices[/i][/b] and their [b][i]attributes[/i][/b], along with some [b][i]texture[/i][/b] lying around somewhere in video memory (if at all).

[img]ram-01.png[/img]

In RAM, the object is nothing more than 36 vertices (on an unoptimised object consisting of independant triangles). They aren't even connected with each other, all they have are certain attributes. One such attribute is the [b]position[/b], which is stored as 3 floating point values and tells us where the vertex is located in [b]object space[/b]. Other attributes are the vertex [b]normal[/b], [b]diffuse[/b], and [b]UV coordinates[/b]. These, however, are all optional, and are defined using the object's flexible vertex format (FVF).

[img]ram-02.png[/img]

You can try this right now if you like. DBP provides you with some tools to edit these vertices after loading an object. Here's an example demonstrating just that:

[code lang=dbp]rem setup screen
sync on
sync rate 60
backdrop on
hide mouse

rem make bob
make object cube 1, 10

rem create memblock from bob and get the header information
make mesh from object 1, 1
make memblock from mesh 1, 1
FVF = memblock dword(1, 0)
vertSize = memblock dword(1, 4)
vertCount = memblock dword(1, 8)

do
	set cursor 0, 0
	print "Bob's specs:"
	print "  FVF: " + str$(FVF)
	print "  vertSize: " + str$(vertSize)
	print "  vertCount: " + str$(vertCount)

	sync
loop[/code]

The Flexible Vertex Format (FVF) should have a value of 274, which basically means that each vertex has the attributes [b]position[/b], [b]normal[/b], and [b]UV coordinate[/b]. I won't go into more detail on FVF since it's not important yet, but you can check out [href=http://forum.thegamecreators.com/?m=forum_view&t=191434&b=1]this link[/href] if want to know more.

Since the position attribute requires 3 floats, the normal attribute requires 3 floats, and the UV coordinates require 2 floats, the total memory size of a single vertex amounts to 28 bytes. This will be the value of [b]vertSize[/b].

Lastly, [b]vertCount[/b] tells us the total amount of vertices stored in memory.

When it's time for an object to be drawn, all of its vertices and its attributes are uploaded to the GPU, and passed to the [b]vertex shader[/b]. At this point, bob is still just a bunch of points. Very important: Bob is located in [b]object space[/b] at this point in time.

The first thing that happens is the GPU will transform all of the vertices into [b]world space[/b]. This effectively places bob into the 3D world at the position he should be, which is determined by the DBP commands [b]position object[/b], [b]rotate object[/b], and [b]scale object[/b]. Those three commands generate what's known as the [b]world matrix[/b], which is also uploaded so the GPU knows how to transform bob into world space.

[img]bob-world.png[/img]

Next, the GPU will transform all vertices into [b]view space[/b]. This effectively places bob relative to where the camera is located and pointing, which is determined by the DBP commands [b]position camera[/b], and [b]rotate camera[/b]. Those commands generate what's known as the [b]view matrix[/b], which is also uploaded so the GPU knows how to transform bob into view space.

[img]bob-view.png[/img]

The GPU now does another transformation on all of Bob's vertices, placing him into [b]projection space[/b]. This effectively places bob into the projection space of the camera, and has the effect of scaling Bob according to how far or how close he is to the camera (in the case of a perspective projection).

[img]bob-projection.png[/img]

At this point, the GPU will do some [b]clipping[/b], discarding any primitives that fall completely outside of the camera's view frustum. This is an optimisation so the pixel shader doesn't have to do as much work.

At this point, the vertex shader has done its job. It outputs the new positions of all of the vertices, and the GPU [b]rasterises[/b] the vertices. Here the vertices are finally [b]connected together[/b] to form actual shapes, and the correct resulting pixel values are determined.

[img]bob-rasterise.png[/img]

At this point, bob consist of a bunch of pixels on the screen, but their colour isn't defined yet. These pixels, just like vertices, have [b]attributes[/b]. To list the most important, each pixel has a [b]colour[/b] and a [b]UV coordinate[/b].

These pixels are passed to the [b]pixel shader[/b].

The pixel shader will go through every pixel and try to determine the final colour. This can include sampling from a [b]texture[/b] by using the UV coordinates, or simply generating a colour on the fly.

The pixel shader outputs the pixels to a [b]render target[/b], which is a buffer located in video memory. After that, the render target can be directly output to the screen, or can be used again in another [b]pass[/b].

And thus, Bob has made it to the screen!

[img]bob.png[/img]

As a DBP programmer, you have the ability to write your own [b]vertex shader[/b] programs, which changes how vertices are tranformed, and you have the ability to write your own [b]pixel shader[/b] programs, which changes how pixels gain their final colour.



[b]Links[/b]

Proceed to the next tutorial here.

TheComet

