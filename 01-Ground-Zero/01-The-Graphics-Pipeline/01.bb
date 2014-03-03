[b][center]TheComet's Shader Tutorial[/center]
[center]01 - Understanding The Graphics Pipeline[/center][/b]

[b]Synopsis[/b]

You will learn the following in this chapter.

[b]*[/b] The basics of what a graphics card is, what it does, and how it does it.
[b]*[/b] What parts of it we can programmatically manipulate through shader programs.
[b]*[/b] When it makes sense to use the GPU and when it makes sense to use the CPU.



[b]What is a grahics card?[/b]

A Graphics Processing Unit (GPU) is a specialised electronic circuit designed to rapidly manipulate and alter memory to accelerate the creation of images in a frame buffer intended for output to a display.

Unlike a CPU, the architecture of a GPU is highly parallelised. A single GPU contains [i]thousands[/i] of cores, with the ability to reach a total processing power of multiple Tflop/s (10¹² floating point operations per second).

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/cuda-core_zpsc8ea9c8d.gif[/img]

You may ask yourself: Why hasn't the CPU been replaced by a GPU yet? The GPU is obviously over a million times faster. The answer is quite simple: Some mathematical problems cannot be solved in parallel efficiently, while some can. The CPU is designed to solve sequential problems, while the GPU is designed to solve parallel problems.

For example, think about the code you wrote in your latest project. Each command you typed in needs to be processed sequentially. One after another. It wouldn't make sense to try and texture an object that hasn't been loaded yet. It wouldn't make sense to try and render an object when you haven't even opened the window yet. It wouldn't make sense for your enemy to search for a path and at the same time try to follow the path, because the path data hasn't finished calculating yet. The path needs to first exist before the enemy can follow it.

There are many things in a program that [i]have[/i] to happen in a particular order, and there's really no way around this.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/sequential-block-diagram_zps2e95ab42.png[/img]

There are some things that [i]can[/i] be parallelised, though. Lets say your game has thousands of bullets that need to be simulated at the same time, because you're writing the next MMOFPS. The bullets don't have to know about each other, all they have to do is travel a certain distance every loop. This makes it possible to update the position of each bullet in parallel, because they are all completely independent of each other.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/parallel-block-diagram_zpsbe9f21cd.png[/img]

Of course, you'd still have to make sure you check for collision [i]after[/i] all of the bullets have been updated, because again, it doesn't make sense to check for collision at the same time you're updating the bullets, because it's possible that some were update while the others aren't yet. So when looking at it from a higher level, you begin to notice that programs are really just a mixture of parallel problems that have to happen in sequence.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/parallelampsequential-block-diagram_zps9a1824c0.png[/img]

With that said, it still wouldn't make sense to calculate the bullts on a GPU. It costs [b]a lot[/b] of time to communicate to the GPU, so even though the GPU could potentially simulate a thousand bullets instantaniously, the time it takes to upload all of the data to the GPU, let it calculate its stuff, and download the results again would take longer than simply doing it directly on the CPU.

We need millions of tasks before it makes sense to use the GPU. That's where drawing objects comes in, because rendering graphics is highly parallelisable.



[b]A Journey Of A 3D Object To The Screen[/b]

There are a number of sequential operations required to get an object from memory to the screen. Here, we will examine how that exactly works.

I want you to meet, for a lack of a better name, Bob. He is the cutest cube ever created, and was just loaded into memory using the following code.
[code lang=dbp]load object "bob.x", 1[/code]

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/cube-1_zpsfd8d9c71.png[/img]

Well, at least that's what he [i]should[/i] look like, but the VRAM isn't concerned with that. All it cares about are the [b]vertices[/b] and their [b]attributes[/b], along with maybe a [b]texture[/b] lying around somewhere in video memory (if at all).

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/ram-01_zpsed05df24.png[/img]

In VRAM, the object is nothing more than 36 vertices (on an unoptimised object consisting of independent triangles). They aren't even connected with each other, all they have are certain attributes. One such attribute is the [b]position[/b], which is stored as 3 floating point values and tells us where the vertex is located in [b]object space[/b]. Other attributes are the vertex [b]normal[/b], [b]diffuse[/b], and [b]UV coordinates[/b]. These, however, are all optional, and are defined using the object's [b]Flexible Vertex Format (FVF)[/b].

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/ram-02_zps1dd18a4b.png[/img]

You can try this right now if you like. DBP provides you with some tools to access and even edit these vertices after loading an object. The following is an example demonstrating just that. This example is located in the folder [b]01-fvf-format[/b]
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

When running the above program, you will notice the Flexible Vertex Format (FVF) will have a value of 274. This number tells us what attributes are being used, and basically means that each vertex has the attributes [b]position[/b], [b]normal[/b], and [b]UV coordinate[/b]. I won't go into more detail on FVF since it's not important yet, but you can check out [href=http://forum.thegamecreators.com/?m=forum_view&t=191434&b=1]this link[/href] if want to know more.

Since the position attribute requires 3 floats, the normal attribute requires 3 floats, and the UV coordinates require 2 floats, and each float consists of 4 bytes, the total memory size of a single vertex amounts to 32 bytes. This will be the value of [b]vertSize[/b].

Lastly, [b]vertCount[/b] tells us the total amount of vertices that are composing the object, which, as mentioned earlier, will be 36 on an unoptimised cube.

When it's time for an object to be drawn, all of its vertices and its attributes are passed to the [b]vertex shader[/b]. At this point, bob is still just a bunch of points located in [b]object space[/b].

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/object-space-vertices_zpsb1cac2d0.png[/img]

The first thing that happens is the GPU will transform all of the vertices into [b]world space[/b]. This effectively places bob into the 3D world at the position the programmed placed him, which is determined by the DBP commands [b]position object[/b], [b]rotate object[/b], and [b]scale object[/b]. Those three commands generate what's known as the [b]world matrix[/b], which is also uploaded so the GPU knows how to transform bob into world space.

In other words, the vertices in RAM never change. Even when you position the object, rotate the object, etc. you aren't actually moving the vertices. You're only telling the GPU how the object was transformed. And if you think about it, that's a good thing, because if you were to actually change the vertices in RAM, the model would begin to distort the more you reposition it, because floating point values have a certain inaccuracy.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/world-transform_zpsb1cac2d0.png[/img]

Next, the GPU will transform all vertices into [b]view space[/b]. This effectively places bob relative to where the camera is located and positioned and pointing, which is determined by the DBP commands [b]position camera[/b], and [b]rotate camera[/b]. Those commands generate what's known as the [b]view matrix[/b], which is also uploaded so the GPU knows how to transform bob into view space.

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/view-transform_zps59944859.png[/img]

The GPU now does another transformation on all of Bob's vertices, placing him into [b]projection space[/b]. This effectively places bob into the projection space of the camera, and has the effect of scaling Bob according to how far or how close he is to the camera (in the case of a perspective projection).

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/projection-transform_zpsf8ac0d6e.png[/img]

Here's the entire process of the vertex shader, again:

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/world-view-projection-transform_zps8a6f3bde.png[/img]

At this point, the vertex shader has done its job. It outputs the new positions of all of the vertices, and the GPU will do some [b]clipping[/b], discarding any primitives that fall completely outside of the camera's view frustum. This is an optimisation so the pixel shader doesn't have to do as much work.

Then the GPU [b]rasterises[/b] the vertices. Here the vertices are finally [b]connected together[/b] to form actual shapes, and the correct resulting pixel values are determined.

This is accomplished by sampling the 3D surfaces from the perspective of a grid, where the grid has the exact dimensions of the render target:

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/rasterise-1_zps7b5f3b94.png[/img]

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/rasterise-2_zps6c281c97.png[/img]

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/rasterise-3_zps2757f640.png[/img]

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/rasterise-4_zps9482698d.png[/img]

Now Bob consist of a bunch of pixels, but their colour isn't defined yet. These pixels, just like vertices, have [b]attributes[/b]. To list the most important, each pixel has a [b]colour[/b] and a [b]UV coordinate[/b].

These pixels are passed to the [b]pixel shader[/b].

The pixel shader will go through every pixel and try to determine the final colour. This can include sampling from a [b]texture[/b] by using the UV coordinates, or simply generating a colour on the fly.

The pixel shader outputs the pixels to a [b]render target[/b], which is a buffer located in video memory. After that, the render target can be directly output to the screen, or can be used again in another [b]render pass[/b].

[img]http://i254.photobucket.com/albums/hh100/TheComet92/shader-tutorial-res/cube-1_zpsfd8d9c71.png[/img]

And thus, Bob has made it to the screen!

As a DBP programmer, you have the ability to write your own [b]vertex shader[/b] programs, which changes how vertices are transformed, and you have the ability to write your own [b]pixel shader[/b] programs, which changes how pixels gain their final colour.

There are hundreds of thousands of vertices and billions of pixels in 3D games. A CPU just would not be able to handle it.



[b]Summary[/b]

[b]*[/b] The GPU is optimised to solve parallel mathematical problems (such as vertex transformations and pixel calculation).
[b]*[/b] We can change the way an object is drawn through vertex and pixel shader programs.



[b]Links[/b]

Proceed to the next tutorial: [href=]02 - Writing Your First Shader[/href]
Proceed to the previous tutorial here: [href=]Master Post[/href]

TheComet

