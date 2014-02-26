[b][center]TheComet's Shader Tutorial[/center]
[center]06 - Sampling a Texture[/center][/b]

[b]Synopsis[/b]

Up until now, we've only generated pretty rainbows out of our objects. Here you will learn the following.

[b]*[/b] How to declare texture types.
[b]*[/b] What is a sampler?
[b]*[/b] How to use samplers



[b]Declaring texture types[/b]

In order to make use of a texture, two things need to be done.

1) The shader needs to know it exists.
2) A sampler needs to be set up so pixel information can be read from the texture.

In DBP, if you use multiple texture stages, the [b]order in which you declare your textures[/b] is the order in which the stages are used.

To declare a texture, we simply have to use the [b]texture[/b] datatype:
[code]texture texDiffuse;[/code]

A convention I like to follow is to prefix everything with what it is. If you don't do this, things can get pretty confusing and messy further down the road. You may have already noticed the world projection matrix to have the prefix "mat" for "matrix". Here I use "tex" for "texture".

So again, if in DBP you were to texture your object with the following:
[code lang=dbp]load image "foo.png", 1
load image "bar.png", 2
make object plain 1, 10, 10

texture object 1, 0, 1 : rem foo.png is applied to stage 0
texture object 1, 1, 2 : rem bar.png is applied to stage 1[/code]

The following applies to declarations of textures in shaders:
[code]texture texDiffuse; // texDiffuse references "foo.png"
texture texNormal; // texNormal references "bar.png"[/code]



[b]How to set up a sampler[/b]

Now that the shader knows about the textures, we have to set up a [b]sampler[/b] so we can read pixel information from them.

A texture has a limited number of pixels, so what happens when a UV coordinate tries to get information from "in between" the pixels in the texture? This is where [b]samplers[/b] come in.

A [b]sampler[/b] has the ability to convert a texture into an infinitely large resolution by [b]interpolating[/b] the pixels of the texture when trying to access information "in between". The resulting pixel is an average of all surrounding pixels.

There are different types of samplers, and different ways to configure a sampler. We'll be using a [b]2D sampler[/b] (because our texture is 2-dimensional), and the default sampling function uses [b]linear interpolation[/b].

Here's how to declare the sampler:
[code]sampler2D sampDiffuse = sampler_state
{
	Texture = <texDiffuse>;
};[/code]

NOTE: It's possible to have multiple samplers sampling from the same texture. Usually you'll want to have one sampler for every texture declared.



[b]Using the sampler[/b]

The sampler is used in the [b]pixel shader[/b] with the command [b]tex2D[/b], and requires the [b]UV coordinates[/b] we calculated in tutorial 05.

Try modifying your pixel shader to look like the following:
[code]PS_OUTPUT ps_main( PS_INPUT input )
{
        // declare output struct, so we can write output data
        PS_OUTPUT output;

        // sample a colour from the diffuse texture
	float4 diffuse = tex2D( sampDiffuse, input.texCoord );

	// set output pixel to diffuse colour
	output.colour = diffuse;

        // output final colour
        return output;
}[/code]

As you can see, the [b]tex2D[/b] command helps pass the UV coordinate to our sampler [b]sampDiffuse[/b]. This causes it to look at the texture its referencing and sample a colour value from it, at the exact location the UV coordinates specify. The result is saved in [b]diffuse[/b].

After that, the value is simply directly written to the screen through the output struct. You should now get something like the following:

[img]result.png[/img]

Important: Sampling textures is an [b]expensive[/b] process, and should be used sparingly.



[b]Summary[/b]

[b]*[/b] Textures need to be declared in the shader in the exact order they were applied to the DBP object.
[b]*[/b] In order to use the texture, a sampler needs to be set up to reference the texture.
[b]*[/b] Samplers interpolate the pixels of a texture, making it possible to access "in between" pixels of the texture.
[b]*[/b] Samplers are expensive to use.

Congratulations! You have successfully completed the beginner tutorial series, and have written one of the most basic shaders, which does what's known as "ambient shading".

The next series will introduce you to some fundamental lighting techniques to make your objects look a lot sweeter. Please do continue!



[b]Links[/b]

Proceed to the next tutorial here.

TheComet

